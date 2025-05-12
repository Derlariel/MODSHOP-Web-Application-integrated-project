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
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.util.List;
import java.util.Objects;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
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

        if (dtoItem.getQuantity() == null || dtoItem.getQuantity() < 1) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Quantity must be at least 1.");
        }

        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());

        SaleItem item = new SaleItem();
        item.setBrand(brand);
        item.setModel(dtoItem.getModel() != null ? dtoItem.getModel().trim() : null);
        item.setDescription(dtoItem.getDescription() != null ? dtoItem.getDescription().trim() : "");
        item.setPrice(dtoItem.getPrice());
        item.setRamGb(dtoItem.getRamGb());
        item.setScreenSizeInch(dtoItem.getScreenSizeInch());
        item.setStorageGb(dtoItem.getStorageGb());
        item.setQuantity(dtoItem.getQuantity());
        item.setColor(dtoItem.getColor() != null ? dtoItem.getColor().trim() : "");

        saleItemRepository.saveAndFlush(item);
        entityManager.refresh(item);

        SaleItemDetailDto result = modelMapper.map(item, SaleItemDetailDto.class);
        result.setBrandName(item.getBrand().getName());
        return result;
    }

    public SaleItemDetailDto updateSaleItemById(Integer id, SaleItemRequestDto dtoItem) {
        SaleItem existingItem = getSaleItemById(id);

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

        return modelMapper.map(saleItemRepository.save(existingItem), SaleItemDetailDto.class);
    }

    public void deleteSaleItemById(Integer id) {
        saleItemRepository.delete(getSaleItemById(id));
    }

    public void deleteAllForTest() {
        saleItemRepository.deleteAll();
    }

    private boolean isIdentical(SaleItem item, SaleItemRequestDto dtoItem, Brand brand) {
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
}