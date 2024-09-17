package io.github.defective4.sdr.msg;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class RawMessageSender implements AutoCloseable {

    private final String address;
    private final ZContext context;
    private final Socket socket;
    private final boolean bind;

    public RawMessageSender(String address) {
        this(address, false);
    }

    public RawMessageSender(String address, boolean bind) {
        this.bind = bind;
        context = new ZContext();
        socket = context.createSocket(SocketType.PUSH);
        this.address = address;
    }

    @Override
    public void close() throws Exception {
        context.close();
        socket.close();
    }

    public void sendMessage(MessagePair msg) {
        socket.send(msg.toBytes());
    }

    public void start() {
        if (bind) socket.bind(address);
        else socket.connect(address);
    }

}
