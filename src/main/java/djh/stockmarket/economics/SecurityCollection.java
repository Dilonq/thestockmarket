package djh.stockmarket.economics;

public class SecurityCollection {
    static int lastid=-1;
    int id;
    int quantity;
    SecurityType type;

    //connections
    Company company;

    public SecurityCollection(int quantity, SecurityType type, Company company) {
        lastid++;
        this.id = lastid;
        this.quantity = quantity;
        this.type = type;
        this.company = company;
    }
}
