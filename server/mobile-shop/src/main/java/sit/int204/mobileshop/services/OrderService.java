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
        return Optional.ofNullable(modelMapper.map(this.orderRepository.findById(id), OrderResponseDto.class));
    }

    public Optional<PageDto<OrderResponseDto>> findByUserId(long userId,
                                                            Integer page,
                                                            Integer size,
                                                            String sortField,
                                                            String sortDirection) {
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principalObj instanceof UserResponseDto principal) {
            if (!principal.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }        final Sort.Direction dir = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        final String field = sortField == null ? "" : sortField.trim().toLowerCase();
        final boolean sortByTotalAmount = "totalamount".equals(field);

        Sort sort = "orderdate".equals(field)
                ? Sort.by(new Sort.Order(dir, "orderDate"))
                : Sort.by(Sort.Direction.DESC, "orderDate");
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Page<Order> pageResult = orderRepository.findAllByUserAndOrderStatus(user, OrderStatus.COMPLETED, pageable);

    List<OrderResponseDto> enriched = pageResult.getContent().stream()
                .map(order -> {
                    OrderResponseDto dto = new OrderResponseDto();
                    dto.setId(order.getId());
                    dto.setBuyerId(Math.toIntExact(order.getUser().getId()));
                    dto.setOrderDate(order.getOrderDate());
                    dto.setPaymentDate(order.getOrderDate());
                    dto.setShippingAddress(order.getShippingAddress());
                    dto.setOrderNote(order.getOrderNote());
                    dto.setOrderStatus(order.getOrderStatus().name());

                    if (!order.getOrderItems().isEmpty() && order.getOrderItems().get(0).getSaleItem() != null) {
                        var seller = order.getOrderItems().get(0).getSaleItem().getSeller();
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

                    int totalAmount = 0;
                    List<OrderItemDto> items = order.getOrderItems().stream().map(oi -> {
                        OrderItemDto oid = new OrderItemDto();
                        oid.setNo(oi.getNo());
                        if (oi.getSaleItem() != null) {
                            oid.setSaleItemId(oi.getSaleItem().getId().longValue());
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
                        oid.setPrice(oi.getPrice());
                        oid.setQuantity(oi.getQuantity());
                        oid.setDescription(oi.getDescription());
                        oid.setLineTotal(oi.getPrice() * oi.getQuantity());
                        return oid;
                    }).collect(Collectors.toList());
                    dto.setOrderItems(items);
                    totalAmount = items.stream().mapToInt(x -> x.getLineTotal() == null ? 0 : x.getLineTotal()).sum();
                    dto.setTotalAmount(totalAmount);
                    return dto;
                }).collect(Collectors.toList());

        if (sortByTotalAmount) {
            enriched.sort((a, b) -> {
                int av = a.getTotalAmount() == null ? 0 : a.getTotalAmount();
                int bv = b.getTotalAmount() == null ? 0 : b.getTotalAmount();
                return dir.isAscending() ? Integer.compare(av, bv) : Integer.compare(bv, av);
            });
        }

        PageDto<OrderResponseDto> dtoPage = new PageDto<>();
        dtoPage.setContent(enriched);
        dtoPage.setFirst(pageResult.isFirst());
        dtoPage.setLast(pageResult.isLast());
        dtoPage.setPage(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
        dtoPage.setTotalElements((int) pageResult.getTotalElements());
        dtoPage.setTotalPages(pageResult.getTotalPages());
    // Report effective sort applied
    String effectiveField = sortByTotalAmount ? "totalAmount" : "orderDate";
    String effectiveDir = sortByTotalAmount
        ? (dir.isAscending() ? "ASC" : "DESC")
        : ("orderdate".equals(field) ? (dir.isAscending() ? "ASC" : "DESC") : "DESC");
    dtoPage.setSort(effectiveField + ": " + effectiveDir);

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
            dto.setPaymentDate(order.getOrderDate());
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
