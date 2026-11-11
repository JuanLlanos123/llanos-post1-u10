package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioInternacionalStrategyTest {

    private final EnvioInternacionalStrategy estrategia = new EnvioInternacionalStrategy();

    @ParameterizedTest
    @CsvSource({
        "3.0, 100.0, 230.0",
        "10.0, 100.0, 245.0",
        "25.0, 100.0, 297.5",
        "3.0, 5001.0, 9151.8"
    })
    void calculaCostoPorTramoDePesoYRecargoDeDistancia(
            double pesoKg, double distanciaKm, double esperado) {
        assertEquals(esperado, estrategia.calcular(pesoKg, distanciaKm), 0.01);
    }
}
