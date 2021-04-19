package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class collection extends AppCompatActivity {
    static boolean flush = false;
    List<String> collection_list;
    classificationDialog classificationDialog;
    addClassificationDialog addClassificationDialog;
    FirebaseFirestore db;
    LinearLayout classificationLayout;
    TextView username;
    Button loginButton;
    Toolbar toolbar;
    private Handler mThreadHandler;
    private HandlerThread mThread;

    RecyclerView mRecyclerView;
    collection.MyListAdapter myListAdapter;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    String CurrentClassification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu6);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setTitle("我的收藏");
        toolbar.inflateMenu(R.menu.collection_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.addClassificationButton:
                        classificationDialog = new classificationDialog(collection.this);
                        classificationDialog.show();
                        classificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Intent intent = new Intent();
                                intent.setClass(collection.this, collection.class);
                                startActivity(intent);
                            }
                        });
                        return true;
                    case R.id.search:
                        Intent home_intent = new Intent();
                        home_intent.setClass(collection.this, search.class);
                        startActivity(home_intent);
                        return true;
                }
                return false;
            }
        });

        // 用toolbar做為APP的ActionBar
        setSupportActionBar(toolbar);

        classificationLayout = findViewById(R.id.classificationLayout);

        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(run);
        mThreadHandler.post(classificationRunnable);
        mThreadHandler.post(loginRunnable);

        mRecyclerView = findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(collection.this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(collection.this, DividerItemDecoration.VERTICAL));

        myListAdapter = new collection.MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);
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
                        service_intent.setClass(collection.this , service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(collection.this , MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(collection.this);
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
        }
    }
    Runnable classificationRunnable = new Runnable() {
        @Override
        public void run() {
            CurrentClassification = "all";
            db.collection(MainActivity.UID).document("collection")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final DocumentSnapshot document = task.getResult();
                                for (Object key : document.getData().keySet()) {
                                    final Object subcollection = key;
                                    if (!key.toString().equals("all")){

                                        final Button tmp = new Button(collection.this);
                                        tmp.setText(subcollection.toString());
                                        tmp.setBackgroundColor(Color.TRANSPARENT);
                                        tmp.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CurrentClassification = subcollection.toString();
                                                collection_list = new ArrayList<>();
                                                myListAdapter.clear();
                                                try {
                                                    collection_list = (List<String>) document.getData().get(subcollection.toString());
                                                }catch (NullPointerException e){
                                                    collection_list = new ArrayList<>();
                                                }
                                                collectionDraw();
                                            }
                                        });
                                        tmp.setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View v) {
                                                new AlertDialog.Builder(collection.this)
                                                        .setTitle("刪除"+subcollection.toString()+"分類嗎?")
                                                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Map<String,Object> data = new HashMap<>();
                                                                data.put(subcollection.toString(), FieldValue.delete());
                                                                db.collection(MainActivity.UID).document("collection")
                                                                        .update(data)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(getApplicationContext(), "移除成功",Toast.LENGTH_SHORT).show();
                                                                                Intent intent = new Intent();
                                                                                intent.setClass(collection.this, collection.class);
                                                                                startActivity(intent);
                                                                            }
                                                                        });
                                                            }
                                                        }).setNegativeButton("取消", null).create()
                                                        .show();
                                                return false;
                                            }
                                        });
                                        classificationLayout.addView(tmp);
                                    }else {
                                        Button allButton = findViewById(R.id.allButton);
                                        allButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                collection_list = new ArrayList<>();
                                                myListAdapter.clear();
                                                CurrentClassification = "all";
                                                collection_list = (List<String>) document.getData().get(subcollection.toString());
                                                collectionDraw();
                                            }
                                        });
                                        if (collection_list != null){
                                            collection_list.clear();
                                            myListAdapter.clear();
                                        }
                                        collection_list = (List<String>) document.getData().get(subcollection.toString());
                                        collectionDraw();
                                    }
                                }
                            }
                        }
                    });
        }
    };
    public void collectionDraw(){
        if (collection_list == null){
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("Id", "");
            hashMap.put("name", "此分類還沒有食譜喔!");
            hashMap.put("ingredients", "");
            hashMap.put("image", "http://i.imgur.com/kyVfpYh.png");
            arrayList.add(hashMap);
            myListAdapter.notifyDataSetChanged();
        }else {
            for(Object tmp:collection_list){
                db.collection("MASArecipe").document(String.valueOf(tmp))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
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
                                    }
                                    myListAdapter.notifyDataSetChanged();
                                }
                            }
                        });

            }
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
                    intent.setClass(collection.this, recognition.class);
                    startActivity(intent);
                }
            }
        }
    }

    private class MyListAdapter extends RecyclerView.Adapter<collection.MyListAdapter.ViewHolder>{
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
        public collection.MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.all_receipt_list_options,parent,false);
            return new collection.MyListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull collection.MyListAdapter.ViewHolder holder, int position) {
            final int tmpPosition = position;
            holder.reName.setText(arrayList.get(position).get("name"));
            holder.reIngredient.setText(arrayList.get(position).get("ingredients"));
            Glide.with(collection.this)
                    .load(arrayList.get(position).get("image"))
                    .into(holder.reImg);

            holder.reName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.getID = arrayList.get(tmpPosition).get("Id");
                    Intent intent = new Intent(v.getContext(), receipt1.class);
                    v.getContext().startActivity(intent);
                }
            });
            holder.reName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (CurrentClassification.equals("all")){
                        addClassificationDialog = new addClassificationDialog(collection.this);
                        addClassificationDialog.show();
                        addClassificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Intent intent = new Intent();
                                intent.setClass(collection.this, collection.class);
                                startActivity(intent);
                            }
                        });
                        addClassificationDialog.recipeID = arrayList.get(tmpPosition).get("Id");
                    }else {
                        new AlertDialog.Builder(collection.this)
                                .setTitle("自本分類刪除該食譜嗎?")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        collection_list.remove(arrayList.get(tmpPosition).get("Id"));
                                        Map<String,Object> data = new HashMap<>();
                                        data.put(CurrentClassification, collection_list);
                                        db.collection(MainActivity.UID).document("collection")
                                                .update(data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getApplicationContext(), "移除成功",Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent();
                                                        intent.setClass(collection.this, collection.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                    }
                                }).setNegativeButton("取消", null).create()
                                .show();
                    }
                    return false;
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
            holder.reIngredient.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (CurrentClassification.equals("all")){
                        addClassificationDialog = new addClassificationDialog(collection.this);
                        addClassificationDialog.show();
                        addClassificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Intent intent = new Intent();
                                intent.setClass(collection.this, collection.class);
                                startActivity(intent);
                            }
                        });
                        addClassificationDialog.recipeID = arrayList.get(tmpPosition).get("Id");
                    }else {
                        new AlertDialog.Builder(collection.this)
                                .setTitle("自本分類刪除該食譜嗎?")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        collection_list.remove(arrayList.get(tmpPosition).get("Id"));
                                        Map<String,Object> data = new HashMap<>();
                                        data.put(CurrentClassification, collection_list);
                                        db.collection(MainActivity.UID).document("collection")
                                                .update(data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getApplicationContext(), "移除成功",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).setNegativeButton("取消", null).create()
                                .show();
                    }
                    return false;
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
            holder.reImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (CurrentClassification.equals("all")){
                        addClassificationDialog = new addClassificationDialog(collection.this);
                        addClassificationDialog.show();
                        addClassificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Intent intent = new Intent();
                                intent.setClass(collection.this, collection.class);
                                startActivity(intent);
                            }
                        });
                        addClassificationDialog.recipeID = arrayList.get(tmpPosition).get("Id");
                    }else {
                        new AlertDialog.Builder(collection.this)
                                .setTitle("自本分類刪除該食譜嗎?")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        collection_list.remove(arrayList.get(tmpPosition).get("Id"));
                                        Map<String,Object> data = new HashMap<>();
                                        data.put(CurrentClassification, collection_list);
                                        db.collection(MainActivity.UID).document("collection")
                                                .update(data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getApplicationContext(), "移除成功",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).setNegativeButton("取消", null).create()
                                .show();
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
        public void clear() {
            int size = arrayList.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    arrayList.remove(0);
                }
                notifyItemRangeRemoved(0, size);
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.collection_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent();
                intent.setClass(collection.this, search.class);
                startActivity(intent);
                return true;
            case R.id.addClassificationButton:
                classificationDialog = new classificationDialog(collection.this);
                classificationDialog.show();
                classificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Intent intent = new Intent();
                        intent.setClass(collection.this, collection.class);
                        startActivity(intent);
                    }
                });
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void photoClick(View v) {
        Intent intent = new Intent();
        intent.setClass(collection.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(collection.this, timer.class);
        startActivity(intent);
    }
    public void loginClick(View v) {
        Intent intent = new Intent();
        intent.setClass(collection.this, login.class);
        startActivity(intent);
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(collection.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(collection.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(collection.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(collection.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(collection.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(collection.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(collection.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
