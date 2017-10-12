package cn.xlink.sdk.demo.ui.module.ctrl;

import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.sdk.core.model.DataPointValueType;
import cn.xlink.sdk.demo.eventbus.DataPointUpdateEvent;
import cn.xlink.sdk.demo.model.Device;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.model.XLinkDataPoint;
import cn.xlink.sdk.v5.module.datapoint.XLinkGetDataPointTask;
import cn.xlink.sdk.v5.module.datapoint.XLinkSetDataPointTask;
import cn.xlink.sdk.v5.module.http.XLinkRemoveDeviceTask;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import cn.xlink.sdk.v5.module.share.XLinkShareDeviceTask;

/**
 * Created by CHENJIAHUI on 2017/2/26.
 */

public class ControlDevicePresenter extends BaseActivityPresenter<ControlDeviceActivity> {
    private static final String TAG = "DeviceCtrlPresenter";
    private Device mDevice;

    public ControlDevicePresenter(ControlDeviceActivity mControlDeviceActivity, Device device) {
        super(mControlDeviceActivity);
        this.mDevice = device;
    }

    /**
     * 调用XLINKSDK修改数据端点
     * 注意DataPoint的type和value要对应上
     * <p>
     * //  public enum ValueType {
     * //                  // 对应的Java基本数据类型
     * //      BOOL,       // Boolean
     * //      BYTE,       // Byte
     * //      SHORT,      // Short
     * //      USHORT,     // Short
     * //      INT,        // Integer
     * //      UINT,       // Integer
     * //      LONG,       // Long
     * //      ULONG,      // Long
     * //      FLOAT,      // Float
     * //      DOUBLE,     // Double
     * //      STRING,     // String
     * //      BYTE_ARRAY, // byte[]
     * //  }
     *
     * @param index DataPoint的index
     * @param type  DataPoint的数据类型，见上表
     * @param value 要设置的值
     */
    void setDataPoint(final int index, final DataPointValueType type, Object value) {
        final XLinkDataPoint dp = new XLinkDataPoint(index, type);
        dp.setValue(value);
        Log.d(TAG, "setDataPoint: " + dp);

        XLinkSetDataPointTask task = XLinkSetDataPointTask.newBuilder()
                .setXDevice(mDevice.getXDevice())
                .setDataPoints(Collections.singletonList(dp))
                .setListener(new XLinkTaskListener<XDevice>() {
                    @Override
                    public void onError(XLinkErrorCode xLinkErrorCode) {
                        Log.d(TAG, "onError() called with: xLinkErrorCode = [" + xLinkErrorCode + "]" + " + " + dp);
                        if (getContext() != null) {
                            Toast.makeText(DemoApplication.getAppInstance(), index + " | " + type + " | " + xLinkErrorCode.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(XDevice xDevice) {
                        Log.d(TAG, "onComplete() called with: = [" + dp + "]");
                        mDevice.setDataPointByIndex(dp);
                        EventBus.getDefault().post(new DataPointUpdateEvent());
                    }
                })
                .build();
        XLinkSDK.startTask(task);
    }

    /**
     * 调用XLINKSDK获取设备数据端点(从本地或者云端)
     */
    public void getDataPoint() {
        // 2. 再用probe取DataPoint的值
        XLinkGetDataPointTask task = XLinkGetDataPointTask.newBuilder()
                .setXDevice(mDevice.getXDevice())
                .setListener(new XLinkTaskListener<List<XLinkDataPoint>>() {
                    @Override
                    public void onError(XLinkErrorCode errorCode) {
                        Log.d(TAG, "XLinkProbeTask onError() called with: errorCode = [" + errorCode + "]");
                        if (getView() != null) {
                            getView().dismissLoading();
                            Toast.makeText(DemoApplication.getAppInstance(), "拉取失败：" + errorCode.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onStart() {
                        Log.d(TAG, "XLinkProbeTask onStart() called");
                        if (getView() != null) {
                            getView().showLoading();
                        }
                    }

                    @Override
                    public void onComplete(List<XLinkDataPoint> dataPoints) {
                        if (getView() != null) {
                            getView().dismissLoading();
                        }
                        Log.d(TAG, "XLinkProbeTask onComplete() called with: result = [" + dataPoints + "]");

                        mDevice.setDataPoints(dataPoints);

                        Collections.sort(mDevice.getDataPoints(), new Comparator<XLinkDataPoint>() {
                            @Override
                            public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
                                return o1.getIndex() - o2.getIndex();
                            }
                        });

                        if (getView() != null) {
                            getView().refreshAdapter();
                        }
                    }
                })
                .build();
        XLinkSDK.startTask(task);
    }

    /**
     * 删除设备
     */
    public void doRemoveDevice() {
        XLinkRemoveDeviceTask removeDeviceTask = XLinkRemoveDeviceTask.newBuilder()
                .setXDevice(mDevice.getXDevice())
                .setListener(new XLinkTaskListener<String>() {
                    @Override
                    public void onError(XLinkErrorCode errorCode) {
                        if (getView() != null) {
                            getView().dismissLoading();
                            getView().showRemoveDeviceError(errorCode);
                        }
                    }

                    @Override
                    public void onStart() {
                        getView().showLoading();
                    }

                    @Override
                    public void onComplete(String result) {
                        if (getView() != null) {
                            getView().dismissLoading();
                            getView().showSuccessRemoveDevice(mDevice);
                        }
                    }
                })
                .build();
        XLinkSDK.startTask(removeDeviceTask);
    }

    public void doShareDevice(final Device device, String account) {
        if (StringUtil.isEmpty(account))
            return;
        if (device.getXDevice().getConnectionState() != XDevice.State.CONNECTED) {
            if (getView() != null) {
                getView().showShareDeviceOffline();
            }
            return;
        }

        int expired = 7200;
        if (account.contains("-")) {
            String[] accountAndExpired = account.split("-");
            expired = Integer.valueOf(accountAndExpired[0]);
            account = accountAndExpired[1];
        }

        XLinkShareDeviceTask task = XLinkShareDeviceTask.newBuilder()
                .setXDevice(device.getXDevice())
                .setMode(XLinkRestfulEnum.ShareMode.ACCOUNT)
                .setAccount(account)
                .setExpired(expired)
                .setListener(new XLinkTaskListener<String>() {
                    @Override
                    public void onError(XLinkErrorCode xLinkErrorCode) {
                        Log.d(TAG, "XLinkShareDeviceTask onError() called with: xLinkErrorCode = [" + xLinkErrorCode + "]");
                        if (getView() != null) {
                            getView().dismissLoading();
                            getView().showShareDeviceFail(xLinkErrorCode);
                        }
                    }

                    @Override
                    public void onStart() {
                        if (getView() != null) {
                            getView().showLoading();
                        }
                    }

                    @Override
                    public void onComplete(String s) {
                        Log.d(TAG, "XLinkShareDeviceTask onComplete() called with: s = [" + s + "]");
                        if (getView() != null) {
                            getView().dismissLoading();
                            getView().showShareDeviceSuccess(s);
                        }
                    }
                })
                .build();
        XLinkSDK.startTask(task);
    }
}
