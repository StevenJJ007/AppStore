package com.anyonavinfo.cpadstore.appFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.anyonavinfo.cpadstore.MainActivity;
import com.anyonavinfo.cpadstore.R;
import com.anyonavinfo.cpadstore.adapter.AppAdapter;
import com.anyonavinfo.cpadstore.entity.AppInfo;
import com.anyonavinfo.cpadstore.utils.CommonData;
import com.anyonavinfo.cpadstore.utils.HttpApi;
import com.anyonavinfo.cpadstore.utils.HttpUtils;

import java.util.List;

/**
 * Created by navinfo-21 on 2016/6/6.
 */
public class AppFragment extends Fragment {
    View view;
    ListView listView;
    AppAdapter appAdapter;
    private MyReceiver myReceiver;
    private String receiverPackageName;
    private ProgressDialog proDialog;
    private ImageView imageView;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 8) {
                showRefreshDialog();
                proDialog.dismiss();
            }
        }
    };

    private void showRefreshDialog() {
        new AlertDialog.Builder(getActivity())
                .setMessage("网络状态不佳，请刷新重试")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestDialog();
                                new NewsAsyncTask().execute(HttpApi.URL_MAIN);
                            }
                        }).setNegativeButton("取消", null).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.app_fragment, null);
        setViews();
        addListener();
        try {
            if (!HttpUtils.isConnected(getActivity())) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                RequestDialog();
                new NewsAsyncTask().execute(HttpApi.URL_MAIN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void RequestDialog() {
        proDialog = ProgressDialog.show(getActivity(), "数据获取中...",
                "请稍后...", true, true);
    }


    private void addListener() {

    }

    private void setViews() {
        listView = (ListView) view.findViewById(R.id.app_listview);
        imageView = (ImageView) view.findViewById(R.id.wrong);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(myReceiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter(CommonData.BROADCAST_ACTION_UNSTALL);
        getActivity().registerReceiver(myReceiver, intentFilter);

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiverPackageName = intent.getExtras().getString("packageName");
            Log.d("receiverPackageName--->", receiverPackageName);

            new NewsAsyncTask().execute(HttpApi.URL_MAIN);
        }
    }


    class NewsAsyncTask extends AsyncTask<String, Void, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(String... params) {
            return HttpUtils.getJsonData(handler);
        }

        @Override
        protected void onPostExecute(List<AppInfo> beans) {
            super.onPostExecute(beans);
            if (beans.size() != 0) {
                for (int i = beans.size() - 1; i >= 0; i--) {
                    //去掉service和应用商店本身
                    if (beans.get(i).getPackageName().contains("com.anyonavinfo.cpadstore")
                            || beans.get(i).getPackageName().contains("com.autopet.hardware.aidl")) {
                        Log.d("login", "getPackageName=" + beans.get(i).packageName);
                        beans.remove(i);
                    }
                }
                appAdapter = new AppAdapter(getActivity(), beans);
                listView.setAdapter(appAdapter);
                proDialog.dismiss();

            } else {
            }
        }
    }
}


