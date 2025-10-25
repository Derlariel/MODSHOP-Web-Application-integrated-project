package sit.int204.mobileshop.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.dtos.OrderRequestDto;
import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.services.OrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v2/orders")
public class OrderController {
    private OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<OrderResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ofNullable(this.orderService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<List<OrderResponseDto>> createOrder(@RequestBody List<OrderRequestDto> orderDto){
        List<OrderResponseDto> response = orderService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Filter and search orders
     * GET /v2/orders/search?userId=1&keyword=apple&startDate=2025-01-01&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Optional<PageDto<OrderResponseDto>>> searchOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "orderDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Optional<PageDto<OrderResponseDto>> result = orderService.findOrdersWithFilters(
                userId, sellerId, sellerName, brandName, model, keyword,
                startDate, endDate, orderStatus,
                page, size, sortField, sortDirection
        );

        return ResponseEntity.ok(result);
    }
}
