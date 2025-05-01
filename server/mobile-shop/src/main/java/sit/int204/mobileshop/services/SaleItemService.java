package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.util.List;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepository;

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    public SaleItem getSaleItemById(Integer id) {
        return saleItemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("SaleItem not found with id: " + id));
    }

}
