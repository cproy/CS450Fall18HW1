package edu.stlawu.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Define variable for our views
    private TextView tv_count = null;
    private Button bt_start = null;
    private Button bt_stop = null;
    private Button bt_reset = null;
    private Button bt_resume = null;
    private Timer t = null;
    private Counter ctr = null;  // TimerTask

    public AudioAttributes  aa = null;
    private SoundPool soundPool = null;
    private int bloopSound = 0;

    DecimalFormat df = new DecimalFormat("00.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        this.tv_count = findViewById(R.id.tv_count);
        this.bt_start = findViewById(R.id.bt_start);
        this.bt_stop = findViewById(R.id.bt_stop);
        this.bt_reset = findViewById(R.id.bt_reset);
        this.bt_resume = findViewById(R.id.bt_resume);

        this.bt_start.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 bt_start.setEnabled(false);
                 MainActivity.this.ctr = new Counter();
                 t.scheduleAtFixedRate(ctr, 0, 100);
                 bt_stop.setEnabled(true);
                 bt_reset.setEnabled(true);
                 bt_resume.setEnabled(false);
             }
            });

        this.bt_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_resume.setEnabled(false);
                MainActivity.this.ctr = new Counter();
                MainActivity.this.ctr.count = getPreferences(MODE_PRIVATE).getFloat("COUNT", 0f);
                t.scheduleAtFixedRate(ctr, 0, 100);
                bt_stop.setEnabled(true);
            }
        });

        this.bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.ctr.count = 0f;
                // This variable placeholder is because this method is being considered static
                // and I can't call the non-static MainActivity.this.ctr through a static method
                float placeholder = MainActivity.this.ctr.count;
                MainActivity.this.tv_count.setText(
                        formatTime(placeholder, df));
                t.cancel();
                t = null;
                t = new Timer();
                bt_stop.setEnabled(false);
                bt_start.setEnabled(true);
                bt_reset.setEnabled(false);
                bt_resume.setEnabled(false);
            }
        });

        this.bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t.cancel();
                getPreferences(MODE_PRIVATE)
                        .edit()
                        .putFloat("COUNT", MainActivity.this.ctr.count)
                        .apply();
                t = null;
                t = new Timer();
                bt_resume.setEnabled(true);
                bt_stop.setEnabled(false);
            }
        });

        this.aa = new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        this.soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aa)
                .build();
        this.bloopSound = this.soundPool.load(
                this, R.raw.bloop, 1);

        this.tv_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(bloopSound, 1f,
                        1f, 1, 0, 1f);
                Animator anim = AnimatorInflater
                        .loadAnimator(MainActivity.this,
                                       R.animator.counter);
                anim.setTarget(tv_count);
                anim.start();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // reload the count from a previous
        // run, if first time running, start at 0.
        /// preferences to share state
        float count = getPreferences(MODE_PRIVATE)
                         .getFloat("COUNT", 0f);
        this.tv_count.setText(formatTime(count, df));
        this.ctr = new Counter();
        this.ctr.count = count;
        this.t = new Timer();

        // set buttons to correct in initial state
        if (tv_count.getText().equals("0:00.0")){
            bt_resume.setEnabled(false);
            bt_reset.setEnabled(false);
        } else {
            bt_start.setEnabled(false);
            bt_reset.setEnabled(true);
        }
        bt_stop.setEnabled(false);

        // factory method - design pattern
        Toast.makeText(this, "Stopwatch is started",
                        Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferences(MODE_PRIVATE)
                .edit()
                .putFloat("COUNT", ctr.count)
                .apply();
    }

    class Counter extends TimerTask {
        private float count = 0f;

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.tv_count.setText(
                                    formatTime(count, df));
                            count += 0.1f;
                        }
                    }
            );
        }
    }

    // Format the time to mm:ss:t
    // I'd like to use time since midnight here, but IDK how Java does it
    private String formatTime(float time, DecimalFormat df){
        int minutes = (int)time / 60;
        float seconds = time - (minutes * 60);
        return Integer.toString(minutes) + ':' + df.format(seconds);
    }
}
