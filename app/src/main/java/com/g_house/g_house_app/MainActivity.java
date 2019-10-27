package com.g_house.g_house_app;

import android.content.Context;
import android.icu.util.Output;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            OutputStreamWriter osw = new OutputStreamWriter((getApplicationContext().openFileOutput("test.txt", Context.MODE_PRIVATE)));
            osw.write("Test 123 456 789");
            osw.close();

            char[] buffer = new char[100];
            InputStreamReader isw = new InputStreamReader(getApplicationContext().openFileInput("test.txt"));
            isw.read(buffer, 0, 100);
            TextView t = (TextView) findViewById(R.id.centered_text);
            t.setText(buffer, 0, 100);
            isw.close();
        }
        catch(Exception ignore) {}
    }
}
