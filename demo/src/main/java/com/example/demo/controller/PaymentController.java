package com.example.demo.controller;

import com.example.demo.service.PaymentService;
import com.mercadopago.exceptions.MPApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pix")
    public ResponseEntity<?> gerarPix(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            Map<String, Object> resposta = paymentService.createPixPayment(userId);
            return ResponseEntity.ok(resposta);
        } catch (MPApiException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "erro", e.getMessage(),
                    "detalhes", e.getApiResponse().getContent()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
        }
    }
}