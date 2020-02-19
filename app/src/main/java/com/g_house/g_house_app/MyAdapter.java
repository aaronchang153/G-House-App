package com.g_house.g_house_app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyAdapter extends FragmentPagerAdapter {
    private Context myContext;
    int totalTabs;

    TableFragment tableFragment;
    ParameterFragment parameterFragment;
    LogFragment logFragment;

    public MyAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;

        tableFragment = new TableFragment();
        parameterFragment = new ParameterFragment();
        logFragment = new LogFragment();
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return tableFragment;
            case 1:
                return parameterFragment;
            case 2:
                return logFragment;
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
