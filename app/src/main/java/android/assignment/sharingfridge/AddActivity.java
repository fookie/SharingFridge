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
import android.util.DisplayMetrics;
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
import android.widget.Toast;

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

@SuppressWarnings("SpellCheckingInspection")
public class AddActivity extends AppCompatActivity implements UploadStatusDelegate {

    private final int CAMERA_CODE = 330;

    private EditText nameEditText;
    private EditText amountEditText;
    public EditText dateEditText;
    public EditText categoryEditText;
    private ImageView itemDisplay;

    private static String[] CATEGORIES;


    private String imageAbsolutePath;
    private String filename;
    private SendRequestTask mAuthTask = null;

    int currentYear, currentMonth, currentDay;
    private Calendar calender = Calendar.getInstance();

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

    private static final int REQUEST_CODE = 0;//TODO Delete or not
    static final String[] PERMISSION = new String[]{
            permission_read,
            permission_write,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
        CATEGORIES = getResources().getStringArray(R.array.categories);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        dateEditText = (EditText) findViewById(R.id.dateEditText);
        categoryEditText = (EditText) findViewById(R.id.categoryEditText);
        itemDisplay = (ImageView) findViewById(R.id.addItemImageView);
        Button cameraButton = (Button) findViewById(R.id.cameraButton);
        Button addButton = (Button) findViewById(R.id.addButton);
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new DatePickerDialog1().show(getFragmentManager(), "expdate");
                calender = Calendar.getInstance();
                currentYear = calender.get(Calendar.YEAR);
                currentMonth = calender.get(Calendar.MONTH);
                currentDay = calender.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                calender.set(year, month, day);
                                canYear = year;
                                canMonth = month;
                                canDay = day;
                                selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calender.getTime());
                                dateEditText.setText(selectedDate);
                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        dateEditText.setLongClickable(false);

        categoryEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.categoryEditText:
                        View outerView = LayoutInflater.from(AddActivity.this).inflate(R.layout.wheel_view, null);
                        WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_v);
//                        categoryEditText.setText("Chicken");
                        wv.setOffset(2);
                        wv.setItems(Arrays.asList(CATEGORIES));
                        wv.setSeletion(3);
                        wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                            @Override
                            public void onSelected(int selectedIndex, String cate) {
                                categoryEditText.setText(cate);
                                selectedCategory = cate;
                            }
                        });

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

        if (cameraButton != null) {
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //                startActivityForResult(cameraIntent, CAMERA_CODE);
                    takeFullSizePicture();
                }
            });
        }


        if (checkBox != null) {
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
        }

        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nameEditText.setError(null);
                    amountEditText.setError(null);
                    amount = amountEditText.getText().toString();
                    name = nameEditText.getText().toString();
                    cat = categoryEditText.getText().toString();
                    if (name.isEmpty()) {
                        nameEditText.setError(getString(R.string.need_name));
                        return;
                    } else if (amount.isEmpty()) {
                        amountEditText.setError(getString(R.string.need_amount));
                        return;
                    } else if (cat.isEmpty()) {
                        categoryEditText.setError(getString(R.string.need_category));
                        return;
                    } else if (canYear < currentYear) {
                        dateEditText.setError(getString(R.string.wrong_date));
                        return;
                    } else if (canMonth < currentMonth) {
                        dateEditText.setError(getString(R.string.wrong_date));
                        return;
                    } else if (canDay < currentDay) {
                        dateEditText.setError(getString(R.string.wrong_date));
                        return;
                    }
                    if (checkCondition) {
                        addCan(canDay, canMonth, canYear, name, amount);
                    }
                    currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    String imgUrl = "image/" + filename;
                    if (UserStatus.hasLogin) {
                        mainDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + name + "', '" + selectedCategory + "', '" + amount + "', '" + currentDate + "', '" + selectedDate + "','" + imgUrl + "','" + UserStatus.username + "', '" + UserStatus.groupName + "')");
                        mAuthTask = new SendRequestTask(name, selectedCategory, amount, currentDate, selectedDate, imgUrl);
                        mAuthTask.execute();
                        uploadInBackgroundService();
                    } else {//do not upload when did not login also set the user name as "local user"
                        mainDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + name + "', '" + selectedCategory + "', '" + amount + "', '" + currentDate + "', '" + selectedDate + "','" + imageAbsolutePath + "','" + "local user" + "', '" + UserStatus.groupName + "')");
                    }
                    finish();
                }
            });
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // itemDisplay.setMinimumHeight(100);
            File photoFile = new File(imageAbsolutePath);
            filename = photoFile.getName();
            Uri uri = Uri.fromFile(photoFile);
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //   photo.compress(Bitmap.CompressFormat.JPEG, 100, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
            } catch (OutOfMemoryError e) {//TODO keep or not
            }
            if (rotationBitmap != photo)
                if (photo != null) {
                    photo.recycle();
                }
            if (rotationBitmap == null)
                rotationBitmap = photo;

            WindowManager wm = this.getWindowManager();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
