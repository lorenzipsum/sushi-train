package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.menu.MenuItemService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.MoneyYenMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto.MenuItemDtoMapperImpl;
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

import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.*;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.MenuItemController.BASE_URL_MENU_ITEM_CONTROLLER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Import({MenuItemDtoMapperImpl.class, MoneyYenMapperImpl.class})
@WebMvcTest(MenuItemController.class)
@AutoConfigureRestTestClient
class MenuItemControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    MenuItemService service;

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} returns 200 with menu item details")
    void getMenuItem_returns200() {
        // arrange
        var amountInYen = 500;
        var menuItem = MenuItem.create("California Roll", PlateTier.GREEN, MoneyYen.of(amountInYen));
        given(service.getMenuItem(menuItem.getId())).willReturn(menuItem);

        // act
        client.get()
                .uri(BASE_URL_MENU_ITEM_CONTROLLER + "/{id}", menuItem.getId())
                .exchange()
                // assert
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(menuItem.getId().toString())
                .jsonPath("$.name").isEqualTo(menuItem.getName())
                .jsonPath("$.defaultTier").isEqualTo(menuItem.getDefaultTier().toString())
                .jsonPath("$.basePrice").isEqualTo(amountInYen)
                .jsonPath("$.createdAt").exists();

        verify(service).getMenuItem(menuItem.getId());
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} with non-existent id returns 404 ProblemDetail")
    void getMenuItem_notFound_returns404() {
        // arrange
        var nonExistentId = UUID.randomUUID();
        given(service.getMenuItem(nonExistentId))
                .willThrow(new ResourceNotFoundException("Menu", nonExistentId));

        // act
        client.get()
                .uri(BASE_URL_MENU_ITEM_CONTROLLER + "/{id}", nonExistentId)
                .exchange()
                // assert
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Menu not found: " + nonExistentId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_MENU_ITEM_CONTROLLER + "/" + nonExistentId)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI);

        verify(service).getMenuItem(nonExistentId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} with invalid UUID returns 400 ProblemDetail")
    void getMenuItem_invalidUUID_returns400() {
        // arrange
        var invalidId = "invalid-uuid";

        // act
        client.get()
                .uri(BASE_URL_MENU_ITEM_CONTROLLER + "/{id}", invalidId)
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_MENU_ITEM_CONTROLLER + "/" + invalidId)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI);

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} with unexpected error returns 500 ProblemDetail")
    void getMenuItem_serverError_returns500() {
        // arrange
        var menuItemId = UUID.randomUUID();
        given(service.getMenuItem(menuItemId))
                .willThrow(new RuntimeException("Unexpected error"));

        // act
        client.get()
                .uri(BASE_URL_MENU_ITEM_CONTROLLER + "/{id}", menuItemId)
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI);

        verify(service).getMenuItem(menuItemId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items with valid pagination returns 200 with paginated menu items")
    void getAllMenuItems_returns200() {
        // arrange
        var menuItem1 = MenuItem.create("California Roll", PlateTier.GREEN, MoneyYen.of(500));
        var menuItem2 = MenuItem.create("Spicy Tuna Roll", PlateTier.RED, MoneyYen.of(700));
        var pageRequest = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(menuItem1, menuItem2), pageRequest, 2);

        given(service.getAllMenuItems(pageRequest)).willReturn(page);

        // act
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_URL_MENU_ITEM_CONTROLLER)
                        .queryParam("page", "0")
                        .queryParam("size", "2")
                        .build())
                .exchange()
                // assert
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.page.number").isEqualTo(0)
                .jsonPath("$.page.size").isEqualTo(2)
                .jsonPath("$.content[0].id").isEqualTo(menuItem1.getId().toString())
                .jsonPath("$.content[1].id").isEqualTo(menuItem2.getId().toString());

        verify(service).getAllMenuItems(pageRequest);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items with invalid pagination parameters returns 400 ProblemDetail")
    void getAllMenuItems_invalidPagination_returns400() {
        // act
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_URL_MENU_ITEM_CONTROLLER)
                        .queryParam("page", "-1")
                        .queryParam("size", "0")
                        .build())
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("One or more parameters are invalid")
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.errors").exists();

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /api/v1/menu-items with unexpected error returns 500 ProblemDetail")
    void getAllMenuItems_serverError_returns500() {
        // arrange
        var pageRequest = PageRequest.of(0, 10);
        given(service.getAllMenuItems(pageRequest))
                .willThrow(new RuntimeException("Unexpected error"));

        // act
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_URL_MENU_ITEM_CONTROLLER)
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                // assert
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI);

        verify(service).getAllMenuItems(pageRequest);
        verifyNoMoreInteractions(service);
    }
}
