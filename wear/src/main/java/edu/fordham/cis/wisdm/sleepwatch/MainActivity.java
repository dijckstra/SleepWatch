package edu.fordham.cis.wisdm.sleepwatch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.fordham.cis.wisdm.sleepwatch.sharedlibrary.FontManager;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static boolean TOGGLE = false;

    private TextView buttonIcon;
    private TextView buttonDescription;
    private Drawable bgSun, bgMoon;
    private ImageView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                buttonIcon = (TextView) findViewById(R.id.button_icon);
                buttonIcon.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));

                buttonDescription = (TextView) findViewById(R.id.button_description);
                bgSun = getDrawable(R.drawable.bg_sun);
                bgMoon = getDrawable(R.drawable.bg_moon);
                background = (ImageView) findViewById(R.id.background);
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

        if (TOGGLE) {
            buttonIcon.setText(R.string.fa_icon_awake);
            startService(i);
            set = new TransitionDrawable(new Drawable[] {bgSun, bgMoon});
            buttonDescription.setText(R.string.awake);
        } else {
            buttonIcon.setText(R.string.fa_icon_bed);
            stopService(i);
            set = new TransitionDrawable(new Drawable[] {bgMoon, bgSun});
            buttonDescription.setText(R.string.begin_sleep);
        }

        set.setCrossFadeEnabled(true);
        background.setImageDrawable(set);
        set.startTransition(3000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TOGGLE) {
            WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
            stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                @Override
                public void onLayoutInflated(WatchViewStub stub) {
                    buttonIcon = (TextView) findViewById(R.id.button_icon);
                    buttonIcon.setText(R.string.fa_icon_awake);
                    buttonIcon.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));

                    buttonDescription = (TextView) findViewById(R.id.button_description);
                    buttonDescription.setText(R.string.awake);

                    bgMoon = getDrawable(R.drawable.bg_moon);
                    background = (ImageView) findViewById(R.id.background);
                    background.setImageDrawable(bgMoon);
                }
            });
        }
    }

}
