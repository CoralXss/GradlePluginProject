package com.coral.demo;

import com.coral.plugin.CallFinish;

/**
 * Created by xss on 2019/1/9.
 */
public class TrackFinishEvent {

    @CallFinish
    public static void track(int pageHashCode, String pageName, String requestTag) {
        // 在此处插入统计网络加载完成的时间节点

    }
}
