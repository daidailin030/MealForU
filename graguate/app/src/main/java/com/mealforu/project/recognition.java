package com.mealforu.project;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.List;

import static com.mealforu.project.MainActivity.UID;

public class recognition extends AppCompatActivity {

    FirebaseVisionImageLabeler labeler; //For running the image labeler
    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder; // Which option is use to run the labeler local or remotely
    ProgressDialog progressDialog; //Show the progress dialog while model is downloading...
    FirebaseModelDownloadConditions conditions; //Conditions to download the model
    FirebaseVisionImage image; // preparing the input image
    EditText EditText; // Displaying the label for the input image
    ImageView selectImageView; // To select the image from device
    public static String ch;
    String [] an = new String[100];
    private FirebaseAutoMLLocalModel localModel;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    TextView username;
    Button loginButton;
    Toolbar toolbar;
    addnoteDialog addnoteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu11);

        EditText = findViewById(R.id.EditText);
        selectImageView = findViewById(R.id.selectImageView);
        progressDialog = new ProgressDialog(recognition.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        Thread t1 = new Thread(run);
        t1.start();
        selectImageView.setImageURI(MainActivity.uri); //set image in imageview
        EditText.setText(""); //so that previous text don't get append with new one
        setLabelerFromLocalModel(MainActivity.uri);
        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(loginRunnable);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MealForU");
        toolbar.setNavigationIcon(R.drawable.mealforu_opt);
        toolbar.setTitleMarginStart(120);
        toolbar.inflateMenu(R.menu.recognition_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.addNote:
                        if (MainActivity.login_status){
                            addnoteDialog = new addnoteDialog(recognition.this);
                            addnoteDialog.show();
                        }else {
                            new AlertDialog.Builder(recognition.this)
                                    .setTitle("請先登入")
                                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setClass(recognition.this, login.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("cancel", null).create()
                                    .show();
                        }

                        return true;
                    case R.id.search:
                        search.ActivityFrom = "recognition";
                        Intent home_intent = new Intent();
                        home_intent.setClass(recognition.this, search.class);
                        startActivity(home_intent);
                        return true;
                }
                return false;
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
    Runnable run = new Runnable() {
        @Override
        public void run() {
            BottomNavigationView();
        }
    };
    public void BottomNavigationView() {
        final BottomNavigationView bn = findViewById(R.id.bottom_navigation);
        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(recognition.this, service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(recognition.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(recognition.this);
                        break;
                }
                return true;
            }
        });
    }

    private void setLabelerFromLocalModel(Uri uri) {
        localModel = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("model/manifest.json")
                .build();
        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.0f)
                            .build();
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            image = FirebaseVisionImage.fromFilePath(recognition.this, uri);
            processImageLabeler(labeler, image);
        } catch (FirebaseMLException | IOException e) {
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
                    selectImageView.setImageURI(MainActivity.uri); //set image in imageview
                    EditText.setText(""); //so that previous text don't get append with new one
                    setLabelerFromLocalModel(MainActivity.uri);
                } else
                    progressDialog.cancel();
            } else
                progressDialog.cancel();
        }
    }

    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                          @Override
                                          public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                              progressDialog.cancel();
                                              int i=1;
                                              for (FirebaseVisionImageLabel label: labels) {
                                                  if(i==1) {
                                                      String text = label.getText();
                                                      if (text.equals("black fungus")){
                                                          ch="黑木耳";
                                                      }else if(text.equals("greenonion")){
                                                          ch="蔥";
                                                      }else if(text.equals("carrot")){
                                                          ch="紅蘿蔔";
                                                      }else if(text.equals("mushroom")){
                                                          ch="香菇";
                                                      }else if(text.equals("milk")){
                                                          ch="牛奶";
                                                      }else if(text.equals("onion")){
                                                          ch="洋蔥";
                                                      }else if(text.equals("butter")){
                                                          ch="奶油";
                                                      }else if(text.equals("cod")){
                                                          ch="鱈魚";
                                                      }else if(text.equals("egg")){
                                                          ch="雞蛋";
                                                      }else if(text.equals("cucumber")){
                                                          ch="小黃瓜";
                                                      }else if(text.equals("snapper")){
                                                          ch="鯛魚";
                                                      }else if(text.equals("garlic")){
                                                          ch="大蒜";
                                                      }else if(text.equals("tofu")){
                                                          ch="豆腐";
                                                      }else if(text.equals("tomato")){
                                                          ch="番茄";
                                                      }else if(text.equals("potato")){
                                                          ch="馬鈴薯";
                                                      }

                                                      EditText.append(ch);
                                                      i++;
                                                  }
                                              }
                                          }
                                      });

    }
    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(recognition.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(recognition.this, timer.class);
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
            intent.setClass(recognition.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(recognition.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(recognition.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(recognition.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(recognition.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(recognition.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(recognition.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(recognition.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(recognition.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
