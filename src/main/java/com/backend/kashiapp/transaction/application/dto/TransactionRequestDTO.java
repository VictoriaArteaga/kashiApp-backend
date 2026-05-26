package com.backend.kashiapp.transaction.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequestDTO {

    // Email del usuario que va a recibir el dinero.
    @NotBlank(
            message = "El email del destinatario es obligatorio"
    )
    @Email(
            message = "Formato de email inválido"
    )
    private String recipientEmail;


    // Monto a transferir, debe ser positivo y no nulo.
    @NotNull(
            message = "El monto es obligatorio"
    )
    @Positive(
            message = "El monto debe ser mayor a cero"
    )
    private BigDecimal amount;

}