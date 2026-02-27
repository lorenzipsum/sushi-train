package com.lorenzipsum.sushitrain.backend.interfaces.rest.order;

import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.OrderSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping(path = OrderController.BASE_URL_ORDER_CONTROLLER, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Operations for listing orders")
public class OrderController {
    static final String BASE_URL_ORDER_CONTROLLER = "/api/v1/orders";

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Get all orders",
            description = "Returns a paginated list of all orders (newest first)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PagedModel.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public PagedModel<OrderSummaryDto> getAllOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PagedModel<>(service.getAllOrders(pageable));
    }
}
