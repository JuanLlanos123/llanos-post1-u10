### Decisión de diseño 1: selección del entorno de análisis de calidad

Para realizar el análisis de calidad del código se seleccionó **SonarQube local mediante Docker**, en lugar de SonarCloud. Esta decisión se fundamentó principalmente en la independencia de la conectividad a internet y en el control de la configuración del entorno. Al ejecutar SonarQube localmente, es posible realizar los análisis sin depender de los límites de un plan gratuito ni de la disponibilidad de una plataforma externa. Además, se tiene mayor control sobre la configuración del servidor y la administración de los resultados obtenidos durante el laboratorio.

Otro criterio considerado fue la privacidad y disponibilidad del proyecto. Aunque SonarCloud ofrece la ventaja de no requerir instalación y permite consultar el dashboard desde cualquier equipo mediante un enlace, necesita una cuenta y conexión a internet. Además, los proyectos configurados como públicos pueden ser consultados por otras personas. Para este trabajo se consideró conveniente mantener el análisis dentro del entorno local.

Sin embargo, elegir SonarQube local también implica aceptar ciertos costos. La ejecución mediante Docker consume recursos de la máquina, como memoria y capacidad de procesamiento, y requiere mantener activo el contenedor durante las actividades de análisis. A pesar de estas limitaciones, se consideró que las ventajas de independencia, control y disponibilidad local justificaban esta elección para el desarrollo del laboratorio.

En conclusión, SonarQube local fue seleccionado porque ofrece un entorno de análisis controlado e independiente de servicios externos, características que resultan convenientes para evaluar la complejidad del código, la cobertura de pruebas y otros indicadores de calidad que orientan las decisiones de refactorización.


### Decisión de diseño 2: exclusiones del análisis de calidad

Antes de ejecutar el análisis de calidad con SonarQube, se decidió no excluir ninguna clase del análisis general mediante la propiedad `sonar.exclusions`. Esta decisión se tomó para mantener una evaluación completa del proyecto y evitar que algún archivo con posibles problemas de calidad quedara fuera de las métricas. De esta manera, tanto `ServicioCalculoEnvio` como las demás clases del proyecto permanecen dentro del alcance del análisis.

Se consideró la posibilidad de excluir la clase de arranque `App`, en caso de que su única responsabilidad fuera iniciar la aplicación mediante el método `main()`, sin contener lógica de negocio. Sin embargo, se prefirió no excluirla del análisis general, ya que hacerlo también impediría que SonarQube evaluara otros aspectos de calidad de ese archivo, como posibles errores o problemas de mantenimiento.

Es importante diferenciar entre excluir una clase del análisis y excluirla únicamente del cálculo de cobertura. Si se comprueba que `App` contiene exclusivamente código de arranque y que su falta de pruebas no aporta información relevante sobre la cobertura de la lógica de negocio, se podría utilizar la propiedad `sonar.coverage.exclusions` para excluirla únicamente de esa métrica, sin dejar de analizarla en SonarQube.

Por tanto, se decidió conservar inicialmente todas las clases dentro del análisis y no establecer exclusiones generales. Esta elección permite obtener una visión completa del estado del código y evita modificar artificialmente los resultados de calidad sin una justificación técnica suficiente.



Pregunta 1. ¿Qué método tiene la complejidad ciclomática más alta?

El método ServicioCalculoEnvio.calcularCosto(String, double, double, String, boolean), con una complejidad ciclomática de 20.

Esto significa que concentra numerosos caminos independientes de ejecución y decisiones condicionales. Por tanto, requiere mayor atención al momento de diseñar pruebas y mantener su lógica.

Pregunta 2. ¿Qué porcentaje de cobertura de ramas tiene ese método?

Su cobertura de ramas es del 18 %.

De las 38 ramas identificadas, solamente 7 están cubiertas y 31 permanecen sin ejercitar. Esto evidencia que las pruebas actuales no comprueban adecuadamente las diferentes alternativas de ejecución del método.

Es importante distinguir que su cobertura de instrucciones es del 13 %, mientras que la cobertura de ramas es del 18 %.

