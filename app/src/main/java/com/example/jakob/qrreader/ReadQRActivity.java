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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ReadQRActivity extends AppCompatActivity {

    public static final String DB_DATA = "com.example.myfirstapp.DB_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_qr);
        final SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);
        final TextView barcodeInfo = (TextView) findViewById(R.id.code_info);
        final Button getData = (Button) findViewById(R.id.button_get_data);

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

                if (barcodes.size() != 0) {
                    // showDocumentData(barcodes.valueAt(0).displayValue);
                    // TODO: too many new activities??

                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );

                            // TODO: call new intent (without user clicking button)
                            /*
                            if (barcode != null) {
                                Intent intent = new Intent();
                                intent.putExtra(BarcodeObject, barcode);
                                setResult(CommonStatusCodes.SUCCESS, intent);
                                finish();
                            }
                             */


                            // enable get_data button when we get QR code
                            getData.setVisibility(View.VISIBLE);

                        }
                    });
                }
            }
        });
    }

    /*
    public void showData(View view) {
        Intent intent = new Intent(this, DisplayDataActivity.class);
        TextView barcodeInfo = (TextView) findViewById(R.id.code_info);

        // for now just displaying text, later making call to REST services
        String data = barcodeInfo.getText().toString();

        intent.putExtra(DB_DATA, data);
        startActivity(intent);
    }
    */


    public void showData(View view) {
        Intent intent = new Intent(this, OrderItemActivity.class);
        TextView barcodeInfo = (TextView) findViewById(R.id.code_info);

        // for now just displaying text, later making call to REST services
        String data = barcodeInfo.getText().toString();

        intent.putExtra(DB_DATA, data);
        startActivity(intent);
    }

}
