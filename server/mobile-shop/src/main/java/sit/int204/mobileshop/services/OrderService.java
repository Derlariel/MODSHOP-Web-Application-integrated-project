package sit.int204.mobileshop.services;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.dtos.PageDto;
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
    private final SaleItemService saleItemService;
    private final SaleItemRepository saleItemRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ModelMapper modelMapper, ListMapper listMapper, SaleItemService saleItemService, SaleItemRepository saleItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.listMapper = listMapper;
        this.saleItemService = saleItemService;
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

    @Transactional
    public List<OrderResponseDto> createOrder(List<OrderResponseDto> orderDtos) {
        User buyer = userRepository.findById(
                ((UserResponseDto) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()).getId()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderDtos.stream().map(orderDto -> {
            Order order = modelMapper.map(orderDto, Order.class);
            order.setUser(buyer);

            List<OrderItem> orderItems = orderDto.getOrderItems().stream().map(itemDto -> {
                OrderItem item = modelMapper.map(itemDto, OrderItem.class);
                SaleItem saleItem = saleItemService.getSaleItemByIdOld(Math.toIntExact(itemDto.getSaleItemId()));
                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
                saleItemRepository.save(saleItem);

                item.setSaleItem(saleItem);
                item.setOrder(order);
                return item;
            }).toList();

            order.setOrderItems(orderItems);
            return order;
        }).toList();

        orderRepository.saveAll(orders);
        return listMapper.mapList(orders, OrderResponseDto.class, modelMapper);
    }



}
