package com.awell.app.utils;

public class NoFastClickUtils {
    private static long lastClickTime = 0;

    private static int spaceTime = 500;
    private static int spaceTimeSpeaker = 250;

    public synchronized static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        LogUtil.d("currentTime : " + currentTime + "  lastClickTime : " + lastClickTime);
        boolean isAllowClick;
        if (currentTime - lastClickTime > spaceTime) {
            isAllowClick = false;
            lastClickTime = currentTime;
        } else {
            isAllowClick = true;
        }
        return isAllowClick;

    }

    public synchronized static void settimeforisFastClick() {
        long currentTime = System.currentTimeMillis() + 500;
        lastClickTime = currentTime;
    }

    public synchronized static boolean isFastClickSpeaker() {
        long currentTime = System.currentTimeMillis();
        LogUtil.d("currentTime : " + currentTime + "  lastClickTime : " + lastClickTime);
        boolean isAllowClick;
        if (currentTime - lastClickTime > spaceTimeSpeaker) {
            isAllowClick = false;
            lastClickTime = currentTime;
        } else {
            isAllowClick = true;
        }
        return isAllowClick;

    }
}
