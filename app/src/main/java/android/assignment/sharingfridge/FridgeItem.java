package android.assignment.sharingfridge;

/**
 * Created by EveLIn3 on 2016/10/16.
 * The class that defines the Item object in our application, with properties like name, expiredate, etc.
 */

public class FridgeItem implements Comparable<FridgeItem> {
    private String name;
    private long date;
    private String photoURL;
    private String owner;
    private String category;
    private int amount;
    private boolean expanded = false;
    private boolean reductionBox = false;

    public FridgeItem(String n, long d, String imgURL, String o, String c, int a) {
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

    public int getDate() {
        return (int) date;
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

    public void setName(String n) {
        this.name = n;
    }

    public void minus(int sub) {
        amount -= sub;
    }

    @Override
    public int compareTo(FridgeItem another) {
        return this.date < another.date ? -1 : 1;
    }
}
