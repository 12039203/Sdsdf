package cn.xlink.sdk.demo.utils.utils;

public class ByteUtil {

    final protected static char[] sHexArray = "0123456789ABCDEF".toCharArray();

    final protected static String[] sBitHexArray = new String[]{
            "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"
    };

    /**
     * 将一个单字节的Byte转换成十六进制的数
     *
     * @param b byte
     * @return convert result
     */
    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[0] = sHexArray[v >>> 4];
        hexChars[1] = sHexArray[v & 0x0F];
        return new String(hexChars);
    }

    /**
     * 将一个Byte数组转换成十六进制表示
     *
     * @param bytes
     * @return convert result
     * <p>
     * http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    public static String bytesToHexWithSeperator(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = sHexArray[v >>> 4];
            hexChars[j * 3 + 1] = sHexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf bytes buffer
     * @param pos 中开始转换的位置
     * @return convert result
     */
    public static long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }

    public static byte[] shortToByte(short src) {
        byte[] targets = new byte[2];
        targets[0] = (byte) ((src >> 8) & 0xff);
        targets[1] = (byte) (src & 0xff);
        return targets;
    }

    /**
     * 32位int转byte[]
     */
    public static byte[] intToByte(int src) {
        byte[] result = new byte[4];
        result[0] = (byte) ((src >> 24) & 0xFF);
        result[1] = (byte) ((src >> 16) & 0xFF);
        result[2] = (byte) ((src >> 8) & 0xFF);
        result[3] = (byte) (src & 0xFF);
        return result;
    }

    public static short byteToShort(byte[] bytes) {
        return (short) ((bytes[0] & 0xff) << 8 | bytes[1] & 0xFF);
    }

    public static short byteToShort(byte low, byte high) {
        return (short) ((low << 8 & 0xff) | high & 0xFF);
    }

    public static int byteToUnsignedShort(byte low, byte high) {
        return (int) ((low & 0xff) << 8 | high & 0xFF);
    }

    public static short byteToShort(byte[] b, int offset) {
        return (short) (((b[offset] & 0xff) << 8) | (b[offset + 1] & 0xff));
    }

    public static String printBits(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b :
                bytes) {
            sb.append(sBitHexArray[b >>> 4 & 0x0F]);
            sb.append(" ");
            sb.append(sBitHexArray[b & 0x0F]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static byte XORShort(short data) {
        return (byte) ((data >>> 8 & 0xFF) ^ (data & 0xFF));
    }

    public static String shortToHex(short cmd) {
        return byteToHex((byte) (cmd >>> 8)) + byteToHex((byte) (cmd & 0xff));
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = sHexArray[v >>> 4];
            hexChars[j * 2 + 1] = sHexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] copyByteArray(byte[] data, int startPos, int copyLen) {
        byte[] bs = new byte[copyLen];
        System.arraycopy(data, startPos, bs, 0, bs.length);
        return bs;
    }

    public static int byteToInt(byte[] src) {
        int value;
        value = (((src[0] & 0xFF) << 24) | ((src[1] & 0xFF) << 16) | ((src[2] & 0xFF) << 8) | (src[3] & 0xFF));
        return value;
    }

    public static byte[] zeroByteArray() {
        return new byte[]{};
    }
}
