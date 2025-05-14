package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.exceptions.BrandAlreadyExitsException;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.BrandRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(Integer id) {
        return brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
    }

    public Optional<Brand> createBrandByName(Brand brand) {
        if (brandRepository.existsByName(brand.getName())) {
            throw new BrandAlreadyExitsException("Brand with name" + brand.getName() + "already exits.");
        }

        brandRepository.save(brand);
        return Optional.of(brand);
    }

}
