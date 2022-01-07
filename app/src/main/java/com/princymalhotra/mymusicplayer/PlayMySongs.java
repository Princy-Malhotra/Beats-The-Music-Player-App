package com.princymalhotra.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayMySongs extends AppCompatActivity {

    TextView textView,songTime,songDuration;
    SeekBar seekBar;
    ImageView pause,next,previous;

    String sName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_my_songs);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pause=findViewById(R.id.pause);
        next=findViewById(R.id.next);
        previous=findViewById(R.id.previous);
        seekBar=findViewById(R.id.seekBar);
        textView=findViewById(R.id.textView);
        songTime=findViewById(R.id.songTime);
        songDuration=findViewById(R.id.songDuration);

        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songname = intent.getStringExtra("songname");
        position = bundle.getInt("pos",0);

        textView.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sName = mySongs.get(position).getName();
        textView.setText(sName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    mediaPlayer.pause();
                    //Incoming call: Pause music
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    mediaPlayer.start();
                    //Not in call: Play music
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mediaPlayer.pause();
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }



        updateSeekBar=new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currPos = 0;
                while(currPos<totalDuration)
                {
                    try{
                        sleep(500);
                        currPos = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currPos);
                    }
                    catch(InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        songDuration.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable(){
                                @Override
                                public void run() {
                                    String currTime = createTime(mediaPlayer.getCurrentPosition());
                                    songTime.setText(currTime);
                                    handler.postDelayed(this,delay);

                                }
                            },delay);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    pause.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                }
                else
                {
                    pause.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=mySongs.size()-1)
                    position=position+1;
                else
                    position = 0;
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sName = mySongs.get(position).getName();
                textView.setText(sName);
                mediaPlayer.start();
                pause.setImageResource(R.drawable.pause);
                songDuration.setText(createTime(mediaPlayer.getDuration()));
                seekBar.setMax(mediaPlayer.getDuration());

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=0)
                    position=position-1;
                else
                    position = mySongs.size()-1;
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sName = mySongs.get(position).getName();
                textView.setText(sName);
                mediaPlayer.start();
                pause.setImageResource(R.drawable.pause);
                songDuration.setText(createTime(mediaPlayer.getDuration()));
                seekBar.setMax(mediaPlayer.getDuration());
            }
        });

    }

    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = (duration/1000)%60;

        time+=min+":";

        if(sec<10)
            time+="0";
        time+=sec;
        return time;
    }
}