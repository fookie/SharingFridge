package android.assignment.sharingfridge;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * The activity for adding an new item that can be accessed by the '+' button on top right.
 *
 */
public class AddActivity extends AppCompatActivity implements UploadStatusDelegate {

    private final int CAMERA_CODE = 330; // just to identify camera intent, not meaningful

    private EditText nameEditText;
    private EditText amountEditText;
    public EditText dateEditText;
    public EditText categoryEditText;
    private ImageView itemDisplay;
    private Button cameraButton;
    private CheckBox checkBox;
    private Button addButton;

    private static final String[] CATEGORYS = new String[]{"Fruit", "Vegetable", "Pork", "Chicken", "Beef", "Fish", "Others"};
    private static final String[] CATEGORYS_CHINESE = new String[]{"水果", "蔬菜", "猪肉", "鸡肉", "牛肉", "鱼肉", "其他"};


    private String imageAbsolutePath, filename;
    private SendRequestTask mAuthTask = null;

    int currentYear, currentMonth, currentDay;
    private Calendar calender = Calendar.getInstance();

    private static String calendarURL = "content://com.android.calendar/events";
    private static String calanderEventURL = "content://com.android.calendar/events";
    private static String calanderRemiderURL = "content://com.android.calendar/reminders";
    public static final String permission_read = Manifest.permission.READ_CALENDAR;
    public static final String permission_write = Manifest.permission.WRITE_CALENDAR;
    public static final String TAG = "CalendarActivity";

    public int canYear;
    public int canMonth;
    public int canDay;
    public String name;
    public String amount;
    public String cat;
    public String selectedDate;
    public String currentDate;
    public String selectedCategory = null;
    public boolean checkCondition;

    private SQLiteDatabase mainDB;

