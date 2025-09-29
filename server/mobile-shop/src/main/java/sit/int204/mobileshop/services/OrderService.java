package sit.int204.mobileshop.services;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sit.int204.mobileshop.dtos.OrderResponseDto;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.repositories.OrderRepository;
import sit.int204.mobileshop.repositories.UserRepository;
import sit.int204.mobileshop.utils.ListMapper;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ListMapper listMapper;
    public OrderService(OrderRepository orderRepository, UserRepository userRepository,  ModelMapper modelMapper, ListMapper listMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.listMapper = listMapper;
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

}
