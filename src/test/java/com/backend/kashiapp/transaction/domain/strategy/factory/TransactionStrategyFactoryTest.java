package com.backend.kashiapp.transaction.domain.strategy.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.common.exception.TransactionStrategyNotFoundException;
import com.backend.kashiapp.transaction.domain.strategy.TransactionStrategy;

@DisplayName("TransactionStrategyFactory")
class TransactionStrategyFactoryTest {

    @Test
    @DisplayName("Debe devolver la estrategia cuyo tipo coincide con la clave solicitada")
    void shouldReturnStrategyWhenTypeMatches() {
        TransactionStrategy transferStrategy = mock(TransactionStrategy.class);
        when(transferStrategy.getType()).thenReturn("TRANSFER");

        TransactionStrategyFactory factory =
                new TransactionStrategyFactory(List.of(transferStrategy));

        TransactionStrategy result = factory.getStrategy("TRANSFER");

        // La estrategia devuelta debe ser la misma instancia registrada en la fábrica.
        assertThat(result).isSameAs(transferStrategy);
    }

    @Test
    @DisplayName("Debe encontrar la estrategia ignorando mayúsculas y minúsculas")
    void shouldReturnStrategyIgnoringCase() {
        // La búsqueda es case-insensitive para que el invocador pueda pasar cualquier capitalización.
        TransactionStrategy transferStrategy = mock(TransactionStrategy.class);
        when(transferStrategy.getType()).thenReturn("TRANSFER");

        TransactionStrategyFactory factory =
                new TransactionStrategyFactory(List.of(transferStrategy));

        assertThat(factory.getStrategy("transfer")).isSameAs(transferStrategy);
        assertThat(factory.getStrategy("Transfer")).isSameAs(transferStrategy);
        assertThat(factory.getStrategy("tRaNsFeR")).isSameAs(transferStrategy);
    }

    @Test
    @DisplayName("Debe elegir la estrategia correcta cuando hay varias registradas")
    void shouldReturnCorrectStrategyWhenMultipleAreRegistered() {
        // Cuando hay varias estrategias registradas la fábrica debe elegir la que tenga el tipo coincidente.
        TransactionStrategy transfer = mock(TransactionStrategy.class);
        when(transfer.getType()).thenReturn("TRANSFER");

        TransactionStrategy recharge = mock(TransactionStrategy.class);
        when(recharge.getType()).thenReturn("RECHARGE");

        TransactionStrategyFactory factory =
                new TransactionStrategyFactory(List.of(transfer, recharge));

        assertThat(factory.getStrategy("TRANSFER")).isSameAs(transfer);
        assertThat(factory.getStrategy("RECHARGE")).isSameAs(recharge);
    }

    @Test
    @DisplayName("Debe lanzar TransactionStrategyNotFoundException si el tipo no está registrado")
    void shouldThrowExceptionWhenTypeIsNotRegistered() {
        TransactionStrategy transferStrategy = mock(TransactionStrategy.class);
        when(transferStrategy.getType()).thenReturn("TRANSFER");

        TransactionStrategyFactory factory =
                new TransactionStrategyFactory(List.of(transferStrategy));

        TransactionStrategyNotFoundException ex = assertThrows(
                TransactionStrategyNotFoundException.class,
                () -> factory.getStrategy("UNKNOWN")
        );
        assertThat(ex.getMessage()).isEqualTo("Estrategia de transacción no encontrada");
    }

    @Test
    @DisplayName("Debe lanzar TransactionStrategyNotFoundException si no hay estrategias registradas")
    void shouldThrowExceptionWhenNoStrategiesAreRegistered() {
        // Un registro vacío también debe lanzar la excepción en lugar de devolver null.
        TransactionStrategyFactory factory =
                new TransactionStrategyFactory(Collections.emptyList());

        assertThrows(
                TransactionStrategyNotFoundException.class,
                () -> factory.getStrategy("TRANSFER")
        );
    }
}
