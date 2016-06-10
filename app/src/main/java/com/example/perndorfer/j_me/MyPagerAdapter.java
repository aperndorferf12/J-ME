package com.example.perndorfer.j_me;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Alexander on 03.06.2016.
 */
public class MyPagerAdapter extends FragmentPagerAdapter
{
    private List<Fragment> fragments;

    public MyPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        this.fragments=fragments;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return this.fragments.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0: return "Kontakte";
            case 1: return "Chats";
            default:
                return "";
        }
    }
}
