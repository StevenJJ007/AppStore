package com.anyonavinfo.cpadstore.entity;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AppInfo implements Serializable{
    public String appName;
    public String packageName;
    public String appSize;
    public String appVersionName;
    public String appVersionCode;
    public String oldappVersionName;
    public String oldappVersionCode;
    public String appIconUrl;
    public String appDownloadUrl;
    public String install;
    public boolean isDownloading = false;
    public String downloadPath;
    public boolean isComplete = false;


    /**
     * 2016-06-12增加app的图标，用于存储已安装的app的图标信息
     */
    public Drawable appIcon = null;

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getOldappVersionCode() {
        return oldappVersionCode;
    }

    public void setOldappVersionCode(String oldappVersionCode) {
        this.oldappVersionCode = oldappVersionCode;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getOldappVersionName() {
        return oldappVersionName;
    }

    public void setOldappVersionName(String oldappVersionName) {
        this.oldappVersionName = oldappVersionName;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }


    public AppInfo() {
        super();
    }

    public AppInfo(String appName, String packageName, String appSize, String appVersionName, String oldappVersionName, String appIconUrl, String appDownloadUrl, String install) {
        super();
        this.appName = appName;
        this.packageName = packageName;
        this.appSize = appSize;
        this.appVersionName = appVersionName;
        this.appIconUrl = appIconUrl;
        this.oldappVersionName = oldappVersionName;
        this.appDownloadUrl = appDownloadUrl;
        this.install = install;
    }

    public String getInstall() {
        return install;
    }

    public void setInstall(String install) {
        this.install = install;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public void setAppIconUrl(String appIconUrl) {
        this.appIconUrl = appIconUrl;
    }

    public String getAppDownloadUrl() {
        return appDownloadUrl;
    }

    public void setAppDownloadUrl(String appDownloadUrl) {
        this.appDownloadUrl = appDownloadUrl;
    }
}
