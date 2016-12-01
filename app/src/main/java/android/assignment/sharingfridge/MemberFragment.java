package android.assignment.sharingfridge;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * The Fragment class for displaying a list of the group members
 */
public class MemberFragment extends Fragment {

    private List<MemberItem> memberItemList;
    private MemberViewAdapter memberViewAdapter;
    RecyclerView memberView;

    private SQLiteDatabase mainDB;

    private OnLoginStatusListener loginRefreshListener;

    public MemberFragment() {
        // Required empty public constructor
    }


    public static MemberFragment newInstance() {
        MemberFragment fragment = new MemberFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS dummy(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        memberItemList = initMemberList();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, "http://178.62.93.103/SharingFridge/");
        // Inflate the layout for this fragment

        View view = null;
        view = inflater.inflate(R.layout.fragment_member, container, false);
        memberView = (RecyclerView) view.findViewById(R.id.memberView);
        memberView.setHasFixedSize(true);
        memberView.setLayoutManager(gridLayoutManager);
        memberView.setAdapter(memberViewAdapter);

        return view;
    }

    // refresh the display by using a new adapter
    public void updateUI(){
        if(isAdded()){  // isAdded is a android built-in function to prevent null invocation from fragment when its context is not loaded yet
            memberItemList = initMemberList();
            memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, "http://178.62.93.103/SharingFridge/");
            memberView.setAdapter(memberViewAdapter);
            memberViewAdapter.notifyDataSetChanged();
        } else {
            Log.d("updateUI", "not added, failed.");
        }
    }

    public void onResume(){
        super.onResume();
        memberItemList = initMemberList();
        memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, "http://178.62.93.103/SharingFridge/");
        memberView.setAdapter(memberViewAdapter);
        memberViewAdapter.notifyDataSetChanged();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginRefreshListener = null;
        if (mainDB != null) {
            mainDB.close();
        }
    }

    public interface OnLoginStatusListener {
        void refreshDueToUserChange();
    }

    /**
     * Initialize the member list
     * @return
     */
    public List<MemberItem> initMemberList() {
        List<MemberItem> memberItems = new LinkedList<>();
        if (!UserStatus.hasLogin) {
            memberItems.add(new MemberItem(getString(R.string.login_hint), getString(R.string.no_group_hint),"noimg.png"));
            return memberItems;
        }
        //the dummy table stores dummy items of each group member in group , so find group member from it
        String sql="select owner from dummy where owner!='"+UserStatus.username+"'";
        Cursor c = mainDB.rawQuery(sql, null);
        while (c.moveToNext()) {
            String owner = c.getString(0);
            memberItems.add(new MemberItem(owner,"",owner+".png"));
        }

        return memberItems;
    }


}
