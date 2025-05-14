package sit.int204.mobileshop.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sit.int204.mobileshop.dtos.BrandDetailDto;
import sit.int204.mobileshop.dtos.BrandDto;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.services.BrandService;
import sit.int204.mobileshop.utils.ListMapper;

import java.util.List;

@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v1/brands")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;


    @GetMapping("")
    public ResponseEntity<List<BrandDto>> getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        if (brands.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(brands, BrandDto.class, modelMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDetailDto> getBrand(@PathVariable Integer id) {
        Brand brand = brandService.getBrandById(id);
        BrandDetailDto brandResponseDto = modelMapper.map(brand, BrandDetailDto.class);
        brandResponseDto.setSaleItemsCount(brand.getSaleItems().size());
        return ResponseEntity.ok(brandResponseDto);
    }

    @PostMapping("")
    public ResponseEntity<BrandDetailDto> postBrand(@RequestBody BrandDetailDto brand) {
        BrandDetailDto brandResponse = brandService.createBrandByName(brand).get();
        return ResponseEntity.status(HttpStatus.CREATED).body(brandResponse);
    }
}
