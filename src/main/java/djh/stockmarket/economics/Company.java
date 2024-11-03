package djh.stockmarket.economics;

import java.util.ArrayList;

public class Company {
    String ticker;
    Account owner;
    int outstandingShares;

    public Company(String ticker, Account owner){
        this.ticker = ticker;
        this.owner = owner;
    }

    public void issueShares(int count){
        owner.assign(new SecurityCollection(count,SecurityType.SHARE,this));
        outstandingShares+=count;
    }
}
