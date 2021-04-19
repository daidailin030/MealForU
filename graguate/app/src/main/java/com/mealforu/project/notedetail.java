package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class notedetail extends AppCompatActivity {
    FirebaseFirestore db;
    LinearLayout linerLayout;
    protected static List<String> ingredient_arr, amount_arr, SearchIngredient_arr, hashtag_arr;
    public static String hashtagSearch;
    EditText title;
    int ingredientCount = 1;
    Toolbar toolbar;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    TextView username;
    Button loginButton;
    TextView tag_input_Text;
    EditText tag_input_Edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu15);

        db = FirebaseFirestore.getInstance();
        linerLayout = findViewById(R.id.linerLayout);
        ingredient_arr = new ArrayList<>();
        amount_arr = new ArrayList<>();
        title = findViewById(R.id.title);

        Thread t1 = new Thread(run);
        t1.start();
        Thread t2 = new Thread(LoadRunnable);
        t2.start();

        Button addingredient = findViewById(R.id.addingredientButton);
        addingredient.setOnClickListener(addingredientClick);

        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(loginRunnable);
        tag_input_Text = findViewById(R.id.tag_input_Text);
        tag_input_Edit = findViewById(R.id.tag_input_Edit);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.mealforu);
        toolbar.setClickable(false);
        toolbar.setTitle("備忘錄");

        // 用toolbar做為APP的ActionBar
        setSupportActionBar(toolbar);

    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            BottomNavigationView();
        }
    };
    public void BottomNavigationView(){
        final BottomNavigationView bn = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(notedetail.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(notedetail.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(notedetail.this);
                        break;
                }
                return true;
            }
        });
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
    Runnable LoadRunnable = new Runnable() {
        @Override
        public void run() {
            db.collection(MainActivity.UID).document(MainActivity.noteID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    title.setText(document.get("title").toString());
                                    ingredient_arr = (List<String>)document.get("ingredients");
                                    amount_arr = (List<String>)document.get("amount");
                                    hashtag_arr = (List<String>)document.get("hashtag");
                                    if (document.contains("ingredients")){
                                        ViewAdd();
                                    }
                                    if (document.contains("hashtag")){
                                        hashtag();
                                    }
                                }
                            }
                        }
                    });


        }
    };
    public void ViewAdd(){
        EditText ingredients[] = new EditText[ingredient_arr.size()];
        EditText amount[] = new EditText[amount_arr.size()];

        for(int i = 0; i<ingredient_arr.size(); i++){
            final LinearLayout tmp = new LinearLayout(notedetail.this);
            tmp.setOrientation(LinearLayout.HORIZONTAL);

            Button dele = new Button(notedetail.this);
            dele.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
            dele.setBackgroundColor(Color.WHITE);
            dele.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
            dele.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    linerLayout.removeView(tmp);
                }
            });
            tmp.addView(dele);

            ingredients[i] = new EditText(notedetail.this);
            ingredients[i].setText(ingredient_arr.get(i));
            ingredients[i].setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            ingredients[i].setId(ingredientCount);
            tmp.addView(ingredients[i]);
            ingredientCount++;

            amount[i] = new EditText(notedetail.this);
            amount[i].setText(amount_arr.get(i));
            amount[i].setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            amount[i].setId(ingredientCount);
            tmp.addView(amount[i]);
            ingredientCount++;
            linerLayout.addView(tmp);
        }
    }
    public void hashtag (){
        HashTagHelper mTextHashTagHelper;
        String hashtagtmp = " ";
        for (String tmp: hashtag_arr){
            hashtagtmp += "#"+tmp+"  ";
        }
        tag_input_Text.setText(hashtagtmp);
        mTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                Log.v("TAG", "onHashTagClicked [" + hashTag + "]");
                hashtagSearch = hashTag;
                System.out.println("hashTag: "+hashTag);
                Intent intent = new Intent();
                intent.setClass(notedetail.this, hashtagSearch.class);
                startActivity(intent);
            }
        });
        mTextHashTagHelper.handle(tag_input_Text);
    }

    private Button.OnClickListener addingredientClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            final LinearLayout tmp = new LinearLayout(notedetail.this);
            tmp.setOrientation(LinearLayout.HORIZONTAL);

            Button dele = new Button(notedetail.this);
            dele.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
            dele.setBackgroundColor(Color.WHITE);
            dele.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
            dele.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    linerLayout.removeView(tmp);
                }
            });
            tmp.addView(dele);

            EditText ingredient = new EditText(notedetail.this);
            ingredient.setId(ingredientCount);
            ingredientCount++;
            ingredient.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            EditText amount = new EditText(notedetail.this);
            amount.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            amount.setId(ingredientCount);

            tmp.addView(ingredient);
            tmp.addView(amount);
            linerLayout.addView(tmp);
            ingredientCount++;
        }
    };
    public void hashtagClick(View v){
        tag_input_Edit.setText(tag_input_Text.getText());
        tag_input_Edit.setVisibility(View.VISIBLE);
        tag_input_Text.setVisibility(View.INVISIBLE);
    }
    public void searchClick(View v) {
        SearchIngredient_arr = new ArrayList<>();
        search.ActivityFrom = "notedetail";
        for(int i = 1; i<=ingredientCount; i+=2) {
            String EditTextID = String.valueOf(i);
            int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
            EditText tmp = findViewById(resID);
            try {
                SearchIngredient_arr.add(tmp.getText().toString());
            }catch(NullPointerException e) {
            }
        }
        Intent intent = new Intent();
        intent.setClass(notedetail.this , search.class);
        startActivity(intent);
    }
    public void saveClick(View v){
        List<String> NewIngredient_arr = new ArrayList<>();
        List<String> NewAmount_arr = new ArrayList<>();

        if(title.equals("")){
            Toast.makeText(getApplicationContext(), "請輸入標題", Toast.LENGTH_SHORT).show();
        }else {
            for(int i = 1; i<=ingredientCount; i+=2) {
                String EditTextID = String.valueOf(i);
                int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
                EditText tmp = findViewById(resID);
                try {
                    NewIngredient_arr.add(tmp.getText().toString());
                }catch(NullPointerException e) {
                }
            }
            for (int x = 2;x<=ingredientCount; x+=2){
                String EditTextID = String.valueOf(x);
                int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
                EditText tmp =  findViewById(resID);
                try {
                    NewAmount_arr.add(tmp.getText().toString());
                }catch(NullPointerException e) {

                }
            }

            Map<String, Object> recipedetail = new HashMap<>();
            recipedetail.put("title", title.getText().toString());
            recipedetail.put("ingredients", NewIngredient_arr);
            recipedetail.put("amount", NewAmount_arr);

            if (tag_input_Edit.getVisibility() == View.VISIBLE){
                HashTagHelper mEditTextHashTagHelper;
                mEditTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimaryDark), null);
                mEditTextHashTagHelper.handle(tag_input_Edit);

                List<String> allHashTags = mEditTextHashTagHelper.getAllHashTags();
                ArrayList<String> hashtag_tmparr = new ArrayList<>(allHashTags);

                recipedetail.put("hashtag", hashtag_tmparr);
            }
            else{
                recipedetail.put("hashtag",hashtag_arr);
            }
            db.collection(MainActivity.UID).document(MainActivity.noteID)
                    .set(recipedetail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"儲存成功",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getApplicationContext(), "請稍後再試", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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
                    intent.setClass(notedetail.this, recognition.class);
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
        intent.setClass(notedetail.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(notedetail.this, timer.class);
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
            intent.setClass(notedetail.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(notedetail.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(notedetail.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(notedetail.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(notedetail.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(notedetail.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(notedetail.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(notedetail.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(notedetail.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}

