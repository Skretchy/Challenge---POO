import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
//codigo hecho por Emilio Zúñiga Arreguín
public class Principal {

    private static final String API_KEY = " f6dbd5509a66e6101d8b047a";

    private static final Map<Integer, Par> opciones = new LinkedHashMap<>();
    static {
        opciones.put(1, new Par("USD", "ARS"));
        opciones.put(2, new Par("ARS", "USD"));
        opciones.put(3, new Par("USD", "BRL"));
        opciones.put(4, new Par("BRL", "USD"));
        opciones.put(5, new Par("USD", "MXN"));
        opciones.put(6, new Par("MXN", "USD"));
    }

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        String apiKeyFinal = obtenerApiKey();
        if (apiKeyFinal.isBlank()) {
            System.out.println("ERROR: Falta API Key. Pégala en API_KEY o crea una.");
            return;
        }

        ConsultaMoneda api = new ConsultaMoneda(apiKeyFinal);
        GuardarJson json = new GuardarJson();

        while (true) {
            mostrarMenu();
            int opcion = leerEntero(teclado, "Elige una opción: ");

            if (opcion == 0) {
                System.out.println("Saliendo...");
                break;
            }

            Par par = opciones.get(opcion);
            if (par == null) {
                System.out.println("Opción inválida.");
                continue;
            }
//se puede ocupar comas para separar
            double monto = leerDoublePositivo(teclado, "Ingresa el monto a convertir: ");

            try {
                ResultadoConversion resultado = api.convertir(par.origen(), par.destino(), monto);

                System.out.printf(
                        "Resultado: %.4f %s = %.4f %s | tasa=%.6f%n",
                        resultado.montoEntrada(), resultado.monedaOrigen(),
                        resultado.montoSalida(), resultado.monedaDestino(),
                        resultado.tasa()
                );

                System.out.print("¿Guardar en JSON? (si, s/no, no): ");
                String respuesta = teclado.nextLine().trim().toLowerCase();

                if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                    json.guardar(
                            resultado.monedaOrigen(),
                            resultado.monedaDestino(),
                            resultado.montoEntrada(),
                            resultado.montoSalida(),
                            resultado.tasa(),
                            LocalDateTime.now().toString()
                    );
                    System.out.println("OK.");
                }

            } catch (IOException e) {
                System.out.println("ERROR de red 404 /IO: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("ERROR: operación interrumpida.");
                Thread.currentThread().interrupt();
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR de datos: " + e.getMessage());
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n=== Conversor de Monedas ===");
        for (Map.Entry<Integer, Par> item : opciones.entrySet()) {
            System.out.printf("%d) %s -> %s%n", item.getKey(), item.getValue().origen(), item.getValue().destino());
        }
        System.out.println("0) Salir");
    }

    private static int leerEntero(Scanner teclado, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = teclado.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("ERROR Escribe un número entero.");
            }
        }
    }

    private static double leerDoublePositivo(Scanner teclado, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = teclado.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(texto);
                if (valor <= 0) {
                    System.out.println("El monto debe ser > 0.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR Escribe un número (ej. 25 o 2,500 25.5).");
            }
        }
    }
//aquí pon tu API si marca error
    private static String obtenerApiKey() {
        String env = System.getenv("EXCHANGE_RATE_API_KEY");
        if (env != null && !env.isBlank()) return env.trim();
        return API_KEY.trim();
    }

    private record Par(String origen, String destino) {}
}