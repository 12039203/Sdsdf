package cn.xlink.sdk.demo.ui.custom.presenter_base;

import android.content.Context;
import android.support.annotation.StringRes;

import cn.xlink.sdk.demo.ui.custom.base.BaseActivity;


/**
 * activity基础 Presenter
 */
public class BaseActivityPresenter<T extends BaseActivity> extends BasePresenter<T> {
    public BaseActivityPresenter(T t) {
        super(t);
    }

    protected Context getContext() {
        if (isViewExisted())
            return getView().getContext();
        return null;
    }

    protected String getString(@StringRes int stringRes) {
        return getView().getString(stringRes);
    }

    protected String getString(@StringRes int stringRes, Object... formatArgs) {
        return getView().getString(stringRes, formatArgs);
    }
}
