package io.github.defective4.sdr.rds;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class RDSReceiver implements AutoCloseable {

    private final String address;
    private boolean allowDuplicateRadiotextUpdates, allowDuplicateStationUpdates;
    private final boolean bind;
    private boolean closed;
    private final ZContext ctx;
    private String lastRadiotext, lastStation;
    private final List<RDSListener> listeners = new CopyOnWriteArrayList<>();
    private final Socket socket;
    private String storedRadiotext, storedStation;

    public RDSReceiver(String address) {
        this(address, false);
    }

    public RDSReceiver(String address, boolean bind) {
        this.bind = bind;
        ctx = new ZContext();
        socket = ctx.createSocket(SocketType.PULL);
        this.address = address;
    }

    public void addListener(RDSListener ls) {
        listeners.add(ls);
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
        ctx.close();
    }

    public List<RDSListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public boolean isAllowDuplicateRadiotextUpdates() {
        return allowDuplicateRadiotextUpdates;
    }

    public boolean isAllowDuplicateStationUpdates() {
        return allowDuplicateStationUpdates;
    }

    public void removeListener(RDSListener ls) {
        listeners.remove(ls);
    }

    public void reset() {
        lastRadiotext = null;
        lastStation = null;
        storedRadiotext = null;
        storedStation = null;
    }

    public void setAllowDuplicateRadiotextUpdates(boolean allowDuplicateRadiotextUpdates) {
        this.allowDuplicateRadiotextUpdates = allowDuplicateRadiotextUpdates;
    }

    public void setAllowDuplicateStationUpdates(boolean allowDuplicateStationUpdates) {
        this.allowDuplicateStationUpdates = allowDuplicateStationUpdates;
    }

    public void start() throws IOException {
        if (bind) socket.bind(address);
        else socket.connect(address);
        while (!closed) {
            byte[] recv = socket.recv();
            if (recv.length < 11) continue;
            byte id = recv[9];
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(recv))) {
                in.skip(11);
                int len = in.readShort();
                byte[] data = new byte[len];
                in.readFully(data);
                String str = new String(data).trim();
                switch (id) {
                    case 0: {
                        listeners.forEach(ls -> ls.programInfoUpdated(str));
                        break;
                    }
                    case 1: {
                        if (str.equals(storedStation)) {
                            if (!allowDuplicateStationUpdates && str.equals(lastStation)) break;
                            lastStation = str;
                            listeners.forEach(ls -> ls.stationUpdated(str));
                        }
                        storedStation = str;
                        break;
                    }
                    case 2: {
                        listeners.forEach(ls -> ls.programTypeUpdated(str));
                        break;
                    }
                    case 4: {
                        if (str.equals(storedRadiotext)) {
                            if (!allowDuplicateRadiotextUpdates && str.equals(lastRadiotext)) break;
                            lastRadiotext = str;
                            listeners.forEach(ls -> ls.radiotextUpdated(str));
                        }
                        storedRadiotext = str;
                        break;
                    }
                    case 5: {
                        listeners.forEach(ls -> ls.clockUpdated(str));
                        break;
                    }
                    case 3: {
                        RDSFlags flags = RDSFlags.parse(str);
                        listeners.forEach(ls -> ls.flagsUpdated(flags));
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

        }
    }

}
