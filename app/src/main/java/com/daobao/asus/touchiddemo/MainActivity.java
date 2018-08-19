package com.daobao.asus.touchiddemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button mTouchIdStartBtn;
    private FingerprintManager mFingerprintManager;
    private TouchIdDialog mTouchIdDialog;
    private CancellationSignal mCancellationSignal;//用于取消指纹识别
    private Boolean isStartAuthenticate = false;//记录是否开启了指纹识别
    private CryptoObjectCreator mCryptoObjectCreator; //用于创建CryptoObject

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTouchIdStartBtn = findViewById(R.id.touch_start_btn);
        initTouchId();
    }

    private void initTouchId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintManager = getFingerprintManagerOrNull();
            if (mFingerprintManager != null) {
                mTouchIdStartBtn.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        /**
                         * mFingerprintManager.isHardwareDetected()判断指纹识别硬件是否存在且能正常使用
                         * mFingerprintManager.hasEnrolledFingerprints()确定是否至少注册了一个指纹
                         */
                        if (mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()) {
                            mTouchIdStartBtn.setClickable(false);
                            if (mTouchIdDialog == null) {
                                mTouchIdDialog = new TouchIdDialog(MainActivity.this, R.style.TouchIdDialog);
                                mTouchIdDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        //如果dialog消失则取消指纹识别
                                        if (mCancellationSignal != null && isStartAuthenticate) {
                                            isStartAuthenticate = false;
                                            mCancellationSignal.cancel();
                                            mCancellationSignal = null;
                                        }
                                        mTouchIdStartBtn.setClickable(true);
                                    }
                                });
                            }
                            mTouchIdDialog.show();
                            mCancellationSignal = new CancellationSignal();
                            if(mCryptoObjectCreator==null){
                                initCryptoObject();
                            }else {
                                //开始验证指纹
                                mFingerprintManager.authenticate(mCryptoObjectCreator.getCryptoObject(), mCancellationSignal, 0, callback, null);
                                isStartAuthenticate = true;
                            }

                        }
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "无法开启指纹识别", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取FingerprintManager
     *
     * @return FingerprintManager
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerprintManager getFingerprintManagerOrNull() {
        if (getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return getApplication().getSystemService(FingerprintManager.class);
        } else {
            return null;
        }
    }

    /**
     * 指纹识别回调监听
     */
    private FingerprintManager.AuthenticationCallback callback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            mTouchIdDialog.dismiss();
            //指纹验证成功
            Toast.makeText(MainActivity.this, "指纹验证成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            mTouchIdDialog.dismiss();
            //指纹验证失败，不可再验
            Toast.makeText(MainActivity.this, "onAuthenticationError:" + errString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            mTouchIdDialog.startIconShackAnimation();
            //指纹验证失败，可再验，可能手指过脏，或者移动过快等原因。
            Toast.makeText(MainActivity.this, "onAuthenticationHelp:" + helpString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            mTouchIdDialog.startIconShackAnimation();
            //指纹验证失败，指纹识别失败，可再验，该指纹不是系统录入的指纹。
            Toast.makeText(MainActivity.this, "无法识别", Toast.LENGTH_SHORT).show();
        }
    };

    private void initCryptoObject() {
        try {
            mCryptoObjectCreator = new CryptoObjectCreator(new CryptoObjectCreator.ICryptoObjectCreateListener() {
                @Override
                public void onDataPrepared(FingerprintManager.CryptoObject cryptoObject) {
                    // 如果需要一开始就进行指纹识别，可以在秘钥数据创建之后就启动指纹认证
                    //开始验证指纹
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mFingerprintManager.authenticate(mCryptoObjectCreator.getCryptoObject(), mCancellationSignal, 0, callback, null);
                        isStartAuthenticate = true;
                    }
                }
            });
        } catch (Throwable throwable) {
            Log.d("initCryptoObject","create cryptoObject failed!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCryptoObjectCreator!=null){
            mCryptoObjectCreator.onDestroy();
            mCryptoObjectCreator = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (mCancellationSignal != null && isStartAuthenticate) {
                isStartAuthenticate = false;
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }
    }
}