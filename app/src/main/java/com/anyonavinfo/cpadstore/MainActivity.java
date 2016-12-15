package com.anyonavinfo.cpadstore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyonavinfo.cpadstore.adapter.MFragmentPagerAdapter;
import com.anyonavinfo.cpadstore.entity.AppInfo;
import com.anyonavinfo.cpadstore.update.UpdateServiceHelper;
import com.anyonavinfo.cpadstore.utils.HttpUtils;

import java.util.List;

import static android.R.attr.key;


public class MainActivity extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    public static List<AppInfo> AppInfoList;
    private ImageButton exitApp;
    private RelativeLayout refresh;
    private TextView tvApp;
    private TextView tvManage;
    private ViewPager mViewPager;
    private NetState receiver;
    private Handler mHandler;


    int selectTextViewIndex = 0;
    private long exitTime = 0;

    private MFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initViews();
        //加载fragment
        adapter = new MFragmentPagerAdapter(this.getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(this);

        receiver = new NetState();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        /**  功能代码  */

        //设置监听
        setListener();
    }

    /**
     * 初始化控件，同时让应用界面默认显示
     */
    private void initViews() {
        exitApp = (ImageButton) findViewById(R.id.exitapp);
        tvApp = (TextView) findViewById(R.id.app);
        tvManage = (TextView) findViewById(R.id.manage);
        mViewPager = (ViewPager) findViewById(R.id.fragment_applist);

        setTextViewSelect(tvApp);//让应用按钮被选中
/*
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d("update","come in");
                if (HttpUtils.isConnected(getApplicationContext())) {
                    Log.d("update1","come in");
                    new UpdateServiceHelper(getApplicationContext()).checkServiceUpdate();
                }
            }
        }, 1000);*/

        new UpdateServiceHelper(MainActivity.this).checkServiceUpdate();
    }

    private void setListener() {
        exitApp.setOnClickListener(this);
        tvApp.setOnClickListener(this);
        tvManage.setOnClickListener(this);

    }

    /**
     * 管理fragment按钮状态
     *
     * @param view 是界面上面的管理和应用
     *             先默认让应用被选中
     */
    private void setTextViewSelect(View view) {
        tvApp.setSelected(false);
        tvManage.setSelected(false);
        view.setSelected(true);
    }

    /**
     * 按钮监听功能
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.exitapp) {
            finish();
            System.exit(0);
        }
        if (v.getId() == R.id.app) {
            //应用界面
            setTextViewSelect(tvApp);
            selectTextViewIndex = 0;
        }
        if (v.getId() == R.id.manage) {
            //管理界面
            setTextViewSelect(tvManage);
            selectTextViewIndex = 1;
        }
        mViewPager.setCurrentItem(selectTextViewIndex);
    }

    /**
     * viewpager监听
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                setTextViewSelect(tvApp);
                break;
            case 1:
                setTextViewSelect(tvManage);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 物理键退出APP
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            Toast.makeText(getApplicationContext(), "sss", Toast.LENGTH_SHORT).show();
            new UpdateServiceHelper(this).hideProgressView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class NetState extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                //Toast.makeText(MainActivity.this, "net is available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "已断开连接，请检查网络", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
