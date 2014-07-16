package com.chronocloud.update.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;


/**
 * Created by peter
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 弹出提示信息
     *
     * @param msg 信息
     */
    public void makeToast(CharSequence msg) {
        makeToast(msg, Toast.LENGTH_SHORT);
    }

    /**
     * 弹出提示信息
     *
     * @param msg      信息
     * @param duration 弹出时间长度
     */
    public void makeToast(CharSequence msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    /**
     * 弹出提示信息
     *
     * @param resId 资源ID
     */
    public void makeToast(int resId) {
        makeToast(resId, Toast.LENGTH_SHORT);
    }

    /**
     * 弹出提示信息
     *
     * @param resId    资源ID
     * @param duration 弹出时间长度
     */
    public void makeToast(int resId, int duration) {
        Toast.makeText(this, resId, duration).show();
    }


}
