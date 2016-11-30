package android.assignment.sharingfridge;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by Paulay on 2016/10/25 0025.
 * The viewholder class for member RecyclerView
 */

public class MemberViewHolder extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView name;
    TextView activity;
    TextView divider;
    RelativeLayout cell;
    AVLoadingIndicatorView avatarProgress;

    public MemberViewHolder(View itemView) {
        super(itemView);
        avatar = (ImageView) itemView.findViewById(R.id.memberAvatar);
        name = (TextView) itemView.findViewById(R.id.memberName);
        activity = (TextView) itemView.findViewById(R.id.memberAct);
        divider = (TextView) itemView.findViewById(R.id.memberItemDivider);
        cell = (RelativeLayout) itemView.findViewById(R.id.memberItemContainer);
        avatarProgress = (AVLoadingIndicatorView) itemView.findViewById(R.id.avatarProgress);
    }
}
