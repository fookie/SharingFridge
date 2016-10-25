package android.assignment.sharingfridge;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Paulay on 2016/10/25 0025.
 */

public class MemberViewHolder extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView name;
    TextView activity;

    public MemberViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Member Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        avatar = (ImageView) itemView.findViewById(R.id.memberAvatar);
        name = (TextView) itemView.findViewById(R.id.memberName);
        activity = (TextView) itemView.findViewById(R.id.memberAct);
    }
}
