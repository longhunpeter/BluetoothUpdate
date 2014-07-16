package com.chronocloud.update.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chronocloud.update.Config;
import com.chronocloud.update.R;
import com.chronocloud.update.server.BluetoothLeService;
import com.chronocloud.update.util.Utils;

import java.util.zip.CRC32;

/**
 * Created by lxl on 14-3-11.
 */
public class ConnectionDeviceActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "peter";
    //    private Button mMarkzXingBtn;
    private Button mSendBtn;
    private Button connectBack;
    private TextView mDataText;
    private EditText mEditText;

    private TextView mConnectStatus;
    private ProgressBar mBar;

    private boolean isSendCommand;//是否发送命令
    private boolean isTestSuccess;
    private String mDeviceAddress;
    private String uuid;
    private BluetoothLeService mBluetoothLeService;
    private PopupWindow mPopupWindow;
    private MyCount myCount;

    private static final int QRCODE_MARK = 1;
    private static final int DEVICE_IS_CONNECT = 2;
    private static final int DEVICE_IS_DISCONNECT = 3;
    private static final int TEST_SUCCESS = 4;
    private static final int TEST_FAIL = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QRCODE_MARK:
                    //TODO
                    break;
                case DEVICE_IS_CONNECT:
                    mSendBtn.setEnabled(true);
                    mDataText.setText("");
                    mBar.setVisibility(View.GONE);
                    mConnectStatus.setText(getResources().getString(R.string.connect_status_success));
                    makeToast(getResources().getString(R.string.connect_success));

                    break;
                case DEVICE_IS_DISCONNECT:
                    mDataText.setText("");
                    mSendBtn.setEnabled(false);
                    mBar.setVisibility(View.GONE);
                    mConnectStatus.setText(getResources().getString(R.string.connect_status_fail));
                    makeToast(getResources().getString(R.string.connect_fail));
                    break;
                case TEST_SUCCESS:
                    break;
                case TEST_FAIL:
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        break;
                    }
                    break;
                default:
                    break;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);
        init_data();
        init_view();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isTestSuccess = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }

        if (null != mPopupWindow && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    private void init_data() {
        int len = Utils.readUpdateFile(this).length;
        Log.i(TAG,"start read -----------");
        Log.i(TAG,"read len is-------------- " + len);
        Log.i(TAG,"end read -----------");

        isSendCommand = false;

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(Config.EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
    }

    private void init_view() {
        mSendBtn = (Button) findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(this);
        connectBack = (Button) findViewById(R.id.connection_back);
        connectBack.setOnClickListener(this);
        mSendBtn.setEnabled(false);
        mDataText = (TextView) findViewById(R.id.get_data);
        mConnectStatus = (TextView) findViewById(R.id.connect_status);
        mBar = (ProgressBar) findViewById(R.id.mProgress);
        mEditText = (EditText)findViewById(R.id.order_edit);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                isSendCommand = true;
//                myCount = new MyCount(3000, 1000);
//                myCount.start();
                String order = mEditText.getText().toString();
                Log.i(TAG,"order is -------- " + order);
                if(TextUtils.isEmpty(order)){
                    makeToast("发送命令不能为空");
                    break;
                }

                mBluetoothLeService.WriteValue(order);

                break;
            case R.id.connection_back:
                finish();
                break;
            default:
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (null != mPopupWindow && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Code to manage Service lifecycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.i(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    /**
     * 接收广播
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //关联成功

                Log.i(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                mHandler.sendEmptyMessage(DEVICE_IS_DISCONNECT);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //建立蓝牙服务
            {
                mHandler.sendEmptyMessage(DEVICE_IS_CONNECT);
                Log.i(TAG, "In what we need");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                Log.i(TAG, "RECV DATA");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                uuid = intent.getStringExtra(BluetoothLeService.EXTAR_UUID);
                if (data != null) {
                    if (mDataText.length() > 500) {
                        mDataText.setText("");
                    }
                    mDataText.append(data);
                }
            }
        }
    };

    /**
     * 倒计时
     */
    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mHandler.sendEmptyMessage(TEST_FAIL);
        }
    }
}
