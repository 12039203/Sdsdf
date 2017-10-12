package cn.xlink.sdk.demo.ui.module.share;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.demo.R;

/**
 * Created by legendmohe on 2017/6/28.
 */

public class ShareListAdapter extends RecyclerView.Adapter<ShareListAdapter.ViewHolder> {

    public static final int MENU_ID_SHARE_CANCEL = 100;
    public static final int MENU_ID_SHARE_DELETE = 101;
    public static final int MENU_ID_SHARE_ACCEPT = 102;
    public static final int MENU_ID_SHARE_DENY = 103;

    private List<DeviceApi.ShareDeviceItem> mData = new ArrayList<>();

    private OnMenuItemClickListener mOnMenuItemClickListener;

    public ShareListAdapter(List<DeviceApi.ShareDeviceItem> data) {
        mData = data;
    }

    ShareListAdapter() {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_content_textView)
        TextView mItemContentTextView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public void setData(List<DeviceApi.ShareDeviceItem> data) {
        mData = data;
    }

    public List<DeviceApi.ShareDeviceItem> getData() {
        return mData;
    }

    @Override
    public long getItemId(int position) {

        return super.getItemId(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_share_device_list_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DeviceApi.ShareDeviceItem request = mData.get(position);
        final MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mOnMenuItemClickListener != null) {
                    return mOnMenuItemClickListener.onMenuItemClick(item, request);
                }
                return false;
            }
        };

        holder.mItemContentTextView.setText(formatShareRequest(request));
        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem mCancelItem = menu.add(Menu.NONE, MENU_ID_SHARE_CANCEL, Menu.NONE, "cancel");
                mCancelItem.setOnMenuItemClickListener(onMenuItemClickListener);
                MenuItem mDenyItem = menu.add(Menu.NONE, MENU_ID_SHARE_DENY, Menu.NONE, "deny");
                mDenyItem.setOnMenuItemClickListener(onMenuItemClickListener);
                MenuItem mAcceptItem = menu.add(Menu.NONE, MENU_ID_SHARE_ACCEPT, Menu.NONE, "accept");
                mAcceptItem.setOnMenuItemClickListener(onMenuItemClickListener);
                MenuItem mDeleteItem = menu.add(Menu.NONE, MENU_ID_SHARE_DELETE, Menu.NONE, "delete");
                mDeleteItem.setOnMenuItemClickListener(onMenuItemClickListener);
            }
        }); //REGISTER ONCREATE MENU LISTENER
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.showContextMenu();
            }
        });
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        mOnMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    private String formatShareRequest(DeviceApi.ShareDeviceItem shareRequest) {
        StringBuffer sb = new StringBuffer();
        sb.append("id: ").append(shareRequest.id).append("\n");
        sb.append("device_id: ").append(shareRequest.deviceId).append("\n");
        sb.append("from_user: ").append(shareRequest.fromUser).append("\n");
        sb.append("to_user: ").append(shareRequest.toUser).append("\n");
        sb.append("state: ").append(shareRequest.state).append("\n");
        sb.append("invite_code: ").append(shareRequest.inviteCode);
        return sb.toString();
    }

    public interface OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item, DeviceApi.ShareDeviceItem data);
    }
}
