package android.assignment.sharingfridge;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

public class FridgeItem {
    private String name;
    private String date;
    private String photoURL;
    private String owner;
    private String category;
    private int amount;

    public FridgeItem(String n, String d, String imgURL, String o, String c, int a) {
        this.name = n;
        this.date = d;
        this.photoURL = imgURL;
        this.owner = o;
        this.category = c;
        this.amount = a;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public int getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getOwner() {
        return owner;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setName(String n) {
        this.name = n;
    }


    public void setDate(String d) {
        this.date = d;
    }

    public void setPhotoURL(String p) {
        this.photoURL = p;
    }

}
