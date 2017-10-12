package cn.xlink.sdk.demo.ui.module.share;

import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.XLinkCallback;
import cn.xlink.restful.XLinkRestful;
import cn.xlink.restful.XLinkRestfulError;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.demo.eventbus.UpdateListEvent;
import cn.xlink.sdk.demo.manager.DeviceManager;
import cn.xlink.sdk.demo.manager.UserManager;
import cn.xlink.sdk.demo.ui.custom.presenter_base.BaseActivityPresenter;
import cn.xlink.sdk.demo.ui.module.DemoApplication;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkErrorCode;
import cn.xlink.sdk.v5.module.main.XLinkSDK;
import cn.xlink.sdk.v5.module.share.XLinkHandleShareDeviceTask;

/**
 * Created by legendmohe on 2017/6/27.
 */

public class ShareListPresenter extends BaseActivityPresenter<ShareListActivity> {
    private static final String TAG = "ShareListPresenter";

    private List<DeviceApi.ShareDeviceItem> mAllShareRequests = new ArrayList<>();

    public ShareListPresenter(ShareListActivity shareListActivity) {
        super(shareListActivity);
    }

    public void updateShareList() {
        if (getView() != null) {
            getView().showGetShareStart();
        }
        XLinkRestful.getApplicationApi().getDeviceShareList().enqueue(new XLinkCallback<List<DeviceApi.ShareDeviceItem>>() {

            @Override
            public void onHttpError(Throwable throwable) {
                Log.d(TAG, "onHttpError() called with: throwable = [" + throwable + "]");
                if (getView() != null && !getView().isDestroyed()) {
                    getView().showGetShareError(throwable);
                }
            }

            @Override
            public void onApiError(XLinkRestfulError.ErrorWrapper.Error error) {
                Log.d(TAG, "onApiError() called with: error = [" + error + "]");
                if (getView() != null && !getView().isDestroyed()) {
                    getView().showGetShareError(error);
                }
            }

            @Override
            public void onSuccess(List<DeviceApi.ShareDeviceItem> shareRequests) {
                Log.d(TAG, "onSuccess() called with: shareRequests = [" + shareRequests + "]");
                mAllShareRequests = shareRequests;
                if (getView() != null && !getView().isDestroyed()) {
                    getView().showGetShareSuccess(shareRequests);
                }
            }
        });
    }

    public List<DeviceApi.ShareDeviceItem> getAllShareRequests() {
        return mAllShareRequests;
    }

    /**
     * 接受分享
     *
     * @param request
     */
    public void acceptShareRequest(DeviceApi.ShareDeviceItem request) {
        handleShareRequest(request, XLinkHandleShareDeviceTask.Action.ACCEPT);
    }

    /**
     * 拒绝分享
     *
     * @param request
     */
    public void denyShareRequest(DeviceApi.ShareDeviceItem request) {
        handleShareRequest(request, XLinkHandleShareDeviceTask.Action.DENY);
    }

    /**
     * 删除分享条目
     *
     * @param request
     */
    public void deleteShareRequest(DeviceApi.ShareDeviceItem request) {
        handleShareRequest(request, XLinkHandleShareDeviceTask.Action.DELETE);
    }

    /**
     * 取消分享（回收权限）
     *
     * @param request
     */
    public void cancelShareRequest(DeviceApi.ShareDeviceItem request) {
        handleShareRequest(request, XLinkHandleShareDeviceTask.Action.CANCEL);
    }

    private void handleShareRequest(DeviceApi.ShareDeviceItem request, final XLinkHandleShareDeviceTask.Action action) {
        XLinkHandleShareDeviceTask task = XLinkHandleShareDeviceTask.newBuilder()
                .setAction(action)
                .setInviteCode(request.inviteCode)
                .setUid(UserManager.getInstance().getUid())
                .setListener(new XLinkTaskListener<String>() {
                    @Override
                    public void onError(XLinkErrorCode xLinkErrorCode) {
                        if (getView() != null && !getView().isDestroyed()) {
                            getView().showHandleShareError(getThrowable());
                        }
                    }

                    @Override
                    public void onStart() {
                        if (getView() != null && !getView().isDestroyed()) {
                            getView().showHandleShareStart();
                        }
                    }

                    @Override
                    public void onComplete(String s) {
                        if (getView() != null && !getView().isDestroyed()) {
                            getView().showHandleShareSuccess(s);
                        }

                        updateShareList();

                        if (action == XLinkHandleShareDeviceTask.Action.ACCEPT) {
                            DeviceManager.getInstance().refreshDeviceList(new XLinkTaskListener<List<XDevice>>() {
                                @Override
                                public void onError(XLinkErrorCode xLinkErrorCode) {
                                    Toast.makeText(DemoApplication.getAppInstance(), "刷新失败：" + xLinkErrorCode, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onComplete(List<XDevice> xDevices) {
                                    // 通知添加完成
                                    EventBus.getDefault().post(new UpdateListEvent());
                                }
                            });
                        }
                    }
                })
                .build();
        XLinkSDK.startTask(task);
    }
}
