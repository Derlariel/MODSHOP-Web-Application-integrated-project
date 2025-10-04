package sit.int204.mobileshop.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.dtos.OrderRequestDto;
import sit.int204.mobileshop.dtos.OrderItemDto;
import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SellerDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.OrderItem;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.repositories.OrderRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.repositories.UserRepository;
import sit.int204.mobileshop.utils.ListMapper;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ListMapper listMapper;
    private final SaleItemRepository saleItemRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ModelMapper modelMapper, ListMapper listMapper, SaleItemRepository saleItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.listMapper = listMapper;
        this.saleItemRepository = saleItemRepository;
    }
    public Optional<OrderResponseDto> findById(long id) {
        return Optional.ofNullable(modelMapper.map(this.orderRepository.findById(id), OrderResponseDto.class));
    }

    public Optional<PageDto<OrderResponseDto>> findByUserId(long userId,
                                                            Integer page,
                                                            Integer size,
                                                            String sortField,
                                                            String sortDirection) {
        if (sortField == null || sortField.isBlank()) sortField = "id";
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortField)));
        Page<Order> pageResult =  orderRepository.findAllByUser(userRepository.findById(userId).get(),pageable);
        return Optional.ofNullable(listMapper.toPageDTO(pageResult, OrderResponseDto.class, modelMapper));
    }

//    @Transactional
//    public List<OrderResponseDto> createOrder(List<OrderResponseDto> orderDtos) {
//        User buyer = userRepository.findById(
//                ((UserResponseDto) SecurityContextHolder.getContext()
//                        .getAuthentication().getPrincipal()).getId()
//        ).orElseThrow(() -> new RuntimeException("User not found"));
//
//        List<Order> orders = orderDtos.stream().map(orderDto -> {
//            Order order = modelMapper.map(orderDto, Order.class);
//            order.setUser(buyer);
//
//            List<OrderItem> orderItems = orderDto.getOrderItems().stream().map(itemDto -> {
//                OrderItem item = modelMapper.map(itemDto, OrderItem.class);
//                SaleItem saleItem = saleItemService.getSaleItemByIdOld(Math.toIntExact(itemDto.getSaleItemId()));
//                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
//                saleItemRepository.save(saleItem);
//
//                item.setSaleItem(saleItem);
//                item.setOrder(order);
//                return item;
//            }).toList();
//
//            order.setOrderItems(orderItems);
//            return order;
//        }).toList();
//
//        orderRepository.saveAll(orders);
//        return listMapper.mapList(orders, OrderResponseDto.class, modelMapper);
//    }
//
//

    @Transactional
    public List<OrderResponseDto> createOrder(List<OrderRequestDto> orderDtos) {
        User buyer = userRepository.findById(
                ((UserResponseDto) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()).getId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        List<Order> orders = orderDtos.stream().map(orderDto -> {
            Order order = new Order();
            order.setUser(buyer);
            order.setShippingAddress(orderDto.getShippingAddress());
            order.setOrderNote(orderDto.getOrderNote());
            order.setOrderDate(Instant.now());
            order.setOrderStatus(OrderStatus.COMPLETED);

            orderDto.getItems().forEach(itemDto -> {
                SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId().intValue())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Sale item not found"));

                if (saleItem.getQuantity() < itemDto.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Out of stock" + itemDto.getSaleItemId());
                }
                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());

                OrderItem item = new OrderItem();
                item.setSaleItem(saleItem);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(saleItem.getPrice());
                item.setDescription(itemDto.getDescription());

                order.addOrderItem(item);; // bidirectional sync
            });

            return order;
        }).collect(Collectors.toList());

        orderRepository.saveAll(orders);

        return orders.stream().map(order -> {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setId(order.getId());
            dto.setBuyerId(Math.toIntExact(order.getUser().getId()));
            dto.setOrderDate(order.getOrderDate());
            dto.setShippingAddress(order.getShippingAddress());
            dto.setOrderNote(order.getOrderNote());
            dto.setOrderStatus(order.getOrderStatus().name());

            List<OrderItemDto> items = order.getOrderItems().stream().map(oi -> {
                OrderItemDto oid = new OrderItemDto();
                oid.setNo(oi.getNo());
                if (oi.getSaleItem() != null && oi.getSaleItem().getId() != null) {
                    oid.setSaleItemId(oi.getSaleItem().getId().longValue());
                }
                oid.setPrice(oi.getPrice());
                oid.setQuantity(oi.getQuantity());
                oid.setDescription(oi.getDescription());
                return oid;
            }).collect(Collectors.toList());
            dto.setOrderItems(items);

            if (!order.getOrderItems().isEmpty()) {
                var firstItem = order.getOrderItems().get(0);
                var seller = firstItem.getSaleItem() != null ? firstItem.getSaleItem().getSeller() : null;
                if (seller != null) {
                    SellerDto sellerDto = new SellerDto();
                    sellerDto.setId(seller.getId());
                    sellerDto.setEmail(seller.getEmail());
                    sellerDto.setFullName(seller.getFullName());
                    sellerDto.setUserType(seller.getUserType());
                    sellerDto.setNickName(seller.getNickName());
                    dto.setSeller(sellerDto);
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

}
