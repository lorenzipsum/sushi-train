package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.menu.MenuItemService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.MoneyYenMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto.MenuItemDtoMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuItemController.class)
@Import({MenuItemDtoMapperImpl.class, MoneyYenMapperImpl.class})
class MenuItemControllerTest {

    public static final String BASE_URL = "/api/v1/menu-items";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    MenuItemService service;

    @Test
    void getMenuItem_returns200() throws Exception {
        // arrange
        var amountInYen = 500;
        var menuItem = MenuItem.create("California Roll", PlateTier.GREEN, MoneyYen.of(amountInYen));
        given(service.getMenuItem(menuItem.getId())).willReturn(menuItem);

        // act & assert
        mockMvc.perform(get(BASE_URL + "/{id}", menuItem.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").value(menuItem.getId().toString()))
                .andExpect(jsonPath("name").value(menuItem.getName()))
                .andExpect(jsonPath("defaultTier").value(menuItem.getDefaultTier().toString()))
                .andExpect(jsonPath("basePrice").value(amountInYen))
                .andExpect(jsonPath("createdAt").exists());
    }

    @Test
    void getMenuItem_notFound_returns404() throws Exception {
        // arrange
        var nonExistentId = UUID.randomUUID();
        given(service.getMenuItem(nonExistentId))
                .willThrow(new ResourceNotFoundException("Menu", nonExistentId));

        // act & assert
        mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Menu not found: " + nonExistentId))
                .andExpect(jsonPath("$.instance").value("/api/v1/menu-items/" + nonExistentId))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/not-found"));
    }

    @Test
    void getMenuItem_invalidUUID_returns400() throws Exception {
        // arrange
        var invalidId = "invalid-uuid";

        // act & assert
        mockMvc.perform(get(BASE_URL + "/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Invalid parameter"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Parameter 'id' must be a UUID"))
                .andExpect(jsonPath("$.instance").value("/api/v1/menu-items/" + invalidId))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/invalid-parameter"));
    }

    @Test
    void getMenuItems_serverError_returns500() throws Exception {
        // arrange
        var menuItemId = UUID.randomUUID();
        given(service.getMenuItem(menuItemId))
                .willThrow(new RuntimeException("Unexpected error"));

        // act & assert
        mockMvc.perform(get(BASE_URL + "/{id}", menuItemId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Unexpected server error"))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/internal"));
    }

    @Test
    void getAllMenuItems_returns200() throws Exception {
        // arrange
        var menuItem1 = MenuItem.create("California Roll", PlateTier.GREEN, MoneyYen.of(500));
        var menuItem2 = MenuItem.create("Spicy Tuna Roll", PlateTier.RED, MoneyYen.of(700));
        var pageRequest = PageRequest.of(0, 2);
        var page = new PageImpl<>(List.of(menuItem1, menuItem2), pageRequest, 2);

        given(service.getAllMenuItems(pageRequest)).willReturn(page);

        // act & assert
        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.content[0].id").value(menuItem1.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(menuItem2.getId().toString()));
    }

    @Test
    void getAllMenuItems_invalidPagination_returns400() throws Exception {
        // act & assert
        mockMvc.perform(get(BASE_URL).param("page", "-1").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more parameters are invalid"))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/validation-failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllMenuItems_serverError_returns500() throws Exception {
        // arrange
        var pageRequest = PageRequest.of(0, 10);
        given(service.getAllMenuItems(pageRequest))
                .willThrow(new RuntimeException("Unexpected error"));

        // act & assert
        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Unexpected server error"))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/internal"));
    }
}