Pregunta 3. ¿La complejidad alta y la cobertura baja corresponden al mismo método?

Sí. Ambas señales coinciden en calcularCosto().

El método presenta una complejidad ciclomática de 20 y una cobertura de ramas del 18 %. Esto permite identificar una situación en la que existe una cantidad considerable de caminos de ejecución, pero las pruebas actuales solamente recorren una pequeña parte de ellos.

### Decisión de diseño 3: selección del método para refactorizar mediante métricas de calidad

Para seleccionar el método que se intervendrá en la Parte 2, se analizaron las métricas de complejidad y cobertura obtenidas mediante JaCoCo y el dashboard de análisis. Se decidió utilizar la complejidad ciclomática como señal principal, porque permite identificar los métodos que concentran una mayor cantidad de caminos independientes de ejecución y decisiones condicionales, lo que puede incrementar la dificultad de su mantenimiento y de la construcción de pruebas unitarias.

En el análisis de `ServicioCalculoEnvio` se identificó que el método `calcularCosto(String, double, double, String, boolean)` presenta una complejidad ciclomática de **20**, mientras que el constructor tiene una complejidad de 1. Por tanto, el método concentra prácticamente toda la complejidad de la clase. Adicionalmente, el reporte de JaCoCo muestra que su cobertura de ramas es del **18 %**: de las 38 ramas identificadas, solamente 7 están cubiertas y 31 permanecen sin ejercitar. También presenta una cobertura de instrucciones del 13 %, lo que refuerza la necesidad de revisar sus pruebas.

La complejidad ciclomática y la cobertura de ramas aportan información diferente, pero complementaria. La primera permite identificar la cantidad de caminos de decisión del método, mientras que la segunda muestra qué proporción de sus alternativas está siendo recorrida por las pruebas. En este caso, ambas señales coinciden en `calcularCosto()`, por lo que se justifica su selección como candidato para la refactorización.

Aunque la complejidad cognitiva también es útil para evaluar la dificultad de comprensión del código, no se utilizó como señal principal porque el objetivo de esta intervención es identificar y abordar la concentración de caminos de decisión, además de mejorar las condiciones para probarlos. Por esta razón, se priorizó la complejidad ciclomática y se utilizó la cobertura de ramas como evidencia complementaria para orientar las pruebas posteriores.






### Decisión de diseño 4: selección del patrón para reducir la complejidad ciclomática

Antes de realizar la refactorización de `ServicioCalculoEnvio.calcularCosto()`, se analizó la naturaleza de las decisiones condicionales presentes en el método para determinar qué patrón de diseño permitiría organizar mejor su lógica. Se consideraron tres alternativas: Strategy, State y Chain of Responsibility, teniendo en cuenta el comportamiento que representa cada una y no solamente su capacidad para reducir la complejidad ciclomática.

El análisis permitió identificar que el método recibe `metodoEnvio` como parámetro y utiliza su valor para seleccionar una fórmula de cálculo específica. Aunque las condiciones son diferentes, todas las ramas persiguen el mismo objetivo: obtener un costo de envío. Cada alternativa representa un algoritmo independiente que produce el mismo tipo de resultado, por lo que la selección se realiza una sola vez según el método de envío solicitado, sin depender de un estado anterior ni de una secuencia de evaluaciones.

Por estas razones, se seleccionó el patrón **Strategy**, ya que permite encapsular cada fórmula de cálculo en una implementación independiente que comparte una misma interfaz. De esta manera, `ServicioCalculoEnvio` puede delegar el cálculo a la estrategia correspondiente sin mantener todas las fórmulas y sus condicionales dentro de un único método. Además, esta estructura facilita incorporar nuevas modalidades de envío sin tener que modificar constantemente la lógica principal del servicio.

Se descartó **State** porque el comportamiento del método no depende de un estado interno que se conserve entre llamadas ni existen transiciones válidas entre diferentes estados del objeto. La selección de la fórmula depende directamente del parámetro `metodoEnvio`, no de una situación previa que modifique el comportamiento futuro.

