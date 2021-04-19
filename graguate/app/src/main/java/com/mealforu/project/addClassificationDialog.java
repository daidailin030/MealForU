package com.mealforu.project;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class addClassificationDialog extends AlertDialog implements View.OnClickListener {
    GridLayout gridLayout;
    Button mBtnCancel, mBtnConnect;
    Context mContext;

    public static String recipeID;
    String classificationName;
    List<String> collection_list;
    FirebaseFirestore db;

    public addClassificationDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addclassificationdialog);

        db = FirebaseFirestore.getInstance();
        gridLayout = findViewById(R.id.gridLayout);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        this.setCancelable(false);

        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);

        Thread t1 = new Thread(classificationLoad);
        t1.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            default:
                break;

        }
    }

    Runnable classificationLoad = new Runnable() {
        @Override
        public void run() {
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
                                        Button tmp = new Button(getContext());
                                        tmp.setText(subcollection.toString());
                                        tmp.setBackgroundColor(Color.TRANSPARENT);
                                        tmp.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                try {
                                                    collection_list = (List<String>) document.get(subcollection.toString());
                                                    collection_list.add(recipeID);
                                                }catch (NullPointerException e){
                                                    collection_list = new ArrayList<>();
                                                    collection_list.add(recipeID);
                                                }
                                                classificationName = subcollection.toString();
                                                AddtoClassification();
                                            }
                                        });
                                        gridLayout.addView(tmp);
                                    }
                                }
                            }
                        }
                    });
        }
    };
    public void AddtoClassification(){

        db.collection(MainActivity.UID).document("collection")
                .update(classificationName, collection_list);
        Toast.makeText(getContext(), "加入成功", Toast.LENGTH_SHORT).show();

        this.dismiss();
    }
}
