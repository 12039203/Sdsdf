package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.support.annotation.LayoutRes;
import android.view.View;

import java.util.List;

/**
 * 单一item布局adapter
 *
 * @author sswukang on 2016/10/10 9:33
 * @version 1.0
 */
public abstract class SingleItemAdapter<T> extends BaseSingleAdapter<T, BaseViewHolder> {
    /**
     * @param layoutId adapter需要的布局资源id
     * @param data     数据
     */
    protected SingleItemAdapter(@LayoutRes int layoutId, List<T> data) {
        super(layoutId, data);
    }

    @Override
    public final void onItemClick(View itemView, int position, int viewType) {
        onItemClick(itemView, getItem(position), position);
    }

    @Override
    public final boolean onItemLongClick(View itemView, int position, int viewType) {
        return onItemLongClick(itemView, getItem(position), position);
    }

    /**
     * item的单击事件
     *
     * @param itemView 触发点击事件的View
     * @param t        每个 position 对应的对象
     * @param position 当前行数，采用{@link BaseViewHolder#getLayoutPosition()}
     */
    public void onItemClick(View itemView, T t, int position) {

    }

    /**
     * item的长按事件
     *
     * @param itemView 触发点击事件的View
     * @param t        每个 position 对应的对象
     * @param position 当前行数，采用{@link BaseViewHolder#getLayoutPosition()}
     * @return 长按事件是否被消费
     */
    public boolean onItemLongClick(View itemView, T t, int position) {
        return false;
    }
}
