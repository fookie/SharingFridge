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
    private boolean expanded = false; //button visibility
    private boolean reductionBox = false; //reduction amount visibility

    /**
     * @param n      Name
     * @param d      Expire date
     * @param imgURL URL of image
     * @param o      Owner (who adds it)
     * @param c      Category
     * @param a      Amount
     */
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

    public void setName(String n) {
        this.name = n;
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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Switch visibility of buttons
     *
     * @return Switched visibility
     */
    public boolean reverseExpanded() {
        return expanded = !expanded;
    }

    /**
     * Check the visibility of reduction box
     *
     * @return Visibility of reduction box
     */
    public boolean isReductionBox() {
        return reductionBox;
    }

    /**
     * Set the visibility of reduction box
     *
     * @param reductionBox box to be set
     */
    public void setReductionBox(boolean reductionBox) {
        this.reductionBox = reductionBox;
    }

    /**
     * Switch the visibility of reduction box
     *
     * @return Switched visibility
     */
    public boolean reverseReductionBox() {
        return reductionBox = !reductionBox;
    }

    /**
     * Check the visibility of buttons
     *
     * @return Visibility of buttons
     */
    public boolean isButtonsExpanded() {
        return expanded;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwner() {
        return owner;
    }

    /**
     * Reduce the amount
     *
     * @param sub amount to be reduced
     */
    public void minus(int sub) {
        amount -= sub;
    }

    /**
     * override compare to so that we cloud compare to two fridge item by expire date
     *
     * @param another fridge item
     * @return
     */
    @Override
    public int compareTo(FridgeItem another) {
        return this.date < another.date ? -1 : 1;
    }
}
