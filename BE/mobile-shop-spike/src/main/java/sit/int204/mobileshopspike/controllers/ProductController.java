package sit.int204.mobileshopspike.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshopspike.dtos.ProductDto;
import sit.int204.mobileshopspike.entities.Product;
import sit.int204.mobileshopspike.services.ProductService;
import sit.int204.mobileshopspike.utils.ListMapper;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        return ResponseEntity.ok(listMapper.toListDto(products, ProductDto.class, modelMapper));


    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(modelMapper.map(productService.getProductById(id), ProductDto.class));
    }

}
