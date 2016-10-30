package android.assignment.sharingfridge;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AddActivity extends AppCompatActivity {

    private final int CAMERA_CODE = 330;

    private EditText amountEditText;
    public EditText dateEditText;
    private ImageView itemDisplay;
    private Button cameraButton;
    private Button addButton;

    int currentYear, currentMonth, currentDay;
    private Calendar calender = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        amountEditText = (EditText) findViewById(R.id.amountEditText);
        dateEditText = (EditText) findViewById(R.id.dateEditText);
        itemDisplay = (ImageView)  findViewById(R.id.addItemImageView);
        cameraButton = (Button) findViewById(R.id.cameraButton);
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
                                String selectedDate = new SimpleDateFormat("MM/dd/yyyy").format(calender.getTime());
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
            itemDisplay.setMinimumHeight(100);
            itemDisplay.setImageBitmap(photo);
        }
    }


}
