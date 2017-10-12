package cn.xlink.sdk.demo.ui.module.share;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.demo.R;
import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;

public class ShareListActivity extends BaseActivity {
    private static final String TAG = "ShareListActivity";

    @BindView(R.id.top_toolbar)
    Toolbar topToolbar;
    @BindView(R.id.top_title)
    TextView topTitle;
    @BindView(R.id.top_subhead)
    TextView topSubhead;
    @BindView(R.id.share_item_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    ShareListPresenter mPresenter;

    private ShareListAdapter mShareAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_share_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new ShareListPresenter(this);

        initViews();
    }

    private void initViews() {
        setSupportActionBar(topToolbar);
        topTitle.setVisibility(View.VISIBLE);
        topTitle.setText(getString(R.string.share_device_list));
        topSubhead.setVisibility(View.GONE);
        topToolbar.setBackgroundColor(0);
        assert null != getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mPresenter.updateShareList();
                    }
                }
        );

        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mPresenter.updateShareList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void refreshAdapter() {
        if (mShareAdapter != null) {
            mShareAdapter.setData(mPresenter.getAllShareRequests());
            mShareAdapter.notifyDataSetChanged();
        }
    }

    private void initRecyclerView() {
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), OrientationHelper.VERTICAL));
        //设置adapter
        mShareAdapter = new ShareListAdapter();
        mShareAdapter.setOnMenuItemClickListener(new ShareListAdapter.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item, DeviceApi.ShareDeviceItem request) {
                int itemId = item.getItemId();
                switch (itemId) {
                    case ShareListAdapter.MENU_ID_SHARE_ACCEPT:
                        mPresenter.acceptShareRequest(request);
                        break;
                    case ShareListAdapter.MENU_ID_SHARE_DELETE:
                        mPresenter.deleteShareRequest(request);
                        break;
                    case ShareListAdapter.MENU_ID_SHARE_CANCEL:
                        mPresenter.cancelShareRequest(request);
                        break;
                    case ShareListAdapter.MENU_ID_SHARE_DENY:
                        mPresenter.denyShareRequest(request);
                        break;
                }
                return false;
            }
        });
        mRecyclerView.setAdapter(mShareAdapter);
    }

    //////////////////////////////////////////////////////////////////////

    public void showGetShareError(Throwable error) {
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.get_share_list_error) + "\n" + error);
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(true);
    }

    public void showGetShareSuccess(List<DeviceApi.ShareDeviceItem> shareRequests) {
        refreshAdapter();
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(true);
    }

    public void showGetShareStart() {
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.setEnabled(false);
    }

    public void showHandleShareStart() {
        showLoading();
    }

    public void showHandleShareError(Throwable error) {
        dismissLoading();
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.get_share_handle_error) + "\n" + error);
    }

    public void showHandleShareSuccess(String inviteCode) {
        dismissLoading();
        showPromptDialog(getString(R.string.prompt_title), getString(R.string.share_handle_success));
        refreshAdapter();
    }
}
