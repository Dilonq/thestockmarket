package djh.stockmarket.economics;


import java.util.ArrayList;
import java.util.Iterator;

import static java.util.Objects.deepEquals;
import static java.util.Objects.nonNull;

public class Market {
    public boolean debugMode = false;
    ArrayList<Account> accounts;
    ArrayList<Company> companies;
    ArrayList<Order> buyOrders;
    ArrayList<Order> sellOrders;
    ArrayList<Transaction> ledger;

    public Market(){
        accounts = new ArrayList<>();
        companies = new ArrayList<>();
        buyOrders = new ArrayList<>();
        sellOrders = new ArrayList<>();
        ledger = new ArrayList<>();
    }

    public Market(boolean debugMode){
        this();
        this.debugMode = debugMode;
    }

    public void tick(){

        Iterator<Order> buyOrdersIterator = buyOrders.iterator();
        while(buyOrdersIterator.hasNext()){

            Order buyOrder = buyOrdersIterator.next();
            Order matchedSellOrder = null;

            //match with eligible sell order
            Iterator<Order> sellOrdersIterator = sellOrders.iterator();
            while(sellOrdersIterator.hasNext()){
                Order sellOrder = sellOrdersIterator.next();

                if (buyOrder.securityCollection.company.equals(sellOrder.securityCollection.company)){
                    //same company

                    if (buyOrder.securityCollection.type.equals(sellOrder.securityCollection.type)){
                        //same security type

                        if (buyOrder.price >= sellOrder.price){
                            //prices meet! eligible orders

                            if (!nonNull(matchedSellOrder)){
                                matchedSellOrder = sellOrder;
                            }else{
                                if (matchedSellOrder.price > sellOrder.price){//match buy order with best price
                                    matchedSellOrder = sellOrder;
                                }else{
                                    if (matchedSellOrder.price == sellOrder.price){//and if 2 prices tie, the oldest sell order
                                        if (matchedSellOrder.date.after(sellOrder.date)){
                                            matchedSellOrder = sellOrder;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (nonNull(matchedSellOrder)){
                //transaction time!

                //find minimum transferable/agree-on-able quantity
                int minQuantity = Math.min(buyOrder.securityCollection.quantity,matchedSellOrder.securityCollection.quantity);
                int price = matchedSellOrder.price; //use seller price

                System.out.println("AA"+minQuantity+".."+price);

                //decrease quantity on both orders
                buyOrder.securityCollection.quantity-=minQuantity;
                matchedSellOrder.securityCollection.quantity-=minQuantity;

                //transfer securities
                buyOrder.account.assign(new SecurityCollection(minQuantity,buyOrder.securityCollection.type,buyOrder.securityCollection.company));
                matchedSellOrder.account.assign(new SecurityCollection(-minQuantity,buyOrder.securityCollection.type,buyOrder.securityCollection.company));

                //transfer money
                buyOrder.account.funds-=price*minQuantity;
                matchedSellOrder.account.funds+=price*minQuantity;

                //fill orders if they hit a quantity of 0
                if (buyOrder.securityCollection.quantity==0){
                    buyOrders.remove(buyOrder);
                }
                if (matchedSellOrder.securityCollection.quantity==0){
                    sellOrders.remove(matchedSellOrder);
                }

                //log transaction
                ledger.add(new Transaction(buyOrder.account,matchedSellOrder.account,minQuantity,price,buyOrder.securityCollection.company,buyOrder.securityCollection.type));
            }else{
                //welp, no matches! skip over this buy order, go to the next one
            }
        }
    }

    public void hardPlaceOrder(Account account, Company company, SecurityType securityType, int quantity, int price, OrderBUYSELL orderBUYSELL, OrderType orderType){
        Order order = new Order(account, new SecurityCollection(quantity, securityType, company), price, orderBUYSELL, orderType);

        if (orderBUYSELL.equals(OrderBUYSELL.BUY)) {
            buyOrders.add(order);
        }else {
            sellOrders.add(order);
        }
    }

    public Account registerAccount(String name, int funds) throws Exception {
        for (Account account : accounts){
            if (account.name.equals(name)){
                //non-unique name, throw
                softThrow(new Exception("Non-unique account name!"));
                return null;
            }
        }

        //unique name
        accounts.add(new Account(name,funds));
        return accounts.get(accounts.size()-1);//return pointer to the newly registered
    }

    public Company registerCompany(String ticker, Account account) throws Exception {
        for (Company company : companies){
            if (company.ticker.equals(ticker)){
                //non-unique ticker, throw
                softThrow(new Exception("Non-unique company ticker!"));
                return null;
            }
        }

        //unique ticker
        companies.add(new Company(ticker,account));
        return companies.get(companies.size()-1);//return pointer to the newly registered
    }

    public Company getCompany(String ticker){
        for (Company company : companies){
            if (company.ticker.equals(ticker)){
                return company;
            }
        }

        return null;
    }

    public Account getAccount(String name){
        for (Account account : accounts){
            if (account.name.equals(name)){
                return account;
            }
        }

        return null;
    }

    public ArrayList<SecurityCollection> findAllSecurityCollections(Company company) throws Exception {
        ArrayList<SecurityCollection> returnArrayList = new ArrayList<>();

        if (companies.contains(company)){
            for (Account account : accounts){
                for (SecurityCollection securityCollection : account.portfolio){
                    if (securityCollection.company.equals(company)){
                        returnArrayList.add(securityCollection);
                    }
                }
            }

            return returnArrayList;
        }else{
            //company doesnt exist, throw
            softThrow(new Exception("Company does not exist!"));
            return null;
        }
    }

    //debug methods
    public void softThrow(Exception e) throws Exception{
        if (debugMode){
            e.printStackTrace();
            throw e;
        }else{
            System.err.println("Warning!\n"+e.getStackTrace());
        }
    }

    public void debug(){
        System.out.println("--Accounts--");
        for (Account account : accounts){
            System.out.println("\t"+account.name+" - $"+account.funds);
            for (SecurityCollection securityCollection : account.portfolio){
                System.out.println("\t"+"\t"+securityCollection.quantity+" of "+securityCollection.company.ticker);
            }
        }
        System.out.println("--Companies--");
        for (Company company : companies){
            System.out.println("\t"+company.ticker+" owned by "+company.owner.name);
        }
        System.out.println("--Orders--");
        for (Order order : buyOrders){
            System.out.println("\t"+order.account.name+" "+order.orderBUYSELL.name()+" #"+order.securityCollection.quantity+" "+order.securityCollection.company.ticker+" @ $"+order.price);
        }
        for (Order order : sellOrders){
            System.out.println("\t"+order.account.name+" "+order.orderBUYSELL.name()+" #"+order.securityCollection.quantity+" "+order.securityCollection.company.ticker+" @ $"+order.price);
        }
        System.out.println("--Transaction Ledger--");
        for (Transaction transaction : ledger){
            System.out.println("\t"+transaction.toString());
        }
    }
}
