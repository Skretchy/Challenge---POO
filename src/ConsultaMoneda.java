import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

    public class ConsultaMoneda {

        private final String apiKey;
        private final HttpClient cliente = HttpClient.newHttpClient();
        private final Gson gson = new Gson();

        public ConsultaMoneda(String apiKey) {
            this.apiKey = apiKey;
        }

        public ResultadoConversion convertir(String monedaOrigen, String monedaDestino, double monto)
                throws IOException, InterruptedException {

            validarCodigo(monedaOrigen);
            validarCodigo(monedaDestino);

            String url = String.format(
                    "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%.8f",
                    apiKey, monedaOrigen, monedaDestino, monto
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " al consultar la API.");
            }

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);

            if (!root.has("result") || !root.get("result").getAsString().equalsIgnoreCase("success")) {
                String error = root.has("error-type") ? root.get("error-type").getAsString() : "unknown_error";
                throw new IllegalArgumentException("API error: " + error);
            }

            double tasa = root.get("conversion_rate").getAsDouble();
            double convertido = root.get("conversion_result").getAsDouble();

            return new ResultadoConversion(monedaOrigen, monedaDestino, monto, convertido, tasa);
        }

        private void validarCodigo(String code) {
            if (code == null || code.isBlank() || code.length() != 3) {
                throw new IllegalArgumentException("Código de moneda inválido: " + code);
            }
            for (char c : code.toCharArray()) {
                if (!Character.isLetter(c)) {
                    throw new IllegalArgumentException("Código de moneda inválido: " + code);
                }
            }
        }
    }
