package cn.xlink.sdk.demo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些工具方法
 */

public class CommonUtil {

    /**
     * 验证 WIFI SSID 是否正确
     */
    public static boolean checkSsid(String ssid) {
        return !TextUtils.isEmpty(ssid);
    }

    /**
     * 验证密码格式
     */
    public static boolean checkWifiPass(String wifiPass) {
        return !TextUtils.isEmpty(wifiPass) && wifiPass.length() >= 6 && wifiPass.length() <= 25;
    }

    /**
     * 验证账号是否正确
     */
    public static boolean checkAccount(String account) {
        return checkEmail(account) || checkPhoneNumber(account);
    }

    /**
     * 验证邮箱格式
     */
    public static boolean checkEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        if (email.length() > 30) {
            return false;
        }

        boolean flag;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码格式
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        boolean flag;
        try {
            String check = "^[1][3|5|7|8][0-9]{9}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(phoneNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证验证码格式
     */
    public static boolean checkVerifyCode(String verifyCode) {
        if (TextUtils.isEmpty(verifyCode)) {
            return false;
        }

        if (verifyCode.length() != 6) {
            return false;
        }

        for (char c : verifyCode.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * 验证密码格式
     */
    public static boolean checkPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6 && password.length() <= 16;
    }

    /**
     * 得到当前网络帐号
     */
    public static String currentSsid(Context context) {
        String result = null;

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wm.getConnectionInfo();
        if (connectionInfo != null) {
            result = connectionInfo.getSSID();
        }

        if (result != null) {
            if (result.startsWith("\"") && result.endsWith("\"")) {//去除SSID的双引号
                result = result.substring(1, result.length() - 1);
            }
        }

        return result;
    }

    /**
     * 得到app版本
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception ignore) {
            return "1.0.0";
        }
    }


    public static Bundle bundleForPair(String key, Object value) {
        Bundle data = new Bundle();
        if (value instanceof String) {
            data.putString(key, (String) value);
        } else if (value instanceof Integer) {
            data.putInt(key, (Integer) value);
        } else if (value instanceof byte[]) {
            data.putByteArray(key, (byte[]) value);
        } else if (value instanceof Parcelable) {
            data.putParcelable(key, (Parcelable) value);
        } else if (value instanceof Serializable) {
            data.putSerializable(key, (Serializable) value);
        } else {
            throw new UnsupportedOperationException();
        }
        return data;
    }

    public static <T> List<T> toList(T[] objects) {
        List<T> result = new ArrayList<>();
        for (T t :
                objects) {
            result.add(t);
        }
        return result;
    }

    public static <T> boolean in(T index, T[] target) {
        for (T t :
                target) {
            if (index.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean isEmpty(T[] t) {
        return !(t != null && t.length != 0);
    }

    public static boolean isEmpty(Collection collection) {
        return !(collection != null && collection.size() != 0);
    }

    public static boolean isEmpty(WeakReference weakReference) {
        return !(weakReference != null && weakReference.get() != null);
    }
}
