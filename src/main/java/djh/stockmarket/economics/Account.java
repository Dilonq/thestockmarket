package djh.stockmarket.economics;

import java.util.ArrayList;

public class Account {
    String name;
    int funds;
    ArrayList<SecurityCollection> portfolio;
    public Account(String name, int funds){
        this.name = name;
        this.funds = funds;
        portfolio = new ArrayList<>();
    }

    public void assign(SecurityCollection securityCollection){
        for (SecurityCollection col : portfolio){
            if (col.company.equals(securityCollection.company) && col.type.equals(securityCollection.type)){
                //if shares are already owned, just update the quantity
                col.quantity+= securityCollection.quantity;

                //TODO delete collection if quantity is 0
                return;
            }
        }

        //if shares are not already owned, assign the new collection
        portfolio.add(securityCollection);
    }

}
