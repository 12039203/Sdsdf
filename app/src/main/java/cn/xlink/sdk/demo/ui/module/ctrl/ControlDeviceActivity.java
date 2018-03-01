package cn.xlink.sdk.demo.ui.module.ctrl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.ui.custom.constant.Constant;
import cn.xlink.sdk.demo.eventbus.DataPointUpdateEvent;
import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.model.Device;
import cn.xlink.sdk.demo.ui.custom.base.AppDialog;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.BaseMultiAdapter;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.BaseViewHolder;
import cn.xlink.sdk.demo.ui.custom.recyclerview_base.MultiItemTypeSupport;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.demo.utils.ByteUtil;
import cn.xlink.sdk.v5.listener.XLinkDeviceStateListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.model.XLinkDataPoint;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

/**
 * Created by CHENJIAHUI on 2017/2/26.
 * 设备控制界面
 */

public class ControlDeviceActivity extends BaseActivity {
    private static final String TAG = "ControlDeviceActivity";

    @BindView(R.id.top_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.top_title)
    TextView mTopTitle;
    @BindView(R.id.top_subhead)
    TextView mTopSubhead;
    @BindView(R.id.device_ctrl_list)
    RecyclerView mDataPointRecyclerView;
    @BindView(R.id.offline_mask_layout)
    ViewGroup mViewGroup;

    private BaseMultiAdapter<XLinkDataPoint, BaseViewHolder> mItemAdapter;

    private ControlDevicePresenter mPresenter;

    private Device mDevice;

    /*
        设备离线时显示一个蒙板
     */
    private XLinkDeviceStateListener mDeviceStateListener = new XLinkDeviceStateListener() {
        @Override
        public void onDeviceStateChanged(XDevice xDevice, XDevice.State state) {
            if (xDevice.equals(mDevice.getXDevice())) {
                mViewGroup.setVisibility(state == XDevice.State.CONNECTED ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        public void onDeviceChanged(XDevice xDevice, XDevice.Event event) {

        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.control_activity_device;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataPointUpdateEvent(DataPointUpdateEvent event) {
        refreshAdapter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        String mac = getIntent().getStringExtra(Constant.BUNDLE_DEVICE_MAC);
        mDevice = DeviceManager.getInstance().getDevice(mac);
        mPresenter = new ControlDevicePresenter(this, mDevice);

        initView();
        initRecycler();
        initData();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        XLinkSDK.getDeviceManager().removeDeviceStateListener(mDeviceStateListener);
        super.onDestroy();
    }

    private void initData() {
        XLinkSDK.getDeviceManager().addDeviceStateListener(mDeviceStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.getDataPoint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_control_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_device:
                showConfirmDialog(getString(R.string.prompt_title), getString(R.string.comfirm_delete_device), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPresenter.doRemoveDevice();
                    }
                });
                break;
            case R.id.share_device:
                showEditDialog(
                        getString(R.string.comfirm_share_device),
                        "",
                        new AppDialog.OnUpdateListener<String>() {
                            @Override
                            public void onUpdate(String val) {

                                mPresenter.doShareDevice(mDevice, val);
                            }
                        }
                );
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        mTopTitle.setVisibility(View.VISIBLE);
        mTopTitle.setText(mDevice.getXDevice().getMacAddress());
        mTopSubhead.setVisibility(View.GONE);
        mToolbar.setBackgroundColor(0);
        assert null != getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewGroup.setVisibility(mDevice.getXDevice().getConnectionState() == XDevice.State.CONNECTED ? View.GONE : View.VISIBLE);
    }

    private void initRecycler() {
        //设置布局管理器
        mDataPointRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDataPointRecyclerView.setItemAnimator(null);
        //设置adapter
        mItemAdapter = new BaseMultiAdapter<XLinkDataPoint, BaseViewHolder>(
                mDevice.getDataPoints(),
                new MultiItemTypeSupport<XLinkDataPoint>() {
                    @Override
                    public int getLayoutId(int viewType) {
                        switch (viewType) {
                            case 0: // BOOL
                                return R.layout.datapoint_item_switch;
                            case 1: // BYTE
                            case 2: // SHORT
                            case 3: // USHORT
                            case 4: // INT
                            case 5: // UINT
                            case 6: // LONG
                            case 7: // ULONG
                                return R.layout.datapoint_item_slider;
                            case 8: // FLOAT
                            case 9: // DOUBLE
                            case 10: // STRING
                            case 11: // BYTE_ARRAY
                            default:
                                return R.layout.datapoint_item_input;
                        }
                    }

                    @Override
                    public int getItemViewType(int position, XLinkDataPoint dataPoint) {
                        return dataPoint.getType().ordinal();
                    }
                }) {

            @Override
            public void convert(BaseViewHolder holder, XLinkDataPoint dataPoint, int position) {
                int viewType = getItemViewType(position);
                switch (viewType) {
                    case 0: // BOOL
                        handleSwitchItemType(holder, dataPoint, position);
                        break;
                    case 1: // BYTE
                    case 2: // SHORT
                    case 3: // USHORT
                    case 4: // INT
                    case 5: // UINT
                    case 6: // LONG
                    case 7: // ULONG
                        handleSliderItemType(holder, dataPoint, position);
                        break;
                    case 8: // FLOAT
                    case 9: // DOUBLE
                    case 10: // STRING
                    case 11: // BYTE_ARRAY
                    default:
                        handleInputItemType(holder, dataPoint, position);
                        break;
                }
            }

            private void handleSwitchItemType(BaseViewHolder holder, final XLinkDataPoint dataPoint, int position) {
                holder.setText(R.id.item_index, getString(R.string.index) + dataPoint.getIndex());
                holder.setText(R.id.item_name, getString(R.string.dp_name) + dataPoint.getName());

                final boolean checked = (Boolean) dataPoint.getValue();
                Switch sw = holder.getView(R.id.valueSwitch);
                if (sw == null) {
                    return;
                }
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (checked != isChecked) {
                            mPresenter.setDataPoint(dataPoint.getIndex(), dataPoint.getType(), isChecked);
                        }
                    }
                });
                sw.setChecked(checked);
            }

            private void handleSliderItemType(BaseViewHolder holder, final XLinkDataPoint dataPoint, int position) {
                holder.setText(R.id.item_index, getString(R.string.index) + dataPoint.getIndex());
                holder.setText(R.id.item_name, getString(R.string.dp_name) + dataPoint.getName());

                SeekBar seekBar = holder.getView(R.id.valueSeekBar);
                if (dataPoint.getMax() > Integer.MAX_VALUE) {
                    seekBar.setMax(Integer.MAX_VALUE);
                } else {
                    seekBar.setMax((int) dataPoint.getMax());
                }
                final int curValue = seekBar.getProgress();
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "onStopTrackingTouch() called with: seekBar = [" + seekBar + "]");
                        if (curValue == seekBar.getProgress())
                            return;

                        Object targetValue = null;
                        switch (dataPoint.getType()) {
                            case BYTE:
                                targetValue = ((byte) seekBar.getProgress());
                                break;
                            case SHORT:
                                targetValue = (short) seekBar.getProgress();
                                break;
                            case USHORT:
                                targetValue = (short) seekBar.getProgress();
                                break;
                            case INT:
                                targetValue = seekBar.getProgress();
                                break;
                            case UINT:
                                targetValue = seekBar.getProgress();
                                break;
                            case LONG:
                            case ULONG:
                                targetValue = (long) seekBar.getProgress();
                                break;
                        }
                        mPresenter.setDataPoint(dataPoint.getIndex(), dataPoint.getType(), targetValue);
                    }

                });

