package sit.int204.mobileshop.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;


@Service
public class SaleItemImageService {
    @Autowired
    private FileService fileService;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    public List<SaleItemImage> getSaleItemImageRepository(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemId(saleItemId);
    }


        public boolean removeImages(Integer saleItemId) {
        fileService.deleteImageById(saleItemId);
        return true;
    }

//    public Object updateImages(Integer saleItemId, List<MultipartFile> images) {
//        fileService.updateImagesByIdAndImgUrls(saleItemId, images);
//        return true;
//    }

//    public boolean updateImages(Integer saleItemId, List<MultipartFile> images) {
//    }

//    public boolean saveImages(List<MultipartFile> images,  Integer saleItemId) {
//        saleItemImageRepository.saveAll(images);
//
//    }
}
