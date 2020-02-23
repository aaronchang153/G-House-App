package com.g_house.g_house_app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;


public abstract class AbstractBluetoothFragment extends Fragment {
    static BluetoothAdapter bluetoothAdapter;
    static BluetoothDevice piDevice;
    static final int REQUEST_ENABLE_BT = 1;

    static final byte CMD_GET_LOG      = 0;
    static final byte CMD_GET_PARAM    = 1;
    static final byte CMD_UPDATE_PARAM = 2;

    BluetoothSocket mmSocket;
    BluetoothDevice[] devices;

    protected int deviceSelection;

    public AbstractBluetoothFragment()
    {
        if(bluetoothAdapter == null)
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

    }

    public void btConnect()
    {
        if(piDevice != null)
        {
            try
            {
                //Using well-known SPP UUID
                mmSocket = piDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothAdapter.cancelDiscovery();
                mmSocket.connect();
            }
            catch(Exception ignored) {}
        }
    }

    public void btClose()
    {
        try
        {
            mmSocket.close();
        }
        catch (Exception ignored) { }
    }

    public void btSelectDevice()
    {
        //Get list of bt device names as String array
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        devices = new BluetoothDevice[pairedDevices.size()];
        pairedDevices.toArray(devices);
        String[] names = new String[devices.length];
        for(int i = 0; i < devices.length; i++)
        {
            names[i] = devices[i].getName();
        }
        selectDevice(names);
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

                piDevice = devices[deviceSelection];
            }
        });
        builder.show();
    }

    public File getDataFile()
    {
        File f = null;

        if(piDevice != null)
        {
            try
            {
                InputStream inputStream = mmSocket.getInputStream();
                OutputStream outputStream = mmSocket.getOutputStream();
                byte[] buffer = new byte[1024];

                buffer[0] = CMD_GET_LOG;
                outputStream.write(buffer, 0, 1);

                inputStream.read(buffer, 0, 4);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int size = byteBuffer.getInt();
                int read, remaining;
                remaining = size;

                f = File.createTempFile("data", null);
                FileOutputStream ofstream = new FileOutputStream(f);
                while(remaining > 0)
                {
                    read = inputStream.read(buffer);
                    ofstream.write(buffer, 0, read);
                    remaining -= read;
                }
                ofstream.close();
                outputStream.write(buffer, 0, 1);

                inputStream.close();
                outputStream.close();
            }
            catch (Exception ignored) { }
        }

        return f;
    }

    public String[] getParameters()
    {
        String[] params = null;
        if(piDevice != null)
        {
            try
            {
                InputStream inputStream = mmSocket.getInputStream();
                OutputStream outputStream = mmSocket.getOutputStream();
                byte[] buffer = new byte[1024];

                buffer[0] = CMD_GET_PARAM;
                outputStream.write(buffer, 0, 1);

                inputStream.read(buffer, 0, 4);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int size = byteBuffer.getInt();

                inputStream.read(buffer, 0, size);
                byteBuffer = ByteBuffer.wrap(buffer, 0, size);
                params = new String(byteBuffer.array(), "UTF-8").split(",");

                outputStream.write(buffer, 0, 1);

                inputStream.close();
                outputStream.close();
            }
            catch (Exception ignored) { }
        }
        return params;
    }

    public void sendParameters(float[] params)
    {
        if(piDevice != null)
        {
            //can't use String.join cause of min API requirements
            String msg = String.format("%.2f,%.2f,%.2f", params[0], params[1], params[2]);

            try
            {
                InputStream inputStream = mmSocket.getInputStream();
                OutputStream outputStream = mmSocket.getOutputStream();
                byte[] buffer = new byte[1024];

                buffer[0] = CMD_UPDATE_PARAM;
                outputStream.write(buffer, 0, 1);

                int len = msg.length();
                //Store length in little endian
                buffer[3] = (byte) (len >> 24);
                buffer[2] = (byte) (len >> 16);
                buffer[1] = (byte) (len >> 8);
                buffer[0] = (byte) (len);
                outputStream.write(buffer, 0, 4);

                int i = 0;
                for(char tok : msg.toCharArray())
                {
                    buffer[i++] = (byte) tok;
                }
                outputStream.write(buffer, 0, i);

                inputStream.close();
                outputStream.close();
            }
            catch (Exception ignored) { }
        }
    }
}
