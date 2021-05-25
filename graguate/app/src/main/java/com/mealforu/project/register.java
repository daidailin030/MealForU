package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;


public class register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String userUID;
    EditText nameEdit, passwdEdit, passwdagainEdit, emailEdit, telEdit;
    RadioGroup genderGroup;
    String gender;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    TextView username;
    Button loginButton;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu12);

        TextView logoText = (TextView) findViewById(R.id.logoText);
        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "font/BRUSHSCI.TTF");
        logoText = (TextView) findViewById(R.id.logoText);
        logoText.setTypeface(myTypeFace);

        BottomNavigationView();

        nameEdit = findViewById(R.id.nameEdit);
        passwdEdit = findViewById(R.id.passwdEdit);
        passwdagainEdit = findViewById(R.id.passwdagainEdit);
        emailEdit = findViewById(R.id.emailEdit);
        telEdit = findViewById(R.id.telEdit);
        genderGroup = findViewById(R.id.genderGroup);

        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setTitle("註冊");
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(loginRunnable);
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

    public void BottomNavigationView() {
        final BottomNavigationView bn = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(register.this, service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(register.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(register.this);
                        break;
                }
                return true;
            }
        });
    }

    public void sendClick(View v) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String name = nameEdit.getText().toString();
        final String passwd = passwdEdit.getText().toString();
        final String passwdagain = passwdagainEdit.getText().toString();
        final String email = emailEdit.getText().toString();
        final String tel = telEdit.getText().toString();

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.maleButton:
                        gender = "男";
                        break;
                    case R.id.femaleButton:
                        gender = "女";
                        break;
                }
            }
        });
        if (name.isEmpty() || name.isEmpty() || passwd.isEmpty() || passwdagain.isEmpty()) {
            new AlertDialog.Builder(register.this)
                    .setTitle("必填欄位空白")
                    .setPositiveButton("確認", null)
                    .show();
        } else if (!passwd.equals(passwdagain) && !passwd.isEmpty() && !passwdagain.isEmpty()) {
            new AlertDialog.Builder(register.this)
                    .setTitle("密碼不同!")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            passwdEdit.setText("");
                            passwdagainEdit.setText("");
                        }
                    })
                    .show();
        } else {
            if (passwd.equals(passwdagain) && !passwd.isEmpty() && !passwdagain.isEmpty()) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener(
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        String message = task.isSuccessful() ? "註冊成功" : "註冊失敗";
                                        if (message.equals("註冊成功")) {
                                            mAuth.signInWithEmailAndPassword(email, passwd)
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.d("onComplete", "登入失敗");
                                                                Intent intent = new Intent();
                                                                intent.setClass(register.this, login.class);
                                                                startActivity(intent);
                                                            } else {
                                                                Map<String, Object> user = new HashMap<>();
                                                                user.put("user", name);
                                                                user.put("gender", gender);
                                                                user.put("email", email);
                                                                user.put("tel", tel);
                                                                user.put("password", passwd);
//                                                                user.put("history", "");
//                                                                user.put("collection", "");
                                                                userUID = mAuth.getUid();
                                                                db.collection("user").document(userUID)
                                                                        .set(user)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                                       Log.d("register", "成功");
                                                                                                   }
                                                                                               }
                                                                        );
                                                                ArrayList<String> tmpList = new ArrayList<>();
                                                                Map<String, Object> data = new HashMap<>();
                                                                Map<String, Object> datail = new HashMap<>();
                                                                Map<String, Object> data2 = new HashMap<>();
                                                                Map<String, Object> tagdatail = new HashMap<>();
                                                                tagdatail.put("日本料理",1);
                                                                data.put("all", tmpList);
                                                                data2.put("keyword", datail);
                                                                data2.put("view",datail );
                                                                data2.put("hashtag", tagdatail);

                                                                System.out.println("id!!!!!!!!!!!!"+userUID);
                                                                db.collection(userUID).document("collection").set(data,SetOptions.merge());
                                                                db.collection(userUID).document("history").set(data2,SetOptions.merge());
//                                                                db.collection(userUID).document()
                                                                login.setting = getSharedPreferences("login", MODE_PRIVATE);
                                                                login.setting.edit()
                                                                        .putString("PREF_USERID", userUID)
                                                                        .commit();
                                                                Intent intent = new Intent();
                                                                intent.setClass(register.this, MainActivity.class);
                                                                finish();
                                                                overridePendingTransition(0, 0);
                                                                startActivity(intent);
                                                                overridePendingTransition(0, 0);
                                                                Toast.makeText(getApplicationContext(), "註冊成功", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                        } else {
                                            new AlertDialog.Builder(register.this)
                                                    .setTitle(message)
                                                    .setMessage("再試一次")
                                                    .setPositiveButton("OK", null)
                                                    .show();

                                        }
                                    }
                                }
                        );
            }
        }
    }

    public void clearClick(View v) {

        genderGroup.clearCheck();
        nameEdit.setText("");
        passwdEdit.setText("");
        passwdagainEdit.setText("");
        emailEdit.setText("");
        telEdit.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    MainActivity.uri = result.getUri();
                    Intent intent = new Intent();
                    intent.setClass(register.this, recognition.class);
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

    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(register.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(register.this, timer.class);
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
            intent.setClass(register.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(register.this, login.class);
            startActivity(intent);
        }
    }

    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(register.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(register.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(register.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(register.this, common.class);
        startActivity(intent);
    }

    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(register.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(register.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(register.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}