También se descartó **Chain of Responsibility** porque no existe una secuencia de manejadores que deban evaluar una solicitud hasta encontrar quién puede resolverla. Las alternativas no necesitan ejecutarse en un orden determinado ni varios responsables pueden intervenir sucesivamente; simplemente se selecciona una fórmula de cálculo según el valor recibido.

En conclusión, Strategy representa mejor la naturaleza del problema porque permite separar algoritmos intercambiables que realizan la misma operación con fórmulas diferentes. La decisión no se tomó únicamente por la necesidad de reducir la complejidad ciclomática, sino porque el patrón corresponde al comportamiento real del método. Con esta refactorización se busca disminuir la complejidad concentrada en `calcularCosto()`, mejorar la organización del código y facilitar las pruebas individuales de cada estrategia de cálculo.

### Decisión de diseño 5: correspondencia entre las ramas sin cubrir y las nuevas pruebas

Antes de realizar la refactorización, se revisó el reporte HTML de JaCoCo de la Parte 1 para identificar las condiciones que no estaban siendo ejercitadas en `ServicioCalculoEnvio.calcularCosto()`. El análisis inicial mostró una complejidad ciclomática de 20 y una cobertura de ramas del 18 %, equivalente a 7 ramas cubiertas de un total de 38, mientras que 31 permanecían sin cubrir. Estos resultados evidenciaron que las pruebas originales no contemplaban adecuadamente las diferentes alternativas de cálculo y las condiciones presentes en el método.

A partir de este diagnóstico se aplicó el patrón Strategy para separar las fórmulas de cálculo en cuatro implementaciones independientes: `EnvioEstandarStrategy`, `EnvioExpressStrategy`, `EnvioInternacionalStrategy` y `EnvioOvernightStrategy`. La incorporación de pruebas unitarias permitió evaluar individualmente los comportamientos de estas implementaciones, comprobando las diferentes alternativas de ejecución y verificando los resultados obtenidos para cada modalidad de envío.

La correspondencia entre los comportamientos que necesitaban cobertura y las pruebas posteriores se organizó de la siguiente manera:

| Comportamiento que debía comprobarse | Nueva clase evaluada | Evidencia en JaCoCo |
|---|---|---|
| Alternativas de cálculo del envío estándar | `EnvioEstandarStrategy` | 6 ramas cubiertas de 6 |
| Alternativas de cálculo del envío express | `EnvioExpressStrategy` | 6 ramas cubiertas de 6 |
| Alternativas de cálculo del envío internacional | `EnvioInternacionalStrategy` | 6 ramas cubiertas de 6 |
| Alternativas de cálculo del envío overnight | `EnvioOvernightStrategy` | 6 ramas cubiertas de 6 |
| Decisiones de selección y delegación del servicio | `ServicioCalculoEnvio` | 8 ramas cubiertas de 8 |

Adicionalmente, las pruebas del servicio permiten comprobar que la modalidad recibida conduce a la estrategia correspondiente y que el resultado del cálculo se obtiene mediante la implementación seleccionada. De esta manera, se evalúa tanto el funcionamiento individual de los algoritmos como su integración con el servicio principal.

Después de incorporar las nuevas pruebas y ejecutar nuevamente JaCoCo, se obtuvo una cobertura de ramas del 100 % en el paquete de estrategias y en el paquete del servicio. Las cuatro estrategias alcanzaron el 100 % de cobertura de instrucciones y ramas, mientras que `ServicioCalculoEnvio` también alcanzó el 100 % en ambas métricas. En conjunto, las estrategias presentan 24 ramas cubiertas y el servicio 8, para un total de 32 ramas cubiertas de 32 identificadas en la versión refactorizada.

Es importante señalar que las 38 ramas del método original y las 32 ramas del código refactorizado no representan necesariamente los mismos puntos de decisión, porque la aplicación de Strategy modificó la distribución de la lógica entre diferentes clases. Por esta razón, la comparación se realiza considerando tanto la cobertura inicial como la cobertura completa de las nuevas implementaciones, sin asumir una correspondencia numérica individual entre las ramas de ambas versiones.

