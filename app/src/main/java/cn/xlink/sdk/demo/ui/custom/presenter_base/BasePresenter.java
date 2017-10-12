package cn.xlink.sdk.demo.ui.custom.presenter_base;

import java.lang.ref.WeakReference;

/**
 * 基础 Presenter
 */

class BasePresenter<T> {
    private WeakReference<T> mView;

    BasePresenter(T t) {
        mView = new WeakReference<>(t);
    }

    protected T getView() {
        return mView.get();
    }

    protected boolean isViewExisted() {
        return mView.get() != null;
    }
}
