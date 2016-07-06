package edu.fordham.cis.wisdm.sleepwatch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import edu.fordham.cis.wisdm.sleepwatch.sharedlibrary.FontManager;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String TEST = "/test";
    private static boolean TOGGLE = false;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView textView = (TextView) findViewById(R.id.button_icon);
                textView.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
            }
        });

        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException ignored) {}
    }

    public void toggleSleep(View view) {
        TOGGLE = !TOGGLE;
        Intent i = new Intent(getApplicationContext(), WearSensorLogService.class);
        TransitionDrawable set;
        TextView textView = (TextView) findViewById(R.id.button_icon);

        if (TOGGLE) {
            textView.setText(R.string.fa_icon_awake);
            startService(i);
            set = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.bg_sun, null), getResources().getDrawable(R.drawable.bg_moon, null)
            });

            textView = (TextView) findViewById(R.id.button_description);
            textView.setText(R.string.awake);
        } else {
            textView.setText(R.string.fa_icon_bed);
            stopService(i);
            set = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.bg_moon, null), getResources().getDrawable(R.drawable.bg_sun, null)
            });

            textView = (TextView) findViewById(R.id.button_description);
            textView.setText(R.string.begin_sleep);
        }

        ImageView background = (ImageView) findViewById(R.id.background);
        set.setCrossFadeEnabled(true);
        background.setImageDrawable(set);
        set.startTransition(3000);
    }

    private void sendMessage(final String message) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                for (Node node : nodes.getNodes()) {

                    MessageApi.SendMessageResult result =
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    message, message.getBytes()).await();

                    Log.d(TAG, "Sent to node: " + node.getId() +
                            " with display name: " + node.getDisplayName());

                    if (!result.getStatus().isSuccess()) {
                        Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
                    }
                    else {
                        Log.d(TAG, "Message Successfully sent.");
                    }
                }
            }
        }).start();
    }

}
