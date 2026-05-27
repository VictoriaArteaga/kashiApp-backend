package com.backend.kashiapp.integration;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TransactionConcurrencyIntegrationTest extends BaseIntegrationTest {

    private User sender;
    private User receiver;
    private String senderToken;

    private static final BigDecimal INITIAL_SENDER_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal INITIAL_RECEIVER_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        cleanDatabase();

        sender = persistActiveUser("concurrent_sender@test.com", "c_sender", "1111111111", "ID-CONC-SENDER");
        persistWallet(sender.getId(), INITIAL_SENDER_BALANCE);

        receiver = persistActiveUser("concurrent_receiver@test.com", "c_receiver", "2222222222", "ID-CONC-RECEIVER");
        persistWallet(receiver.getId(), INITIAL_RECEIVER_BALANCE);

        senderToken = jwtService.generateToken(sender.getEmail());
    }

    @Test
    @DisplayName("KAN-69 / KAN-275 / TC-005: Múltiples clics en 'Enviar' no deben romper la consistencia ni duplicar el descuento")
    void shouldBlockDuplicateRequestsOnMultipleClicks() throws InterruptedException {
        int numberOfThreads = 5;
        BigDecimal transferAmount = new BigDecimal("100.00");

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(transferAmount);

        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    String jsonRequest = objectMapper.writeValueAsString(request);
                    int status = mockMvc.perform(post("/api/v1/transactions/transfer")
                                    .header("Authorization", "Bearer " + senderToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest))
                            .andReturn().getResponse().getStatus();

                    if (status == 200) {
                        successfulRequests.incrementAndGet();
                    } else {
                        failedRequests.incrementAndGet();
                    }
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS),
                "Todos los hilos debieron terminar dentro del timeout");
        executorService.shutdownNow();

        WalletEntity finalSenderWallet = walletRepository.findByUserId(sender.getId()).orElseThrow();
        WalletEntity finalReceiverWallet = walletRepository.findByUserId(receiver.getId()).orElseThrow();

        int successful = successfulRequests.get();
        int failed = failedRequests.get();

        // 1. Invariante de totalidad: cada hilo terminó como éxito o fallo
        assertEquals(numberOfThreads, successful + failed,
                "Cada hilo debe contabilizarse como exitoso o fallido");

        // 2. Invariante de consistencia del emisor: balance == inicial - (exitosas * monto)
        // Si hay race condition, el balance no encajará con este cálculo.
        BigDecimal expectedSenderBalance =
                INITIAL_SENDER_BALANCE.subtract(transferAmount.multiply(BigDecimal.valueOf(successful)));
        assertEquals(0, expectedSenderBalance.compareTo(finalSenderWallet.getBalance()),
                "El balance del emisor debe ser exactamente initial - (successful * amount). " +
                        "Esperado: " + expectedSenderBalance + ", Real: " + finalSenderWallet.getBalance() +
                        " (successful=" + successful + ", failed=" + failed + "). " +
                        "Diferencia indica race condition o doble descuento.");

        // 3. Invariante de consistencia del receptor
        BigDecimal expectedReceiverBalance =
                INITIAL_RECEIVER_BALANCE.add(transferAmount.multiply(BigDecimal.valueOf(successful)));
        assertEquals(0, expectedReceiverBalance.compareTo(finalReceiverWallet.getBalance()),
                "El balance del receptor debe ser initial + (successful * amount). " +
                        "Esperado: " + expectedReceiverBalance + ", Real: " + finalReceiverWallet.getBalance());

        // 4. El balance del emisor no puede ser negativo
        assertTrue(finalSenderWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "El balance del emisor nunca puede quedar negativo por concurrencia");

        // 5. Idempotencia (TC-005): bajo el comportamiento ideal del flujo de pago,
        //    múltiples clics seguidos sobre el botón "Enviar" deben procesar UNA sola
        //    transferencia (con un idempotency key o lock a nivel de UI/backend).
        //    Si esta aserción falla, el sistema permite descuentos múltiples por doble clic.
        assertEquals(1, successful,
                "TC-005: Solo una de las " + numberOfThreads + " peticiones duplicadas debe procesarse. " +
                        "Se procesaron " + successful + ". Falta deduplicación / idempotencia en el flujo de pago.");
    }

    @Test
    @DisplayName("KAN-274: Múltiples usuarios concurrentes hacia el mismo receptor mantienen consistencia")
    void shouldHandleMultipleUsersConcurrently() throws InterruptedException {
        int numberOfUsers = 5;
        BigDecimal transferAmount = new BigDecimal("50.00");

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfUsers);

        List<String> userTokens = new ArrayList<>();

        for (int i = 0; i < numberOfUsers; i++) {
            User u = persistActiveUser(
                    "user" + i + "@test.com", "user" + i, "1000000" + i, "ID-" + i);
            persistWallet(u.getId(), new BigDecimal("100.00"));
            userTokens.add(jwtService.generateToken(u.getEmail()));
        }

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(transferAmount);

        AtomicInteger successfulRequests = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    latch.await();
                    int status = mockMvc.perform(post("/api/v1/transactions/transfer")
                                    .header("Authorization", "Bearer " + userTokens.get(index))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn().getResponse().getStatus();
                    if (status == 200) {
                        successfulRequests.incrementAndGet();
                    }
                } catch (Exception e) {
                    // contabilizado como fallo
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS),
                "Todos los hilos debieron terminar dentro del timeout");
        executorService.shutdownNow();

        // Se esperan 5 transferencias exitosas de 50 cada una hacia el receptor.
        // El balance debe ser EXACTAMENTE inicial + (exitosas * monto), sin race conditions.
        WalletEntity finalReceiverWallet = walletRepository.findByUserId(receiver.getId()).orElseThrow();

        BigDecimal expectedReceiverBalance =
                INITIAL_RECEIVER_BALANCE.add(transferAmount.multiply(BigDecimal.valueOf(successfulRequests.get())));

        assertEquals(0, expectedReceiverBalance.compareTo(finalReceiverWallet.getBalance()),
                "Race condition detectada: el balance del receptor no coincide con la suma esperada. " +
                        "Esperado: " + expectedReceiverBalance + ", Real: " + finalReceiverWallet.getBalance());

        // Validamos también el escenario ideal: las 5 transferencias deben procesarse exitosamente.
        assertEquals(numberOfUsers, successfulRequests.get(),
                "Todas las transferencias de usuarios distintos deben procesarse exitosamente");
        assertEquals(0, new BigDecimal("750.00").compareTo(finalReceiverWallet.getBalance()),
                "El saldo del receptor debe ser 750 tras 5 transferencias concurrentes de 50");
    }

    @Test
    @DisplayName("KAN-69b: Transferencias inversas A->B y B->A simultáneas no deben interbloquearse")
    void shouldNotDeadlockOnBidirectionalConcurrentTransfers() throws InterruptedException {
        // Para B->A necesitamos el JWT del receptor.
        String receiverToken = jwtService.generateToken(receiver.getEmail());

        // Montos distintos por dirección: así no se activa la deduplicación de duplicados
        // y todas las transferencias deben procesarse.
        List<BigDecimal> amounts = List.of(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("30.00")
        );

        int totalTransfers = amounts.size() * 2; // 3 de A->B + 3 de B->A
        ExecutorService executorService = Executors.newFixedThreadPool(totalTransfers);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalTransfers);
        AtomicInteger successfulRequests = new AtomicInteger(0);

        // A -> B (emisor hacia receptor)
        for (BigDecimal amount : amounts) {
            submitTransfer(executorService, latch, doneLatch,
                    senderToken, receiver.getEmail(), amount, successfulRequests);
        }
        // B -> A (receptor hacia emisor), en sentido inverso y al mismo tiempo
        for (BigDecimal amount : amounts) {
            submitTransfer(executorService, latch, doneLatch,
                    receiverToken, sender.getEmail(), amount, successfulRequests);
        }

        latch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS),
                "Todos los hilos debieron terminar dentro del timeout (sin interbloqueo)");
        executorService.shutdownNow();

        // Si hubiera interbloqueo, la base de datos abortaría una transacción y esa transferencia
        // no llegaría a 200. Que las 6 terminen en éxito demuestra que el orden de locks lo evita.
        assertEquals(totalTransfers, successfulRequests.get(),
                "Todas las transferencias inversas deben completarse sin interbloqueo");

        // Conservación del dinero: A envía 60 y recibe 60; B envía 60 y recibe 60.
        WalletEntity walletA = walletRepository.findByUserId(sender.getId()).orElseThrow();
        WalletEntity walletB = walletRepository.findByUserId(receiver.getId()).orElseThrow();

        assertEquals(0, INITIAL_SENDER_BALANCE.compareTo(walletA.getBalance()),
                "El saldo de A debe volver a su valor inicial (envía 60, recibe 60)");
        assertEquals(0, INITIAL_RECEIVER_BALANCE.compareTo(walletB.getBalance()),
                "El saldo de B debe volver a su valor inicial (envía 60, recibe 60)");
        assertEquals(0, INITIAL_SENDER_BALANCE.add(INITIAL_RECEIVER_BALANCE)
                        .compareTo(walletA.getBalance().add(walletB.getBalance())),
                "El dinero total del sistema debe conservarse");
    }

    // Encola una transferencia que se dispara cuando se libera 'latch' y contabiliza los éxitos.
    private void submitTransfer(ExecutorService executorService,
                                CountDownLatch latch,
                                CountDownLatch doneLatch,
                                String token,
                                String recipientEmail,
                                BigDecimal amount,
                                AtomicInteger successfulRequests) {
        executorService.submit(() -> {
            try {
                latch.await();
                TransactionRequestDTO request = new TransactionRequestDTO();
                request.setRecipientEmail(recipientEmail);
                request.setAmount(amount);

                int status = mockMvc.perform(post("/api/v1/transactions/transfer")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn().getResponse().getStatus();

                if (status == 200) {
                    successfulRequests.incrementAndGet();
                }
            } catch (Exception e) {
                // contabilizado como fallo
            } finally {
                doneLatch.countDown();
            }
        });
    }
}
