package android.assignment.sharingfridge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.Objects;

import io.rong.imkit.RongIM;

/**
 * The adapter for member recycler view
 * Used external library: Glide, CircularImageView
 * Authors: Glide-Sam Judd. @samajudd on Twitter; CircularImageView-jack wang
 * @see <a href="https://github.com/bumptech/glide">Glide</a>, <a href="https://github.com/Pkmmte/CircularImageView">CircularImageView</a>
 */
public class MemberViewAdapter extends RecyclerView.Adapter<MemberViewHolder> {
    private Context homeContext;
    private List<MemberItem> memberItemList;
    private String picPath = "avatars/";

    MemberViewAdapter(Context context, List<MemberItem> memberList, String serverAddr) {
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
    public void onBindViewHolder(final MemberViewHolder holder, int position) {
        if (position == getItemCount() - 1) holder.divider.setVisibility(View.GONE);

        String name = memberItemList.get(position).getName();
        holder.name.setText(name);
        String defaultHint;
        defaultHint = homeContext.getString(R.string.login_hint);
        holder.activity.setText(memberItemList.get(position).getAct());
        if (!Objects.equals(name, "") && !Objects.equals(name, defaultHint)) {
            holder.cell.setOnClickListener(new cellOnClickListener(name));
        }
        Glide.with(homeContext).load(picPath + memberItemList.get(position).getAvatarUrl())
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.avatarProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.avatarProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
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

        public cellOnClickListener(String id) {
            userID = id;
        }

        @Override
        public void onClick(View v) {
            RongIM.getInstance().startPrivateChat(homeContext, userID, null);
        }
    }
}
