package com.food.FoopApp.payments.services;

import com.food.FoopApp.email_notification.domain.dto.NotificationDto;
import com.food.FoopApp.email_notification.services.NotificationService;
import com.food.FoopApp.enums.OrderStatus;
import com.food.FoopApp.enums.PaymentGateway;
import com.food.FoopApp.enums.PaymentStatus;
import com.food.FoopApp.exceptions.BadRequestException;
import com.food.FoopApp.exceptions.NotFoundException;
import com.food.FoopApp.order.domain.entity.Order;
import com.food.FoopApp.order.repository.OrderRepository;
import com.food.FoopApp.payments.domain.dto.PaymentDto;
import com.food.FoopApp.payments.domain.entity.Payment;
import com.food.FoopApp.payments.repository.PaymentRepository;
import com.food.FoopApp.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;
    private final ModelMapper modelMapper;

    @Value("${stripe.api.secret.key}")
    private String stripeSecretKey;

    @Value("${frontend.base.url}")
    private String frontendUrl;

    @Override
    public Response<?> initializePayment(PaymentDto paymentDto) {
        log.info("initializePayment");

        Stripe.apiKey = stripeSecretKey;
        Long orderId = paymentDto.getOrderId();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

        if(order.getPaymentStatus() == PaymentStatus.COMPLETED){
            throw new BadRequestException("Payment already made from this order");
        }

        if(order.getTotalAmount() == null){
            throw new BadRequestException("Amount you are passing is null");
        }

        if(order.getTotalAmount().compareTo(paymentDto.getAmount()) != 0){
            throw new BadRequestException("Amount does not have it");
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentDto.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("sgd")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId = intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("success")
                    .data(uniqueTransactionId)
                    .build();

        }catch (Exception e){
            throw new RuntimeException("Error creating payment unique transaction");
        }
    }

    @Override
    public Response<PaymentDto> getPaymentById(Long paymentId) {
        log.info("getPaymentById");

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        PaymentDto paymentDto = modelMapper.map(payment, PaymentDto.class);

        paymentDto.getUser().setRoles(null);
        paymentDto.getOrder().setUser(null);
        paymentDto.getOrder().getOrderItems().forEach(item->{
            item.getMenu().setReviews(null);
        });

        return Response.<PaymentDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("payment retreived succeessfully by id")
                .data(paymentDto)
                .build();
    }

    @Override
    public void updatePayment(PaymentDto paymentDto) {
        log.info("updatePayment");

        Long orderId = paymentDto.getOrderId();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentDto.getAmount());
        payment.setTransactionId(payment.getTransactionId());
        payment.setPaymentStatus(paymentDto.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());

        if(!paymentDto.isSuccess()){
            payment.setFailureReasons(paymentDto.getFailureReason());
        }

        paymentRepository.save(payment);

        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("orderId", order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount", "$" + paymentDto.getAmount());

        if(paymentDto.isSuccess()){
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // Add success-specific variables
            context.setVariable("transactionId", payment.getTransactionId());
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            context.setVariable("frontendBaseUrl", this.frontendUrl);

            String emailBody = templateEngine.process("payment-success", context);

            log.info("HAVE GOTTEN TEMPLATE");

            notificationService.sendEmail(NotificationDto.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Successful - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());

        }else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            log.info("PAYMENT IS FAILED ABOUT TO SEND EMAIL");

            // Add success-specific variables
            String emailBody = templateEngine.process("payment-failed", context);

            notificationService.sendEmail(NotificationDto.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Failed - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        }

    }

    @Override
    public Response<List<PaymentDto>> getAllPayment() {
        log.info("getAllPayment");

        List<Payment> paymentList = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PaymentDto> paymentDtoList = paymentList.stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .toList();

        paymentDtoList.forEach(item -> {
            item.setOrder(null);
            item.setUser(null);
        });

        return Response.<List<PaymentDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("payment retreived succeessfully")
                .data(paymentDtoList)
                .build();
    }
}
