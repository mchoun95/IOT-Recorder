package com.example.mark.iotapp_v1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jetpac.deepbelief.DeepBelief;
import com.jetpac.deepbelief.DeepBelief.JPCNNLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;


public class CamActivity extends Activity {

    /*Declare all Variables*/
    private static final String TAG = "CamActivity";

    /*For camera and text*/
    Preview preview;
    TextView labelsView;
    Camera camera;
    Activity act;
    Context ctx;

    /*For Deep Belief*/

    //Pointer variables
    Pointer networkHandle = null;
    Pointer trainerHandle = null;
    Pointer predictor = null;

    //Prediction label
    float preVal = 0;
    String preName = "";
    String labelsText;

    Bitmap bmp;

    /*Photon*/
    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";

    private TextView tv;

    Handler mUpdater = new Handler();
    Runnable mUpdateView = new Runnable() {
        @Override
        public void run() {
            Async.executeAsync(ParticleCloud.get(CamActivity.this), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {
                    ParticleCloud.logIn("mchoun95@mit.edu", "mark1995");
                    ParticleDevice device = ParticleCloud.getDevice("210034000c47343233323032");
                    Object variable;
                    try {
                        variable = device.getVariable("weight");
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        Toaster.l(CamActivity.this, e.getMessage());
                        variable = -1;
                    }
                    return variable;
                }

                @Override
                public void onSuccess(Object i) { // this goes on the main thread
                    if (preVal > .6) {
                        tv.setText("Weight: " + i.toString());
                        tv.invalidate();
                    }else{
                        tv.setText("");
                        tv.invalidate();
                    }

                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    e.printStackTrace();
                }
            });
            mUpdater.postDelayed(this, 25);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cam);

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        ((FrameLayout) findViewById(R.id.preview)).addView(preview);
        preview.setKeepScreenOn(true);

        labelsView = (TextView) findViewById(R.id.labelsView);
        labelsView.setText("");

        initDeepBelief();
        tv = (TextView) findViewById(R.id.text);
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));

        mUpdateView.run();
    }

    @Override
    protected void onResume() {
        super.onResume();

        camera = Camera.open();
        camera.startPreview();
        preview.setCamera(camera);
        camera.setPreviewCallback(previewCallback);
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    /*Deep Belief*/
    void initDeepBelief() {
        AssetManager am = ctx.getAssets();
        String baseFileName = "jetpac.ntwk";
        String dataDir = ctx.getFilesDir().getAbsolutePath();
        String networkFile = dataDir + "/" + baseFileName;
        copyAsset(am, baseFileName, networkFile);
        networkHandle = JPCNNLibrary.INSTANCE.jpcnn_create_network(networkFile);
        AssetManager ap = ctx.getAssets();
        preName = "predictor.txt";
        String preFile = dataDir + "/" + preName;
        copyAsset(ap, preName, preFile);
        predictor = JPCNNLibrary.INSTANCE.jpcnn_load_predictor(preFile);
    }

    //Converts camera preview to bitmap
    PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Size previewSize = camera.getParameters().getPreviewSize();
            YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
            byte[] jdata = baos.toByteArray();
            bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
            classifyBitmap(bmp);
        }
    };

    //Uses predictor to classify bitmap and print results
    void classifyBitmap(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int pixelCount = (width * height);
        final int bytesPerPixel = 4;
        final int byteCount = (pixelCount * bytesPerPixel);
        ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] pixels = buffer.array();
        Pointer imageHandle = JPCNNLibrary.INSTANCE.jpcnn_create_image_buffer_from_uint8_data(pixels, width, height, 4, (4 * width), 0, 0);

        PointerByReference predictionsValuesRef = new PointerByReference();
        IntByReference predictionsLengthRef = new IntByReference();
        PointerByReference predictionsNamesRef = new PointerByReference();
        IntByReference predictionsNamesLengthRef = new IntByReference();
        long startT = System.currentTimeMillis();
        JPCNNLibrary.INSTANCE.jpcnn_classify_image(
                networkHandle,
                imageHandle,
                2, //SAMPLE FLAGS: 0 = DEFAULT(CENTERED), 1 = MULTISAMPLE, 2 = RANDOM_SAMPLE
                -2, //LAYEROFFSET
                predictionsValuesRef,
                predictionsLengthRef,
                predictionsNamesRef,
                predictionsNamesLengthRef);
        long stopT = System.currentTimeMillis();
        float duration = (float)(stopT-startT) / 1000.0f;
        System.err.println("jpcnn_classify_image() took " + duration + " seconds.");

        JPCNNLibrary.INSTANCE.jpcnn_destroy_image_buffer(imageHandle);

        Pointer predictionsValuesPointer = predictionsValuesRef.getValue();
        final int predictionsLength = predictionsLengthRef.getValue();

        System.err.println(String.format("predictionsLength = %d", predictionsLength));

        //Send predictions to predictor
        preVal = JPCNNLibrary.INSTANCE.jpcnn_predict(predictor,predictionsValuesPointer,predictionsLength);
        String pv = String.valueOf(preVal);
        Log.i("preVal", pv);
        PredictionLabel label = new PredictionLabel(preName,preVal);
        labelsText = String.format("%s - %.2f\n",label.name, label.predictionValue);

        labelsView.setText(labelsText);
    }

    //For finding asset files
    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //For reading asset files
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    //Makes prediction label
    private class PredictionLabel implements Comparable<PredictionLabel> {
        public String name;
        public float predictionValue;

        public PredictionLabel(String inName, float inPredictionValue) {
            this.name = inName;
            this.predictionValue = inPredictionValue;
        }

        public int compareTo(PredictionLabel anotherInstance) {
            final float diff = (this.predictionValue - anotherInstance.predictionValue);
            if (diff < 0.0f) {
                return 1;
            } else if (diff > 0.0f) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /*Photon code*/
    public static Intent buildIntent(Context ctx, Integer value, String deviceid) {
        Intent intent = new Intent(ctx, CamActivity.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceid);

        return intent;
    }


}