Finalmente, el proyecto alcanzó un 97 % de cobertura de instrucciones y un 100 % de cobertura de ramas. El porcentaje global de instrucciones no llegó al 100 % debido a que todavía existen instrucciones sin ejecutar en el paquete principal de la aplicación, mientras que las clases de estrategias y servicio presentan cobertura completa. Esto demuestra que las nuevas pruebas no se limitaron a incrementar su cantidad, sino que permitieron comprobar los diferentes comportamientos de las estrategias y las decisiones del servicio que anteriormente no estaban suficientemente cubiertos.

### Decisión de diseño 6: selección y justificación de los umbrales de calidad

Para evaluar la calidad del código después de la refactorización, se establecieron dos umbrales principales: una complejidad ciclomática máxima de **10 por método (CC ≤ 10)** y una cobertura mínima de pruebas del **70 %**. Estos valores se seleccionaron teniendo en cuenta los criterios de la Tabla 3.2 y la Sección 2.3 de la guía de contenido, buscando un equilibrio entre exigencia técnica y viabilidad de mantenimiento.

En cuanto a la complejidad ciclomática, se eligió el valor 10 porque representa el límite superior del rango «Moderada» establecido en la guía y coincide con el valor predeterminado del perfil de calidad Sonar Way. Se consideró que un límite de 5 sería demasiado estricto para este proyecto, ya que podría provocar la división innecesaria de métodos cuya complejidad todavía resulta razonable de mantener. Por otro lado, un límite de 20 sería demasiado permisivo, porque permitiría conservar métodos con numerosos caminos de decisión, como el `calcularCosto()` original, que presentaba una complejidad ciclomática de 20. Por esta razón, CC = 10 se considera un punto de equilibrio que permite controlar la complejidad sin fomentar una refactorización excesiva.

Respecto a la cobertura, se estableció un mínimo del 70 % en lugar de exigir el 100 %. Aunque una cobertura completa puede parecer un objetivo ideal, no garantiza por sí misma que las pruebas comprueben correctamente el comportamiento del sistema. Exigir ese porcentaje en todos los casos puede generar incentivos perversos, como incorporar pruebas triviales que únicamente ejecuten instrucciones para aumentar el indicador, sin verificar resultados significativos. Tal como advierte la Sección 2.3 de la guía, las métricas deben orientar las decisiones de calidad y no convertirse en objetivos numéricos que se persiguen sin considerar su utilidad real.

El umbral del 70 % permite mantener una cobertura significativa sobre la lógica de negocio, dejando un margen razonable para componentes de infraestructura, configuración y arranque que no necesariamente requieren el mismo nivel de pruebas. Además, permite priorizar escenarios importantes y verificar comportamientos diferentes mediante aserciones, en lugar de agregar pruebas únicamente para mejorar el porcentaje.

En conclusión, los umbrales seleccionados buscan que el proyecto mantenga métodos con una complejidad razonable y una cobertura suficiente para detectar errores en sus comportamientos principales. La intención no es alcanzar indicadores perfectos de manera artificial, sino utilizar las métricas como criterios objetivos para orientar la refactorización y mejorar la mantenibilidad, la testabilidad y la calidad general del software.


## Comparación antes / después

| Métrica | Antes (Parte 1) | Después (Parte 2) |
|---------|------------------|--------------------|
| Complejidad ciclomática de `calcularCosto()` | 20 | 5 |
| Cobertura de líneas de `ServicioCalculoEnvio` | 24 % | 100 % |
| Cobertura de ramas de la lógica de envío | 18 % | 100 % |
| Code Smells | 0 observados | 0 observados |
| Quality Gate | — | Estandar Labooratorio |

## Interpretación de los resultados

