package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.view.View;

/**
 * RecyclerView Item 添加监听接口
 */
interface RecyclerItemClickListener {
    void onItemClick(View v, int position, int viewType);

    boolean onItemLongClick(View v, int position, int viewType);
}

