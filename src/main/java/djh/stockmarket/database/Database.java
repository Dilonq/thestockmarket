package djh.stockmarket.database;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.FileReader;

import djh.stockmarket.networking.Server;

import java.sql.*;

public class Database {
    public static final String url = Server.url;
    public static final int bcryptCost = 6;

    public static void main(String[] args) throws Exception{
        //try this if weird errors happen about preparing sql statements
//        Class.forName("org.h2.Driver");


        try (Connection con = DriverManager.getConnection(url)){

            //if database is new
            if (!tableExists(con,"ACCOUNTS")){
                System.out.println("setup");
                runSQLFile(con,"newDBSetup");
            }
            registerAdminIfNotAlreadyExists(con);


            con.close();

        }catch(Exception e){
          e.printStackTrace();
        }
    }

    public static int getAccountIDFromUsername(Connection con, String username){
        String sql = "SELECT ID FROM ACCOUNTS WHERE USERNAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                } else {
                    //return -1 if invalid session
                    return -1;
                }
            }
        } catch (SQLException e) {
            //return -1 if weird error
            e.printStackTrace();
            return -1;
        }
    }

    public static int getAccountIDFromToken(Connection con, String token){
        String sql = "SELECT ID FROM SESSIONS WHERE TOKEN = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                } else {
                    //return -1 if invalid session
                    return -1;
                }
            }
        } catch (SQLException e) {
            //return -1 if weird error
            return -1;
        }
    }

    public static void endSession(Connection con, int accountID) {
        String sql = "DELETE FROM SESSIONS WHERE ACCOUNT_ID = ?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, accountID);
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public static void endSession(Connection con, String token) {
        String sql = "DELETE FROM SESSIONS WHERE TOKEN = ?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public static boolean createSession(Connection con, int accountID, String token){
        //end session if one exists already
        endSession(con, accountID);

        String sql = "INSERT INTO SESSIONS (TOKEN, ACCOUNT_ID) VALUES (?, ?)";
        try{
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, token);
            stmt.setInt(2, accountID);
            stmt.executeUpdate();

            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean authenticateAccount(Connection con, String username, String password) {
        String sql = "SELECT HASHED_PASSWORD FROM ACCOUNTS WHERE USERNAME = ?";

        try{
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("HASHED_PASSWORD");
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash.toCharArray());
                return result.verified;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean registerAccount(Connection con, String username, String password) {
        String hashedPassword = new String(BCrypt.withDefaults().hashToChar(bcryptCost, password.toCharArray()));

        String sql = "INSERT INTO ACCOUNTS (USERNAME, HASHED_PASSWORD) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();

            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static void registerAdminIfNotAlreadyExists(Connection con){
        String hashedPassword = "$2a$06$BZsWJ8cXl/Rt2za5V2A68Of7e67/NsDLskzDFy/kSJcrEyC5MgoTS";

        String sql = "INSERT INTO ACCOUNTS (USERNAME, HASHED_PASSWORD) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "admin");
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        }catch(Exception e){
            //account already exists then
            //or maybe some other error but lets hope not!
        }
    }

    public static boolean tableExists(Connection con, String tableName) throws Exception {
        boolean tableExists = false;

        ResultSet rset = con.getMetaData().getTables(null, null, tableName, null);
        if (rset.next()) {
            tableExists = true;
        }

        return tableExists;
    }

    public static void runSQLFile(Connection con, String name){
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/djh/stockmarket/database/sql/"+name+".sql"))){
            Statement stmt = con.createStatement();
            String line;
            StringBuilder sql = new StringBuilder();

            // Read the SQL file line by line
            while ((line = reader.readLine()) != null) {
                sql.append(line);
                sql.append("\n");
            }

            // Execute the SQL commands
            stmt.execute(sql.toString());

            //close out shtuff
            reader.close();
            stmt.close();
            //DONT CLOSE CONNECTION
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
