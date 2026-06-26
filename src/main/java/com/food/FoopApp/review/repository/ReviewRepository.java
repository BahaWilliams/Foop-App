package com.food.FoopApp.review.repository;

import com.food.FoopApp.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    public List<Review> findByMenuIdOrderByIdDesc(Long menuId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menu.id = :menuId")
    public Double calculateAverageRatingByMenuId(@Param("menuId") Long menuId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Review r
            WHERE r.user.id = :userId AND r.menu.id = :menuId AND r.orderId = :orderId
            """)
    public boolean existsByUserIdAndMenuIdAndOrderId(
            @Param("userId") Long userId,
            @Param("menuId") Long menuId,
            @Param("orderId") Long orderId);
}
