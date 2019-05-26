package com.exuberant.egws_admin.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.fragments.MapFragment;
import com.exuberant.egws_admin.fragments.UserControlFragment;

import lib.kingja.switchbutton.SwitchMultiButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Fragment mapFragment = new MapFragment();
        final Fragment userFragment = new UserControlFragment();
        addFragment(mapFragment);
        addFragment(userFragment);
        ((SwitchMultiButton)findViewById(R.id.smb_fragment_type)).setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                switch (position){
                    case 0 : addFragment(userFragment);
                    break;
                    case 1 : popFragment();
                    break;
                }
            }
        });
    }

    void addFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().add(R.id.view_pager, fragment).addToBackStack("Admin").commit();
    }

    void popFragment(){
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAfterTransition();
    }

}
