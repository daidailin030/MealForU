package com.mealforu.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HotReceiptFragment extends Fragment {
    private RecyclerView recyclerView;
    public static ArrayList<HotAdapter.Post> data;
    HotAdapter hotAdapter;
    public HotReceiptFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragment_hot_receipt, container, false);

        recyclerView = root.findViewById(R.id.hot_receipt_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        hotAdapter = new HotAdapter(HotReceiptFragment.this,data);
        recyclerView.setAdapter(hotAdapter);
        return root;
    }

}