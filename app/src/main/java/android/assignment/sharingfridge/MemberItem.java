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

    public String getName() {
        return name;
    }

    public String getAct() {
        return act;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setName(String n) {
        name = n;
    }

}
