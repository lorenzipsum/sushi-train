package com.lorenzipsum.sushitrain.backend.interfaces.rest;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.MoneyYenMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.PlateController;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.CreatePlateRequest;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.PlateDtoMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.CREATED;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.*;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.PlateController.BASE_URL_PLATE_CONTROLLER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Import({PlateDtoMapperImpl.class, MoneyYenMapperImpl.class})
@WebMvcTest(PlateController.class)
@AutoConfigureRestTestClient
class PlateControllerTest {
    @Autowired
    RestTestClient client;

    @MockitoBean
    PlateService service;

    @Test
    @DisplayName("GET /api/v1/plates/{id} with existing id should return 200 and plate")
    void getPlate_returns200() {
        // arrange
        UUID menuItemId = UUID.randomUUID();
        PlateTier tier = PlateTier.GREEN;
        MoneyYen price = MoneyYen.of(300);
        Instant expiresAt = Instant.now().plusSeconds(600);
        Plate plate = Plate.create(menuItemId, tier, price, expiresAt);
        UUID plateId = plate.getId();
        given(service.getPlate(plateId)).willReturn(plate);

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}", plateId)
                .exchange()
                // assert
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(plateId.toString())
                .jsonPath("$.menuItemId").isEqualTo(menuItemId.toString())
                .jsonPath("$.tierSnapshot").isEqualTo(tier.toString())
                .jsonPath("$.priceAtCreation").isEqualTo(price.amount())
                .jsonPath("$.createdAt").isEqualTo(plate.getCreatedAt().toString())
                .jsonPath("$.expiresAt").isEqualTo(expiresAt.toString())
                .jsonPath("$.status").isEqualTo(CREATED.toString());

