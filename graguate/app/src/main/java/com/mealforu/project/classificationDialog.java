package com.mealforu.project;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class classificationDialog extends AlertDialog implements View.OnClickListener {
    EditText mInputEdit;
    Button mBtnCancel, mBtnConnect;
    Context mContext;

    FirebaseFirestore db;

    public classificationDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classification);

        db = FirebaseFirestore.getInstance();
        mInputEdit = findViewById(R.id.inputEdit);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        this.setCancelable(false);

        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);

        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_connect:
                if (TextUtils.isEmpty(mInputEdit.getText())) {
                    Toast.makeText(mContext, "請輸入類別名稱", Toast.LENGTH_SHORT).show();
                } else {
                    List<String> tmpList = new ArrayList<>();
                    Map<String, Object> data = new HashMap<>();
                    data.put(mInputEdit.getText().toString(), tmpList);
                    db.collection(MainActivity.UID).document("collection")
                            .set(data, SetOptions.merge());

                    this.dismiss();
                }
                break;
            default:
                break;

        }
    }
}
