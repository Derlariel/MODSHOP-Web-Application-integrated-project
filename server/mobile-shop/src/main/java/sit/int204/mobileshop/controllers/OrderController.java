package sit.int204.mobileshop.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.OrderRequestDto;
import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.services.OrderService;

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
}