La comparación entre el análisis inicial y el posterior a la refactorización evidencia una mejora importante en la estructura y la testabilidad del proyecto. Inicialmente, el método `ServicioCalculoEnvio.calcularCosto()` presentaba una complejidad ciclomática de 20, mientras que después de aplicar el patrón Strategy su complejidad se redujo a 5. Esto representa una disminución del 75 % en la complejidad del método original, al trasladar las diferentes fórmulas de cálculo a cuatro implementaciones independientes: `EnvioEstandarStrategy`, `EnvioExpressStrategy`, `EnvioInternacionalStrategy` y `EnvioOvernightStrategy`.

En cuanto a la cobertura, `ServicioCalculoEnvio` pasó de un 24 % de cobertura de líneas y un 18 % de cobertura de ramas a un 100 % en ambas métricas. El nuevo reporte de JaCoCo muestra que el servicio tiene 8 de 8 ramas cubiertas y las cuatro estrategias reúnen otras 24 ramas, también cubiertas en su totalidad. Por tanto, la lógica de envío presenta 32 de 32 ramas cubiertas, sin alternativas de ejecución pendientes de ejercitar en estas clases.

También se observa que cada estrategia tiene una complejidad ciclomática de 5, mientras que el servicio mantiene una complejidad total de 6 al considerar sus dos métodos, incluido el constructor. Esto permite distribuir la lógica en componentes más pequeños y especializados, facilitando su comprensión, mantenimiento y comprobación mediante pruebas unitarias.

Es importante señalar que la complejidad no desapareció del sistema, sino que se distribuyó entre las implementaciones de Strategy. Por esta razón, la mejora principal consiste en reducir la complejidad concentrada en el método original y separar cada algoritmo según su responsabilidad. Asimismo, el número de ramas pasó de 38 a 32 porque la estructura del código cambió con la refactorización, por lo que la comparación representa una mejora en las métricas y no una correspondencia individual entre las ramas antiguas y las nuevas.

Finalmente, el análisis global posterior de JaCoCo muestra un 97 % de cobertura de instrucciones y un 100 % de cobertura de ramas en el proyecto completo. La diferencia restante en instrucciones corresponde al paquete principal de la aplicación, mientras que los paquetes de estrategias y servicios presentan un 100 % de cobertura. Los resultados de Code Smells y Quality Gate deben complementarse con el dashboard de SonarQube, ya que no son métricas proporcionadas por JaCoCo.

En conclusión, la aplicación de Strategy permitió reducir la complejidad ciclomática del método principal, mejorar la separación de responsabilidades y alcanzar una cobertura completa de ramas en la lógica de envío. Estos resultados respaldan la decisión de refactorización y demuestran que las mejoras no se limitaron a reorganizar el código, sino que también facilitaron la comprobación de sus diferentes comportamientos.

Capturas



Análisis inicial — Parte 1 --->docs/parte1-antes-de-refatcorizar

Análisis final — Parte 2--->docs/refactor-strategy


##Herramientas utilizadas

Java 21.Apache Maven.
JUnit 5 para las pruebas unitarias.
SonarQube Community Edition para el análisis estático de calidad.
JaCoCo 0.8.16 para medir la cobertura de instrucciones, líneas y ramas.
Docker para ejecutar SonarQube localmente.
Git y GitHub para el control de versiones y almacenamiento del repositorio.

##Conclusiones

El desarrollo de este proyecto permitió comprender que las métricas de calidad son herramientas para identificar problemas de diseño y orientar decisiones de refactorización, no simplemente indicadores que se deben mejorar numéricamente. Mediante el análisis inicial se identificó que ServicioCalculoEnvio.calcularCosto() concentraba una complejidad ciclomática de 20 y una cobertura de ramas del 18 %, lo que justificó la aplicación del patrón Strategy para separar las diferentes fórmulas de cálculo. Después de la refactorización, la complejidad del método se redujo a 5 y se alcanzó un 100 % de cobertura de ramas en el servicio y las estrategias, demostrando una mejora en la organización del código y en su testabilidad. Finalmente, se comprendió la importancia de establecer umbrales realistas de complejidad y cobertura, priorizando pruebas que verifiquen comportamientos reales en lugar de perseguir porcentajes artificiales.