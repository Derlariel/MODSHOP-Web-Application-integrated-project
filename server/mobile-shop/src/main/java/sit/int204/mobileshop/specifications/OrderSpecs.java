package sit.int204.mobileshop.specifications;

import org.springframework.data.jpa.domain.Specification;
import sit.int204.mobileshop.entities.Order;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class OrderSpecs {
    
    /**
     * Search by seller name (fullName or nickName)
     */
    public static Specification<Order> sellerNameContains(String sellerName) {
        if (sellerName == null || sellerName.isBlank()) return null;
        return (root, query, cb) -> {
            String like = "%" + sellerName.trim() + "%";
            return cb.or(
                    cb.like(root.get("seller").get("fullName"), like),
                    cb.like(root.get("seller").get("nickName"), like)
            );
        };
    }

    /**
     * Search by buyer name (fullName or nickName)
     */
    public static Specification<Order> buyerNameContains(String buyerName) {
        if (buyerName == null || buyerName.isBlank()) return null;
        return (root, query, cb) -> {
            String like = "%" + buyerName.trim() + "%";
            return cb.or(
                    cb.like(root.get("user").get("fullName"), like),
                    cb.like(root.get("user").get("nickName"), like)
            );
        };
    }

    /**
     * Filter by order date (exact date match)
     */
    public static Specification<Order> orderDateEquals(LocalDate date) {
        if (date == null) return null;
        return (root, query, cb) -> {
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            return cb.between(root.get("orderDate"), startOfDay, endOfDay);
        };
    }

    /**
     * Filter by order date range
     */
    public static Specification<Order> orderDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) return null;
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                return cb.between(root.get("orderDate"), start, end);
            } else if (startDate != null) {
                Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                return cb.greaterThanOrEqualTo(root.get("orderDate"), start);
            } else if (endDate != null) {
                Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                return cb.lessThan(root.get("orderDate"), end);
            }
            return cb.conjunction();
        };
    }

    /**
     * Search by brand name in order items
     */
    public static Specification<Order> brandNameContains(String brandName) {
        if (brandName == null || brandName.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Object, Object> orderItems = root.join("orderItems", JoinType.INNER);
            Join<Object, Object> saleItem = orderItems.join("saleItem", JoinType.INNER);
            Join<Object, Object> brand = saleItem.join("brand", JoinType.INNER);
            
            if (query != null) {
                query.distinct(true); // Prevent duplicate orders
            }
            
            String like = "%" + brandName.trim() + "%";
            return cb.like(brand.get("name"), like);
        };
    }

    /**
     * Search by model in order items
     */
    public static Specification<Order> modelContains(String model) {
        if (model == null || model.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Object, Object> orderItems = root.join("orderItems", JoinType.INNER);
            Join<Object, Object> saleItem = orderItems.join("saleItem", JoinType.INNER);
            
            if (query != null) {
                query.distinct(true); // Prevent duplicate orders
            }
            
            String like = "%" + model.trim() + "%";
            return cb.like(saleItem.get("model"), like);
        };
    }

    /**
     * General keyword search across seller, brand, and model (for buyer)
     */
    public static Specification<Order> keywordSearchForBuyer(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> {
            String like = "%" + keyword.trim() + "%";
            
            Join<Object, Object> orderItems = root.join("orderItems", JoinType.LEFT);
            Join<Object, Object> saleItem = orderItems.join("saleItem", JoinType.LEFT);
            Join<Object, Object> brand = saleItem.join("brand", JoinType.LEFT);
            
            if (query != null) {
                query.distinct(true); // Prevent duplicate orders
            }
            
            return cb.or(
                    cb.like(root.get("seller").get("fullName"), like),
                    cb.like(root.get("seller").get("nickName"), like),
                    cb.like(brand.get("name"), like),
                    cb.like(saleItem.get("model"), like)
            );
        };
    }

    /**
     * General keyword search across buyer, brand, and model (for seller)
     */
    public static Specification<Order> keywordSearchForSeller(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> {
            String like = "%" + keyword.trim() + "%";
            
            Join<Object, Object> orderItems = root.join("orderItems", JoinType.LEFT);
            Join<Object, Object> saleItem = orderItems.join("saleItem", JoinType.LEFT);
            Join<Object, Object> brand = saleItem.join("brand", JoinType.LEFT);
            
            if (query != null) {
                query.distinct(true); // Prevent duplicate orders
            }
            
            return cb.or(
                    cb.like(root.get("user").get("fullName"), like),
                    cb.like(root.get("user").get("nickName"), like),
                    cb.like(brand.get("name"), like),
                    cb.like(saleItem.get("model"), like)
            );
        };
    }

    /**
     * Filter by user (buyer)
     */
    public static Specification<Order> userEquals(Long userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    /**
     * Filter by seller
     */
    public static Specification<Order> sellerEquals(Long sellerId) {
        if (sellerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
    }
}
