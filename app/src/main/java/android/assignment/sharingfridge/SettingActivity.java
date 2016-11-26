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

        english = (RadioButton)findViewById(R.id.language_english);
        chinese = (RadioButton)findViewById(R.id.language_chinese);
        systemDefault = (RadioButton)findViewById(R.id.language_default);
        cleancache=(Button)findViewById(R.id.clean_cache_button);
        cleandb=(Button)findViewById(R.id.clean_db_button);
        RadioGroup language = (RadioGroup)findViewById(R.id.radioGroup);

        int i=0;
        SharedPreferences userSettings= getSharedPreferences("setting", 0);
        int ID = userSettings.getInt("language",i);
        if(ID==1)
            english.setChecked(true);
        else if(ID==2)
            chinese.setChecked(true);
        else
            systemDefault.setChecked(true);
        setTitle(getString(R.string.settings));

        language.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                ChangeLanguage(radioButtonId);
                finish();
                Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        cleancache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void,Void,Void>(){
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
        cleandb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase taskDB;
                taskDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
                taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
                String sql="delete from items";
                taskDB.execSQL(sql);
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                taskDB.close();
            }
        });
    }

    protected void ChangeLanguage(int i) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if(i==english.getId()){
            config.locale = Locale.ENGLISH;
            english.setChecked(true);
            setSharedPreference(1);
        }
        else if(i==chinese.getId()) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            chinese.setChecked(true);
            setSharedPreference(2);
        }
        else
                config.locale = Locale.getDefault();

        resources.updateConfiguration(config, dm);
    }

    public void setSharedPreference(int i) {
        SharedPreferences languageSettings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = languageSettings.edit();
        editor.putInt("language", i);
        editor.commit();
    }



}
