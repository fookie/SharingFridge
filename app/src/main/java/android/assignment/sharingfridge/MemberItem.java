package android.assignment.sharingfridge;

/**
 * Created by Paulay on 2016/10/24.
 * The class that defines each member object stored in the lists, with name and avatarURL.
 */

public class MemberItem {
    private String name;
    private String act;
    private String avatarUrl;

    public MemberItem(String n, String a, String avaUrl) {
        name = n;
        act = a;
        avatarUrl = avaUrl;
    }

    // return the username of the member item
    public String getName() {
        return name;
    }

    // set the username to n
    public void setName(String n) {
        name = n;
    }

    // return the recent action of the member item
    public String getAct() {
        return act;
    }

    // return the avatar url of the member item
    public String getAvatarUrl() {
        return avatarUrl;
    }

}
