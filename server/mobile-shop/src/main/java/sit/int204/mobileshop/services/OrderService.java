package sit.int204.mobileshop.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.*;
import sit.int204.mobileshop.repositories.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleItemImageRepository saleItemImageRepository;
    private final ModelMapper modelMapper;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ModelMapper modelMapper,
                        SaleItemRepository saleItemRepository,
                        SaleItemImageRepository saleItemImageRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.saleItemRepository = saleItemRepository;
        this.saleItemImageRepository = saleItemImageRepository;
    }

    // Find order by ID (with buyer/seller validation)
    public Optional<OrderResponseDto> findById(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            Long principalId = principal.getId();
            boolean isBuyer = order.getUser() != null && order.getUser().getId().equals(principalId);
            boolean isSeller = order.getOrderItems().stream()
                    .anyMatch(oi -> oi.getSaleItem().getSeller().getId().equals(principalId));

            if (!isBuyer && !isSeller) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }
        return Optional.of(buildOrderResponseDto(order));
    }

    // Get orders by buyer
    public Optional<PageDto<OrderResponseDto>> findByUserId(long userId,
                                                            Integer page,
                                                            Integer size,
                                                            String sortField,
                                                            String sortDirection) {
        validateUserAccess(userId);
        Pageable pageable = createPageRequest(page, size, sortField, sortDirection);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<Order> pageResult = orderRepository.findAllByUser(user, pageable);
        return Optional.of(toPageDto(pageResult));
    }

    // Get orders by buyer and status
    public Optional<PageDto<OrderResponseDto>> findByUserIdAndStatus(long userId,
                                                                     OrderStatus status,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String sortField,
                                                                     String sortDirection) {
        validateUserAccess(userId);
        Pageable pageable = createPageRequest(page, size, sortField, sortDirection);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<Order> pageResult = orderRepository.findAllByUserAndOrderStatus(user, status, pageable);
        return Optional.of(toPageDto(pageResult));
    }

    //  Create Order (with stock check + transactional integrity)
    @Transactional
    public List<OrderResponseDto> createOrder(List<OrderRequestDto> orderDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserResponseDto)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        UserResponseDto principal = (UserResponseDto) authentication.getPrincipal();
        User buyer = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //  Validate orders structure and ownership
        for (OrderRequestDto orderDto : orderDtos) {
            if (orderDto.getBuyerId() != null &&
                    !orderDto.getBuyerId().equals(principal.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Order buyer ID does not match authenticated user");
            }
            if (orderDto.getSellerId() != null &&
                    orderDto.getSellerId().equals(principal.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Buyer and seller cannot be the same user");
            }
            if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order items must not be empty");
            }
        }

        // Validate stock availability before processing any order
        validateStockBeforeOrder(orderDtos);

        // Create orders
        List<Order> orders = orderDtos.stream().map(orderDto -> {
            Order order = new Order();
            order.setUser(buyer);
            order.setShippingAddress(orderDto.getShippingAddress());
            order.setOrderNote(orderDto.getOrderNote());
            order.setOrderDate(Instant.now());
            order.setOrderStatus(OrderStatus.COMPLETED);

            // Add order items and reduce stock atomically
            orderDto.getItems().forEach(itemDto -> {
                SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId().intValue())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Sale item " + itemDto.getSaleItemId() + " not found"));

                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
                saleItemRepository.save(saleItem);

                OrderItem orderItem = new OrderItem();
                orderItem.setSaleItem(saleItem);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(saleItem.getPrice());
                orderItem.setDescription(itemDto.getDescription());
                order.addOrderItem(orderItem);
            });

            return order;
        }).collect(Collectors.toList());

        orderRepository.saveAll(orders);
        return orders.stream().map(this::buildOrderResponseDto).collect(Collectors.toList());
    }

    //  Helper: Validate stock before placing order
    private void validateStockBeforeOrder(List<OrderRequestDto> orderDtos) {
        for (OrderRequestDto orderDto : orderDtos) {
            for (var itemDto : orderDto.getItems()) {
                SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId().intValue())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Sale item " + itemDto.getSaleItemId() + " not found"));

                if (saleItem.getQuantity() < itemDto.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for " + saleItem.getModel() +
                                    " (available: " + saleItem.getQuantity() +
                                    ", requested: " + itemDto.getQuantity() + ")");
                }
            }
        }
    }

    //  Helper: Check user authorization
    private void validateUserAccess(long userId) {
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            if (!principal.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }
    }

    // Helper: Paging and sorting config
    private Pageable createPageRequest(Integer page, Integer size, String sortField, String sortDirection) {
        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(new Sort.Order(dir, sortField != null ? sortField : "orderDate"));
        return PageRequest.of(page == null ? 0 : page, size == null ? 10 : size, sort);
    }

    // Helper: Map Page<Order> → PageDto<OrderResponseDto>
    private PageDto<OrderResponseDto> toPageDto(Page<Order> pageResult) {
        List<OrderResponseDto> content = pageResult.getContent().stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(content);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
        dtoPage.setTotalElements((int) pageResult.getTotalElements());
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setSort(pageResult.getSort().toString());
        return dtoPage;
    }

    // Helper: Map Order → DTO
    private OrderResponseDto buildOrderResponseDto(Order order) {
        OrderResponseDto dto = modelMapper.map(order, OrderResponseDto.class);
        dto.setPaymentDate(order.getOrderDate());

        // seller info
        if (!order.getOrderItems().isEmpty()) {
            var firstItem = order.getOrderItems().get(0);
            var seller = firstItem.getSaleItem().getSeller();
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

        // items
        List<OrderItemDto> items = order.getOrderItems().stream()
                .map(this::buildOrderItemDto)
                .collect(Collectors.toList());
        dto.setOrderItems(items);

        return dto;
    }

    private OrderItemDto buildOrderItemDto(OrderItem oi) {
        OrderItemDto oid = new OrderItemDto();
        oid.setNo(oi.getNo());
        oid.setPrice(oi.getPrice());
        oid.setQuantity(oi.getQuantity());
        oid.setDescription(oi.getDescription());

        if (oi.getSaleItem() != null) {
            var saleItem = oi.getSaleItem();
            oid.setSaleItemId(saleItem.getId().longValue());
            if (saleItem.getBrand() != null) {
                oid.setBrandName(saleItem.getBrand().getName());
            }
            oid.setModel(saleItem.getModel());
            oid.setColor(saleItem.getColor());
            oid.setStorageGb(saleItem.getStorageGb());

            var images = saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItem.getId());
            if (images != null && !images.isEmpty()) {
                oid.setImage(images.get(0).getFileName());
            }
        }
        return oid;
    }
}
