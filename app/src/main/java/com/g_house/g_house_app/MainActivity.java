package com.g_house.g_house_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bluetoothTest();
            }
        });

        bluetoothTest();

        //try
        //{
        //    OutputStreamWriter osw = new OutputStreamWriter((getApplicationContext().openFileOutput("test.txt", Context.MODE_PRIVATE)));
        //    osw.write("Test 123\n456 789");
        //    osw.close();

        //    char[] buffer = new char[100];
        //    InputStreamReader isw = new InputStreamReader(getApplicationContext().openFileInput("test.txt"));
        //    isw.read(buffer, 0, 100);
        //    TextView t = findViewById(R.id.centered_text);
        //    t.setText(buffer, 0, 100);
        //    isw.close();
        //}
        //catch(Exception ignore) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
        {
            listBtDevices();
        }
        else
        {
            //TextView t = findViewById(R.id.centered_text);
            //t.append("An Error Had Occurred.\n");
        }
    }

    private void bluetoothTest()
    {
        //TextView t = findViewById(R.id.centered_text);
        //t.setText("");

        //t.append("Getting Bluetooth adapter.\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            //t.append("Device doesn't support bluetooth.\n");
            return;
        }

        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            listBtDevices();
        }
    }

    private void listBtDevices()
    {
        //TextView t = findViewById(R.id.centered_text);

        BluetoothDevice piDevice = null;

        //t.append("Getting list of paired devices:\n");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if(device.getName().equals("raspberrypi"))
            {
                piDevice = device;
            }
            //t.append(String.format("%s\t%s\n", device.getName(), device.getAddress()));
        }

        if(piDevice != null)
        {
            //t.append("Raspberry Pi found.\n");

            ConnectThread thread = new ConnectThread(piDevice);
            try
            {
                thread.start();
                thread.join();
            }
            catch(Exception ignored) {}
        }

        //t.append("Done.\n");
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try
            {
                //Using well-known SPP UUID
                tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            }
            catch(Exception ignored) {}

            mmSocket = tmp;
        }

        @Override
        public void run()
        {
            //TextView t = findViewById(R.id.centered_text);
            bluetoothAdapter.cancelDiscovery();
            try
            {
                //t.append("Attempting to connect to Pi.\n");

                mmSocket.connect();
            }
            catch(IOException connectException)
            {
                //t.append("Failed to connect to Pi: ");
                //t.append(String.format("%s\n", connectException.toString()));
                try
                {
                    mmSocket.close();
                }
                catch (IOException ignored) { }

                return;
            }

            // Do whatever needs to be done with mmSocket
            manageSocket();
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException ignored) { }
        }

        public void manageSocket()
        {
            InputStream inputStream;

            try
            {
                inputStream = mmSocket.getInputStream();
                byte[] buffer = new byte[1024];

                inputStream.read(buffer, 0, 4);
                int size = ByteBuffer.wrap(buffer, 0, 4).getInt();
                int read;

                File f = File.createTempFile("data", null);
                FileOutputStream ofstream = new FileOutputStream(f);
                while((read = inputStream.read(buffer)) > 0)
                {
                    ofstream.write(buffer, 0, read);
                }
                ofstream.close();

                TableLayout tl = findViewById(R.id.dataTable);
                tl.removeAllViews(); //clear the table

                String[] headings = {"Time", "pH", "EC", "Temperature"};
                addTableRow(tl, headings);

                String line;
                BufferedReader reader = new BufferedReader(new FileReader(f));
                while((line = reader.readLine()) != null)
                {
                    addTableRow(tl, line.split(","));
                }

                f.deleteOnExit();
            }
            catch(Exception ignored) {}

            cancel();
        }

        private void addTableRow(TableLayout tl, String[] data)
        {
            TableRow row = new TableRow(MainActivity.this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            for(String tok : data)
            {
                TextView col = new TextView(MainActivity.this);
                col.setText(tok);
                row.addView(col);
            }

            tl.addView(row);
        }
    }
}
