package com.food.FoopApp.cart.controller;

import com.food.FoopApp.cart.domain.dto.CartDto;
import com.food.FoopApp.cart.services.CartServices;
import com.food.FoopApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartServices cartServices;

    @PostMapping("/items")
    public ResponseEntity<Response<?>> createCart(@RequestBody @Valid CartDto cartDto){
        return ResponseEntity.ok(cartServices.addItemToChart(cartDto));
    }

    @PutMapping("/items/increment/{menuId}")
    public ResponseEntity<Response<?>> incrementItems(@PathVariable Long menuId){
        return ResponseEntity.ok(cartServices.incrementItem(menuId));
    }

    @PutMapping("/items/decrement/{menuId}")
    public ResponseEntity<Response<?>> decrementItems(@PathVariable Long menuId){
        return ResponseEntity.ok(cartServices.decrementItem(menuId));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Response<?>> removeItems(@PathVariable Long cartItemId){
        return ResponseEntity.ok(cartServices.removeItem(cartItemId));
    }

    @GetMapping
    public ResponseEntity<Response<CartDto>> getShoppingCart(){
        return ResponseEntity.ok(cartServices.getShoppingChart());
    }

    @DeleteMapping
    public ResponseEntity<Response<?>> deleteShoppingCart(){
        return ResponseEntity.ok(cartServices.clearChart());
    }
}
