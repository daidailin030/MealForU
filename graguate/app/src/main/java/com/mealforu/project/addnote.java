package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class addnote extends AppCompatActivity {
    LinearLayout ingredientLayout;
    int ingredientCount = 1;
    FirebaseFirestore db;
    EditText notetitle;
    Toolbar toolbar;
    TextView username;
    Button loginButton;
    private Handler mThreadHandler;
    private HandlerThread mThread;

    private ArrayList<String> ingredient_arr = new ArrayList<>();
    private ArrayList<String> amount_arr = new ArrayList<>();
    private Map<String, Object> recipedetail = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu5);

        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);

        db = FirebaseFirestore.getInstance();
        ingredientLayout = findViewById(R.id.ingredientLayout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setTitle("新增備忘錄");

        setSupportActionBar(toolbar);

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler=new Handler(mThread.getLooper());
        mThreadHandler.post(run);
        mThreadHandler.post(loginRunnable);
        mThreadHandler.post(noteRunnable);
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
        final BottomNavigationView bn = (BottomNavigationView)findViewById(R.id.bottom_navigation);
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
                        service_intent.setClass(addnote.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(addnote.this , MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(addnote.this);
                        break;
                }
                return true;
            }
        });
    }
    Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            note();
        }
    };
    private void note(){
        LinearLayout tmp = new LinearLayout(addnote.this);
        tmp.setOrientation(LinearLayout.HORIZONTAL);

        Button dele = new Button(addnote.this);
        dele.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        dele.setGravity(1);
        dele.setBackgroundColor(Color.WHITE);
        dele.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
        tmp.addView(dele);

        EditText ingredient = new EditText(addnote.this);
        ingredient.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ingredient.setHint("例：高麗菜");
        ingredient.setId(ingredientCount);
        ingredientCount++;

        EditText amount = new EditText(addnote.this);
        amount.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        amount.setHint("例：100g");
        amount.setId(ingredientCount);
        ingredientCount++;

        tmp.addView(ingredient);
        tmp.addView(amount);
        ingredientLayout.addView(tmp);

        Button addingredient = findViewById(R.id.addIngredient);
        addingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout tmp = new LinearLayout(addnote.this);
                tmp.setOrientation(LinearLayout.HORIZONTAL);

                Button dele = new Button(addnote.this);
                dele.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
                dele.setBackgroundColor(Color.WHITE);
                dele.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
                dele.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        ingredientLayout.removeView(tmp);
                    }
                });
                tmp.addView(dele);

                EditText ingredient = new EditText(addnote.this);
                ingredient.setId(ingredientCount);
                ingredientCount++;
                ingredient.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                EditText amount = new EditText(addnote.this);
                amount.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                amount.setId(ingredientCount);


                tmp.addView(ingredient);
                tmp.addView(amount);
                ingredientLayout.addView(tmp);
                ingredientCount++;
            }
        });
    }
    public void addButton(View v){
        notetitle = findViewById(R.id.notetitle);
        if(notetitle.equals("")){
            Toast.makeText(getApplicationContext(), "請輸入標題", Toast.LENGTH_SHORT).show();
        }else{
            for(int i = 1; i<=ingredientCount; i+=2) {
                String EditTextID = String.valueOf(i);
                int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
                EditText tmp = findViewById(resID);
                try {
                    ingredient_arr.add(tmp.getText().toString());
                }catch(NullPointerException e) {
                }
            }
            for (int x = 2;x<=ingredientCount; x+=2){
                String EditTextID = String.valueOf(x);
                int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
                EditText tmp =  findViewById(resID);
                try {
                    amount_arr.add(tmp.getText().toString());
                }catch(NullPointerException e) {

                }
            }
            //hashtag
            HashTagHelper mEditTextHashTagHelper;
            final EditText tag_input = findViewById(R.id.tag_input);
            mEditTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimaryDark), null);
            mEditTextHashTagHelper.handle(tag_input);

            List<String> allHashTags = mEditTextHashTagHelper.getAllHashTags();
            System.out.println("allHashTags: "+allHashTags.toString());
            ArrayList<String> hashtag_arr = new ArrayList<>(allHashTags);

            recipedetail.put("title", notetitle.getText().toString());
            recipedetail.put("ingredients", ingredient_arr);
            recipedetail.put("amount", amount_arr);
            recipedetail.put("hashtag", hashtag_arr);

            db.collection(MainActivity.UID)
                    .add(recipedetail)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent();
                                intent.setClass(addnote.this, MainActivity.class);
                                Toast.makeText(getApplicationContext(),"成功",Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        }
                    });
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if (mThread != null) {
            mThread.quit();
        }
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
                    intent.setClass(addnote.this, recognition.class);
                    startActivity(intent);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blank_menu, menu);
        return true;
    }
    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(addnote.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(addnote.this, timer.class);
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
            intent.setClass(addnote.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(addnote.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(addnote.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(addnote.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(addnote.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(addnote.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(addnote.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(addnote.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(addnote.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
