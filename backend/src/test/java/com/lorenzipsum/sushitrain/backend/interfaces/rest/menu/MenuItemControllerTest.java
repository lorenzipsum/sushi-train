package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
    void getMenuItem() throws Exception {
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
}