package com.mealforu.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.mealforu.project.MainActivity.UID;

public class recipehashtag extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static String hashtag = "";

    RecyclerView mRecyclerView;
    recipehashtag.MyListAdapter myListAdapter;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    Toolbar toolbar;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    TextView username;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu18);

        mRecyclerView = findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(recipehashtag.this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(recipehashtag.this, DividerItemDecoration.VERTICAL));
        myListAdapter = new recipehashtag.MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);

        Thread t1 = new Thread(run);
        t1.start();
        Thread t2 = new Thread(Load);
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
        toolbar.setTitle("#"+hashtag);

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
                        service_intent.setClass(recipehashtag.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(recipehashtag.this , MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(recipehashtag.this);
                        break;
                }
                return true;
            }
        });
    }
    Runnable Load = new Runnable() {
        @Override
        public void run() {
            db.collection("MASArecipe")
                    .whereArrayContainsAny("hashtag", Arrays.asList(hashtag))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String imageUri;
                                    try {
                                        imageUri = document.get("image").toString();
                                    } catch (NullPointerException e) {
                                        imageUri = "http://i.imgur.com/kyVfpYh.png";
                                    }
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id", document.getId());
                                    hashMap.put("name", document.get("name").toString());
                                    hashMap.put("ingredients", document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                    hashMap.put("image",imageUri);
                                    arrayList.add(hashMap);
                                    i++;
                                }
                                if (i == 0) {
                                    System.out.println("Recipe Not Found.");
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("Id", "0");
                                    hashMap.put("name", "查無食譜");
                                    hashMap.put("ingredients", "");
                                    hashMap.put("image","http://i.imgur.com/kyVfpYh.png");
                                    arrayList.add(hashMap);
                                }
                                myListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
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
                    intent.setClass(recipehashtag.this, recognition.class);
                    startActivity(intent);
                }
            }
        }
    }

    private class MyListAdapter extends RecyclerView.Adapter<recipehashtag.MyListAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView reName, reIngredient;
            private ImageView reImg;
            private LinearLayout linearLayout;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                reName = itemView.findViewById(R.id.receipt_name_for_list);
                reIngredient = itemView.findViewById(R.id.receipt_ingredient_for_list);
                linearLayout = itemView.findViewById(R.id.this_receipt);
                reImg = itemView.findViewById(R.id.receipt_pic_in_list);
            }
        }

        @NonNull
        @Override
        public recipehashtag.MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.all_receipt_list_options,parent,false);
            return new recipehashtag.MyListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull recipehashtag.MyListAdapter.ViewHolder holder, int position) {
            final int tmpPosition = position;
            holder.reName.setId(Integer.valueOf(arrayList.get(position).get("Id")));
            holder.reName.setText(arrayList.get(position).get("name"));
            holder.reIngredient.setText(arrayList.get(position).get("ingredients"));
            holder.reIngredient.setId(Integer.valueOf(arrayList.get(position).get("Id")));
            Glide.with(recipehashtag.this)
                    .load(arrayList.get(position).get("image"))
                    .into(holder.reImg);
            holder.reImg.setId(Integer.valueOf(arrayList.get(position).get("Id")));

            holder.reName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.getID = arrayList.get(tmpPosition).get("Id");
                    Intent intent = new Intent(v.getContext(), receipt1.class);
                    v.getContext().startActivity(intent);
                }
            });
            holder.reIngredient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.getID = arrayList.get(tmpPosition).get("Id");
                    Intent intent = new Intent(v.getContext(), receipt1.class);
                    v.getContext().startActivity(intent);
                }
            });
            holder.reImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.getID = arrayList.get(tmpPosition).get("Id");
                    Intent intent = new Intent(v.getContext(), receipt1.class);
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blank_menu, menu);
        return true;
    }
}
