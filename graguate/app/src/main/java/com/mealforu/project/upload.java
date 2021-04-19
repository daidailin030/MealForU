package com.mealforu.project;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class upload extends AppCompatActivity {
    LinearLayout ingredientLayout, stepLayout;
    int ingredientCount = 1;
    int stepCount = 1;
    int stepIDCount = 100;
    FirebaseFirestore db;
    EditText nameEdit, inforEdit;
    String docID, person;
    Toolbar toolbar;

    private Handler mThreadHandler;
    private Handler mUI_Handler = new Handler();
    private HandlerThread mThread;
    private ImageButton photoBotton;
    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference mStorageRef;
    private String image;
    TextView username;
    Button loginButton;
    private ArrayList<String> ingredient_arr = new ArrayList<>();
    private ArrayList<String> amount_arr = new ArrayList<>();
    private ArrayList<String> step_arr = new ArrayList<>();
    private Map<String,Object> recipedetail = new HashMap<>();
    private ArrayList<String> hashtag_arr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu14);

        db = FirebaseFirestore.getInstance();
        ingredientLayout = findViewById(R.id.ingredientLayout);
        photoBotton = findViewById(R.id.photoButton);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        nameEdit = findViewById(R.id.nameEdit);
        inforEdit = findViewById(R.id.inforEdit);

        photoBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        Thread t1 = new Thread(run);
        t1.start();

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(contentRun);
        mThreadHandler.post(loginRunnable);
        toolbar = findViewById(R.id.toolbar);
        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);
        toolbar.setClickable(false);
        toolbar.setLogo(R.drawable.mealforu);
        toolbar.setTitle("上傳食譜");

        // 用toolbar做為APP的ActionBar
        setSupportActionBar(toolbar);
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            BottomNavigationView();
        }
    };
    Runnable contentRun = new Runnable() {
        @Override
        public void run() {
            ingredient();
            step();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacks(run);
        }
        if (mThread != null) {
            mThread.quit();
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
            username.setText(UID);
            loginButton.setText("登出");

            MainActivity.login_status = true;
//            reload_status = true;
        }
    }
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
                        service_intent.setClass(upload.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(upload.this , MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(upload.this);
                        break;
                }
                return true;
            }
        });
    }
    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(photoBotton);
            photoBotton.setImageURI(mImageUri);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    MainActivity.uri = result.getUri();
                    Intent intent = new Intent();
                    intent.setClass(upload.this, recognition.class);
                    startActivity(intent);
                }
            }
        }
    }

    private void ingredient(){
        final LinearLayout tmp = new LinearLayout(upload.this);
        tmp.setOrientation(LinearLayout.HORIZONTAL);

        Button dele = new Button(upload.this);
        dele.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        dele.setGravity(1);
        dele.setBackgroundColor(Color.WHITE);
        dele.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
        dele.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ingredientLayout.removeView(tmp);
            }
        });
        tmp.addView(dele);

        EditText ingredient = new EditText(upload.this);
        ingredient.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ingredient.setHint("例：高麗菜");
        ingredient.setId(ingredientCount);
        ingredientCount++;

        EditText amount = new EditText(upload.this);
        amount.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        amount.setHint("例：100g");
        amount.setId(ingredientCount);
        ingredientCount++;

        tmp.addView(ingredient);
        tmp.addView(amount);
        ingredientLayout.addView(tmp);


        Button addingredient = findViewById(R.id.addingredientButton);
        addingredient.setOnClickListener(addingredientClick);
    }
    private Button.OnClickListener addingredientClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            final LinearLayout tmp = new LinearLayout(upload.this);
            tmp.setOrientation(LinearLayout.HORIZONTAL);

            Button dele = new Button(upload.this);
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

            EditText ingredient = new EditText(upload.this);
            ingredient.setId(ingredientCount);
            ingredientCount++;
            ingredient.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            EditText amount = new EditText(upload.this);
            amount.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            amount.setId(ingredientCount);

            tmp.addView(ingredient);
            tmp.addView(amount);
            ingredientLayout.addView(tmp);
            ingredientCount++;
        }
    };
    public void step(){
        stepLayout = findViewById(R.id.stepLayout);
        TextView stepnumber = new TextView(upload.this);
        stepnumber.setText(stepCount+"、");
        stepnumber.setTextSize(14);
        stepLayout.addView(stepnumber);

        EditText tmp = new EditText(upload.this);
        tmp.setTextSize(14);
        tmp.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tmp.setHint("例：熱鍋");
        tmp.setId(stepIDCount);
        stepLayout.addView(tmp);
        stepCount++;
        stepIDCount++;

        Button stepButton = findViewById(R.id.addstepButton);
        stepButton.setOnClickListener(addstepClick);
    }
    private Button.OnClickListener addstepClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView stepnumber = new TextView(upload.this);
            stepnumber.setText(stepCount+"、");
            stepnumber.setTextSize(14);
            stepLayout.addView(stepnumber);

            EditText tmp = new EditText(upload.this);
            tmp.setTextSize(14);
            tmp.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            tmp.setId(stepIDCount);
            tmp.setTag(stepnumber);
            System.out.println("get Tag: "+tmp.getTag());
            stepLayout.addView(tmp);
            stepCount++;
        }
    };

    public void enterClick(View v) {

        if (nameEdit.getText().equals("")){
            Toast.makeText(getApplicationContext(),"請輸入食譜名稱",Toast.LENGTH_SHORT).show();
        }else {
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
            for (int x = 100;x<=stepIDCount; x++){
                String EditTextID = String.valueOf(x);
                int resID = getResources().getIdentifier(EditTextID, "id", getPackageName());
                EditText tmp =  findViewById(resID);
                try{
                    step_arr.add(tmp.getText().toString());
                }catch (NullPointerException e){

                }
            }
            HashTagHelper mEditTextHashTagHelper;
            final EditText tag_input = findViewById(R.id.tag_input);
            mEditTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimaryDark), null);
            mEditTextHashTagHelper.handle(tag_input);

            List<String> allHashTags = mEditTextHashTagHelper.getAllHashTags();
            hashtag_arr = new ArrayList<>(allHashTags);
            EditText personEdit = findViewById(R.id.personEdit);
            person = "["+personEdit.getText()+"人份]";
            mThreadHandler.post(ImageuploadThread);

        }

    }
    private String getExtension(Uri Uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(Uri));
    }
    Runnable ImageuploadThread = new Runnable() {
        @Override
        public void run() {
            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(upload.this,"上傳中", Toast.LENGTH_SHORT).show();
            }else {
                Imageuploader();
            }
        }
    };
    public void Imageuploader(){
        try {
            final StorageReference Ref = mStorageRef.child("image"+mImageUri.getLastPathSegment());

            Ref.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            image = uri.toString();
                            mThreadHandler.post(UploadDatabase);
                        }
                    });
                }
            });

        }catch (NullPointerException e){
            image = "";
            mThreadHandler.post(UploadDatabase);
        }

    }
    Runnable UploadDatabase = new Runnable() {
        @Override
        public void run() {
            recipedetail.put("ingredients",ingredient_arr);
            recipedetail.put("amount",amount_arr);
            recipedetail.put("steps",step_arr);
            recipedetail.put("name",nameEdit.getText().toString());
            recipedetail.put("information",inforEdit.getText().toString());
            recipedetail.put("count",0);
            recipedetail.put("image",image);
            recipedetail.put("hashtag",hashtag_arr);
            recipedetail.put("person", person);

            db.collection("MASArecipe")
                    .add(recipedetail)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            docID = documentReference.getId();
                            Thread t1 = new Thread(collectionRunnable);
                            t1.start();
                            MainActivity.getID = docID;

                            Intent intent = new Intent();
                            intent.setClass(upload.this, receipt1.class);
                            Toast.makeText(getApplicationContext(), "上傳完成",Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }
                    });
        }
    };

    Runnable collectionRunnable = new Runnable() {
        @Override
        public void run() {
            DocumentReference likeRef = db.collection(MainActivity.UID).document("collection");
            likeRef.update("all", FieldValue.arrayUnion(docID));
        }
    };
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blank_menu, menu);
        return true;
    }
    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(upload.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(upload.this, timer.class);
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
            intent.setClass(upload.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(upload.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(upload.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(upload.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(upload.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(upload.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(upload.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(upload.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(upload.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
