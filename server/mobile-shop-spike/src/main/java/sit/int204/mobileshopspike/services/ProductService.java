package sit.int204.mobileshopspike.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int204.mobileshopspike.entities.Product;
import sit.int204.mobileshopspike.exceptions.ItemNotFoundException;
import sit.int204.mobileshopspike.repositories.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Product not found with id: " + id));
    }

}
