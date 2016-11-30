package android.assignment.sharingfridge;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

/**
 * The ViewHolder class for Fridge RecyclerView
 * Used external library: AVLoadingIndicatorView Author: jack wang
 * Copyright 2015 jack wang
 * @see <a href="https://github.com/81813780/AVLoadingIndicatorView">AVLoadingIndicatorView</a>
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
    AVLoadingIndicatorView progressBar;

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
        progressBar = (AVLoadingIndicatorView) itemView.findViewById(R.id.imageProgress);
        minusButton.setTag(holder);//Used for elements to find their parents
        deleteButton.setTag(holder);
        reductionAmount.setTag(holder);
    }


}
