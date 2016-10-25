package android.assignment.sharingfridge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Paulay on 2016/10/25 0025.
 */

public class MemberViewAdapter extends RecyclerView.Adapter<MemberViewHolder> {
    private Context homeContext;
    private List<MemberItem> memberItemList;
    private String picPath = "avatars/";

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
        holder.name.setText(memberItemList.get(position).getName());
        holder.activity.setText(memberItemList.get(position).getAct());
        //TODO change the placeholder and error later
        Glide.with(homeContext).load(picPath + memberItemList.get(position).getAvatarUrl())
                .centerCrop()
                .placeholder(R.drawable.shine)
                .error(R.mipmap.ic_launcher)
                .dontAnimate()
                .into(holder.avatar);
    }


    @Override
    public int getItemCount() {
        return memberItemList.size();
    }
}
