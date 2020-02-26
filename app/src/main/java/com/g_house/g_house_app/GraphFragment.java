package com.g_house.g_house_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GraphFragment extends AbstractBluetoothFragment {
    View view;
    TextView log;

    GraphView ph_graph;
    GraphView ec_graph;
    GraphView temp_graph;
    GraphView co2_graph;

    public GraphFragment()
    {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_table, container, false);

        log = inflater.inflate(R.layout.fragment_log, container, false).findViewById(R.id.log_text);
        //log = view.findViewById(R.id.debug);
        log.append("Debug log start\n");

        final Button updateB = view.findViewById(R.id.updateButton);
        updateB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //updateTable();
                updateGraphs();
                updateSensorData();
            }
        });

        final Button selectB = view.findViewById(R.id.selectButton);
        selectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelectDevice();
            }
        });

        ph_graph = view.findViewById(R.id.graph_ph);
        ph_graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        ec_graph = view.findViewById(R.id.graph_ec);
        ec_graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        temp_graph = view.findViewById(R.id.graph_temp);
        temp_graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        co2_graph = view.findViewById(R.id.graph_co2);
        co2_graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        TableLayout tl = view.findViewById(R.id.currentValueTable);
        tl.removeAllViews();
        String[] row = {"pH", "EC", "Temp", "CO2"};
        addTableRow(tl, row);
        row[0] = "0.00";
        row[1] = "0.00";
        row[2] = "0.00";
        row[3] = "0.00";
        addTableRow(tl, row);

        return view;
    }

    private void addTableRow(TableLayout tl, String[] data)
    {
        TableRow row = new TableRow(getActivity());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        for(String tok : data)
        {
            TextView col = new TextView(getActivity());
            col.setText(tok.substring(0, Math.min(tok.length(), 5))); //TODO: Fix table formatting in a better way
            row.addView(col);
        }

        tl.addView(row);
    }

    private void updateTable()
    {
        btConnect();
        final File f = getDataFile();
        btClose();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout tl = view.findViewById(R.id.dataTable);
                tl.removeAllViews(); //clear the table

                String[] headings = {"Time", "pH", "EC", "Temp", "CO2"};
                addTableRow(tl, headings);

                try
                {
                    String line;
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    while((line = reader.readLine()) != null)
                    {
                        addTableRow(tl, line.split(","));
                        //log.append(line + "\n");
                    }

                    f.deleteOnExit();
                }
                catch (Exception ignored) { }
            }
        });
    }

    private void updateGraphs()
    {
        btConnect();
        final File f = getDataFile();
        btClose();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ph_graph.removeAllSeries();
                ec_graph.removeAllSeries();
                temp_graph.removeAllSeries();
                co2_graph.removeAllSeries();

                LineGraphSeries<DataPoint> ph_data_series = new LineGraphSeries<>();
                LineGraphSeries<DataPoint> ec_data_series = new LineGraphSeries<>();
                LineGraphSeries<DataPoint> temp_data_series = new LineGraphSeries<>();
                LineGraphSeries<DataPoint> co2_data_series = new LineGraphSeries<>();

                try
                {
                    String line;
                    String[] tok;
                    double time;
                    int current_epoch = (int) (System.currentTimeMillis() / 1000);
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    while((line = reader.readLine()) != null)
                    {
                        tok = line.split(",");
                        time = (double) (Integer.parseInt(tok[0]) - current_epoch);
                        ph_data_series.appendData(new DataPoint(time, Double.parseDouble(tok[1])), false, 3600);
                        ec_data_series.appendData(new DataPoint(time, Double.parseDouble(tok[2])), false, 3600);
                        temp_data_series.appendData(new DataPoint(time, Double.parseDouble(tok[3])), false, 3600);
                        co2_data_series.appendData(new DataPoint(time, Double.parseDouble(tok[4])), false, 3600);
                    }

                    ph_graph.addSeries(ph_data_series);
                    ec_graph.addSeries(ec_data_series);
                    temp_graph.addSeries(temp_data_series);
                    co2_graph.addSeries(co2_data_series);

                    f.deleteOnExit();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateSensorData()
    {
        btConnect();
        final String[] data = getSensorData();
        btClose();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout tl = view.findViewById(R.id.currentValueTable);
                tl.removeAllViews(); //clear the table

                String[] row = {"pH", "EC", "Temp", "CO2"};
                addTableRow(tl, row);
                addTableRow(tl, data);
            }
        });
    }
}
