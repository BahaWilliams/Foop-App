package com.food.FoopApp.payments.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.food.FoopApp.auth_user.domain.dto.UserDto;
import com.food.FoopApp.enums.PaymentGateway;
import com.food.FoopApp.enums.PaymentStatus;
import com.food.FoopApp.order.domain.dto.OrderDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private PaymentGateway paymentGateway;
    private String failureReason;
    private boolean success;
    private LocalDateTime paymentDate;
    private OrderDto order;
    private UserDto user;
}
