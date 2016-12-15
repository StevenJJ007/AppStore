package com.anyonavinfo.cpadstore.utils;

import com.anyonavinfo.cpadstore.entity.AppInfo;

public class CommonData {
	
	public static final int TYPE_GETAPPLIST = 101;
	
	public static final String RESULTCODE_ERROR = "400";

	public static AppInfo appInfo = null;

	public static final String BROADCAST_ACTION_UNSTALL = "com.anyonavinfo.cpadstore.broadcast.action.unstall";
	public static final String BROADCAST_ACTION_UPDATE = "com.anyonavinfo.cpadstore.broadcast.action.update";
}
