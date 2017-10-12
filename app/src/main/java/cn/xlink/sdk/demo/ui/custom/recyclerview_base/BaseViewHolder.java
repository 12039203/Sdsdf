package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;


/**
 * 自定义 RecyclerView 的 ViewHolder
 */
public class BaseViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    private final SparseArray<View> mCacheViews;
    @LayoutRes
    private int mLayoutId;
    private int mViewType;
    private RecyclerItemClickListener mClickListener;

    private BaseViewHolder(View root, @LayoutRes int layoutId, int viewType,
                           RecyclerItemClickListener mClickListener) {
        super(root);
        this.mCacheViews = new SparseArray<>();
        this.mLayoutId = layoutId;
        this.mViewType = viewType;
        this.mClickListener = mClickListener;

        //添加监听事件
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    static BaseViewHolder getHolder(View root, @LayoutRes int layoutId, int viewType,
                                    RecyclerItemClickListener listener) {
        return new BaseViewHolder(root, layoutId, viewType, listener);
    }

    /**
     * 得到view
     *
     * @param viewId view在当前layout里设置的id
     * @param <T>    view的子类型
     * @return view的子类型实例
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends View> T getView(@IdRes int viewId) {
        View view = mCacheViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mCacheViews.put(viewId, view);
        }
        return (T) view;
    }

    @Override
    public void onClick(View v) {
        mClickListener.onItemClick(v, getLayoutPosition(), getViewType());
    }

    @Override
    public boolean onLongClick(View v) {
        return mClickListener.onItemLongClick(v, getLayoutPosition(), getViewType());
    }

    public Context getContext() {
        return itemView.getContext();
    }

    @LayoutRes
    public int getLayoutId() {
        return mLayoutId;
    }

    public int getViewType() {
        return mViewType;
    }

    public void setText(@IdRes int viewId, @StringRes int resId) {
        TextView tv = getView(viewId);
        tv.setText(resId);
    }

    public void setText(@IdRes int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
    }
}
