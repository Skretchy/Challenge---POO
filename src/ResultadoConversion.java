public record ResultadoConversion(
        String monedaOrigen,
        String monedaDestino,
        double montoEntrada,
        double montoSalida,
        double tasa
) {}