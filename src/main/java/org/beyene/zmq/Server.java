package org.beyene.zmq;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Server implements Runnable {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ZContext context;
    private final Poller poller;
    private final String address;
    private final Function<Integer, Integer> function;

    public Server(String address, Function<Integer, Integer> function) {
        this.address = address;
        this.context = new ZContext();
        this.poller = context.createPoller(1);
        this.function = function;
    }

    @PostConstruct
    public void init() throws Exception {
        Socket socket = context.createSocket(ZMQ.ROUTER);
        socket.bind(address);
        poller.register(socket, ZMQ.Poller.POLLIN);

        executor.submit(this);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(0)) {
                Socket currentSocket = poller.getSocket(0);
                ZMsg request = ZMsg.recvMsg(currentSocket);
                System.out.printf("%s: %s%n", address, request);

                if (request == null || request.size() != 2)
                    continue;

                handleRequest(request, currentSocket);
            }
        }
    }

    private void handleRequest(ZMsg request, Socket currentSocket) {
        ZFrame identity = request.poll();
        String message = request.poll().getString(ZMQ.CHARSET);

        ZMsg response = new ZMsg();
        response.add(identity.getData()); // specify receiver by sending identity
        response.add(new byte[0]); // empty delimiter frame for dealer socket
        response.add(message);
        response.add(Objects.toString(function.apply(Integer.parseInt(message))));

        response.send(currentSocket);
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (context.isClosed())
            context.close();
        executor.shutdownNow();
    }
}
