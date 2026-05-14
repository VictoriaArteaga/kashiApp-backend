package com.backend.kashiapp.transaction.application.listener;

// Clase provicional hasta la implementación del módulo notification.

import com.backend.kashiapp.transaction.domain.models.TransferCompletedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Escucha eventos de transferencia completada y notifica a ambos usuarios
// Solo se ejecuta después del commit, así nunca se notifica algo que se revirtió.

@Component
public class TransferNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    TransferNotificationListener.class
            );

    // Se dispara automáticamente cuando TransferCompletedEvent se publica.
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onTransferCompleted(
            TransferCompletedEvent event
    ) {

        // Log general de la transferencia para trazabilidad.
        log.info(
                "Transferencia exitosa [ref={}]: "
                        + "Usuario {} envió ${} a usuario {}",
                event.getTransferReference(),
                event.getSenderUserId(),
                event.getAmount(),
                event.getRecipientUserId()
        );

        // Notificación al emisor.
        notifyUser(
                event.getSenderUserId(),
                "Transferencia enviada exitosamente por $"
                        + event.getAmount()
                        + " (ref: "
                        + event.getTransferReference()
                        + ")"
        );

        // Notificación al receptor.
        notifyUser(
                event.getRecipientUserId(),
                "Has recibido una transferencia por $"
                        + event.getAmount()
                        + " (ref: "
                        + event.getTransferReference()
                        + ")"
        );
    }

    private void notifyUser(
            java.util.UUID userId, String message
    ) {

        // Por ahora usamos el log (SE DEBE cambiar cuando exista el módulo de notificaciones).
        log.info(
                "Notificación para usuario {}: {}",
                userId,
                message
        );
    }
}
