
package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
        server.createContext("/logo.png", Main::logo);

        server.start();

        log("INFO", "Aplicação iniciada com sucesso");
        log("INFO", "Servidor HTTP escutando na porta " + port);

        Thread logThread = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(10000);

                    log(
                            "INFO",
                            "Aplicação executando normalmente - Floccus Java Test"
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("ERROR", "Thread de logs foi interrompida");
                    break;
                }
            }

        });

        logThread.setDaemon(true);
        logThread.start();
    }

    // Página inicial
    private static void home(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestURI().getPath().equals("/")) {
            send(exchange, "Página não encontrada", "text/plain", 404);
            return;
        }

        logRequest(exchange);
        log("INFO", "Endpoint / acessado");

        String meuNome = env("MEU_NOME");
        String minhaIdade = env("MINHA_IDADE");

        String response = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">

                    <title>Floccus Java Test</title>

                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                            background: #f8fafc;
                            font-family: Arial, sans-serif;
                            color: #18181b;
                        }

                        .container {
                            width: 100%;
                            max-width: 480px;
                            padding: 40px;
                            background: white;
                            border: 1px solid #e4e4e7;
                            border-radius: 16px;
                            text-align: center;
                            box-shadow: 0 8px 30px rgba(0,0,0,0.04);
                        }

                        .logo {
                            width: 170px;
                            max-height: 90px;
                            object-fit: contain;
                            margin-bottom: 20px;
                        }

                        h1 {
                            font-size: 25px;
                            margin-bottom: 8px;
                        }

                        .description {
                            color: #71717a;
                            margin-bottom: 30px;
                        }

                        .info {
                            padding: 16px;
                            background: #f4f4f5;
                            border-radius: 10px;
                            margin-bottom: 12px;
                            text-align: left;
                            overflow-wrap: anywhere;
                        }

                        .label {
                            display: block;
                            font-size: 12px;
                            color: #71717a;
                            margin-bottom: 6px;
                        }

                        .value {
                            font-size: 16px;
                            font-weight: bold;
                        }

                        a {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 12px 20px;
                            background: #18181b;
                            color: white;
                            text-decoration: none;
                            border-radius: 8px;
                            transition: background 0.2s;
                        }

                        a:hover {
                            background: #3f3f46;
                        }

                        .status {
                            color: #059669;
                            font-size: 13px;
                            margin-top: 25px;
                        }

                        .footer {
                            color: #a1a1aa;
                            font-size: 11px;
                            margin-top: 25px;
                        }
                    </style>
                </head>

                <body>

                    <main class="container">

                        <img
                            class="logo"
                            src="/logo.png"
                            alt="Floccus"
                        >

                        <h1>Floccus Java Test</h1>

                        <p class="description">
                            Aplicação implantada com sucesso!
                        </p>

                        <div class="info">
                            <span class="label">MEU_NOME</span>
                            <span class="value">%s</span>
                        </div>

                        <div class="info">
                            <span class="label">MINHA_IDADE</span>
                            <span class="value">%s</span>
                        </div>

                        <a href="/env">
                            Visualizar variáveis JSON
                        </a>

                        <p class="status">
                            ● Aplicação funcionando normalmente
                        </p>

                        <p class="footer">
                            Gerenciado pela plataforma Floccus
                        </p>

                    </main>

                </body>
                </html>
                """.formatted(
                escapeHtml(meuNome),
                escapeHtml(minhaIdade)
        );

        send(exchange, response, "text/html", 200);

        log("INFO", "Resposta enviada para /");
    }

    // Endpoint para disponibilizar a logo
    private static void logo(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        try (var input = Main.class.getResourceAsStream("/logo.png")) {

            if (input == null) {
                log("ERROR", "Arquivo logo.png não encontrado");
                send(exchange, "Logo não encontrada", "text/plain", 404);
                return;
            }

            byte[] image = input.readAllBytes();

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "image/png"
            );

            exchange.getResponseHeaders().set(
                    "Cache-Control",
                    "public, max-age=86400"
            );

            exchange.sendResponseHeaders(200, image.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(image);
            }

            log("INFO", "Logo enviada com sucesso");

        }
    }

    // Endpoint para visualizar as variáveis
    private static void env(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        log("INFO", "Endpoint /env acessado");
        log("INFO", "Lendo variáveis de ambiente");

        String meuNome = env("MEU_NOME");
        String minhaIdade = env("MINHA_IDADE");

        log("INFO", "MEU_NOME = " + meuNome);
        log("INFO", "MINHA_IDADE = " + minhaIdade);

        String response = """
                {
                  "MEU_NOME": "%s",
                  "MINHA_IDADE": "%s"
                }
                """.formatted(
                escapeJson(meuNome),
                escapeJson(minhaIdade)
        );

        send(exchange, response, "application/json", 200);

        log("INFO", "Resposta enviada para /env");
    }

    // Leitura das variáveis de ambiente
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

    // Proteção para exibição HTML
    private static String escapeHtml(String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Proteção para retorno JSON
    private static String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Registro das requisições
    private static void logRequest(HttpExchange exchange) {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String address = exchange.getRemoteAddress().toString();

        log(
                "INFO",
                "Requisição recebida: " +
                method + " " + path +
                " | Cliente: " + address
        );
    }

    // Envio das respostas HTTP
    private static void send(
            HttpExchange exchange,
            String response,
            String contentType,
            int status
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType + "; charset=UTF-8"
        );

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    // Sistema de logs
    private static void log(String level, String message) {

        System.out.println(
                "[" +
                LocalDateTime.now() +
                "] [" +
                level +
                "] " +
                message
        );
    }
}

package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
        server.createContext("/logo.png", Main::logo);

        server.start();

        log("INFO", "Aplicação iniciada com sucesso");
        log("INFO", "Servidor HTTP escutando na porta " + port);

        Thread logThread = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(10000);

                    log(
                            "INFO",
                            "Aplicação executando normalmente - Floccus Java Test"
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("ERROR", "Thread de logs foi interrompida");
                    break;
                }
            }

        });

        logThread.setDaemon(true);
        logThread.start();
    }

    // Página inicial
    private static void home(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestURI().getPath().equals("/")) {
            send(exchange, "Página não encontrada", "text/plain", 404);
            return;
        }

        logRequest(exchange);
        log("INFO", "Endpoint / acessado");

        String meuNome = env("MEU_NOME");
        String minhaIdade = env("MINHA_IDADE");

        String response = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">

                    <title>Floccus Java Test</title>

                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                            background: #f8fafc;
                            font-family: Arial, sans-serif;
                            color: #18181b;
                        }

                        .container {
                            width: 100%;
                            max-width: 480px;
                            padding: 40px;
                            background: white;
                            border: 1px solid #e4e4e7;
                            border-radius: 16px;
                            text-align: center;
                            box-shadow: 0 8px 30px rgba(0,0,0,0.04);
                        }

                        .logo {
                            width: 170px;
                            max-height: 90px;
                            object-fit: contain;
                            margin-bottom: 20px;
                        }

                        h1 {
                            font-size: 25px;
                            margin-bottom: 8px;
                        }

                        .description {
                            color: #71717a;
                            margin-bottom: 30px;
                        }

                        .info {
                            padding: 16px;
                            background: #f4f4f5;
                            border-radius: 10px;
                            margin-bottom: 12px;
                            text-align: left;
                            overflow-wrap: anywhere;
                        }

                        .label {
                            display: block;
                            font-size: 12px;
                            color: #71717a;
                            margin-bottom: 6px;
                        }

                        .value {
                            font-size: 16px;
                            font-weight: bold;
                        }

                        a {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 12px 20px;
                            background: #18181b;
                            color: white;
                            text-decoration: none;
                            border-radius: 8px;
                            transition: background 0.2s;
                        }

                        a:hover {
                            background: #3f3f46;
                        }

                        .status {
                            color: #059669;
                            font-size: 13px;
                            margin-top: 25px;
                        }

                        .footer {
                            color: #a1a1aa;
                            font-size: 11px;
                            margin-top: 25px;
                        }
                    </style>
                </head>

                <body>

                    <main class="container">

                        <img
                            class="logo"
                            src="/logo.png"
                            alt="Floccus"
                        >

                        <h1>Floccus Java Test</h1>

                        <p class="description">
                            Aplicação implantada com sucesso!
                        </p>

                        <div class="info">
                            <span class="label">MEU_NOME</span>
                            <span class="value">%s</span>
                        </div>

                        <div class="info">
                            <span class="label">MINHA_IDADE</span>
                            <span class="value">%s</span>
                        </div>

                        <a href="/env">
                            Visualizar variáveis JSON
                        </a>

                        <p class="status">
                            ● Aplicação funcionando normalmente
                        </p>

                        <p class="footer">
                            Gerenciado pela plataforma Floccus
                        </p>

                    </main>

                </body>
                </html>
                """.formatted(
                escapeHtml(meuNome),
                escapeHtml(minhaIdade)
        );

        send(exchange, response, "text/html", 200);

        log("INFO", "Resposta enviada para /");
    }

    // Endpoint para disponibilizar a logo
    private static void logo(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        try (var input = Main.class.getResourceAsStream("/logo-color.png")) {

            if (input == null) {
                log("ERROR", "Arquivo logo.png não encontrado");
                send(exchange, "Logo não encontrada", "text/plain", 404);
                return;
            }

            byte[] image = input.readAllBytes();

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "image/png"
            );

            exchange.getResponseHeaders().set(
                    "Cache-Control",
                    "public, max-age=86400"
            );

            exchange.sendResponseHeaders(200, image.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(image);
            }

            log("INFO", "Logo enviada com sucesso");

        }
    }

    // Endpoint para visualizar as variáveis
    private static void env(HttpExchange exchange) throws IOException {

        logRequest(exchange);

        log("INFO", "Endpoint /env acessado");
        log("INFO", "Lendo variáveis de ambiente");

        String meuNome = env("MEU_NOME");
        String minhaIdade = env("MINHA_IDADE");

        log("INFO", "MEU_NOME = " + meuNome);
        log("INFO", "MINHA_IDADE = " + minhaIdade);

        String response = """
                {
                  "MEU_NOME": "%s",
                  "MINHA_IDADE": "%s"
                }
                """.formatted(
                escapeJson(meuNome),
                escapeJson(minhaIdade)
        );

        send(exchange, response, "application/json", 200);

        log("INFO", "Resposta enviada para /env");
    }

    // Leitura das variáveis de ambiente
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

    // Proteção para exibição HTML
    private static String escapeHtml(String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Proteção para retorno JSON
    private static String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Registro das requisições
    private static void logRequest(HttpExchange exchange) {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String address = exchange.getRemoteAddress().toString();

        log(
                "INFO",
                "Requisição recebida: " +
                method + " " + path +
                " | Cliente: " + address
        );
    }

    // Envio das respostas HTTP
    private static void send(
            HttpExchange exchange,
            String response,
            String contentType,
            int status
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType + "; charset=UTF-8"
        );

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    // Sistema de logs
    private static void log(String level, String message) {

        System.out.println(
                "[" +
                LocalDateTime.now() +
                "] [" +
                level +
                "] " +
                message
        );
    }
}
