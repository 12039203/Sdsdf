package cn.xlink.sdk.demo.ui.custom.recyclerview_base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * RecyclerView基础多布局Adapter。
 */
public abstract class BaseMultiAdapter<T, H extends BaseViewHolder> extends BaseSingleAdapter<T, H> {

    private MultiItemTypeSupport<T> multiItemTypeSupport;

    /**
     * @param data                 数据
     * @param multiItemTypeSupport 多布局支持接口
     */
    public BaseMultiAdapter(List<T> data, MultiItemTypeSupport<T> multiItemTypeSupport) {
        super(-1, data);
        this.multiItemTypeSupport = multiItemTypeSupport;

        if (multiItemTypeSupport == null)
            throw new IllegalArgumentException("the MultiItemTypeSupport<T> can not be null.");
    }

    // 根据item类型分配布局类型
    @Override
    public int getItemViewType(int position) {
        return multiItemTypeSupport.getItemViewType(position, getItem(position));
    }

    // 创建hold
    @SuppressWarnings("unchecked")
    @Override
    public H onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = multiItemTypeSupport.getLayoutId(viewType);
        return (H) H.getHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false),
                layoutId, viewType, this);
    }

}
