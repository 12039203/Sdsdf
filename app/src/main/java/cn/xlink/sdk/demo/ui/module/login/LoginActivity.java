package cn.xlink.sdk.demo.ui.module.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.module.main.MainActivity;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

/**
 * Created by CHENJIAHUI on 2017/2/25.
 * <p>
 * 该界面将使用Config中的用户账号密码进行登录
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.corpIdEditText)
    EditText mCorpIdEditText;
    @BindView(R.id.accountEditText)
    EditText mAccountEditText;
    @BindView(R.id.pwdEditText)
    EditText mPwdEditText;
    @BindView(R.id.signInButton)
    Button mLoginButton;
    @BindView(R.id.inputMainLayout)
    LinearLayout mInputMainLayout;
    private LoginPresenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new LoginPresenter(this);

        String corpId = UserManager.getInstance().getCorpId();
        String account = UserManager.getInstance().getAccount();
        if (StringUtil.isAllNotEmpty(corpId, account)) {
            mCorpIdEditText.setText(corpId);
            mAccountEditText.setText(account);
        }

        mPwdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.signInButton})
    public void onSignInClick(View view) {
        switch (view.getId()) {
            case R.id.signInButton:
                attemptLogin();
                break;
        }
    }

    //////////////////////////////////////////////////////////////////////

    private void attemptLogin() {
        String corpId = mCorpIdEditText.getText().toString();
        String account = mAccountEditText.getText().toString();
        String pwd = mPwdEditText.getText().toString();

        presenter.doLogin(corpId, account, pwd);
    }

    public void showLoginSuccess() {
        XLinkSDK.start();
        startActivity(new Intent(getContext(), MainActivity.class));
        supportFinishAfterTransition();
    }
}
