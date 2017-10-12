package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.support.annotation.LayoutRes;

/**
 * 多布局支持接口
 */
public interface MultiItemTypeSupport<T> {
    @LayoutRes
    int getLayoutId(int viewType);

    int getItemViewType(int position, T t);
}
