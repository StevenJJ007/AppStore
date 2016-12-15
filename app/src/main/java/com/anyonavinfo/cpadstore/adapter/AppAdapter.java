package com.anyonavinfo.cpadstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anyonavinfo.cpadstore.R;
import com.anyonavinfo.cpadstore.entity.AppInfo;
import com.anyonavinfo.cpadstore.utils.AppManager;
import com.anyonavinfo.cpadstore.utils.FileDownloadThread;
import com.anyonavinfo.cpadstore.utils.HttpUtils;
import com.anyonavinfo.cpadstore.utils.ImageLoader;
import com.anyonavinfo.cpadstore.utils.NoDoubleClick;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;


/**
 * Created by shijj on 2016/6/7.
 */
public class AppAdapter extends BaseAdapter {
    /*  boolean isconnected;
      MyTimerTask timerTask;
      Timer timer;*/
    private Context mContext;
    private List<AppInfo> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    public static String currentPackageName = null;//当前包名
    public static int currentPosition;//当前位置
    private Map<Integer, downloadTask> map = new HashMap<>();


    public AppAdapter(Context context, List<AppInfo> data) {
        mContext = context;
        mList = data;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.d("position", position + "");
        final AppInfo appInfo = mList.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.app_item, null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.appitem_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.appitem_name);
            holder.appSize = (TextView) convertView.findViewById(R.id.appitem_size);
            holder.appVersion = (TextView) convertView.findViewById(R.id.appitem_version);
            holder.btnFix = (Button) convertView.findViewById(R.id.appitem_action1);
            holder.currentSize = (TextView) convertView.findViewById(R.id.app_currentsize);
            holder.currentSpeed = (TextView) convertView.findViewById(R.id.app_currentspeed);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.currentprogress);
            holder.downloadLayout = (LinearLayout) convertView.findViewById(R.id.downlayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MyHandler mHandler = new MyHandler(holder);
        // 获取app的初始信息与服务端比较
        String action = getAppStates(appInfo);
        /**
         * 和服务器比较后，已经安装过的apk信息显示为老版本信息（图片，大小，名字，版本名）
         */
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mList.get(position).getPackageName(), PackageManager.GET_META_DATA);
            holder.appIcon.setImageDrawable(packageInfo.applicationInfo.loadIcon(mContext.getPackageManager()));
            holder.appName.setText(packageInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString());
            File file = new File(mContext.getPackageManager().getApplicationInfo(mList.get(position).getPackageName(), 0).sourceDir);

            if (file.exists()) {
                long len = file.length();
                double size = (double) len;
                double M = size / (1024 * 1024);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
                holder.appSize.setText(df.format(M) + "M");
            }
            holder.appVersion.setText("当前版本:" + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!appInfo.isDownloading && !AppManager.AppIsExist(mContext, mList.get(position).packageName)) {
            holder.btnFix.setText("安装");
            holder.appSize.setVisibility(View.VISIBLE);
            holder.appVersion.setVisibility(View.VISIBLE);
            holder.btnFix.setTextColor(Color.parseColor("#ffffff"));
            holder.btnFix.setBackgroundResource(R.drawable.an_zhuang_kuang);
            holder.downloadLayout.setVisibility(View.INVISIBLE);
        }
        if (appInfo.isDownloading) {
            Log.d("ME-------", "GO FIRST");
            File file = new File(Environment.getExternalStorageDirectory() + "/cpaddownload/" + appInfo.appName + ".apk");
            if (file.exists() && String.valueOf(file.length()).equals(appInfo.appSize)) {//已下载好了的
                Log.d("ME-------", "GO COMPLETE");
                holder.currentSpeed.setText("下载完成");
                holder.btnFix.setText("下载完成");
                holder.btnFix.setTextColor(Color.parseColor("#cbcbcb"));
                holder.btnFix.setBackgroundResource(R.drawable.zhang_zai);
                holder.downloadLayout.setVisibility(View.INVISIBLE);
                holder.appSize.setVisibility(View.VISIBLE);
                holder.appVersion.setVisibility(View.VISIBLE);
            } else {
                Log.d("ME-------", "GO NOW");
                holder.btnFix.setText("正在下载");
                holder.appSize.setVisibility(View.INVISIBLE);
                holder.appVersion.setVisibility(View.INVISIBLE);
                holder.btnFix.setTextColor(Color.parseColor("#cbcbcb"));
                holder.btnFix.setBackgroundResource(R.drawable.zhang_zai);
                holder.downloadLayout.setVisibility(View.VISIBLE);
            }
        }
        if (AppManager.AppIsExist(mContext, mList.get(position).packageName)) {
            holder.btnFix.setText("打开");
            holder.appSize.setVisibility(View.VISIBLE);
            holder.appVersion.setVisibility(View.VISIBLE);
            holder.btnFix.setTextColor(Color.parseColor("#476ec6"));
            holder.btnFix.setBackgroundResource(R.drawable.da_kai_kuang);
            holder.downloadLayout.setVisibility(View.INVISIBLE);
        }

        holder.btnFix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnFix.getText().equals("安装") && !NoDoubleClick.isDoubleClick()) {
                    holder.btnFix.setText("正在下载");
                    holder.btnFix.setTextColor(Color.parseColor("#cbcbcb"));
                    holder.btnFix.setBackgroundResource(R.drawable.zhang_zai);
                    holder.downloadLayout.setVisibility(View.VISIBLE);
                    appInfo.setDownloading(true);
                    holder.appSize.setVisibility(View.INVISIBLE);
                    holder.appVersion.setVisibility(View.INVISIBLE);
                    if (!HttpUtils.isConnected(mContext)) {
                        Toast.makeText(mContext, "无网络连接，请检查网络", Toast.LENGTH_SHORT).show();
                    } else {
                        doDownload(appInfo.getAppDownloadUrl(), appInfo.getAppName(), appInfo.getPackageName(), mHandler, position);
                    }
                } else if (holder.btnFix.getText().equals("打开") && !NoDoubleClick.isDoubleClick()) {
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mList.get(position).getPackageName());
                    mContext.startActivity(intent);
                } else if (holder.btnFix.getText().equals("正在下载") && !NoDoubleClick.isDoubleClick()) {
                    holder.btnFix.setText("安装");
                    holder.btnFix.setTextColor(Color.parseColor("#ffffff"));
                    holder.btnFix.setBackgroundResource(R.drawable.an_zhuang_kuang);
                    holder.downloadLayout.setVisibility(View.INVISIBLE);
                    appInfo.setDownloading(false);
                    Message msg = new Message();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                    holder.appSize.setVisibility(View.VISIBLE);
                    holder.appVersion.setVisibility(View.VISIBLE);
                    cancelTasks(position);
                } else if (holder.btnFix.getText().equals("下载完成") && !NoDoubleClick.isDoubleClick()) {
                    installApk(position);
                }
            }
        });

        String url = mList.get(position).getAppIconUrl();

        if (!holder.btnFix.getText().equals("打开")) {
             /*每张图片绑定自己的url*/
            holder.appIcon.setTag(url);
            mImageLoader.showImageByAsyncTask(holder.appIcon, url);
            holder.appName.setText(mList.get(position).getAppName());
            holder.appVersion.setText("当前版本:" + mList.get(position).getAppVersionName());
            String bytes = mList.get(position).getAppSize();
            double S = Double.parseDouble(bytes);
            double M = S / (1024 * 1024);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
            holder.appSize.setText(df.format(M) + "M");
        }

        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appSize;
        TextView appVersion;
        TextView currentSpeed;
        TextView currentSize;
        ProgressBar mProgressBar;
        Button btnFix;
        LinearLayout downloadLayout;
    }

    // 比较app信息，并决定按键的状态
    private String getAppStates(AppInfo appInfo) {
        if (!AppManager.AppIsExist(mContext, appInfo.getPackageName())) {
            return "安装";
        } else {
            return "打开";
        }
    }

    /**
     * 使用hander更新UI界面
     */
    class MyHandler extends Handler {
        private ViewHolder holder;
        private float previewSize = 0;
        private float downloadSpeed = 0;
        private int i = 0;

        public MyHandler(ViewHolder holder) {
            this.holder = holder;
        }

        public void handleMessage(final Message msg) {
            downloadSpeed = msg.getData().getInt("size") - previewSize;
            holder.currentSpeed.setText((int) (downloadSpeed / 1024) + "KB/S");
            holder.mProgressBar.setProgress(msg.getData().getInt("size"));
            previewSize = msg.getData().getInt("size");
//          float temp = (float) holder.mProgressBar.getProgress() / (float) holder.mProgressBar.getMax();
            float temp = (float) (msg.getData().getInt("size")) / (float) holder.mProgressBar.getMax();

            float mSize = (float) (holder.mProgressBar.getProgress() / 1024.0 / 1024);
            mSize = (float) (Math.round(mSize * 10)) / 10;
            float maxSize = (float) (holder.mProgressBar.getMax() / 1024.0 / 1024);
            maxSize = (float) (Math.round(maxSize * 10)) / 10;
            holder.currentSize.setText(mSize + "M/" + maxSize + "M");
            int progress = (int) (temp * 100);
            String name = msg.getData().getString("packagename");
            if (name != null) {
                currentPackageName = name;
            }
            final String position = msg.getData().getString("position");
            if (position != null) {
                currentPosition = Integer.valueOf(position);
            }
            // 任务下载完成
            if (progress == 100) {
                Toast.makeText(mContext, "下载完成！", Toast.LENGTH_LONG).show();
                Log.d("ME-------", "GO STEP");
                holder.currentSpeed.setText("下载完成");
                holder.btnFix.setText("下载完成");
                holder.downloadLayout.setVisibility(View.INVISIBLE);
                holder.btnFix.setTextColor(Color.parseColor("#cbcbcb"));
                holder.btnFix.setBackgroundResource(R.drawable.zhang_zai);
                holder.appSize.setVisibility(View.VISIBLE);
                holder.appVersion.setVisibility(View.VISIBLE);
                mList.get(currentPosition).isDownloading = true;
                // mList.get(currentPosition).setDownloading(true);
                final String packageName = mList.get(currentPosition).packageName;

                // 自动安装
                installApk(currentPosition);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            // 安装成功
                            if (AppManager.AppIsExist(mContext, packageName)) {
                                Looper.prepare();
                                Message msg1 = new Message();
                                msg1.what = 1;
                                MyHandler.this.sendMessage(msg1);
                                break;
                            }
                        }
                    }
                }).start();

            }
            //安装完更新界面
            if (msg.what == 1) {
                holder.downloadLayout.setVisibility(View.INVISIBLE);
                holder.btnFix.setText("打开");
                holder.btnFix.setTextColor(Color.parseColor("#476ec6"));
                holder.btnFix.setBackgroundResource(R.drawable.da_kai_kuang);
                holder.appSize.setVisibility(View.VISIBLE);
                holder.appVersion.setVisibility(View.VISIBLE);
                /*mList.get(currentPosition).setDownloading(false);*/
//                mList.get(currentPosition).isDownloading = false;
            } else if (msg.what == 2) {
                previewSize = 0;
                holder.currentSpeed.setText("0KB/S");
            }
        }
    }

    /**
     * 下载准备工作，获取SD卡路径、开启线程
     */
    private void doDownload(String downloadUrl, String appName, String packageName, MyHandler mHandler, int position) {
        // 获取SD卡路径
        String path = Environment.getExternalStorageDirectory() + "/cpaddownload/";
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        String bytes = mList.get(position).getAppSize();
        double S = Double.parseDouble(bytes);
        double M = S / (1024 * 1024);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#0.0");
        // 设置progressBar初始化
        mHandler.holder.mProgressBar.setProgress(0);
        mHandler.holder.currentSize.setText("0.0M/" + df.format(M) + "M");
        int threadNum = 5;
        String filepath = path + appName + ".apk";
        Log.i("filepath", "filepath=" + filepath);
        if (new File(filepath).exists()) {
            new File(filepath).delete();
        }
        mList.get(position).setDownloadPath(filepath);

        downloadTask task = new downloadTask(downloadUrl, threadNum, filepath, packageName, mHandler, position);
        map.put(position, task);
        task.start();


    }

    /**
     * 取消下载任务
     */
    public void cancelTasks(int position) {
        if (map.containsKey(position)) {
            map.remove(position).interrupt();
        }
    }

    /**
     * 多线程文件下载
     *
     * @author
     * @2014-8-7
     */
    class downloadTask extends Thread {
        private String downloadUrl;// 下载链接地址

        private int threadNum;// 开启的线程数
        private String filePath;// 保存文件路径地址
        private int blockSize;// 每一个线程的下载量
        private MyHandler mHandler;
        private int position;
        private String packageName;

        public downloadTask(String downloadUrl, int threadNum, String fileptah, String packageName, MyHandler handler, int position) {
            this.downloadUrl = downloadUrl;
            this.threadNum = threadNum;
            this.filePath = fileptah;
            this.mHandler = handler;
            this.position = position;
            this.packageName = packageName;
        }

        @Override
        public void run() {

            FileDownloadThread[] threads = new FileDownloadThread[threadNum];
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                if (conn.getResponseCode() == 200) {
                    // 读取下载文件总大小
                    int fileSize = conn.getContentLength();
                    if (fileSize <= 0) {
                        System.out.println("读取文件失败");
                        return;
                    }
                    // 设置ProgressBar最大的长度为文件Size
                    mHandler.holder.mProgressBar.setMax(fileSize);

                    // 计算每条线程下载的数据长度
                    blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum : fileSize / threadNum + 1;

                    Log.d("sjjsize---", "fileSize:" + fileSize + "  blockSize:");

                    File file = new File(filePath);
                    for (int i = 0; i < threads.length; i++) {
                        // 启动线程，分别下载每个线程需要下载的部分
                        threads[i] = new FileDownloadThread(url, file,
                                blockSize, (i + 1), mList.get(position).isDownloading());
                        threads[i].setName("Thread:" + i);
                        threads[i].start();
                    }
                    boolean isfinished = false;
                    int downloadedAllSize = 0;
                    while (!isfinished) {
                        isfinished = true;
                        // 当前所有线程下载总量
                        downloadedAllSize = 0;
                        for (int i = 0; i < threads.length; i++) {
                            downloadedAllSize += threads[i].getDownloadLength();
                            if (!threads[i].isCompleted()) {
                                isfinished = false;
                            }
                        }
                        // 通知handler去更新视图组件
                        Message msg = new Message();
                        msg.getData().putInt("size", downloadedAllSize);
                        msg.getData().putString("position", String.valueOf(position));
                        msg.getData().putString("packagename", this.packageName);
                     /*   msg.getData().putBoolean("isconnected", isconnected);
                        Log.d("isconnected", isconnected + "");*/
                        Log.d("sjjsize---", "downloadedAllSize:" + downloadedAllSize);
                        mHandler.sendMessage(msg);
                        Thread.sleep(1000);// 休息1秒后再读取下载进度
                    }
                    Log.d("sjjsize---", " all of downloadSize:" + downloadedAllSize);
                } else {
                    Toast.makeText(mContext, "文件获取失败", Toast.LENGTH_SHORT).show();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    // 安装apk
    private void installApk(int position) {
        File apkfile = new File(mList.get(position).getDownloadPath());
        if (!apkfile.exists()) {
            Toast.makeText(mContext, "要安装的文件不存在，请检查路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

}

