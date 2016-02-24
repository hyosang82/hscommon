package kr.hyosang.common;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hyosang on 2016-02-23.
 */
public class HttpImageCache extends BitmapCache {
    private static final String TAG = "HttpImageCache";

    private ThreadPoolExecutor mHttpThreadPool;
    private String mTempPath;

    public HttpImageCache(Context context) {
        super(context);

        mHttpThreadPool = new ThreadPoolExecutor(2, 10, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
        mTempPath = context.getExternalCacheDir().getAbsolutePath();
    }

    public void setBitmap(ImageView iv, String imgUrl) {
        //make local saving path
        String path = mTempPath + "/" + makeKey(imgUrl);
        String key = makeKey(path);

        if(isCacheExists(key)) {
            //use cache
            super.setBitmap(iv, path);
        }else {
            //download image
            mHttpThreadPool.execute(new DownloadRunner(iv, imgUrl, path));
        }
    }

    private class DownloadRunner implements Runnable {
        private ImageView targetView = null;
        private String url = null;
        private String localPath = null;

        public DownloadRunner(ImageView iv, String imgUrl, String saveto) {
            targetView = iv;
            url = imgUrl;
            localPath = saveto;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;

            File f = new File(localPath);
            if(f.exists() && f.length() > 0) {
                //downloaded file exists
                HttpImageCache.super.setBitmap(targetView, localPath);
            }else {
                try {
                    URL urlObj = new URL(url);
                    conn = (HttpURLConnection) urlObj.openConnection();

                    conn.setDoOutput(false);
                    conn.setDoInput(true);

                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        InputStream is = null;
                        FileOutputStream fos = null;

                        try {
                            is = conn.getInputStream();
                            fos = new FileOutputStream(localPath);

                            byte[] buf = new byte[1024];
                            int nRead;

                            while ((nRead = is.read(buf)) > 0) {
                                fos.write(buf, 0, nRead);
                            }
                        } catch (IOException e) {
                            Log.w(TAG, e);
                            throw e;
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                }
                            }
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                }
                            }
                        }

                        //save completed
                        HttpImageCache.super.setBitmap(targetView, localPath);
                    }else {
                        Log.w(TAG, "HTTP Response " + conn.getResponseCode() + " on " + url);
                    }
                } catch (IOException e) {
                    Log.w(TAG, Log.getStackTraceString(e));
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

        }
    }
}
