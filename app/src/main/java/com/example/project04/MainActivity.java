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

    ArrayList<String> listItems1 = new ArrayList<String>();
    ArrayList<String> listItems2 = new ArrayList<String>();
    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;

    Handler mainHandler;
    PlayerOneThread playerOneThread;
    PlayerTwoThread playerTwoThread;

    final int MAKING_GUESS = 0;
    final int MISSED_DIGIT = 1;

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

            final int sequence = generateNumber();
            final ArrayList<Character> missedDigits = new ArrayList<>();

            playerOneHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here

                    switch (msg.what) {
                        case MAKING_GUESS:
                            evaluateGuess(sequence, msg.arg1, listItems2, adapter2, playerTwoThread.playerTwoHandler);
                            break;
                        case MISSED_DIGIT:
                            missedDigits.add((char) msg.arg1);

                        default:

                    }
                }
            };

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


            // Display current thread's guess
            final int guess = generateNumber();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems1.add("Guessing: " + guess);
                    adapter1.notifyDataSetChanged();
                }
            });


            // Send guess to opponent thread
            Message msg = Message.obtain();
            msg.what = MAKING_GUESS;
            msg.arg1 = guess;
            playerTwoThread.playerTwoHandler.sendMessage(msg);


            Looper.loop();
        }
    }

    public class PlayerTwoThread extends Thread {

        public Handler playerTwoHandler;

        public void run() {
            Looper.prepare();

            final int sequence = generateNumber();
            final ArrayList<Character> missedDigits = new ArrayList<>();

            playerTwoHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    switch (msg.what) {
                        case MAKING_GUESS:
                            evaluateGuess(sequence, msg.arg1, listItems1, adapter1, playerOneThread.playerOneHandler);
                            break;
                        case MISSED_DIGIT:
                            missedDigits.add((char) msg.arg1);
                            break;
                        default:

                    }
                }
            };

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

            // Display current thread's guess
            final int guess = generateNumber();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems2.add("Guessing: " + guess);
                    adapter2.notifyDataSetChanged();
                }
            });

            // Send guess to opponent thread
            Message msg = Message.obtain();
            msg.what = MAKING_GUESS;
            msg.arg1 = guess;
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

    public void evaluateGuess(int sequence, int guess, final ArrayList<String> listItems, final ArrayAdapter<String> adapter, Handler handler) {
        if (sequence == guess) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems.add("Guess was correct!");
                    adapter.notifyDataSetChanged();
                }
            });
            return;
        }

        // count correct digits
        char[] sequenceArray = Integer.toString(sequence).toCharArray();
        char[] guessArray = Integer.toString(guess).toCharArray();
        int numCorrectPosition = 0;
        int numIncorrectPosition = 0;
        HashSet<Character> set = new HashSet<>();
        ArrayList<Character> missedDigits = new ArrayList<>();
        for (char c : sequenceArray) set.add(c);
        for (int i = 0; i < guessArray.length; ++i) {
            if (guessArray[i] == sequenceArray[i]) numCorrectPosition++;
            else if (set.contains(guessArray[i])) numIncorrectPosition++;
            else missedDigits.add(guessArray[i]);
        }

        // display back information
        final int finalNumCorrectPosition = numCorrectPosition;
        final int finalNumIncorrectPosition = numIncorrectPosition;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listItems.add("Num correct pos: " + finalNumCorrectPosition);
                listItems.add("Num incorrect pos: " + finalNumIncorrectPosition);
                adapter.notifyDataSetChanged();
            }
        });

        // send back missed digits
        int length = missedDigits.size();
        if (length > 0) {
            int index = ThreadLocalRandom.current().nextInt(0, length);
            final char missedDigit = missedDigits.get(index);
            Message msg = Message.obtain();
            msg.what = MISSED_DIGIT;
            msg.arg1 = missedDigit;
            handler.sendMessage(msg);

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems.add("Missed Digit: " + missedDigit);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


}