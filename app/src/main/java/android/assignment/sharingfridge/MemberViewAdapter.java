package android.assignment.sharingfridge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        if (position == getItemCount() - 1) holder.divider.setVisibility(View.GONE);
        holder.name.setText(memberItemList.get(position).getName());
        holder.activity.setText(memberItemList.get(position).getAct());
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
}
