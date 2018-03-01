package cn.xlink.sdk.demo.ui.module.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.xlink.sdk.demo.BuildConfig;
import butterknife.BindView;
import butterknife.OnClick;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.configwifi.ConfigWifiActivity;
import cn.xlink.sdk.demo.ui.custom.constant.Constant;
import cn.xlink.sdk.demo.eventbus.UpdateListEvent;
import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.model.Device;
import cn.xlink.sdk.demo.ui.custom.base.AppDialog;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.BaseViewHolder;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.SingleItemAdapter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.demo.ui.module.add.AddDeviceActivity;
import cn.xlink.sdk.demo.ui.module.ctrl.ControlDeviceActivity;
import cn.xlink.sdk.demo.ui.module.login.LoginActivity;
import cn.xlink.sdk.demo.ui.module.share.ShareListActivity;
import cn.xlink.sdk.demo.utils.PrefUtil;
import cn.xlink.sdk.demo.utils.StringUtil;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.manager.CloudConnectionState;
import cn.xlink.sdk.v5.manager.XLinkCloudConnectionManager;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.top_toolbar)
    Toolbar topToolbar;
    @BindView(R.id.top_title)
    TextView topTitle;
    @BindView(R.id.top_subhead)
    TextView topSubhead;
    @BindView(R.id.device_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    View mEmptyView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    private SingleItemAdapter<Device> deviceAdapter;

    private long mExitTimeStamp;
    private boolean mIsShowProductId;
    private MainPresenter mPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        EventBus.getDefault().register(this);
        String pid = BuildConfig.pid;

        PrefUtil.setStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, pid);

        mRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        DeviceManager.getInstance().refreshDeviceList(new XLinkTaskListener<List<XDevice>>() {
                            @Override
                            public void onError(XLinkErrorCode xLinkErrorCode) {
                                mRefreshLayout.setRefreshing(false);
                                Toast.makeText(DemoApplication.getAppInstance(), "刷新失败：" + xLinkErrorCode, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onStart() {
                                mRefreshLayout.setRefreshing(true);
                            }

                            @Override
                            public void onComplete(List<XDevice> xDevices) {
                                mRefreshLayout.setRefreshing(false);
                                refreshAdapter();
                            }
                        });
                    }
                }
        );

        mPresenter = new MainPresenter(this);
        mPresenter.refreshDeviceList();
    }

    private void initView() {
        setSupportActionBar(topToolbar);
        topTitle.setVisibility(View.VISIBLE);
        topSubhead.setVisibility(View.GONE);
        topToolbar.setBackgroundColor(0);
        assert null != getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initRecyclerView();

        updateCloudConnectionState();
    }

    private void updateCloudConnectionState() {
        CloudConnectionState connectionState = XLinkCloudConnectionManager.getInstance().getConnectionState();
        topTitle.setText(getString(R.string.device_list) + "|云端:"
                + (connectionState == CloudConnectionState.CONNECTED ? getString(R.string.state_online) : getString(R.string.state_not_online))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tab_device_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
//            case R.id.edit_choice_id:
//                showChoice();
//                break;
//            case R.id.edit_product_id:
//                showEditProductId();
//                break;
//            case R.id.edit_company_id:
//                showEditCorpId();
//                break;
            case R.id.share_device_list:
                showShareDeviceList();
                break;
            case R.id.sdk_version:
                showSDKVersion();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        refreshAdapter();
//        if (!mIsShowProductId) {
//            showEditProductId();
//            mIsShowProductId = true;
//        }
//    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTimeStamp) > 2000) {
            Toast.makeText(DemoApplication.getAppInstance(), getString(R.string.exit_app), Toast.LENGTH_SHORT).show();
            mExitTimeStamp = System.currentTimeMillis();
        } else {
            mPresenter.exitApp();
            super.onBackPressed();
        }
    }

    @OnClick(R.id.bottom_view)
    protected void toAddDevice() {
        final String pid = PrefUtil.getStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, "160fa6b314e203e9160fa6b314e23e01");
        if (StringUtil.isEmpty(pid)) {
            ((BaseActivity) getActivity()).showEditDialog("设置Product ID", "", new AppDialog.OnUpdateListener<String>() {
                @Override
                public void onUpdate(String newPid) {
                    Log.d(TAG, "onUpdate() called with: newPid = [" + newPid + "]");

                    PrefUtil.setStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, newPid);
                    Intent intent = new Intent(getContext(), ConfigWifiActivity.class);
                    intent.putExtra(Constant.BUNDLE_SCAN_PID, newPid);
                    startActivity(intent);
                }
            });
        } else {
            Intent intent = new Intent(getContext(), ConfigWifiActivity.class);
            intent.putExtra(Constant.BUNDLE_SCAN_PID, pid);
            startActivity(intent);
        }
    }

    void refreshAdapter() {
        if (deviceAdapter != null) {
            deviceAdapter.setData(DeviceManager.getInstance().getAllDevices());
            deviceAdapter.notifyDataSetChanged();
        }
        isNullDevice();
    }

    private void initRecyclerView() {
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), OrientationHelper.VERTICAL));
        //设置adapter
        deviceAdapter = new SingleItemAdapter<Device>(R.layout.recycler_tab_device_item,
                DeviceManager.getInstance().getAllDevices()) {
            @Override
            public void convert(BaseViewHolder holder, Device device, int position) {
                // 设备名
                holder.setText(R.id.item_tab_device_name, device.getXDevice().getDeviceName());
                // 设备mac
                holder.setText(R.id.item_tab_device_mac, device.getXDevice().getMacAddress());
                // 设备状态，实际app开发时，使用getConnectionState即可，无需区分cloud和local。
                StringBuffer sb = new StringBuffer();
                sb.append(getString(R.string.cloud_connection)).append(":").append(device.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? getString(R.string.state_online) : getString(R.string.state_not_online));
                sb.append(" | ");
                sb.append(getString(R.string.local_connection)).append(":").append(device.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? getString(R.string.state_online) : getString(R.string.state_not_online));
                holder.setText(R.id.item_tab_device_state, sb.toString());
            }

            @Override
            public void onItemClick(View v, Device device, int position) {
                startActivity(new Intent(getContext(), ControlDeviceActivity.class)
                        .putExtra(Constant.BUNDLE_DEVICE_MAC, device.getXDevice().getMacAddress()));
            }
        };
        mRecyclerView.setAdapter(deviceAdapter);
        isNullDevice();
    }


    private void isNullDevice() {
        if (DeviceManager.getInstance().getSize() > 0) {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * 选择产品id
     */
    private void showChoice() {
        final String pid = PrefUtil.getStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, "");
        showChoiceDialog("Product ID", pid, new AppDialog.OnUpdateListener<String>() {
            @Override
            public void onUpdate(String newPid) {
                Log.d(TAG, "onUpdate() called with: newPid = [" + newPid + "]");


                PrefUtil.setStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, newPid);
            }
        });
    }
    /**
     * 修改产品ID
     */
    private void showEditProductId() {
        final String pid = PrefUtil.getStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, "");
        showEditDialog("设置Product ID", pid, new AppDialog.OnUpdateListener<String>() {
            @Override
            public void onUpdate(String newPid) {
                Log.d(TAG, "onUpdate() called with: newPid = [" + newPid + "]");
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("pid1",pid);

                PrefUtil.setStringValue(getContext(), Constant.PREF_KEY_PRODUCT_ID, newPid);
            }
        });
    }

    /**
     * 显示SDK版本
     */
    private void showSDKVersion() {
        Toast.makeText(this, XLinkSDK.getVersion(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示分享列表
     */
    private void showShareDeviceList() {
        startActivity(new Intent(getContext(), ShareListActivity.class));
    }

    /**
     * 修改企业ID
     */
    private void showEditCorpId() {
        final String pid = PrefUtil.getStringValue(getContext(), Constant.PREF_KEY_CORP_ID, "");
        showEditDialog("设置Corp ID", pid, new AppDialog.OnUpdateListener<String>() {
            @Override
            public void onUpdate(String newCorpId) {
                Log.d(TAG, "onUpdate() called with: newCorpId = [" + newCorpId + "]");

                PrefUtil.setStringValue(getContext(), Constant.PREF_KEY_CORP_ID, newCorpId);
                signOut();
            }
        });
    }

    private void signOut() {
        mPresenter.doSignOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        supportFinishAfterTransition();
    }

    /**
     * 刷新设备列表
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateListEvent(UpdateListEvent event) {
        refreshAdapter();
    }

    /**
     * 云端连接状态
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloudConnectionState(CloudConnectionState state) {
        updateCloudConnectionState();
    }
}
