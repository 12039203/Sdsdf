package cn.xlink.sdk.demo.ui.custom.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;

/**
 * 基础activity
 */

public abstract class BaseActivity extends AppCompatActivity {

    private AppDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (StatusBarCompat.isMIUI()) {
                StatusBarCompat.setMiuiStatusBarDarkMode(this.getWindow(), !isDarkMode());
            }

            if (StatusBarCompat.isFlyme()) {
                StatusBarCompat.setMeizuStatusBarDarkIcon(this.getWindow(), !isDarkMode());
            }
        }

        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public Context getContext() {
        return this;
    }

    public BaseActivity getActivity() {
        return this;
    }

    public void showPromptDialog(@StringRes int title, @StringRes int prompt) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextOneButton(getContext(), getString(title), getString(prompt)).show();
    }

    public void showPromptDialog(String title, String prompt) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextOneButton(getContext(), title, prompt).show();
    }

    public void showPromptDialog(String title, String prompt, View.OnClickListener clickListener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextOneButton2(getContext(), title, prompt, clickListener).show();
    }

    public void showConfirmDialog(@StringRes int title, @StringRes int prompt,
                                  View.OnClickListener listener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextDoubleButton(getContext(), getString(title), getString(prompt), null, listener).show();
    }

    public void showConfirmDialog(String title, String prompt, View.OnClickListener listener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextDoubleButton(getContext(), title, prompt, null, listener).show();
    }

    public void showConfirmDialog(String title, String prompt, View.OnClickListener cancelListener, View.OnClickListener confirmListener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextDoubleButton(getContext(), title, prompt, cancelListener, confirmListener).show();
    }

    public void showConfirmDialog(String title, String prompt, String leftBtnTitle, String rightBtnTitle, View.OnClickListener cancelListener, View.OnClickListener confirmListener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextDoubleButton(getContext(), title, prompt, leftBtnTitle, rightBtnTitle, cancelListener, confirmListener).show();
    }

    public void showBoolValueDialog(String title, boolean value,
                                         AppDialog.OnUpdateListener<Boolean> listener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextCheckBox(getContext(), title, value, listener).show();
    }

    public void showValueDialog(String title, int value, int min, int max,
                                AppDialog.OnUpdateListener<Integer> listener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextSeekBar(getContext(), title, value, min, max, listener).show();
    }

    public void showEditDialog(String title, String value,
                               AppDialog.OnUpdateListener<String> listener) {
        if (isDestroyed())
            return;
        AppDialog.doubleTextEditText(getContext(), title, value, listener).show();
    }


    public void showLoading() {
        if (getContext() != null && !this.isFinishing() && !this.isDestroyed()) {
            if (mLoadingDialog == null) {
                mLoadingDialog = AppDialog.loading(getContext());
            }
            mLoadingDialog.show();
        }
    }

    public void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing())
            mLoadingDialog.dismiss();
    }

    protected boolean isDarkMode() {
        return false;
    }

    //////////////////////////////////////////////////////////////////////

    protected abstract int getLayoutId();
}
