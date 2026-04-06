package com.lorenzipsum.sushitrain.backend.interfaces.rest.version;

import com.lorenzipsum.sushitrain.backend.infrastructure.version.VersionInfo;
import com.lorenzipsum.sushitrain.backend.infrastructure.version.VersionInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@WebMvcTest(VersionController.class)
@AutoConfigureRestTestClient
class VersionControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    VersionInfoService versionInfoService;

    @Test
    @DisplayName("GET /api/version returns 200 with version metadata and no auth requirement")
    void getVersion_returns200WithoutAuth() {
        given(versionInfoService.getVersionInfo()).willReturn(new VersionInfo(
                "Sushi Train Backend",
                "0.2.0",
                "abc1234",
                "2026-03-24T21:12:00Z",
                "test"
        ));

        client.get()
                .uri(VersionController.BASE_URL_VERSION_CONTROLLER)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.service").isEqualTo("Sushi Train Backend")
                .jsonPath("$.version").isEqualTo("0.2.0")
                .jsonPath("$.commitSha").isEqualTo("abc1234")
                .jsonPath("$.buildTime").isEqualTo("2026-03-24T21:12:00Z")
                .jsonPath("$.environment").isEqualTo("test");

        verify(versionInfoService).getVersionInfo();
        verifyNoMoreInteractions(versionInfoService);
    }

    @Test
    @DisplayName("GET /api/version returns nullable metadata fields when unavailable")
    void getVersion_returnsNullsForUnavailableMetadata() {
        given(versionInfoService.getVersionInfo()).willReturn(new VersionInfo(
                "Sushi Train Backend",
                "0.2.0",
                null,
                null,
                null
        ));

        client.get()
                .uri(VersionController.BASE_URL_VERSION_CONTROLLER)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("Sushi Train Backend")
                .jsonPath("$.version").isEqualTo("0.2.0")
                .jsonPath("$.commitSha").isEmpty()
                .jsonPath("$.buildTime").isEmpty()
                .jsonPath("$.environment").isEmpty();

        verify(versionInfoService).getVersionInfo();
        verifyNoMoreInteractions(versionInfoService);
    }

        @Test
        @DisplayName("GET / returns 404 ProblemDetail instead of 500")
        void rootPath_returns404ProblemDetail() {
                client.get()
                                .uri("/")
                                .exchange()
                                .expectStatus().isNotFound()
                                .expectHeader().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                                .expectBody()
                                .jsonPath("$.title").isEqualTo("Resource not found")
                                .jsonPath("$.status").isEqualTo(404)
                                .jsonPath("$.type").isEqualTo("https://api.sushitrain/errors/not-found")
                                .jsonPath("$.instance").isEqualTo("/");

                verifyNoMoreInteractions(versionInfoService);
        }
}
