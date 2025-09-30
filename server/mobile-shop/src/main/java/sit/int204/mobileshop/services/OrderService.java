package sit.int204.mobileshop.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int204.mobileshop.dtos.Order.OrderDto;
import sit.int204.mobileshop.dtos.Order.OrderResponseDto;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.OrderItem;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.exceptions.OutOfStockException;
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
        // 1. new order
        Order order = new Order();
        order.setBuyerId(orderDto.getBuyerId());
        order.setSellerId(orderDto.getSellerId());
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress(orderDto.getShippingAddress());
        order.setOrderNote(orderDto.getOrderNote());

        Order savedOrder = orderRepository.save(order);

        // 2. build order items
        List<OrderItem> orderItems = orderDto.getItems().stream().map(itemDto -> {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId())
                    .orElseThrow(() -> new ItemNotFoundException("SaleItem not found"));

            if (saleItem.getQuantity() < itemDto.getQuantity()) {
                throw new OutOfStockException("Not enough stock for " + saleItem.getModel() + ". Available: " + saleItem.getQuantity());
            }

            // update stock
            saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
            saleItemRepository.save(saleItem);

            // create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setSaleItem(saleItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(saleItem.getPrice());
            orderItem.setNo(itemDto.getNo());
            orderItem.setDescription(itemDto.getDescription());

            return orderItem;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // 3. calculate total price
        int total = orderItems.stream()
                .mapToInt(oi -> oi.getPrice() * oi.getQuantity())
                .sum();

        savedOrder.setTotalPrice(total);
        orderRepository.save(savedOrder);

        // 4. build response dto
        return new OrderResponseDto(savedOrder, orderItems);
    }
}
