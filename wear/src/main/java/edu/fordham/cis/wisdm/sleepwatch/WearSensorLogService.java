package edu.fordham.cis.wisdm.sleepwatch;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;

public class WearSensorLogService extends WearableListenerService implements SensorEventListener {

    private static final String TAG = "WearSensorLogService";
    private static final String ACCEL_ASSET = "ACCEL_ASSET";
    private static final String GYRO_ASSET = "GYRO_ASSET";
    private static final String DATA = "/data";

    private ArrayList<ThreeTupleRecord> mWatchAccelerometerRecords = new ArrayList<>();
    private ArrayList<ThreeTupleRecord> mWatchGyroscopeRecords = new ArrayList<>();
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private GoogleApiClient mGoogleApiClient;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    public WearSensorLogService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerListeners();
        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "Collected data: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mWatchAccelerometerRecords.add(
                        new ThreeTupleRecord(
                                event.timestamp, event.values[0], event.values[1], event.values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:
                mWatchGyroscopeRecords.add(
                        new ThreeTupleRecord(
                                event.timestamp, event.values[0], event.values[1], event.values[2]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(WearSensorLogService.this, mAccelerometer);
        mSensorManager.unregisterListener(WearSensorLogService.this, mGyroscope);
        sendData();

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void registerListeners() {
        getSensors();
        acquireWakeLock();

        mSensorManager.registerListener(WearSensorLogService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(WearSensorLogService.this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Get the accelerometer and gyroscope if available on device
     */
    private void getSensors() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    /**
     * Acquire wake lock to sample with the screen off.
     */
    private void acquireWakeLock() {
        mPowerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire();
    }

    private void sendData() {
        Log.d(TAG, "Sending data from watch to phone");
        Asset accelAsset = Asset.createFromBytes(SerializationUtils.serialize(mWatchAccelerometerRecords));
        Asset gyroAsset = Asset.createFromBytes(SerializationUtils.serialize(mWatchGyroscopeRecords));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        PutDataMapRequest dataMap = PutDataMapRequest.create(DATA);
        dataMap.getDataMap().putAsset(ACCEL_ASSET, accelAsset);
        dataMap.getDataMap().putAsset(GYRO_ASSET, gyroAsset);
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }
}
