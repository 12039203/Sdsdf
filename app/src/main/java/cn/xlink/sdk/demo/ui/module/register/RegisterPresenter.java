package cn.xlink.sdk.demo.ui.module.register;


import android.util.Log;

import org.json.JSONObject;

import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulEnum;
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

public class RegisterPresenter extends BaseActivityPresenter<RegisterActivity> {
    public RegisterPresenter(RegisterActivity registerActivity) {
        super(registerActivity);
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
     * 邮箱注册操作
     *
     * @param mail     邮箱号码
     * @param cropid   企业ID
     * @param pwd      密码
     * @param nickName 昵称
     */
    public void doMailRegister(final String mail, final String cropid, String pwd, String nickName) {
        if (StringUtil.isAllNotEmpty(mail, cropid, pwd)) {
            UserAuthApi.EmailRegisterRequest request = new UserAuthApi.EmailRegisterRequest();
            request.email = mail;
            request.corpId = cropid;
            request.password = pwd;
            request.localLang = XLinkRestfulEnum.LocalLang.ZH_CN;
            request.source = XLinkRestfulEnum.UserSource.ANDROID;
            request.nickname = nickName;

            getView().showLoading();
            Call<UserAuthApi.EmailRegisterResponse> call = XLinkRestful.getApplicationApi().registEmailAccount(request);
            call.enqueue(new RegisterCallback<UserAuthApi.EmailRegisterResponse>() {
                @Override
                protected boolean saveResult(UserAuthApi.EmailRegisterResponse result) {
                    if (mail.equals(result.email)) {
                        //注册成功,缓存数据
                        UserManager.getInstance().setCorpId(cropid);
                        UserManager.getInstance().setAccount(mail);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    /**
     * 手机注册操作
     *
     * @param phone    手机号码
     * @param captcha  验证码
     * @param cropid   企业ID
     * @param pwd      密码
     * @param nickName 昵称
     */
    public void doPhoneRegister(String phoneZone, final String phone, String captcha, final String cropid, String pwd, String nickName) {
        if (StringUtil.isAllNotEmpty(phoneZone, phone, captcha, cropid, pwd)) {
            UserAuthApi.PhoneRegisterRequest request = new UserAuthApi.PhoneRegisterRequest();
            request.phoneZone = phoneZone;
            request.phone = phone;
            request.corpId = cropid;
            request.verifycode = captcha;
            request.localLang = XLinkRestfulEnum.LocalLang.ZH_CN;
            request.source = XLinkRestfulEnum.UserSource.ANDROID;
            request.password = pwd;
            request.nickname = nickName;


            getView().showLoading();
            Call<UserAuthApi.PhoneRegisterResponse> call = XLinkRestful.getApplicationApi().registPhoneAccount(request);
            call.enqueue(new RegisterCallback<UserAuthApi.PhoneRegisterResponse>() {
                @Override
                protected boolean saveResult(UserAuthApi.PhoneRegisterResponse result) {
                    if (phone.equals(result.phone)) {
                        //注册成功,缓存相关信息
                        UserManager.getInstance().setCorpId(cropid);
                        UserManager.getInstance().setAccount(phone);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            getView().showPromptDialog(R.string.prompt_title, R.string.prompt_invaild_input);
        }
    }

    private String getErrorMsg(Response response, Throwable t) {
        String msg = "请求出错,请稍后重试";
        if (t != null) {
            t.printStackTrace();
        }
        if (response.errorBody() != null) {
            String error = "unknowError";
            try {
                error = response.errorBody().string();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.e("Register response error", error);
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
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return msg;
    }

    private abstract class RegisterCallback<T> implements Callback<T> {
        protected abstract boolean saveResult(T result);

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            boolean isSuccess = false;
            if (response.isSuccessful()) {
                T result = response.body();
                if (result != null) {
                    isSuccess = saveResult(result);
                }
            }
            if (!isViewExisted()) {
                return;
            }

            final String errorMsg = getErrorMsg(response, null);
            final boolean finalResult = isSuccess;
            getView().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg;
                    if (finalResult) {
                        //注册成功
                        msg = "注册成功";
                    } else {
                        //注册失败
                        msg = errorMsg;
                    }
                    getView().showRegisterResult(finalResult, null, msg);
                }
            });
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            //注册失败
            String msg = "请校验提交信息再重新尝试注册";
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
                    getView().showRegisterResult(false, null, finalMsg);
                }
            });
        }
    }
}
