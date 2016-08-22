package edu.fordham.cis.wisdm.sleepwatch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

/**
 * A simple XYPlot
 */
public class PlotDisplayActivity extends Activity {

    private static final String TAG = "PlotDisplayActivity";
    ArrayList<Long> timeStamps = new ArrayList<>();
    ArrayList<Double> sensorDataX = new ArrayList<>();
    ArrayList<Double> sensorDataY = new ArrayList<>();
    ArrayList<Double> sensorDataZ = new ArrayList<>();
    private XYPlot plot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        StringBuffer sb = new StringBuffer();

        try {
            String fileName = getIntent().getStringExtra("FILE");
            String[] info = fileName.split("_");

            sb.append((info[0].equals("watch")) ? "Watch " : "Phone ");
            sb.append("data from ");
            sb.append((info[1].equals("accel")) ? "accelerometer - " : "gyroscope - ");

            Date date = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(info[2]);
            String formattedDate = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(date);
            sb.append(formattedDate);

            ((TextView) findViewById(R.id.plot_info)).setText(sb.toString());

            Scanner in = new Scanner(openFileInput(fileName));
            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(",");

                timeStamps.add(Long.valueOf(line[0]));
                sensorDataX.add(Double.valueOf(line[1]));
                sensorDataY.add(Double.valueOf(line[2]));
                sensorDataZ.add(Double.valueOf(line[3]));
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = new Number[sensorDataX.size()]; sensorDataX.toArray(series1Numbers);
        Number[] series2Numbers = new Number[sensorDataY.size()]; sensorDataY.toArray(series2Numbers);
        Number[] series3Numbers = new Number[sensorDataZ.size()]; sensorDataZ.toArray(series3Numbers);

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "X");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Y");

        XYSeries series3 = new SimpleXYSeries(Arrays.asList(series3Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Z");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(null);
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(null);
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels_2);

        LineAndPointFormatter series3Format = new LineAndPointFormatter();
        series3Format.setPointLabelFormatter(null);
        series3Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels_3);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);
        plot.addSeries(series3, series3Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);

    }
}
