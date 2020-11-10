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
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

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
    PlayerTwoThread playerTwoThread;

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


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "helloooooooo", Toast.LENGTH_SHORT).show();

                playerOneThread = new PlayerOneThread();
                playerTwoThread = new PlayerTwoThread();

                playerOneThread.start();
                playerTwoThread.start();

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

                    final int num = msg.arg1;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listItems1.add("Received: " + num);
                            adapter1.notifyDataSetChanged();
                        }
                    });
                }
            };

            final int sequence = generateNumber();

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems1.add("Chosen Sequence: " + sequence);
                    adapter1.notifyDataSetChanged();
                }
            });


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Message msg = Message.obtain();
            msg.arg1 = sequence;
            playerTwoThread.playerTwoHandler.sendMessage(msg);


            Looper.loop();
        }
    }

    public class PlayerTwoThread extends Thread {

        public Handler playerTwoHandler;

        public void run() {
            Looper.prepare();

            playerTwoHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here

                    final int num = msg.arg1;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listItems2.add("Received: " + num);
                            adapter2.notifyDataSetChanged();
                        }
                    });
                }
            };

            final int sequence = generateNumber();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems2.add("Chosen Sequence: " + sequence);
                    adapter2.notifyDataSetChanged();
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Message msg = Message.obtain();
            msg.arg1 = sequence;
            playerOneThread.playerOneHandler.sendMessage(msg);

            Looper.loop();
        }
    }

    public boolean isValidNumber(int number) {
        if (number < 1234 || number > 9876) return false;
        char[] numberArray = Integer.toString(number).toCharArray();
        HashSet<Character> set = new HashSet<>();
        for (char c : numberArray) {
            if (set.contains(c)) return false;
            set.add(c);
        }
        return true;
    }

    public int generateNumber() {
        int num = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        while (!isValidNumber(num))
            num = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        return num;
    }


}