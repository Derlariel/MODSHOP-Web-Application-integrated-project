package sit.int204.mobileshop.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.services.OrderService;

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
}
