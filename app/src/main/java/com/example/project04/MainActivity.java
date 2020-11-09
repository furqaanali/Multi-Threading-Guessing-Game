package com.example.project04;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button button;
    Button button2;

    ListView listview1;
    ListView listview2;

    ArrayList<String> listItems1=new ArrayList<String>();
    ArrayList<String> listItems2=new ArrayList<String>();

    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;

    Handler mainHandler;

    PlayerOneThread playerOneThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        listview1 = findViewById(R.id.list_thread1);
        listview2 = findViewById(R.id.list_thread2);

        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems1);
        listview1.setAdapter(adapter1);
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems2);
        listview2.setAdapter(adapter2);

        mainHandler = new Handler();

        playerOneThread = new PlayerOneThread();
        playerOneThread.start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "helloooooooo", Toast.LENGTH_SHORT).show();

                playerOneThread.playerOneHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < 10; i++) {
                            try {
                                Log.i("Test", "hi " + i);
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            final int finalI = i;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    button.setText("x: " + finalI);
                                    listItems1.add("list1: "+finalI);
                                    listItems2.add("list2: "+finalI);
                                    adapter1.notifyDataSetChanged();
                                    adapter2.notifyDataSetChanged();
                                }
                            });
                        }

                    }
                });

            }
        });
    }


    public class PlayerOneThread extends Thread {

        public Handler playerOneHandler;

        public void run() {
            Looper.prepare();

            playerOneHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                }
            };

            Looper.loop();
        }
    }


}