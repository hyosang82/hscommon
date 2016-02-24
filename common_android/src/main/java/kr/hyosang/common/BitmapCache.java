package kr.hyosang.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hyosang on 2016-02-18.
 */
public class BitmapCache {
    private static final String TAG = "BitmapCache";

    private static final int MSG_SET_BITMAP = 0x01;

    private ThreadPoolExecutor mExecutor;
    private LruCache<String, Bitmap> mCache;

    public BitmapCache(Context context) {
        mExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        mCache = new LruCache<String, Bitmap>(10 * 1024 * 1024);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SET_BITMAP: {
                    CacheData data = (CacheData) msg.obj;
                    data.targetView.setImageBitmap(data.bitmap);
                }
                break;
            }
        }
    };

    public void setBitmap(ImageView iv, String localPath) {
        String key = makeKey(localPath);

        Bitmap b = mCache.get(key);

        CacheData data = new CacheData(localPath, iv);

        if (b == null) {
            //cache not exists
            mExecutor.execute(new BitmapDecoder(data));
            Log.d("TEST", "Decode NEW : " + key);
        } else {
            //cache exists
            data.bitmap = b;
            mHandler.obtainMessage(MSG_SET_BITMAP, data).sendToTarget();
            Log.d("TEST", "Cache : " + key);
        }
    }

    protected boolean isCacheExists(String key) {
        return !(mCache.get(key) == null);
    }

    protected String makeKey(String path) {
        String key = path.replaceAll("[^a-zA-Z0-9]", "");

        if(key.length() >= 10) {
            key = key.substring(key.length() - 10);
        }

        key = key + "_" + path.hashCode();

        return key;
    }

    private class BitmapDecoder implements Runnable {
        private CacheData decoderData;

        public BitmapDecoder(CacheData d) {
            decoderData = d;
        }

        @Override
        public void run() {
            Bitmap b = BitmapFactory.decodeFile(decoderData.localPath);
            if(b == null) {
                Log.d(TAG, "Decode image failed : " + decoderData.localPath);
            }else {
                decoderData.bitmap = b;
                mHandler.obtainMessage(MSG_SET_BITMAP, decoderData).sendToTarget();

                mCache.put(makeKey(decoderData.localPath), decoderData.bitmap);
            }
        }
    }

    public static class CacheData {
        public String localPath;
        public ImageView targetView;
        public Bitmap bitmap = null;

        public CacheData(String p, ImageView t) {
            localPath = p;
            targetView = t;
        }
    }
}
