package com.mealforu.project;

import android.app.AlertDialog;
import android.app.Person;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class addnoteDialog extends AlertDialog implements View.OnClickListener {
    GridView gridView;
    Button mBtnCancel, mBtnConnect;
    Context mContext;

    EditText amountEdit;
    FirebaseFirestore db;
    List<String> ingredient_list, amount_list;
    ArrayList<String> selected_arr = new ArrayList<>();

    String CurrentnoteID;

    public addnoteDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnotedialog);

        db = FirebaseFirestore.getInstance();
        gridView = findViewById(R.id.gridView);
        amountEdit = findViewById(R.id.amountEdit);
        gridView.setAdapter(new GridViewAdapter(getContext(), MainActivity.notename_list, MainActivity.noteID_list));
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        this.setCancelable(false);

        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               int count = gridView.getAdapter().getCount();
                                               for (int i = 0; i < count; i++) {
                                                   LinearLayout itemLayout = (LinearLayout) gridView.getChildAt(i);
                                                   CheckBox checkbox = itemLayout.findViewById(R.id.grid_item_checkbox);
                                                   if(checkbox.isChecked()){
                                                       selected_arr.add(checkbox.getTag().toString());
                                                   }
                                               }
                                               AddtoNote();
                                           }
                                       });

        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              dismiss();
                                          }
                                      });

//        mThread = new HandlerThread("name");
//        mThread.start();
//        mThreadHandler = new Handler(mThread.getLooper());
//        mThreadHandler.post(NoteLoad);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_connect:
                int count = gridView.getAdapter().getCount();
                for (int i = 0; i < count; i++) {
                    LinearLayout itemLayout = (LinearLayout) gridView.getChildAt(i);
                    CheckBox checkbox = itemLayout.findViewById(R.id.grid_item_checkbox);
                    if(checkbox.isChecked()){
                        selected_arr.add(checkbox.getTag().toString());
                    }
                }
                AddtoNote();
            default:
                break;

        }
    }
    public void AddtoNote(){
            if (!amountEdit.getText().toString().equals("") && !selected_arr.isEmpty()){
                System.out.println("select: "+selected_arr);
                for(final String tmp : selected_arr) {

                    db.collection(MainActivity.UID).document(tmp)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    final DocumentSnapshot document = task.getResult();
                                    ingredient_list = (List<String>) document.get("ingredients");
                                    amount_list = (List<String>) document.get("amount");
                                    ingredient_list.add(recognition.ch);
                                    amount_list.add(amountEdit.getText().toString());
                                    CurrentnoteID = tmp;
                                    System.out.println(CurrentnoteID+" Before "+ingredient_list+": "+amount_list);
                                    WriteDB();
                                }
                            });

                    this.dismiss();
                }
            }else{
                Toast.makeText(getContext(), "請輸入份量", Toast.LENGTH_SHORT).show();
            }

    }
    public void WriteDB(){
        db.collection(MainActivity.UID).document(CurrentnoteID)
                .update("ingredients", ingredient_list);
        db.collection(MainActivity.UID).document(CurrentnoteID)
                .update("amount", amount_list);
        System.out.println(CurrentnoteID+" After "+ingredient_list+": "+amount_list);
        Toast.makeText(getContext(), "完成加入", Toast.LENGTH_SHORT).show();
    }
    public class GridViewAdapter extends BaseAdapter {
        private Context context;
        private List <String> name_list, ID_list;
        public GridViewAdapter(Context c, List <String> name, List<String> ID){
            // TODO Auto-generated method stub
            context = c;
            name_list = name;
            ID_list = ID;
        }
        public int getCount() {
            // TODO Auto-generated method stub
            return name_list.size();
        }
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;

        }
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gridview_item, null);
            }

            String noteName = name_list.get(position);
            String noteID = ID_list.get(position);

            // CheckBox
            CheckBox Chkbox = (CheckBox) convertView.findViewById(R.id.grid_item_checkbox);
            Chkbox.setTag(noteID);
            Chkbox.setText(noteName);
            return convertView;
        }
    }
}
