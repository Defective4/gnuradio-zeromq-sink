package io.github.defective4.sdr.msg;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class RawMessageSender implements AutoCloseable {

    private final String address;
    private final ZContext context;
    private final Socket socket;

    public RawMessageSender(String address) {
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
        socket.bind(address);
    }

}
