package com.truetech.xltrackingbeacon.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.truetech.xltrackingbeacon.fragments.ConnectionFragment;
import com.truetech.xltrackingbeacon.fragments.SettingsFragment;
import com.truetech.xltrackingbeacon.fragments.StatusFragment;

public class TabsAdapter  extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    public TabsAdapter(FragmentManager fm, int NoofTabs){
        super(fm);
        this.mNumOfTabs = NoofTabs;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                SettingsFragment settingsFragment = new SettingsFragment();
                return settingsFragment;
            case 1:
                StatusFragment statusFragment = new StatusFragment();
                return statusFragment;
            case 2:
                ConnectionFragment connectionFragment = new ConnectionFragment();
                return connectionFragment;
            default:
                return null;
        }
    }



}
