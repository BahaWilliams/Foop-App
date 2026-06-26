package com.food.FoopApp.cart.services;

import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.auth_user.services.UserService;
import com.food.FoopApp.cart.domain.dto.CartDto;
import com.food.FoopApp.cart.domain.entity.Cart;
import com.food.FoopApp.cart.domain.entity.CartItem;
import com.food.FoopApp.cart.repository.CartItemRepository;
import com.food.FoopApp.cart.repository.CartRepository;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.menu.domain.entity.Menu;
import com.food.FoopApp.menu.repository.MenuRepository;
import com.food.FoopApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServicesImpl implements CartServices {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> addItemToChart(CartDto cartDto) {
        log.info("add cart item");

        Long menuId = cartDto.getMenuId();
        int quantity = cartDto.getQuantity();
        User user = userService.getCurrentLoggedInUser();
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new NotFoundException("Menu was not found"));
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());

            return cartRepository.save(newCart);
        });

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter((cartItem) -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();

        if(optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        }else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();

            cart.getCartItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .build();
    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("increment item");

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() -> new NotFoundException("Not found items"));
        CartItem cartItem  = cart.getCartItems().stream()
                .filter((item) -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Not found items"));

        int quantity = cartItem.getQuantity() + 1;
        cartItem.setQuantity(quantity);
        cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity incremented successfully")
                .build();
    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("decrement item");

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() -> new NotFoundException("Not found items"));
        CartItem cartItem  = cart.getCartItems().stream()
                .filter((item) -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Not found items"));

        int quantity = cartItem.getQuantity() - 1;

        if(quantity > 0){
            cartItem.setQuantity(quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(quantity)));
            cartItemRepository.save(cartItem);
        }else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity incremented successfully")
                .build();
    }

    @Override
    public Response<?> removeItem(Long cartItemId) {
        log.info("remove item");

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() -> new NotFoundException("Not found items"));
        CartItem cartItem  = cartItemRepository.findById(cartItemId).orElseThrow(() -> new NotFoundException("Not found items"));

        if(!cart.getCartItems().contains(cartItem)){
            throw new NotFoundException("Cart item does not belong to this user cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item remove successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDto> getShoppingChart() {
        log.info("get shopping cart");

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));
        List<CartItem> cartItems = cart.getCartItems();
        CartDto cartDtoMapped = modelMapper.map(cart, CartDto.class);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if(cartItems != null){
            for (CartItem cartItem : cartItems) {
                totalAmount = totalAmount.add(cartItem.getSubTotal());
            }
        }

        cartDtoMapped.setTotalAmount(totalAmount);

        if(cartDtoMapped.getCartItems() != null){
            cartDtoMapped.getCartItems().forEach((item) -> item.getMenu().setReviews(null));
        }

        return Response.<CartDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart retrieved successfully")
                .data(cartDtoMapped)
                .build();
    }

    @Override
    public Response<?> clearChart() {
        log.info("Inside clearShoppingCart()");

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElseThrow(() -> new NotFoundException("Cart not found for user"));
        //Delete cart items from the database first
        cartItemRepository.deleteAll(cart.getCartItems());
        //Clear the cart's items collection
        cart.getCartItems().clear();
        //update the database
        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart cleared successfully")
                .build();
    }
}
