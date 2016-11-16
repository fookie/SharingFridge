package android.assignment.sharingfridge;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

class FridgeViewHolder extends RecyclerView.ViewHolder {

    TextView nameView;
    TextView dateView;
    ImageView photoView;
    TextView ownerView;
    TextView categoryView;
    TextView amountView;

    FridgeViewHolder(View itemView) {
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
        ownerView = (TextView) itemView.findViewById(R.id.ownerView);
        categoryView = (TextView) itemView.findViewById(R.id.categoryView);
        amountView = (TextView) itemView.findViewById(R.id.amountView);
    }

}
