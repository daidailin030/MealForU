package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    GridLayout gridLayout;
    public static int width, height;
    public static String UID, getID, noteID;
    public static boolean login_status = false;
    public static List<String> notename_list = new ArrayList<>();
    public static List<String> noteID_list = new ArrayList<>();

    Button loginButton;
    Toolbar toolbar;

    FirebaseFirestore db;
    TextView username;

    private Handler mThreadHandler;
    private HandlerThread mThread;

    public static Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        UID = getSharedPreferences("login", MODE_PRIVATE)
                .getString("PREF_USERID", "");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setNavigationIcon(R.drawable.mealforu_opt);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // 用toolbar做為APP的ActionBar

        db = FirebaseFirestore.getInstance();


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        gridLayout = findViewById(R.id.gridLayout);
        loginButton = findViewById(R.id.loginButton);
        username = findViewById(R.id.username);

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(run);    //BottomNavigation

        if (!login_status) {
            mThreadHandler.post(loginRunnable);
        } else {
            username.setText(UID);
            loginButton.setText("登出");
            mThreadHandler.post(noteRunnable);
        }
    }

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            loginCheck();
        }
    };

    public void loginCheck() {
        String email = getSharedPreferences("login", MODE_PRIVATE)
                .getString("PREF_USEREMAIL", "");
        String password = getSharedPreferences("login", MODE_PRIVATE)
                .getString("PREF_USERPASSWORD", "");
        if (!email.equals("") && !password.equals("")) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password);
            Thread t3 = new Thread(noteRunnable);
            t3.start();
            try {
                t3.join();
            } catch (InterruptedException e) {

            }
            username.setText(UID);
            loginButton.setText("登出");

            login_status = true;
//            reload_status = true;
        }

    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            BottomNavigationView();
        }
    };

    public void BottomNavigationView() {
        final BottomNavigationView bn = findViewById(R.id.bottom_navigation);
//        bn.post(new Runnable() {
//            @Override
//            public void run() {
//                int bnheight = (int) bn.getMeasuredHeight();
//                height -= bnheight;
//                ScrollView ScrollView = findViewById(R.id.ScrollView);
//                ScrollView.setLayoutParams(new RelativeLayout.LayoutParams(
//                        width, height));
//            }
//        });
        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(MainActivity.this, service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(MainActivity.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(MainActivity.this);
                        break;
                }
                return true;
            }
        });
    }

    public void searchClick(View v) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, search.class);
        startActivity(intent);
    }

    public void onClick(View v) {
        getID = String.valueOf(v.getId());
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, receipt1.class);
        startActivity(intent);
    }

    public void loginClick(View v) {
        Button loginButton = findViewById(R.id.loginButton);
        if (MainActivity.login_status) {
            login.setting = getSharedPreferences("login", MODE_PRIVATE);
            login.setting.edit()
                    .clear()
                    .commit();

            MainActivity.login_status = false;
            Toast.makeText(this, "登出成功", Toast.LENGTH_SHORT).show();
            MainActivity.UID = getSharedPreferences("login", MODE_PRIVATE)
                    .getString("PREF_USERID", "");
            loginButton.setText("登入");

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, login.class);
            startActivity(intent);
        }
    }

    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, timer.class);
        startActivity(intent);
    }
    Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            noteLoad();
        }
    };

    public void noteLoad() {
        notename_list.clear();
        noteID_list.clear();

        db.collection(UID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!document.getId().equals("collection") && !document.getId().equals("history")){
                                    final LinearLayout tmp = new LinearLayout(MainActivity.this);
                                    tmp.setOrientation(LinearLayout.VERTICAL);
                                    DisplayMetrics metrics = new DisplayMetrics();
                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                    int with = 2 * metrics.widthPixels / 5;
                                    TextView nametmp = new TextView(MainActivity.this);
                                    TextView ingredienttmp = new TextView(MainActivity.this);
                                    notename_list.add(document.get("title").toString().replaceAll("[\\[\\]\\s]",""));
                                    noteID_list.add(document.getId());

                                    nametmp.setText(document.get("title").toString().replaceAll("[\\[\\]\\s]",""));
//                                    nametmp.setTextSize(30);
                                    nametmp.setGravity(Gravity.CENTER);
                                    nametmp.setTextSize(20);
                                    nametmp.setTextColor(Color.parseColor("#000000"));
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    params.setMargins(5, 20, 5, 0);
                                    nametmp.setLayoutParams(params);
                                    tmp.addView(nametmp);

                                    List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                    String nameString = "";
                                    int count = 0;
                                    for (String Stringtmp: ingredient_arr){
                                        count++;
                                        nameString += Stringtmp+"\n";
                                        if(count>3){
                                            break;
                                        }
                                    }
                                    ingredienttmp.setText(nameString);
                                    ingredienttmp.setTextSize(15);
                                    ingredienttmp.setGravity(Gravity.CENTER);
                                    ingredienttmp.setTextColor(Color.parseColor("#000000"));
                                    tmp.addView(ingredienttmp);
                                    tmp.setLayoutParams(new ViewGroup.LayoutParams(with, with));
                                    tmp.setTag(document.getId());
                                    tmp.setBackground(getResources().getDrawable(R.drawable.note));
                                    gridLayout.setColumnCount(2);

                                    tmp.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            noteID = (String) tmp.getTag();
                                            Intent intent = new Intent();
                                            intent.setClass(MainActivity.this, notedetail.class);
                                            startActivity(intent);
                                        }
                                    });
                                    tmp.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("要刪除嗎")
                                                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            noteID = (String) tmp.getTag();
                                                            db.collection(UID)
                                                                    .document(noteID)
                                                                    .delete();
                                                            gridLayout.removeAllViews();
                                                            noteLoad();
                                                        }
                                                    }).setNegativeButton("取消", null).create()
                                                    .show();
                                            return false;
                                        }
                                    });
                                    gridLayout.addView(tmp);
                                }

                            }
                        }
                    }
                });
    }

    public void addnoteClick(View v) {
        if (login_status){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, addnote.class);
            startActivity(intent);
        }else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mThread != null) {
            mThread.quit();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, common.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    uri = result.getUri();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, recognition.class);
                    startActivity(intent);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, search.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}