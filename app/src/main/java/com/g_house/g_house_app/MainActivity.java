package com.g_house.g_house_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            TextView t = findViewById(R.id.centered_text);
            t.append("An Error Had Occurred.\n");
        }
    }

    private void bluetoothTest()
    {
        TextView t = findViewById(R.id.centered_text);
        t.setText("");

        t.append("Getting Bluetooth adapter.\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            t.append("Device doesn't support bluetooth.\n");
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
        TextView t = findViewById(R.id.centered_text);

        t.append("Getting list of paired devices:\n");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            t.append(String.format("%s\t%s\n", device.getName(), device.getAddress()));
        }

        t.append("Done.\n");
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
                tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
            }
            catch(Exception ignored) {}

            mmSocket = tmp;
        }

        @Override
        public void run()
        {
            bluetoothAdapter.cancelDiscovery();
            try
            {
                mmSocket.connect();
            }
            catch(IOException connectException)
            {
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
            OutputStream outputStream;

            try
            {
                inputStream = mmSocket.getInputStream();
                outputStream = mmSocket.getOutputStream();

                byte[] buffer = new byte[1024];

                inputStream.read(buffer);
                outputStream.write(buffer);
            }
            catch(Exception ignored) {}

            cancel();
        }
    }
}
