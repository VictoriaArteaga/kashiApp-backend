package com.backend.kashiapp.transaction.application.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.backend.kashiapp.transaction.domain.models.TransferCompletedEvent;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@DisplayName("TransferNotificationListener")
class TransferNotificationListenerTest {

    private TransferNotificationListener listener;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setup() {
        listener = new TransferNotificationListener();
        logger = (Logger) LoggerFactory.getLogger(TransferNotificationListener.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("Debe loguear la transferencia y notificar a emisor y receptor")
    void shouldLogTransferAndNotifyBothUsers() {
        // Se esperan tres entradas de log: un resumen general más una notificación por usuario.
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        UUID reference = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("75.00");

        TransferCompletedEvent event =
                new TransferCompletedEvent(sender, recipient, amount, reference);

        listener.onTransferCompleted(event);

        List<ILoggingEvent> logs = appender.list;

        assertThat(logs).hasSize(3);

        // El log de traza debe incluir la referencia, ambos ids de usuario y el monto.
        assertThat(logs.get(0).getFormattedMessage())
                .contains(reference.toString())
                .contains(sender.toString())
                .contains(recipient.toString())
                .contains(amount.toString());

        // La segunda línea de log es la notificación para el emisor.
        assertThat(logs.get(1).getFormattedMessage())
                .contains(sender.toString())
                .contains("enviada")
                .contains(amount.toString());

        // La tercera línea de log es la notificación para el receptor.
        assertThat(logs.get(2).getFormattedMessage())
                .contains(recipient.toString())
                .contains("recibido")
                .contains(amount.toString());
    }
}
