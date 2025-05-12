package sit.int204.mobileshop.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.Brand;

import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.BrandRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;
    private boolean isIdentical(SaleItem item , SaleItemRequestDto dtoItem , Brand brand){
        return Objects.equals(item.getModel(), dtoItem.getModel().trim()) &&
                Objects.equals(item.getDescription(), dtoItem.getDescription().trim()) &&
                Objects.equals(item.getBrand().getId(), brand.getId()) &&
                Objects.equals(item.getPrice(), dtoItem.getPrice()) &&
                Objects.equals(item.getRamGb(), dtoItem.getRamGb()) &&
                Objects.equals(item.getStorageGb(), dtoItem.getStorageGb()) &&
                Objects.equals(item.getScreenSizeInch(), dtoItem.getScreenSizeInch()) &&
                Objects.equals(item.getQuantity(), dtoItem.getQuantity()) &&
                Objects.equals(item.getColor(), dtoItem.getColor() == null ? null : dtoItem.getColor().trim());
    }

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    public SaleItem getSaleItemById(Integer id) {
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    @Transactional
    public SaleItemDetailDto createSaleItem(SaleItemRequestDto dtoItem) {
        SaleItem item = new SaleItem();

        if (dtoItem.getBrand() == null || dtoItem.getBrand().getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name must not be null.");
        }

        Brand brand = brandService.getBrandByName(dtoItem.getBrand().getName());
        if (brand == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand not found brand name: " + dtoItem.getBrand().getName());
        }

        item.setBrand(brand);
        item.setModel(dtoItem.getModel());
        item.setDescription(dtoItem.getDescription());
        item.setPrice(dtoItem.getPrice());
        item.setRamGb(dtoItem.getRamGb());
        item.setScreenSizeInch(dtoItem.getScreenSizeInch());
        item.setStorageGb(dtoItem.getStorageGb());
        item.setQuantity(dtoItem.getQuantity() == null || dtoItem.getQuantity() < 1 ? 1 : dtoItem.getQuantity());

        if (dtoItem.getColor() != null && !dtoItem.getColor().isBlank()) {
            item.setColor(dtoItem.getColor().trim());
        }

        SaleItem saved = saleItemRepository.saveAndFlush(item);
        entityManager.refresh(saved);

        // 🔥 Manual mapping part
        SaleItemDetailDto dto = modelMapper.map(saved, SaleItemDetailDto.class);
        dto.setBrandName(saved.getBrand().getName());
        return dto;
    }



    public SaleItemDetailDto updateSaleItemById(Integer id, SaleItemRequestDto dtoItem) {
        SaleItem existingItem = saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Sale item not found"));

        Brand brand = brandService.getBrandByName(dtoItem.getBrand().getName());
        if (brand == null) {
            throw new ItemNotFoundException("Brand not found: " + dtoItem.getBrand().getName());
        }

        dtoItem.setModel(dtoItem.getModel().trim());
        dtoItem.setDescription(dtoItem.getDescription().trim());
        if (dtoItem.getColor() != null) dtoItem.setColor(dtoItem.getColor().trim());

        if (isIdentical(existingItem, dtoItem, brand)) {
            return modelMapper.map(existingItem, SaleItemDetailDto.class);
        }

        existingItem.setBrand(brand);
        existingItem.setModel(dtoItem.getModel());
        existingItem.setDescription(dtoItem.getDescription());
        existingItem.setPrice(dtoItem.getPrice());
        existingItem.setRamGb(dtoItem.getRamGb());
        existingItem.setScreenSizeInch(dtoItem.getScreenSizeInch());
        existingItem.setQuantity(dtoItem.getQuantity());
        existingItem.setStorageGb(dtoItem.getStorageGb());
        existingItem.setColor(dtoItem.getColor());
        SaleItem updatedItem = saleItemRepository.save(existingItem);

        if (updatedItem == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Sale Item");
        }


        return modelMapper.map(updatedItem, SaleItemDetailDto.class);
    }

    public void deleteSaleItemById(Integer id) {
        SaleItem deletedItem = saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Sale item not found for this id :: " + id));
        saleItemRepository.delete(deletedItem);
    }


    public void deleteAllForTest() {
        saleItemRepository.deleteAll();
    }


}
