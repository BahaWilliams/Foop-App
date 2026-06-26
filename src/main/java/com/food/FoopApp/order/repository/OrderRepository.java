package com.food.FoopApp.order.repository;

import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.enums.OrderStatus;
import com.food.FoopApp.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    public Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
    public List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    public Long countDistinctUser();
}
