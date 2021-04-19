package com.mealforu.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {
    @NonNull
    private RecommandReceiptFragment mContext;
    private ArrayList<RecommendAdapter.Post> mData = null;
    public RecommendAdapter(RecommandReceiptFragment recommandReceiptFragment, ArrayList<RecommendAdapter.Post> data) {
        this.mContext = recommandReceiptFragment;
        this.mData = data;
    }

    @Override
    public RecommendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_receipt_list_options, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendAdapter.ViewHolder holder, int position) {
        final RecommendAdapter.Post post = mData.get(position);
        holder.reName.setText(post.posterName);
        holder.reIngredient.setText(post.content);
        Glide.with(mContext)
                .load(post.posterThumbnailUrl)
                .into(holder.reImag);
        holder.reImag.setVisibility(post.content == null ?
                View.GONE : View.VISIBLE);

        holder.reName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getID = post.recipeID;
                Intent intent = new Intent(v.getContext(), receipt1.class);
                v.getContext().startActivity(intent);
            }
        });
        holder.reIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getID = post.recipeID;
                Intent intent = new Intent(v.getContext(), receipt1.class);
                v.getContext().startActivity(intent);
            }
        });
        holder.reImag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getID = post.recipeID;
                Intent intent = new Intent(v.getContext(), receipt1.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView reName;
        private TextView reIngredient;
        private ImageView reImag;
        private LinearLayout linearLayout;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            reName = itemView.findViewById(R.id.receipt_name_for_list);
            reIngredient = itemView.findViewById(R.id.receipt_ingredient_for_list);
            reImag = itemView.findViewById(R.id.receipt_pic_in_list);
            linearLayout = itemView.findViewById(R.id.this_receipt);
        }
    }
    public static class Post {

        public String posterName;
        public String posterThumbnailUrl;
        public String content;
        public String recipeID;

        public Post(String posterName, String posterThumbnailUrl, String content, String recipeID) {
            this.posterName = posterName;
            this.posterThumbnailUrl = posterThumbnailUrl;
            this.content = content;
            this.recipeID = recipeID;
        }
    }
}
