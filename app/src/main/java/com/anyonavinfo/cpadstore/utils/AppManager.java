package com.anyonavinfo.cpadstore.utils;

import java.util.List;
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

/**
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 */
public class AppManager {

	private static Stack<Activity> activityStack;
	private static AppManager instance;

	private AppManager() {
	}

	/**
	 * 单一实例
	 */
	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}

	/**
	 * 添加Activity到堆栈
	 * 自娱自乐的方法？没用到的啊
	 */
	public void getSize(String name) {
		System.out.println("---" + name + "--" + activityStack.size());
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		Stack<Activity> activitys = new Stack<Activity>();
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				activitys.add(activity);
			}
		}

		for (Activity activity : activitys) {
			finishActivity(activity);
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		for (int i = 0, size = activityStack.size(); i < size; i++) {
			if (null != activityStack.get(i)) {
				activityStack.get(i).finish();
			}
		}
		activityStack.clear();
	}


	// 获得version版本code
	public static int getVersionCode(Context context, String packageName) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			int version = info.versionCode;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// 获得version版本code
	public static String getVersionName(Context context, String packageName) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "找不到版本name";
		}
	}

	// 获得app图标
	public static Drawable getAppIcon(Context context, String packageName) {
		try {
			PackageManager manager = context.getPackageManager();
			ApplicationInfo info = manager.getApplicationInfo(packageName, 0);
			return info.loadIcon(manager);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	//检查app是否安装
	public static boolean AppIsExist(Context context, String packageName) {
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			if (p.versionName == null) {
				continue;
			}
			if (p.packageName.equalsIgnoreCase(packageName)) {
				return true;
			}
		}
		return false;
	}
	public static boolean taskIsRunning(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(packageName)
					&& info.baseActivity.getPackageName().equals(packageName)) {
				return true;
			}

		}
		return false;
	}
}
