package com.backend.kashiapp.transaction.domain.models.enums;

// Dirección de la transacción desde la perspectiva de cada usuario.
// OUTGOING = el emisor envía dinero, INCOMING = el receptor recibe dinero.
public enum TransactionType {

    OUTGOING,
    INCOMING

}
