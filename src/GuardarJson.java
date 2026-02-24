import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

public class GuardarJson {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void guardar(String origen, String destino,
                        double montoEntrada, double montoSalida,
                        double tasa, String fechaHora) throws IOException {

        String nombre = String.format("conversion_%s_%s.json", origen, destino);

        RegistroConversion registro = new RegistroConversion(
                origen, destino, montoEntrada, montoSalida, tasa, fechaHora
        );

        try (FileWriter writer = new FileWriter(nombre)) {
            writer.write(gson.toJson(registro));
        }
    }

    private record RegistroConversion(
            String origen,
            String destino,
            double montoEntrada,
            double montoSalida,
            double tasa,
            String fechaHora
    ) {}
}