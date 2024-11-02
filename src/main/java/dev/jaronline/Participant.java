package dev.jaronline;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Participant {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter the ip address of the person you want to connect to:");
        Scanner scanner = new Scanner(System.in);
        String ipAddress = scanner.next();
        String port = "8080";
        String path = "join";
        String path2 = "heartbeat";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://" + ipAddress + ":" + port + "/" + path))
                .build();
        HttpResponse response = httpClient.send(request, new ParticipantHandler());
        String message = (String) response.body();
        System.out.println(message);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                HttpRequest request1 = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://" + ipAddress + ":" + port + "/" + path2))
                        .build();
                try {
                    HttpResponse response1 = httpClient.send(request1, new ParticipantHandler());
                    String message1 = (String) response1.body();
                    System.out.println(message1);
                } catch (IOException | InterruptedException exception) {
                    throw new RuntimeException(exception);
                }

            }
        }, 5*1000, 5*1000);

    }

    static class ParticipantHandler implements HttpResponse.BodyHandler<String> {
        @Override
        public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
            HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

            if (responseInfo.statusCode() != 200) {
                return HttpResponse.BodySubscribers.mapping(
                        upstream,
                        _ -> "Nee, ga weg!"
                );
            }

            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    _ -> "Hey!"
            );
        }
    }
}