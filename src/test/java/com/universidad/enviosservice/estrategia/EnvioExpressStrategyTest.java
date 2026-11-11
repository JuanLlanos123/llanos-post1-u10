package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioExpressStrategyTest {

    private final EnvioExpressStrategy estrategia = new EnvioExpressStrategy();

    @ParameterizedTest
    @CsvSource({
        "3.0, 100.0, 156.0",
        "10.0, 100.0, 165.75",
        "25.0, 100.0, 198.25",
        "3.0, 501.0, 814.06"
    })
    void calculaCostoPorTramoDePesoYRecargoDeDistancia(
            double pesoKg, double distanciaKm, double esperado) {
        assertEquals(esperado, estrategia.calcular(pesoKg, distanciaKm), 0.01);
    }
}
