package android.assignment.sharingfridge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;


/**
 * Adapter for the main Fridge RecyclerView in {@link FridgeFragment}
 * <br/>
 * Used external libraries: Glide, Glide transformations.
 * Author: Sam Judd. @samajudd on Twitter
 *
 * @see <a href="https://github.com/bumptech/glide">Glide</a>, <a href="https://github.com/wasabeef/glide-transformations">Glide transformations</a>
 */

class FridgeViewAdapter extends RecyclerView.Adapter<FridgeViewHolder> {

    private String serverPicsPath = "";
    private List<FridgeItem> fridgeItemsList;
    private Context homeContext;
    private EditText reductionAmount;
    private SendRequestTask mAuthTask;
    private SQLiteDatabase db;

    FridgeViewAdapter(Context context, List<FridgeItem> fil, String serverPath) {
        homeContext = context;
        fridgeItemsList = fil;
        db = SQLiteDatabase.openOrCreateDatabase(homeContext.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        serverPicsPath = serverPath + serverPicsPath;
    }

    @Override
    public FridgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View eachView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, null);
        return new FridgeViewHolder(eachView);
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
        int d = fridgeItemsList.get(position).getDate();

        holder.dateView.setText(d < 0 ? (-d < 2 ? (String.format(homeContext.getString(R.string.bad_day), -d + "")) : (String.format(homeContext.getString(R.string.bad_days), -d + ""))) : (d < 2 ? (d + " " + homeContext.getString(R.string.left_day)) : (d + " " + homeContext.getString(R.string.left_days))));
        if (d < 0) {//change text color according to expiration date
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.red, homeContext.getTheme()));
            } else {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.red));
            }
        } else if (d <= 2) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.orange, homeContext.getTheme()));
            } else {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.orange));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.green, homeContext.getTheme()));
            } else {
                holder.dateView.setTextColor(homeContext.getResources().getColor(R.color.green));
            }
        }
        holder.categoryView.setText(fridgeItemsList.get(position).getCategory());
        holder.amountView.setText(String.valueOf(fridgeItemsList.get(position).getAmount()));
        holder.ownerView.setText(fridgeItemsList.get(position).getOwner());
        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reductionAmount = holder.reductionAmount;
                if (reductionAmount.getText().toString().equals("")) {
                    reductionAmount.setText(String.valueOf(1));
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
        //long click to show a text field which allow user to set its own value
        holder.minusButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                reductionAmount = holder.reductionAmount;
                if (reductionAmount.getVisibility() == View.GONE) {
                    reductionAmount.setVisibility(View.VISIBLE);
                } else if (reductionAmount.getVisibility() == View.VISIBLE) {
                    reductionAmount.setVisibility(View.GONE);
                }
                fridgeItemsList.get(position).reverseReductionBox();
                return true;
            }
        });
        //delete item
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position, fridgeItemsList.get(position).getName(), fridgeItemsList.get(position).getOwner());
                notifyDataSetChanged();
            }
        });
        if (d < 0) {
            Glide.with(homeContext).load(((fridgeItemsList.get(position).getOwner().equals("local user")) ? "" : serverPicsPath) + fridgeItemsList.get(position).getPhotoURL())
                    .centerCrop()
                    .placeholder(R.drawable.image_loading)//don't know why, placeholder is necessary or the app will crash. Probably just a minor bug of Glide.
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            if (!e.getMessage().equals("Request failed 404: Not Found")) {
                                holder.progressBar.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.image_corrupt)
                    .dontAnimate().bitmapTransform(new GrayscaleTransformation(homeContext))//turn the out-dated items images to grey
                    .into(holder.photoView);
        } else {
            Glide.with(homeContext).load(((fridgeItemsList.get(position).getOwner().equals("local user")) ? "" : serverPicsPath) + fridgeItemsList.get(position).getPhotoURL())
                    .centerCrop()
                    .placeholder(R.drawable.image_loading)//don't know why, placeholder is necessary or the app will crash. Probably just a minor bug of Glide.
                    .listener(new RequestListener<String, GlideDrawable>() {//hide the progress indicator if the loading is completed or failed
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.image_corrupt)
                    .dontAnimate()
                    .into(holder.photoView);
        }

    }

    /**
     * Switch visibility of the buttons for a card between VISIBLE and GONE
     *
     * @param position Position in the list
     */
    private void reverseVisibility(int position) {
        fridgeItemsList.get(position).reverseExpanded();
    }

    /**
     * Hide a button if it's not GONE
     *
     * @param v Button to be hid
     */
    private void hideIfNot(View v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Show a button if it's not VISIBLE
     *
     * @param v Button to be showed
     */
    private void showIfNot(View v) {
        if (v.getVisibility() == View.GONE) {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Apply the visibility setting to specific position
     *
     * @param card     Holder to be applied
     * @param position Position of the item
     */
    private void applyVisibility(FridgeViewHolder card, int position) {
        if (fridgeItemsList.get(position).isButtonsExpanded()) {
            showIfNot(card.deleteButton);
            showIfNot(card.minusButton);
        } else {
            hideIfNot(card.deleteButton);
            card.minusButton.setVisibility(View.GONE);
            fridgeItemsList.get(position).setReductionBox(false);
            card.reductionAmount.setVisibility(View.GONE);
        }
        if (fridgeItemsList.get(position).isReductionBox()) {
            card.reductionAmount.setVisibility(View.VISIBLE);
        } else {
            card.reductionAmount.setVisibility(View.GONE);
        }
    }

    /**
     * Reduce the amount
     * @param position position of the item in the list
     * @param sub Amount to decreased
     * @param name Name of the Item
     * @param owner Owner of the item
     */
    private void amountReduction(int position, int sub, String name, String owner) {
        fridgeItemsList.get(position).minus(sub);
        db.execSQL("UPDATE items SET amount = amount - " + sub + " WHERE item = '" + name + "';");
        mAuthTask = new SendRequestTask(owner, name, sub);
        mAuthTask.execute();
    }

    /**
     * Delete an item
     * @param position position of the item in the list
     * @param name name of the item
     * @param owner owner of the item
     */
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

    /**
     * send delete request to the server
     */
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/delete.php";
        private String owner, item;
        private int amount;

        SendRequestTask(String owner, String item, int amount) {
            this.owner = owner;
            this.item = item;
            this.amount = amount;
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        String performPostCall() {
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

                conn.getResponseCode();

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

        String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader;
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
