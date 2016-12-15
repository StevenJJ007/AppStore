package com.anyonavinfo.cpadstore.manageFragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.anyonavinfo.cpadstore.entity.AppInfo;

import java.util.ArrayList;

/**
 * Created by zza on 2016/6/21.
 * 读取CPCAD中的apps.db
 */
public class MyContentResolver {
    public static Uri uri = Uri.parse("content://com.anyonavinfo.pcpad.Apps/appinfo");
    private Context context;

    public MyContentResolver(Context context) {
        this.context=context;
    }

    public ArrayList<AppInfo> queryData(){
        ArrayList<AppInfo> appInfos=new ArrayList<AppInfo>();
        Cursor cursor= context.getContentResolver().query(uri,null,null,null,null);
        Log.i("测试", "queryData2 cursor="+cursor.getCount());
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            AppInfo appInfo=new AppInfo();
            appInfo.packageName = cursor.getString(cursor.getColumnIndex("apppackagename"));
            Log.i("测试", "queryData,packageName=" + appInfo.packageName);
            if (appInfo.packageName!=null){
                appInfos.add(appInfo);//陈仲勇的数据有个空值，放进去会代码中断
            }
        }
        cursor.close();
        cursor=null;
        return appInfos;
    }
}
