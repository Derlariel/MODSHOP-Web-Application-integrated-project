package sit.int204.mobileshop.utils;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import sit.int204.mobileshop.dtos.PageDto;

import java.util.List;

public class ListMapper {
    private static final ListMapper listMapper = new ListMapper();

    private ListMapper() {
    }

    public static ListMapper getInstance() {
        return listMapper;
    }

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass, ModelMapper modelMapper) {
        return source.stream().map(entity -> modelMapper.map(entity, targetClass)).toList();
    }

    public <S, T> List<T> toListDto(List<S> source, Class<T> targetClass, ModelMapper modelMapper) {
        return mapList(source, targetClass, modelMapper);
    }

    public <S, T> PageDto<T> toPageDTO(Page<S> source, Class<T> targetClass, ModelMapper modelMapper) {
        PageDto<T> pageDto = new PageDto<>();

        pageDto.setContent(mapList(source.getContent(), targetClass, modelMapper));

        pageDto.setLast(source.isLast());
        pageDto.setFirst(source.isFirst());
        pageDto.setTotalPages(source.getTotalPages());
        pageDto.setSize(source.getSize());
        pageDto.setPage(source.getNumber());
        pageDto.setTotalElements((int) source.getTotalElements());

        if (source.getSort().isSorted()) {
            StringBuilder sortString = new StringBuilder();
            source.getSort().forEach(order -> {
                if (sortString.length() > 0)
                    sortString.append(", ");
                sortString.append(order.getProperty()).append(": ").append(order.getDirection());
            });
            pageDto.setSort(sortString.toString());
        } else {
            pageDto.setSort("unsorted");
        }

        return pageDto;
    }
}