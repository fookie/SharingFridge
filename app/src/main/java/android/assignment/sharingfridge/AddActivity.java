package android.assignment.sharingfridge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static android.assignment.sharingfridge.R.id.nameEditText;


public class AddActivity extends AppCompatActivity {

    private final int CAMERA_CODE = 330;

    private EditText nameEditText;
    private EditText amountEditText;
    public EditText dateEditText;
    private ImageView itemDisplay;
    private Button cameraButton;
    private Button calendarButton;
    private Button addButton;

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
        calendarButton = (Button) findViewById(R.id.calendarButton);
        addButton = (Button) findViewById(R.id.addButton);



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
                                String selectedDate = new SimpleDateFormat("dd-MM-yyyy").format(calender.getTime());
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
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CODE);
            }
        });



        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = amountEditText.getText().toString();
                name = nameEditText.getText().toString();
                addCan(canDay, canMonth, canYear, name, amount);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // database inserting...
                // possible UI fresh...
                finish();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // itemDisplay.setMinimumHeight(100);

            WindowManager wm = this.getWindowManager();
            int width = wm.getDefaultDisplay().getWidth();
            int height = width;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            int bitmapWidth = photo.getWidth();
            int bitmapHeight = photo.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = (float) (width/bitmapWidth)/2;
            float scaleHeight = (float) (height/bitmapHeight)/2;

            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newBitmap = Bitmap.createBitmap(photo, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            photo.recycle();

            itemDisplay.setImageBitmap(newBitmap);
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

        String title = ""+ name;
        String des = "amount:"+ amount;

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
        Toast.makeText(AddActivity.this, "success!!!", Toast.LENGTH_LONG).show();
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
