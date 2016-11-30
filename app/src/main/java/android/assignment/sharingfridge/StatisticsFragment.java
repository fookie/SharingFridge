package android.assignment.sharingfridge;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;


/**
 * The fourth fragments showing Statistics of fridge items, e.g. how many items are going to expire
 * in the next 7 days. What portion of items belong to a group member.
 * Used external library: HelloCharts.
 * Copyright 2014 Leszek Wach.
 * @see <a href="https://github.com/lecho/hellocharts-android">HelloCharts</a>
 *
 */
public class StatisticsFragment extends Fragment {

    public SQLiteDatabase mainDB;
    private String formattedDate, dayOfWeek;

    private LineChartView lineChart;
    private LineChartData lineData;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 7;

    int[][] frigeDataSet = new int[maxNumberOfLines][numberOfPoints];
    float maximumDayTotalSpending = 100f; // default

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = true;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor = false;


    private PieChartView pieChart;
    private PieChartData pieData;
    private boolean hasLabelsOutside = false;
    private boolean hasCenterCircle = true;
    private boolean hasCenterText1 = true;
    private boolean hasCenterText2 = true;
    private boolean isExploded = false;

    private OnFragmentInteractionListener mListener;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date nowDate = new Date(System.currentTimeMillis());
        formattedDate = formatter.format(nowDate);

        SimpleDateFormat formatter2 = new SimpleDateFormat("EEE");
        Date nowDate2 = new Date(System.currentTimeMillis());
        dayOfWeek = formatter2.format(nowDate2);

        lineChart = (LineChartView) view.findViewById(R.id.lineChart);
        lineChart.setOnValueTouchListener(new ValueTouchListener());
        getDataForLineChart();
        lineChart.setViewportCalculationEnabled(true); // Y Axis Unit!
//        resetViewport();

        pieChart = (PieChartView) view.findViewById(R.id.pieChart);
        pieChart.setOnValueTouchListener(new PieValueTouchListener());
        getDataForPieChart();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void getDataForLineChart(){
        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        int dayIndex = 0;
        int weekLength = dayNumOfWeek(dayOfWeek);
        while(dayIndex<=6){
            String dayToCheck = getDateString(formattedDate, dayIndex);
            Cursor cursor = mainDB.rawQuery("SELECT * FROM items WHERE groupname = '" + UserStatus.groupName + "' AND expiretime = '" + dayToCheck + "'", null);
            int count = cursor.getCount();
            while(cursor.moveToNext()){
                int dayTotal = cursor.getInt(0);// index 0 stands for sum(amount)
                frigeDataSet[0][dayIndex] = count;
            }
            Log.i("=linedata=","dayToCheck: " + dayToCheck + " GroupName: " + UserStatus.groupName +  " num: " + frigeDataSet[0][dayIndex]);
            cursor.close();
            dayIndex++;
        }

        mainDB.close();

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, frigeDataSet[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor){
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        lineData = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                // axisX.setName("Week");
                List<AxisValue> unit = new ArrayList<AxisValue>();

                AxisValue today = new AxisValue(0);
                unit.add(today.setLabel(getString(R.string.today)));
                AxisValue tomorrow = new AxisValue(1);
                unit.add(tomorrow.setLabel(getString(R.string.tomorrow)));
                AxisValue dayAfterTomw = new AxisValue(2);
                unit.add(dayAfterTomw.setLabel("2"+getString(R.string.day)));
                AxisValue threeDay = new AxisValue(3);
                unit.add(threeDay.setLabel("3 "+getString(R.string.day)));
                AxisValue fourDay = new AxisValue(4);
                unit.add(fourDay.setLabel("4 "+getString(R.string.day)));
                AxisValue fiveDay = new AxisValue(5);
                unit.add(fiveDay.setLabel("5"+getString(R.string.day)));
                AxisValue sixDay = new AxisValue(6);
                unit.add(sixDay.setLabel("6 "+getString(R.string.day)));

                axisX.setValues(unit);

                axisY.setName(getString(R.string.quantities));
            }
            lineData.setAxisXBottom(axisX);
            lineData.setAxisYLeft(axisY);
        } else {
            lineData.setAxisXBottom(null);
            lineData.setAxisYLeft(null);
        }

        lineData.setBaseValue(Float.NEGATIVE_INFINITY);
        lineChart.setLineChartData(lineData);

    }

    public void getDataForPieChart(){
        List<SliceValue> values = new ArrayList<SliceValue>();
        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        Cursor defaultCursor = mainDB.rawQuery("SELECT SUM(amount), owner FROM items where groupname = '" + UserStatus.groupName + "' GROUP BY owner", null);
        while(defaultCursor.moveToNext()){
            float eachCategoryTotal = defaultCursor.getFloat(0);
            String categoryName = defaultCursor.getString(1);
            SliceValue partValue = new SliceValue(eachCategoryTotal, ChartUtils.pickColor());
            values.add(partValue.setLabel(categoryName));
        }
        defaultCursor.close();
        mainDB.close();

        pieData = new PieChartData(values);
        pieData.setHasLabels(hasLabels);
        pieData.setHasLabelsOnlyForSelected(hasLabelForSelected);
        pieData.setHasLabelsOutside(hasLabelsOutside);
        pieData.setHasCenterCircle(hasCenterCircle);

        if (isExploded) {
            pieData.setSlicesSpacing(24);
        }

        if (hasCenterText1) {
            pieData.setCenterText1(getString(R.string.insights));
            pieData.setCenterText1FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                    (int) getResources().getDimension(R.dimen.pie_chart_text1_size)));
        }

        if (hasCenterText2) {
            pieData.setCenterText2(formattedDate);
            pieData.setCenterText2FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                    (int) getResources().getDimension(R.dimen.pie_chart_text2_size)));
        }

        pieChart.setPieChartData(pieData);
    }

    public void updateUI(){
        getDataForLineChart();
        getDataForPieChart();
    }

    private int dayNumOfWeek(String dayOfweek){
        if(dayOfweek.equalsIgnoreCase("SUN")||dayOfweek.equalsIgnoreCase("星期日")){
            return 0;
        } else if(dayOfweek.equalsIgnoreCase("MON")||dayOfweek.equalsIgnoreCase("星期一")) {
            return 1;
        } else if(dayOfweek.equalsIgnoreCase("TUE")||dayOfweek.equalsIgnoreCase("星期二")) {
            return 2;
        } else if(dayOfweek.equalsIgnoreCase("WED")||dayOfweek.equalsIgnoreCase("星期三")) {
            return 3;
        } else if(dayOfweek.equalsIgnoreCase("THU")||dayOfweek.equalsIgnoreCase("星期四")) {
            return 4;
        } else if(dayOfweek.equalsIgnoreCase("FRI")||dayOfweek.equalsIgnoreCase("星期五")) {
            return 5;
        } else if(dayOfweek.equalsIgnoreCase("SAT")||dayOfweek.equalsIgnoreCase("星期六")) {
            return 6;
        }

        return 0;
    }

    public static String getDateString(String today,int dayAddNum) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date nowDate = null;
        try {
            nowDate = formatter.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date newDate2 = new Date(nowDate.getTime() + dayAddNum * 24 * 60 * 60 * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dateOk = simpleDateFormat.format(newDate2);
        return dateOk;
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }
    }

    private class PieValueTouchListener implements PieChartOnValueSelectListener {

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
