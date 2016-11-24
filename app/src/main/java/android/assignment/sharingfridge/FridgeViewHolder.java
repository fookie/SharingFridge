package android.assignment.sharingfridge;

import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
    FloatingActionButton minusButton;
    FloatingActionButton deleteButton;
    EditText reductionAmount;

    FridgeViewHolder(final View itemView) {
        super(itemView);
        nameView = (TextView) itemView.findViewById(R.id.nameView);
        dateView = (TextView) itemView.findViewById(R.id.dateView);
        photoView = (ImageView) itemView.findViewById(R.id.photoView);
        ownerView = (TextView) itemView.findViewById(R.id.ownerView);
        categoryView = (TextView) itemView.findViewById(R.id.categoryView);
        amountView = (TextView) itemView.findViewById(R.id.amountView);
        minusButton = (FloatingActionButton) itemView.findViewById(R.id.minusButton);
        deleteButton = (FloatingActionButton) itemView.findViewById(R.id.deleteButton);
        reductionAmount = (EditText) itemView.findViewById(R.id.reductionAmount);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minusButton.getVisibility() == View.GONE) {
                    //TODO Animation
                    minusButton.setVisibility(View.VISIBLE);
                } else {
                    minusButton.setVisibility(View.GONE);
                }
                if (deleteButton.getVisibility() == View.GONE) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
                Toast.makeText(v.getContext(), "Item Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
