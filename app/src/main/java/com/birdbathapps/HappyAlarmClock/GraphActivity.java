package com.birdbathapps.HappyAlarmClock;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class GraphActivity extends AppCompatActivity {

    private List<File> fileList;
    private LineChart lineChartView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle(R.string.title_graph_activity);
        initListOfFiles();
        initLineChart();
    }

    private void initListOfFiles() {
        File dir = this.getFilesDir();
        File[] files;
        try {
            files = dir.listFiles();
            fileList = Arrays.asList(files);
        } catch (Exception e) {
            Timber.e(e,"Exception getting files from directory");
        }
    }

    private void initLineChart() {
        lineChartView = findViewById(R.id.line_chart_time);

        // Set x axis labels and chart data
        String[] xLabels = new String[fileList.size()];
        List<Entry> wakeUpTimes = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            String photoFileName = fileList.get(i).getName();
            String[] parts = photoFileName.split("_");
            String[] dateParts = parts[1].split("");
            String[] timeParts = parts[2].split("");
            xLabels[i] = String.format("%s%s/%s%s/%s%s", dateParts[7], dateParts[8], dateParts[5],
                    dateParts[6], dateParts[3], dateParts[4]);
            wakeUpTimes.add(new Entry((float) i, Float.parseFloat(String.format("%s%s%s%s",
                    timeParts[1], timeParts[2], timeParts[3], timeParts[4]))));
        }

        LineDataSet lineDataSet = initLineDataSet(wakeUpTimes);
        LineData data = initLineData(lineDataSet);
        setupLineChart(data);
        formatXAxis(xLabels);
        formatYAxis();
    }

    private LineDataSet initLineDataSet(List<Entry> wakeUpTimes) {
        LineDataSet lineDataSet = new LineDataSet(wakeUpTimes, getString(R.string.dataset_label_graph_activity));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        ValueFormatter plotValueFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return to24HourTimeFormat(value);
            }
        };
        lineDataSet.setValueFormatter(plotValueFormatter);
        return lineDataSet;
    }

    private LineData initLineData(LineDataSet lineDataSet) {
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        lineDataSets.add(lineDataSet);
        return new LineData(lineDataSets);
    }

    private void setupLineChart(LineData data) {
        lineChartView.getAxisLeft().setAxisMinimum(0000f);
        lineChartView.getAxisLeft().setAxisMaximum(2359f);
        lineChartView.getAxisRight().setEnabled(false);
        lineChartView.getDescription().setEnabled(false);
        lineChartView.setData(data);
        lineChartView.invalidate(); // refresh
    }

    private void formatXAxis(String[] xLabels) {
        IndexAxisValueFormatter xAxisFormatter = new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels[(int) value];
            }
        };
        XAxis xAxis = lineChartView.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(xAxisFormatter);
    }

    private void formatYAxis() {
        IndexAxisValueFormatter yAxisFormatter = new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return to24HourTimeFormat(value);
            }
        };
        lineChartView.getAxisLeft().setValueFormatter(yAxisFormatter);
    }

    private String to24HourTimeFormat(float value) {
        StringBuilder strValue = new StringBuilder(String.valueOf(Math.round(value)));
        while (strValue.length() < 4) {
            strValue.insert(0, "0");
        }
        String[] strValueParts = strValue.toString().split("");
        return strValueParts[1] + strValueParts[2] + ":" + strValueParts[3] + strValueParts[4];
    }
}
