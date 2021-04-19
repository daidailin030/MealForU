package com.mealforu.project;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import java.security.Provider;

public class timerService extends IntentService {
    private long mTimeLeftInMillis;
    private long mStartTimeInMillis;
    private long mEndTime;


    public timerService() {
        super("timer");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(timer.mTimerRunning != false){
            System.out.println("new");
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
            mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
            mEndTime = prefs.getLong("endTime", 0);

            for (int i=0;i<mEndTime - System.currentTimeMillis()+1;i++){
                System.out.println(mEndTime - System.currentTimeMillis());
                SystemClock.sleep(500);
                if (mEndTime - System.currentTimeMillis() <= 0) {
                    Intent it = new Intent("time up"); //設定廣播識別碼
                    sendBroadcast(it);
                    System.out.println("yes!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    onDestroy();
                    break;
                }
            }
        }
    }
}
