package android.assignment.sharingfridge;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

public class FridgeViewHolder extends RecyclerView.ViewHolder {

    public TextView nameView;
    public TextView dateView;
    public ImageView photoView;

    public FridgeViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Item Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        nameView = (TextView) itemView.findViewById(R.id.nameView);
        dateView = (TextView) itemView.findViewById(R.id.dateView);
        photoView = (ImageView) itemView.findViewById(R.id.photoView);
    }

}
