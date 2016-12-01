package android.assignment.sharingfridge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Locale;

/**
 * The activity class for settings in this application.
 */
public class SettingActivity extends AppCompatActivity {

    RadioButton english;
    RadioButton chinese;
    RadioButton systemDefault;
    Button cleancache;
    Button cleandb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        english = (RadioButton) findViewById(R.id.language_english);
        chinese = (RadioButton) findViewById(R.id.language_chinese);
        systemDefault = (RadioButton) findViewById(R.id.language_default);
        cleancache = (Button) findViewById(R.id.clean_cache_button);
        cleandb = (Button) findViewById(R.id.clean_db_button);
        RadioGroup language = (RadioGroup) findViewById(R.id.radioGroup);

        //check language user selected and stored
        SharedPreferences userSettings = getSharedPreferences("setting", 0);
        int ID = userSettings.getInt("language", 0);
        if (ID == 1)
            english.setChecked(true);
        else if (ID == 2)
            chinese.setChecked(true);
        else
            systemDefault.setChecked(true);
        setTitle(getString(R.string.settings));

        language.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                ChangeLanguage(radioButtonId);
                //finish current activity and restart the home activity
                finish();
                Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //clean cache
        cleancache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Glide.get(getApplicationContext()).clearDiskCache();
                        return null;
                    }
                }.execute();
                Glide.get(getApplicationContext()).clearMemory();
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
            }
        });

        //clean local database
        cleandb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase taskDB;
                taskDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
                taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
                String sql = "delete from items";
                taskDB.execSQL(sql);
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                taskDB.close();
            }
        });
    }

    /***
     * change application language
     *
     * @param i language ID
     */
    protected void ChangeLanguage(int i) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //change language to english
        if (i == english.getId()) {
            config.locale = Locale.ENGLISH;
            english.setChecked(true);
            setSharedPreference(1);
        }
        //change language to chinese
        else if (i == chinese.getId()) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            chinese.setChecked(true);
            setSharedPreference(2);
        }
        //use system default language
        else
            config.locale = Locale.getDefault();

        resources.updateConfiguration(config, dm);
    }

    // store the user's setting in shared preference
    public void setSharedPreference(int i) {
        SharedPreferences languageSettings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = languageSettings.edit();
        editor.putInt("language", i);
        editor.commit();
    }


}
