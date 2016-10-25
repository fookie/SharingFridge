package android.assignment.sharingfridge;

/**
 * Created by Paulay on 2016/10/24.
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

    public void setAct(String a) {
        act = a;
    }

    public void setAvatarUrl(String a) {
        avatarUrl = a;
    }
}
