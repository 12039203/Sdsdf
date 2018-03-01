package cn.xlink.sdk.demo.configwifi;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hiflying.smartlink.ISmartLinker;
import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;
import com.hiflying.smartlink.v7.MulticastSmartLinker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.ui.custom.base.AppDialog;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.custom.constant.Constant;
import cn.xlink.sdk.demo.ui.module.add.AddDeviceActivity;
import cn.xlink.sdk.demo.utils.PrefUtil;

import static cn.xlink.sdk.v5.module.main.XLinkSDK.getContext;

public class ConfigWifiActivity extends BaseActivity implements View.OnClickListener {
    private String TAG="yyy";
    private WifiAdmin wifiAdmin;
    private List<ScanResult> data;
    private ListView listview;
    private WifiConfiguration wifi;
    private EditText edWifiPassword;
    private TextView edwifissid;
    private String mPid;
    public AppDialog appDialog;
//    private CheckBox knowpassword;
    private WifiManager wifiManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edWifiPassword = (EditText) findViewById(R.id.ed_wifi_password);
//        knowpassword = (CheckBox) findViewById(R.id.cb_forgot_password);
        edwifissid = (TextView) findViewById(R.id.ed_wifi_ssid);
        edwifissid.setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        edwifissid.setText(wifiInfo.getSSID().replace("\"",""));
        data=new ArrayList<>();
        wifiAdmin =new WifiAdmin(this);
        wifiAdmin.openWifi();
        String passSsid =  edwifissid.getText().toString();
        final String pass = PrefUtil.getStringValue(getContext(), passSsid, "");
        edWifiPassword.setText(pass);
        findViewById(R.id.bt_start).setOnClickListener(this);
//        knowpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (knowpassword.isChecked()){
//                    ckKnowPassword();
//                }
//            }
//        });

    }



    @Override
    protected int getLayoutId() {
        return R.layout.activity_config_wifi;
    }

    public void judge(){
        String pass = edWifiPassword.getText().toString();
        if (pass.equals("8888")){
            mPid = getIntent().getStringExtra(Constant.BUNDLE_SCAN_PID);
            Intent intent = new Intent(getContext(), AddDeviceActivity.class);
            intent.putExtra(Constant.BUNDLE_SCAN_PID, mPid);
            startActivity(intent);
        }else {

            configureNetwork();
        }
    }
    public void ckKnowPassword(){
        String passSsid = edwifissid.getText().toString();
        String password = edWifiPassword.getText().toString();
        PrefUtil.setStringValue(getContext(), passSsid, password);

    }

    @Override
    public void onClick(View v) {//监听事件
        switch (v.getId()){
            case R.id.ed_wifi_ssid:
                refresh();
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.bt_start:
                ckKnowPassword();
                judge();
                break;

        }
    }

    private void configureNetwork(){
        final ISmartLinker mSnifferSmartLinker = MulticastSmartLinker.getInstance();
        showLoading();

        mSnifferSmartLinker.setOnSmartLinkListener(new OnSmartLinkListener() {
            @Override
            public void onLinked(SmartLinkedModule smartLinkedModule) {

                mPid = getIntent().getStringExtra(Constant.BUNDLE_SCAN_PID);
                Intent intent = new Intent(getContext(), AddDeviceActivity.class);
                intent.putExtra(Constant.BUNDLE_SCAN_PID, mPid);
                startActivity(intent);

            }


            @Override
            public void onCompleted() {


            }
            @Override
            public void onTimeOut() {


            }
        });
        try {

            mSnifferSmartLinker.setTimeoutPeriod(30000);
            String password = edWifiPassword.getText().toString();
            String ssid = edwifissid.getText().toString();
            mSnifferSmartLinker.start(getContext(), password, ssid);
        } catch (Exception e) {
        }

    }
    public void refresh() {
        finish();
        Intent intent = new Intent(ConfigWifiActivity.this, ConfigWifiActivity.class);
        startActivity(intent);
    }
}
