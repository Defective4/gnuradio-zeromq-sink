package io.github.defective4.sdr.msg;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class RawMessageReceiver implements AutoCloseable {

    private final String address;
    private final boolean bind;
    private boolean closed;
    private final ZContext ctx;
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();
    private final Socket socket;

    public RawMessageReceiver(String address) {
        this(address, false);
    }

    public RawMessageReceiver(String address, boolean bind) {
        this.bind = bind;
        ctx = new ZContext();
        socket = ctx.createSocket(SocketType.PULL);
        this.address = address;
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
        ctx.close();
    }

    public List<MessageListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    public void start() throws IOException {
        if (bind) socket.bind(address);
        else socket.connect(address);
        while (!closed) {
            byte[] recv = socket.recv();
            if (recv.length > 0) {
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(recv))) {
                    byte id = in.readByte();
                    in.skip(1);
                    String key = null;
                    if (id == 7) {
                        byte[] keyBytes = new byte[in.readShort()];
                        in.readFully(keyBytes);
                        key = new String(keyBytes, StandardCharsets.UTF_8);
                    }
                    byte type = in.readByte();
                    Object value = null;
                    switch (type) {
                        case 2 -> {
                            byte[] valBytes = new byte[in.readShort()];
                            in.readFully(valBytes);
                            value = new String(valBytes, StandardCharsets.UTF_8);
                        }
                        case 3 -> value = in.readInt();
                        case 4 -> value = in.readDouble();
                        default -> {}
                    }
                    if (value != null) {
                        for (MessageListener ls : listeners) ls.messageReceived(new MessagePair(key, value));
                    }
                }
            }
        }
    }

}
