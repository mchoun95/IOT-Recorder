package com.example.mark.particlesdktesting;

        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.widget.TextView;

        import java.io.IOException;

        import io.particle.android.sdk.cloud.ParticleCloud;
        import io.particle.android.sdk.cloud.ParticleCloudException;
        import io.particle.android.sdk.cloud.ParticleDevice;
        import io.particle.android.sdk.utils.Async;
        import io.particle.android.sdk.utils.Toaster;

public class ParticleSDK extends AppCompatActivity {

    Handler mUpdater = new Handler();
    Runnable mUpdateView = new Runnable() {
        @Override
        public void run() {
            Async.executeAsync(ParticleCloud.get(ParticleSDK.this), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {
                    ParticleCloud.logIn("mchoun95@mit.edu", "mark1995");
                    ParticleDevice device = ParticleCloud.getDevice("210034000c47343233323032");
                    Object variable;
                    try {
                        variable = device.getVariable("weight");
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        Toaster.l(ParticleSDK.this, e.getMessage());
                        variable = -1;
                    }
                    return variable;
                }

                @Override
                public void onSuccess(Object i) { // this goes on the main thread
                    tv.setText(i.toString());
                    tv.invalidate();
                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    e.printStackTrace();
                }
            });
            mUpdater.postDelayed(this, 250);
        }
    };


    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";

    private TextView tv;

    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_sdk);
        tv = (TextView) findViewById(R.id.value);
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));

        mUpdateView.run();
                //...
                // Do network work on background thread

    }

    public static Intent buildIntent(Context ctx, Integer value, String deviceid) {
        Intent intent = new Intent(ctx, ParticleSDK.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceid);

        return intent;
    }


}



/*
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
//import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
//import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;


public class ParticleSDK extends AppCompatActivity {

    ParticleCloud aCloud = ParticleCloudSDK.getCloud();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_sdk);
        Log.i("starting", "working");
        //ParticleDeviceSetupLibrary.init(this.getApplicationContext(), ParticleSDK.class);
        //ParticleDeviceSetupLibrary.startDeviceSetup(this.getBaseContext());

        Log.i("Logging in", "check");

        try {
            Log.i("Logging in","check");
            Login();
        } catch (ParticleCloudException e) {
            e.printStackTrace();
        }
    }

    void Login() throws ParticleCloudException {

        /*Async.executeAsync(aCloud, new Async.ApiProcedure<ParticleCloud>() {
        ParticleCloudSDK.getCloud().logIn("mchoun95@mit.edu","mark1995");
        });
        Log.i("phase 3", "in");
        //Toaster.s(this.getParent(), "Logged in!");
        try {
            Log.i("phase 4", "in");
            List();
        }catch (ParticleCloudException e) {
            e.printStackTrace();
        }
    }

    void List() throws ParticleCloudException {
        List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();
        for (ParticleDevice device : devices) {
            if (device.getName().equals("bobcat_jetpack")) {
                Log.i("Check","Device Found");
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_particle_sdk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}*/
