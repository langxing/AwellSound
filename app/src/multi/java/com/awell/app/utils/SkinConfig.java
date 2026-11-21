package com.awell.app.utils;

import android.os.SystemProperties;

/**
 * 运行期 UI 皮肤配置。
 * <p>
 * persist.sys.current.lamborg.ui:
 * 1 -> car_50
 * 2 -> lamborghini
 * 其他值 / 读取失败 -> gongban
 */
public final class SkinConfig {

    private static final String PROP_UI_TYPE = "persist.sys.awell.themeUI";

    public static final int GONGBAN = 0;
    public static final int CAR_50 = 1;
    public static final int LAMBORGHINI = 2;

    private SkinConfig() {
    }

    /**
     * 每次读取最新值，不缓存，保证修改属性后无需重启进程即可生效。
     */
    public static int uiType() {
        return readUiType();
    }

    /**
     * 无缓存，保留方法兼容调用。
     */
    public static void reload() {
        // no-op
    }

    public static boolean isGongban() {
        return uiType() != CAR_50 && uiType() != LAMBORGHINI;
    }

    public static boolean isCar50() {
        return uiType() == CAR_50;
    }

    public static boolean isLamborghini() {
        return uiType() == LAMBORGHINI;
    }

    private static int readUiType() {
        int value = GONGBAN;
        try {
            value = SystemProperties.getInt(PROP_UI_TYPE, GONGBAN);
        } catch (Throwable e) {
            LogUtil.e("read " + PROP_UI_TYPE + " failed: " + e);
        }
        LogUtil.i(PROP_UI_TYPE + " = " + value);
        return value;
    }

}
