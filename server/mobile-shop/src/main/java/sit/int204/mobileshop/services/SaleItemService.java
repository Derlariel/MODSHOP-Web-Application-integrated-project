package sit.int204.mobileshop.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.utils.ListMapper;
import org.springframework.data.domain.*;
import sit.int204.mobileshop.dtos.PageDto;
import java.util.Optional;

import java.util.List;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAllByOrderByCreatedOnAsc();
    }

    public PageDto<SaleItemDto> getAllSaleItemsPage(Integer page,
            Integer size,
            List<String> filterBrands,
            String sortField,
            String sortDirection) {

        if(sortField == null || sortField.trim().isEmpty()) {
            sortField = "createdOn";
        }


        Sort.Direction direction;

        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<SaleItem> saleItemPage = null;
        

        if (filterBrands == null || filterBrands.isEmpty() || filterBrands.contains("[]")) {
            saleItemPage = saleItemRepository.findAll(pageable);
        } else {
            saleItemPage = saleItemRepository.findAllFilter(pageable, filterBrands);
        }
                
        return listMapper.toPageDTO(saleItemPage, SaleItemDto.class, modelMapper);
    }

    public SaleItem getSaleItemById(Integer id) {
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    @Transactional
    public SaleItemDetailDto createSaleItem(SaleItemRequestDto dtoItem) {

        if (dtoItem.getBrand() == null || dtoItem.getBrand().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID must not be null.");
        }
        if (dtoItem.getModel() == null || dtoItem.getModel().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model must not be blank.");
        }
        if (dtoItem.getPrice() == null || dtoItem.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be non-negative.");
        }

        if (dtoItem.getQuantity() == null || dtoItem.getQuantity() < 0) {
            dtoItem.setQuantity(1);
        }


        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());


        SaleItem item = new SaleItem();
        item.setBrand(brand);
        item.setModel(dtoItem.getModel().trim());
        item.setDescription(dtoItem.getDescription() == null ? null : dtoItem.getDescription().trim());
        item.setPrice(dtoItem.getPrice());
        item.setRamGb(dtoItem.getRamGb() != null ? dtoItem.getRamGb() : null);
        item.setScreenSizeInch(dtoItem.getScreenSizeInch());
        item.setStorageGb(dtoItem.getStorageGb() != null ? dtoItem.getStorageGb() : null);
        item.setQuantity(dtoItem.getQuantity());
        item.setColor(
                dtoItem.getColor() != null && !dtoItem.getColor().trim().isEmpty() ? dtoItem.getColor().trim() : null);


        SaleItem savedItem = saleItemRepository.saveAndFlush(item);
        entityManager.refresh(savedItem);


        SaleItemDetailDto result = modelMapper.map(savedItem, SaleItemDetailDto.class);
        result.setBrandName(brand.getName());
        return result;
    }


    public SaleItemDetailDto updateSaleItemById(Integer id, SaleItemRequestDto dtoItem) {
        SaleItem existingItem = getSaleItemById(id);

        if (dtoItem.getBrand() == null || dtoItem.getBrand().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID must not be null.");
        }

        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());

        if (dtoItem.getQuantity() == null || dtoItem.getQuantity() < 0) {
            dtoItem.setQuantity(1);
        }


        existingItem.setBrand(brand);
        existingItem.setModel(dtoItem.getModel() != null ? dtoItem.getModel().trim() : null);
        existingItem.setDescription(dtoItem.getDescription() != null ? dtoItem.getDescription().trim() : null);
        existingItem.setPrice(dtoItem.getPrice());
        existingItem.setRamGb(dtoItem.getRamGb() != null ? dtoItem.getRamGb() : null);
        existingItem.setScreenSizeInch(dtoItem.getScreenSizeInch());
        existingItem.setQuantity(dtoItem.getQuantity());
        existingItem.setStorageGb(dtoItem.getStorageGb() != null ? dtoItem.getStorageGb() : null);
        existingItem.setColor(
                dtoItem.getColor() != null && !dtoItem.getColor().trim().isEmpty() ? dtoItem.getColor().trim() : null);

        SaleItem updatedItem = saleItemRepository.save(existingItem);
        SaleItemDetailDto result = modelMapper.map(updatedItem, SaleItemDetailDto.class);
        result.setBrandName(brand.getName());
        return result;
    }

    public void deleteSaleItemById(Integer id) {
        saleItemRepository.delete(getSaleItemById(id));
    }

    public void deleteAllForTest() {
        saleItemRepository.deleteAll();
    }


}