//            int height = wm.getDefaultDisplay().getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            int bitmapWidth = rotationBitmap != null ? rotationBitmap.getWidth() : 0;
            int bitmapHeight = rotationBitmap != null ? rotationBitmap.getHeight() : 0;
//            float proportionOfWL = (float) bitmapHeight/bitmapWidth;
            Matrix matrix = new Matrix();
            float scaleWidth = (float) width / (bitmapWidth * 2);
            float scaleHeight = scaleWidth * bitmapWidth / bitmapHeight;

            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newBitmap = Bitmap.createBitmap(rotationBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            if (rotationBitmap != null) {
                rotationBitmap.recycle();
            }
            itemDisplay.setImageBitmap(newBitmap);
//            itemDisplay.setImageBitmap(photo);
        }
    }


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
        String imageRelativePath = "file:" + image.getAbsolutePath();//TODO Keep or not
        imageAbsolutePath = image.getAbsolutePath();
        return image;
    }

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
                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
        }
    }


    /*Author: geotv
    * reference from: https://github.com/gotev/android-upload-service
    * */
    public void uploadInBackgroundService() {
        try {
            Log.v("picPath", imageAbsolutePath);
            MultipartUploadRequest req = new MultipartUploadRequest(this, "http://178.62.93.103/SharingFridge/upload2.php")
                    .addFileToUpload(imageAbsolutePath, "file")
                    .setNotificationConfig(new UploadNotificationConfig().setIcon(R.drawable.ic_upload)
                            .setCompletedIcon(R.drawable.ic_upload_done)
                            .setErrorIcon(R.drawable.ic_error).setTitle("Image sync")
                            .setInProgressMessage("Picture synchronizing")
                            .setErrorMessage("Error in synchronizing")
                            .setCompletedMessage("Synchronization completed in [[ELAPSED_TIME]]").setClearOnAction(true).setRingToneEnabled(false))
                    .setUsesFixedLengthStreamingMode(true)
                    .setMaxRetries(3);

            req.setUtf8Charset();

            String uploadId = req.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(UploadInfo uploadInfo) {
                    Toast.makeText(AddActivity.this, "InProgress", Toast.LENGTH_SHORT).show();
                    Log.v("=network=", uploadInfo.getUploadRateString());
                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                    Toast.makeText(AddActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                    Toast.makeText(AddActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                    Log.v("=network=", serverResponse.getBodyAsString());
                }

                @Override
                public void onCancelled(UploadInfo uploadInfo) {
                    Toast.makeText(AddActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }).startUpload();

            Log.i("upload", "id:" + uploadId);
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }


    public void addCan(int addday, int addmonth, int addyear, String name, String amount) {
        String calId = "";
        String calendarURL = "content://com.android.calendar/events";
        Cursor userCursor = getContentResolver().query(Uri.parse(calendarURL), null,
                null, null, null);
        if ((userCursor != null ? userCursor.getCount() : 0) > 0) {
            if (userCursor != null) {
                userCursor.moveToFirst();
            }
            calId = userCursor != null ? userCursor.getString(userCursor.getColumnIndex("_id")) : null;

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
        mCalendar.set(Calendar.HOUR_OF_DAY, 10);
        mCalendar.set(Calendar.MINUTE, 0);
        long start = mCalendar.getTime().getTime();
        mCalendar.set(Calendar.HOUR_OF_DAY, 11);
        long end = mCalendar.getTime().getTime();

        event.put("dtstart", start);
        event.put("dtend", end);
        event.put("hasAlarm", 1);

        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        String calanderEventURL = "content://com.android.calendar/events";
        Uri newEvent = getContentResolver().insert(Uri.parse(calanderEventURL), event);
        long id = Long.parseLong(newEvent != null ? newEvent.getLastPathSegment() : null);
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        values.put("minutes", 10);
        String calanderRemiderURL = "content://com.android.calendar/reminders";
        getContentResolver().insert(Uri.parse(calanderRemiderURL), values);
        if (userCursor != null) {
            userCursor.close();
        }
    }

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
                    finish();//not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

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
        for (byte aBa : ba) {
            Log.e(TAG, String.format("%02X ", aBa));
        }
    }

    @Override
    public void onCancelled(UploadInfo uploadInfo) {

    }


    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/share.php";
        private String item, category, amount, addtime, expiretime, imageurl;

        SendRequestTask(String item, String category, String amount, String addtime, String expiretime, String imageurl) {
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
