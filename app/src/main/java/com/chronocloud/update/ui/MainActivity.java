package com.chronocloud.update.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.chronocloud.update.Config;
import com.chronocloud.update.R;
import com.chronocloud.update.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "peter";
    private Button mScanDeviceBtn;
    private Button mScanQRCodeBtn;
    private ListView mListView;

    private boolean mScanning;

    private Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDeviceListAdapter;

    private static final long SCAN_PERIOD = 5000;
    public static final int SCAN_BLE_DEVICE_SUCCESS = 0;

    private Handler scanDeviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_BLE_DEVICE_SUCCESS:
                    mScanDeviceBtn.setText(getResources().getString(R.string.btn_scan));
                    break;
                default:
                    break;
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_data();
        init_view();
//        if (null != bufferData && bufferData.size() > 0) {
//            againUploadDialog();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanning = false;
        if (null != mScanDeviceBtn) {
            mScanDeviceBtn.setText(this.getResources().getString(R.string.btn_scan));
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_device_btn:
                if (mScanning) {
                    mScanDeviceBtn.setText(this.getResources().getString(R.string.btn_scan));
                    scanDevice(false);
                } else {
                    mScanDeviceBtn.setText(this.getResources().getString(R.string.btn_scan_stop));
                    mDeviceListAdapter.clear();
                    scanDevice(true);
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     */
    private void init_data() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            makeToast(R.string.ble_not_supported);
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            makeToast(R.string.ble_not_supported);
            finish();
            return;
        }
//        dbHelper = new ClientDBHelper(this);
//        bufferData = UploadInfo.queryBuffer(dbHelper);
        mDeviceListAdapter = new DeviceListAdapter(this);
        mHandler = new Handler();


    }

    /**
     *
     */
    private void init_view() {
        mScanDeviceBtn = (Button) findViewById(R.id.scan_device_btn);
        mScanQRCodeBtn = (Button) findViewById(R.id.get_qr_btn);
        mScanDeviceBtn.setOnClickListener(this);
        mScanQRCodeBtn.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.device_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mDeviceListAdapter.getDevice(position);
                if (device == null) return;
                final Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
//                final Intent intent = new Intent(MainActivity.this, ConnectionDeviceActivity.class);
                intent.putExtra(Config.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(Config.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                startActivity(intent);
            }
        });
        mListView.setAdapter(mDeviceListAdapter);
    }


    /**
     * 是否扫描
     *
     * @param isEnable
     */
    private void scanDevice(final boolean isEnable) {
        if (isEnable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        scanDeviceHandler.sendEmptyMessage(SCAN_BLE_DEVICE_SUCCESS);
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 上传缓存提示
     */
    private void againUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("有信息未提交是否提交？");
        builder.setTitle("提示");

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                againUploadBuffer();

            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    /**
     * 重新提交未提交的数据
     *
     * @return
     */
//    private void againUploadBuffer() {
//        String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
//        for (UploadInfo uploadInfo : bufferData) {
//            final String mDimensionalCode = uploadInfo.dimensionalCode;
//            BodyScaleRestClient.newInstance(this).executeUpload(
//                    Config.ANDROID_DEVICE_TYPE,
//                    Utils.getIMEI(this),
//                    Utils.getVersionName(this),
//                    currentTime,
//                    mDimensionalCode,
//                    Utils.getIMEI(this),
//                    Utils.encryption(mDimensionalCode + Utils.getIMEI(this) + Config.CONVENTIOS_STR),
//                    new Callback<UploadData>() {
//                        @Override
//                        public void success(UploadData uploadData, Response response) {
//                            if (null != uploadData.result &&
//                                    uploadData.result.equals(Config.UPLOAD_SUCCESS)) {
//                                UploadInfo.deleteBuffer(mDimensionalCode, dbHelper);
//                                return;
//                            }
//
//                        }
//
//                        @Override
//                        public void failure(RetrofitError retrofitError) {
//                            if (retrofitError.isNetworkError()) {
//                                makeToast(getResources().getString(R.string.network_error));
//                            }
//                        }
//                    });
//        }
//    }

    /**
     * device scan callback.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDeviceListAdapter.addDevice(device, rssi, scanRecord);
                            mDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    /**
     * Adapter for holding devices found through scanning.
     */
    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<Integer> rssis;
        private ArrayList<byte[]> bRecord;
        private LayoutInflater mInflator;

        public DeviceListAdapter(Context context) {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            rssis = new ArrayList<Integer>();
            bRecord = new ArrayList<byte[]>();
//            mInflator = getLayoutInflater().from(context);
            mInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addDevice(BluetoothDevice device, int rs, byte[] record) {
            if (!mLeDevices.contains(device) ) {
                mLeDevices.add(device);
                rssis.add(rs);
                bRecord.add(record);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            rssis.clear();
            bRecord.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }

        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    }
}
