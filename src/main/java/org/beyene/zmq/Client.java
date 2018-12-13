package org.beyene.zmq;

import org.springframework.scheduling.annotation.Scheduled;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Client {

    private final ZContext context;
    private final Poller poller;
    private final List<String> addresses;

    public Client(List<String> addresses) {
        this.addresses = addresses;
        this.context = new ZContext();
        this.poller = context.createPoller(addresses.size());
    }

    @PostConstruct
    public void init() throws Exception {
        addresses.stream()
                .map(this::createSocketAndConnect)
                .forEach(socket -> poller.register(socket, ZMQ.Poller.POLLIN));
    }

    private Socket createSocketAndConnect(String addr) {
        Socket socket = context.createSocket(ZMQ.DEALER);
        socket.setIdentity("CLIENT".getBytes(ZMQ.CHARSET));
        socket.connect(addr);
        return socket;
    }

    @Scheduled(fixedDelay = 500)
    public void send() {
        for (int i = 0; i < poller.getSize(); i++) {
            Socket currentSocket = poller.getSocket(i);
            currentSocket.send(Objects.toString(new Random().nextInt(100)));

            poller.poll(10);
            if (poller.pollin(i)) {
                ZMsg message = ZMsg.recvMsg(currentSocket);
                System.out.println("CLIENT      : " + message);
                message.destroy();
            }
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (context.isClosed())
            context.close();
    }

}
