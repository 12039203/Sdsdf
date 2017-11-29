package cn.xlink.sdk.demo.ui.module.main;

import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.xlink.sdk.demo.eventbus.UpdateListEvent;
import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

/**
 * Created by CHENJIAHUI on 2017/2/25.
 */

public class MainPresenter extends BaseActivityPresenter<MainActivity> {
    private static final String TAG = "MainPresenter";

    public MainPresenter(MainActivity mainActivity) {
        super(mainActivity);
        // 启动SDK,启动后TCP或UDP服务会被开启。因为启动app或者app从后台进入前台会经过MainActivity，所以在这里start
        XLinkSDK.start();
    }

    public void refreshDeviceList() {
        DeviceManager.getInstance().refreshDeviceList(new XLinkTaskListener<List<XDevice>>() {
            @Override
            public void onError(XLinkErrorCode xLinkErrorCode) {
                Toast.makeText(DemoApplication.getAppInstance(), "刷新列表失败，请重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<XDevice> xDevices) {
                // 通知添加完成
                EventBus.getDefault().post(new UpdateListEvent());
            }
        });
    }

    public void doSignOut() {
        UserManager.getInstance().logout();
        DeviceManager.getInstance().clear();
        // 停止SDK, 断开云端连接，清除授权信息
        XLinkSDK.logoutAndStop();
//        XLinkSDK.logout();
    }

    public void exitApp() {
        // 停止SDK, 断开云端连接
        XLinkSDK.stop();
    }
}
