# Reembolsos Integrado — Proyecto integrador

Post-contenido de la Unidad 12 de Patrones de Diseño de Software.
Sistema para registrar, validar, calcular y notificar solicitudes de
reembolso de gastos del área de Finanzas, construido como un único
proyecto Spring Boot (`reembolsos-integrado`).

## Diseño Propuesto

Esta sección se escribió antes de implementar los patrones. Para cada
requisito se identifica qué es lo que varía, qué patrón lo resuelve y
por qué ese y no otro.

### RF1 — Validación en varias etapas
- **Qué varía.** Cuántas verificaciones debe pasar una solicitud y
  cuáles son. Hoy son tres (tope por categoría, documentos soporte,
  duplicados) y Finanzas ya anunció una cuarta, el límite anual
  acumulado por empleado. Cada una puede rechazar por su cuenta, con
  su propio motivo.
- **Patrón: Chain of Responsibility.**
- **Justificación en una oración.** Cada verificación es un eslabón
  independiente que rechaza la solicitud o la pasa al siguiente, de
  modo que agregar una verificación es agregar un eslabón sin tocar
  los que ya existen.
- **Por qué no otro.** Tres `if` seguidos dentro del servicio
  funcionan hoy, pero cada verificación nueva obliga a modificar el
  servicio y a volver a probarlo, que es lo que el requisito pide
  evitar. Decorator tampoco encaja: no se le agrega comportamiento a
  la solicitud, se decide si continúa o se detiene.

### RF2 — Cálculo distinto por categoría de gasto
- **Qué varía.** La fórmula del monto aprobado: una por categoría
  (viáticos, representación, capacitación), con más categorías
  anunciadas ("bienestar"). La categoría se conoce al crear la
  solicitud y no cambia después.
- **Patrón: Strategy.**
- **Justificación en una oración.** Cada categoría es un algoritmo
  intercambiable con la misma entrada y la misma salida, así que una
  categoría nueva es una clase nueva y las fórmulas existentes no se
  tocan.
- **Por qué no otro.** Un `switch` sobre `CategoriaGasto` obliga a
  editar el mismo método con cada categoría. State no aplica porque
  la solicitud no cambia de categoría ni hay transiciones. Tampoco
  hace falta una Factory propia: las calculadoras las crea el
  contenedor de Spring, y elegir la correcta es una búsqueda en un
  mapa, no un problema de creación.

### RF3 — Notificación simultánea a varios interesados
- **Qué varía.** Quiénes se enteran de un cambio de estado y qué hace
  cada uno. Hoy son tres con reacciones distintas (empleado,
  Contabilidad solo en aprobaciones, auditoría siempre) y Finanzas ya
  mencionó un cuarto, el panel de métricas.
- **Patrón: Observer.**
- **Justificación en una oración.** El servicio que cambia el estado
  avisa a una lista de interesados sin conocer cuántos son ni qué
  hace cada uno, y sumar un interesado no modifica ni al servicio ni a
  los demás.
- **Por qué no otro.** Llamar a los tres desde el servicio lo acopla
  a cada interesado y lo obliga a cambiar con cada uno nuevo. Chain of
  Responsibility sería un error aquí: en una cadena un eslabón puede
  cortar el flujo, y en este requisito todos deben enterarse siempre.

### Por qué tres patrones y no más
Cada patrón resuelve un problema que los otros dos no resuelven:
decidir si la solicitud continúa (RF1), con qué fórmula se calcula
(RF2) y quién se entera (RF3). En los tres casos la variación está
escrita en el requisito, no supuesta. `SolicitudReembolsoService`
coordina los tres y es el único punto de entrada para el controlador,
pero no se cuenta como un cuarto patrón agregado: es el servicio de
aplicación que existiría de todas formas.

| Requisito | Qué varía | Patrón | Paquete |
|-----------|-----------|--------|---------|
| RF1 | Las verificaciones previas a la aprobación | Chain of Responsibility | `procesamiento.validacion` |
| RF2 | La fórmula del monto aprobado | Strategy | `procesamiento.calculo` |
| RF3 | Los interesados en un cambio de estado | Observer | `procesamiento.notificacion` |

## Decisiones de diseño

### Decisión 1 — Orden de la cadena: documentos, monto, duplicados
El eslabón más caro va al final.

| Eslabón | Qué hace | Costo |
|---------|----------|-------|
| `ValidadorDocumentosSoporte` | Lee un booleano de la solicitud | Constante |
| `ValidadorMontoMaximo` | Busca el tope en un mapa y compara | Constante |
| `ValidadorDuplicados` | Recorre las solicitudes pendientes del empleado | Crece con la lista |

- **Qué se gana.** Una solicitud que falla por algo evidente (sin
  documentos o por encima del tope) se rechaza sin recorrer la lista
  de pendientes. Si duplicados fuera primero, todas las solicitudes
  pagarían ese recorrido, incluidas las que iban a fallar por un
  booleano.
- **Qué se pierde.** El empleado solo ve el primer motivo. Si a la
  solicitud le faltan documentos y además está duplicada, corrige los
  documentos y recién entonces se entera del duplicado.
  `CadenaValidadoresTest` deja fijo ese comportamiento.
- **Límite de la ganancia.** `SolicitudReembolsoService.crear()`
  consulta las pendientes en la base de datos antes de llamar a la
  cadena, así que esa consulta se paga siempre; el orden solo ahorra
  el recorrido en memoria. Para ahorrar también la consulta habría que
  entregar la lista de forma diferida, lo que cambia la firma de los
  validadores y hoy no se justifica: un empleado tiene pocas
  solicitudes pendientes.

### Decisión 2 — Selección de la calculadora con un mapa armado por Spring
`SelectorCalculadora` recibe la `List<CalculadoraReembolso>` que
Spring inyecta con todos los `@Component` y arma un mapa por
categoría. Se descartó un `switch` sobre `CategoriaGasto`.

- **Frente al RF2.** Para agregar "bienestar" se crea
  `CalculadoraBienestar` con `@Component` y el selector, el servicio y
  las tres calculadoras existentes no cambian. Con el `switch` habría
  que editar el selector con cada categoría, que es justo lo que el
  requisito pide evitar, y si se olvida la rama el error aparece en
  ejecución.
- **Qué controla el mapa.** Dos calculadoras con la misma categoría
  hacen fallar `Collectors.toMap` al arrancar, no en la primera
  solicitud. Una categoría sin calculadora termina en
  `IllegalArgumentException("Categoria no soportada")`, cubierto por
  `SelectorCalculadoraTest`.
- **Costo.** La selección deja de estar escrita en un solo lugar: para
  saber qué categorías están soportadas hay que buscar las
  implementaciones de la interfaz.
- **No es una Factory.** El selector no crea nada; las calculadoras
  las construye el contenedor. Por eso no se agregó un Factory Method
  propio encima de la inyección de dependencias.
