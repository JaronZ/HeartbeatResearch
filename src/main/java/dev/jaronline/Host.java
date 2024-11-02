package dev.jaronline;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class Host {
    private static final List<InetSocketAddress> addresses = new ArrayList<>();
    private static final Map<InetSocketAddress, HeartbeatTimer> heartbeatTimers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/join", new HostJoinHandler(addresses, heartbeatTimers));
        server.createContext("/heartbeat", new HostHeartbeatHandler(heartbeatTimers));
        server.setExecutor(null);
        server.start();
        System.out.println("Server has started on: " + server.getAddress());
    }

    static class HostJoinHandler implements HttpHandler {
        private final List<InetSocketAddress> addresses;
        private final Map<InetSocketAddress, HeartbeatTimer> heartbeatTimers;

        public HostJoinHandler(List<InetSocketAddress> addresses, Map<InetSocketAddress, HeartbeatTimer> heartbeatTimers) {
            this.addresses = addresses;
            this.heartbeatTimers = heartbeatTimers;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Join received");
            addresses.add(exchange.getLocalAddress());
            createResponse(exchange);
            createHeartbeatTimer(exchange);
        }

        private void createResponse(HttpExchange exchange) throws IOException {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void createHeartbeatTimer(HttpExchange exchange) {
            HeartbeatTimer timer = new HeartbeatTimer();
            InetSocketAddress address = exchange.getLocalAddress();
            timer.scheduleHeartbeatTask(() -> {
                System.out.println("Removing address");
                heartbeatTimers.remove(address);
                System.out.println(heartbeatTimers);
            });
            heartbeatTimers.put(address, timer);
        }
    }

    static class HostHeartbeatHandler implements HttpHandler {
        private final Map<InetSocketAddress, HeartbeatTimer> heartbeatTimers;

        public HostHeartbeatHandler(Map<InetSocketAddress, HeartbeatTimer> heartbeatTimers) {
            this.heartbeatTimers = heartbeatTimers;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Heartbeat received");
            if (!heartbeatTimers.containsKey(exchange.getLocalAddress())) {
                String response = "Forbidden";
                exchange.sendResponseHeaders(403, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

            HeartbeatTimer timer = heartbeatTimers.get(exchange.getLocalAddress());
            timer.restart();

            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class HeartbeatTimer extends Timer {
        private TimerTask task;
        private Runnable action;

        public void scheduleHeartbeatTask(Runnable action) {
            this.action = action;
            this.task = new TimerTask() {
                @Override
                public void run() {
                    action.run();
                }
            };
            schedule(task, 10 * 1000);
        }

        public void restart() {
            if (this.task != null && this.action != null) {
                this.task.cancel();
                scheduleHeartbeatTask(action);
            }
        }
    }
}
