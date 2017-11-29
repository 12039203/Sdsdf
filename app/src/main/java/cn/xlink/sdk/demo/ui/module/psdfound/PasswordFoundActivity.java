package cn.xlink.sdk.demo.ui.module.psdfound;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.module.login.LoginActivity;
import cn.xlink.sdk.demo.utils.CommonUtil;
import cn.xlink.sdk.demo.utils.StringUtil;

/**
 * 注册界面
 * Created by taro on 2017/9/5.
 */

public class PasswordFoundActivity extends BaseActivity {
    @BindView(R.id.tv_psd_found_title)
    TextView mTvTitle;
    @BindView(R.id.et_psd_found_cropid)
    EditText mEtCropId;
    @BindView(R.id.et_psd_found_pwd)
    EditText mEtPwd;
    @BindView(R.id.et_psd_found_mail)
    EditText mEtMail;
    @BindView(R.id.et_psd_found_phone_zone)
    EditText mEtPhoneZone;
    @BindView(R.id.et_psd_found_phone)
    EditText mEtPhone;
    @BindView(R.id.et_psd_found_captcha)
    EditText mEtCaptcha;
    @BindView(R.id.tv_psd_found_switch)
    TextView mTvSwitchTip;
    @BindView(R.id.tv_psd_found_to_login)
    TextView mTvRegisterLogin;
    @BindView(R.id.btn_psd_found_send_code)
    Button mBtnSend;
    @BindView(R.id.btn_psd_found_register)
    Button mBtnRegister;
    @BindView(R.id.ll_psd_found_phone_container)
    View mVPhoneContainer;

    CountDownTimer mTimer;
    //是否手机注册方式
    boolean mIsPhoneRegister;
    PasswordFoundPresenter mPresetner;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password_found;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresetner = new PasswordFoundPresenter(this);
        String account = UserManager.getInstance().getAccount();
        //根据当前已存在的账号自动切换到对应的注册方式
        mIsPhoneRegister = CommonUtil.checkPhoneNumber(account);
        switchRegisterWay(mIsPhoneRegister);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @OnClick(value = {R.id.btn_psd_found_register, R.id.btn_psd_found_send_code,
            R.id.tv_psd_found_switch, R.id.tv_psd_found_to_login})
    public void onRegister(View view) {
        switch (view.getId()) {
            case R.id.btn_psd_found_register:
                //注册操作
                doFound(mIsPhoneRegister);
                break;
            case R.id.btn_psd_found_send_code:
                //发送验证码操作
                String phone = mEtPhone.getText().toString();
                String cropId = mEtCropId.getText().toString();
                String phoneZone = mEtPhoneZone.getText().toString();
                if (StringUtil.isEmpty(cropId)
                        || StringUtil.isEmpty(phone)
                        || StringUtil.isEmpty(phoneZone)) {
                    showPromptDialog("提示", "企业ID,国际码及手机号码不可为空!\n中国大陆可使用 +86");
                } else {
                    mPresetner.sendVerifyCode(phoneZone, phone, cropId);
                }
                break;
            case R.id.tv_psd_found_switch:
                //切换注册方式
                mIsPhoneRegister = !mIsPhoneRegister;
                switchRegisterWay(mIsPhoneRegister);
                break;
            case R.id.tv_psd_found_to_login:
                //返回登录
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }

    /**
     * 显示发送验证码结果
     *
     * @param isSuccess 是否发送成功
     * @param title     标题
     * @param msg       消息内容
     */
    public void showSendCodeResult(boolean isSuccess, String title, String msg) {
        dismissLoading();
        if (title == null) {
            title = "提示";
        }
        if (isSuccess) {
            showPromptDialog(title, "验证码发送成功,有效期为2分钟");
            //发送成功开始倒计时
            startNewCountDownTimer();
        } else {
            if (msg == null) {
                msg = "验证码发送失败,请稍后重试";
            }
            showPromptDialog(title, msg);
        }
    }

    /**
     * 显示注册结果
     *
     * @param isSuccess 是否注册成功
     * @param title     标题
     * @param msg       消息
     */
    public void showFoundResult(boolean isSuccess, String title, String msg) {
        dismissLoading();
        if (title == null) {
            title = "提示";
        }
        if (isSuccess) {
            if (msg == null) {
                msg = "重置成功";
            }
            //注册成功时,确定自动跳转登录界面
            showPromptDialog(title, msg, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(PasswordFoundActivity.this, LoginActivity.class));
                    finish();
                }
            });
        } else {
            if (msg == null) {
                msg = "请校验提交信息再重新尝试注册";
            }
            showPromptDialog(title, msg);
        }
    }

    /**
     * 注册操作
     *
     * @param isPhoneFound 是否手机注册方式
     */
    private void doFound(boolean isPhoneFound) {
        String cropId = mEtCropId.getText().toString();
        String pwd = mEtPwd.getText().toString();

        if (isPhoneFound) {
            //手机找回
            String phone = mEtPhone.getText().toString();
            String phoneZone = mEtPhoneZone.getText().toString();
            //不需要验证码
            mPresetner.doPhoneFoundBack(phoneZone, phone, null, cropId, pwd);
        } else {
            //邮箱找回,不需要验证码
            String mail = mEtMail.getText().toString();
            mPresetner.doMailFoundBack(mail, cropId, pwd, null);
        }
    }

    /**
     * 切换注册方式
     *
     * @param isPhoneRegister 是否为手机注册
     */
    private void switchRegisterWay(boolean isPhoneRegister) {
        String corpId = UserManager.getInstance().getCorpId();
        if (!StringUtil.isEmpty(corpId)) {
            mEtCropId.setText(corpId);
        }

        if (isPhoneRegister) {
            mTvTitle.setText("手机找回");
            mTvSwitchTip.setText("切换成邮箱找回");
            mVPhoneContainer.setVisibility(View.VISIBLE);
            mEtMail.setVisibility(View.GONE);

            String account = UserManager.getInstance().getAccount();
            if (CommonUtil.checkPhoneNumber(account)) {
                mEtPhone.setText(account);
            } else {
                mEtPhone.setText("");
            }
        } else {
            mTvTitle.setText("邮箱找回");
            mTvSwitchTip.setText("切换成手机找回");
            mVPhoneContainer.setVisibility(View.GONE);
            mEtMail.setVisibility(View.VISIBLE);

            String account = UserManager.getInstance().getAccount();
            if (CommonUtil.checkEmail(account)) {
                mEtMail.setText(account);
            } else {
                mEtMail.setText("");
            }
        }
    }

    /**
     * 启动新的倒计时线程
     */
    private void startNewCountDownTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new SendCodeCountDownTimer();
        mTimer.start();
    }

    private class SendCodeCountDownTimer extends CountDownTimer {
        SendCodeCountDownTimer() {
            super(120000, 1000);
            mBtnSend.setEnabled(false);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mBtnSend != null) {
                mBtnSend.setText(String.format("%d秒", (millisUntilFinished + 500) / 1000));
            }
        }

        @Override
        public void onFinish() {
            if (mBtnSend != null) {
                mBtnSend.setEnabled(true);
                mBtnSend.setText("发送验证码");
            }
        }
    }
}
