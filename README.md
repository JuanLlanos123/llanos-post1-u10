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


Estado inicial del análisis

Métrica

Valor real observado

Complejidad ciclomática de calcularCosto()

20

Cobertura de líneas de ServicioCalculoEnvio

24 %

Cobertura de ramas de ServicioCalculoEnvio

18 %

Code Smells reportados

No aparecen issue

Detalle de JaCoCo: la clase presenta 10 de 42 líneas cubiertas y 7 de 38 ramas cubiertas. El método calcularCosto(String, double, double, String, boolean) concentra una complejidad ciclomática de 20, mientras que el constructor presenta una complejidad de 1.

Interpretación

El análisis inicial permitió identificar que el método ServicioCalculoEnvio.calcularCosto() concentra el principal problema de complejidad del proyecto. Su complejidad ciclomática es de 20, lo que evidencia una cantidad considerable de caminos independientes de ejecución y decisiones condicionales. Esto puede dificultar la comprensión, el mantenimiento y la construcción de pruebas que contemplen todos sus comportamientos.

En cuanto a la cobertura, el reporte de JaCoCo muestra que ServicioCalculoEnvio tiene una cobertura de líneas del 24 % y una cobertura de ramas del 18 %. De las 38 ramas identificadas, solamente 7 están cubiertas y 31 permanecen sin ejercitar. Por tanto, las pruebas existentes no recorren adecuadamente las diferentes alternativas de ejecución del método. En el detalle del reporte HTML se deben identificar las condiciones específicas que aparecen sin cubrir, para orientar la creación de pruebas adicionales.

Decisión de diseño 3: selección de la métrica principal. Se decidió utilizar la complejidad ciclomática como señal principal para seleccionar el método que se intervendrá en la Parte 2, debido a que permite identificar la concentración de decisiones y caminos independientes de ejecución. La cobertura de ramas se utilizó como indicador complementario, porque permite comprobar qué proporción de esas alternativas está siendo recorrida por las pruebas.

En este proyecto ambas señales apuntan a ServicioCalculoEnvio.calcularCosto(), que presenta una complejidad ciclomática de 20 y una cobertura de ramas del 18 %. Esta coincidencia justifica su selección como candidato para la refactorización, ya que combina una lógica con numerosos caminos de decisión y una cobertura insuficiente. Aunque la complejidad cognitiva también es útil para evaluar la facilidad de comprensión del código, se priorizó la complejidad ciclomática porque el objetivo de la intervención es abordar la cantidad de caminos de ejecución y mejorar las condiciones para probarlos.

Capturas del análisis inicial ---> docs/parte1/
