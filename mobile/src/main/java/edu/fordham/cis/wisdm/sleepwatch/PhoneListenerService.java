package edu.fordham.cis.wisdm.sleepwatch;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PhoneListenerService extends WearableListenerService {

    private static final String TAG = "PhoneListenerService";
    private static final String ACCEL_ASSET = "ACCEL_ASSET";
    private static final String GYRO_ASSET = "GYRO_ASSET";
    private static final String DATA = "/data";
    private static final String WATCH_ACCEL = "watch_accel";
    private static final String WATCH_GYRO = "watch_gyro";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged called");

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals(DATA)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset watchAccelAsset = dataMapItem.getDataMap().getAsset(ACCEL_ASSET);
                Asset watchGyroAsset = dataMapItem.getDataMap().getAsset(GYRO_ASSET);

                ArrayList<ThreeTupleRecord> watchAccelData = loadDataFromAsset(watchAccelAsset);
                ArrayList<ThreeTupleRecord> watchGyroData = loadDataFromAsset(watchGyroAsset);

                writeFiles(watchAccelData, watchGyroData);
            }
        }
    }

    private void writeFiles(ArrayList<ThreeTupleRecord> watchAccelRecords,
                            ArrayList<ThreeTupleRecord> watchGyroRecords) {
        File directory = SensorFileSaver.getDirectory(this);
        File watchAccelFile = SensorFileSaver.createFile(directory, WATCH_ACCEL);
        File watchGyroFile = SensorFileSaver.createFile(directory, WATCH_GYRO);
        SensorFileSaver.writeFile(watchAccelFile, watchAccelRecords);
        SensorFileSaver.writeFile(watchGyroFile, watchGyroRecords);
    }

    private ArrayList<ThreeTupleRecord> loadDataFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult result = googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!result.isSuccess()) {
            return null;
        }

        InputStream assetInputStream = Wearable.DataApi
                .getFdForAsset(googleApiClient, asset).await().getInputStream();
        googleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
        }

        return (ArrayList<ThreeTupleRecord>) SerializationUtils.deserialize(assetInputStream);
    }
}