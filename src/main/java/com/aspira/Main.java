package com.aspira;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int threadsCount = 3;
        if (args.length >= 1) {
            threadsCount = Integer.parseInt(args[0]);
        }
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        try (client; executorService) {
            Map<String, Sport> sports = getSports(client);
            Map<String, League> leagues = sports.values().stream()
                    .flatMap(mapper -> mapper.getLeagueList().stream())
                    .collect(Collectors.toMap(League::getId, v -> v));
            List<CompletableFuture<Void>> futures = leagues.values().stream()
                    .map(league -> getEvents(client, league).thenAccept(league::setEventList))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> print(sports))
                    .join();
        }
    }

    static void print(Map<String, Sport> sports) {
        sports.forEach((k, v) -> System.out.println(v));
    }

    static Map<String, Sport> getSports(HttpClient client) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leonbets.com/api-2/betline/sports?ctag=en-US"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        List<Map<String, Object>> sports = JsonPath.parse(jsonString).read("$.*");
        return sports.stream().map(mapper -> {
            String sportId = mapper.get("id").toString();
            String sportName = mapper.get("name").toString();
            List<League> topLeagueList = getTopLeagues(jsonString, sportId);
            return new Sport(sportId, sportName, topLeagueList);
        }).filter(sport -> sport.getLeagueList().size() > 0).collect(Collectors.toMap(Sport::getId, value -> value));
    }

    static List<League> getTopLeagues(String jsonString, String sportId) {
        List<Map<String, Object>> topLeagues = JsonPath.parse(jsonString)
                .read("$[?(@.id == " + sportId + ")]..leagues[?(@.top == true)]");
        return topLeagues.stream().map(m -> new League(m.get("id").toString(), m.get("name").toString())).toList();
    }

    static CompletableFuture<List<Event>> getEvents(HttpClient httpClient, League league) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id="
                        + league.getId() + "&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
            DocumentContext documentContext = JsonPath.parse(response);
            List<Map<String, Object>> firstTwoEvents = documentContext.read("$.events[:2]");
            return firstTwoEvents.stream().map(mapper -> {
                String eventId = mapper.get("id").toString();
                String eventName = mapper.get("name").toString();
                Long kickoff = Long.valueOf(mapper.get("kickoff").toString());
                List<Market> marketList = getMarkets(documentContext, eventId);
                return new Event(eventId, eventName, kickoff, marketList);
            }).collect(Collectors.toList());
        }).exceptionally(e -> {
            System.out.println("Error during getting events: " + e.getMessage());
            return List.of();
        });
    }

    static List<Market> getMarkets(DocumentContext documentContext, String eventId) {
        List<Market> marketList;
        List<Map<String, Object>> markets = documentContext.read("$.events[?(@.id == " + eventId + ")].markets.*");
        marketList = markets.stream().map(mapEntry -> {
            String marketId = mapEntry.get("id").toString();
            String marketName = mapEntry.get("name").toString();
            List<Runner> runnerList = getRunners(documentContext, eventId, marketId);
            return new Market(marketId, marketName, runnerList);
        }).toList();
        return marketList;
    }

    static List<Runner> getRunners(DocumentContext documentContext, String eventId, String marketId) {
        List<Runner> runnerList;
        List<Map<String, Object>> runners = documentContext
                .read("$.events[?(@.id == " + eventId + ")].markets[?(@.id == " + marketId + ")].runners.*");
        runnerList = runners.stream().map(runnerMapEntry -> {
            String runnerId = runnerMapEntry.get("id").toString();
            String runnerName = runnerMapEntry.get("name").toString();
            Double ratio = Double.valueOf(runnerMapEntry.get("price").toString());
            return new Runner(runnerId, runnerName, ratio);
        }).collect(Collectors.toList());
        return runnerList;
    }
}