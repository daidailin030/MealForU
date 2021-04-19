package com.mealforu.project;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.fonts.Font;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class receipt1 extends AppCompatActivity {
    public static boolean like = false;
    ImageButton likeButton;
    FirebaseFirestore db;
    List<String> collection_list, hashtag_arr;
    int countFromDB;
    String imageUri;
    static Boolean switchPref;
    ImageView photoView;
    TextView resourceText;
    Toolbar toolbar;
    public static boolean add=false;
    TextView username;
    Button loginButton;
    private Handler mUI_Handler = new Handler();
    private Handler mThreadHandler;
    private HandlerThread mThread;

    TextView tag_input_Text;
    EditText tag_input_Edit;

    Map<String, Integer> historyhashtag_list, historyview_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu3);

        System.out.println("recipeID: "+MainActivity.getID);
        resourceText = findViewById(R.id.resourceText);
        likeButton = findViewById(R.id.likeButton);
        photoView = findViewById(R.id.photoView);

        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);

        db = FirebaseFirestore.getInstance();
        try {
            if (Integer.valueOf(MainActivity.getID)<500){
                resourceText.setText("資料來源：MASAの料理ABC");
            }
        }catch (NumberFormatException e){

        }


        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(run);
        mThreadHandler.post(contentRunnable);
        mThreadHandler.post(loginRunnable);

        if (MainActivity.login_status){
            mThreadHandler.post(collectionCheck);
        }

        tag_input_Text = findViewById(R.id.tag_input_Text);
        tag_input_Edit = findViewById(R.id.tag_input_Edit);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setLogo(R.drawable.mealforu);
        toolbar.setTitle("食譜");

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
                        service_intent.setClass(receipt1.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(receipt1.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(receipt1.this);
                        break;
                }
                return true;
            }
        });
    }

    Runnable contentRunnable = new Runnable() {
        @Override
        public void run() {
            content();
        }
    };
    public void content(){
        DocumentReference docRef = db.collection("MASArecipe").document(String.valueOf(MainActivity.getID));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        TextView nameText = findViewById(R.id.nameText);
                        String name = document.get("name")+"";
                        nameText.setText(name);
                        nameText.setTextSize(30);

                        final TextView inforText = findViewById(R.id.inforText);
                        final Button more=findViewById(R.id.more);
                        final Button re=findViewById(R.id.re);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                inforText.setMaxLines(3);
                                more.setText("更多...");
                                more.setOnClickListener(new View.OnClickListener(){

                                    @Override
                                    public void onClick(View view) {
                                        inforText.setMaxLines(20);
                                        more.setVisibility(View.INVISIBLE);
                                        more.setClickable(false);
                                        re.setVisibility(View.VISIBLE);
                                        re.setClickable(true);
                                    }
                                });
                                re.setOnClickListener(new View.OnClickListener(){

                                    @Override
                                    public void onClick(View view) {
                                        inforText.setMaxLines(3);
                                        more.setVisibility(View.VISIBLE);
                                        more.setClickable(true);
                                        re.setVisibility(View.INVISIBLE);
                                        re.setClickable(false);
                                    }
                                });
                            }
                        });



                        TextPaint paint =inforText.getPaint();
                        paint.setFakeBoldText(true);
                        String infor = document.get("information")+"";
                        TextView personText = findViewById(R.id.personText);
                        if(!document.get("person").toString().equals("Null")){
                            personText.setText(document.get("person")+"");
                        }

                        String counttmp = String.valueOf(document.get("count"));
                        countFromDB = Integer.valueOf(counttmp);

                        Thread t4 = new Thread(countUpdate);
                        t4.start();

                        if (infor.equals("null")){
                            inforText.setText("");
                        }else {
                            inforText.setText(infor);
                        }

                        try{
                            imageUri = document.get("image").toString();
                            if (imageUri.matches("^https.+")){
                                Glide.with(receipt1.this)
                                        .load(imageUri)
                                        .into(photoView);
                            }else{
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                StorageReference photoReference= storageReference.child(imageUri);

                                final long ONE_MEGABYTE = 1024 * 1024;
                                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        photoView.setImageBitmap(bmp);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }catch(NullPointerException e){

                        }

                        hashtag_arr = (List<String>)document.get("hashtag");
                        if (hashtag_arr != null){
                            hashtag();
                        }


                        LinearLayout ingredientLayout = findViewById(R.id.ingredientLayout);
                        List<String> ingredient_arr = (List<String>)document.get("ingredients");
                        List<String> amount_arr = (List<String>) document.get("amount");
                        TextView ingredients[] = new TextView[ingredient_arr.size()];
                        TextView amount[] = new TextView[amount_arr.size()];
                        for(int i = 0; i<ingredient_arr.size(); i++){
                            LinearLayout tmpLL = new LinearLayout(receipt1.this);
                            tmpLL.setOrientation(LinearLayout.HORIZONTAL);
                            ingredients[i] = new TextView(receipt1.this);
                            ingredients[i].setText(ingredient_arr.get(i));
                            ingredients[i].setTextSize(20);
                            for(String ing : search.search_arr){
                                if(ingredient_arr.get(i).equals(ing)){
                                    ingredients[i].setTextColor(Color.rgb(255,0,0));
                                }
                            }
                            ingredients[i].setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            tmpLL.addView(ingredients[i]);
                            amount[i] = new TextView(receipt1.this);
                            amount[i].setText(amount_arr.get(i));
                            amount[i].setTextSize(20);
                            amount[i].setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                            tmpLL.addView(amount[i]);
                            tmpLL.setId(i);
                            ingredientLayout.addView(tmpLL);
                        }
                        LinearLayout stepLayout = findViewById(R.id.stepLayout);
                        List<String> step_arr = (List<String>)document.get("steps");
                        for(int i = 0; i<step_arr.size(); i++){
                            TextView steps = new TextView(receipt1.this);
                            steps.setId(i);
                            steps.setText(step_arr.get(i).replaceAll("[\\[\\]\\s]",""));
                            steps.setTextSize(20);
                            steps.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            stepLayout.addView(steps);
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
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

                recipehashtag.hashtag = hashTag;
                if (MainActivity.login_status) {
                    if (historyhashtag_list.containsKey(recipehashtag.hashtag)) {
                        int hashtagcount = Integer.valueOf(String.valueOf(historyhashtag_list.get(recipehashtag.hashtag))) + 1;
                        historyhashtag_list.remove(recipehashtag.hashtag);
                        historyhashtag_list.put(recipehashtag.hashtag, hashtagcount);
                    } else {
                        historyhashtag_list.put(recipehashtag.hashtag, 1);
                    }
                }
                Thread t1 = new Thread(historyWrite);
                t1.start();

                Intent intent = new Intent();
                intent.setClass(receipt1.this, recipehashtag.class);
                startActivity(intent);
            }
        });
        mTextHashTagHelper.handle(tag_input_Text);
    }
    Runnable collectionCheck = new Runnable() {
        @Override
        public void run() {
            db.collection(UID).document("collection")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    collection_list = (List<String>) document.get("all");
                                    if (collection_list.contains(MainActivity.getID)){
                                        like = true;
                                        likeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
                                    }else {
                                        like = false;
                                    }
                                } else {
                                    like = false;
                                    collection_list = new ArrayList<>();
                                }

                            }else{
                                Log.d("recipe1", "get failed with ", task.getException());
                            }
                        }
                    });

            mThreadHandler.post(historyUpdate);
        }
    };

    public void addlikeClick(View v) {
        if(MainActivity.login_status){
            if(like){
                //dele
                likeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));

                DocumentReference likeRef = db.collection(UID).document("collection");
                likeRef.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final DocumentSnapshot document = task.getResult();
                                    for (Object key : document.getData().keySet()) {
                                        final Object subcollection = key;
                                        try {
                                            collection_list = (List<String>) document.getData().get(subcollection.toString());
                                            if (collection_list.contains(MainActivity.getID)){
                                                collection_list.remove(MainActivity.getID);

                                                Map<String, Object> data = new HashMap<>();
                                                data.put(subcollection.toString(), collection_list);

                                                db.collection(UID).document("collection")
                                                        .update(data);

//                                                    deleFromClassification();
                                            }
                                        }catch (ClassCastException e){
                                        }
                                    }
                                }
                            }
                        });
                Toast.makeText(getApplicationContext(), "移除收藏", Toast.LENGTH_SHORT).show();
                like = false;
            }else{
                //add
                collection_list.add(MainActivity.getID);
                Map<String, Object> data = new HashMap<>();
                data.put("all", collection_list);

                likeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
                DocumentReference likeRef = db.collection(UID).document("collection");
                likeRef.update(data);

                Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                like = true;
            }
            collection.flush = true;
        }else{
            new AlertDialog.Builder(receipt1.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(receipt1.this,login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消",null)
                    .show();
        }
    }
    Runnable countUpdate = new Runnable() {
        @Override
        public void run() {
            countFromDB++;
            System.out.println("count: "+countFromDB);
            db.collection("MASArecipe").document(MainActivity.getID)
                    .update("count",countFromDB);
        }
    };
    Runnable historyUpdate = new Runnable() {
        @Override
        public void run() {
            db.collection(UID).document("history")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            historyhashtag_list = (Map<String, Integer>)document.get("hashtag");
                            historyview_list = (Map<String, Integer>)document.get("view");
                            if (hashtag_arr != null){
                                for (String hashtagtmp : hashtag_arr){
                                    if (historyhashtag_list.containsKey(hashtagtmp)){
                                        int hashtagcount = Integer.valueOf(String.valueOf(historyhashtag_list.get(hashtagtmp))) + 1;
                                        historyhashtag_list.remove(hashtagtmp);
                                        historyhashtag_list.put(hashtagtmp, hashtagcount);
                                    }else{
                                        historyhashtag_list.put(hashtagtmp, 1);
                                    }
                                }
                            }

                            if (historyview_list.containsKey(MainActivity.getID)){
                                int viewcount = Integer.valueOf(String.valueOf(historyview_list.get(MainActivity.getID))) + 1;
                                historyview_list.remove(MainActivity.getID);
                                historyview_list.put(MainActivity.getID, viewcount);
                            }else {
                                System.out.println("getID: "+MainActivity.getID);
                                historyview_list.put(MainActivity.getID, 1);
                                System.out.println("historyview_list: "+historyview_list);
                            }
                            mThreadHandler.post(historyWrite);
                        }
                    });
        }
    };
    Runnable historyWrite = new Runnable() {
        @Override
        public void run() {
            if(MainActivity.login_status) {
                db.collection(UID).document("history")
                        .update("hashtag", historyhashtag_list);
                db.collection(UID).document("history")
                        .update("view", historyview_list);
            }

        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    MainActivity.uri = result.getUri();
                    Intent intent = new Intent();
                    intent.setClass(receipt1.this, recognition.class);
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
        intent.setClass(receipt1.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(receipt1.this, timer.class);
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
            UID = getSharedPreferences("login", MODE_PRIVATE)
                    .getString("PREF_USERID", "");
            loginButton.setText("登入");

            Intent intent = new Intent();
            intent.setClass(receipt1.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(receipt1.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(receipt1.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(receipt1.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(receipt1.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(receipt1.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(receipt1.this, collection.class);
            startActivity(intent);
        }else{
            new AlertDialog.Builder(receipt1.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(receipt1.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
