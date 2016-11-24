package android.assignment.sharingfridge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

class FridgeViewAdapter extends RecyclerView.Adapter<FridgeViewHolder> {

    private String serverPicsPath = "";
    private List<FridgeItem> fridgeItemsList;
    private Context homeContext;
    private EditText reductionAmount;
    private int position;

    SQLiteDatabase db;

    FridgeViewAdapter(Context context, List<FridgeItem> fil, String serverPath) {
        homeContext = context;
        fridgeItemsList = fil;
        serverPicsPath = serverPath + serverPicsPath;
    }

    @Override
    public FridgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View eachView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, null);
        db = SQLiteDatabase.openOrCreateDatabase(homeContext.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        FridgeViewHolder fvh = new FridgeViewHolder(eachView);
        return fvh;
    }

    @Override
    public void onBindViewHolder(final FridgeViewHolder holder, final int position) {
        this.position = position;
        holder.nameView.setText(fridgeItemsList.get(this.position).getName());
        holder.dateView.setText(fridgeItemsList.get(this.position).getDate());
        holder.categoryView.setText(fridgeItemsList.get(this.position).getCategory());
        holder.amountView.setText(String.valueOf(fridgeItemsList.get(this.position).getAmount()));
        holder.ownerView.setText(fridgeItemsList.get(this.position).getOwner());
        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reductionAmount = holder.reductionAmount;
                if (reductionAmount.getVisibility() == View.GONE) {
                    if (fridgeItemsList.get(FridgeViewAdapter.this.position).getAmount() - 1 < 0) {
                        deleteItem(fridgeItemsList.get(FridgeViewAdapter.this.position).getName());
                    } else {
                        amountReduction(1, fridgeItemsList.get(FridgeViewAdapter.this.position).getName());
                    }
                } else {
                    if (fridgeItemsList.get(FridgeViewAdapter.this.position).getAmount() - Integer.parseInt(reductionAmount.getText().toString()) < 0) {
                        deleteItem(fridgeItemsList.get(FridgeViewAdapter.this.position).getName());
                    } else {
                        amountReduction(Integer.parseInt(reductionAmount.getText().toString()), fridgeItemsList.get(FridgeViewAdapter.this.position).getName());
                    }
                }
                notifyDataSetChanged();
            }
        });
        holder.minusButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                reductionAmount = holder.reductionAmount;
                if (reductionAmount.getVisibility() == View.GONE) {//TODO Animation
                    reductionAmount.startAnimation(AnimationUtils.loadAnimation(homeContext, android.support.v7.appcompat.R.anim.abc_slide_in_bottom));
                    reductionAmount.setVisibility(View.VISIBLE);
                } else if (reductionAmount.getVisibility() == View.VISIBLE) {
                    reductionAmount.startAnimation(AnimationUtils.loadAnimation(homeContext, android.support.v7.appcompat.R.anim.abc_slide_out_bottom));
                    reductionAmount.setVisibility(View.GONE);
                }
                notifyDataSetChanged();
                return true;
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(fridgeItemsList.get(FridgeViewAdapter.this.position).getName());
                notifyDataSetChanged();
            }
        });
        Glide.with(homeContext).load(((fridgeItemsList.get(this.position).getOwner().equals("local user")) ? "" : serverPicsPath) + fridgeItemsList.get(this.position).getPhotoURL())
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_corrupt)
                .dontAnimate()
                .into(holder.photoView);

    }

    private void amountReduction(int sub, String name) {
        db.execSQL("UPDATE items SET amount = amount - " + sub + " WHERE item = '" + name + "';");
        //TODO network operations!
    }

    private void deleteItem(String name) {
        db.execSQL("DELETE FROM items WHERE item = '" + name + "';");
        //TODO network operations!
    }

    @Override
    public int getItemCount() {
        return fridgeItemsList.size();
    }
}
