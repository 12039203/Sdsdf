package cn.xlink.sdk.demo.ui.module.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.module.login.LoginActivity;
import cn.xlink.sdk.demo.ui.module.main.MainActivity;
import cn.xlink.sdk.demo.utils.StringUtil;

/**
 * 广告页
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                gotoLoginOrMainActivity();
            }
        }.sendEmptyMessageDelayed(0, 3 * 1000);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    private void gotoLoginOrMainActivity() {
        if (!StringUtil.isEmpty(UserManager.getInstance().getAuthString())) {
            startActivity(new Intent(getContext(), MainActivity.class));
        } else {
            startActivity(new Intent(getContext(), LoginActivity.class));
        }
        supportFinishAfterTransition();
    }
}
