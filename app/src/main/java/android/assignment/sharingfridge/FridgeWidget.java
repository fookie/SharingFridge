package android.assignment.sharingfridge;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality that will create a widget on the dashboard.
 */
public class FridgeWidget extends AppWidgetProvider {
    private static SQLiteDatabase taskDB;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String widgetTitle = "Sharing Fridge";
        //select attributes from database
        String sql = "select count(*) from items";
        Cursor c = taskDB.rawQuery(sql, null);
        String todisplay = null;
        //display the total number of items user owned
        while (c.moveToNext()) {
            int count = c.getInt(0);
            todisplay = "Total: " + count + " items";
        }

        //get the item nearest to the expire date and display to the user
        Cursor cursor = taskDB.rawQuery("SELECT * from items", null);
        String expday = "Unknown";
        long i = 100;
        String itemName = null;
        String itemQuantity = null;
        String widgetQuantity = null;
        while (cursor.moveToNext()) {
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {

                Date nd = cal.getTime();
                Date ed = df.parse(cursor.getString(cursor.getColumnIndex("expiretime")));
                long days = (ed.getTime() - nd.getTime()) / (1000 * 60 * 60 * 24);
                if (days < i) {
                    i = days;
                    itemName = cursor.getString(cursor.getColumnIndex("item"));
                    itemQuantity = cursor.getString(cursor.getColumnIndex("amount"));
                }
                expday = i + 1 + ((i + 1 <= 1) ? "day left" : "days left");//+1 ensure expire today shows 0 days
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        widgetQuantity = "Still " + itemQuantity + " left!";
        CharSequence widgetText = "Remind " + itemName + ": " + expday;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fridge_widget);
        views.setTextViewText(R.id.appwidget_title, widgetTitle);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.appwidget_quantity, widgetQuantity);
        views.setTextViewText(R.id.appwidget_text_detail, todisplay);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        if(taskDB==null){
            taskDB = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
            taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        }
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        taskDB = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (taskDB != null) {
            taskDB.close();
        }
    }
}

