package cn.xlink.sdk.demo.ui.module.add;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.model.Device;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.connection.XLinkScanDeviceTask;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import cn.xlink.sdk.v5.module.subscription.XLinkAddDeviceTask;

/**
 * Created by CHENJIAHUI on 2017/2/25.
 */

public class AddDevicePresenter extends BaseActivityPresenter<AddDeviceActivity> {
    private static final String TAG = "AddDevicePresenter";

    //扫描超时  SDK默认90秒
    public static final int SEARCH_NEW_DEVICE_TIMEOUT = 30000;
    //扫描间隔
    public static final int SEARCH_NEW_DEVICE_INTERVAL = 1000;

    public static final int MSG_ADD_DEVICE = 100;

    private XLinkScanDeviceTask mScanTask;
    private Queue<XDevice> mPendingDevice = new ConcurrentLinkedQueue<>();
    private CopyOnWriteArrayList<XDevice> devices;
    private H mAddDeviceHandler;

    private volatile boolean mScanning = false;

    public AddDevicePresenter(AddDeviceActivity addDeviceActivity) {
        super(addDeviceActivity);
        mAddDeviceHandler = new H(this);
    }

    public List<XDevice> getDevices() {
        return devices;
    }

    void initList() {
        if (devices == null)
            devices = new CopyOnWriteArrayList<>();
        devices.clear();
    }

