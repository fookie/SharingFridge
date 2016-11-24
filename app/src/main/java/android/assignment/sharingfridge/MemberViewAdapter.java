package android.assignment.sharingfridge;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import io.rong.imkit.RongIM;

/**
 * Created by Paulay on 2016/10/25 0025.
 */

public class MemberViewAdapter extends RecyclerView.Adapter<MemberViewHolder> {
    private Context homeContext;
    private List<MemberItem> memberItemList;
    private String picPath = "avatars/";
    private String name;

    public MemberViewAdapter(Context context, List<MemberItem> memberList, String serverAddr) {
        homeContext = context;
        memberItemList = memberList;
        picPath = serverAddr + picPath;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, null);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        if (position == getItemCount() - 1) holder.divider.setVisibility(View.GONE);

        name = memberItemList.get(position).getName();
        holder.name.setText(name);
        holder.activity.setText(memberItemList.get(position).getAct());
//        holder.cell.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("chat","Name: " + name);
//                RongIM.getInstance().startPrivateChat(homeContext, name, "Talking to " + name);
//            }
//        });
        holder.cell.setOnClickListener(new cellOnClickListener(name));
        //TODO change the placeholder and error later
        Glide.with(homeContext).load(picPath + memberItemList.get(position).getAvatarUrl())
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_corrupt)
                .dontAnimate()
                .into(holder.avatar);
    }


    @Override
    public int getItemCount() {
        return memberItemList.size();
    }

    class cellOnClickListener implements View.OnClickListener {

        String userID;
        public cellOnClickListener(String id){
            userID = id;
        }
        @Override
        public void onClick(View v) {
            RongIM.getInstance().startPrivateChat(homeContext, userID, null);
        }
    }
}
