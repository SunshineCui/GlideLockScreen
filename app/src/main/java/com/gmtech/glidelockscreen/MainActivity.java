package com.gmtech.glidelockscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.gmtech.glidelockscreen.databinding.ActivityHomeBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private final  static  String TAG  = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();
    }

    private void initView() {
        binding.gmMain.setMoveListen(new GlideLockPagerLayout.MoveCallback() {
            @Override
            public void onTouchUp(boolean isHidden) {
                Log.d(TAG, "onTouchUp: "+isHidden);
            }

            @Override
            public void onPointerCount(int pointerCount) {
                Log.d(TAG, "onPointerCount: "+pointerCount);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (binding != null){
                    binding.gmMain.moveChild(0,false);
                }
            }
        }, 500);
        Log.d(TAG, "onPause: ");
    }
}