        verify(service).getPlate(plateId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates/{id} with non-existing id should return 404 ProblemDetail")
    void getPlate_returns404_problemDetail() {
        // arrange
        UUID nonExistentId = UUID.randomUUID();
        given(service.getPlate(nonExistentId))
                .willThrow(new ResourceNotFoundException("Plate", nonExistentId));

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}", nonExistentId)
                .exchange()
                // assert
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Plate not found: " + nonExistentId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + nonExistentId)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI);

        verify(service).getPlate(nonExistentId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates/{id} with invalid UUID should return 400 ProblemDetail")
    void getPlate_returns400_problemDetail_onInvalidUuid() {
        // arrange
        String invalidId = "invalid-uuid";

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}", invalidId)
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + invalidId)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI);

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates/{id} when service throws should return 500 ProblemDetail")
    void getPlate_returns500_problemDetail_onUnexpected() {
        // arrange
        UUID id = UUID.randomUUID();
        given(service.getPlate(id)).willThrow(new RuntimeException("boom"));

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}", id)
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + id);

        verify(service).getPlate(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates with valid body should create and return 201")
    void createPlate_returns201() {
        // arrange
        var request = new CreatePlateRequest(
                UUID.randomUUID(),
                PlateTier.RED,
                500,
                Instant.now().plusSeconds(3600)
        );
        var plate = Plate.create(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        );

        given(service.createPlate(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        )).willReturn(plate);

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                // assert
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(plate.getId().toString())
                .jsonPath("$.menuItemId").isEqualTo(request.menuItemId().toString())
                .jsonPath("$.tierSnapshot").isEqualTo(request.tierSnapshot().toString())
                .jsonPath("$.priceAtCreation").isEqualTo(request.priceAtCreation())
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.expiresAt").isEqualTo(request.expiresAt().toString())
                .jsonPath("$.status").isEqualTo(CREATED.toString());

        verify(service).createPlate(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        );
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates with invalid body should return 400 ProblemDetail")
    void createPlate_returns400_problemDetail_onValidationError() {
        // arrange: invalid json missing menuItemId, expiresAt
        String invalidJson = """
                {"tierSnapshot":"RED","priceAtCreation":500}
                """;

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidJson)
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER);

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates when service throws should return 500 ProblemDetail")
    void createPlate_returns500_problemDetail_onUnexpected() {
        // arrange
        var request = new CreatePlateRequest(
                UUID.randomUUID(),
                PlateTier.RED,
                500,
                Instant.now().plusSeconds(3600)
        );
        given(service.createPlate(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        )).willThrow(new RuntimeException("boom"));

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER);

        verify(service).createPlate(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        );
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates/{id}/expire with existing id should return 200 and expired plate")
    void expirePlate_returns200() {
        // arrange
        UUID menuItemId = UUID.randomUUID();
        PlateTier tier = PlateTier.RED;
        MoneyYen price = MoneyYen.of(400);
        Instant expiresAt = Instant.now().plusSeconds(1200);
        Plate plate = Plate.create(menuItemId, tier, price, expiresAt);
        UUID plateId = plate.getId();
        plate.expire();
        given(service.expirePlate(plateId)).willReturn(plate);

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}/expire", plateId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                // assert
                .jsonPath("$.id").isEqualTo(plateId.toString())
                .jsonPath("$.menuItemId").isEqualTo(menuItemId.toString())
                .jsonPath("$.tierSnapshot").isEqualTo(tier.toString())
                .jsonPath("$.priceAtCreation").isEqualTo(price.amount())
                .jsonPath("$.createdAt").isEqualTo(plate.getCreatedAt().toString())
                .jsonPath("$.expiresAt").isEqualTo(expiresAt.toString())
                .jsonPath("$.status").isEqualTo(PlateStatus.EXPIRED.toString());

        verify(service).expirePlate(plateId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates/{id}/expire with non-existing id should return 404 ProblemDetail")
    void expirePlate_returns404_problemDetail() {
        // arrange
        UUID id = UUID.randomUUID();
        given(service.expirePlate(id)).willThrow(new ResourceNotFoundException("Plate", id));

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}/expire", id)
                .exchange()
                // assert
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Plate not found: " + id)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + id + "/expire")
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI);

        verify(service).expirePlate(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates/{id}/expire with invalid UUID should return 400 ProblemDetail")
    void expirePlate_returns400_problemDetail_onInvalidUuid() {
        // arrange
        String invalidId = "not-a-uuid";

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}/expire", invalidId)
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + invalidId + "/expire");

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /api/v1/plates/{id}/expire when service throws should return 500 ProblemDetail")
    void expirePlate_returns500_problemDetail_onUnexpected() {
        // arrange
        UUID id = UUID.randomUUID();
        given(service.expirePlate(id)).willThrow(new RuntimeException("boom"));

        // act
        client.post()
                .uri(BASE_URL_PLATE_CONTROLLER + "/{id}/expire", id)
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER + "/" + id + "/expire");

        verify(service).expirePlate(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates?page=0&size=2 should return 200 and paged plates")
    void getAllPlates_returns200() {
        // arrange
        var plate1 = Plate.create(UUID.randomUUID(), PlateTier.GREEN, MoneyYen.of(300), Instant.now().plusSeconds(600));
        var plate2 = Plate.create(UUID.randomUUID(), PlateTier.RED, MoneyYen.of(500), Instant.now().plusSeconds(1200));
        var pageRequest = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(plate1, plate2), pageRequest, 2);
        given(service.getAllPlates(pageRequest)).willReturn(page);

        // act
        client.get()
                .uri(uri -> uri.path(BASE_URL_PLATE_CONTROLLER).queryParam("page", "0").queryParam("size", "2").build())
                .exchange()
                // assert
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.page.totalElements").isEqualTo(2)
                .jsonPath("$.page.number").isEqualTo(0)
                .jsonPath("$.page.size").isEqualTo(2)
                .jsonPath("$.content[0].id").isEqualTo(plate1.getId().toString())
                .jsonPath("$.content[1].id").isEqualTo(plate2.getId().toString());

        verify(service).getAllPlates(pageRequest);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates with missing params should use defaults and return 200")
    void getAllPlates_returns200_withDefaultParamsWhenMissing() {
        // arrange
        Plate plate1 = Plate.create(UUID.randomUUID(), PlateTier.GREEN, MoneyYen.of(300), Instant.now().plusSeconds(600));
        Plate plate2 = Plate.create(UUID.randomUUID(), PlateTier.RED, MoneyYen.of(500), Instant.now().plusSeconds(1200));
        PageRequest defaultPageRequest = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(plate1, plate2), defaultPageRequest, 2);
        given(service.getAllPlates(defaultPageRequest)).willReturn(page);

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                // assert
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.page.totalElements").isEqualTo(2)
                .jsonPath("$.page.number").isEqualTo(0)
                .jsonPath("$.page.size").isEqualTo(10);

        verify(service).getAllPlates(defaultPageRequest);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates when service throws should return 500 ProblemDetail")
    void getAllPlates_returns500_problemDetail_onUnexpected() {
        // arrange
        PageRequest defaultPageRequest = PageRequest.of(0, 10);
        given(service.getAllPlates(defaultPageRequest)).willThrow(new RuntimeException("boom"));

        // act
        client.get()
                .uri(BASE_URL_PLATE_CONTROLLER)
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER);

        verify(service).getAllPlates(defaultPageRequest);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates?page=abc should return 400 ProblemDetail (invalid parameter)")
    void getAllPlates_returns400_problemDetail_onWrongType() {
        // act
        client.get()
                .uri(uri -> uri.path(BASE_URL_PLATE_CONTROLLER).queryParam("page", "abc").queryParam("size", "10").build())
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER);

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/plates?page=-1&size=0 should return 400 ProblemDetail (validation failed)")
    void getAllPlates_returns400_problemDetail_onValidationError() {
        // act
        client.get()
                .uri(uri -> uri.path(BASE_URL_PLATE_CONTROLLER).queryParam("page", "-1").queryParam("size", "0").build())
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("One or more parameters are invalid")
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_PLATE_CONTROLLER)
                .jsonPath("$.errors").exists();

        verifyNoInteractions(service);
    }
}
