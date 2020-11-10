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

    TextView textView1;
    TextView textView2;

    Button button;

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
    final int PLAY_TURN = 2;

    boolean gameOver;
    int numGuesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);

        button = findViewById(R.id.button);

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
                if (playerOneThread != null) {
                    playerOneThread.playerOneHandler.getLooper().quit();
                    playerTwoThread.playerTwoHandler.getLooper().quit();
                }

                listItems1.clear();
                listItems2.clear();
                adapter1.notifyDataSetChanged();
                adapter2.notifyDataSetChanged();

                gameOver = false;
                numGuesses = 0;

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

            final ArrayList<Character> missedDigits = new ArrayList<>();
            final ArrayList<Integer> prevGuesses = new ArrayList<>();
            final int sequence = generateNumber(missedDigits, prevGuesses);

            playerOneHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here

                    switch (msg.what) {
                        case MAKING_GUESS:
                            evaluateGuess(sequence, msg.arg1, listItems2, adapter2, playerTwoThread.playerTwoHandler, listItems1, adapter1, playerOneHandler);
                            break;
                        case MISSED_DIGIT:
                            missedDigits.add((char) msg.arg1);
                            break;
                        case PLAY_TURN:
                            if (!gameOver) playTurn(missedDigits, prevGuesses);
                            break;
                        default:

                    }
                }
            };


            // Display player's chosen sequence
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    textView1.setText("Thread 1\nChosen Sequence: " + sequence);
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            playTurn(missedDigits, prevGuesses);

            Looper.loop();
        }

        public void playTurn(ArrayList<Character> missedDigits, ArrayList<Integer> prevGuesses) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Display current thread's guess
            final int guess = generateNumber(missedDigits, prevGuesses);
            prevGuesses.add(guess);

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listItems1.add("Guessing: " + guess);
                    adapter1.notifyDataSetChanged();
                }
            });

            // Send guess to opponent thread for evaluation
            Message msg = Message.obtain();
            msg.what = MAKING_GUESS;
            msg.arg1 = guess;
            playerTwoThread.playerTwoHandler.sendMessage(msg);
        }
    }

    public class PlayerTwoThread extends Thread {

        public Handler playerTwoHandler;

        public void run() {
            Looper.prepare();

            final ArrayList<Character> missedDigits = new ArrayList<>();
            final ArrayList<Integer> prevGuesses = new ArrayList<>();
            final int sequence = generateNumber(missedDigits, prevGuesses);

            playerTwoHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    switch (msg.what) {
                        case MAKING_GUESS:
                            evaluateGuess(sequence, msg.arg1, listItems1, adapter1, playerOneThread.playerOneHandler, listItems2, adapter2, playerTwoHandler);
                            numGuesses++;
                            break;
                        case MISSED_DIGIT:
                            missedDigits.add((char) msg.arg1);
                            break;
                        case PLAY_TURN:
                            if (!gameOver) playTurn(missedDigits, prevGuesses);
                            break;
                        default:

                    }
                }
            };


            // Display player's chosen sequence
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    textView2.setText("Thread 2\nChosen Sequence: " + sequence);
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            playTurn(missedDigits, prevGuesses);

            Looper.loop();
        }

        public void playTurn(ArrayList<Character> missedDigits, ArrayList<Integer> prevGuesses) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Display current thread's guess
            final int guess = generateNumber(missedDigits, prevGuesses);
            prevGuesses.add(guess);

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
        }
    }

    public boolean isValidNumber(int number, ArrayList<Character> missedDigits, ArrayList<Integer> prevGuesses) {
        if (number < 1234 || number > 9876) return false;
        if (prevGuesses.contains(number)) return false;
        char[] numberArray = Integer.toString(number).toCharArray();
        HashSet<Character> set = new HashSet<>();
        for (char c : numberArray) {
            if (set.contains(c)) return false;
            if (missedDigits.contains(c)) return false;
            set.add(c);
        }
        return true;
    }

    public int generateNumber(ArrayList<Character> missedDigits, ArrayList<Integer> prevGuesses) {
        int num = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        while (!isValidNumber(num, missedDigits, prevGuesses))
            num = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
        return num;
    }

    public void evaluateGuess(final int sequence, final int guess, final ArrayList<String> listItems, final ArrayAdapter<String> adapter, Handler handler, final ArrayList<String> otherListItems, final ArrayAdapter<String> otherAdapter, Handler selfHandler) {

        if (sequence == guess || numGuesses >= 20) {
            gameOver = true;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (sequence == guess) {
                        listItems.add("Guess was correct. You Win!");
                        otherListItems.add("Opponent guessed sequence. You Lose!");
                    }
                    else {
                        listItems.add("20 guesses made. Game draw!");
                        otherListItems.add("20 guesses made. Game draw!");
                    }
                    adapter.notifyDataSetChanged();
                    otherAdapter.notifyDataSetChanged();
//                    Message msg = Message.obtain();
//                    msg.what = GAME_OVER;
                    playerOneThread.playerOneHandler.getLooper().quit();
                    playerTwoThread.playerTwoHandler.getLooper().quit();
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

        String results = "Num correct pos: " + numCorrectPosition + "\nNum incorrect pos: " + numIncorrectPosition;

        // send back missed digits
        int length = missedDigits.size();
        if (length > 0) {
            int index = ThreadLocalRandom.current().nextInt(0, length);
            final char missedDigit = missedDigits.get(index);
            Message msg = Message.obtain();
            msg.what = MISSED_DIGIT;
            msg.arg1 = missedDigit;
            handler.sendMessage(msg);

            results += "\nMissed Digit: " + missedDigit;
        }

        // Display back results
        final String finalResults = results;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listItems.add(finalResults);
                adapter.notifyDataSetChanged();
            }
        });


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message msg = Message.obtain();
        msg.what = PLAY_TURN;
        selfHandler.sendMessage(msg);

    }


}