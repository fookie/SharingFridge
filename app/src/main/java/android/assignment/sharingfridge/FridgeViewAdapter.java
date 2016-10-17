package android.assignment.sharingfridge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by EveLIn3 on 2016/10/16.
 */

public class FridgeViewAdapter extends RecyclerView.Adapter<FridgeViewHolder> {

    private String serverPicsPath = "http://178.62.93.103/SharingFridge/";
    private List<FridgeItem> fridgeItemsList;
    private Context homeContext;

    public FridgeViewAdapter(Context context, List<FridgeItem> fil) {
        homeContext = context;
        fridgeItemsList = fil;
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
//        holder.photoView.setImageResource(R.drawable.shine);
        // here need to work with glide or picasso to deal with image loading maybe.
        Glide.with(homeContext).load(serverPicsPath + fridgeItemsList.get(position).getPhotoURL()).placeholder(R.drawable.shine).error(R.mipmap.ic_launcher).into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return fridgeItemsList.size();
    }
}
