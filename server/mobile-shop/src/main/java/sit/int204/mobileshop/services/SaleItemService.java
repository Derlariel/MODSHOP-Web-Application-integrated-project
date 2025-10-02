package sit.int204.mobileshop.services;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.*;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.repositories.SellerRepository;
import sit.int204.mobileshop.repositories.UserRepository;
import sit.int204.mobileshop.specifications.SaleItemSpecs;
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
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ListMapper listMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileService fileService;

    public List<SaleItem> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    public PageDto<SaleItemDto> getAllSaleItemsPageBySellerId(Long sellerId,
                                                              Integer page,
                                                              Integer size,
                                                              String sortField,
                                                              String sortDirection) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserResponseDto principal = (UserResponseDto) authentication.getPrincipal();
            if (!principal.getId().equals(sellerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Request user id not matched with id in access token");
            }
        }

        if (sortField == null || sortField.isBlank()) sortField = "createdOn";
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortField)));
        Page<SaleItem> pageResult =  saleItemRepository.findSaleItemsBySellerId(sellerId, pageable);
        return listMapper.toPageDTO(pageResult, SaleItemDto.class, modelMapper);
    }

    public PageDto<SaleItemDto> getAllSaleItemsPage(
            Integer page,
            Integer size,
            List<String> filterBrands,
            List<String> storageSize,   // มาจาก FE เป็น String
            Integer lowerPrice,
            Integer upperPrice,
            Boolean isExactPrice,
            String sortField,
            String sortDirection,
            String q // คีย์เวิร์ด search
    ) {

        if (sortField == null || sortField.isBlank()) sortField = "createdOn";
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sortField)));

        // sanitize brand list
        if (filterBrands != null && !filterBrands.isEmpty()) {
            filterBrands = filterBrands.stream()
                    .filter(s -> s != null && !s.isBlank() && !"[]".equals(s))
                    .toList();
            if (filterBrands.isEmpty()) filterBrands = null;
        }

        // จัดการ storage + include null
        boolean includeNullStorage = false;
        List<Integer> storageNumbers = null;
        if (storageSize != null && !storageSize.isEmpty()) {
            includeNullStorage = storageSize.contains("null");
            storageNumbers = storageSize.stream()
                    .filter(s -> s != null && !s.isBlank() && !"[]".equals(s) && !"null".equalsIgnoreCase(s))
                    .map(Integer::valueOf)
                    .toList();
            if (storageNumbers.isEmpty()) storageNumbers = null;
        }

        // Build Specification
        Specification<SaleItem> spec = Specification.where(null);
        spec = spec.and(SaleItemSpecs.keyword(q));
        spec = spec.and(SaleItemSpecs.brandNamesIn(filterBrands));

        Specification<SaleItem> storageSpec = null;
        if (storageNumbers != null) storageSpec = SaleItemSpecs.storageIn(storageNumbers);
        if (includeNullStorage) {
            storageSpec = (storageSpec == null)
                    ? SaleItemSpecs.includeNullStorage(true)
                    : storageSpec.or(SaleItemSpecs.includeNullStorage(true));
        }
        if (storageSpec != null) spec = spec.and(storageSpec);

        if (Boolean.TRUE.equals(isExactPrice) && lowerPrice != null) {
            spec = spec.and(SaleItemSpecs.exactPrice(lowerPrice));
        } else {
            spec = spec.and(SaleItemSpecs.priceBetween(lowerPrice, upperPrice));
        }

        Page<SaleItem> pageResult = saleItemRepository.findAll(spec, pageable);

        PageDto<SaleItemDto> dtoPage = listMapper.toPageDTO(pageResult, SaleItemDto.class, modelMapper);

        for (SaleItemDto dto : dtoPage.getContent()) {
            saleItemImageRepository.findAllBySaleItemId(dto.getId()).stream()
                    .filter(img -> img.getImageViewOrder() == 1)
                    .findFirst()
                    .ifPresent((SaleItemImage img) -> dto.setImage(img.getFileName()));
        }

        return dtoPage;
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
        if (item.getSeller() != null) {
            dto.setSellerId(item.getSeller().getId().intValue());
            dto.setSellerNickname(item.getSeller().getNickName());
        }

        return dto;
    }

