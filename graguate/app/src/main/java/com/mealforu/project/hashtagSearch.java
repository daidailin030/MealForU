package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import static com.mealforu.project.MainActivity.UID;
import static com.mealforu.project.MainActivity.noteID;
import static com.mealforu.project.MainActivity.noteID_list;
import static com.mealforu.project.MainActivity.notename_list;

public class hashtagSearch extends AppCompatActivity {
    GridLayout gridLayout;
    public FirebaseFirestore db;
    EditText searchEdit;
    Toolbar toolbar;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    TextView username;
    Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu17);

        db = FirebaseFirestore.getInstance();
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(2);

        Thread t1 = new Thread(run);
        t1.start();
        Thread t2 = new Thread(hashtagRunnable);
        t2.start();
        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(loginRunnable);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setLogo(R.drawable.mealforu);
        toolbar.setTitle("#"+notedetail.hashtagSearch);

        // 用toolbar做為APP的ActionBar
        setSupportActionBar(toolbar);
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
            username.setText(UID);
            loginButton.setText("登出");

            MainActivity.login_status = true;
//            reload_status = true;
        }
    }
    Runnable run = new Runnable() {
        @Override
        public void run() {
            BottomNavigationView();
        }
    };
    public void BottomNavigationView(){
        final BottomNavigationView bn = findViewById(R.id.bottom_navigation);
        bn.post(new Runnable() {
            @Override
            public void run() {
                ScrollView ScrollView = findViewById(R.id.ScrollView);
                ScrollView.setLayoutParams(new RelativeLayout.LayoutParams(
                        MainActivity.width, MainActivity.height));
            }
        });
        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(hashtagSearch.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(hashtagSearch.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        Intent photo_intent = new Intent();
                        photo_intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
                        startActivity(photo_intent);
                        break;
                }
                return true;
            }
        });
    }
    Runnable hashtagRunnable = new Runnable() {
        @Override
        public void run() {
            noteLoad();
        }
    };
    public void noteLoad(){
        db.collection(MainActivity.UID)
                .whereArrayContains("hashtag",notedetail.hashtagSearch)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final LinearLayout tmp = new LinearLayout(hashtagSearch.this);
                                tmp.setOrientation(LinearLayout.VERTICAL);
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int with = 2 * metrics.widthPixels / 5;
                                TextView nametmp = new TextView(hashtagSearch.this);
                                TextView ingredienttmp = new TextView(hashtagSearch.this);
                                notename_list.add(document.get("title").toString().replaceAll("[\\[\\]\\s]",""));
                                noteID_list.add(document.getId());

                                nametmp.setText(document.get("title").toString().replaceAll("[\\[\\]\\s]",""));
                                nametmp.setTextSize(30);
                                nametmp.setGravity(Gravity.CENTER);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 50, 0, 0);
                                nametmp.setLayoutParams(params);
                                tmp.addView(nametmp);

                                List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                String nameString = "";
                                int count = 0;
                                for (String Stringtmp: ingredient_arr){
                                    count++;
                                    nameString += Stringtmp+"\n";
                                    if(count>5){
                                        break;
                                    }
                                }
                                ingredienttmp.setText(nameString);
                                ingredienttmp.setTextSize(15);
                                ingredienttmp.setGravity(Gravity.CENTER);

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
                                        intent.setClass(hashtagSearch.this, notedetail.class);
                                        startActivity(intent);
                                    }
                                });
                                tmp.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        new AlertDialog.Builder(hashtagSearch.this)
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
                });
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
                intent.setClass(hashtagSearch.this, search.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
