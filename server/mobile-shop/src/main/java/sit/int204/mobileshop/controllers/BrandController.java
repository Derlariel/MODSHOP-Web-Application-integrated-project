package sit.int204.mobileshop.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import jakarta.validation.Valid;


@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v1/brands")
@Tag(name = "Brand API", description = "API for managing brand data in the system")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @Operation(summary = "Get all brands", description = "Retrieve all brands")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved brands", 
                    content = @Content(schema = @Schema(implementation = BrandDetailDto.class))),
        @ApiResponse(responseCode = "200", description = "No brands found",
                    content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<List<BrandDetailDto>> getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        if (brands.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(brands, BrandDetailDto.class, modelMapper));
    }

    @Operation(summary = "Get brand by ID", description = "Search and retrieve brand using brand ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Brand found", 
                    content = @Content(schema = @Schema(implementation = BrandInfoDto.class))),
        @ApiResponse(responseCode = "404", description = "Brand not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BrandInfoDto> getBrand(
            @Parameter(description = "ID of the brand to search", required = true)
            @PathVariable Integer id) {
        Brand brand = brandService.getBrandById(id);
        BrandInfoDto brandInfoDto = modelMapper.map(brand, BrandInfoDto.class);
        brandInfoDto.setNoOfSaleItems(brand.getSaleItems().size());
        return ResponseEntity.ok(brandInfoDto);
    }

    @Operation(summary = "Create new brand", description = "Add new brand to the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Brand created successfully", 
                    content = @Content(schema = @Schema(implementation = BrandInfoDto.class))),
        @ApiResponse(responseCode = "400", description = "Brand already exists or invalid input", 
                    content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<BrandInfoDto> postBrand(
            @Parameter(description = "Brand data to create", required = true)
            @RequestBody BrandInfoDto brand) {

        if (brand.getIsActive() == null)
            brand.setIsActive(true);

        BrandInfoDto brandInfoDto = brandService.createBrandByName(brand).get();
        return ResponseEntity.status(HttpStatus.CREATED).body(brandInfoDto);
    }

    @Operation(summary = "Update brand", description = "Update existing brand information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Brand updated successfully", 
                    content = @Content(schema = @Schema(implementation = BrandInfoDto.class))),
        @ApiResponse(responseCode = "400", description = "Brand name already exists or invalid input", 
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Brand not found", 
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BrandInfoDto> putBrand(
            @Parameter(description = "ID of the brand to update", required = true)
            @PathVariable Integer id,
            
            @Parameter(description = "Updated brand data", required = true)
            @Valid @RequestBody BrandInfoDto brandInfoDto) {
        brandService.updateBrand(id, brandInfoDto);
        return ResponseEntity.ok().body(brandInfoDto);
    }

    @Operation(summary = "Delete brand", description = "Delete brand by specified ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Brand deleted successfully", 
                    content = @Content),
        @ApiResponse(responseCode = "400", description = "Brand has associated products and cannot be deleted", 
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Brand not found", 
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "ID of the brand to delete", required = true)
            @PathVariable Integer id) {
        brandService.removeBrand(id);
        return ResponseEntity.noContent().build();
    }
}
