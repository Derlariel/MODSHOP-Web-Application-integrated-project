package sit.int204.mobileshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int204.mobileshop.dtos.Order.OrderDto;
import sit.int204.mobileshop.dtos.Order.OrderItemDto;
import sit.int204.mobileshop.dtos.Order.OrderResponseDto;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.OrderItem;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.repositories.Orders.OrderItemRepository;
import sit.int204.mobileshop.repositories.Orders.OrderRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SaleItemRepository saleItemRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderDto orderDto) {
        // 1. สร้าง order
        Order order = new Order();
        order.setBuyerId(orderDto.getBuyerId());
        order.setStatus(Order.Status.PENDING);
        Order saveOrder = orderRepository.save(order);

        // 2. loop cart items
        List<OrderItem> orderItems = orderDto.getItems().stream().map(itemDto -> {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId())
                    .orElseThrow(() -> new RuntimeException("SaleItem not found"));

            if (saleItem.getQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + saleItem.getModel());
            }

            saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
            saleItemRepository.save(saleItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(saveOrder);
            orderItem.setSaleItem(saleItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(saleItem.getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // 3. map ไป response dto
        return new OrderResponseDto(order.getId(), order.getBuyerId(), order.getStatus().name(), orderItems);
    }

}
