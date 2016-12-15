package com.anyonavinfo.cpadstore.manageFragment;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.anyonavinfo.cpadstore.entity.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zza on 2016/6/12.
 * 获取已安装并需要管理的app信息
 */
public class InstallAppManager {

    private Context context;
    ArrayList<AppInfo> appInfos;

    public InstallAppManager(Context context,ArrayList<AppInfo> appInfos) {
        this.context=context;
        if (appInfos==null){
            this.appInfos=new ArrayList<AppInfo>();
        }else {
            this.appInfos=appInfos;
        }
    }

    /**
     * 获取本地非系统app信息，和网络返回数据比较
     * @return 返回要管理的并且已经安装的app信息
     */
    public ArrayList<AppInfo> getInstallAppInfo(){
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); //用来存储获取的应用信息数据
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);//非系统包信息

        for(int i=0;i<packages.size();i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo tmpInfo =new AppInfo();

            if (mFlag(packageInfo.packageName,appInfos)){

                tmpInfo.appName=packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();

                tmpInfo.packageName=packageInfo.packageName;

                tmpInfo.oldappVersionName=packageInfo.versionName;
                tmpInfo.oldappVersionCode=String.valueOf(packageInfo.versionCode);
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            if(packageInfo.packageName!=null&&tmpInfo.oldappVersionCode!=null){
                appList.add(tmpInfo);
            }
            }
        }
        return appList;
    }

    /**
     *
     * @param pageName 本地app包名
     * @param appInfos 网络app信息集合
     * @return true 需要管理的app已安装了，
     *
     **/
    public static boolean mFlag(String pageName,ArrayList<AppInfo> appInfos){
        for (int i = 0; i < appInfos.size(); i++) {
            boolean flag=pageName.equals(appInfos.get(i).getPackageName());
            if (flag==true){
                return flag;
            }
        }
        return false;
    }

}