//    @Transactional
//    public SaleItemDetailDto createSaleItem(SaleItemRequestDto dtoItem, List<MultipartFile> images) throws IOException {
//        if (images != null && images.size() > MAX_IMAGES) {
//            throw new IllegalArgumentException("Cannot upload more than " + MAX_IMAGES + " images for a sale item.");
//        }
//
//        validateSaleItemRequest(dtoItem);
//
//        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());
//        SaleItem item = mapToSaleItem(dtoItem, brand);
//        SaleItem savedItem = saleItemRepository.saveAndFlush(item);
//        entityManager.refresh(savedItem);
//
//        if (images != null && !images.isEmpty()) {
//            Integer order = 1;
//            for (MultipartFile file : images) {
//                if (!file.isEmpty()) {
//                    fileService.saveFile(file, savedItem.getId(), order);
//                    order++;
//                }
//            }
//        }
//
//        SaleItemDetailDto saleItem = modelMapper.map(savedItem, SaleItemDetailDto.class);
//        saleItem.setSaleItemImages(listMapper.mapList(saleItemImageRepository.findAllBySaleItemId(saleItem.getId()), SaleItemImageDto.class, modelMapper));
//
//        log.info("Created SaleItem with ID: " + savedItem.getId());
//        return saleItem;
//    }

    @Transactional
    public SaleItemDetailDto createSaleItem(SaleItemRequestDto dtoItem, List<MultipartFile> images) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserResponseDto principal = (UserResponseDto) authentication.getPrincipal();

        Optional<Seller> seller = Optional.ofNullable(sellerRepository.getSellerById(principal.getId()));
        if (images != null && images.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_IMAGES + " images for a sale item.");
        }

        validateSaleItemRequest(dtoItem);

        Brand brand = brandService.getBrandById(dtoItem.getBrand().getId());
        SaleItem item = mapToSaleItem(dtoItem, brand);
        item.setSeller(seller.get());
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
        saleItem.setSaleItemImages(listMapper.mapList(
                saleItemImageRepository.findAllBySaleItemId(saleItem.getId()),
                SaleItemImageDto.class,
                modelMapper
        ));

        log.info("[SELLER CREATE] SaleItem with ID: " + savedItem.getId());
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

    // แก้ไขใน SaleItemService - ส่วนที่เรียกใช้ FileService

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

        // --- จัดการ images (optional) ---
        List<SaleItemImageRequest> newImages = saleItemWithImageInfo.getImageInfos();
        if (newImages != null && !newImages.isEmpty()) {
            List<SaleItemImage> existingImages = saleItemImageRepository.findAllBySaleItemId(id);
            processImages(id, existingImages, newImages);
        }

        result.setBrandName(brand.getName());
        return result;
    }

    private void processImages(Integer saleItemId, List<SaleItemImage> existingImages, List<SaleItemImageRequest> newImages) throws IOException {
        // map รูปเก่าตาม fileName -> object
        Map<String, SaleItemImage> existingImageMap =
                existingImages.stream().collect(Collectors.toMap(SaleItemImage::getFileName, img -> img));

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

        // loop รูปใหม่
        for (int i = 0; i < newImages.size(); i++) {
            SaleItemImageRequest newImage = newImages.get(i);
            String status = newImage.getStatus() != null ? newImage.getStatus().toUpperCase() : null;

            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image status cannot be null at index " + i);
            }

            switch (status) {
                case "ONLINE":
                    break;

                case "NEW":
                    if (newImage.getImageFile() == null || newImage.getImageFile().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Image file is required for NEW status at index " + i);
                    }
                    fileService.saveFile(newImage.getImageFile(), saleItemId, newImage.getOrder());
                    break;

                case "MOVE":
                    SaleItemImage existingImage = existingImageMap.get(newImage.getFileName());
                    if (existingImage == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No existing image found for MOVE status at index " + i);
                    }
                    existingImage.setImageViewOrder(newImage.getOrder());
                    existingImage.setUpdatedOn(Instant.now());
                    saleItemImageRepository.save(existingImage);
                    break;

                case "DELETE":
                    SaleItemImage toDelete = existingImageMap.get(newImage.getFileName());
                    if (toDelete == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No existing image found for DELETE status at index " + i);
                    }
                    fileService.deleteSpecificImages(saleItemId, List.of(toDelete.getFileName()));
                    break;

                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Unknown status: " + status + " at index " + i);
            }
        }

        normalizeImageOrder(saleItemId);
    }

    private void normalizeImageOrder(Integer saleItemId) {
        List<SaleItemImage> remainingImages = saleItemImageRepository.findAllBySaleItemId(saleItemId)
                .stream()
                .sorted(Comparator.comparingInt(SaleItemImage::getImageViewOrder))
                .toList();

        List<SaleItemImage> imagesToUpdate = new ArrayList<>();
        for (int j = 0; j < remainingImages.size(); j++) {
            SaleItemImage img = remainingImages.get(j);
            int expectedOrder = j + 1;

            if (!img.getImageViewOrder().equals(expectedOrder)) {
                img.setImageViewOrder(expectedOrder);
                img.setUpdatedOn(Instant.now());
                imagesToUpdate.add(img);
            }
        }

        if (!imagesToUpdate.isEmpty()) {
            saleItemImageRepository.saveAll(imagesToUpdate);
        }
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