    static final String[] PERMISSION = new String[]{
            permission_read,
            permission_write,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //Some dangerous permission need to be special granted if target API > 6.0
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        dateEditText = (EditText) findViewById(R.id.dateEditText);
        categoryEditText = (EditText) findViewById(R.id.categoryEditText);
        itemDisplay = (ImageView) findViewById(R.id.addItemImageView);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        addButton = (Button) findViewById(R.id.addButton);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");

        //show date selection dialog in a DatePickerDialog and save data in the format "dd-mm-yyyy"
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calender = Calendar.getInstance();
                currentYear = calender.get(Calendar.YEAR);
                currentMonth = calender.get(Calendar.MONTH)+1;
                currentDay = calender.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                calender.set(year, month, day);
                                canYear = year;
                                canMonth = month+1;
                                canDay = day;
                                selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calender.getTime());
                                dateEditText.setText(selectedDate);
                            }
                        }, currentYear, currentMonth-1, currentDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
        //forbid the paste function  by long click the edittext
        dateEditText.setLongClickable(false);

        //using a wheel view inside a dialog to select category
        categoryEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.categoryEditText:
                        View outerView = LayoutInflater.from(AddActivity.this).inflate(R.layout.wheel_view, null);
                        WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_v);
                        //set initial point
                        wv.setOffset(2);
                        //language localization
                        Locale locale = getResources().getConfiguration().locale;
                        String language = locale.getLanguage();
                        if (language.endsWith("zh")) {
                            categoryEditText.setText("鸡肉");
                            wv.setItems(Arrays.asList(CATEGORYS_CHINESE));
                        } else {
                            categoryEditText.setText("Chicken");
                            wv.setItems(Arrays.asList(CATEGORYS));
                        }
                        wv.setSeletion(3);
                        wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                            @Override
                            public void onSelected(int selectedIndex, String cate) {
                                categoryEditText.setText(cate);
                                int i = selectedIndex;
                                //catch out of bound exception for the selected index
                                try {
                                    selectedCategory = CATEGORYS[i];
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    if (i > 6) {
                                        selectedCategory = CATEGORYS[6];
                                    } else {
                                        selectedCategory = CATEGORYS[0];
                                    }
                                }
                            }
                        });

                        //dialog interface
                        new AlertDialog.Builder(AddActivity.this)
                                .setTitle(getString(R.string.choose_category))
                                .setView(outerView)
                                .setPositiveButton(getString(R.string.submit), null)
                                .show();

                        break;
                }

            }
        });

        categoryEditText.setLongClickable(false);

        //take picture for the new item
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeFullSizePicture();
            }
        });


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkCondition = true;
                } else {
                    checkCondition = false;
                }
            }
        });

        //check the condition of each attribute before adding into database
        //missing information and invalid expire date will be reminded
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameEditText.setError(null);
                amountEditText.setError(null);
                amount = amountEditText.getText().toString();
                name = nameEditText.getText().toString();
                cat = categoryEditText.getText().toString();
                int currentTime=currentYear*10000+currentMonth*100+currentDay;
                int canTime=canYear*10000+canMonth*100+canDay;
                //check if there is any empty or invalid field in the form first
                if (name.isEmpty()) {
                    nameEditText.setError(getString(R.string.need_name));
                    return;
                } else if (amount.isEmpty()) {
                    amountEditText.setError(getString(R.string.need_amount));
                    return;
                } else if (cat.isEmpty()) {
                    categoryEditText.setError(getString(R.string.need_category));
                    return;
                } else if (canTime < currentTime) {
                    dateEditText.setError(getString(R.string.wrong_date));
                    return;
                }
                //send information to calender
                if (checkCondition) {
                    addCan(canDay, canMonth, canYear, name, amount);
                }
                currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new java.util.Date());
                String imgUrl = "image/" + filename;
                //check the login status, only upload to the server when user has login
                if (UserStatus.hasLogin) {
                    mainDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + name + "', '" + selectedCategory + "', '" + amount + "', '" + currentDate + "', '" + selectedDate + "','" + imgUrl + "','" + UserStatus.username + "', '" + UserStatus.groupName + "')");
                    mAuthTask = new SendRequestTask(name, selectedCategory, amount, currentDate, selectedDate, imgUrl);
                    mAuthTask.execute();
                    uploadInBackgroundService();
                } else {
                    //set the user name as "local user" and no upload
                    mainDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + name + "', '" + selectedCategory + "', '" + amount + "', '" + currentDate + "', '" + selectedDate + "','" + imageAbsolutePath + "','" + "local user" + "', '" + UserStatus.groupName + "')");
                }
                finish();
            }
        });

    }

    //some operations for the photo, deal with the auto rotation and change size for  preview
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            File photoFile = new File(imageAbsolutePath);
            filename = photoFile.getName();
            Uri uri = Uri.fromFile(photoFile);
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //some devices have rotation info saving together with the photo,which will make preview in wrong direction
            //check the direction first and adjust when it has been rotated.
            int rotation = 0;
            try {
                ExifInterface exifInterface = new ExifInterface(photoFile.getPath());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(rotation);
            Bitmap rotationBitmap = null;
            try {
                rotationBitmap = Bitmap.createBitmap(photo, 0, 0, photo != null ? photo.getWidth() : 0, photo != null ? photo.getHeight() : 0, rotationMatrix, true);
            } catch (OutOfMemoryError e) {
            }
            if (rotationBitmap != photo && photo != null)
                photo.recycle();
            if (rotationBitmap == null)
                rotationBitmap = photo;

            //change photo size based on the resolution of display device
            WindowManager wm = this.getWindowManager();
            int width = wm.getDefaultDisplay().getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            int bitmapWidth = rotationBitmap != null ? rotationBitmap.getWidth() : 0;
            int bitmapHeight = rotationBitmap != null ? rotationBitmap.getHeight() : 0;
            Matrix matrix = new Matrix();
            float scaleWidth = (float) width / (bitmapWidth * 2);
            float scaleHeight = scaleWidth * bitmapWidth / bitmapHeight;

            matrix.postScale(scaleWidth, scaleHeight);
            //change size by multiply a given matrix
            Bitmap newBitmap = Bitmap.createBitmap(rotationBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            if (rotationBitmap != null) {
                rotationBitmap.recycle();
            }
            itemDisplay.setImageBitmap(newBitmap);
        }
    }

    /**
     * create an empty image file on the pictures directory in the phone
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = UserStatus.username + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageAbsolutePath = image.getAbsolutePath();
        return image;
    }

    /**
     * connect to the camera and capture an full-sized image
     */
    private void takeFullSizePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                        String permissions[] = {Manifest.permission.CAMERA};
                        ActivityCompat.requestPermissions(AddActivity.this, permissions, 5238);
                    }else {
                        startActivityForResult(takePictureIntent, CAMERA_CODE);
                    }
            }
        }
    }

    /**
     * upload the photo to the server using android-upload-service.
     * The server side needs a php to handle the file.
     * Author: geotv
     * reference from: https://github.com/gotev/android-upload-service
     */
    public void uploadInBackgroundService() {
        try {
            Log.v("picPath", imageAbsolutePath);
            MultipartUploadRequest req = new MultipartUploadRequest(this, "http://178.62.93.103/SharingFridge/upload2.php")
                    .addFileToUpload(imageAbsolutePath, "file")
                    .setNotificationConfig(new UploadNotificationConfig().setIcon(R.drawable.ic_upload)
                            .setCompletedIcon(R.drawable.ic_upload_done)
                            .setErrorIcon(R.drawable.ic_error).setTitle(getString(R.string.image_synced))
                            .setInProgressMessage(getString(R.string.image_syncing))
                            .setErrorMessage(getString(R.string.image_sync_error))
                            .setCompletedMessage(getString(R.string.image_sync_completed)).setClearOnAction(true).setRingToneEnabled(false))
                    .setUsesFixedLengthStreamingMode(true)
                    .setMaxRetries(3);

            req.setUtf8Charset();

            String uploadId = req.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(UploadInfo uploadInfo) {
                    Log.v("=network=", uploadInfo.getUploadRateString());
                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                    Log.v("=network=", serverResponse.getBodyAsString());
                }

                @Override
                public void onCancelled(UploadInfo uploadInfo) {
                }
            }).startUpload();

            Log.i("upload", "id:" + uploadId);
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    /**
     * add an reminder event to the system's calender
     *
     * @param addday
     * @param addmonth
     * @param addyear
     * @param name    items that going to expire
     * @param amount
     */
    public void addCan(int addday, int addmonth, int addyear, String name, String amount) {
        String calId = "";
        Cursor userCursor = getContentResolver().query(Uri.parse(calendarURL), null,
                null, null, null);
        if (userCursor != null && userCursor.getCount() > 0) {
            userCursor.moveToFirst();
            calId = userCursor.getString(userCursor.getColumnIndex("_id"));

        }

        String title = "" + name;
        String des = "amount:" + amount;

        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", des);
        event.put("calendar_id", calId);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.DAY_OF_MONTH, addday);
        mCalendar.set(Calendar.MONTH, addmonth);
        mCalendar.set(Calendar.YEAR, addyear);
        //set alert time as 10 AM
        mCalendar.set(Calendar.HOUR_OF_DAY, 10);
        mCalendar.set(Calendar.MINUTE, 0);
        long start = mCalendar.getTime().getTime();
        mCalendar.set(Calendar.HOUR_OF_DAY, 11);
        long end = mCalendar.getTime().getTime();

        //put start and end time to system calendar
        event.put("dtstart", start);
        event.put("dtend", end);
        //open alert alarm
        event.put("hasAlarm", 1);

        //use current timezone for the event
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri newEvent = getContentResolver().insert(Uri.parse(calanderEventURL), event);
        long id = Long.parseLong(newEvent != null ? newEvent.getLastPathSegment() : null);
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        values.put("minutes", 10);
        getContentResolver().insert(Uri.parse(calanderRemiderURL), values);
    }

    /***
     * check permission every time enter the activity
     * if permission is already granted, don't ask again
     */
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //not granted
                    finish();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /***
     * upload progress
     * @param uploadInfo
     */
    @Override
    public void onProgress(UploadInfo uploadInfo) {
        Log.i(TAG, String.format(Locale.getDefault(), "ID: %1$s (%2$d%%) at %3$.2f Kbit/s",
                uploadInfo.getUploadId(), uploadInfo.getProgressPercent(),
                uploadInfo.getUploadRate()));
    }

    @Override
    public void onError(UploadInfo uploadInfo, Exception exception) {

    }

    @Override
    public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.i(TAG, String.format(Locale.getDefault(),
                "ID %1$s: completed in %2$ds at %3$.2f Kbit/s. Response code: %4$d, body:[%5$s]",
                uploadInfo.getUploadId(), uploadInfo.getElapsedTime() / 1000,
                uploadInfo.getUploadRate(), serverResponse.getHttpCode(),
                serverResponse.getBodyAsString()));
        for (Map.Entry<String, String> header : serverResponse.getHeaders().entrySet()) {
            Log.i("Header", header.getKey() + ": " + header.getValue());
        }

        Log.e(TAG, "Printing response body bytes");
        byte[] ba = serverResponse.getBody();
        for (int j = 0; j < ba.length; j++) {
            Log.e(TAG, String.format("%02X ", ba[j]));
        }
    }

    @Override
    public void onCancelled(UploadInfo uploadInfo) {

    }

    /**
     *  Aysnctask subclass that send an adding item quest to server
     */
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/share.php";
        private String item, category, amount, addtime, expiretime, imageurl;

        public SendRequestTask(String item, String category, String amount, String addtime, String expiretime, String imageurl) {
            this.item = item;
            this.category = category;
            this.amount = amount;
            this.addtime = addtime;
            this.expiretime = expiretime;
            this.imageurl = imageurl;
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
                //make json object
                JSONObject jo = new JSONObject();
                jo.put("item", item);
                jo.put("category", category);
                jo.put("amount", amount);
                jo.put("addtime", addtime);
                jo.put("expiretime", expiretime);
                jo.put("imageurl", imageurl);
                jo.put("owner", UserStatus.username);
                jo.put("groupname", UserStatus.groupName);

                String tosend = jo.toString();
                Log.d("JSON", tosend);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("share=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = conn.getResponseCode();

                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                int length = 500;
                return convertInputStreamToString(inputStream, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            String permission;
            try {
                JSONObject confirm = new JSONObject(result);
                permission = confirm.get("result").toString();
            } catch (JSONException je) {
                Log.d("UPLOAD DB", "failed!");
                je.printStackTrace();
            }
        }
    }


}
