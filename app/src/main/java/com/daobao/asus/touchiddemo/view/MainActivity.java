package com.daobao.asus.touchiddemo.view;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.daobao.asus.touchiddemo.R;
import com.daobao.asus.touchiddemo.keyStoreUtil.EncryUtils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mTouchIdStartBtn;
    private Button mEncodeBtn;
    private EditText mEncodeEt;
    private TextView mEncodedTv;
    private TextView mDecodedTv;
    private FingerprintManager mFingerprintManager;
    private TouchIdDialog mTouchIdDialog;
    private CancellationSignal mCancellationSignal;//用于取消指纹识别
    private Boolean isStartAuthenticate = false;//记录是否开启了指纹识别
    private final String mAlias = "touch_id_demo_key";//用于获取加密key

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initTouchId();
    }

    private void initView() {
        mTouchIdStartBtn = findViewById(R.id.touch_start_btn);
        mEncodeBtn = findViewById(R.id.encode_btn);
        mEncodeEt = findViewById(R.id.need_encode_string_et);
        mEncodedTv = findViewById(R.id.encoded_string_tv);
        mDecodedTv = findViewById(R.id.decoded_string_tv);
        mEncodeBtn.setOnClickListener(this);
    }

    private void initTouchId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintManager = getFingerprintManagerOrNull();
            if (mFingerprintManager != null) {
                mTouchIdStartBtn.setOnClickListener(this);
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
            //解密
            try {
                 String decryptString = EncryUtils.getInstance().decryptString(mEncodedTv.getText().toString(), mAlias);
                mDecodedTv.setText(decryptString);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (mCancellationSignal != null && isStartAuthenticate) {
                isStartAuthenticate = false;
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.touch_start_btn:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            mCancellationSignal.cancel();
                                        }
                                        mCancellationSignal = null;
                                    }
                                    mTouchIdStartBtn.setClickable(true);
                                }
                            });
                        }
                        mTouchIdDialog.show();
                        mCancellationSignal = new CancellationSignal();
                        //开始验证指纹
                        mFingerprintManager.authenticate(new
                                        FingerprintManager.CryptoObject(EncryUtils.getInstance().getCipher(mAlias))
                                , mCancellationSignal, 0, callback, null);
                        isStartAuthenticate = true;
                    }
                }
                break;
            case R.id.encode_btn:
                //加密数据
                try {
                    String encodedString = EncryUtils.getInstance().encryptString(mEncodeEt.getText().toString().trim(),mAlias);
                    mEncodedTv.setText(encodedString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}