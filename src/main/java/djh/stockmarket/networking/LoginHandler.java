package djh.stockmarket.networking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import djh.stockmarket.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            // Serve the login form
            Server.serveHtmlFile(exchange, "login.html");

        } else if (method.equalsIgnoreCase("POST")) {
            // Handle login submission
            handleLoginSubmission(exchange);
        } else {
            // Unsupported HTTP method
            Server.sendResponse(exchange, "Method Not Allowed", 405);
        }
    }

    private void handleLoginSubmission(HttpExchange exchange) throws IOException {
        // Parse form data
        String formData = new String(exchange.getRequestBody().readAllBytes());
        String username = Server.getFieldValue(formData, "username");
        String password = Server.getFieldValue(formData, "password");

        // Verify credentials
        boolean success = Server.authenticateUser(username, password);

        // Respond to client
        if (success) {
            try (Connection con = DriverManager.getConnection(Database.url)){

                //set up token in database
                String token = Server.generateSessionToken();
                Database.createSession(con,Database.getAccountIDFromUsername(con,username),token);

                //set up token on client frontend
                Server.setSessionCookie(exchange, token);

                //redirect to welcome page
                Server.redirect(exchange,"/welcome");
                con.close();

            }catch(Exception e){
                e.printStackTrace();
            }
        } else {
            // Serve the login form, with error
            Server.sendResponse(exchange, Server.getFrontendFile("login.html").replace("{{invalid_credentials}}","<h>Invalid Credentials!</h>"), 200);
        }
    }
}
