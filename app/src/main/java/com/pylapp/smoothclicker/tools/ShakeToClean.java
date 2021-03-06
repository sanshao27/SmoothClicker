/*
    Copyright 2016 Pierre-Yves Lapersonne (aka. "pylapp",  pylapp(dot)pylapp(at)gmail(dot)com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
// ✿✿✿✿ ʕ •ᴥ•ʔ/ ︻デ═一

package com.pylapp.smoothclicker.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class which shows features for a Shake To Clean service.
 *
 * @author pylapp
 * @version 1.0.0
 * @since 17/03/2016
 * @see SensorEventListener
 */
public class ShakeToClean implements SensorEventListener {


    /* ********** *
     * ATTRIBUTES *
     * ********** */

    /**
     * The sensor manager for the accelerometer
     */
    private SensorManager mSensorManager;
    /**
     * The accelerometer
     */
    private Sensor mAccelerometer;
    /**
     * The last timestamp for the shake event
     */
    private long mLastTimestampAcc;
    /**
     * The variations of the axis
     */
    private float mX, mY, mZ, mXprev, mYprev, mZprev;

    /**
     * The object to warn when the shake to clean event has been detected
     */
    private ShakeToCleanCallback mCallback;

    /**
     *
     */
    private Context mContext;

    /**
     * The singleton
     */
    private static ShakeToClean sInstance;


    /* ********* *
     * CONSTANTS *
     * ********* */

    /**
     * A threshold for the accelerometer (in ms)
     */
    private static final long SEUIL_TIMESTAMP_ACCELEROMETER = 100;
    /**
     * A threshold for the shake event
     */
    private static final long SEUIL_SHAKE = 800;


    /* *********** *
     * CONSTRUCTOR *
     * *********** */

    /**
     *
     * @paramc -
     */
    public ShakeToClean( Context c ){
        super();
        mContext = c;
        init();
    }


    /* ******************************** *
     * METHODS FROM SensorEventListener *
     * ******************************** */

    /**
     *
     * @param se -
     */
    @Override
    public void onSensorChanged( SensorEvent se ){
        long actualAccTs = System.currentTimeMillis();
        if ( (actualAccTs - mLastTimestampAcc) > SEUIL_TIMESTAMP_ACCELEROMETER ){
            long diffTime = (actualAccTs - mLastTimestampAcc);
            mLastTimestampAcc = actualAccTs;
            mX = se.values[0];
            mY = se.values[1];
            mZ = se.values[2];
            float speed = Math.abs(mX+mY+mZ-mXprev-mYprev-mZprev) / diffTime * 10000;
            if ( speed > SEUIL_SHAKE ){
                if ( mCallback == null ){
                    throw new IllegalStateException("Nobody can handle the shake to clean event !");
                } else {
                    mCallback.shakeToClean();
                }
            }
            mXprev = mX;
            mYprev = mY;
            mZprev = mZ;
        }
    }

    /**
     * Does nothing
     * @param sensor -
     * @param accuracy -
     */
    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ){
        // DoNothing
        return;
    }


    /* ************* *
     * OTHER METHODS *
     * ************* */

    /**
     * Initializes the object
     */
    public void init(){
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Unregister the listener to the sensor manager
     */
    public void unregister(){
        mSensorManager.unregisterListener(this);
    }

    /**
     * Register the listener to the sensor manager
     */
    public void register(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Unregisters the shake to clean callback
     */
    public void unregisterCallback(){
        mCallback = null;
    }

    /**
     * Registers a shake to clean callback
     */
    public void registerCallback( ShakeToCleanCallback stcc ){
        if ( stcc == null ) throw new IllegalArgumentException("The ShakeToClean callback is null !");
        mCallback = stcc;
    }

    /**
     * Returns the singleton
     * @param c -
     * @return ShakeToClean
     */
    public static ShakeToClean getInstance( Context c ){
        if ( sInstance == null ) sInstance = new ShakeToClean(c);
        return sInstance;
    }


    /* *************** *
     * INNER INTERFACE *
     * *************** */

    /**
     * Interface which represents a callback to trigger on each shake to clean event
     */
    public interface ShakeToCleanCallback {

        /**
         * The callback triggered when a shake to clean event has been made
         */
        void shakeToClean();

    } // End of public interface ShakeToCleanCallback

}
