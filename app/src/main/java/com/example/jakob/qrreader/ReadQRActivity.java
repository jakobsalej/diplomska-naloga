package com.example.jakob.qrreader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ReadQRActivity extends AppCompatActivity {

    public static final String DB_DATA = "com.example.myfirstapp.DB_DATA";
    private static int first = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_qr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);

        // new instance of barcode detector...
        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        // ... and camera source
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final CameraSource cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30.0f)
                .build();


        // start drawing preview frames
        final Context myActivity = this;
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(myActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                Log.v("QR", "New detection!");

                if (barcodes.size() != 0 && first == 0) {
                    first = 1;
                    Intent intent = new Intent(getBaseContext(), OrderItemActivity.class);
                    intent.putExtra(DB_DATA, barcodes.valueAt(0).displayValue);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        first = 0;
    }


    public void showData(View view) {

        // hide keyboard
        // Check if no view has focus:
        View view2 = this.getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
        }

        Intent intent = new Intent(this, OrderItemActivity.class);
        EditText barcodeInfo = (EditText) findViewById(R.id.editText_id);

        // for now just displaying text, later making call to REST services
        String data = barcodeInfo.getText().toString();

        // only start new activity if there is some data in edit field
        if (data.length() > 0) {
            intent.putExtra(DB_DATA, data);
            startActivity(intent);
        }
    }

}
