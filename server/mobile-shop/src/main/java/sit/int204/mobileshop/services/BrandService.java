package sit.int204.mobileshop.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import sit.int204.mobileshop.dtos.BrandDetailDto;
import sit.int204.mobileshop.dtos.BrandInfoDto;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.exceptions.BrandAlreadyExitsException;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.exceptions.BrandAlreadyExitsException;
import sit.int204.mobileshop.repositories.BrandRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    public Optional<BrandInfoDto> createBrandByName(BrandInfoDto brandInfoDto) {
        if (brandRepository.existsByNameIgnoreCase(brandInfoDto.getName())) {
            throw new BrandAlreadyExitsException("Brand with name" + brandInfoDto.getName() + "already exits.");
        }

        brandInfoDto.setId(null);
        Brand brand = modelMapper.map(brandInfoDto, Brand.class);
        return Optional.of(modelMapper.map(brandRepository.saveAndFlush(brand), BrandInfoDto.class));
    }

    public Optional<BrandInfoDto> updateBrand(Integer id, BrandInfoDto brandInfoDto) {

        if (getBrandById(id) == null)
            throw new ItemNotFoundException(null);

        Brand brand = getBrandById(id);

        if (!brand.getName().equals(brandInfoDto.getName()) && brandRepository.existsByNameIgnoreCase(brandInfoDto.getName())) {
            throw new BrandAlreadyExitsException("Brand name '" + brandInfoDto.getName() + "' is already used.");
        }

        brandInfoDto.setId(id);
        brandInfoDto.setNoOfSaleItems(brand.getSaleItems().size());

        return Optional.of(modelMapper.map(brandRepository.saveAndFlush(
                modelMapper.map(brandInfoDto, Brand.class)), BrandInfoDto.class));

    }
    

}
