package cn.xlink.sdk.demo.ui.custom.base;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.xlink.sdk.demo.R;


/**
 * App 用到的dialog类型
 *
 * @author sswukang on 2016/8/26 11:00
 * @version 1.0
 */

public class AppDialog extends Dialog {

    private AppDialog(Context context, View view, @LayoutRes int layoutRes) {
        super(context, R.style.AppDialog_Bottom);
        // set content
        if (view != null) setContentView(view);
        else setContentView(layoutRes);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
        }
    }

    public static AppDialog bottomSheetList(Context context, @NonNull String[] listContent,
                                            AdapterView.OnItemClickListener listener) {
        LinearLayoutCompat list = new LinearLayoutCompat(context);
        list.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundColor_white));
        list.setOrientation(LinearLayoutCompat.VERTICAL);
        list.setShowDividers(LinearLayoutCompat.SHOW_DIVIDER_MIDDLE);
        list.setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.divider_line));

        for (int i = 0, j = listContent.length; i < j; i++) {
            list.addView(textItem(context, listContent[i], j > 2 && i == j - 1, i, listener));
        }

        AppDialog dialog = new AppDialog(context, list, 0);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private static TextView textItem(Context context, String content, boolean isLast, final int index,
                                     final AdapterView.OnItemClickListener listener) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55,
                context.getResources().getDisplayMetrics());
        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);

        TextView tv = new TextView(context);
        tv.setText(content);
        tv.setTextColor(ContextCompat.getColor(context, isLast ? R.color.pm_moderately_polluted : R.color.colorText));
        tv.setLayoutParams(params);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(null, v, index, v.getId());
            }
        });
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private AppDialog(Context context, @LayoutRes int layoutRes, int width, int height) {
        super(context, R.style.AppDialog);
        // set content
        setContentView(layoutRes);

        Window window = getWindow();
        if (window != null) {

            WindowManager.LayoutParams params = window.getAttributes();
            if (width > 0) {
                params.width = width;
            } else {
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            }
            if (height > 0) {
                params.height = height;
            } else {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            }

            window.setAttributes(params);
        }
    }

    /**
     * 简单的加载等待框
     */
    public static AppDialog loading(Context context) {
        AppDialog dialog = new AppDialog(context, R.layout.dialog_loading,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.35f),
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.35f));
        dialog.setCanceledOnTouchOutside(false);

        ContentLoadingProgressBar bar = (ContentLoadingProgressBar) dialog.findViewById(R.id.loading_progress);
        if (bar != null) {
            bar.show();
        }

        return dialog;
    }

    public static AppDialog userExtrusion(Context context, View.OnClickListener listener) {
        AppDialog dialog = doubleTextOneButton(context, context.getString(R.string.user_extrusion_title),
                context.getString(R.string.user_extrusion_content), listener);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * 包含标题、内容和单按钮的dialog
     */
    public static AppDialog doubleTextOneButton(Context context, String title, String content) {
        return doubleTextOneButton(context, title, content, null);
    }

    /**
     * 包含标题、内容和单按钮的dialog  点击事件
     */
    public static AppDialog doubleTextOneButton2(Context context, String title, String content, View.OnClickListener clickListener) {
        return doubleTextOneButton(context, title, content, clickListener);
    }


    /**
     * 包含标题、内容和单按钮以及按钮监听的dialog
     */
    private static AppDialog doubleTextOneButton(Context context, String title, String content,
                                                 final View.OnClickListener listener) {
        final AppDialog dialog = new AppDialog(context, R.layout.dialog_double_text_one_button,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f), 0);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.text_title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title))
                tvTitle.setText(title);
        }
        TextView tvContent = (TextView) dialog.findViewById(R.id.text_content);
        if (tvContent != null) {
            if (!TextUtils.isEmpty(content))
                tvContent.setText(content);
        }
        AppCompatButton bt = (AppCompatButton) dialog.findViewById(R.id.text_button);
        if (bt != null) {
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onClick(v);
                    dialog.dismiss();
                }
            });
        }

        return dialog;
    }

    static AppDialog doubleTextDoubleButton(Context context, String title, String content,
                                            View.OnClickListener leftListener, View.OnClickListener rightListener) {
        return doubleTextDoubleButton(context, title, content, null, null, leftListener, rightListener);
    }

    /**
     * 包含标题、内容和双按钮的dialog
     */
    public static AppDialog doubleTextDoubleButton(Context context, String title, String content,
                                                   String left, String right, final View.OnClickListener leftListener,
                                                   final View.OnClickListener rightListener) {
        final AppDialog dialog = new AppDialog(context, R.layout.dialog_double_text_double_button,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f), 0);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.text_title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title))
                tvTitle.setText(title);
        }
        TextView tvContent = (TextView) dialog.findViewById(R.id.text_content);
        if (tvContent != null) {
            if (!TextUtils.isEmpty(content))
                tvContent.setText(content);
        }

        AppCompatButton cancel = (AppCompatButton) dialog.findViewById(R.id.button_cancel);
        if (!TextUtils.isEmpty(left)) {
            cancel.setText(left);
        }
        if (cancel != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (leftListener != null)
                        leftListener.onClick(v);
                }
            });
        }

        AppCompatButton confirm = (AppCompatButton) dialog.findViewById(R.id.button_confirm);
        if (!TextUtils.isEmpty(right)) {
            confirm.setText(right);
        }
        if (confirm != null) {
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (rightListener != null)
                        rightListener.onClick(v);
                }
            });
        }

        return dialog;
    }


    /**
     * 包含标题、内容和checkbox的dialog
     */
    public static AppDialog doubleTextCheckBox(Context context, String title, boolean value,
                                               final OnUpdateListener<Boolean> listener) {
        final AppDialog dialog = new AppDialog(context, R.layout.dialog_set_value,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f), 0);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.text_title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title))
                tvTitle.setText(title);
        }
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.item_bool);
        checkBox.setVisibility(View.VISIBLE);
        if (checkBox != null) {
            checkBox.setChecked(value);
        }

        AppCompatButton cancel = (AppCompatButton) dialog.findViewById(R.id.button_cancel);
        cancel.setText(context.getString(R.string.cancel));

        if (cancel != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        AppCompatButton confirm = (AppCompatButton) dialog.findViewById(R.id.button_confirm);
        confirm.setText(context.getString(R.string.confirm));
        if (confirm != null) {
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (listener != null)
                        listener.onUpdate(checkBox.isChecked());
                }
            });
        }

        return dialog;
    }

    /**
     * 包含标题、内容和seekbar的dialog
     */
    public static AppDialog doubleTextSeekBar(Context context, String title, final int value, int min, int max,
                                              final OnUpdateListener<Integer> listener) {
        final AppDialog dialog = new AppDialog(context, R.layout.dialog_set_value,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f), 0);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.text_title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title))
                tvTitle.setText(title);
        }

        final TextView tvValue = (TextView) dialog.findViewById(R.id.item_value);
        tvValue.setVisibility(View.VISIBLE);
        if (tvValue != null) {
            tvValue.setText(value + "");
        }

        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.item_seekbar);
        seekBar.setVisibility(View.VISIBLE);
        if (seekBar != null) {
            seekBar.setMax(max - min);
            seekBar.setProgress(value - min);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvValue.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AppCompatButton cancel = (AppCompatButton) dialog.findViewById(R.id.button_cancel);
        cancel.setText(context.getString(R.string.cancel));

        if (cancel != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        AppCompatButton confirm = (AppCompatButton) dialog.findViewById(R.id.button_confirm);
        confirm.setText(context.getString(R.string.confirm));
        if (confirm != null) {
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (listener != null)
                        listener.onUpdate(seekBar.getProgress());
                }
            });
        }

        return dialog;
    }

    /**
     * 包含标题、内容和edittext的dialog
     */
    public static AppDialog doubleTextEditText(Context context, String title, String value,
                                               final OnUpdateListener<String> listener) {
        final AppDialog dialog = new AppDialog(context, R.layout.dialog_set_value,
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f), 0);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.text_title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title))
                tvTitle.setText(title);
        }
        final EditText editText = (EditText) dialog.findViewById(R.id.item_edittext);
        editText.setVisibility(View.VISIBLE);
        if (editText != null) {
            editText.setText(value);
        }

        AppCompatButton cancel = (AppCompatButton) dialog.findViewById(R.id.button_cancel);
        cancel.setText(context.getString(R.string.cancel));

        if (cancel != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        AppCompatButton confirm = (AppCompatButton) dialog.findViewById(R.id.button_confirm);
        confirm.setText(context.getString(R.string.confirm));
        if (confirm != null) {
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (listener != null)
                        listener.onUpdate(editText.getText().toString());
                }
            });
        }

        return dialog;
    }


    public interface OnUpdateListener<T> {
        void onUpdate(T obj);
    }

}
