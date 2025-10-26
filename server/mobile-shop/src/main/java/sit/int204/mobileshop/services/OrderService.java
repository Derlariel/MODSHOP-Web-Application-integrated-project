package sit.int204.mobileshop.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import sit.int204.mobileshop.specifications.OrderSpecs;

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
    @Transactional
    public Optional<OrderResponseDto> findById(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            Long principalId = principal.getId();
            boolean isBuyer = order.getUser() != null && order.getUser().getId() != null
                    && order.getUser().getId().equals(principalId);
            boolean isSeller = order.getSeller() != null
                    && order.getSeller().getId() != null
                    && order.getSeller().getId().equals(principalId);

            if (isSeller && order.getOrderStatus() == OrderStatus.NEW) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
            }
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
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User ID does not match authenticated user");
            }
            // No role restriction here: buyers should be able to access their own orders
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
            // If buyerId is provided, it must match; if null, we'll use authenticated user
            if (orderDto.getBuyerId() != null && !orderDto.getBuyerId().equals(authenticatedUser.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create order for another user");
            }
            
            // Prevent seller from buying their own items
            if (orderDto.getSellerId() != null && orderDto.getSellerId().equals(authenticatedUser.getId().intValue())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot buy your own items");
            }

            // Guard: items must be provided
            if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order items must not be empty");
            }
            
            // Force buyerId to match authenticated user (override any provided value)
            orderDto.setBuyerId(authenticatedUser.getId().intValue());
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

            // 1) Lock all involved SaleItems for this seller and validate stock atomically
            // Collect locked sale items to ensure consistent update
            final List<SaleItem> lockedItems = orderDto.getItems().stream().map(itemDto -> {
                Integer saleItemId = itemDto.getSaleItemId().intValue();
                SaleItem saleItem = saleItemRepository.findByIdForUpdate(saleItemId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item " + saleItemId + " not found"));

                if (orderDto.getSellerId() != null &&
                        !saleItem.getSeller().getId().equals(orderDto.getSellerId().longValue())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale item does not exist for specified seller");
                }
                return saleItem;
            }).collect(Collectors.toList());

            // ตรวจสอบสต็อกทุกรายการก่อน
            boolean hasInsufficientStock = false;
            for (int i = 0; i < orderDto.getItems().size(); i++) {
                var itemDto = orderDto.getItems().get(i);
                var saleItem = lockedItems.get(i);
                if (saleItem.getQuantity() < itemDto.getQuantity()) {
                    hasInsufficientStock = true;
                    break;
                }
            }

            if (hasInsufficientStock) {
                order.setOrderStatus(OrderStatus.CANCELLED);
            } else {
                // หักสต็อก
                for (int i = 0; i < orderDto.getItems().size(); i++) {
                    var itemDto = orderDto.getItems().get(i);
                    var saleItem = lockedItems.get(i);
                    saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
                    saleItemRepository.save(saleItem);
                }
                order.setOrderStatus(OrderStatus.NEW);
            }

            // Add order items (use the locked sale items)
            for (int i = 0; i < orderDto.getItems().size(); i++) {
                var itemDto = orderDto.getItems().get(i);
                var saleItem = lockedItems.get(i);

                OrderItem item = new OrderItem();
                item.setSaleItem(saleItem);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(saleItem.getPrice());
                item.setDescription(itemDto.getDescription());
                order.addOrderItem(item);
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
                // Show all except cancelled => include NEW and COMPLETED
                pageResult = orderRepository.findAllBySellerIdAndOrderStatusNot(seller.getId(), OrderStatus.CANCELLED, pageable);
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

    /**
     * Filter and search orders with multiple criteria
     * @param userId - Filter by buyer (optional)
     * @param sellerId - Filter by seller (optional)
     * @param buyerName - Search by buyer name (optional)
     * @param sellerName - Search by seller name (optional)
     * @param brandName - Search by brand name (optional)
     * @param model - Search by model (optional)
     * @param keyword - General keyword search (optional)
     * @param startDate - Filter by start date (optional)
     * @param endDate - Filter by end date (optional)
     * @param orderStatus - Filter by order status (optional)
     * @param page - Page number
     * @param size - Page size
     * @param sortField - Sort field
     * @param sortDirection - Sort direction (asc/desc)
     * @return PageDto of OrderResponseDto
     */
    public Optional<PageDto<OrderResponseDto>> findOrdersWithFilters(
            Long userId,
            Long sellerId,
            String buyerName,
            String sellerName,
            String brandName,
            String model,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus orderStatus,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection) {

        // Build specifications
        Specification<Order> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and(OrderSpecs.userEquals(userId));
        }

        if (sellerId != null) {
            spec = spec.and(OrderSpecs.sellerEquals(sellerId));
        }

        if (buyerName != null && !buyerName.isBlank()) {
            spec = spec.and(OrderSpecs.buyerNameContains(buyerName));
        }

        if (sellerName != null && !sellerName.isBlank()) {
            spec = spec.and(OrderSpecs.sellerNameContains(sellerName));
        }

        if (brandName != null && !brandName.isBlank()) {
            spec = spec.and(OrderSpecs.brandNameContains(brandName));
        }

        if (model != null && !model.isBlank()) {
            spec = spec.and(OrderSpecs.modelContains(model));
        }

        if (keyword != null && !keyword.isBlank()) {
            // Use appropriate keyword search based on whether it's a buyer or seller search
            if (sellerId != null) {
                // Seller searching their orders - search by buyer
                spec = spec.and(OrderSpecs.keywordSearchForSeller(keyword));
            } else {
                // Buyer searching their orders - search by seller
                spec = spec.and(OrderSpecs.keywordSearchForBuyer(keyword));
            }
        }

        if (startDate != null || endDate != null) {
            spec = spec.and(OrderSpecs.orderDateBetween(startDate, endDate));
        }

        if (orderStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("orderStatus"), orderStatus));
        }

        // Setup pagination and sorting
        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(new Sort.Order(dir, sortField != null ? sortField : "orderDate"));
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute query
        Page<Order> pageResult = orderRepository.findAll(spec, pageable);

        // Map to DTOs
        List<OrderResponseDto> orders = pageResult.getContent().stream()
                .map(this::buildOrderResponseDto)
                .collect(Collectors.toList());

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(orders);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
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

    /**
     * Get distinct buyer names for a seller's orders
     * @param sellerId - The seller ID
     * @return List of buyer full names
     */
    public List<String> getBuyerNamesForSeller(Long sellerId) {
        return orderRepository.findDistinctBuyerNamesBySellerId(sellerId);
    }

    /**
     * Get distinct seller names for a user's orders
     * @param userId - The user ID
     * @return List of seller full names
     */
    public List<String> getSellerNamesForUser(Long userId) {
        return orderRepository.findDistinctSellerNamesByUserId(userId);
    }

}
