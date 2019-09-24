package com.jzxl.ocs_plugin.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.jzxl.ocs_plugin.R;

/**
 * @author liqingsong
 * @version 1.0
 * @date 2016/8/24
 * @Description
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public Context mContext;
    protected Toolbar toolbar;
    protected FrameLayout baseContent;

    protected LayoutInflater mLayoutInflater;
    protected FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mLayoutInflater = LayoutInflater.from(mContext);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        fragmentManager = getSupportFragmentManager();
        initView();
        initVariable();
        initData();
        initEvent();
    }

    protected void initView() {
        setContentView(R.layout.activity_base);
        baseContent = (FrameLayout) findViewById(R.id.activity_base_content);
        initToolBar();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
    }

    protected void initVariable() {
    }

    protected void initData() {
    }


    protected void initEvent() {
    }

    protected void processClick(View v) {
    }


    protected void initToolBar() {
        toolbar = findViewById(R.id.common_toolbar_suppport);
        toolbar.setTitle("个人信息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * @date 2016/8/24
     * @author liqingsong
     * @methodName 捕获其返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        processClick(view);
    }

    /**
     * 打开一个结束当前页面的Activity
     *
     * @param pClass
     */
    protected void openFinishActivity(Class<?> pClass) {
        Intent intent = new Intent();
        intent.setClass(mContext, pClass);
        startActivity(intent);
        finish();
    }

    /**
     * 打开一个结束当前页面的Activity
     *
     * @param pClass
     */
    protected void openActivity(Class<?> pClass) {
        Intent intent = new Intent();
        intent.setClass(mContext, pClass);
        startActivity(intent);
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     */
    public void hideInputWindow(Context context) {
        if (context == null) {
            return;
        }
        final View v = ((Activity) context).getWindow().peekDecorView();
        if (v != null && v.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}
