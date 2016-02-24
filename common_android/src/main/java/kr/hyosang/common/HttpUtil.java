package kr.hyosang.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hyosang on 2015-12-21.
 */
public class HttpUtil extends Thread {
    private static final String TAG = "HttpUtil";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";

    private static final int MSG_REQUEST_COMPLETE = 0x01;
    private static final int MSG_CALL_COMPLETE_LISTENER = 0x02;

    private static HttpUtil mInstance = null;

    private HttpUtil() {
    }

    private ArrayBlockingQueue<HttpData> mQueue = new ArrayBlockingQueue<HttpData>(20);
    private Cookie mCookie = Cookie.getInstance();

    public static HttpUtil getInstance() {
        if(mInstance == null) {
            mInstance = new HttpUtil();
            mInstance.start();
        }

        return mInstance;
    }

    public void add(HttpData data) {
        mQueue.add(data);
    }


    @Override
    public void run() {
        while(true) {
            HttpURLConnection conn = null;

            try {
                HttpData item = mQueue.take();

                URL url = new URL(item.url);
                conn = (HttpURLConnection) url.openConnection();

                boolean bPost = "POST".equals(item.method);

                conn.setRequestMethod(item.method);

                //set headers
                conn.addRequestProperty("User-Agent", USER_AGENT);
                conn.addRequestProperty("Cookie", mCookie.getCookie());
                for(Map.Entry<String, String> entry : item.requestHeaders.entrySet()) {
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }


                conn.setDoInput(true);

                if(bPost) {
                    conn.setDoOutput(true);
                }


                if(bPost) {
                    OutputStream os = conn.getOutputStream();
                    os.write(item.getPostBody().getBytes());
                    os.flush();
                }

                if(conn.getResponseCode() == 200) {
                    //ok
                    //set-cookie
                    Map<String, List<String>> respHeaders = conn.getHeaderFields();
                    List<String> setcookie = respHeaders.get("Set-Cookie");
                    if(setcookie != null && setcookie.size() > 0) {
                        for (String cookie : setcookie) {
                            mCookie.add(cookie);
                        }
                    }

                    //content-type
                    String encoding = "UTF-8";

                    String contentType = conn.getHeaderField("Content-Type");
                    Pattern p = Pattern.compile("charset=([A-Za-z0-9-_]+)");
                    Matcher m = p.matcher(contentType);
                    if(m.find()) {
                        encoding = m.group(1);
                    }

                    Log.d(TAG, "Body Encoding = " + encoding);

                    //process body
                    byte [] buf = new byte[1024];
                    InputStream is = conn.getInputStream();
                    int read;

                    if(item.saveTo != null && item.saveTo.length() > 0) {
                        //저장경로 설정됨
                        mkdir(item.saveTo);
                        FileOutputStream fos = new FileOutputStream(item.saveTo);
                        while((read = is.read(buf)) > 0) {
                            fos.write(buf, 0, read);
                        }
                        fos.close();
                    }else {
                        if(encoding != null && encoding.toUpperCase().equals("UTF-8")) {
                            item.responseBody = readUtf8Stream(is);
                        }else {
                            StringBuffer respBody = new StringBuffer();

                            while ((read = is.read(buf)) > 0) {
                                respBody.append(new String(buf, 0, read, encoding));
                            }
                            item.responseBody = respBody.toString();
                        }
                    }

                    if(item.mListener != null) {
                        mHandler.obtainMessage(MSG_CALL_COMPLETE_LISTENER, item).sendToTarget();
                    }
                }else {
                }

                Log.d(TAG, "HTTP Result = " + conn.getResponseCode());
            }catch(InterruptedException e) {
                Log.d(TAG, Log.getStackTraceString(e));
                break;
            }catch(MalformedURLException e) {
                Log.d(TAG, Log.getStackTraceString(e));
            }catch(IOException e) {
                Log.d(TAG, Log.getStackTraceString(e));
            }finally {
                if(conn != null) { conn.disconnect(); }
            }
        }
    }

    private String readUtf8Stream(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        byte [] buf = new byte[1024];
        int nRead;
        int offset = 0;

        while( (nRead = is.read(buf, offset, buf.length - offset)) > 0) {
            nRead += offset;
            offset = 0;
            for(int i=nRead-2;i<nRead;i++) {
                if(i < 0) continue;

                byte c = buf[i];

                if( ((c & 0xF8) == 0xF0) ||    /* 네바이트짜리 */
                        (((c & 0xF0) == 0xE0) && (i > 0)) ||    /* 세바이트짜리 */
                        (((c & 0xE0) == 0xC0) && (i > 1)))     /* 두바이트짜리 */
                {
                    offset = nRead - i;
                    break;
                }
            }

            sb.append(new String(buf, 0, (nRead - offset)));

            //잘린부분 앞으로 복사
            for(int i=0;i<offset;i++) {
                buf[i] = buf[nRead-offset+i];
            }
        }

        return sb.toString();
    }

    private void mkdir(String filepath) {
        File f = new File(filepath);
        File d = f.getParentFile();

        if(d.exists()) {
            //OK
        }else {
            d.mkdirs();
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_CALL_COMPLETE_LISTENER: {
                    HttpData data = (HttpData) msg.obj;
                    if(data.mListener != null) {
                        data.mListener.onCompleted(data);
                    }
                }

                break;
            }

        }
    };

    public static interface HttpListener {
        public void onCompleted(HttpData data);
    }

    public static class HttpData {
        public String url;
        public String method = "GET";
        public String saveTo = null;
        public Map<String, String> postData = new HashMap<String, String>();
        public Map<String, String> requestHeaders = new HashMap<String, String>();
        public String responseBody = null;
        public HttpListener mListener = null;

        private HttpData() {
            //block default constructor
        }

        private static HttpData create(String method, String url) {
            HttpData inst = new HttpData();
            inst.method = method;
            inst.url = url;

            return inst;
        }

        public static HttpData createGetRequest(String url) {
            return create("GET", url);
        }

        public static HttpData createPostRequest(String url) {
            return create("POST", url);
        }


        public String getPostBody() {
            StringBuffer sb = new StringBuffer();
            for(Map.Entry<String, String> entry : postData.entrySet()) {
                try {
                    sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
                }catch(UnsupportedEncodingException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                }
            }

            return sb.toString();
        }
    }
}