    // 判断是否已存在列表里
    private boolean containsDevice(String mac) {
        for (XDevice device : devices) {
            if (TextUtils.equals(mac, device.getMacAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行扫描设备, 在异步线程上执行。
     */
    void scan(String pid) {
        if (mScanning || StringUtil.isEmpty(pid)) {
            if (getView() != null) {
                getView().showScanningUncompleted();
            }
            return;
        }

        mScanTask = XLinkScanDeviceTask.newBuilder()
                .setTotalTimeout(SEARCH_NEW_DEVICE_TIMEOUT)// 设置超时，单位毫秒，默认90秒
                .setProductIds(pid)
                .setRetryInterval(SEARCH_NEW_DEVICE_INTERVAL)// scan per 1 sec
                .setScanDeviceListener(new XLinkScanDeviceListener() {// 设置搜索回调, **回调在主线程上执行**
                    @Override
                    public void onScanResult(XDevice xDevice) {
                        // 同一设备仅会回调一次
                        Log.d(TAG, "onGotDeviceByScan() called with: " + "xDevice = [" + xDevice + "]");
                        if (xDevice != null) {
                            if (!containsDevice(xDevice.getMacAddress())) {
                                // 默认没选择
                                devices.add(xDevice);
                                // 刷新列表
                                getView().refreshAdapter();
                            }
                        }
                    }

                    @Override
                    public void onError(XLinkErrorCode xLinkErrorCode) {
                        if (xLinkErrorCode != XLinkErrorCode.ERROR_TASK_TIMEOUT) {
                            getView().showScanningError();
                        } else {
                            // 对于XLinkScanDeviceTask来说，timeout的发生在业务层并不是异常，所以这里通知view层Scan正常完成
                            getView().showCompleteScanning();
                        }
                        mScanning = false;
                    }

                    @Override
                    public void onStart() {
                        mScanning = true;
                        if (getView() != null) {
                            getView().showStartScanning();
                        }
                    }

                    @Override
                    public void onComplete(Void aVoid) {
                        mScanning = false;
                        getView().showCompleteScanning();
                    }
                }).build();
        XLinkSDK.startTask(mScanTask);
    }


    /**
     * 停止扫描
     */
    void stopScanning() {
        if (mScanTask != null) {
            // 中断扫描
            mScanTask.cancel();
            mScanTask = null;
        }
    }

    /**
     * 停止订阅
     */
    void stopAddingDevice() {
        mAddDeviceHandler.shutDown();
    }


    /**
     * 执行设备订阅
     */
    void doAddDevice(final Set<XDevice> selectedDevices) {
        if (selectedDevices == null || selectedDevices.size() == 0)
            return;

        mPendingDevice.addAll(selectedDevices);
        mAddDeviceHandler.removeMessages(MSG_ADD_DEVICE);
        mAddDeviceHandler.sendEmptyMessage(MSG_ADD_DEVICE);
    }

    private void onSubscribeDeviceSuccess(XDevice device) {
        if (isViewExisted()) {
            getView().onSubscribeSuccess(device);
        }
    }

    private void onSubscribeDeviceStart(XDevice device) {
        if (isViewExisted() && getView() != null) {
            getView().showLoading();
        }
    }

    private void onSubscribeDeviceFail(XDevice device, XLinkErrorCode xLinkErrorCode) {
        if (isViewExisted()) {
            getView().dismissLoading();
            getView().onSubscribeFail(device, xLinkErrorCode);
        }

        if (xLinkErrorCode == XLinkErrorCode.ERROR_LOCAL_PAIRING_LIMIT_REACHED) {
            Toast.makeText(DemoApplication.getAppInstance(), "配对数已达上限，请重置设备", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSubscribeDeviceComplete() {
        if (isViewExisted()) {
            getView().dismissLoading();
            getView().onSubscribeDeviceComplete();
        }
    }

    //////////////////////////////////////////////////////////////////////

    private static class H extends Handler {
        private final Queue<XDevice> mPendingDevice;
        private final AddDevicePresenter mAddDevicePresenter;
        private boolean mSubscribing = false;
        private XLinkAddDeviceTask mTask;

        public H(AddDevicePresenter addDevicePresenter) {
            super(addDevicePresenter.getContext().getMainLooper());
            mPendingDevice = addDevicePresenter.mPendingDevice;
            mAddDevicePresenter = addDevicePresenter;
        }

        public void shutDown() {
            mPendingDevice.clear();
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_DEVICE: {
                    if (mSubscribing) {
                        return;
                    }
                    // 从添加队列里拿出来一个设备进行添加操作
                    final XDevice device = mPendingDevice.poll();
                    if (device == null) {
                        mAddDevicePresenter.onSubscribeDeviceComplete();
                        return;
                    }

                    mTask = XLinkAddDeviceTask.newBuilder()
                            .setXDevice(device)
                            .setListener(new XLinkTaskListener<XDevice>() {// 设置本次订阅任务的回调

                                @Override
                                public void onError(XLinkErrorCode xLinkErrorCode) {
                                    Log.d(TAG, "subscribe device fail: " + device.getMacAddress() + " -> " + xLinkErrorCode);
                                    mAddDevicePresenter.onSubscribeDeviceFail(device, xLinkErrorCode);

                                    // 失败一个即停止
                                    mPendingDevice.clear();
                                    H.this.removeMessages(MSG_ADD_DEVICE);
                                    mSubscribing = false;
                                }

                                @Override
                                public void onStart() {
                                    Log.d(TAG, "start subscribe device: " + device.getMacAddress());
                                    mSubscribing = true;
                                    mAddDevicePresenter.onSubscribeDeviceStart(device);
                                }

                                @Override
                                public void onComplete(XDevice xDevice) {
                                    // 订阅成功
                                    Log.d(TAG, "subscribe device successfully: " + xDevice.getMacAddress());
                                    Device device = new Device(xDevice);
                                    DeviceManager.getInstance().addDevice(device);

                                    // 正式开发环境无需添加这一行代码
                                    // 正式开发环境无需添加这一行代码
                                    // 正式开发环境无需添加这一行代码
//                                    DeviceManager.getInstance().syncDataPointMetaInfo(Collections.singletonList(xDevice));

                                    mAddDevicePresenter.onSubscribeDeviceSuccess(xDevice);

                                    // 从添加队列里拿下一个设备进行添加操作
                                    mSubscribing = false;
                                    H.this.removeMessages(MSG_ADD_DEVICE);
                                    H.this.sendEmptyMessage(MSG_ADD_DEVICE);
                                }
                            })
                            .build();
                    XLinkSDK.startTask(mTask);
                }
                break;
            }
        }
    }
}
