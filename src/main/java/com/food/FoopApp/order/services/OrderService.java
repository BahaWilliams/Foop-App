package com.food.FoopApp.order.services;

import com.food.FoopApp.enums.OrderStatus;
import com.food.FoopApp.order.domain.dto.OrderDto;
import com.food.FoopApp.order.domain.dto.OrderItemDto;
import com.food.FoopApp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    Response<?> placeOrderFromCart();
    Response<OrderDto> getOrderById(Long id);
    Response<Page<OrderDto>> getAllOrder(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDto>> getOrdersOfUser();
    Response<OrderItemDto> getOrdersItemsById(Long id);
    Response<OrderDto> updateStatus(OrderDto orderDto);
    Response<Long> countUniqueCustomers();
}
