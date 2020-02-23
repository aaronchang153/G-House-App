package com.g_house.g_house_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ParameterFragment extends AbstractBluetoothFragment {
    View view;

    public ParameterFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_parameter, container, false);

        final Button updateB = view.findViewById(R.id.updateButton);
        updateB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                updateCurrentInfo();
            }
        });

        final Button selectB = view.findViewById(R.id.selectButton);
        selectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelectDevice();
            }
        });

        final Button phLo_down = view.findViewById(R.id.phLo_down);
        phLo_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.phLo_value);
                float val = Float.parseFloat(tv.getText().toString());
                val -= 0.1;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button phLo_up = view.findViewById(R.id.phLo_up);
        phLo_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.phLo_value);
                float val = Float.parseFloat(tv.getText().toString());
                val += 0.1;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button phHi_down = view.findViewById(R.id.phHi_down);
        phHi_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.phHi_value);
                float val = Float.parseFloat(tv.getText().toString());
                val -= 0.1;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button phHi_up = view.findViewById(R.id.phHi_up);
        phHi_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.phHi_value);
                float val = Float.parseFloat(tv.getText().toString());
                val += 0.1;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button ecLo_down = view.findViewById(R.id.ecLo_down);
        ecLo_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.ecLo_value);
                float val = Float.parseFloat(tv.getText().toString());
                val -= 10.0;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button ecLo_up = view.findViewById(R.id.ecLo_up);
        ecLo_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = view.findViewById(R.id.ecLo_value);
                float val = Float.parseFloat(tv.getText().toString());
                val += 10.0;
                tv.setText(String.format("%.2f", val));
            }
        });

        final Button send_button = view.findViewById(R.id.send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNewParams();
            }
        });

        TableLayout tl = view.findViewById(R.id.tableCurrentParam);
        tl.removeAllViews();
        String[] row = {"pH Low", "pH High", "EC Low"};
        addTableRow(tl, row);
        row[0] = "0.0";
        row[1] = "0.0";
        row[2] = "0.0";
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
            col.setText(tok);
            row.addView(col);
        }

        tl.addView(row);
    }

    private void updateCurrentInfo()
    {
        btConnect();
        final String[] params = getParameters();
        btClose();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout tl = view.findViewById(R.id.tableCurrentParam);
                tl.removeAllViews();
                String[] headings = {"pH Low", "pH High", "EC Low"};
                addTableRow(tl, headings);
                addTableRow(tl, params);

                TextView textView = view.findViewById(R.id.phLo_value);
                textView.setText(params[0]);

                textView = view.findViewById(R.id.phHi_value);
                textView.setText(params[1]);

                textView = view.findViewById(R.id.ecLo_value);
                textView.setText(params[2]);
            }
        });
    }

    private void sendNewParams()
    {
        float[] params = new float[3];
        params[0] = Float.parseFloat(((TextView)view.findViewById(R.id.phLo_value)).getText().toString());
        params[1] = Float.parseFloat(((TextView)view.findViewById(R.id.phHi_value)).getText().toString());
        params[2] = Float.parseFloat(((TextView)view.findViewById(R.id.ecLo_value)).getText().toString());
        btConnect();
        sendParameters(params);
        btClose();
    }
}
