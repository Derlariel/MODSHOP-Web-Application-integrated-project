package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.Brand;

import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private BrandService brandService;

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    public SaleItem getSaleItemById(Integer id) {
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    public SaleItem createSaleItem(SaleItemRequestDto dtoItem) {
        SaleItem item = new SaleItem();
//       Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());
        Brand brand = brandService.getBrandByName(dtoItem.getBrand().getName());
        item.setModel(dtoItem.getModel());
        item.setBrand(brand);
        item.setDescription(dtoItem.getDescription());
        item.setPrice(dtoItem.getPrice());
        //optional field
        if (dtoItem.getRamGb() != null) item.setRamGb(dtoItem.getRamGb());
        if (dtoItem.getScreenSizeInch() != null) item.setScreenSizeInch(dtoItem.getScreenSizeInch());
        item.setQuantity(dtoItem.getQuantity());
        //optional field
        if (dtoItem.getStorageGb() != null) item.setStorageGb(dtoItem.getStorageGb());
        if (dtoItem.getColor() != null && !dtoItem.getColor().isBlank()) item.setColor(dtoItem.getColor().trim());
        item.setCreatedOn(Instant.now());
        item.setUpdatedOn(Instant.now());
        return saleItemRepository.save(item);
     }

    public void deleteAllForTest() {
        saleItemRepository.deleteAll();
    }




}
