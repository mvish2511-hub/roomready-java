package com.roomready;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Start-up seeds once - the app started twice on the same database still has 60 rooms, not 120.*/
class SeedTwiceTest {

    @TempDir
    Path dir;

    private static ConfigurableApplicationContext start(String databaseUrl) {
        return new SpringApplicationBuilder(RoomReadyApplication.class)
                .properties("spring.datasource.url=" + databaseUrl, "server.port=0")
                .run();
    }

     private static JsonNode board(ConfigurableApplicationContext app) throws Exception {
        String port = app.getEnvironment().getProperty("local.server.port");
        HttpResponse<String> r = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(URI.create(                                       "http://127.0.0.1:" + port + "/properties/harbour-view/rooms"))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        assertThat(r.statusCode()).isEqualTo(200);
        return new ObjectMapper().readTree(r.body());
    }

    @Test
    void aSecondStartLoadsNothingAgain() throws Exception {
        String url = "jdbc:h2:file:" + dir.resolve("roomready").toString().replace('\\', '/');
        try (ConfigurableApplicationContext first = start(url)) {
            assertThat(board(first).path("rooms")).hasSize(60);
        }
        try (ConfigurableApplicationContext second = start(url)) {
            JsonNode board = board(second);
            assertThat(board.path("rooms")).hasSize(60);
            assertThat(board.path("dirty").asInt()).isZero();
        }
    }
}
