package com.backend.kashiapp.common.exception;

// Se lanza cuando llega una transferencia idéntica (mismo emisor, destinatario y monto)
// dentro de una ventana de tiempo muy corta. Sirve para descartar los envíos duplicados
// que provoca el doble/múltiple clic sobre el botón "Enviar" (idempotencia del pago).
public class DuplicateTransferException extends RuntimeException {

    public DuplicateTransferException(String message) {
        super(message);
    }
}
