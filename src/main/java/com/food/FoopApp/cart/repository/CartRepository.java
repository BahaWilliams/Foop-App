package com.food.FoopApp.cart.repository;

import com.food.FoopApp.cart.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    public Optional<Cart> findByUser_Id(Long userId);
}
