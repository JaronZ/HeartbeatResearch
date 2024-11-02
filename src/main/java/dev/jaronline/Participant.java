package dev.jaronline;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Participant {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter the ip address of the person you want to connect to:");
        Scanner scanner = new Scanner(System.in);
        String ipAddress = scanner.next();
        String port = "8080";
        String path = "join";

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://" + ipAddress + ":" + port + "/" + path))
                    .build();
            HttpResponse response = httpClient.send(request, new ParticipantHandler());
            System.out.println(response);
        }

    }

    static class ParticipantHandler implements HttpResponse.BodyHandler<String> {
        @Override
        public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
            HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    (_) -> "Hey!"
            );
        }
    }
}