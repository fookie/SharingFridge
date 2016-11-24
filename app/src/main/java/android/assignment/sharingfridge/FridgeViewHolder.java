package android.assignment.sharingfridge;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

class FridgeViewHolder extends RecyclerView.ViewHolder {

    View holder;
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
        holder = itemView;
        nameView = (TextView) itemView.findViewById(R.id.nameView);
        dateView = (TextView) itemView.findViewById(R.id.dateView);
        photoView = (ImageView) itemView.findViewById(R.id.photoView);
        ownerView = (TextView) itemView.findViewById(R.id.ownerView);
        categoryView = (TextView) itemView.findViewById(R.id.categoryView);
        amountView = (TextView) itemView.findViewById(R.id.amountView);
        minusButton = (FloatingActionButton) itemView.findViewById(R.id.minusButton);
        deleteButton = (FloatingActionButton) itemView.findViewById(R.id.deleteButton);
        reductionAmount = (EditText) itemView.findViewById(R.id.reductionAmount);
        minusButton.setTag(holder);
        deleteButton.setTag(holder);
        reductionAmount.setTag(holder);
    }


}