                int value = 0;
                switch (dataPoint.getType()) {
                    case BYTE:
                    case BOOL:
                        value = ((Byte) dataPoint.getValue()) & 0xff;
                        break;
                    case SHORT:
                        value = ((Short) dataPoint.getValue());
                        break;
                    case USHORT:
                        value = ((Short) dataPoint.getValue()) & 0xffff;
                        break;
                    case INT:
                        value = ((Integer) dataPoint.getValue());
                        break;
                    case UINT:
                        value = ((Integer) dataPoint.getValue()) & 0x7fffffff;
                        break;
                    case LONG:
                    case ULONG:
                        value = (int) seekBar.getProgress();
                        break;
                }
                seekBar.setProgress(value);
                holder.setText(R.id.valueTextView, "" + value);
            }

            private void handleInputItemType(BaseViewHolder holder, final XLinkDataPoint dataPoint, int position) {
                holder.setText(R.id.item_index, getString(R.string.index) + dataPoint.getIndex());
                holder.setText(R.id.item_name, getString(R.string.dp_name) + dataPoint.getName());

                final String title = getString(R.string.index) + dataPoint.getIndex();
                String hint = "";
                switch (dataPoint.getType()) {
                    case FLOAT:
                    case DOUBLE:
                        hint = dataPoint.getValue().toString();
                        break;
                    case STRING:
                        if (dataPoint.getValue() != null) {
                            hint = dataPoint.getValue().toString();
                        }
                        break;
                    case BYTE_ARRAY:
                        if (dataPoint.getValue() != null) {
                            hint = ByteUtil.bytesToHexString((byte[]) dataPoint.getValue());
                        }
                        break;
                }

                TextView editText = holder.getView(R.id.valueTextView);
                editText.setText(hint);

                final String finalHint = hint;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(title, finalHint, new AppDialog.OnUpdateListener<String>() {
                            @Override
                            public void onUpdate(String obj) {
                                Object value = null;
                                try {
                                    switch (dataPoint.getType()) {
                                        case FLOAT: {
                                            value = Float.valueOf(obj);
                                            if ((float) value > Float.MAX_VALUE
                                                    || (float) value < -Float.MAX_VALUE) {
                                                // 超出float的范围
                                                Toast.makeText(DemoApplication.getAppInstance(), "输入不合法", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        break;
                                        case DOUBLE:
                                            value = Double.valueOf(obj);
                                            break;
                                        case STRING:
                                            value = obj.trim();
                                            break;
                                        case BYTE_ARRAY:
                                            value = ByteUtil.hexToBytes(obj);
                                            break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(DemoApplication.getAppInstance(), "输入不合法", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (value != null) {
                                    mPresenter.setDataPoint(dataPoint.getIndex(), dataPoint.getType(), value);
                                }
                            }
                        });
                    }
                });
            }
        };
        mDataPointRecyclerView.setAdapter(mItemAdapter);
    }

    void refreshAdapter() {
        if (mItemAdapter != null) {
            mItemAdapter.setData(mDevice.getDataPoints());
            mItemAdapter.notifyDataSetChanged();
        }
    }

    public void showRemoveDeviceError(XLinkErrorCode errorCode) {
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.prompt_delete_device_error) + ":" + errorCode);
    }

    public void showSuccessRemoveDevice(Device device) {
        DeviceManager.getInstance().removeDevice(device);
        supportFinishAfterTransition();
    }

    public void showShareDeviceFail(XLinkErrorCode xLinkErrorCode) {
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.share_device_fail) + "\n" + xLinkErrorCode);
    }

    public void showShareDeviceSuccess(String s) {
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.share_device_success) + "\n" + s);
    }

    public void showShareDeviceOffline() {
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.share_device_offline));
    }

    //////////////////////////////////////////////////////////////////////
}
