package com.food.FoopApp.cart.services;

import com.food.FoopApp.cart.domain.dto.CartDto;
import com.food.FoopApp.response.Response;

public interface CartServices {
    Response<?> addItemToChart(CartDto cartDto);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDto> getShoppingChart();
    Response<?> clearChart();

}
