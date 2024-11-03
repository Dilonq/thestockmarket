package djh.stockmarket.economics;

import java.util.Date;

public class Order {
    static int lastid=-1;
    int id;
    Account account;
    SecurityCollection securityCollection;
    int price;
    OrderBUYSELL orderBUYSELL;
    OrderType orderType;
    Date date;

    public Order(Account account, SecurityCollection securityCollection, int price, OrderBUYSELL orderBUYSELL, OrderType orderType ){
        lastid++;
        this.id = lastid;

        this.account = account;
        this.securityCollection = securityCollection;
        this.price = price;
        this.orderBUYSELL = orderBUYSELL;
        this.orderType = orderType;

        this.date = new Date();//store time that order is created
    }
}
