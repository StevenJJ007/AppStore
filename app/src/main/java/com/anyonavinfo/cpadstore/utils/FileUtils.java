package com.anyonavinfo.cpadstore.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by chenzhongyong on 2016/6/7.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int GW_LOG_FILE_MAX_SIZE = 50 * 1024 * 1024; // 内存中日志文件最大值，10M
    private static String gwCurrLogPath; // 如果当前的日志写在内存中，记录当前的日志文件

    public static final String GW_LOG_SAVE_GPS = FileUtils.getSDPath() + "/SERVICE_LOG/log_gps";
    public static final String GW_LOG_SAVE_CARINFOINSTANT = FileUtils.getSDPath() + "/SERVICE_LOG/log_carinfoinstant";
    public static final String GW_LOG_SAVE_CARINFONONEINSTANT = FileUtils.getSDPath() + "/SERVICE_LOG/log_carinfononeinstant";
    public static final String GW_LOG_SAVE_CARINFOONCE = FileUtils.getSDPath() + "/SERVICE_LOG/log_carinfoonce";
    public static final String GW_LOG_SAVE_CARINFOWARNING = FileUtils.getSDPath() + "/SERVICE_LOG/log_carinfowarning";
    
    public static final String GW_LOG_SAVE_IDDATA=FileUtils.getSDPath() + "/SERVICE_LOG/log_iddata";
    public static final String GW_LOG_SAVE_NORMALLOG = FileUtils.getSDPath() + "/SERVICE_LOG/log_normal";
    public static final String GW_LOG_SAVE_TESTTIME1 = FileUtils.getSDPath() + "/SERVICE_LOG/log_testtime1";
    public static final String GW_LOG_SAVE_TESTTIME2 = FileUtils.getSDPath() + "/SERVICE_LOG/log_testtime2";
    public static final String DOWNLOAD_PATH = FileUtils.getSDPath() + "/Download/";
    /**
     * 打开日志文件并写入日志
     *
     * @return
     **/
    public static void saveToFile(String filepath, String text) {// 新建或打开日志文件
        File file = new File(filepath);
        // 如果日志文件不存在，则新建日志文件
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                Log.v("TAG", "saveToFile file.createNewFile ERROR E==" + e);
                e.printStackTrace();
            }
        }
        // CURR_INSTALL_LOG_NAME =file.getName();
        gwCurrLogPath = file.toString();
        checkLogSize(filepath, text); // 判断日志是否大于10m，若大于，则写到下一个日志中
    }

    /**
     * 判断日志文件是否存在
     *
     * @return
     */
    public static boolean isExitFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.length() > 3) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除日志文件
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        } else {
            return null;
        }
    }

    /**
     * <p>Discription: TODO[判断手机中sd存储器是否存在]</p>
     *
     * @return
     * @author : wangyam
     * @update :
     */
    public static boolean sdCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * <p>Discription: TODO[获取手机SDCARD剩余存储空间]</p>
     *
     * @return
     * @author : wangyam
     * @update :
     */
    public static long getMobileStoragespace() {
        if (sdCardExist()) {// 判断手机中sd存储器是否存在
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * <p>Discription: TODO[检查日志文件大小是否超过了规定大小 10M,若超过了10M则写入下一个日志文件]</p>
     *
     * @param filepath
     * @param text
     * @author : wangyam
     * @update :
     */
    private static void checkLogSize(String filepath, String text) {
        if (!TextUtils.isEmpty(gwCurrLogPath)) {//

            File file = new File(filepath);
            // 如果当前日志大于10M，则写入下一个日志,否则继续在当前日志中写信息
            if (file.length() >= GW_LOG_FILE_MAX_SIZE) {

                // 根据当前日志信息，获取下一个日志路径
                if (GW_LOG_SAVE_GPS.equals(filepath)) {
                    filepath = GW_LOG_SAVE_GPS;
                } else if (GW_LOG_SAVE_CARINFOINSTANT.equals(filepath)) {
                    filepath = GW_LOG_SAVE_CARINFOINSTANT;
                } 
                else if (GW_LOG_SAVE_CARINFONONEINSTANT.equals(filepath)) {
                    filepath = GW_LOG_SAVE_CARINFONONEINSTANT;
                }
                else if (GW_LOG_SAVE_CARINFOONCE.equals(filepath)) {
                    filepath = GW_LOG_SAVE_CARINFOONCE;
                }else {
                    filepath = GW_LOG_SAVE_CARINFOWARNING;
                }
                // 对要写入的日志进行判断，如果大于10M，则将其删除后重新写入，否则直接写。
                if (new File(filepath).length() >= GW_LOG_FILE_MAX_SIZE) {
                    deleteFile(filepath);
                } else {
                    // wreteFile(filepath,text);
                }
                saveToFile(filepath, text); // 判断将要写入的日志是否存在及更改记录的当前日志
            } else {
                writeFile(filepath, text);
            }
        }
    }

    /**
     * <p>Discription: TODO[写文件]</p>
     *
     * @param filepath
     * @param text
     * @author : wangyam
     * @update :
     */
    public static void writeFile(String filepath, String text) {
        File file = new File(filepath);
        if (file.exists()) {
            try {
                FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖

                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                bufWriter.write(text);
                bufWriter.newLine();
                bufWriter.close();
                filerWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.v("TAG", "saveToFile file.saveToFile ERROR E==" + e);
                e.printStackTrace();
            }
        }

    }

    /**
     * <p>Discription: TODO[获取当前正在执行的日志]</p>
     *
     * @return
     * @author : wangyam
     * @update :
     */
    public static String getCurrLogPath() {
        // 判断当前正在执行的日志路径是否存在，如果不存在说明是首次进入，则默认赋值为GW_LOG_SAVE_PATH1
        if (TextUtils.isEmpty(gwCurrLogPath)) {
            Log.e(TAG, "gwCurrLogPath" + "gwCurrLogPath is empty");
            // 首次进入程序，如果手机中存在路径相同的日志则将其删除。
            if (isExitFile(GW_LOG_SAVE_GPS)) {
                deleteFile(GW_LOG_SAVE_GPS);
            }
            if (isExitFile(GW_LOG_SAVE_CARINFOINSTANT)) {
                deleteFile(GW_LOG_SAVE_CARINFOINSTANT);
            }
            if (isExitFile(GW_LOG_SAVE_CARINFONONEINSTANT)) {
                deleteFile(GW_LOG_SAVE_CARINFONONEINSTANT);
            }
            if (isExitFile(GW_LOG_SAVE_CARINFOONCE)) {
                deleteFile(GW_LOG_SAVE_CARINFOONCE);
            }
            if (isExitFile(GW_LOG_SAVE_CARINFOWARNING)) {
                deleteFile(GW_LOG_SAVE_CARINFOWARNING);
            }
            gwCurrLogPath = GW_LOG_SAVE_GPS;
        }

        return gwCurrLogPath;
    }
}
