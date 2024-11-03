package djh.stockmarket.networking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class WelcomeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Server.serveHtmlFile(exchange,"welcome.html");
    }
}