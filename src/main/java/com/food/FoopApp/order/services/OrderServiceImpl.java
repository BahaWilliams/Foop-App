package com.food.FoopApp.order.services;

import com.food.FoopApp.auth_user.domain.entity.User;
import com.food.FoopApp.auth_user.services.UserService;
import com.food.FoopApp.cart.domain.entity.Cart;
import com.food.FoopApp.cart.domain.entity.CartItem;
import com.food.FoopApp.cart.repository.CartRepository;
import com.food.FoopApp.cart.services.CartServices;
import com.food.FoopApp.email_notification.domain.dto.NotificationDto;
import com.food.FoopApp.email_notification.services.NotificationService;
import com.food.FoopApp.enums.OrderStatus;
import com.food.FoopApp.enums.PaymentStatus;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.menu.domain.dto.MenuDto;
import com.food.FoopApp.order.domain.dto.OrderDto;
import com.food.FoopApp.order.domain.dto.OrderItemDto;
import com.food.FoopApp.order.domain.entity.Order;
import com.food.FoopApp.order.domain.entity.OrderItem;
import com.food.FoopApp.order.repository.OrderItemRepository;
import com.food.FoopApp.order.repository.OrderRepository;
import com.food.FoopApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final CartRepository cartRepository;
    private final CartServices cartServices;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;

    @Value("${base.payment.link}")
    private String basePaymentLink;

    @Override
    @Transactional
    public Response<?> placeOrderFromCart() {
        log.info("place order from cart");

        User customer = userService.getCurrentLoggedInUser();

        log.info("user passed");

        String deliveryAddress = customer.getAddress();

        log.info("deliveryAddress passed");

        if (deliveryAddress == null) {
            throw new NotFoundException("Delivery Address Not present for the user");
        }
        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found for the user" ));


        log.info("cart passed");

        List<CartItem> cartItems = cart.getCartItems();

        log.info("cartItems passed");

        if (cartItems == null || cartItems.isEmpty()) throw new BadRequestException("Cart is empty");

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;


        log.info("totalAmount passed");

        for (CartItem cartItem: cartItems){

            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getPricePerUnit())
                    .subTotal(cartItem.getSubTotal())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        log.info("orderItem adding passed");

        Order order = Order.builder()
                .user(customer)
                .orderItems(orderItems)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.INITIALIZE)
                .paymentStatus(PaymentStatus.PENDING)
                .build();


        log.info("order build passed");

        Order savedOrder = orderRepository.save(order); //save order


        log.info("order saved passed");

        orderItems.forEach(orderItem -> orderItem.setOrder(savedOrder));

        orderItemRepository.saveAll(orderItems); //save order item


        log.info("order items saved");

        // Clear the user's cart after the order is placed
        cartServices.clearChart();

        log.info("shopping cart cleared");

        OrderDto orderDTO = modelMapper.map(savedOrder, OrderDto.class);


        log.info("model mappern mapped savedOrder to OrderDTO");

        // Send email notifications
        sendOrderConfirmationEmail(customer, orderDTO);


        log.info("building response to send");


        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your order has been received! We've sent a secure payment link to your email. Please proceed for payment to confirm your order.")
                .build();
    }

    private void sendOrderConfirmationEmail(User customer, OrderDto orderDto) {
        String subject =  "Your Order Confirmation - Order #" + orderDto.getId();

        //create a Thymeleaf contex and set variables. import the context from Thymeleaf
        Context context = new Context(Locale.getDefault());

        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", String.valueOf(orderDto.getId()));
        context.setVariable("orderDate", orderDto.getOrderDate().toString());
        context.setVariable("totalAmount", orderDto.getTotalAmount().toString());

        // Format delivery address
        String deliveryAddress = orderDto.getUser().getAddress();
        context.setVariable("deliveryAddress", deliveryAddress);

        context.setVariable("currentYear", java.time.Year.now());

        // Build the order items HTML using StringBuilder
        StringBuilder orderItemsHtml = new StringBuilder();

        for (OrderItemDto item : orderDto.getOrderItems()) {
            orderItemsHtml.append("<div class=\"order-item\">")
                    .append("<p>").append(item.getMenu().getName()).append(" x ").append(item.getQuantity()).append("</p>")
                    .append("<p> $ ").append(item.getSubTotal()).append("</p>")
                    .append("</div>");
        }

        context.setVariable("orderItemsHtml", orderItemsHtml.toString());
        context.setVariable("totalItems", orderDto.getOrderItems().size());


        String paymentLink = basePaymentLink + orderDto.getId() + "&amount=" + orderDto.getTotalAmount(); // Replace "yourdomain.com"
        context.setVariable("paymentLink", paymentLink);

        // Process the Thymeleaf template to generate the HTML email body
        String emailBody = templateEngine.process("order-confirmation", context);  // "order-confirmation" is the template name

        notificationService.sendEmail(NotificationDto.builder()
                .recipient(customer.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());
    }

    @Override
    public Response<OrderDto> getOrderById(Long id) {
        log.info("get order by id");

        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("order was not found"));
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);

        return Response.<OrderDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieve successfully")
                .data(orderDto)
                .build();
    }

    @Override
    public Response<Page<OrderDto>> getAllOrder(OrderStatus orderStatus, int page, int size) {
        log.info("get all order");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;

        if(orderStatus != null){
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        }else {
            orderPage = orderRepository.findAll(pageable);
        }

        Page<OrderDto> orderDtoPage = orderPage.map(order -> {
            OrderDto orderDto = modelMapper.map(order, OrderDto.class);
            orderDto.getOrderItems().forEach(orderItemDto -> orderItemDto.getMenu().setReviews(null));
            return orderDto;
        });

        return Response.<Page<OrderDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieve successfully")
                .data(orderDtoPage)
                .build();
    }

    @Override
    public Response<List<OrderDto>> getOrdersOfUser() {
        log.info("get order of user");

        User customer = userService.getCurrentLoggedInUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(customer);
        List<OrderDto> orderDto = orders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .toList();

        orderDto.forEach(orderItem -> {
            orderItem.setUser(null);
            orderItem.getOrderItems().forEach(items -> items.getMenu().setReviews(null));
        });

        return Response.<List<OrderDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieve successfully")
                .data(orderDto)
                .build();
    }

    @Override
    public Response<OrderItemDto> getOrdersItemsById(Long id) {
        log.info("get order items by id");

        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderItemDto orderItemDto = modelMapper.map(orderItem, OrderItemDto.class);
        orderItemDto.setMenu(modelMapper.map(orderItem.getMenu(), MenuDto.class));

        return Response.<OrderItemDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order item retrieve successfully")
                .data(orderItemDto)
                .build();
    }

    @Override
    public Response<OrderDto> updateStatus(OrderDto orderDto) {
        log.info("update status");

        Order order = orderRepository.findById(orderDto.getId()).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatus orderStatus = orderDto.getOrderStatus();
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        return Response.<OrderDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order item retrieve successfully")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        log.info("count unique customer");

        long uniqueCustomerCount = orderRepository.countDistinctUser();

        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order item retrieve successfully")
                .data(uniqueCustomerCount)
                .build();
    }
}
