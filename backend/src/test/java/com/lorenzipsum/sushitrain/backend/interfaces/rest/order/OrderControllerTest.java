package com.lorenzipsum.sushitrain.backend.interfaces.rest.order;

import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.application.view.OrderLineView;
import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.order.dto.OrderSummaryDtoMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.PROBLEM_400_VALIDATION_FAILED_TITLE;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.PROBLEM_400_VALIDATION_FAILED_URI;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.PROBLEM_500_TITLE;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.PROBLEM_500_URI;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.order.OrderController.BASE_URL_ORDER_CONTROLLER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@WebMvcTest(OrderController.class)
@AutoConfigureRestTestClient
@Import(OrderSummaryDtoMapperImpl.class)
class OrderControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    OrderService orderService;

    @Test
    @DisplayName("GET /api/v1/orders - OK")
    void getAllOrders_ok() {
        UUID orderId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        var content = List.of(
                new OrderSummaryView(
                        orderId,
                        seatId,
                        OrderStatus.OPEN,
                        createdAt,
                        null,
                        List.of(new OrderLineView("Salmon Nigiri", PlateTier.GREEN, 100)),
                        100
                )
        );
        var response = new PageImpl<>(content, pageable, content.size());

        given(orderService.getAllOrders(pageable)).willReturn(response);

        client.get()
                .uri(BASE_URL_ORDER_CONTROLLER)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].orderId").isEqualTo(orderId.toString())
                .jsonPath("$.content[0].seatId").isEqualTo(seatId.toString())
                .jsonPath("$.content[0].status").isEqualTo(OrderStatus.OPEN.name())
                .jsonPath("$.content[0].createdAt").isEqualTo(createdAt.toString())
                .jsonPath("$.content[0].lines.length()").isEqualTo(1)
                .jsonPath("$.content[0].lines[0].menuItemName").isEqualTo("Salmon Nigiri")
                .jsonPath("$.content[0].lines[0].plateTier").isEqualTo(PlateTier.GREEN.name())
                .jsonPath("$.content[0].lines[0].price").isEqualTo(100)
                .jsonPath("$.content[0].totalPrice").isEqualTo(100)
                .jsonPath("$.page.number").isEqualTo(0)
                .jsonPath("$.page.size").isEqualTo(20);

        verify(orderService).getAllOrders(pageable);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("GET /api/v1/orders - Invalid Pagination")
    void getAllOrders_invalidPagination() {
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_URL_ORDER_CONTROLLER)
                        .queryParam("page", "-1")
                        .queryParam("size", "0")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.detail").isEqualTo("One or more parameters are invalid")
                .jsonPath("$.errors").exists();

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("GET /api/v1/orders - Unexpected Error")
    void getAllOrders_unexpectedError() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        given(orderService.getAllOrders(pageable)).willThrow(new RuntimeException("boom"));

        client.get()
                .uri(BASE_URL_ORDER_CONTROLLER)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.instance").isEqualTo(BASE_URL_ORDER_CONTROLLER);

        verify(orderService).getAllOrders(pageable);
        verifyNoMoreInteractions(orderService);
    }
}
