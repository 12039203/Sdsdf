package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * RecyclerView通用的基础Adapter。
 */
abstract class BaseSingleAdapter<T, H extends BaseViewHolder> extends RecyclerView.Adapter<H>
        implements RecyclerItemClickListener {

    @LayoutRes
    private int layoutId;
    private List<T> data;

    /**
     * @param layoutId adapter需要的布局资源id
     * @param data     数据
     */
    BaseSingleAdapter(@LayoutRes int layoutId, List<T> data) {
        this.layoutId = layoutId;
        this.data = data;
        setHasStableIds(true);
    }

    // 子类获得layoutId
    protected int getLayoutId() {
        return layoutId;
    }

    /**
     * @param position item下标
     * @return 获得item数据封装
     */
    protected T getItem(int position) {
        if (data != null && data.size() > position)
            return data.get(position);
        return null;
    }

    /**
     * @return 数据总数
     */
    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        return 0;
    }

    // 设置ID，保证item操作不错乱
    @Override
    public long getItemId(int position) {
        T t = getItem(position);
        if (t != null)
            return t.hashCode();
        else
            return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    // 创建hold
    @SuppressWarnings("unchecked")
    @Override
    public H onCreateViewHolder(ViewGroup parent, int viewType) {
        return (H) H.getHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false),
                layoutId, viewType, this);
    }

    // 绑定hold
    @Override
    public void onBindViewHolder(H holder, int position) {
        convert(holder, getItem(position), position);
    }

    /**
     * 实现该抽象方法，完成数据的填充。
     *
     * @param holder   {@link H}
     * @param t        每个 position 对应的对象
     * @param position 当前行数，采用{@link H#getLayoutPosition()}
     */
    public abstract void convert(H holder, T t, int position);

    // 单击
    @Override
    public void onItemClick(View itemView, int position, int viewType) {
    }

    // 长按
    @Override
    public boolean onItemLongClick(View itemView, int position, int viewType) {
        return false;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
