package com.ecovolt.demo.components;

import java.util.Map;
import java.util.Random;

public class SimuladorEnergiaUtils {

    private static final Map<String, Double> CONSUMOS_BASE = Map.ofEntries(
            Map.entry("tv", 120.0),
            Map.entry("televisor", 120.0),
            Map.entry("refrigerador", 250.0),
            Map.entry("nevera", 250.0),
            Map.entry("frigorifico", 250.0),
            Map.entry("luz", 20.0),
            Map.entry("foco", 20.0),
            Map.entry("lampara", 20.0),
            Map.entry("laptop", 45.0),
            Map.entry("computadora", 180.0),
            Map.entry("pc", 180.0),
            Map.entry("aire acondicionado", 1200.0),
            Map.entry("ac", 1200.0),
            Map.entry("climatizador", 1200.0)
    );

    private static final double CONSUMO_DEFAULT_WATTS = 60.0;
    private static final double WATTS_POR_KILOWATT = 1000.0;
    private static final double SEGUNDOS_POR_HORA = 3600.0;
    private static final Random random = new Random();

    private SimuladorEnergiaUtils() {
    }

    public static double obtenerPotenciaBaseWatts(String tipo) {
        return CONSUMOS_BASE.getOrDefault(normalizarTipo(tipo), CONSUMO_DEFAULT_WATTS);
    }

    public static double calcularConsumoIntervalo(String tipo, int segundos) {
        return calcularConsumoIntervalo(obtenerPotenciaBaseWatts(tipo), segundos);
    }

    public static double calcularConsumoIntervalo(Double watts, int segundos) {
        if (segundos <= 0) {
            return 0;
        }

        double potenciaWatts = watts == null || watts <= 0 ? CONSUMO_DEFAULT_WATTS : watts;
        double variacion = 1 + ((random.nextDouble() * 0.1) - 0.05);
        double horas = segundos / SEGUNDOS_POR_HORA;
        return (potenciaWatts * variacion * horas) / WATTS_POR_KILOWATT;
    }

    private static String normalizarTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toLowerCase();
    }
}
