package com.fluency.api;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

@SpringBootTest
class FluencyApiApplicationTests {

    static MockWebServer aiMockServer;

    @BeforeAll
    static void startMockServer() throws IOException {
        aiMockServer = new MockWebServer();
        aiMockServer.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        aiMockServer.shutdown();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("ai-orchestrator.url", () -> aiMockServer.url("/").toString());
    }

    @Test
    void contextLoads() {
    }
}
