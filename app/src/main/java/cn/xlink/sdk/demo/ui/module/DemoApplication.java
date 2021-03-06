package cn.xlink.sdk.demo.ui.module;

import android.app.Application;
import android.widget.Toast;
import cn.xlink.sdk.demo.BuildConfig;
import cn.xlink.sdk.common.XLog;
import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.v5.manager.XLinkUser;
import cn.xlink.sdk.v5.module.main.XLinkConfig;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

/**
 * 自定义Application
 * Created by CHENJIAHUI on 2017/2/24.
 */

public class DemoApplication extends Application {

    private static DemoApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        ExceptionCrashUnhandler.getInstance().init(this);

        initManager();
        initXLink();
    }

    private void initXLink() {
        // 加载已经保存的授权信息
        XLinkUser lastUser = new XLinkUser();
        lastUser.setAccessToken(UserManager.getInstance().getAccessToken());
        lastUser.setRefreshToken(UserManager.getInstance().getRefreshToken());
        lastUser.setUid(UserManager.getInstance().getUid());
        lastUser.setAuthString(UserManager.getInstance().getAuthString());

        //配置SDK
        DemoApplicationListener xlinkListener = new DemoApplicationListener(this);
        XLinkConfig config = new XLinkConfig.Builder()
                //正式环境和CM地址
                //测试环境时需要开启 SSL false
//                .setEnableSSL(true)
                .setXLinkUser(lastUser) // 加载已经保存的授权信息
                .setDebug(true) // 是否打印SDK日志
                .setAutoDumpCrash(true) //是否自动转存崩溃日志
                .setXLinkCloudListener(xlinkListener) // 设置云端回调监听
                .setUserListener(xlinkListener) // 设置用户登录回调监听
                .setDataListener(xlinkListener) // 设置数据回调监听
                .setDeviceStateListener(xlinkListener) //  设置设备状态监听
                .build();
        // 初始化SDK, 仅调用一次
        XLinkSDK.init(this, config);

        Toast.makeText(DemoApplication.getAppInstance(), "log file:" + XLog.getLogFilePath(), Toast.LENGTH_SHORT).show();
        XLinkSDK.debugMQTT(true);
        XLinkSDK.debugGateway(true);

        /**
         * 建议不要在这里调用XLinkSDK.start()。因为如果在这里调用start()，用户可能在“退出app”（按返回键）时调用stop()，当app再次进入前台时
         * 不一定走Application的onCreate()，导致SDK因没有start而无法调用其他接口。所以应该在用户使用app时必经过的页面放置
         * start()，例如登录页和app主页。
         * 或者，开发者在“退出app”时不要调用stop()。当app进入后台时，让系统自己杀掉app。再次进入时便会走Application的onCreate()
         * 从而start了SDK
         *
         * demo中，在LoginPresenter和MainPresenter中调用了XLinkSDK.start()
         */
    }

    private void initManager() {
        UserManager.getInstance().init(this);
        DeviceManager.getInstance().init(this);
    }

    public static DemoApplication getAppInstance() {
        return sInstance;
    }
}
