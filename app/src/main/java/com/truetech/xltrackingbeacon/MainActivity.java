package com.truetech.xltrackingbeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.truetech.xltrackingbeacon.Utils.Util;
import com.truetech.xltrackingbeacon.adapter.TabsAdapter;
import com.truetech.xltrackingbeacon.fragments.StatusFragment;
import com.truetech.xltrackingbeacon.service.TrackerService;

import java.util.List;

import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.ARRAY_PERMISSIONS;
import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.PERMISSION_ALL;
import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.checkAllPermissions;
import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.hasPermissions;
import static com.truetech.xltrackingbeacon.Utils.Constant.DEF_VALUE_NULL;
import static com.truetech.xltrackingbeacon.Utils.Constant.LIMIT_TRY_CONNECT;
import static com.truetech.xltrackingbeacon.Utils.Util.getStringFromPref;
import static com.truetech.xltrackingbeacon.Utils.Util.isGPSEnable;
import static com.truetech.xltrackingbeacon.Utils.Util.setIntFromPref;

public class MainActivity extends AppCompatActivity  {

    private BroadcastReceiver broadcastReceiver;
    private static TabLayout tabLayout;
    private  ViewPager viewPager;
    private  int pagerLastPosition;
    private static StatusFragment statusFragment;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        tabLayout =  findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_settings)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_status)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_connection)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = findViewById(R.id.view_pager);
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(tabsAdapter);
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                    statusFragment =(StatusFragment) getSupportFragmentManager().getFragments().get(0);
                    updateStatusScreen();
                if(tab.getPosition() == 0 && getStringFromPref(R.string.key_block_password_setting) == null){
                    viewPager.setCurrentItem(tab.getPosition());
                    pagerLastPosition = tab.getPosition();
                } else if (tab.getPosition() == 0 && !getStringFromPref(R.string.key_block_password_setting).equals("")){
                    checkPassword();
                } else {
                    viewPager.setCurrentItem(tab.getPosition());
                    pagerLastPosition = tab.getPosition();
                }
                if (Util.SERVICE_ACTIVATE)
                    tabLayout.getTabAt(2).setText(getString(R.string.btn_disconnect));
                else tabLayout.getTabAt(2).setText(getString(R.string.tab_connection));
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void checkPassword() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.password_dialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = promptsView
                .findViewById(R.id.editTextDialogPassword);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(this.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(getStringFromPref(R.string.key_block_password_setting).equals(userInput.getText().toString())) {
                                    viewPager.setCurrentItem(0);
                                    pagerLastPosition = 0;
                                } else {
                                    viewPager.setCurrentItem(pagerLastPosition);
                                    tabLayout.getTabAt(pagerLastPosition).select();
                                    Toast.makeText(MainActivity.this,getText(R.string.wrong_data),Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setPositiveButton(this.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                tabLayout.getTabAt(pagerLastPosition).select();
                                viewPager.setCurrentItem(pagerLastPosition);
                            }

                        }

                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void  init(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_REBOOT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, intentFilter);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (hasPermissions(context,ARRAY_PERMISSIONS) && Util.SERVICE_ACTIVATE) {
                    context.startService(new Intent(context, TrackerService.class));
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
        String title = getString(R.string.title_location_permission);
        String message = getString(R.string.text_location_permission);
        String butOk = getString(R.string.ok);
        if (checkAllPermissions(this, PERMISSION_ALL, title, message, butOk, ARRAY_PERMISSIONS)) {
            beginApp();
        }
    }

    private void beginApp(){
        setContentView(R.layout.activity_main);
        setIntFromPref(LIMIT_TRY_CONNECT, DEF_VALUE_NULL);
        if (!isGPSEnable()){
            String title = getString(R.string.title_enable_gps);
            String message = getString(R.string.text_enable_gps);
            String butOk = getString(R.string.ok);
            Util.showDialog(this,title,message,butOk,null);
        }
    }


    public static void updateStatusScreen(){
        statusFragment.updateScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    public static void updateTab(Context context){
        if (Util.SERVICE_ACTIVATE) tabLayout.getTabAt(2).setText(context.getString(R.string.btn_disconnect));
        else tabLayout.getTabAt(2).setText(context.getString(R.string.tab_connection));
    }


}
