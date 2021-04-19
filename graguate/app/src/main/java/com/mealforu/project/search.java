package com.mealforu.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mealforu.project.MainActivity.UID;

public class search extends AppCompatActivity implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String search = "", style = "";
    EditText searchEdit;
    Spinner spinner;
    public  static String[] search_arr= new String[100];
    CollectionReference collection;
    ProgressBar progressBar;
    public static String ActivityFrom="";
    public static boolean switch_status = true;
    Map<String, Integer> keyword_list;
    Toolbar toolbar;
    TextView username;
    Button loginButton;
    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    private Handler mThreadHandler;
    private HandlerThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);

        mRecyclerView = findViewById(R.id.recycleview);
        progressBar = findViewById(R.id.progressBar);
        searchEdit = findViewById(R.id.searchEdit);
        if(ActivityFrom.equals("notedetail")){
            searchEdit.setText(notedetail.SearchIngredient_arr.toString().replaceAll("[\\[\\]\\s]",""));
            ActivityFrom = "";
        }
        if (ActivityFrom.equals("recognition")){
            searchEdit.setText(recognition.ch);
            ActivityFrom = "";
        }
        else {
            ActivityFrom = "";
        }
        spinner = findViewById(R.id.spinner);
        final String[] lunch = {"份量", "1人份", "2人份", "4人份", "4~6人份"};
        ArrayAdapter<String> lunchList = new ArrayAdapter<>(search.this, android.R.layout.simple_spinner_dropdown_item, lunch);
        spinner.setAdapter(lunchList);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                style = adapterView.getSelectedItem().toString();
            }
            public void onNothingSelected(AdapterView arg0) {
            }
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setClickable(false);
        toolbar.setLogo(R.drawable.mealforu);
        toolbar.setTitle("搜尋");

        setSupportActionBar(toolbar);


        mRecyclerView = findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(search.this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(search.this, DividerItemDecoration.VERTICAL));
        myListAdapter = new MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);
        username = findViewById(R.id.username);
        loginButton = findViewById(R.id.loginButton);
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(run);
        mThreadHandler.post(loginRunnable);

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
    public void switchClick(View v){
        Button switchButton = findViewById(R.id.switchButton);
        if(com.mealforu.project.search.switch_status){
            com.mealforu.project.search.switch_status = false;
            switchButton.setText("食材查詢");
            SpannableString s = new SpannableString("請輸入食譜");
            searchEdit.setHint(s);
            searchEdit.setTag("name");
        }else{
            com.mealforu.project.search.switch_status = true;
            switchButton.setText("食譜查詢");
            SpannableString s = new SpannableString("請輸入食材");
            searchEdit.setHint(s);
            searchEdit.setTag("ingredient");
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

        bn.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_service:
                        Intent service_intent = new Intent();
                        service_intent.setClass(search.this, service.class);
                        startActivity(service_intent);
                        break;
                    case R.id.nav_home:
                        Intent home_intent = new Intent();
                        home_intent.setClass(search.this, MainActivity.class);
                        startActivity(home_intent);
                        break;
                    case R.id.nav_photo:
                        CropImage.activity().start(search.this);
                        break;
                }
                return true;
            }
        });
    }
    public void onClick(View v){
        MainActivity.getID = String.valueOf(v.getId());
        Intent intent2 = new Intent();
        intent2.setClass(search.this, receipt1.class);
        startActivity(intent2);
    }
    public void searchClick(View v){
        mRecyclerView.removeAllViews();
        arrayList.clear();
        collection = db.collection("MASArecipe");
        search = searchEdit.getText().toString();
        search_arr = search.split("[,\\s]");
        System.out.println("tag: "+searchEdit.getTag());

        if(searchEdit.getTag().equals("ingredient") && style.equals("份量")){
            mThreadHandler.post(IngredientNonPerson);
        }else if(searchEdit.getTag().equals("ingredient") && !style.equals("份量")){
            mThreadHandler.post(IngredientPerson);
        }else if(searchEdit.getTag().equals("name") && style.equals("份量")){
            mThreadHandler.post(NameNonPerson);
        }else if (searchEdit.getTag().equals("name") && !style.equals("份量")){
            mThreadHandler.post(NamePerson);
        }
    }
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView reName, reIngredient, reMatch, reAllIngredient;
            private LinearLayout linearLayout;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                reName = itemView.findViewById(R.id.receipt_name_for_list);
                reIngredient = itemView.findViewById(R.id.receipt_ingredient_for_list);
                reMatch = itemView.findViewById(R.id.receipt_match_in_list);
                linearLayout = itemView.findViewById(R.id.this_receipt);
                reAllIngredient = itemView.findViewById(R.id.all_ingredient_in_list);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_recyclerview,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final int tmpPosition = position;
            holder.reName.setText(arrayList.get(position).get("name"));
            holder.reIngredient.setText(arrayList.get(position).get("ingredients"));
            holder.reMatch.setText(arrayList.get(position).get("match"));
            holder.reAllIngredient.setText(arrayList.get(position).get("totalingredients"));

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
            holder.reMatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.getID = arrayList.get(tmpPosition).get("Id");
                    Intent intent = new Intent(v.getContext(), receipt1.class);
                    v.getContext().startActivity(intent);
                }
            });
            holder.reAllIngredient.setOnClickListener(new View.OnClickListener() {
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
    Runnable NamePerson = new Runnable() {
        @Override
        public void run() {
            collection.whereGreaterThanOrEqualTo("name",search)
                    .whereEqualTo("person","["+style+"]")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                int i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.get("name").toString().matches(".*"+search+".*")){
                                        List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("Id",document.getId());
                                        hashMap.put("name",document.get("name").toString());
                                        hashMap.put("ingredients",document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                        hashMap.put("match","");
                                        hashMap.put("totalingredients", ingredient_arr.size()+"");
                                        arrayList.add(hashMap);
                                        i++;
                                    }
                                }
                                if (i == 0) {
                                    System.out.println("Recipe Not Found.");
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id","0");
                                    hashMap.put("name","查無食譜");
                                    hashMap.put("ingredients","");
                                    arrayList.add(hashMap);
                                }
                                Collections.sort (arrayList, new MyMapComparator ());
                                myListAdapter.notifyDataSetChanged();
                            }else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    };
    Runnable NameNonPerson = new Runnable() {
        @Override
        public void run() {

            collection.whereGreaterThanOrEqualTo("name",search)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                int i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.get("name").toString().matches(".*"+search+".*")){
                                        List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("Id",document.getId());
                                        hashMap.put("name",document.get("name").toString());
                                        hashMap.put("ingredients",document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                        hashMap.put("match","");
                                        hashMap.put("totalingredients", ingredient_arr.size()+"");
                                        arrayList.add(hashMap);
                                        i++;
                                    }
                                }
                                if (i == 0) {
                                    System.out.println("Recipe Not Found.");
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id","0");
                                    hashMap.put("name","查無食譜");
                                    hashMap.put("ingredients","");
                                    hashMap.put("match", "");
                                    hashMap.put("totalingredients", "");
                                    arrayList.add(hashMap);
                                }
                                Collections.sort (arrayList, new MyMapComparator ());
                                myListAdapter.notifyDataSetChanged();
                            }else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    };
    Runnable IngredientPerson = new Runnable() {
        @Override
        public void run() {
            String person = "["+style+"]";
            collection.whereEqualTo("person",person)
                    .whereArrayContainsAny("ingredients", Arrays.asList(search_arr))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                int i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                    int ifmatch = 0;
                                    System.out.println(ingredient_arr.toString());
                                    for (String tmp : ingredient_arr){
                                        if (Arrays.asList(search_arr).contains(tmp)){
                                            ifmatch++;
                                        }
                                    }
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id",document.getId());
                                    hashMap.put("name",document.get("name").toString());
                                    hashMap.put("ingredients",document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                    hashMap.put("match",ifmatch+"");
                                    hashMap.put("totalingredients", ingredient_arr.size()+"");
                                    arrayList.add(hashMap);
                                    i++;

                                }
                                if (i == 0) {
                                    System.out.println("Recipe Not Found.");
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id","0");
                                    hashMap.put("name","查無食譜");
                                    hashMap.put("ingredients","");
                                    hashMap.put("match", "");
                                    hashMap.put("totalingredients", "");
                                    arrayList.add(hashMap);
                                }
                                Collections.sort (arrayList, new MyMapComparator ());
                                myListAdapter.notifyDataSetChanged();

                                mThreadHandler.post(keywordReadRunnable);
                            }else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }
    };
    Runnable IngredientNonPerson = new Runnable() {
        @Override
        public void run() {
            collection.whereArrayContainsAny("ingredients", Arrays.asList(search_arr))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
//                                System.out.println(Arrays.asList(search_arr));
                                int i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    List<String> ingredient_arr = (List<String>)document.get("ingredients");
                                    int ifmatch = 0;
                                    for (String tmp : ingredient_arr){
                                        if (Arrays.asList(search_arr).contains(tmp)){
                                            ifmatch++;
                                        }
                                    }
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id", document.getId());
                                    hashMap.put("name", document.get("name").toString());
                                    hashMap.put("ingredients", document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                    hashMap.put("match", ifmatch+"");
                                    hashMap.put("totalingredients", ingredient_arr.size()+"");
                                    arrayList.add(hashMap);
                                    i++;

                                }
                                if (i == 0) {
                                    System.out.println("Recipe Not Found.");
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id","0");
                                    hashMap.put("name","查無食譜");
                                    hashMap.put("ingredients","");
                                    hashMap.put("match", "");
                                    hashMap.put("totalingredients", "");
                                    arrayList.add(hashMap);
                                }
                                Collections.sort(arrayList, new MyMapComparator ());
                                myListAdapter.notifyDataSetChanged();
                                if (MainActivity.login_status){
                                    mThreadHandler.post(keywordReadRunnable);
                                }

                            }else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }
    };
    public class MyMapComparator implements Comparator<Map <String, String>>{
        @Override
        public int compare (Map<String, String> o1, Map<String, String> o2){
            int c, d;
            c = o2.get ("match").compareTo(o1.get ("match"));
            if (c != 0) {
                return  c;
            }
            return o1.get("totalingredients").compareTo(o2.get("totalingredients"));
        }
    }

    Runnable keywordReadRunnable = new Runnable() {
        @Override
        public void run() {
            db.collection(MainActivity.UID).document("history")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    keyword_list = (Map<String, Integer>) document.get("keyword");
                                    if (keyword_list == null){
                                        keyword_list = new HashMap<>();
                                    }
                                    for (String strtmp : search_arr) {
                                        if (keyword_list.containsKey(strtmp)) {
                                            int keywordcount = Integer.valueOf(String.valueOf(keyword_list.get(strtmp))) + 1;
                                            keyword_list.remove(strtmp);
                                            keyword_list.put(strtmp, keywordcount);
                                        } else {
                                            keyword_list.put(strtmp, 1);
                                        }
                                    }
                                    mThreadHandler.post(keywordWriteRunnable);
                                }

                            }else{
                                Log.d("recipe1", "get failed with ", task.getException());
                            }
                        }
                    });
        }
    };
    Runnable keywordWriteRunnable = new Runnable() {
        @Override
        public void run() {
            db.collection(MainActivity.UID).document("history")
                    .update("keyword",keyword_list);
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
                    intent.setClass(search.this, recognition.class);
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
        intent.setClass(search.this, photo.class);
        startActivity(intent);
    }

    public void timerClick(View v) {
        Intent intent = new Intent();
        intent.setClass(search.this, timer.class);
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
            intent.setClass(search.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent();
            intent.setClass(search.this, login.class);
            startActivity(intent);
        }
    }
    public void uploadClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(search.this, upload.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(search.this)
                    .setTitle("請先登入")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(search.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }

    public void commonClick(View v) {
        Intent intent = new Intent();
        intent.setClass(search.this, common.class);
        startActivity(intent);
    }
    public void likeClick(View v) {
        if (MainActivity.login_status) {
            Intent intent = new Intent();
            intent.setClass(search.this, collection.class);
            startActivity(intent);
        }
        if (!MainActivity.login_status) {
            new AlertDialog.Builder(search.this)
                    .setMessage("請先登入")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(search.this, login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", null).create()
                    .show();
        }
    }
}
