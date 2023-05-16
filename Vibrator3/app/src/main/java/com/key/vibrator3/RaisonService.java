package com.key.vibrator3;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import static android.content.ContentValues.TAG;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class RaisonService extends Service implements SensorEventListener {

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private SensorManager mSensorManager;
    private boolean phone_was_horizontal = false;

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private float getInclinationAngle(SensorManager mSensorManager) {
        // Rotation matrix based on current readings from accelerometer and magnetometer.
        mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        // Express the updated rotation matrix as three orientation angles.
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        //Log.i(TAG, "z: " + mOrientationAngles[0] + " x: " + mOrientationAngles[1] + " y: " + mOrientationAngles[2]);
        return mOrientationAngles[1]; //Pitch angle in radians
    }

    private boolean isPhoneFaceUp() {
        return mOrientationAngles[2] > -1.5;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.i(TAG, "Raisetowake service started!");

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI, new Handler());
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI, new Handler());

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }

        //check position
        float angle = getInclinationAngle(mSensorManager);

        if (phone_was_horizontal && isPhoneFaceUp() && ((angle >= -1) && (angle < -0.5))) {
            //show toast
            Toast.makeText(getApplicationContext(), "检测抬起手机", Toast.LENGTH_SHORT).show();
            sendBroadcastMessage(true);
            phone_was_horizontal = false;
        } else if (Math.abs(angle) < 0.5){
            phone_was_horizontal = true;
        }
    }

    private void sendBroadcastMessage(boolean phoneRaised) {
        Intent intent = new Intent("phone_raised_event");
        intent.putExtra("phone_raised", phoneRaised);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


}

