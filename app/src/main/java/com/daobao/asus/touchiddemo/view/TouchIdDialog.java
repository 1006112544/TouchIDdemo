package com.daobao.asus.touchiddemo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daobao.asus.touchiddemo.R;

/**
 * TouchID dialog
 * Created by db on 2018/8/19.
 */
public class TouchIdDialog extends Dialog implements View.OnClickListener{
    private TextView mCancelTv;
    private TextView mPassWordTv;
    private Context mContext;
    private ImageView mTouchIdImg;

    public TouchIdDialog(@NonNull Context context) {
        this(context,0);
    }

    public TouchIdDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_touch_id);
        initView();
    }

    private void initView() {
        mCancelTv = findViewById(R.id.touch_id_cancel_tv);
        mCancelTv.setOnClickListener(this);
        mPassWordTv = findViewById(R.id.touch_id_password_tv);
        mPassWordTv.setOnClickListener(this);
        mTouchIdImg = findViewById(R.id.ic_touch_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.touch_id_cancel_tv:
                TouchIdDialog.this.dismiss();
                break;
            case R.id.touch_id_password_tv:
                TouchIdDialog.this.dismiss();
                Toast.makeText(mContext, "自行发挥", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void startIconShackAnimation(){
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.shack_animation);
        anim.setFillAfter(false);
        mTouchIdImg.startAnimation(anim);
    }
}
