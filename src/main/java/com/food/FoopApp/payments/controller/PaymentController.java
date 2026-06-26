package com.food.FoopApp.payments.controller;

import com.food.FoopApp.payments.domain.dto.PaymentDto;
import com.food.FoopApp.payments.services.PaymentService;
import com.food.FoopApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Response<?>> createPayment(@RequestBody @Valid PaymentDto paymentDto){
        return ResponseEntity.ok(paymentService.initializePayment(paymentDto));
    }

    @PutMapping("/update")
    public void updatePayment(@RequestBody PaymentDto paymentDto){
        paymentService.updatePayment(paymentDto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<PaymentDto>>> getAllPayment(){
        return ResponseEntity.ok(paymentService.getAllPayment());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Response<PaymentDto>> getPaymentById(@PathVariable Long paymentId){
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
