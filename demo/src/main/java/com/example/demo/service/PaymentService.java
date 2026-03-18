package com.example.demo.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final CartService cartService;
    private final UserService userService;

    public PaymentService(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    // Gera PIX com base no carrinho do usuário
    public Map<String, Object> createPixPayment(Long userId) throws Exception {

        MercadoPagoConfig.setAccessToken(accessToken);

        // Pega email e total dinamicamente do carrinho
        String emailPagador = userService.getUserById(userId).getEmail();
        Double total = cartService.getCartTotal(userId);

        if (total == null || total <= 0) {
            throw new RuntimeException("Carrinho vazio ou valor inválido");
        }

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(new BigDecimal(String.valueOf(total)))
                .description("Compra Young Zone")
                .paymentMethodId("pix")
                .payer(
                        PaymentPayerRequest.builder()
                                .email(emailPagador)
                                .firstName("Test")
                                .lastName("User")
                                .identification(
                                        IdentificationRequest.builder()
                                                .type("CPF")
                                                .number("19119119100")
                                                .build()
                                )
                                .build()
                )
                .build();

        PaymentClient client = new PaymentClient();
        Payment payment = client.create(request);

        Map<String, Object> response = new HashMap<>();
        response.put("id", payment.getId());
        response.put("status", payment.getStatus());
        response.put("qr_code", payment.getPointOfInteraction().getTransactionData().getQrCode());
        response.put("qr_code_base64", payment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
        response.put("total", total);

        return response;
    }
}
