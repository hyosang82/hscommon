package kr.hyosang.samples;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import kr.hyosang.common.HttpUtil;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startHttpActivity(View v) {
        HttpUtil.HttpData test = HttpUtil.HttpData.createGetRequest("http://www.naver.com/");
        test.mListener = new HttpUtil.HttpListener() {
            @Override
            public void onCompleted(HttpUtil.HttpData data) {
                Log.d("TEST", "RECEIVED = " + data.responseBody);
            }
        };

        HttpUtil.getInstance().add(test);
    }

    public void startLocalBitmapCache(View v) {
        Intent i = new Intent(this, LocalBitmapCache.class);
        startActivity(i);
    }

    public void startHttpBitmapCache(View v) {
        Intent i = new Intent(this, HttpBitmapCache.class);
        startActivity(i);
    }
}
