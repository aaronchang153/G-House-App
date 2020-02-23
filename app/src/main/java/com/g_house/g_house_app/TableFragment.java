package com.g_house.g_house_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TableFragment extends AbstractBluetoothFragment {
    View view;
    TextView log;

    public TableFragment()
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
                updateTable();
            }
        });

        final Button selectB = view.findViewById(R.id.selectButton);
        selectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelectDevice();
            }
        });

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

                String[] headings = {"Time", "pH", "EC", "Temperature", "CO2"};
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
}
