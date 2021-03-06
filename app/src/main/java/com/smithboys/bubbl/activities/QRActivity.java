package com.smithboys.bubbl.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.smithboys.bubbl.MainActivity;
import com.smithboys.bubbl.activities.DashboardActivity;
import com.smithboys.bubbl.R;
import com.smithboys.bubbl.utils.UpdateDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class QRActivity extends AppCompatActivity {
    private static final int RC_PERMISSION = 10;
    private CodeScanner mCodeScanner;
    private boolean mPermissionGranted;

    private boolean qrVerified;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        try {
                            JSONObject reader = new JSONObject(result.getText());
                            int day = reader.getInt("day");
                            int month = reader.getInt("month");
                            int year = reader.getInt("year");
                            String token = reader.getString("token");
                            if (!token.equals("vOEVroRAwn")) {
                                throw new Exception();
                            }
                            String site = reader.getString("site");

                            System.out.println(month + "/" + day + "/" + year + " " + site);
                            Toast.makeText(QRActivity.this, "Test Verified", Toast.LENGTH_LONG).show();
                            qrVerified = true;
                            UpdateDataUtils.updateUserData(true);
                            startActivity(new Intent(QRActivity.this, DashboardActivity.class));
                        } catch (JSONException je) {
                            Toast.makeText(QRActivity.this, "Invalid QR Code", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(QRActivity.this, QRActivity.class));
                        } catch (Exception e) {
                            Toast.makeText(QRActivity.this, "Invalid Token", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(QRActivity.this, QRActivity.class));
                        }
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = false;
                requestPermissions(new String[] {Manifest.permission.CAMERA}, RC_PERMISSION);
            } else {
                mPermissionGranted = true;
            }
        } else {
            mPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                mCodeScanner.startPreview();
            } else {
                mPermissionGranted = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}