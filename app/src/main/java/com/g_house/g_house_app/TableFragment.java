package com.g_house.g_house_app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class TableFragment extends Fragment {
    View view;

    TextView log;

    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_ENABLE_BT = 1;
    BluetoothDevice piDevice;

    int deviceSelection;

    public TableFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_table, container, false);
        piDevice = null;

        log = inflater.inflate(R.layout.fragment_log, container, false).findViewById(R.id.log_text);
        //log = view.findViewById(R.id.debug);
        log.append("Debug log start\n");

        final Button refreshB = view.findViewById(R.id.refreshButton);
        refreshB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(piDevice != null)
                {
                    ConnectThread thread = new ConnectThread(piDevice);
                    try
                    {
                        thread.start();
                        thread.join();
                    }
                    catch(Exception ignored) {}
                }
            }
        });

        final Button connectB = view.findViewById(R.id.connectButton);
        connectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btConnect();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
        {
            listBtDevices();
        }
        else
        {
            log.append("An Error Had Occurred.\n");
        }
    }

    private void btConnect()
    {
        /* Some of this stuff might be able to be moved elsewhere so it's only executed once */
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            log.append("Device doesn't support bluetooth.\n");
            return;
        }

        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            //Get list of bt device names as String array
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice[] devices = new BluetoothDevice[pairedDevices.size()];
            pairedDevices.toArray(devices);
            String[] names = new String[devices.length];
            for(int i = 0; i < devices.length; i++)
            {
                names[i] = devices[i].getName();
            }
            selectDevice(names);

            piDevice = devices[deviceSelection];

            if(piDevice != null)
            {
                log.append("Raspberry Pi found.\n");

                ConnectThread thread = new ConnectThread(piDevice);
                try
                {
                    thread.start();
                    thread.join();
                }
                catch(Exception ignored) {}
            }
        }
    }

    private void selectDevice(String[] list)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a bluetooth device");
        builder.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                deviceSelection = which;
            }
        });
        builder.show();
    }

    private void bluetoothTest()
    {
        log.append("Getting Bluetooth adapter.\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            log.append("Device doesn't support bluetooth.\n");
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
        BluetoothDevice piDevice = null;

        log.append("Getting list of paired devices:\n");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if(device.getName().equals("raspberrypi"))
            {
                piDevice = device;
            }
            log.append(String.format("%s\t%s\n", device.getName(), device.getAddress()));
        }

        if(piDevice != null)
        {
            log.append("Raspberry Pi found.\n");

            ConnectThread thread = new ConnectThread(piDevice);
            try
            {
                thread.start();
                thread.join();
            }
            catch(Exception ignored) {}
        }

        log.append("Done.\n");
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
            bluetoothAdapter.cancelDiscovery();
            try
            {
                log.append("Attempting to connect to Pi.\n");

                mmSocket.connect();
            }
            catch(IOException connectException)
            {
                log.append("Failed to connect to Pi: ");
                log.append(String.format("%s\n", connectException.toString()));
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

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }

        public void manageSocket()
        {
            InputStream inputStream;
            OutputStream outputStream;

            try
            {
                log.append("Getting socket input stream\n");
                inputStream = mmSocket.getInputStream();
                outputStream = mmSocket.getOutputStream();
                byte[] buffer = new byte[1024];

                buffer[0] = 0;
                outputStream.write(buffer, 0, 1);

                log.append("Getting data file size\n");
                inputStream.read(buffer, 0, 4);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int size = byteBuffer.getInt();
                int read, remaining;
                remaining = size;

                log.append("Receiving data file\n");
                final File f = File.createTempFile("data", null);
                FileOutputStream ofstream = new FileOutputStream(f);
                while(remaining > 0)
                {
                    read = inputStream.read(buffer);
                    ofstream.write(buffer, 0, read);
                    remaining -= read;
                }
                ofstream.close();

                outputStream.write(buffer, 0, 1);

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
            catch(Exception e)
            {
                e.printStackTrace();
            }

            log.append("Done.\n");
            cancel();
        }
    }
}
