package com.mealforu.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class TestPagerActivity extends AppCompatActivity {

    private ViewPager2 testPager;//宣告一個ViewPager2物件
    private List<Integer> pagerList;//宣告一個數字陣列(用來設定你的頁面數量的)
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pager);
        mTabLayout = findViewById(R.id.tablayout);

        mTabLayout.addTab(mTabLayout.newTab().setText("Tab0"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Tab1"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                testPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        // 註冊頁面變化的回調接口
        testPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mTabLayout.setScrollPosition(position,0,false);
            }
        });
    }
}
