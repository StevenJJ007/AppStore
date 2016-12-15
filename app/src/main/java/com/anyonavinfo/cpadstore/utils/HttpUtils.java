package com.anyonavinfo.cpadstore.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.anyonavinfo.cpadstore.entity.AppInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtils {
    private static final int TIMEOUT_IN_MILLIONS = 5000;

    public interface CallBack {
        void onRequestComplete(String result, int code);
    }

    /**
     * 判断网络是否连接
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将url对应的数据转化为封装的对象
     *
     * @param
     * @return
     */
    public static ArrayList<AppInfo> getJsonData(Handler handler) {
        ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
        String jsonString = getURLResponse(handler);
        Log.i("测试", "MainActivity的getJsonData=" + jsonString);
        JSONObject jsonObject;
        AppInfo appInfo;
        try {
            jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                appInfo = new AppInfo();
                appInfo.appName = jsonObject.getString("appName");
                appInfo.appIconUrl = jsonObject.getString("iconUrl");
                appInfo.appSize = jsonObject.getString("appSize");
                appInfo.appDownloadUrl = jsonObject.getString("apkUrl");
                appInfo.appVersionName = jsonObject.getString("version");
                appInfo.packageName = jsonObject.getString("packageName");
                appInfo.install = jsonObject.getString("validStatus");
                /** 2016-06-20 zza 添加*/
                appInfo.appVersionCode = jsonObject.getString("versionCode");
                appInfoList.add(appInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("shijj--->", appInfoList.size() + "");
        return appInfoList;
    }

    /**
     * 通过InputStream解析网页返回的数据
     *
     * @param is
     * @return
     */
    public static String readStream(InputStream is) {
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("测试", result.toString());
        return result;

    }

    /**
     * 获取指定URL的响应字符串
     *
     * @return
     */
    private static String getURLResponse(Handler handler) {
        HttpURLConnection conn = null; //连接对象
        InputStream is = null;
        String resultData = "";
        Message msg = new Message();
        try {
            URL url = new URL(HttpApi.URL_MAIN); //URL对象
            conn = (HttpURLConnection) url.openConnection(); //使用URL打开一个链接
            conn.setReadTimeout(6000);
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("POST"); //使用post请求
            is = conn.getInputStream();   //获取输入流，此时才真正建立链接
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(isr);
            String inputLine = "";
            while ((inputLine = bufferReader.readLine()) != null) {
                resultData += inputLine + "\n";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            Log.d("badNet", "连接失败");
            msg.what = 8;
            handler.sendMessage(msg);
        } catch (SocketTimeoutException e) {
            Log.d("badNet1", "连接失败");
            msg.what = 8;
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        Log.d("sjjjjj+++", resultData + "");
        return resultData;
    }

    /**
     * 异步的Get请求
     */
    public static void doGetAsyn(final String urlStr, final int code,
                                 final CallBack callBack) {
        new Thread() {
            public void run() {
                try {
                    String result = doGet(urlStr);
                    if (callBack != null) {
                        callBack.onRequestComplete(result, code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }

            };
        }.start();
    }

    /**
     * Get请求，获得返回数据
     */
    public static String doGet(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            // 检测网络连接超时和网络连接异常
            if (e instanceof SocketTimeoutException) {
                return "SocketTimeoutException";
            } else if (e instanceof UnknownHostException) {
                return "UnknownHostException";
            }
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (baos != null)
                    baos.close();
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

}
