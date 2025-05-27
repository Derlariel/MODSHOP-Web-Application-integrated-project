package sit.int204.mobileshop.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sit.int204.mobileshop.dtos.BrandDetailDto;

import sit.int204.mobileshop.dtos.BrandInfoDto;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.services.BrandService;
import sit.int204.mobileshop.utils.ListMapper;

import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;


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
    public ResponseEntity<List<BrandDetailDto>> getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        if (brands.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(brands, BrandDetailDto.class, modelMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandInfoDto> getBrand(@PathVariable Integer id) {
        Brand brand = brandService.getBrandById(id);
        BrandInfoDto brandInfoDto = modelMapper.map(brand, BrandInfoDto.class);
        brandInfoDto.setNoOfSaleItems(brand.getSaleItems().size());
        return ResponseEntity.ok(brandInfoDto);
    }

    @PostMapping("")
    public ResponseEntity<BrandInfoDto> postBrand(@RequestBody BrandInfoDto brand) {

        if (brand.getIsActive() == null)
            brand.setIsActive(true);

        BrandInfoDto brandInfoDto = brandService.createBrandByName(brand).get();
        return ResponseEntity.status(HttpStatus.CREATED).body(brandInfoDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandInfoDto> putBrand(@PathVariable Integer id,@Valid @RequestBody BrandInfoDto brandInfoDto) {
        brandService.updateBrand(id, brandInfoDto);
        return ResponseEntity.ok().body(brandInfoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
        brandService.removeBrand(id);
        return ResponseEntity.noContent().build();
    }
}
