package com.mealforu.project;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReceiptPagerAdapter extends FragmentStateAdapter {
    public ReceiptPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position){
            case 0:
                return new RecommandReceiptFragment();
            default:
                return new HotReceiptFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
