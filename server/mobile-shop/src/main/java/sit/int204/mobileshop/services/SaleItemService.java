package sit.int204.mobileshop.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
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
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.Brand;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.utils.ListMapper;


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

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

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
        PageDto<SaleItemDto> saleItemDtoPageDto = listMapper.toPageDTO(saleItemPage, SaleItemDto.class, modelMapper);
        for(int i = 0; i < saleItemDtoPageDto.getContent().size(); i++ ) {

            System.out.println(saleItemDtoPageDto.getContent().get(i));
            Optional<SaleItemImage> imageOpt = saleItemImageRepository
                    .findAllBySaleItemId(saleItemDtoPageDto.getContent().get(i).getId()).stream()
                    .filter(img -> img.getImageViewOrder() == 1)
                    .findFirst();

            if(imageOpt.isPresent()) {
                System.out.println(imageOpt.orElse(null).getFileName());
                saleItemDtoPageDto.getContent().get(i).setImage(imageOpt.orElse(null).getFileName());
                System.out.println("image get" +   saleItemDtoPageDto.getContent().get(i).getImage());
            }
        }
        return saleItemDtoPageDto;
    }

    public SaleItemDetailDto getSaleItemById(Integer id) {
        SaleItem item = saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));

        SaleItemDetailDto dto = modelMapper.map(item, SaleItemDetailDto.class);

        List<SaleItemImageDto> imageDtos = new ArrayList<>(
                listMapper.mapList(
                        saleItemImageRepository.findAllBySaleItemId(id),
                        SaleItemImageDto.class,
                        modelMapper
                )
        );

        imageDtos.sort(Comparator.comparingInt(SaleItemImageDto::getImageViewOrder));

        dto.setSaleItemImages(imageDtos);
        dto.setBrandName(item.getBrand().getName());

        return dto;
    }

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


    public SaleItem getSaleItemByIdOld(Integer id) {
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    public SaleItemDetailDto updateSaleItemById(Integer id, SaleItemRequestDto dtoItem) {
        SaleItem existingItem = getSaleItemByIdOld(id);

        if (dtoItem.getBrand() == null || dtoItem.getBrand().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID must not be null.");
        }

        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());

        if (dtoItem.getQuantity() == null || dtoItem.getQuantity() < 0) {
            dtoItem.setQuantity(1);
        }

        existingItem.setBrand(brand);
        existingItem.setModel(dtoItem.getModel() != null ? dtoItem.getModel().trim() : null);
        existingItem.setDescription(dtoItem.getDescription() != null ? dtoItem.getDescription().trim() : null);
        existingItem.setPrice(dtoItem.getPrice());
        existingItem.setRamGb(dtoItem.getRamGb() != null ? dtoItem.getRamGb() : null);
        existingItem.setScreenSizeInch(dtoItem.getScreenSizeInch());
        existingItem.setQuantity(dtoItem.getQuantity());
        existingItem.setStorageGb(dtoItem.getStorageGb() != null ? dtoItem.getStorageGb() : null);
        existingItem.setColor(
                dtoItem.getColor() != null && !dtoItem.getColor().trim().isEmpty() ? dtoItem.getColor().trim() : null);

        SaleItem updatedItem = saleItemRepository.save(existingItem);
        SaleItemDetailDto result = modelMapper.map(updatedItem, SaleItemDetailDto.class);
        result.setBrandName(brand.getName());
        return result;
    }

    @Transactional
    public SaleItemDetailDto updateSaleItemByIdWithImages(Integer id, SaleItemWithImageInfo saleItemWithImageInfo) throws IOException {
        // 1. โหลดข้อมูลเก่า
        SaleItem existingItem = getSaleItemByIdOld(id);

        // 2. validate brand
        if (saleItemWithImageInfo.getSaleItem() == null ||
                saleItemWithImageInfo.getSaleItem().getBrand() == null ||
                saleItemWithImageInfo.getSaleItem().getBrand().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID must not be null.");
        }

        Brand brand = brandService.getBrandById(saleItemWithImageInfo.getSaleItem().getBrand().getId());
        existingItem.setBrand(brand);

        if (saleItemWithImageInfo.getSaleItem().getQuantity() == null ||
                saleItemWithImageInfo.getSaleItem().getQuantity() < 0) {
            existingItem.setQuantity(1);
        }

        // 3. map ข้อมูลใหม่ลง object เก่า
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(saleItemWithImageInfo.getSaleItem(), existingItem);

        // 4. save item
        SaleItem updatedItem = saleItemRepository.save(existingItem);
        SaleItemDetailDto result = modelMapper.map(updatedItem, SaleItemDetailDto.class);

        // --- จัดการ images ---
        List<SaleItemImage> images = saleItemImageRepository.findAllBySaleItemId(id);
        List<SaleItemImageRequest> newImages = saleItemWithImageInfo.getImageInfos();

        if (newImages == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image list cannot be null.");
        }

        // map รูปเก่าตาม fileName -> object
        Map<String, SaleItemImage> existingImageMap =
                images.stream().collect(Collectors.toMap(SaleItemImage::getFileName, img -> img));

        // ตรวจ duplicate order
        Set<Integer> orders = new HashSet<>();
        for (SaleItemImageRequest image : newImages) {
            String status = image.getStatus() != null ? image.getStatus().toUpperCase() : null;
            if (status != null && !status.equals("DELETE")) {
                if (!orders.add(image.getOrder())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Duplicate order value: " + image.getOrder());
                }
            }
        }

        for (int i = 0; i < newImages.size(); i++) {
            SaleItemImageRequest newImage = newImages.get(i);
            String status = newImage.getStatus() != null ? newImage.getStatus().toUpperCase() : null;

            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image status cannot be null at index " + i);
            }

            switch (status) {
                case "ONLINE":
                    // ไม่ต้องทำอะไร
                    break;

                case "NEW":
                    if (newImage.getImageFile() == null || newImage.getImageFile().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Image file is required for NEW status at index " + i);
                    }
                    String newFileName = fileService.saveUpdate(newImage.getImageFile(), id, newImage.getOrder());
                    SaleItemImage newSaleItemImage = new SaleItemImage();
                    newSaleItemImage.setSaleItem(existingItem);
                    newSaleItemImage.setFileName(newFileName);
                    newSaleItemImage.setImageViewOrder(newImage.getOrder());
                    newSaleItemImage.setCreatedOn(Instant.now());
                    newSaleItemImage.setUpdatedOn(Instant.now());
                    saleItemImageRepository.save(newSaleItemImage);
                    break;

                case "MOVE":
                    SaleItemImage existingImage = existingImageMap.get(newImage.getFileName());
                    if (existingImage == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No existing image found for MOVE status at index " + i);
                    }
                    try {
                        String updatedFileName = fileService.updateFileName(existingImage.getFileName(), id, newImage.getOrder());
                        existingImage.setFileName(updatedFileName);
                        existingImage.setImageViewOrder(newImage.getOrder());
                        existingImage.setUpdatedOn(Instant.now());
                        saleItemImageRepository.save(existingImage);
                    } catch (IOException e) {
                        throw new RuntimeException("Error moving file: " + existingImage.getFileName(), e);
                    }
                    break;

                case "DELETE":
                    SaleItemImage toDelete = existingImageMap.get(newImage.getFileName());
                    if (toDelete == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No existing image found for DELETE status at index " + i);
                    }
                    try {
                        if (fileService.deleteByFileName(toDelete.getFileName())) {
                            System.out.println("File deleted: " + toDelete.getFileName());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error deleting file: " + toDelete.getFileName(), e);
                    }
                    saleItemImageRepository.delete(toDelete);
                    break;

                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Unknown status: " + status + " at index " + i);
            }
        }


        List<SaleItemImage> remainingImages = saleItemImageRepository.findAllBySaleItemId(id)
                .stream()
                .sorted(Comparator.comparingInt(SaleItemImage::getImageViewOrder))
                .toList();

        for (int j = 0; j < remainingImages.size(); j++) {
            SaleItemImage img = remainingImages.get(j);
            int expectedOrder = j + 1;
            if (!img.getImageViewOrder().equals(expectedOrder)) {
                img.setImageViewOrder(expectedOrder);
                img.setUpdatedOn(Instant.now());
                saleItemImageRepository.save(img);

                try {
                    String updatedFileName = fileService.updateFileName(img.getFileName(), id, expectedOrder);
                    img.setFileName(updatedFileName);
                    saleItemImageRepository.save(img);
                } catch (IOException e) {
                    throw new RuntimeException("Error renaming file after normalize: " + img.getFileName(), e);
                }
            }
        }

        result.setBrandName(brand.getName());
        return result;
    }

    public void deleteSaleItemByIdOld(Integer id) {
        saleItemRepository.delete(getSaleItemByIdOld(id));
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