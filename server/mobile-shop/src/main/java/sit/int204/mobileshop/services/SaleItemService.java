package sit.int204.mobileshop.services;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemImageDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.utils.ListMapper;

/**
 * Service for managing SaleItem operations, including retrieval, creation, update, and deletion.
 */
@Service
public class SaleItemService {
    private static final Logger log = Logger.getLogger(SaleItemService.class.getName());
    private static final int MAX_IMAGES = 4;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileService fileService;

    /**
     * Retrieves all SaleItems sorted by creation date.
     *
     * @return list of all SaleItems
     */
    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    /**
     * Retrieves SaleItems with pagination and optional filtering.
     *
     * @param page          page number
     * @param size          page size
     * @param filterBrands  list of brand names to filter
     * @param storageSize   list of storage sizes to filter
     * @param lowerPrice    minimum price filter
     * @param upperPrice    maximum price filter
     * @param isExactPrice  whether to use exact price matching for single price filter
     * @param sortField     field to sort by
     * @param sortDirection sort direction (ASC/DESC)
     * @return paginated list of SaleItem DTOs
     */
    public PageDto<SaleItemDto> getAllSaleItemsPage(
            Integer page,
            Integer size,
            List<String> filterBrands,
            List<String> storageSize,
            Integer lowerPrice,
            Integer upperPrice,
            Boolean isExactPrice,
            String sortField,
            String sortDirection) {

        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "createdOn";
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }

        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 10;
        }

        Sort sort = Sort.by(new Sort.Order(direction, sortField));
        Pageable pageable = PageRequest.of(page, size, sort);

        if (filterBrands != null) {
            filterBrands = filterBrands.stream()
                    .filter(b -> b != null && !b.trim().isEmpty() && !b.equals("[]"))
                    .collect(Collectors.toList());
            if (filterBrands.isEmpty()) {
                filterBrands = null;
            }
        }

        if (storageSize != null) {
            storageSize = storageSize.stream()
                    .filter(b -> b != null && !b.trim().isEmpty() && !b.equals("[]"))
                    .collect(Collectors.toList());
            if (storageSize.isEmpty()) {
                storageSize = null;
            }
        }

        // Check if null storage should be included in the filter
        Boolean includeNullStorage = false;
        if (storageSize != null && storageSize.contains("null")) {
            includeNullStorage = true;
            // Remove "null" from the storage list as it's handled separately
            storageSize = storageSize.stream()
                    .filter(s -> !"null".equals(s))
                    .collect(Collectors.toList());
            if (storageSize.isEmpty()) {
                storageSize = null;
            }
        }

        Page<SaleItem> saleItemPage = saleItemRepository.findAllFilter(pageable, filterBrands, storageSize, includeNullStorage, lowerPrice, upperPrice, isExactPrice);
        return listMapper.toPageDTO(saleItemPage, SaleItemDto.class, modelMapper);
    }

    /**
     * Retrieves a SaleItem by ID, including its associated image URLs.
     *
     * @param id the ID of the SaleItem
     * @return SaleItemDetailDto with item details and image URLs
     * @throws ItemNotFoundException if the SaleItem is not found
     */
    public SaleItemDetailDto getSaleItemById(Integer id) {
        SaleItem item = saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));

        SaleItemDetailDto dto = modelMapper.map(item, SaleItemDetailDto.class);
        System.out.println("saleItemImageRepository.findAllBySaleItemId(id)" + saleItemImageRepository.findAllBySaleItemId(id));
        dto.setSaleItemImages(listMapper.mapList(saleItemImageRepository.findAllBySaleItemId(id), SaleItemImageDto.class, modelMapper));
        dto.setBrandName(item.getBrand().getName());
//        dto.setImageUrls(getImageUrls(id));

        return dto;
    }

    /**
     * Creates a new SaleItem with optional images.
     *
     * @param dtoItem the SaleItem request DTO
     * @param images  list of image files to upload
     * @return SaleItemDetailDto of the created item
     * @throws IOException if file upload fails
     */
    @Transactional
    public SaleItemDetailDto createSaleItem(SaleItemRequestDto dtoItem, List<MultipartFile> images) throws IOException {
        if (images != null && images.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_IMAGES + " images for a sale item.");
        }

        validateSaleItemRequest(dtoItem);

        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());
        SaleItem item = mapToSaleItem(dtoItem, brand);
        SaleItem savedItem = saleItemRepository.saveAndFlush(item);
        entityManager.refresh(savedItem);

        if (images != null && !images.isEmpty()) {
            Integer order = 1;
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    fileService.saveFile(file, savedItem.getId(), order);
                    order++;
                }
            }
        }

        SaleItemDetailDto saleItem = modelMapper.map(savedItem, SaleItemDetailDto.class);
        saleItem.setSaleItemImages(listMapper.mapList(saleItemImageRepository.findAllBySaleItemId(saleItem.getId()), SaleItemImageDto.class, modelMapper));

        log.info("Created SaleItem with ID: " + savedItem.getId());
        return saleItem;
    }

    @Transactional
    public void deleteSaleItemById(Integer id) {
        if(!saleItemRepository.findById(id).isPresent()) {
            throw new ItemNotFoundException("SaleItem not found for this id :: " + id);
        }
        saleItemRepository.deleteById(id);
    }

//    public void deleteSaleItemById(Integer id) {
//        SaleItem item = saleItemRepository.findById(id)
//                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
//        saleItemImageRepository.findAllBySaleItemId(id).forEach(image ->
//                fileService.deleteImageById(image.getId()));
//        saleItemRepository.delete(item);
//        log.info("Deleted SaleItem with ID: " + id);
//    }

    // ===== Helper Methods =====

    private void validateSaleItemRequest(SaleItemRequestDto dtoItem) {
        if (dtoItem.getBrand() == null || dtoItem.getBrand().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID must not be null.");
        }
        if (dtoItem.getModel() == null || dtoItem.getModel().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model must not be blank.");
        }
        if (dtoItem.getPrice() == null || dtoItem.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be non-negative.");
        }
        if (dtoItem.getQuantity() == null || dtoItem.getQuantity() < 0) {
            dtoItem.setQuantity(1);
        }
    }

    private SaleItem mapToSaleItem(SaleItemRequestDto dtoItem, Brand brand) {
        SaleItem item = new SaleItem();
        item.setBrand(brand);
        item.setModel(dtoItem.getModel().trim());
        item.setDescription(dtoItem.getDescription() == null ? null : dtoItem.getDescription().trim());
        item.setPrice(dtoItem.getPrice());
        item.setRamGb(dtoItem.getRamGb() != null ? dtoItem.getRamGb() : null);
        item.setScreenSizeInch(dtoItem.getScreenSizeInch());
        item.setStorageGb(dtoItem.getStorageGb() != null ? dtoItem.getStorageGb() : null);
        item.setQuantity(dtoItem.getQuantity());
        item.setColor(dtoItem.getColor() != null && !dtoItem.getColor().trim().isEmpty() ?
                dtoItem.getColor().trim() : null);
        return item;
    }

//    private List<String> getImageUrls(Integer saleItemId) {
//        return saleItemImageRepository.findAllBySaleItemId(saleItemId).stream()
//                .map(SaleItemImage::getImageUrl)
//                .collect(Collectors.toList());
//    }
}
