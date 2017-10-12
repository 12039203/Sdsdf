package cn.xlink.sdk.demo.ui.module.add;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.constant.Constant;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.BaseViewHolder;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.SingleItemAdapter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;

/**
 * Created by CHENJIAHUI on 2017/2/25.
 */

public class AddDeviceActivity extends BaseActivity {

    @BindView(R.id.top_toolbar)
    Toolbar topToolbar;
    @BindView(R.id.top_title)
    TextView topTitle;
    @BindView(R.id.top_subhead)
    TextView topSubhead;
    @BindView(R.id.select_device_list)
    RecyclerView selectDeviceList;

    private SingleItemAdapter<XDevice> mAdapter;

    private AddDevicePresenter mPresenter;

    private Set<XDevice> mSelectedDevices = new HashSet<>();

    private String mTargetPid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_device;
    }

    @OnClick(R.id.select_next)
    protected void toSubscribeDevice() {
        if (mSelectedDevices.size() != 0) {
            mPresenter.doAddDevice(mSelectedDevices);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTargetPid = getIntent().getStringExtra(Constant.BUNDLE_SCAN_PID);

        mPresenter = new AddDevicePresenter(this);
        mPresenter.initList();

        initView();
        initRecycler();
        mPresenter.scan(mTargetPid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.stopScanning();
        mPresenter.stopAddingDevice();
    }

    private void initView() {
        setSupportActionBar(topToolbar);
        topTitle.setVisibility(View.VISIBLE);
        topTitle.setText(getString(R.string.add_device));
        topSubhead.setVisibility(View.GONE);
        topToolbar.setBackgroundColor(0);
        assert null != getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tab_add_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_retry:
                mPresenter.scan(mTargetPid);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initRecycler() {
        //设置布局管理器
        selectDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        selectDeviceList.setItemAnimator(null);
        //设置adapter
        mAdapter = new SingleItemAdapter<XDevice>(R.layout.recycler_add_device_item,
                mPresenter.getDevices()) {
            @Override
            public void convert(BaseViewHolder holder, XDevice xDevice, int position) {
                holder.setText(R.id.item_add_device_name, getString(R.string.device_name));
                holder.setText(R.id.item_add_device_mac, xDevice.getMacAddress());
                holder.getView(R.id.item_add_device_select).setVisibility(
                        mSelectedDevices.contains(xDevice) ? View.VISIBLE : View.INVISIBLE
                );
            }

            @Override
            public void onItemClick(View v, XDevice xDevice, int position) {
                // 选中的设备
                if (mSelectedDevices.contains(xDevice)) {
                    mSelectedDevices.remove(xDevice);
                } else {
                    mSelectedDevices.add(xDevice);
                }
                // 刷新列表
                notifyDataSetChanged();
            }
        };
        selectDeviceList.setAdapter(mAdapter);
    }

    protected void refreshAdapter() {
        dismissLoading();
        if (mAdapter != null) {
            mAdapter.setData(mPresenter.getDevices());
            mAdapter.notifyDataSetChanged();
        }
    }

    void showScanningError() {
        if (!isDestroyed()) {
            topSubhead.setVisibility(View.GONE);
            topSubhead.setText("");
            dismissLoading();
            showConfirmDialog(getString(R.string.tips), getString(R.string.scan_fail), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.scan(mTargetPid);
                }
            });
        }
    }

    void onSubscribeSuccess(XDevice device) {
        Toast.makeText(DemoApplication.getAppInstance(), getString(R.string.add_success) + "[" + device.getMacAddress() + "]", Toast.LENGTH_SHORT).show();
    }

    void onSubscribeFail(XDevice device, XLinkErrorCode xLinkErrorCode) {
        if (!isDestroyed()) {
            dismissLoading();
            showPromptDialog(getString(R.string.tips), getString(R.string.add_fail) + "[" + xLinkErrorCode + "]");
        }
    }

    public void showStartScanning() {
        topSubhead.setVisibility(View.VISIBLE);
        topSubhead.setText("(正在扫描)");
    }

    public void showCompleteScanning() {
        topSubhead.setVisibility(View.GONE);
        topSubhead.setText("");
        if (!isDestroyed()) {
            dismissLoading();
            if (mPresenter.getDevices().size() > 0) {
                return;
            }
            showConfirmDialog(getString(R.string.tips), getString(R.string.scan_not_device), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.scan(mTargetPid);
                }
            });
        }
    }

    public void onSubscribeDeviceComplete() {
        finish();
    }
}
