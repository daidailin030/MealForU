package com.mealforu.project;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mealforu.project.MainActivity.UID;

public class RecommandReceiptFragment extends Fragment {
    private RecyclerView recyclerView;
    public static ArrayList<RecommendAdapter.Post> data;
    RecommendAdapter recommendAdapter;

    private Handler mThreadHandler;
    private HandlerThread mThread;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    FirebaseFirestore db;
    Map<String, Integer> hashtag_list, keyword_list, view_list;

    public RecommandReceiptFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragment_recommand_receipt, container, false);
        db = FirebaseFirestore.getInstance();

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        if (MainActivity.login_status){
            mThreadHandler.post(userPerferedRunnable);
        }else {
            mThreadHandler.post(nonlogin);
        }


        recyclerView = root.findViewById(R.id.recommend_receipt_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        return root;
    }

    Runnable userPerferedRunnable = new Runnable() {
        @Override
        public void run() {
            db.collection(MainActivity.UID).document("history")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            hashtag_list = (Map<String, Integer>)document.get("hashtag");
                            keyword_list = (Map<String, Integer>)document.get("keyword");
                            view_list = (Map<String, Integer>)document.get("view");

                            hashtag_list = sortByValue(hashtag_list);
                            keyword_list = sortByValue(keyword_list);
                            view_list = sortByValue(view_list);

                            mThreadHandler.post(t1);
                        }
                    });

        }
    };
    Runnable t1 = new Runnable() {
        @Override
        public void run() {
            List<String> hashtag_indexes = new ArrayList<>(hashtag_list.keySet());
            db.collection("MASArecipe")
                    .whereArrayContainsAny("hashtag", Arrays.asList(hashtag_indexes.get(0)))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    double recommendPercent = 0;

                                    recommendPercent += Integer.valueOf(document.get("count").toString())*0.1;
                                    for (String tmp : (List<String>)document.get("ingredients")) {
                                        if (keyword_list.containsKey(tmp)) {
                                            recommendPercent += ((1/keyword_list.size())*0.6);
                                        }
                                    }

                                    if (view_list.containsKey(document.getId())){
                                        recommendPercent += 0.3;
                                    }

                                    String imageUri;
                                    try {
                                        imageUri = document.get("image").toString();
                                    } catch (NullPointerException e) {
                                        imageUri = "http://i.imgur.com/kyVfpYh.png";
                                    }

                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id",document.getId());
                                    hashMap.put("name",document.get("name").toString());
                                    hashMap.put("ingredients",document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                    hashMap.put("recommendPercent", String.valueOf(recommendPercent));
                                    hashMap.put("image", imageUri);
                                    arrayList.add(hashMap);

                                }
                                Collections.sort (arrayList, new RecommandReceiptFragment.MyMapComparator());
                                for (int x = 0; x<arrayList.size(); x++){
                                    RecommandReceiptFragment.data.add(new RecommendAdapter.Post(arrayList.get(x).get("name"), arrayList.get(x).get("image"), arrayList.get(x).get("ingredients"), arrayList.get(x).get("Id")));
                                }

                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }

                            recommendAdapter = new RecommendAdapter(RecommandReceiptFragment.this, data);
                            recyclerView.setAdapter(recommendAdapter);
                        }
                    });
        }
    };
    Runnable nonlogin = new Runnable() {
        @Override
        public void run() {
            db.collection("MASArecipe")
                    .limit(25)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String imageUri;
                                    try {
                                        imageUri = document.get("image").toString();
                                    } catch (NullPointerException e) {
                                        imageUri = "http://i.imgur.com/kyVfpYh.png";
                                    }

                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("Id",document.getId());
                                    hashMap.put("name",document.get("name").toString());
                                    hashMap.put("ingredients",document.get("ingredients").toString().replaceAll("[\\[\\]]",""));
                                    hashMap.put("image", imageUri);
                                    arrayList.add(hashMap);
                                }
                                for (int x = 0; x<arrayList.size(); x++){
                                  RecommandReceiptFragment.data.add(new RecommendAdapter.Post(arrayList.get(x).get("name"), arrayList.get(x).get("image"), arrayList.get(x).get("ingredients"), arrayList.get(x).get("Id")));
                                }

                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }

                            recommendAdapter = new RecommendAdapter(RecommandReceiptFragment.this, data);
                            recyclerView.setAdapter(recommendAdapter);
                        }
                    });
        }
    };
    public class MyMapComparator implements Comparator<Map <String, String>>{
        @Override
        public int compare (Map<String, String> o1, Map<String, String> o2){
            return o1.get ("recommendPercent").compareTo(o2.get ("recommendPercent"));
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (Integer.valueOf(String.valueOf(o2.getValue())).compareTo(Integer.valueOf(String.valueOf(o1.getValue()))));
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}