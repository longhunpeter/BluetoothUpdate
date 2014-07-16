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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chronocloud.update.Config;
import com.chronocloud.update.R;
import com.chronocloud.update.server.BluetoothLeService;
import com.chronocloud.update.util.Utils;

import java.util.List;

/**
 * Created by lxl on 2014/5/8.
 */
public class UpdateActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = getClass().getCanonicalName();
    private TextView mStatusText;
    private Button mBack;
    private Button mStartUpdateBtn;
    private Button mCancelUpdateBtn;
    //    private Button mResetBtn;
    private BluetoothLeService mBluetoothLeService;

    private String mDeviceMac;
    private List<byte[]> mUpdateListParent;
    private int breakIndex = 20;
    private int packageIndex = 0;
    private int startPackageCount = 0;
    private int endPackageCount = 0;
    private int sonPackageCount = 0;
    private int updatePackageSize = 0;//升级包大小以K计算
    private int mVersionCode = 2;//版本号
    private int mOrderStatus = 0;//命令状态标志
    private List<byte[]> mUpdateListSon;

    private byte[] startOrder = new byte[]{(byte) 83, (byte) 02, (byte) 79, (byte) 75};
    private byte[] stopOrder = new byte[]{(byte) 83, (byte) 03, (byte) 79, (byte) 75};

    private byte[] sendPackageOrder = new byte[]{(byte) 83, (byte) 01, (byte) 79, (byte) 75};

    private byte[] isSendEndPackage = new byte[]{(byte) 83, (byte) 04, (byte) 79, (byte) 75};

    private byte[] getVersionOrder = new byte[]{(byte) 83, (byte) 85, (byte) 68, (byte) 86};

    private byte[] updateResetOrder = new byte[]{(byte) 83, (byte) 85, (byte) 79, (byte) 75};

    private static final int START_UPDATE_DEVICE = 0x00;
    private static final int CANCEL_UPDATE_DEVICE = 0x01;

    /**
     * 开始命令对应包反馈命令
     */
    private static final byte START_FEEDBACK_BREAK = (byte) 65;
    private static final byte START_FEEDBACK_VERSION_ERROR = (byte) 66;
    private static final byte START_FEEDBACK_SIZE_ERROR = (byte) 67;
    private static final byte START_FEEDBACK_CRC_ERROR = (byte) 68;
    private static final byte START_FEEDBACK_OK = (byte) 86;

    /**
     * 发送结束包对应反馈
     */
    private static final byte END_FEEDBACK_BREAK = (byte) 75;
    private static final byte END_FEEDBACK_VERSION_ERROR = (byte) 76;
    private static final byte END_FEEDBACK_SIZE_ERROR = (byte) 77;
    private static final byte END_FEEDBACK_CRC_ERROR = (byte) 78;
    private static final byte END_FEEDBACK_OK = (byte) 87;
    /**
     * 发送数据包对应反馈
     */
    private static final byte UPDATE_PACKAGE_FEEDBACK_BREAK = (byte) 72;
    private static final byte UPDATE_PACKAGE_FEEDBACK_SIZE = (byte) 73;
    private static final byte UPDATE_PACKAGE_FEEDBACK_CRC = (byte) 74;
    private static final byte UPDATE_PACKAGE_FEEDBACK_OK = (byte) 88;


    /**
     * 升级成功与否反馈
     */

    private static final byte UPDATE_PACKAGE_SUCCESS = (byte) 89;
    private static final byte UPDATE_PACKAGE_FAIL = (byte) 90;

    private static final int ORDER_GET_VERSION = 0x01;
    private static final int ORDER_UPDATE_RESET = 0x02;
    private static final int ORDER_STATUS_START_ORDER = 0x03;
    private static final int ORDER_STATUS_START_PACKAGE = 0x04;
    private static final int ORDER_STATUS_SEND_PACKAGE = 0x05;
    private static final int ORDER_STATUS_CANCEL_ORDER = 0x06;
    private static final int ORDER_STATUS_END_PACKAGE = 0x07;
    private static final int ORDER_SEND_PACKAGE_CHECKOUT = 0x08;
    private static final int ORDER_SEND_END_ORDER = 0x09;

    private int continueIndex = 0;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_layout);
        initData();
        initView();

    }


    @Override
    protected void onResume() {
        super.onResume();
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_btn:
                finish();
                break;
            case R.id.startUpdate_btn:
                mStartUpdateBtn.setEnabled(false);
                mCancelUpdateBtn.setEnabled(true);
                writeGetVersionOrder();
//                startUpdateDevice();
                break;
            case R.id.cancelUpdate_btn:
                mStartUpdateBtn.setEnabled(true);
                mCancelUpdateBtn.setEnabled(false);
                cancelUpdateDevice();
                break;
            default:
                break;
        }
    }

    private void initData() {
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra(Config.EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
        mUpdateListParent = Utils.getUpdatePartPackage(Utils.readUpdateFile(this), 1024, 0);
        updatePackageSize = mUpdateListParent.size();
        Log.i(TAG, "size one parent is-------- " + updatePackageSize);

    }

    private void initView() {
        mStatusText = (TextView) findViewById(R.id.update_status);
        mStartUpdateBtn = (Button) findViewById(R.id.startUpdate_btn);
        mCancelUpdateBtn = (Button) findViewById(R.id.cancelUpdate_btn);
        mStartUpdateBtn.setEnabled(false);
        mCancelUpdateBtn.setEnabled(false);
        mBack = (Button) findViewById(R.id.back_btn);
        mBack.setOnClickListener(this);
        mStartUpdateBtn.setOnClickListener(this);
        mCancelUpdateBtn.setOnClickListener(this);

    }


    /**
     * 获取版本号命令
     */
    private void writeGetVersionOrder() {
        mStatusText.setText("正在获取设备版本号...");
        mOrderStatus = ORDER_GET_VERSION;
        mBluetoothLeService.WriteValue(getVersionOrder);
    }

    /**
     * 复位升级命令
     */
    private void writeResetOrder() {
        mStatusText.setText("正在复位设备...");
        mOrderStatus = ORDER_UPDATE_RESET;
        mBluetoothLeService.WriteValue(updateResetOrder);
    }

    /**
     * 开始升级设备
     */
    private void startUpdateDevice() {
        mStatusText.setText("正在升级固件请耐心等待...");
        mOrderStatus = ORDER_STATUS_START_ORDER;//开始升级状态
        mBluetoothLeService.WriteValue(startOrder);
    }

    /**
     * 取消升级
     */
    private void cancelUpdateDevice() {
        mOrderStatus = ORDER_STATUS_CANCEL_ORDER;//结束升级状态
        mBluetoothLeService.WriteValue(stopOrder);
    }

    /**
     * 开始发包命令
     */
    private void writeSendPackageOrder() {
        Log.i(TAG, "writeSendPackageOrder--------------准备发包命令");
        mOrderStatus = ORDER_SEND_PACKAGE_CHECKOUT;
        mBluetoothLeService.WriteValue(sendPackageOrder);
    }


    /**
     * 得到设备返回的版本号
     *
     * @param bytes
     * @return
     */
    private int isGetVersion(byte[] bytes) {
        Log.i(TAG, "isGetVersion is ------ " + Utils.byteToString(bytes));
        int len = bytes.length;
        if (len <= 0 || len < 4)
            return -1;

        String version = Utils.byteToStringArrays(bytes)[3].replaceAll(" ", "");
        Log.i(TAG, "version -----------  " + version);
        if (null != version || version.equals(""))
            return Integer.parseInt(version);

        return -1;
    }

    /**
     * 判断设备是否复位准备成功
     *
     * @param bytes
     * @return
     */
    private boolean isUpdateReset(byte[] bytes) {
        int len = bytes.length;
        if (len <= 0 || len < 4)
            return false;
        if (bytes[1] == (byte) 85) {
            return true;
        }
        return false;
    }

    /**
     * 发送升级包确认命令
     *
     * @param bytes
     * @return
     */
    private boolean isSendPackageSuccess(byte[] bytes) {
        int len = bytes.length;
        if (len <= 0 || len < 2)
            return false;
        if (bytes[1] == (byte) 01)
            return true;
        return false;
    }

    /**
     * 发送结束包命令确认
     *
     * @param bytes
     * @return
     */
    private boolean isEndPackageOrder(byte[] bytes) {
        int len = bytes.length;
        if (len <= 0 || len < 2)
            return false;
        if (bytes[1] == (byte) 04)
            return true;
        return false;
    }

    /**
     * 升级成功与否
     *
     * @param bytes
     * @return
     */
    private boolean isUpdateSuccess(byte[] bytes) {
        int len = bytes.length;
        if (len <= 0 || len < 2)
            return false;
        if (bytes[1] == UPDATE_PACKAGE_SUCCESS) {
            return true;
        } else if (bytes[1] == UPDATE_PACKAGE_FAIL) {
            return false;
        }
        return false;

    }


    /**
     * 写入升级程序
     */
    private void writeUpdatePackage() {
        mOrderStatus = ORDER_STATUS_SEND_PACKAGE;//写入升级包状态
        final int lenParent = mUpdateListParent.size();
        Log.i(TAG, "lenParent is -------- " + lenParent);
        if (lenParent <= 0)
            return;
        writeUpdateSon(packageIndex);
    }

    /**
     * 发送结束包确认命令
     */
    private void writeEndPackageOrder() {
        mOrderStatus = ORDER_SEND_END_ORDER;
        mBluetoothLeService.WriteValue(isSendEndPackage);
    }

    /**
     * 写入升级程序开始包
     */
    private void writeStartPackage() {
        mOrderStatus = ORDER_STATUS_START_PACKAGE;
        byte[] head = new byte[]{(byte) 0x02};
        byte[] startPackage = new byte[]{
                (byte) 0xff,
                (byte) updatePackageSize, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

        byte[] crc32 = Utils.crc32byByte(startPackage);
        byte[] writeArrays = Utils.concatByte(head, startPackage, crc32);
        if (writeArrays.length <= 0)
            return;
        mBluetoothLeService.WriteValue(writeArrays);
    }

    /**
     * 写入结束包
     */
    private void writeEndPackage() {
        mOrderStatus = ORDER_STATUS_END_PACKAGE;//进入结束包状态
        byte[] packageHead = new byte[]{(byte) 0x04};
        byte[] endBytes = new byte[]{(byte) 0xff, (byte) updatePackageSize, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] crc32 = Utils.crc32byByte(endBytes);
        byte[] endArrays = Utils.concatByte(packageHead, endBytes, crc32);
        if (endArrays.length <= 0)
            return;
        mBluetoothLeService.WriteValue(endArrays);
        Log.i(TAG, "writeEndPackage-------------" + Utils.byteToString(crc32));
    }

    /**
     * 写入子包;
     *
     * @param index
     */
    private void writeUpdateSon(int index) {

        byte[] mUpdateSonArrays = mUpdateListParent.get(index);
        mUpdateListSon = Utils.getUpdatePartPackage(mUpdateSonArrays, 20, 1);
        int lenSon = mUpdateListSon.size();
        if (lenSon <= 0)
            return;


        byte[] packageSonHead = new byte[]{(byte) 0x01, (byte) index};
        mBluetoothLeService.WriteValue(packageSonHead);
        for (int i = 0; i < lenSon; i++) {
            final int num = i;
            for (int j = 0; j < 600000; j++) {
                int x = 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5;
            }
            mBluetoothLeService.WriteValue(mUpdateListSon.get(num));
        }
        byte[] crcArrays = Utils.crc32byByte(mUpdateSonArrays);
        mBluetoothLeService.WriteValue(crcArrays);
//        Log.i(TAG, "crcArrays son is ----- " + Utils.byteToString(crcArrays));
    }

    /**
     * 开始命理反馈
     *
     * @param bytes
     * @return
     */
    private boolean isStartUpdate(byte[] bytes) {
        final int len = bytes.length;
        if (len <= 0 || len < 2)
            return false;

        if (bytes[1] == (byte) 0X02)
            return true;


        return false;

    }

    /**
     * 取消升级命理反馈
     *
     * @param bytes
     * @return
     */
    private boolean isCancelUpdate(byte[] bytes) {
        final int len = bytes.length;
        if (len <= 0 || len < 2)
            return false;

        if (bytes[1] == (byte) 0X03)
            return true;


        return false;
    }

    /**
     * 开始包相关判定
     *
     * @param bytes
     */
    private void startPackageJudgement(byte[] bytes) {
        final int len = bytes.length;
        if (len <= 0)
            return;
        if (bytes[0] == START_FEEDBACK_BREAK) {
            mStatusText.setText("数据中断请重发开始包");
            againSendPackage(ORDER_STATUS_START_PACKAGE);
//            writeStartPackage();
            return;
        } else if (bytes[0] == START_FEEDBACK_VERSION_ERROR) {
            mStatusText.setText("版本号不合法");
            return;
        } else if (bytes[0] == START_FEEDBACK_SIZE_ERROR) {
            mStatusText.setText("文件大小不合法");
            return;

        } else if (bytes[0] == START_FEEDBACK_CRC_ERROR) {
            mStatusText.setText("CRC校验错误,从发数据包...");
            againSendPackage(ORDER_STATUS_START_PACKAGE);

            return;
        } else if (bytes[0] == START_FEEDBACK_OK) {
            mStatusText.setText("开始命令成功，发送开始数据包...");
            Log.i(TAG, "Start package is ok,continue.");
            writeSendPackageOrder();
            return;
        }
    }


    /**
     * 结束包相关判定
     *
     * @param bytes
     */
    private boolean endPackageJudgement(byte[] bytes) {
        final int len = bytes.length;
        if (len <= 0)
            return false;
        if (bytes[0] == END_FEEDBACK_BREAK) {
            mStatusText.setText("结束包数据中断请重发开始包");
//            writeEndPackage();
            againSendPackage(ORDER_STATUS_END_PACKAGE);
            return false;
        } else if (bytes[0] == END_FEEDBACK_VERSION_ERROR) {
            mStatusText.setText("结束包版本号不合法");
            return false;
        } else if (bytes[0] == END_FEEDBACK_SIZE_ERROR) {
            mStatusText.setText("结束包文件大小不合法");
            return false;

        } else if (bytes[0] == END_FEEDBACK_CRC_ERROR) {
            mStatusText.setText("结束包CRC校验错误");
            againSendPackage(ORDER_STATUS_END_PACKAGE);
            return false;
        } else if (bytes[0] == END_FEEDBACK_OK) {
            mStatusText.setText("结束包发送成功.");
            return true;
        }
        return false;
    }

    /**
     * 升级文件分包发送判断
     *
     * @param bytes
     */
    private void updatePackageJudgement(byte[] bytes) {
        Log.i(TAG, "updatePackageJudgement-----------------");
        final int len = bytes.length;
        if (len <= 0)
            return;
        if (bytes[0] == UPDATE_PACKAGE_FEEDBACK_BREAK) {
            mStatusText.setText("传输中断，重发中...");
//            writeSendPackageOrder();
            againSendPackage(ORDER_STATUS_SEND_PACKAGE);
            return;

        } else if (bytes[0] == UPDATE_PACKAGE_FEEDBACK_SIZE) {
            mStatusText.setText("数据包个数不正确");
            return;
        } else if (bytes[0] == UPDATE_PACKAGE_FEEDBACK_CRC) {
            mStatusText.setText("CRC校验错误,重发中...");
//            writeSendPackageOrder();
            againSendPackage(ORDER_STATUS_SEND_PACKAGE);
            return;
        } else if (bytes[0] == UPDATE_PACKAGE_FEEDBACK_OK) {
            Log.i(TAG, "Update package send ok,continue.");

            if (packageIndex == updatePackageSize - 1) {
                mStatusText.setText("升级包发送完成");
                writeEndPackageOrder();
                return;
            }
            mStatusText.setText("当前正在发送的包 " + packageIndex);
            packageIndex++;
            writeSendPackageOrder();
            return;
        }
    }

    /**
     * 重发相关包
     *
     * @param orderStatus
     */
    private void againSendPackage(int orderStatus) {
        switch (orderStatus) {
            case ORDER_STATUS_START_PACKAGE:
                if (startPackageCount > breakIndex) {
                    Log.i(TAG, "startPackageCount ------------- " + startPackageCount);
                    break;
                }
                writeStartPackage();
                startPackageCount++;
                break;
            case ORDER_STATUS_END_PACKAGE:
                if (endPackageCount > breakIndex) {
                    Log.i(TAG, "endPackageCount ------------- " + endPackageCount);
                    break;
                }
                writeEndPackage();
                endPackageCount++;
                break;
            case ORDER_STATUS_SEND_PACKAGE:
                if (sonPackageCount > breakIndex) {
                    Log.i(TAG, "sonPackageCount ------------- " + sonPackageCount);
                    break;
                }
                writeSendPackageOrder();
                Log.i(TAG, "ORDER_STATUS_SEND_PACKAGE 当前包--------- " + sonPackageCount);
                sonPackageCount++;
                break;
            default:
                break;
        }

    }

    /**
     * 命令状态判定
     *
     * @param data
     */
    private void updateStatus(byte[] data) {
        switch (mOrderStatus) {
            case ORDER_GET_VERSION:
//                String dataStrVersion = Utils.byteToString(data);
//                Log.i(TAG, "dataStrVersion ------------ " + dataStrVersion);
                int getVersion = isGetVersion(data);
                Log.i(TAG, "dataStrVersion ------------ " + getVersion);
                if (getVersion < mVersionCode && getVersion < 0) {
                    Log.i(TAG, "获取版本号 失败！");
                    return;
                }
                writeResetOrder();
                break;
            case ORDER_UPDATE_RESET:
                String dataStrReset = Utils.byteToString(data);
                Log.i(TAG, "dataStrReset ------------ " + dataStrReset + "  " + data.length);
                if (isUpdateReset(data)) {
                    startUpdateDevice();
                }
                break;
            case ORDER_STATUS_START_ORDER:
                String dataStr1 = Utils.byteToString(data);
                Log.i(TAG, "dataStr1 ------------ " + dataStr1);
                if (isStartUpdate(data)) {
                    writeStartPackage();
                }

                break;
            case ORDER_STATUS_CANCEL_ORDER:
                String dataStr2 = Utils.byteToString(data);
                Log.i(TAG, "dataStr2 " + dataStr2);
                if (isCancelUpdate(data)) {
                    mStatusText.setText("固件升级取消");
                }
                break;
            case ORDER_STATUS_START_PACKAGE:
                String dataStr3 = Utils.byteToString(data);
                Log.i(TAG, "dataStr3 " + dataStr3);
                startPackageJudgement(data);
                break;
            case ORDER_SEND_PACKAGE_CHECKOUT:
                String dataStr4 = Utils.byteToString(data);
                Log.i(TAG, "dataStr4 " + dataStr4);
                if (isSendPackageSuccess(data)) {
                    Log.i(TAG, "可以发送数据包了..........");
                    writeUpdatePackage();
                }
                break;
            case ORDER_STATUS_SEND_PACKAGE:
                String dataStr5 = Utils.byteToString(data);
                Log.i(TAG, "dataStr5 ------------ " + dataStr5);
                updatePackageJudgement(data);
                break;
            case ORDER_SEND_END_ORDER:
                String dataStr6 = Utils.byteToString(data);
                Log.i(TAG, "dataStr6 ------------ " + dataStr6);
                if (isEndPackageOrder(data)) {

                    writeEndPackage();
                }
                break;
            case ORDER_STATUS_END_PACKAGE:
                String dataStr7 = Utils.byteToString(data);
                Log.i(TAG, "dataStr7 ------------ " + dataStr7);
                boolean end = endPackageJudgement(data);
                if (end) {
                    Log.i(TAG, "判断升级是否成功");
                    boolean isSuccess = isUpdateSuccess(data);

                }
                break;
            default:
                break;

        }

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
            mBluetoothLeService.connect(mDeviceMac);
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
                mStartUpdateBtn.setEnabled(false);
                mCancelUpdateBtn.setEnabled(false);
                mStatusText.setText(getResources().getString(R.string.update_connect_fail));

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //建立蓝牙服务
            {
                mStartUpdateBtn.setEnabled(true);
                mStatusText.setText(getResources().getString(R.string.update_connect_success));
                Log.i(TAG, "In what we need");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                Log.i(TAG, "RECV DATA");
//                Log.i(TAG,"------data---------" +  Utils.byteToString(data));
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (data.length <= 0) {
                    Log.i(TAG, " set data is null");
                    return;
                }
                updateStatus(data);
            }
        }
    };


    /**
     * 倒计时
     */
//    class MyCount extends CountDownTimer {
//
//        public MyCount(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//
//        }
//
//        @Override
//        public void onFinish() {
//            mHandler.sendEmptyMessage(0);
//        }
//    }


}
