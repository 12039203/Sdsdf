package cn.xlink.sdk.demo.ui.module.login;

import android.text.TextUtils;
import android.view.View;

import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.utils.CommonUtil;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.module.http.XLinkUserAuthorizeTask;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

/**
 * 登录界面Presenter
 */
public class LoginPresenter extends BaseActivityPresenter<LoginActivity> {

    LoginPresenter(LoginActivity loginActivity) {
        super(loginActivity);

        // 启动SDK,启动后TCP或UDP服务会被开启。因为退出登录（XLinkSDK.logoutAndStop()）后会回到此画面
        // 所以要在这里start
        XLinkSDK.start();
    }

    /**
     * 登录
     *
     * @param corpId
     * @param account
     * @param pwd
     */
    public void doLogin(String corpId, String account, String pwd) {
        if (StringUtil.isAllNotEmpty(corpId, account, pwd) && CommonUtil.checkAccount(account)) {
            if (CommonUtil.checkPhoneNumber(account)) {
                login(corpId, account, null, pwd);
                return;
            }

            if (CommonUtil.checkEmail(account)) {
                login(corpId, null, account, pwd);
                return;
            }
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    /**
     * 执行用户验证
     */
    private void login(final String corpId, String phone, String email, final String password) {
        final String account = TextUtils.isEmpty(phone) ? email : phone;

        UserManager.getInstance().setCorpId(corpId);
        UserManager.getInstance().setAccount(account);

//        XLinkThirdPartyAuthorizeTask task = XLinkThirdPartyAuthorizeTask.newBuilder()
//                .setCorpId(corpId)
//                .setOpenId("123123", "123123")
//                .setName("123123")
//                .setSource(XLinkRestfulEnum.UserSource.QQ)
//                .setListener(new XLinkTaskListener<ThirdPartyAuthApi.AuthResponse>() {
//                    @Override
//                    public void onError(XLinkErrorCode errorCode) {
//                        getView().dismissLoading();
//                        showLogError(errorCode);
//                    }
//
//                    @Override
//                    public void onStart() {
//                        getView().showLoading();
//                    }
//
//                    @Override
//                    public void onComplete(ThirdPartyAuthApi.AuthResponse result) {
//                        getView().dismissLoading();
//
//                        // app要保存好授权信息，下次打开app跳过登陆的步骤
//                        UserManager.getInstance().setUid(result.userId);
//                        UserManager.getInstance().setAccessToken(result.accessToken);
//                        UserManager.getInstance().setAuthString(result.authorize);
//                        UserManager.getInstance().setRefreshToken(result.refreshToken);
//
//                        getView().showLoginSuccess();
//                    }
//                })
//                .build();
        XLinkUserAuthorizeTask task = XLinkUserAuthorizeTask.newBuilder()
                .setPhone(account.toLowerCase(), password) // 这里没有判断帐号类型，都设置上也行
                .setEmail(account.toLowerCase(), password) // 这里没有判断帐号类型，都设置上也行
                .setCorpId(corpId)
                .setListener(new XLinkTaskListener<UserAuthApi.UserAuthResponse>() {
                    @Override
                    public void onError(XLinkErrorCode errorCode) {
                        getView().dismissLoading();
                        showLogError(errorCode);
                    }

                    @Override
                    public void onStart() {
                        getView().showLoading();
                    }

                    @Override
                    public void onComplete(UserAuthApi.UserAuthResponse result) {
                        getView().dismissLoading();

                        // app要保存好授权信息，下次打开app跳过登陆的步骤
                        UserManager.getInstance().setUid(result.userId);
                        UserManager.getInstance().setAccessToken(result.accessToken);
                        UserManager.getInstance().setAuthString(result.authorize);
                        UserManager.getInstance().setRefreshToken(result.refreshToken);

                        getView().showLoginSuccess();
                    }
                })
                .build();
        XLinkSDK.startTask(task);
    }

    void showLogError(XLinkErrorCode code) {
        String account = UserManager.getInstance().getAccount();
        String tips;
        switch (code) {
            case ERROR_API_ACCOUNT_VAILD_ERROR:
                tips = getString(R.string.error_account_invalid, account);
                break;
            case ERROR_API_ACCOUNT_PASSWORD_ERROR:
                tips = getString(R.string.error_account_or_pwd_invalid);
                break;
            case ERROR_API_USER_NOT_EXISTS:
                if (CommonUtil.checkEmail(account)) {
                    tips = getString(R.string.error_email_none_register, account);
                    break;
                }
                if (CommonUtil.checkPhoneNumber(account)) {
                    tips = getString(R.string.error_phone_none_register, account);
                    break;
                }
            default:
                tips = getString(R.string.error_login_failure);
                break;
        }
        getView().showPromptDialog(getString(R.string.error_login_failure), tips, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getView().finish();
            }
        });
    }
}
