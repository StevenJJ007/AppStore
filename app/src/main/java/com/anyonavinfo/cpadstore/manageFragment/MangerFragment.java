package com.anyonavinfo.cpadstore.manageFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyonavinfo.cpadstore.R;
import com.anyonavinfo.cpadstore.entity.AppInfo;
import com.anyonavinfo.cpadstore.utils.CommonData;
import com.anyonavinfo.cpadstore.utils.HttpApi;
import com.anyonavinfo.cpadstore.utils.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by navinfo-21 on 2016/6/6.
 */
public class MangerFragment extends Fragment {
    View view;
    private ArrayList<AppInfo> appInfoList;//网络请求回来的app数据
    private ArrayList<AppInfo> tempAppInfos;//当前安装的app数据
    private ArrayList<AppInfo> child1;//需要更新的app数据
    private ArrayList<AppInfo> child2;//可以卸载的app数据
    private ArrayList<AppInfo> cpadAppInfos;//CPad中保存的app数据
    private ArrayList<ParentInfo> parentInfos;//一级列表数据信息列表
    private HashMap<String, ArrayList<AppInfo>> mapData;//二级数据信息集合
    private ExpandableListView exListView;
    private MyExpandAdapter expandableAdapter;
    private InnerReceiver receiver;
    private ImageView imageView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    expandableAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    tempAppInfos = new InstallAppManager(getActivity(), appInfoList).getInstallAppInfo();
                    if (tempAppInfos == null) {
                        tempAppInfos = new ArrayList<>();
                    }
                    /**
                     * 二级列表第一组数据：更新的数据：
                     * 本地已安装的，和网络获取的appinfos比较，包名相同的且版本号网络大于本地的数据，添加到child1
                     */
                    child1 = new ArrayList<>();
                    for (int i = 0; i < tempAppInfos.size(); i++) {
                        for (int j = 0; j < appInfoList.size(); j++) {
                            if ((tempAppInfos.get(i).packageName).equals(appInfoList.get(j).packageName) && Integer.parseInt(tempAppInfos.get(i).oldappVersionCode) <
                                    Integer.parseInt(appInfoList.get(j).appVersionCode)) {
                                /** 网络appinfo的appVersionCode、appVersionName 赋值给本地的appinfo
                                 * 然后把筛选出来的数据放到第一组数据中*/

                                tempAppInfos.get(i).appVersionCode = appInfoList.get(j).appVersionCode;
                                tempAppInfos.get(i).appVersionName = appInfoList.get(j).appVersionName;
                                tempAppInfos.get(i).appSize = appInfoList.get(j).appSize;
                                tempAppInfos.get(i).appDownloadUrl = appInfoList.get(j).appDownloadUrl;
                                child1.add(tempAppInfos.get(i));
                            }
                        }
                    }
                    /**
                     * 二级列表第二组数据：卸载的数据
                     * 网络请求的不成功时候，读取CPad中的value,
                     * 目前网络数据不全，先强制读取CPad中的value
                     */
                    if (appInfoList == null) {
                        cpadAppInfos = new MyContentResolver(getActivity()).queryData();
                        child2 = new InstallAppManager(getActivity(), cpadAppInfos).getInstallAppInfo();
                    } else {
                        child2 = tempAppInfos;
                    }
                    /** 屏蔽卸载内容 */
                    for (int i = child2.size() - 1; i >= 0; i--) {
                        if (child2.get(i).getPackageName().contains("navinfo")
                                || child2.get(i).getPackageName().equals("com.wedrive.welink.sgmw.navigation")
                                || child2.get(i).getPackageName().equals("com.wedrive.wecar.volation.sgmw")
                                || child2.get(i).getPackageName().equals("com.wedrive.wecar.computer.sgmw")
                                || child2.get(i).getPackageName().contains("com.autopet.hardware.aidl")
                                || child2.get(i).getPackageName().contains("com.wedrive.wecar.dianping.map")
                                ) {
                            Log.i("getPackageName", "getPackageName=" + child2.get(i).packageName);
                            child2.remove(i);
                        }
                    }

