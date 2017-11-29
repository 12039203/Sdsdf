package cn.xlink.sdk.demo.ui.module.psdfound;


import android.support.annotation.NonNull;

import org.json.JSONObject;

import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.api.app.UserAuthApi;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 注册业务操作
 * Created by taro on 2017/9/5.
 */

public class PasswordFoundPresenter extends BaseActivityPresenter<PasswordFoundActivity> {
    public PasswordFoundPresenter(PasswordFoundActivity foundActivity) {
        super(foundActivity);
        XLinkSDK.start();
    }

    /**
     * 发送手机验证码,仅支持中国大陆手机<br>
     * 发送手机验证码实际上存在可能反复发送的情况,如果短时间内反复发送需要进行图片验证码校验,此处没有进行图片验证码的操作
     *
     * @param phone  手机号
     * @param cropId 企业ID
     */
    public void sendVerifyCode(String phoneZone, String phone, String cropId) {
        if (StringUtil.isAllNotEmpty(phoneZone, cropId, phone)) {
            UserAuthApi.RegisterVerifyCodeRequest request = new UserAuthApi.RegisterVerifyCodeRequest();
            //中国大陆手机
            request.phoneZone = phoneZone;
            request.phone = phone;
            request.corpId = cropId;
            //图片验证码,不一定需要,当有图片验证时才需要
//            request.captcha="xxx";

            getView().showLoading();
            Call<String> call = XLinkRestful.getApplicationApi().registerPhoneVerifyCode(request);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    boolean isSuccess = false;
                    if (response.isSuccessful()) {
                        isSuccess = true;
                    }
                    //界面存在时才进行界面通知与刷新
                    if (!isViewExisted()) {
                        return;
                    }
                    final boolean finalResult = isSuccess;
                    getView().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //通知发送结果
                            getView().showSendCodeResult(finalResult, null, null);
                        }
                    });
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    String msg = "验证码发送失败";
                    if (t != null && t.getMessage() != null) {
                        msg = t.getMessage();
                    }
                    if (!isViewExisted()) {
                        return;
                    }
                    final String finalMsg = msg;
                    getView().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getView().showSendCodeResult(false, null, finalMsg);
                        }
                    });
                }
            });
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    /**
     * 邮箱密码找回操作
     *
     * @param mail    邮箱号码
     * @param cropid  企业ID
     * @param pwd     密码
     * @param captcha 验证码
     */
    public void doMailFoundBack(final String mail, final String cropid, String pwd, String captcha) {
        if (StringUtil.isAllNotEmpty(mail, cropid)) {
            UserManager.getInstance().setCorpId(cropid);
            UserManager.getInstance().setAccount(mail);

            UserAuthApi.PasswordForgotRequest request = new UserAuthApi.PasswordForgotRequest();
            request.corpId = cropid;
            request.email = mail;
            request.corpId = cropid;
            request.captcha = captcha;

            getView().showLoading();
            Call<String> call = XLinkRestful.getApplicationApi().sendPasswordFound(request);
            call.enqueue(new FoundCallback<String>() {
                @Override
                protected int isMailOrPhone() {
                    return CODE_MAIL;
                }
            });
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    /**
     * 手机密码找回操作
     *
     * @param phone   手机号码
     * @param captcha 验证码
     * @param cropid  企业ID
     * @param pwd     密码
     */
    public void doPhoneFoundBack(String phoneZone, final String phone, String captcha, final String cropid, String pwd) {
        if (StringUtil.isAllNotEmpty(phoneZone, phone, cropid)) {
            UserManager.getInstance().setCorpId(cropid);
            UserManager.getInstance().setAccount(phone);

            UserAuthApi.PasswordForgotRequest request = new UserAuthApi.PasswordForgotRequest();
            request.corpId = cropid;
            request.phone = phone;
            request.phoneZone = phoneZone;
            request.captcha = captcha;


            getView().showLoading();
            Call<String> call = XLinkRestful.getApplicationApi().sendPasswordFound(request);
            call.enqueue(new FoundCallback<String>() {
                @Override
                protected int isMailOrPhone() {
                    return CODE_PHONE;
                }
            });
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    private String getErrorMsg(@NonNull String defaultMsg, Response response, Throwable t) {
        String msg = null;
        if (t != null) {
            t.printStackTrace();
        }
        if (response.errorBody() != null) {
            String error = String.valueOf(response.errorBody());
            if (!"null".equals(error)) {
                try {
                    JSONObject object = new JSONObject(error);
                    if (object.has("error")) {
                        JSONObject errorObj = object.getJSONObject("error");
                        if (errorObj != null && errorObj.has("code")) {
                            int code = errorObj.getInt("code");
                            if (code == XLinkErrorCode.ERROR_API_CORP_NOT_EXISTS.getValue()) {
                                msg = "企业不存在,请重新确认";
                            } else if (code == XLinkErrorCode.ERROR_API_REGISTER_PHONE_EXISTS.getValue()) {
                                msg = "该手机已注册,请使用该邮箱登录";
                            } else if (code == XLinkErrorCode.ERROR_API_REGISTER_EMAIL_EXISTS.getValue()) {
                                msg = "该邮箱已注册,请使用该邮箱登录";
                            }
                        }

                        if (msg == null && errorObj.has("msg")) {
                            msg = errorObj.getString("msg");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (msg == null) {
            msg = defaultMsg;
        }
        return msg;
    }

    private abstract class FoundCallback<T> implements Callback<T> {
        public static final int CODE_MAIL = 1;
        public static final int CODE_PHONE = 2;

        protected abstract int isMailOrPhone();

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            final boolean isSucess = response.isSuccessful();
            if (!isViewExisted()) {
                return;
            }

            String errorMsg = getErrorMsg("请求出错,请稍后重试", response, null);
            String successMsg = isMailOrPhone() == CODE_MAIL ? "请登录邮箱重置密码" : "请留意手机短信获取重置密码";
            final String finalMsg = isSucess ? successMsg : errorMsg;
            getView().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getView().showFoundResult(isSucess, null, finalMsg);
                }
            });
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            //注册失败
            String msg = "请校验提交信息再重新尝试";
            if (t != null && t.getMessage() != null) {
                msg = t.getMessage();
            }
            if (!isViewExisted()) {
                return;
            }
            final String finalMsg = msg;
            getView().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getView().showFoundResult(false, null, finalMsg);
                }
            });
        }
    }
}
