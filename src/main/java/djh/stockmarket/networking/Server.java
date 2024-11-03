package djh.stockmarket.networking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import djh.stockmarket.database.Database;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    public static final String url = "jdbc:h2:./db/thestockmarket";
    public static final String frontendPath = "src/main/java/djh/stockmarket/frontend/";
    public static final int tokenLength = 6;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
//        server.createContext("/signup", new SignupHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/welcome", new WelcomeHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    //file manager methods
    public static String getFrontendFile(String filename) throws IOException {
        return Files.readString(Path.of(frontendPath+filename), StandardCharsets.UTF_8);
    }
    public static boolean FrontendFileExists(String filename) {
        File f = new File(frontendPath+filename);
        return f.exists();
    }

    // Helper methods
    public static String generateSessionToken() {
        int lower = (int) Math.pow(10,tokenLength-1);
        int upper = (int) Math.pow(10,tokenLength) -1;
        System.out.println(lower+" "+upper);
        return new String(String.valueOf(ThreadLocalRandom.current().nextInt(lower, upper)));
    }

    public static void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Set-Cookie", "session_token=; Path=/; Max-Age=0; HttpOnly; Secure");
    }

    public static String getSessionTokenFromCookies(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String cookie : cookies.split(";")) {
                String[] parts = cookie.split("=");
                if (parts.length == 2 && parts[0].trim().equals("session_token")) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }

    public static void setSessionCookie(HttpExchange exchange, String sessionToken) {
        exchange.getResponseHeaders().add("Set-Cookie", "session_token=" + sessionToken + "; Path=/; HttpOnly; Secure");
    }

    public static String sanitizeHTML(String html) {
        return html.replaceAll("\\{\\{.*?}}", "");
    }

    public static void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", path);
        exchange.sendResponseHeaders(301, -1);
        exchange.close();
    }

    public static void serveHtmlFile(HttpExchange exchange, String fileName) throws IOException {
        if (Server.FrontendFileExists(fileName)) {
            Server.sendResponse(exchange, sanitizeHTML(getFrontendFile(fileName)));
        } else {
            Server.sendResponse(exchange, sanitizeHTML(getFrontendFile("404.html")),404);
        }
    }

    public static void sendResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response,200);
    }

    public static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        String css = "<style>"+getFrontendFile("global_style.css")+"</style>";
        exchange.sendResponseHeaders(statusCode, response.length()+css.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
            os.write(css.getBytes());
        }

//        exchange.sendResponseHeaders(200, response.length());
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes());
//        }
    }

    public static String getFieldValue(String formData, String fieldName) {
        for (String param : formData.split("&")) {
            String[] pair = param.split("=");
            if (pair[0].equals(fieldName)) {
                return pair.length > 1 ? pair[1] : "";
            }
        }
        return "";
    }

    //database interactions
    public static boolean registerUser(String username, String password) {
        try {
            Connection con = DriverManager.getConnection(Server.url);

            if (Database.registerAccount(con,username, password)) {
                return true;
            } else {
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean authenticateUser(String username, String password) {
        try {
            Connection con = DriverManager.getConnection(url);

            if (Database.authenticateAccount(con,username,password)) {
                return true;
            } else {
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}