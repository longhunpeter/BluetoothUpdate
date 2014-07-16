package com.chronocloud.update.network;

import android.content.Context;
import android.util.Log;

import com.chronocloud.update.Config;
import com.chronocloud.update.data.UploadData;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by lxl on 14-3-12.
 */
public class BodyScaleRestClient {
    private final String TAG = "peter";
    private String url;
    public static BodyScaleRestClient mBodyScaleRestClient;
    private final RestAdapter mRestAdapter;
    private Context mContext;


    public BodyScaleRestClient(Context context) {
        url = Config.BASE_URL;
        this.mRestAdapter = new RestAdapter.Builder().
                setEndpoint(url)
                .setClient(new BodyScaleClient(context))
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String s) {
                        Log.e(TAG, s);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();


    }

    public static BodyScaleRestClient newInstance(Context context) {
        if (mBodyScaleRestClient == null) {
            mBodyScaleRestClient = new BodyScaleRestClient(context);
        }
        return mBodyScaleRestClient;
    }

    public RestAdapter getRestAdapter() {
        return mRestAdapter;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 获取二维码
     *
     * @param mDeviceType
     * @param addVer
     * @param mDeviceCode
     * @param reqTime
     * @param reservedCode
     * @param devCode
     * @param devVersion
     * @param devColor
     * @param devSupply
     * @param devFlag
     * @param devMac
     * @param mImei
     * @param mSign
     * @param cb
     */
    public void executeUpload(String mDeviceType,
                              String addVer,
                              String mDeviceCode,
                              String reqTime,

                              String reservedCode,
                              String devCode,
                              String devVersion,
                              String devColor,
                              String devSupply,
                              String devFlag,
                              String devMac,

                              String mImei,
                              String mSign,
                              Callback<UploadData> cb) {
        UploadExecutor mUploadExecutor = mRestAdapter.create(UploadExecutor.class);
        mUploadExecutor.execute(mDeviceType,
                addVer,
                mDeviceCode,
                reqTime,

                reservedCode,
                devCode,
                devVersion,
                devColor,
                devSupply,
                devFlag,
                devMac,

                mImei,
                mSign,
                cb);
    }

    interface UploadExecutor {
        @FormUrlEncoded
        @POST("/submitDimensionalCode.htm")
        void execute(
                @Field("deviceType") String mDeviceType,
                @Field("appVer") String mAppVer,
                @Field("deviceCode") String mDeviceCode,
                @Field("reqTime") String mReqTime,

                @Field("reservedCode") String reservedCode,
                @Field("devCode") String devCode,
                @Field("devVersion") String devVersion,
                @Field("devColor") String devColor,
                @Field("devSupply") String devSupply,
                @Field("devFlag") String devFlag,
                @Field("devMac") String devMac,

                @Field("imei") String imei,
                @Field("sign") String sign,
                Callback<UploadData> cb);
    }

}