                    /** 把二级数据放到map中传给MyExpandAdapter*/
                    mapData.put(parentInfos.get(0).title, child1);//更新管理
                    mapData.put(parentInfos.get(1).title, child2);//已安装管理
                    expandableAdapter = new MyExpandAdapter(getActivity(), mapData, parentInfos, exListView);
                    exListView.setAdapter(expandableAdapter);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        Log.i("Fragment", "onActivityCreated");
        // 注册广播接收者
        receiver = new InnerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);//卸载
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);//安装
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);//更新
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {

            view = View.inflate(getActivity(), R.layout.manage_fragment, null);
            //初始化控件
            setViews();
            //加载数据
            initInfo();
            //设置监听
            addListener();
            new InstallAppManager(getActivity(), appInfoList).getInstallAppInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void setViews() {
        exListView = (ExpandableListView) view.findViewById(R.id.lv_manage);
        imageView = (ImageView) view.findViewById(R.id.wrong);
    }

    private void initInfo() {
        if (!HttpUtils.isConnected(getActivity())) {
            imageView.setVisibility(View.VISIBLE);
        }
        new NewsAsyncTask().execute(HttpApi.URL_MAIN);
        /**
         * 一级列表数据
         */
        parentInfos = ParentInfo.getParentInfos();
        mapData = new HashMap<String, ArrayList<AppInfo>>();

    }

    private void addListener() {

    }

    private class InnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取Intent中的Action
            String action = intent.getAction();
            Log.i("测试", "onReceive,action=" + action);

            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {//卸载
                String packageName = intent.getData().getSchemeSpecificPart();
                Log.i("测试", "ACTION_PACKAGE_REMOVED+packageName=" + packageName);
                for (int i = 0; i < child2.size(); i++) {
                    if ((child2.get(i).getPackageName()).equals(packageName)) {
                        child2.remove(i);
//                        Log.i("测试", "ACTION_PACKAGE_REMOVED+remove=" + i);
                    }
                }
                for (int i = 0; i < child1.size(); i++) {
                    if ((child1.get(i).getPackageName()).equals(packageName)) {
                        child1.remove(i);
                    }
                }

                //为了界面效果自然些，添加了延时0.5秒更新二级listview
                handler.sendEmptyMessageDelayed(1, 500);
                /** 再发一条广播，更新应用界面的显示效果，
                 * 另外cPad后续也要同步管理数据，通过接受广播来处理
                 * */
                Intent unstallIntent = new Intent();
                unstallIntent.setAction(CommonData.BROADCAST_ACTION_UNSTALL);
                unstallIntent.putExtra("packageName", packageName);
//                Log.d("测试-----", packageName);
                getActivity().sendBroadcast(unstallIntent);
            }

            if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {//更新
                String packageName = intent.getData().getSchemeSpecificPart();
                Log.i("测试", "ACTION_PACKAGE_REPLACED=" + packageName);

                for (int i = 0; i < child1.size(); i++) {
                    if ((child1.get(i).getPackageName()).equals(packageName)) {
                        child1.remove(i);
                    }
                }
                //为了界面效果自然些，添加了延时0.5秒更新二级listview
                handler.sendEmptyMessageDelayed(1, 500);
                /** 再发一条广播，更新应用界面的显示效果，
                 * 另外cPad后续也要同步管理数据，通过接受广播来处理
                 * */
                Intent updateIntent = new Intent();
                updateIntent.setAction(CommonData.BROADCAST_ACTION_UPDATE);
                updateIntent.putExtra("packageName", packageName);
                getActivity().sendBroadcast(updateIntent);
            }

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {//安装
                String packageName = intent.getData().getSchemeSpecificPart();
//                Log.i("测试", "ACTION_PACKAGE_ADDED+packageName=" + packageName);
                if (InstallAppManager.mFlag(packageName, appInfoList)) {//当新安装的是要管理的app
                    AppInfo tmpInfo = new AppInfo();
                    PackageManager manager = getActivity().getPackageManager();
                    try {
                        PackageInfo packageInfo = manager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                        tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
                        tmpInfo.setPackageName(packageName);
                        tmpInfo.setOldappVersionName(packageInfo.versionName);
                        tmpInfo.setOldappVersionCode(String.valueOf(packageInfo.versionCode));
                        tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());

                        if (!(tmpInfo.getPackageName().contains("navinfo"))
                                && !(tmpInfo.getPackageName().equals("com.wedrive.welink.sgmw.navigation"))
                                && !(tmpInfo.getPackageName().equals("com.wedrive.wecar.volation.sgmw"))
                                && !(tmpInfo.getPackageName().equals("com.wedrive.wecar.computer.sgmw"))
                                && !(tmpInfo.getPackageName().equals("com.wedrive.wecar.dianping.map"))
                                && !(tmpInfo.getPackageName().equals("com.autopet.hardware.aidl"))
                                ) {
                            child2.add(tmpInfo);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                expandableAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.i("Fragment", "onDestroy");
        getActivity().unregisterReceiver(receiver);
        receiver = null;
        mapData.clear();
        mapData = null;

    }

    class NewsAsyncTask extends AsyncTask<String, Void, ArrayList<AppInfo>> {

        @Override
        protected ArrayList<AppInfo> doInBackground(String... params) {
            appInfoList = HttpUtils.getJsonData(handler);
            Log.d("mini", appInfoList.toString());
            return appInfoList;
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> appInfos) {
            super.onPostExecute(appInfos);
            if (appInfos.size() != 0) {
                for (int i = appInfos.size() - 1; i >= 0; i--) {
                    //去掉service和应用商店本身
                    if (appInfos.get(i).getPackageName().contains("com.anyonavinfo.cpadstore")
                            || appInfos.get(i).getPackageName().contains("com.autopet.hardware.aidl")) {
                        Log.d("login", "getPackageName=" + appInfos.get(i).packageName);
                        appInfos.remove(i);
                    }
                }
                handler.sendEmptyMessage(2);
            }
        }
    }
}