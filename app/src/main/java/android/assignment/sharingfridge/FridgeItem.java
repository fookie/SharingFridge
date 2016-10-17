package android.assignment.sharingfridge;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

public class FridgeItem {
    private String name;
    private String date;
    private String photoURL;

    public FridgeItem(String n, String d, String imgURL){
        this.name = n;
        this.date = d;
        this.photoURL = imgURL;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getPhotoURL(){
        return photoURL;
    }

    public void setName(String n){
        this.name = n;
    }

    public void setDate(String d){
        this.date = d;
    }

    public void setPhotoURL(String p){
        this.photoURL = p;
    }

}
