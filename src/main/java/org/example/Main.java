package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8181")
        );

        log("INFO", "Iniciando aplicação Floccus Java Test");
        log("INFO", "Porta configurada: " + port);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(port),
                0
        );

        server.createContext("/", Main::home);
        server.createContext("/env", Main::env);

        server.start();

        log("INFO", "Aplicação iniciada com sucesso");
        log("INFO", "Servidor HTTP escutando na porta " + port);

        Thread logThread = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(10000);

                    log(
                            "INFO",
                            "Aplicação executando normalmente - "
                                    + "Floccus Java Test"
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    log(
                            "ERROR",
                            "Thread de logs foi interrompida"
                    );

                    break;
                }
            }

        });

        logThread.setDaemon(true);
        logThread.start();
    }

    private static void home(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        log("INFO", "Endpoint / acessado");

        String response = """
                <h1>Floccus Java Test</h1>
                <p>Aplicação funcionando!</p>
                <p>Acesse <a href="/env">/env</a> para visualizar as variáveis.</p>
                """;

        send(exchange, response, "text/html");

        log("INFO", "Resposta enviada para /");
    }

    private static void env(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        log("INFO", "Endpoint /env acessado");
        log("INFO", "Lendo variáveis de ambiente");

        String appName = env("APP_NAME");
        String appEnv = env("APP_ENV");
        String databaseHost = env("DATABASE_HOST");
        String databasePort = env("DATABASE_PORT");
        String testVariable = env("TEST_VARIABLE");

        log("INFO", "APP_NAME = " + appName);
        log("INFO", "APP_ENV = " + appEnv);
        log("INFO", "DATABASE_HOST = " + databaseHost);
        log("INFO", "DATABASE_PORT = " + databasePort);
        log("INFO", "TEST_VARIABLE = " + testVariable);

        String response = """
                {
                  "APP_NAME": "%s",
                  "APP_ENV": "%s",
                  "DATABASE_HOST": "%s",
                  "DATABASE_PORT": "%s",
                  "TEST_VARIABLE": "%s"
                }
                """.formatted(
                appName,
                appEnv,
                databaseHost,
                databasePort,
                testVariable
        );

        send(exchange, response, "application/json");

        log("INFO", "Resposta enviada para /env");
    }

    private static String env(String name) {

        String value = System.getenv(name);

        if (value == null) {
            log(
                    "WARN",
                    "Variável de ambiente não definida: " + name
            );

            return "NÃO DEFINIDA";
        }

        return value;
    }

    private static void logRequest(HttpExchange exchange) {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String address = exchange.getRemoteAddress().toString();

        log(
                "INFO",
                "Requisição recebida: "
                        + method
                        + " "
                        + path
                        + " | Cliente: "
                        + address
        );
    }

    private static void send(
            HttpExchange exchange,
            String response,
            String contentType
    ) throws IOException {

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType + "; charset=UTF-8"
                );

        byte[] bytes = response.getBytes();

        exchange.sendResponseHeaders(
                200,
                bytes.length
        );

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void log(
            String level,
            String message
    ) {

        System.out.println(
                "["
                        + LocalDateTime.now()
                        + "] ["
                        + level
                        + "] "
                        + message
        );
    }
}
