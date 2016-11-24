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
    private boolean expanded = false;
    private boolean reductionBox = false;

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

    public boolean reverseExpanded() {
        return expanded = !expanded;
    }

    public void setButtonsExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isReductionBox() {
        return reductionBox;
    }

    public void setReductionBox(boolean reductionBox) {
        this.reductionBox = reductionBox;
    }

    public boolean reverseReductionBox() {
        return reductionBox = !reductionBox;
    }

    public boolean isButtonsExpanded() {
        return expanded;
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

    public void minus(int sub) {
        amount -= sub;
    }

    public void setDate(String d) {
        this.date = d;
    }

    public void setPhotoURL(String p) {
        this.photoURL = p;
    }

}
