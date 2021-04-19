package com.mealforu.project;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ClockActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        mediaPlayer = mediaPlayer.create(this,R.raw.ring);
        mediaPlayer.start();
        //創建一個鬧鐘提醒的對話框,點擊確定關閉鈴聲與頁面
        new AlertDialog.Builder(ClockActivity.this).setTitle("鬧鐘").setMessage("小豬小豬快起床~")
                .setPositiveButton("關閉鬧鈴", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.stop();
                ClockActivity.this.finish();
            }
        }).show();
    }
}