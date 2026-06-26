package com.food.FoopApp.payments.services;

import com.food.FoopApp.payments.domain.dto.PaymentDto;
import com.food.FoopApp.response.Response;

import java.util.List;

public interface PaymentService {
    Response<?> initializePayment(PaymentDto paymentDto);
    Response<PaymentDto> getPaymentById(Long paymentId);
    void updatePayment(PaymentDto paymentDto);
    Response<List<PaymentDto>> getAllPayment();

}
