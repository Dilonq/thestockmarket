package djh.stockmarket;


import djh.stockmarket.database.Database;
import djh.stockmarket.economics.Market;
import djh.stockmarket.economics.OrderBUYSELL;
import djh.stockmarket.economics.OrderType;
import djh.stockmarket.economics.SecurityType;
import djh.stockmarket.networking.Server;

import java.util.Date;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("start");
        Database.main(args);
        Server.main(args);
    }
}