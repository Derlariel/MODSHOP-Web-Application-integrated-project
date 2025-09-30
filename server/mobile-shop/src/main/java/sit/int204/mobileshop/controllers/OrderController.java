package sit.int204.mobileshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int204.mobileshop.dtos.Order.OrderDto;
import sit.int204.mobileshop.dtos.Order.OrderResponseDto;
import sit.int204.mobileshop.services.OrderService;

@RestController
@RequestMapping("/v2/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderDto orderDto){
        OrderResponseDto response = orderService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
