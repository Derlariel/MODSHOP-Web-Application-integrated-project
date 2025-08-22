package sit.int204.mobileshop.specifications;

import org.springframework.data.jpa.domain.Specification;
import sit.int204.mobileshop.entities.SaleItem;

import java.util.List;

public class SaleItemSpecs {
    public static Specification<SaleItem> keyword(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("model")), like),
                cb.like(cb.lower(root.get("color")), like)
        );
    }

    public static Specification<SaleItem> brandNamesIn(List<String> brandNames) {
        if (brandNames == null || brandNames.isEmpty()) return null;
        return (root, query, cb) -> root.get("brand").get("name").in(brandNames);
    }

    public static Specification<SaleItem> storageIn(List<Integer> storageGb) {
        if (storageGb == null || storageGb.isEmpty()) return null;
        return (root, query, cb) -> root.get("storageGb").in(storageGb);
    }

    // ใช้ร่วมกับ storageIn เพื่อ OR ให้รวมรายการที่ storage = null ได้
    public static Specification<SaleItem> includeNullStorage(boolean include) {
        if (!include) return null;
        return (root, query, cb) -> cb.isNull(root.get("storageGb"));
    }

    public static Specification<SaleItem> priceBetween(Integer lower, Integer upper) {
        if (lower == null && upper == null) return null;
        if (lower != null && upper != null) {
            return (root, query, cb) -> cb.between(root.get("price"), lower, upper);
        }
        return (root, query, cb) ->
                (lower != null) ? cb.greaterThanOrEqualTo(root.get("price"), lower)
                        : cb.lessThanOrEqualTo(root.get("price"), upper);
    }

    public static Specification<SaleItem> exactPrice(Integer price) {
        if (price == null) return null;
        return (root, query, cb) -> cb.equal(root.get("price"), price);
    }
}
