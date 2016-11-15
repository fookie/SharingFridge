package android.assignment.sharingfridge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.assignment.sharingfridge.R.id.nameEditText;
import static java.lang.System.out;

public class AddActivity extends AppCompatActivity {

    private final int CAMERA_CODE = 330;

    private EditText nameEditText;
    private EditText amountEditText;
    public EditText dateEditText;
    private ImageView itemDisplay;
    private Button cameraButton;
    //private Button calendarButton;
    private CheckBox checkBox;
    private Button addButton;

    private String imageRelativePath, imageAbsolutePath;
    private Uri imageUri;

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
    public String selectedDate;
    public String currentDate;
    public boolean checkCondition;

    private SQLiteDatabase mainDB;

    private static final int REQUEST_CODE = 0;
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

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        dateEditText = (EditText) findViewById(R.id.dateEditText);
        itemDisplay = (ImageView) findViewById(R.id.addItemImageView);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        addButton = (Button) findViewById(R.id.addButton);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
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
                                selectedDate = new SimpleDateFormat("dd-MM-yyyy").format(calender.getTime());
                                dateEditText.setText(selectedDate);
                            }
                        }, currentYear, currentMonth, currentDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_CODE);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameEditText.setError(null);
                amountEditText.setError(null);
                amount = amountEditText.getText().toString();
                name = nameEditText.getText().toString();
                if (name.isEmpty()) {
                    nameEditText.setError(getString(R.string.need_name));
                    return;
                } else if (amount.isEmpty()) {
                    amountEditText.setError(getString(R.string.need_amount));
                    return;
                } else if (canYear < currentYear) {
                    dateEditText.setError(getString(R.string.wrong_date));
                    return;
                } else if (canMonth < currentMonth) {
                    dateEditText.setError(getString(R.string.wrong_date));
                    {
                        return;
                    }
                } else if (canDay < currentDay) {
                    dateEditText.setError(getString(R.string.wrong_date));
                    return;
                }
                if (checkCondition) {
                    addCan(canDay, canMonth, canYear, name, amount);
                }
                currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date());
                // database inserting...
                //TODO:finish imgUrl
                String imgUrl = "";
                mainDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + name + "', 'friut', '" + amount + "', '" + currentDate + "', '" + selectedDate + "','" + imgUrl + "','" + UserStatus.username + "', '" + UserStatus.groupName + "')");
                uploadInBackgroundService(getApplicationContext());
                // possible UI fresh...

                finish();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // itemDisplay.setMinimumHeight(100);
            File photoFile = new File(imageAbsolutePath);
            Uri uri = Uri.fromFile(photoFile);
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            WindowManager wm = this.getWindowManager();
            int width = wm.getDefaultDisplay().getWidth();
            int height = width;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            int bitmapWidth = photo.getWidth();
            int bitmapHeight = photo.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = (float) (width / bitmapWidth) / 2;
            float scaleHeight = (float) (height / bitmapHeight) / 2;

            matrix.postScale((float)5.0,(float)5.0);
            Bitmap newBitmap = Bitmap.createBitmap(photo, 0, 0, width, height, matrix, true);
            photo.recycle();

            itemDisplay.setImageBitmap(newBitmap);
//            itemDisplay.setImageBitmap(photo);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd:HHmmss").format(new Date());
        String imageFileName = UserStatus.username + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageRelativePath = "file:" + image.getAbsolutePath();
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
                imageUri = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
        }
    }

    private void takePhoto() {
        Intent photoIntent = new Intent();
    }

    /*Author: geotv
    * reference from: https://github.com/gotev/android-upload-service
    * */
    public void uploadInBackgroundService(final Context context) {
        try {
            String uploadId =
                    new MultipartUploadRequest(context, "http://178.62.93.103/SharingFridge/image")
                            .addFileToUpload(imageAbsolutePath, "sample")
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2)
                            .startUpload();
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    public void addCan(int addday, int addmonth, int addyear, String name, String amount) {
        String calId = "";
        Cursor userCursor = getContentResolver().query(Uri.parse(calendarURL), null,
                null, null, null);
        if (userCursor.getCount() > 0) {
            userCursor.moveToFirst();
            calId = userCursor.getString(userCursor.getColumnIndex("_id"));

        }

        String title = "" + name;
        String des = "amount:" + amount;

//        String day_str="";
//        String month_str="";

//        EditText title_text=(EditText)  findViewById(R.id.editText);
//        EditText des_text=(EditText)findViewById(R.id.editText2);
//        EditText day_text=(EditText)findViewById(R.id.editText3);
//        EditText month_text=(EditText)findViewById(R.id.editText4);

//        title = title_text .getText().toString();
//        des = des_text .getText().toString();
//        day_str = day_text.getText().toString();
//        month_str = month_text.getText().toString();

//        int day=Integer.parseInt(day_str);
//        int month=Integer.parseInt(month_str)-1;

//        int day= Integer.parseInt(date.substring(0,1));
//        int month=Integer.parseInt(date.substring(3,4));
        int day = addday;
        int month = addmonth;
        int year = addyear;

        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", des);
        event.put("calendar_id", calId);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.HOUR_OF_DAY, 10);
        mCalendar.set(Calendar.MINUTE, 0);
        long start = mCalendar.getTime().getTime();
        mCalendar.set(Calendar.HOUR_OF_DAY, 11);
        long end = mCalendar.getTime().getTime();

        event.put("dtstart", start);
        event.put("dtend", end);
        event.put("hasAlarm", 1);

        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri newEvent = getContentResolver().insert(Uri.parse(calanderEventURL), event);
        long id = Long.parseLong(newEvent.getLastPathSegment());
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        values.put("minutes", 10);
        getContentResolver().insert(Uri.parse(calanderRemiderURL), values);
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
}
