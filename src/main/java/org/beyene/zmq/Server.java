package org.beyene.zmq;

import org.beyene.zmq.message.Dto;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

                if (request == null || request.size() != 2)
                    continue;

                handleRequest(request, currentSocket);
            }
        }
    }

    private void handleRequest(ZMsg request, Socket currentSocket) {
        ZFrame identity = request.poll();

        Dto.Request req = null;
        try {
            req = Dto.Request.parseFrom(request.poll().getData());
            request.destroy();
        } catch (Exception e) {
            return;
        }

        System.out.printf("%s: %n%s%n", address, req);

        ZMsg response = new ZMsg();
        response.add(identity.getData()); // specify receiver by sending identity
        response.add(new byte[0]); // empty delimiter frame for dealer socket

        Dto.Response res = Dto.Response.newBuilder()
                .setType(Dto.Type.COMPUTE)
                .setOperand(req.getOperand())
                .setResult(function.apply(req.getOperand()))
                .build();
        response.add(res.toByteArray());

        response.send(currentSocket);
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (context.isClosed())
            context.close();
        executor.shutdownNow();
    }
}
