package android.assignment.sharingfridge;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.util.List;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

public class FridgeViewAdapter extends RecyclerView.Adapter<FridgeViewHolder> {

    private String serverPicsPath = "";
    private List<FridgeItem> fridgeItemsList;
    private Context homeContext;

    public FridgeViewAdapter(Context context, List<FridgeItem> fil, String serverPath) {
        homeContext = context;
        fridgeItemsList = fil;
        serverPicsPath = serverPath + serverPicsPath;
    }

    @Override
    public FridgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View eachView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, null);
        FridgeViewHolder fvh = new FridgeViewHolder(eachView);
        return fvh;
    }

    @Override
    public void onBindViewHolder(FridgeViewHolder holder, int position) {
        holder.nameView.setText(fridgeItemsList.get(position).getName());
        holder.dateView.setText(fridgeItemsList.get(position).getDate());
        //TODO change the place holder and error
        Glide.with(homeContext).load(serverPicsPath + fridgeItemsList.get(position).getPhotoURL())
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_corrupt)
                .dontAnimate()
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return fridgeItemsList.size();
    }
}
