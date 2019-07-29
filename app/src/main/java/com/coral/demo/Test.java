package com.coral.demo;

import android.util.Log;

import com.coral.plugin.CallFinish;

/**
 * Created by xss on 2018/11/20.
 */
public class Test {

    @CallFinish(pageName = "otherTest")
    public void otherTest() {
        Log.e("--->", "================");
    }
}
