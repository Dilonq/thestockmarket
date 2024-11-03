package djh.stockmarket.economics;

import java.util.Date;

public class Transaction {
    Account buyer;
    Account seller;
    int quantity;
    int pricePerUnit;
    Company company;
    SecurityType type;
    Date date;

    public Transaction(Account buyer, Account seller, int quantity, int pricePerUnit, Company company, SecurityType type) {
        this.buyer = buyer;
        this.seller = seller;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.company = company;
        this.type = type;
        this.date = new Date();//log time transaction occurred
    }

    public String toString(){
        return seller.name + " sold " + buyer.name + " " + quantity + " " + type.toString() + " of " + company.ticker + " @ $" + pricePerUnit + "each totalling $" + pricePerUnit*quantity + " @ " + date.toString();
    }
}
