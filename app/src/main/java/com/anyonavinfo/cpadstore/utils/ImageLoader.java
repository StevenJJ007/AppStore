package com.anyonavinfo.cpadstore.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import com.anyonavinfo.cpadstore.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shijj on 2016/2/17.
 */
public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;

    private LruCache<String, Bitmap> mCache;

    public ImageLoader() {
        //获取运行时最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;

        mCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    /**
     * 增加到缓存
     *
     * @param url
     * @param bitmap
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
        /*判断缓存中是否存在当前url对应的图片*/
        if (getBitmapFromCache(url) == null) {
            mCache.put(url, bitmap);
        }

    }

    /**
     * 从缓存中换取数据
     *
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCache(String url) {


        return mCache.get(url);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mImageView.getTag().equals(mUrl)) {
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };


    public Bitmap getBitmapFromUrl(String urlString) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();

            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    public void showImageByAsyncTask(ImageView imageView, String url) {
        /*先判断缓存中是否已经有当前url对应的图片，没有就设置为默认图片，有就直接设置bitmap*/
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
            new NewsAsyncTask(imageView, url).execute(url);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mImageView;
        private String mUrl;

        public NewsAsyncTask(ImageView imageView, String url) {
            mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            /*开启线程下载后，为了方便下载好的bitmap以后可以不再下载，可将它添加到缓存中*/
            String url = params[0];
            Bitmap bitmap = getBitmapFromUrl(url);/*从网络获得图片*/
            /*判断图片不为空，就把它添加到缓存中*/
            if (bitmap != null) {
               /*将不在缓存的图片加入图片*/
                addBitmapToCache(url, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mImageView.getTag().equals(mUrl)) {
                if(bitmap != null) {
                    mImageView.setImageBitmap(bitmap);
                }else{
                    mImageView.setImageResource(R.mipmap.ic_launcher);
                }
            }

        }
    }
}
