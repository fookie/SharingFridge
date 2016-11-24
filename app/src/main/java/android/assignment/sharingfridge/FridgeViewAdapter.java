package android.assignment.sharingfridge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * Created by EveLIn3 on 2016/10/16.
 */

class FridgeViewAdapter extends RecyclerView.Adapter<FridgeViewHolder> {

    private String serverPicsPath = "";
    private List<FridgeItem> fridgeItemsList;
    private Context homeContext;
    private EditText reductionAmount;
    private SendRequestTask mAuthTask;

    SQLiteDatabase db;

    FridgeViewAdapter(Context context, List<FridgeItem> fil, String serverPath) {
        homeContext = context;
        fridgeItemsList = fil;
        db = SQLiteDatabase.openOrCreateDatabase(homeContext.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        serverPicsPath = serverPath + serverPicsPath;
    }

    @Override
    public FridgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View eachView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, null);
        FridgeViewHolder fvh = new FridgeViewHolder(eachView);
        return fvh;
    }

    @Override
    public void onBindViewHolder(final FridgeViewHolder holder, final int position) {
        applyVisibility(holder, position);
        holder.holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverseVisibility(position);
                notifyDataSetChanged();
                applyVisibility(holder, position);
            }
        });
        holder.nameView.setText(fridgeItemsList.get(position).getName());
        holder.dateView.setText(fridgeItemsList.get(position).getDate());
        holder.categoryView.setText(fridgeItemsList.get(position).getCategory());
        holder.amountView.setText(String.valueOf(fridgeItemsList.get(position).getAmount()));
        holder.ownerView.setText(fridgeItemsList.get(position).getOwner());
        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reductionAmount = holder.reductionAmount;
                if (reductionAmount.getText().equals("")) {
                    reductionAmount.setText(1 + "");
                }
                if (reductionAmount.getVisibility() == View.GONE) {
                    if (fridgeItemsList.get(position).getAmount() - 1 <= 0) {
                        deleteItem(position, fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
                    } else {
                        amountReduction(position, 1, fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
                    }
                } else {
                    if (reductionAmount.hasFocus()) {
                        reductionAmount.clearFocus();//prevent buttons' position shifting
                    }
                    if (fridgeItemsList.get(position).getAmount() - Integer.parseInt(reductionAmount.getText().toString()) <= 0) {
                        deleteItem(position, fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
                    } else {
                        amountReduction(position, Integer.parseInt(reductionAmount.getText().toString()), fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
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
                    reductionAmount.setVisibility(View.VISIBLE);
                } else if (reductionAmount.getVisibility() == View.VISIBLE) {
                    reductionAmount.setVisibility(View.GONE);
                }
                fridgeItemsList.get(position).reverseReductionBox();
                return true;
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position, fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
                notifyDataSetChanged();
            }
        });
        Glide.with(homeContext).load(((fridgeItemsList.get(position).getOwner().equals("local user")) ? "" : serverPicsPath) + fridgeItemsList.get(position).getPhotoURL())
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_corrupt)
                .dontAnimate()
                .into(holder.photoView);

    }

    private void reverseVisibility(int position) {
        fridgeItemsList.get(position).reverseExpanded();
    }

    private void hideIfNot(View v) {
        //TODO Animation
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        }
    }

    private void showIfNot(View v) {
        if (v.getVisibility() == View.GONE) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void applyVisibility(FridgeViewHolder holder, int position) {
        if (fridgeItemsList.get(position).isButtonsExpanded()) {
            showIfNot(holder.deleteButton);
            showIfNot(holder.minusButton);
        } else {
            hideIfNot(holder.deleteButton);
            holder.minusButton.setVisibility(View.GONE);
            fridgeItemsList.get(position).setReductionBox(false);
            holder.reductionAmount.setVisibility(View.GONE);
        }
        if (fridgeItemsList.get(position).isReductionBox()) {
            holder.reductionAmount.setVisibility(View.VISIBLE);
        } else {
            holder.reductionAmount.setVisibility(View.GONE);
        }
    }

    private void amountReduction(int position, int sub, String name, String owner) {
        fridgeItemsList.get(position).minus(sub);
        db.execSQL("UPDATE items SET amount = amount - " + sub + " WHERE item = '" + name + "';");
        mAuthTask = new SendRequestTask(owner, name, sub);
        mAuthTask.execute();
    }

    private void deleteItem(int position, String name, String owner) {
        fridgeItemsList.remove(position);
        db.execSQL("DELETE FROM items WHERE item = '" + name + "';");
        mAuthTask = new SendRequestTask(owner, name, -1);
        mAuthTask.execute();
    }

    @Override
    public int getItemCount() {
        return fridgeItemsList.size();
    }

    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/delete.php";
        private String owner, item;
        private int amount;

        public SendRequestTask(String owner, String item, int amount) {
            this.owner = owner;
            this.item = item;
            this.amount = amount;
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        public String performPostCall() {
            Log.d("send post", "performPostCall");
            String response = "";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);/* milliseconds */
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //conn.setRequestProperty("Content-Type", "application/json");
                //make json object
                JSONObject jo = new JSONObject();
                jo.put("owner", owner);
                jo.put("item", item);
                jo.put("amount", amount);
                String tosend = jo.toString();
                Log.d("JSON", tosend);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("delete=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = conn.getResponseCode();

                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                int length = 500;
                String contentAsString = convertInputStreamToString(inputStream, length);
                conn.disconnect();
                return contentAsString;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        public String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;

        }
    }

}
