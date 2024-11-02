package dev.jaronline;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Host {
    private static final List<InetSocketAddress> addresses = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/join", new HostHandler(addresses));
        server.setExecutor(null);
        server.start();
        System.out.println("Server has started on: " + server.getAddress());
    }

    static class HostHandler implements HttpHandler {
        private final List<InetSocketAddress> addresses;

        public HostHandler(List<InetSocketAddress> addresses) {
            this.addresses = addresses;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addresses.add(exchange.getLocalAddress());
            System.out.println(addresses);

            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
