package kr.hyosang.samples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import kr.hyosang.common.HttpUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpUtil.HttpData test = HttpUtil.HttpData.createGetRequest("http://www.naver.com/");
        test.mListener = new HttpUtil.HttpListener() {
            @Override
            public void onCompleted(HttpUtil.HttpData data) {
                Log.d("TEST", "RECEIVED = " + data.responseBody);
            }
        };

        HttpUtil.getInstance().add(test);



    }
}
