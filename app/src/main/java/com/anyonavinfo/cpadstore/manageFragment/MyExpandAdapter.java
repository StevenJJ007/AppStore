package com.anyonavinfo.cpadstore.manageFragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyonavinfo.cpadstore.R;
import com.anyonavinfo.cpadstore.entity.AppInfo;
import com.anyonavinfo.cpadstore.utils.AppManager;
import com.anyonavinfo.cpadstore.utils.FileDownloadThread;
import com.anyonavinfo.cpadstore.utils.HttpUtils;
import com.anyonavinfo.cpadstore.utils.NoDoubleClick;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by zza on 2016/6/07.
 * 自定义的二级列表adapter
 */
public class MyExpandAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater inflater;
    public final ArrayList<ParentInfo> parent;
    private Context context;
    private HashMap<String, ArrayList<AppInfo>> mapData;
    private ExpandableListView exListView;
    public static String currentPackageName = null;//当前包名
    public static int currentPosition;//当前位置
    private HashMap<Integer, downloadTask> mTask = new HashMap<>();

    public MyExpandAdapter(Context context, HashMap<String, ArrayList<AppInfo>> mapData,
                           ArrayList<ParentInfo> parent, ExpandableListView exListView) {
        this.context = context;
        if (mapData == null) {
            this.mapData = new HashMap<>();
        } else {
            this.mapData = mapData;
        }
        this.parent = parent;
        this.exListView = exListView;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getGroupCount() {//获取父item数量
        Log.i("测试", "parent.size()=" + parent.size());
        return parent.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {//获取当前父item的子item数量
        String key = parent.get(groupPosition).title;
        int count = 0;
        if (mapData.get(key) != null) {
            count = mapData.get(key).size();
        }
//        Log.i("测试", "getChildrenCount="+mapData.get(key).size());
        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {//父类对象
        return parent.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {//子类对象
        String key = parent.get(groupPosition).title;
        return mapData.get(key).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {//父类ID
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {//子类ID
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {//顺序问题
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        ParentInfo parentInfo = this.parent.get(groupPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.parent_item_manage, null);
            groupHolder = new GroupHolder();
            groupHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            groupHolder.tvSubTitles = (TextView) convertView.findViewById(R.id.tv_subtitles);
            groupHolder.ibMore = (ImageButton) convertView.findViewById(R.id.ib_more);
            groupHolder.rlParent = (RelativeLayout) convertView.findViewById(R.id.rl_parent);
            groupHolder.tvUpdateNumber = (TextView) convertView.findViewById(R.id.bt_update_number);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        groupHolder.tvTitle.setText(parentInfo.title);
        groupHolder.tvSubTitles.setText(parentInfo.subtitles);

        if (groupPosition == 0) {//更新管理
            groupHolder.tvUpdateNumber.setVisibility(View.VISIBLE);
            /**更新数量是child1的数量*/
            int appCount = 0;
            if (mapData.get(parentInfo.title) != null) {
                appCount = mapData.get(parentInfo.title).size();
            }
            if (appCount > 0) {
                groupHolder.tvUpdateNumber.setVisibility(View.VISIBLE);
                groupHolder.tvUpdateNumber.setText(appCount + "");
            } else {
                groupHolder.tvUpdateNumber.setVisibility(View.INVISIBLE);
            }
        } else {
            groupHolder.tvUpdateNumber.setVisibility(View.INVISIBLE);
        }

        /**
         * 判断一级菜单状态，设置状态效果
         */
        if (isExpanded) {//一级菜单已张开
            groupHolder.ibMore.setBackgroundResource(R.drawable.more);
            groupHolder.rlParent.setBackgroundResource(R.color.contentBg);
        } else {//一级菜单已关闭
            groupHolder.ibMore.setBackgroundResource(R.drawable.down_target);
            groupHolder.rlParent.setBackgroundResource(R.color.listvewBg_select);
        }

        /**
         * 添加一级菜单监听效果
         */
        groupHolder.rlParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {//一级菜单已张开
                    exListView.collapseGroup(groupPosition);
                } else {//一级菜单已关闭
                    exListView.expandGroup(groupPosition);
                    if (groupPosition == 0 && !HttpUtils.isConnected(context)) {
                        Toast.makeText(context, "网络没有连接，请检查你的网络", Toast.LENGTH_SHORT).show();
                    }
                    if (getChildrenCount(groupPosition) == 0 && groupPosition == 0 && HttpUtils.isConnected(context)) {
                        Toast.makeText(context, "暂无需要更新项目", Toast.LENGTH_SHORT).show();
                    }
                    if (getChildrenCount(groupPosition) == 0 && groupPosition == 1) {
                        Toast.makeText(context, "未安装管理项目", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        final String key = this.parent.get(groupPosition).title;
        final ArrayList<AppInfo> childList = mapData.get(key);

        final AppInfo child = childList.get(childPosition);//数据

        final ChildHolder childHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_item_manage, null);
            childHolder = new ChildHolder();
            childHolder.ivAppIcon = (ImageView) convertView.findViewById(R.id.iv_update_app);
            childHolder.btn = (Button) convertView.findViewById(R.id.bt_update);
            childHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_update_name);
            childHolder.tvSubTitles = (TextView) convertView.findViewById(R.id.tv_update_version);
            childHolder.tvSize = (TextView) convertView.findViewById(R.id.tv_update_size);
            childHolder.pbUpdate = (ProgressBar) convertView.findViewById(R.id.update_progress);
            childHolder.rlUpdate = (RelativeLayout) convertView.findViewById(R.id.rl_update);
            childHolder.tvCruentSize = (TextView) convertView.findViewById(R.id.tv_update_currentsize);
            childHolder.tvSpeed = (TextView) convertView.findViewById(R.id.tv_update_speed);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }
        //加载数据
        childHolder.tvTitle.setText(child.getAppName());
        if (childHolder.ivAppIcon != null) {
            childHolder.ivAppIcon.setImageDrawable(child.appIcon);
        }
        String oldversionName;
        childHolder.btn.setText("更新");
        if (child.oldappVersionName.contains(".")) {
            oldversionName = child.oldappVersionName.substring(0, child.oldappVersionName.lastIndexOf(".") + 2);
        } else {
            oldversionName = child.oldappVersionName;
        }
        String versionName;
        if (child.appVersionName != null) {
            versionName = child.appVersionName.substring(0, child.appVersionName.lastIndexOf(".") + 2);
        } else {
            versionName = oldversionName;
        }

        if (groupPosition == 0) {//更新管理
            if (!child.isDownloading) {
                childHolder.tvSize.setVisibility(View.VISIBLE);//最新版本号及大小
                childHolder.rlUpdate.setVisibility(View.INVISIBLE);//进度情况
                childHolder.pbUpdate.setVisibility(View.INVISIBLE);//进度条
                childHolder.tvSubTitles.setVisibility(View.VISIBLE);//当前版本号
                childHolder.btn.setTextColor(Color.parseColor("#ffffff"));
                childHolder.btn.setBackgroundResource(R.drawable.an_zhuang_kuang);
                childHolder.btn.setText("更新");
            }
            if (child.isDownloading) {
                File file = new File(Environment.getExternalStorageDirectory() + "/cpaddownload/" + child.appName + ".apk");
                if (file.exists() && String.valueOf(file.length()).equals(child.appSize)) {//已下载好了的
                    childHolder.tvSize.setVisibility(View.VISIBLE);//最新版本号及大小
                    childHolder.btn.setTextColor(Color.parseColor("#cbcbcb"));
                    childHolder.btn.setBackgroundResource(R.drawable.zhang_zai);
                    childHolder.rlUpdate.setVisibility(View.INVISIBLE);//进度情况
                    childHolder.pbUpdate.setVisibility(View.INVISIBLE);//进度条
                    childHolder.tvSubTitles.setVisibility(View.VISIBLE);//当前版本号
                    childHolder.btn.setText("下载完成");
                } else {
                    childHolder.tvSubTitles.setVisibility(View.INVISIBLE);
                    childHolder.tvSize.setVisibility(View.INVISIBLE);
                    childHolder.pbUpdate.setVisibility(View.VISIBLE);
                    childHolder.rlUpdate.setVisibility(View.VISIBLE);
                    childHolder.btn.setTextColor(Color.parseColor("#cbcbcb"));
                    childHolder.btn.setBackgroundResource(R.drawable.zhang_zai);
                    childHolder.btn.setText("正在更新");
                }
            }

            double size = Double.parseDouble(child.appSize) / (1024 * 1024);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
            childHolder.tvSubTitles.setText("当前版本 " + oldversionName);//当前版本
            childHolder.tvSize.setText(" ->最新版本:" + versionName + ", 大小"
                    + df.format(size) + "M");//最新版本及大小

        } else {//卸载管理
            childHolder.btn.setText("卸载");
            childHolder.btn.setTextColor(Color.parseColor("#ffffff"));
            childHolder.btn.setBackgroundResource(R.drawable.an_zhuang_kuang);
            childHolder.tvSubTitles.setVisibility(View.VISIBLE);//当前版本号
            childHolder.tvSize.setVisibility(View.INVISIBLE);
            childHolder.pbUpdate.setVisibility(View.INVISIBLE);
            childHolder.rlUpdate.setVisibility(View.INVISIBLE);

            if (childHolder.tvSubTitles != null) {
                childHolder.tvSubTitles.setText("当前版本 " + oldversionName);
            }
        }

        final MyHandler mHandler = new MyHandler(childHolder);
        childHolder.btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!NoDoubleClick.isDoubleClick()) {
                    if (groupPosition == 0) {
                        if (!HttpUtils.isConnected(context)) {
                            Toast.makeText(context, "没有连接网络，请检查你的网络", Toast.LENGTH_SHORT).show();
                        }
                        /**
                         * 更新功能，
                         * 1、要判断本地的版本和网络版本是否一致，网络版本大显示更新
                         * 2、点击更新后判断是否有网络，有开始执行下载，无提醒
                         * 2.1、开始下载，并保存到本地，然后开始自动安装
                         * 3、安装完成收收系统广播，toast提醒用户更新完成，并notifyDataSetChanged
                         */
                        Log.i("测试", " childHolder.btn.setOnClickListener" + child.getAppName());
                        if (childHolder.btn.getText().equals("更新")) {
                            //更新下载，安装
                            child.setDownloading(true);
                            childHolder.tvSubTitles.setVisibility(View.INVISIBLE);
                            childHolder.tvSize.setVisibility(View.INVISIBLE);
                            childHolder.pbUpdate.setVisibility(View.VISIBLE);
                            childHolder.rlUpdate.setVisibility(View.VISIBLE);
                            childHolder.btn.setTextColor(Color.parseColor("#cbcbcb"));
                            childHolder.btn.setBackgroundResource(R.drawable.zhang_zai);
                            childHolder.btn.setText("正在更新");

                            doDownload(child.appDownloadUrl, child.appName,
                                    child.packageName, mHandler, childPosition, groupPosition);
                            Log.i("测试", "update=" + child.getAppName());
                        } else if (childHolder.btn.getText().equals("正在更新")) {
                            child.setDownloading(false);
                            childHolder.btn.setText("更新");
                            childHolder.btn.setTextColor(Color.parseColor("#ffffff"));
                            childHolder.btn.setBackgroundResource(R.drawable.an_zhuang_kuang);
                            childHolder.tvSize.setVisibility(View.VISIBLE);//最新版本号及大小
                            childHolder.rlUpdate.setVisibility(View.INVISIBLE);//进度情况
                            childHolder.pbUpdate.setVisibility(View.INVISIBLE);//进度条
                            childHolder.tvSubTitles.setVisibility(View.VISIBLE);//当前版本号

                            cancelTasks(childPosition);
                            Log.i("cancelTasks", "cancelTasks=" + childPosition);

                            Message msg = Message.obtain();
                            msg.what = 2;
                            msg.arg1 = childPosition;
                            mHandler.sendMessage(msg);
                        } else {
                            installApk(childPosition);
                        }

                    }
                    if (groupPosition == 1) {
                        /**
                         * 卸载功能，
                         * 1、点击按钮，谈出dialog，询问是否要卸载，
                         * 2、确认卸载后，开始卸载已安装的app
                         * 2.1、根据点击按钮的获得的包名，卸载相应报名的app
                         * 3、卸载完成，接收系统发送的广播，开始移除卸载的数据，并notifyDataSetChanged
                         */
                        childHolder.tvTitle.setVisibility(View.VISIBLE);
                        childHolder.pbUpdate.setVisibility(View.INVISIBLE);
                        childHolder.rlUpdate.setVisibility(View.INVISIBLE);
                        unstallApk(child.getPackageName());
                        Log.i("测试", "unstallApk=" + child.getAppName());
                    }
                }
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {//是否可被选中
        return true;
    }

    class GroupHolder {
        TextView tvTitle;
        TextView tvSubTitles;
        TextView tvUpdateNumber;
        ImageButton ibMore;
        RelativeLayout rlParent;
    }

    class ChildHolder {
        ImageView ivAppIcon;
        Button btn;
        TextView tvTitle;
        TextView tvSubTitles;
        TextView tvSize;
        ProgressBar pbUpdate;

        RelativeLayout rlUpdate;
        TextView tvCruentSize;
        TextView tvSpeed;

    }

    /**
     * 卸载apk
     */
    private void unstallApk(String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    // 安装apk
    private void installApk(int position) {
        File apkfile = new File(mapData.get(parent.get(0).title).get(position).getDownloadPath());
        if (!apkfile.exists()) {
            Toast.makeText(context, "要安装的文件不存在，请检查路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(i);
    }

    /**
     * 使用hander更新UI界面
     */
    class MyHandler extends Handler {
        private ChildHolder holder;
        private float previewSize = 0;
        private float downloadSpeed;

        public MyHandler(ChildHolder holder) {
            this.holder = holder;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                previewSize = 0;
                holder.tvSpeed.setText("0KB/S");
            }
            downloadSpeed = msg.getData().getInt("size") - previewSize;
            holder.tvSpeed.setText((int) (downloadSpeed / 1024) + "KB/S");
            holder.pbUpdate.setProgress(msg.getData().getInt("size"));
            previewSize = msg.getData().getInt("size");
//            float temp = (float) holder.pbUpdate.getProgress() / (float) holder.pbUpdate.getMax();
            float temp = (float)(msg.getData().getInt("size")) / (float) holder.pbUpdate.getMax();

            float mSize = (float) (holder.pbUpdate.getProgress() / 1024.0 / 1024);
            mSize = (float) (Math.round(mSize * 10)) / 10;
            float maxSize = (float) (holder.pbUpdate.getMax() / 1024.0 / 1024);
            maxSize = (float) (Math.round(maxSize * 10)) / 10;
            holder.tvCruentSize.setText(mSize + "M/" + maxSize + "M");
            int progress = (int) (temp * 100);
            String name = msg.getData().getString("packagename");
            if (name != null) {
                currentPackageName = name;
            }
            String position = msg.getData().getString("position");
            if (position != null) {
                currentPosition = Integer.valueOf(position);
            }
            // 任务下载完成
            if (progress == 100) {
                Toast.makeText(context, "下载完成！", Toast.LENGTH_LONG).show();
//                previewSize=0;
//                holder.pbUpdate.setProgress(100);
                holder.tvSize.setVisibility(View.VISIBLE);//最新版本号及大小
                holder.rlUpdate.setVisibility(View.INVISIBLE);//进度情况
                holder.pbUpdate.setVisibility(View.INVISIBLE);//进度条
                holder.tvSubTitles.setVisibility(View.VISIBLE);//当前版本号
                holder.btn.setTextColor(Color.parseColor("#cbcbcb"));
                holder.btn.setBackgroundResource(R.drawable.zhang_zai);
                holder.btn.setText("下载完成");
//                final String packageName = mapData.get(parent.get(0).title).get(currentPosition).packageName;
                // 自动安装
                installApk(currentPosition);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            // 安装成功
                            if (AppManager.AppIsExist(context, currentPackageName)) {
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
        }
    }

    /**
     * 下载准备工作，获取SD卡路径、开启线程
     */
    private void doDownload(String downloadUrl, String appName, String packageName, MyHandler mHandler, int position, int groupPosition) {
        // 获取SD卡路径
        String path = Environment.getExternalStorageDirectory() + "/cpaddownload/";
        File file = new File(path);

        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        // 设置progressBar初始化
        mHandler.holder.pbUpdate.setProgress(0);
        int threadNum = 5;
        String filepath = path + appName + ".apk";
        Log.i("filepath", "filepath=" + filepath);
        if (new File(filepath).exists()) {
            new File(filepath).delete();
        }
        mapData.get(parent.get(groupPosition).title).get(position).setDownloadPath(filepath);
        downloadTask task = new downloadTask(downloadUrl, threadNum, filepath, packageName, mHandler, position, groupPosition);
        task.start();
        mTask.put(position, task);
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
        private int groupPosition;
        private String packageName;

        public downloadTask(String downloadUrl, int threadNum, String fileptah, String packageName, MyHandler handler, int position, int groupPosition) {
            this.downloadUrl = downloadUrl;
            this.threadNum = threadNum;
            this.filePath = fileptah;
            this.mHandler = handler;
            this.position = position;
            this.groupPosition = groupPosition;
            this.packageName = packageName;
        }

        @Override
        public void run() {
            FileDownloadThread[] threads = new FileDownloadThread[threadNum];
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(5000);
                if (conn.getResponseCode() == 200) {
                    // 读取下载文件总大小
                    int fileSize = conn.getContentLength();
                    if (fileSize <= 0) {
                        System.out.println("读取文件失败");
                        return;
                    }
                    // 设置ProgressBar最大的长度为文件Size
                    mHandler.holder.pbUpdate.setMax(fileSize);

                    // 计算每条线程下载的数据长度
                    blockSize = (fileSize % threadNum) == 0 ? fileSize
                            / threadNum : fileSize / threadNum + 1;

                    File file = new File(filePath);
                    for (int i = 0; i < threads.length; i++) {
                        // 启动线程，分别下载每个线程需要下载的部分
                        threads[i] = new FileDownloadThread(url, file,
                                blockSize, (i + 1), mapData.get(parent.get(groupPosition).title).get(position).isDownloading());
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
                        mHandler.sendMessage(msg);
                        Thread.sleep(1000);// 休息1秒后再读取下载进度
                    }
                } else {
                    Toast.makeText(context, "文件获取失败", Toast.LENGTH_SHORT).show();
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

    /**
     * 取消单个下载任务
     */
    public void cancelTasks(int position) {
        Log.i("cancelTasks", "containsKey=" + mTask.containsKey(position)
                + mTask.size()
        );
        if (mTask.containsKey(position)) {
            mTask.remove(position).interrupt();
            Log.i("cancelTasks", "remove=" + position);
        }
    }
}
