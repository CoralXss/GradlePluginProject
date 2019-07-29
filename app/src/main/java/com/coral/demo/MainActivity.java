package com.coral.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coral.plugin.CallFinish;

//import com.coral.demo.TestClass;

public class MainActivity extends AppCompatActivity {

    public static final String name = "Callback;http://www.baidu.com;MainActivity";

//    @CallFinish(pageName = "Main")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("--->", "================");
        new Test();
        Log.e("--->", "================");

        new LoadCallback() {

            @Override
            public void onStart() {

            }

            @CallFinish(pageName = "Callback")
            @Override
            public void onFinish() {

            }
        };

        loadCallback.onFinish();

    }

    private LoadCallback loadCallback = new LoadCallback() {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {
            TrackFinishEvent.track(MainActivity.this.hashCode(), "MainActivity", "loadData");
        }
    };



}
