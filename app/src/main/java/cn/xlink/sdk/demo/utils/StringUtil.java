package cn.xlink.sdk.demo.utils;

import android.text.TextUtils;

import java.util.Collection;

/**
 * Created by legendmohe on 16/5/20.
 */
public class StringUtil {

    private static final int PAD_LIMIT = 8192;

    public static boolean isEmpty(String target) {
        return target == null || target.length() == 0;
    }

    public static boolean isNotEmpty(String target) {
        return target != null && target.length() != 0;
    }

    public static boolean isAllNotEmpty(String... targets) {
        for (String target :
                targets) {
            if (isEmpty(target)) {
                return false;
            }
        }
        return true;
    }

    public static void appendIfNotEmpty(StringBuilder sb, String target, String sep) {
        if (sb == null)
            return;
        if (isNotEmpty(target)) {
            sb.append(target);
            if (isNotEmpty(sep)) {
                sb.append(sep);
            }
        }
    }

    public static void appendIfNotEmpty(StringBuilder sb, String target) {
        appendIfNotEmpty(sb, target, null);
    }

    public static String joinStrings(String sep, Collection<String> strings) {
        if (CommonUtil.isEmpty(strings))
            return "";
        StringBuilder sb = new StringBuilder();
        for (String item :
                strings) {
            if (item != null && item.length() != 0) {
                sb.append(item);
                sb.append(sep);
            }
        }
        if (sb.length() != 0) {
            sb.delete(sb.length() - sep.length(), sb.length() - 1);
        }
        return sb.toString();
    }

    public static String joinStrings(String sep, String... strings) {
        if (CommonUtil.isEmpty(strings))
            return "";
        StringBuilder sb = new StringBuilder();
        for (String item :
                strings) {
            if (item != null && item.length() != 0) {
                sb.append(item);
                sb.append(sep);
            }
        }
        if (sb.length() != 0) {
            sb.delete(sb.length() - sep.length(), sb.length() - 1);
        }
        return sb.toString();
    }

    public static String chooseNotNull(String... strings) {
        for (String curString :
                strings) {
            if (!TextUtils.isEmpty(curString))
                return curString;
        }
        return "";
    }

    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(padding(pads, padChar));
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return padding(pads, padChar).concat(str);
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }
}
