package com.backend.kashiapp.transaction.domain.strategy.factory;

import com.backend.kashiapp.common.exception.TransactionStrategyNotFoundException;
import com.backend.kashiapp.transaction.domain.strategy.TransactionStrategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

// Implementación de patrones de software.
// Fábrica que resuelve qué estrategia usar según el tipo de transacción.
// Spring inyecta automáticamente todas las implementaciones de TransactionStrategy.
@Component
@RequiredArgsConstructor
public class TransactionStrategyFactory {

    // Lista con todas las estrategias registradas en el contexto.
    private final List<TransactionStrategy>
            strategies;

    // Busca la estrategia que coincida con el tipo solicitado.
    public TransactionStrategy getStrategy(
            String type
    ) {

        return strategies.stream()
                .filter(strategy ->
                        strategy.getType()
                                .equalsIgnoreCase(type)
                )
                .findFirst()
                .orElseThrow(() ->
                        new TransactionStrategyNotFoundException(
                                "Estrategia de transacción no encontrada"
                        )
                );
    }
}
