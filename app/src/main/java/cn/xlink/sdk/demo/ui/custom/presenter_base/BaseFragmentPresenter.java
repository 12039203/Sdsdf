package cn.xlink.sdk.demo.ui.custom.presenter_base;

import android.content.Context;
import android.support.annotation.StringRes;

import cn.xlink.sdk.demo.ui.custom.base.BaseFragment;


/**
 * fragment 基础 Presenter
 */
public class BaseFragmentPresenter<T extends BaseFragment> extends BasePresenter<T> {
    public BaseFragmentPresenter(T t) {
        super(t);
    }

    protected Context getContext() {
        return getView().getContext();
    }

    protected String getString(@StringRes int stringRes) {
        return getView().getString(stringRes);
    }

    protected String getString(@StringRes int stringRes, Object... formatArgs) {
        return getView().getString(stringRes, formatArgs);
    }
}
