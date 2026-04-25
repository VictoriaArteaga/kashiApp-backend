# KashiApp - Backend Services

KashiApp es una billetera digital robusta diseñada bajo los principios de **Clean Architecture** y **Arquitectura Hexagonal**. El proyecto está enfocado en la modularidad, la seguridad y el cumplimiento de estándares de calidad **ISO 25010**.

---

##  Arquitectura del Sistema: Puertos y Adaptadores

Para mantener el proyecto ordenado y cumplir con las normas ISO 25010, dividimos cada módulo en tres capas obligatorias. Regla general: Las dependencias siempre apuntan hacia adentro (domain).

###  Estructura de Capas por Módulo

Cada módulo funcional se divide en cuatro carpetas principales:

1.  **`domain` (El Núcleo)**
    * **Models:** Entidades puras de Java que contienen la lógica y reglas de negocio.
    * **Repository:** Interfaces que definen las operaciones de persistencia.
    * **Service:** Interfaces de servicios de dominio para reglas complejas.

2.  **`application` (La Orquestación / Puertos)**
    * **Use Cases:** Implementación de las acciones del usuario (ej: `TransferMoneyUseCase`).
    * **DTOs:** Objetos para la transferencia de datos entre la API y el cliente (frontend).
    * **Mappers:** Clases encargadas de transformar objetos entre capas (No ensuciar la lógica con getters y setters manuales).

3.  **`infrastructure` (Adaptadores de Salida y Configuración)**
    * **Persistence:** Implementaciones JPA y entidades de base de datos (`@Entity`).
    * **Config:** Configuración manual de Beans para mantener el dominio libre de anotaciones de Spring.
    * **External/Security:** Adaptadores para servicios externos (PDF, Email, JWT).

4. **`presentation` (Adaptadores de entrada)**
   * Controllers: Aquí van los @RestController. Su única función es recibir la petición, llamar al UseCase correspondiente y devolver la respuesta al cliente (frontend).

---

##  Módulos del Proyecto
 
| Módulo            | Descripción                                                                                   | Estado                             |
|:------------------|:----------------------------------------------------------------------------------------------|:-----------------------------------|
| **User**          | Registro, perfiles y gestión de autenticación con JWT.                                        | Base lista                         |
| **Wallet**        | Gestión de saldos, cuentas y visibilidad financiera.                                          | Base lista                         |
| **Transaction**   | Procesamiento atómico de transferencias y movimientos.                                        | Base lista                         |
| **Contact**       | Agenda de contactos para transferencias rápidas.                                              | Base lista                         |
| **Report**        | Generación de estadísticas y reportes exportables en PDF.                                     | Base lista                         |
| **Notification**  | Sistema de alertas transaccionales.                                                           | Base lista                         |
| **Admin**         | Supervisión de métricas del sistema y gestión de estados.                                     | Base lista                         |
| **Common**        | Centralización de excepciones (`GlobalHeaderException`) y estandarización de respuestas JSON. | Base lista                         |

---

##  Configuración Global

El paquete raíz `config/` contiene las configuraciones transversales que aseguran la seguridad y la interoperabilidad de la plataforma:
* **SecurityConfig:** Políticas de acceso y filtros de seguridad.
* **CorsConfig:** Configuración de intercambio de recursos para el frontend en React.
* **GlobalConfig:** Auditorías JPA y configuraciones de serialización.

---

## Reglas que deben tener en cuenta

* **Clean domain:** No usen anotaciones de Spring (@Service, @Autowired, @Component) dentro de la carpeta domain.
* **Excepciones:** Si van a capturar un fallo, utilicen las excepciones de `common`. No creen nuevos errores locales sin avisar.
* **Naming:** Ingles para variables y clases / español para comentarios.
* **Inyección de dependecias:** Todo se conecta en la carpeta config de cada módulo.

## Reglas para el uso de ramas
* Siempre usen minúsiculas.
* Utilicen guiones, no usen espacios ni barras bajas.
* No coloquen un parrafo, con 3 a 4 palabras esta bien.
* El nombre debe ser significativo con la función o módulo que están tocando.
* Utilicen la convención estándar: `feature/`: para nuevas funcionalidades, `fix/ o bugfix/`: para corregir errores, `refactor/`: Cuando se hacen mejoras al código, pero no cambian como funciona, `docs/`: Si solo suben READMES.md
