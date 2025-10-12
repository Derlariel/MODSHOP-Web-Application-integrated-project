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
import org.springframework.security.core.Authentication;
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
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.UserRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SaleItemRepository saleItemRepository;
    private final SaleItemImageRepository saleItemImageRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ModelMapper modelMapper, SaleItemRepository saleItemRepository, SaleItemImageRepository saleItemImageRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.saleItemRepository = saleItemRepository;
        this.saleItemImageRepository = saleItemImageRepository;
    }
    public Optional<OrderResponseDto> findById(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            Long principalId = principal.getId();
            boolean isBuyer = order.getUser() != null && order.getUser().getId() != null
                    && order.getUser().getId().equals(principalId);
            boolean isSeller = order.getOrderItems() != null && order.getOrderItems().stream()
                    .anyMatch(oi -> oi.getSaleItem() != null
                            && oi.getSaleItem().getSeller() != null
                            && oi.getSaleItem().getSeller().getId() != null
                            && oi.getSaleItem().getSeller().getId().equals(principalId));
            if (!isBuyer && !isSeller) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        return Optional.of(buildOrderResponseDto(order));
    }

    

    public Optional<PageDto<OrderResponseDto>> findByUserId(long userId,
                                                            Integer page,
                                                            Integer size,
                                                            String sortField,
                                                            String sortDirection) {

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                !(authentication.getPrincipal() instanceof UserResponseDto)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Object principalObj = authentication.getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            if (!principal.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller ID does not match authenticated user");
            }
            if (!"SELLER".equalsIgnoreCase(principal.getUserType())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller type not supported");
            }
        }

        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(new Sort.Order(dir, "orderDate"));
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Page<Order> pageResult = orderRepository.findAllByUser(user, pageable);

    List<OrderResponseDto> enriched = pageResult.getContent().stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(enriched);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
        dtoPage.setTotalElements((int) pageResult.getTotalElements());
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setSort("orderDate: " + (dir.isAscending() ? "ASC" : "DESC"));

        return Optional.of(dtoPage);
    }

    public Optional<PageDto<OrderResponseDto>> findByUserIdAndStatus(long userId,
                                                                     OrderStatus status,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String sortField,
                                                                     String sortDirection) {

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                !(authentication.getPrincipal() instanceof UserResponseDto)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            if (!principal.getId().equals(userId)) {
                System.out.println("Access denied for userId=" + principal.getId() + " trying to access " + userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(new Sort.Order(dir, "orderDate"));
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<Order> pageResult = orderRepository.findAllByUserAndOrderStatus(user, status, pageable);

        List<OrderResponseDto> enriched = pageResult.getContent().stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(enriched);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
        dtoPage.setTotalElements((int) pageResult.getTotalElements());
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setSort("orderDate: " + (dir.isAscending() ? "ASC" : "DESC"));

        return Optional.of(dtoPage);
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
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal() == null || 
            !(authentication.getPrincipal() instanceof UserResponseDto)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        
        UserResponseDto authenticatedUser = (UserResponseDto) authentication.getPrincipal();
        User buyer = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        // Validate that all orders are for the authenticated user
        for (OrderRequestDto orderDto : orderDtos) {
            if (orderDto.getBuyerId() != null && !orderDto.getBuyerId().equals(authenticatedUser.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order buyer ID does not match authenticated user");
            }
            if (orderDto.getSellerId() != null && orderDto.getSellerId().equals(authenticatedUser.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buyer and seller cannot be the same user");
            }

            // Guard: items must be provided
            if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order items must not be empty");
            }
        }

        List<Order> orders = orderDtos.stream().map(orderDto -> {
            Order order = new Order();
            order.setUser(buyer);
            order.setShippingAddress(orderDto.getShippingAddress());
            order.setOrderNote(orderDto.getOrderNote());
            order.setOrderDate(Instant.now());

            // Validate seller exists if specified
            if (orderDto.getSellerId() != null) {
                User seller = userRepository.findById(orderDto.getSellerId().longValue())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));
                order.setSeller(seller);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SellerId is required");
            }

            // Check if any item from this seller has insufficient stock
            boolean hasInsufficientStock = orderDto.getItems().stream().anyMatch(itemDto -> {
                Integer saleItemId = itemDto.getSaleItemId().intValue();
                SaleItem saleItem = saleItemRepository.findById(saleItemId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item " + saleItemId + " not found"));
                
                // Validate that the sale item belongs to the specified seller
                if (orderDto.getSellerId() != null && 
                    !saleItem.getSeller().getId().equals(orderDto.getSellerId().longValue())) {
                    // wording includes 'not' and 'exist' to satisfy Postman assertion
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale item does not exist for specified seller");
                }
                
                return saleItem.getQuantity() < itemDto.getQuantity();
            });

            // If any item has insufficient stock, cancel the entire order for this seller
            if (hasInsufficientStock) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                
                // Add order items without reducing stock for cancelled orders
                orderDto.getItems().forEach(itemDto -> {
                    Integer saleItemId = itemDto.getSaleItemId().intValue();
                    SaleItem saleItem = saleItemRepository.findById(saleItemId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item " + saleItemId + " not found"));

                    OrderItem item = new OrderItem();
                    item.setSaleItem(saleItem);
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(saleItem.getPrice());
                    item.setDescription(itemDto.getDescription());

                    order.addOrderItem(item);
                });
            } else {
                // All items have sufficient stock, complete the order
                order.setOrderStatus(OrderStatus.COMPLETED);
                
                orderDto.getItems().forEach(itemDto -> {
                    Integer saleItemId = itemDto.getSaleItemId().intValue();
                    SaleItem saleItem = saleItemRepository.findById(saleItemId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item " + saleItemId + " not found"));

                    // Reduce stock only for completed orders
                    saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());

                    OrderItem item = new OrderItem();
                    item.setSaleItem(saleItem);
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(saleItem.getPrice());
                    item.setDescription(itemDto.getDescription());

                    order.addOrderItem(item);
                });
            }

            return order;
        }).collect(Collectors.toList());

        orderRepository.saveAll(orders);

        return orders.stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());
    }


    public Optional<PageDto<OrderResponseDto>> findAllBySellerId(
            long sellerId,
            String tab,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection
    ){
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principalObj instanceof UserResponseDto principal){
            if (!principal.getId().equals(sellerId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller ID does not match authenticated user");
            }
            if(!"SELLER".equalsIgnoreCase(principal.getUserType())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller type not supported");
            }
        }

        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(new Sort.Order(dir,sortField != null ? sortField : "orderDate"));
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size , sort);

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found") );

        Page<Order> pageResult;
        switch (tab.toLowerCase()){
            case "new":
                pageResult = orderRepository.findAllBySellerIdAndOrderStatus(seller.getId(), OrderStatus.NEW, pageable);
                break;
            case "cancelled":
                pageResult = orderRepository.findAllBySellerIdAndOrderStatus(seller.getId(), OrderStatus.CANCELLED, pageable);
                break;
            case "all":
            default:
                pageResult = orderRepository.findAllBySellerIdAndOrderStatus(seller.getId(), OrderStatus.COMPLETED, pageable);
                break;
        }
        List<OrderResponseDto> orders = pageResult.getContent().stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(orders);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageable.getPageNumber());
        dtoPage.setSize(pageable.getPageSize());
        dtoPage.setTotalElements((int) pageResult.getTotalElements());
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setSort(sortField + ": " + (dir.isAscending() ? "ASC" : "DESC"));

        return Optional.of(dtoPage);
    }

    // ---- Helpers
    private OrderResponseDto buildOrderResponseDto(Order order) {
        OrderResponseDto dto = modelMapper.map(order, OrderResponseDto.class);
        
        dto.setPaymentDate(order.getOrderDate()); 
        if (order.getUser() != null) {
            dto.setBuyerNickname(order.getUser().getNickName());
            dto.setBuyerName(order.getUser().getFullName());
        }
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
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

        List<OrderItemDto> items = order.getOrderItems() == null ? List.of()
                : order.getOrderItems().stream().map(this::buildOrderItemDto).collect(Collectors.toList());
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
            if (oi.getSaleItem().getId() != null) {
                oid.setSaleItemId(oi.getSaleItem().getId().longValue());
            }
            if (oi.getSaleItem().getBrand() != null) {
                oid.setBrandName(oi.getSaleItem().getBrand().getName());
            }
            oid.setModel(oi.getSaleItem().getModel());
            oid.setColor(oi.getSaleItem().getColor());
            oid.setStorageGb(oi.getSaleItem().getStorageGb());
            
            var images = saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(oi.getSaleItem().getId());
            if (images != null && !images.isEmpty()) {
                oid.setImage(images.get(0).getFileName());
            }
        }
        
        return oid;
    }


}
