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

package com.pylapp.smoothclicker.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.pylapp.smoothclicker.clicker.ATClicker;
import com.pylapp.smoothclicker.notifiers.NotificationsManager;
import com.pylapp.smoothclicker.tools.ShakeToClean;
import com.pylapp.smoothclicker.utils.Config;
import com.pylapp.smoothclicker.R;
import com.pylapp.smoothclicker.utils.ConfigStatus;
import com.pylapp.smoothclicker.utils.ConfigVersions;
import com.pylapp.smoothclicker.tools.Logger;

import com.sa90.materialarcmenu.ArcMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * The main activity of this SmoothClicker app.
 * It shows the configuration widgets to set up the click actions
 *
 * @author pylapp
 * @version 2.7.0
 * @since 02/03/2016
 * @see AppCompatActivity
 * @see com.pylapp.smoothclicker.tools.ShakeToClean.ShakeToCleanCallback
 */
public class ClickerActivity extends AppCompatActivity implements ShakeToClean.ShakeToCleanCallback {


    /* ********* *
     * CONSTANTS *
     * ********* */

    /**
     * The result code for the SelectPointActivity
     */
    @Deprecated
    private static final int SELECT_POINT_ACTIVITY_RESULT_CODE = 0x000011;
    /**
     * The key to get the selected point
     */
    @Deprecated
    public static final String SELECT_POINT_ACTIVITY_RESULT = "0x000012";

    /**
     * The result code for the SelectMultiPointsActivity
     */
    private static final int SELECT_POINTS_ACTIVITY_RESULT_CODE = 0x000013;
    /**
     * The key to get the selected points
     */
    public static final String SELECT_POINTS_ACTIVITY_RESULT = "0x000014";



    private static final String LOG_TAG = "ClickerActivity";


    /* ****************************** *
     * METHODS FROM AppCompatActivity *
     * ****************************** */

    /**
     * Triggered to create the view
     *
     * @param savedInstanceState -
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            Switch typeOfStart = (Switch) findViewById(R.id.sTypeOfStartDelayed);
            typeOfStart.setChecked(savedInstanceState.getBoolean(Config.SP_START_TYPE_DELAYED));
            EditText et = (EditText) findViewById(R.id.etDelay);
            et.setText(savedInstanceState.getString(Config.SP_KEY_DELAY));
            et = (EditText) findViewById(R.id.etTimeBeforeEachClick);
            et.setText(savedInstanceState.getString(Config.SP_KEY_TIME_GAP));
            et = (EditText) findViewById(R.id.etRepeat);
            et.setText(savedInstanceState.getString(Config.SP_KEY_REPEAT));
            CheckBox cb = (CheckBox) findViewById(R.id.cbVibrateOnStart);
            cb.setChecked(savedInstanceState.getBoolean(Config.SP_KEY_VIBRATE_ON_START));
            cb = (CheckBox) findViewById(R.id.cbVibrateOnClick);
            cb.setChecked(savedInstanceState.getBoolean(Config.SP_KEY_VIBRATE_ON_CLICK));
            cb = (CheckBox) findViewById(R.id.cbNotifOnClick);
            cb.setChecked(savedInstanceState.getBoolean(Config.SP_KEY_NOTIF_ON_CLICK));
            cb = (CheckBox) findViewById(R.id.cbEndlessRepeat);
            cb.setChecked(savedInstanceState.getBoolean(Config.SP_KEY_REPEAT_ENDLESS));
            et = (EditText) findViewById(R.id.etRepeat);
            et.setEnabled( ! cb.isEnabled() );
        }

        setContentView(R.layout.activity_clicker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initInnerListeners();

    }

    /**
     * Triggered when the view has been created
     *
     * @param savedInstanceState -
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        initDefaultValues();
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Triggered when the back button is pressed
     */
    @Override
    public void onBackPressed(){
        handleExit();
    }

    /**
     * Triggered to save the state
     * @param savedInstanceState -
     */
    @Override
    public void onSaveInstanceState( Bundle savedInstanceState ){

        // Get values to save

        Switch sTypeOfStart = (Switch) findViewById(R.id.sTypeOfStartDelayed);
        boolean isDelayed = sTypeOfStart.isChecked();

        EditText et = (EditText) findViewById(R.id.etDelay);
        int delayInS;
        if ( et.getText() == null || et.getText().toString().length() <= 0 ) delayInS = 0;
        else delayInS = Integer.parseInt(et.getText().toString());

        et = (EditText) findViewById(R.id.etTimeBeforeEachClick);
        int timeGapInS;
        if ( et.getText() == null || et.getText().toString().length() <= 0 ) timeGapInS = 0;
        else timeGapInS = Integer.parseInt(et.getText().toString());

        et = (EditText) findViewById(R.id.etRepeat);
        int repeatEach;
        if ( et.getText() == null || et.getText().toString().length() <= 0 ) repeatEach = 0;
        else repeatEach = Integer.parseInt(et.getText().toString());

        CheckBox cb = (CheckBox) findViewById(R.id.cbEndlessRepeat);
        boolean isEndlessRepeat = cb.isChecked();

        cb = (CheckBox) findViewById(R.id.cbVibrateOnStart);
        boolean isVibrateOnStart = cb.isChecked();

        cb = (CheckBox) findViewById(R.id.cbVibrateOnClick);
        boolean isVibrateOnClick = cb.isChecked();

        cb = (CheckBox) findViewById(R.id.cbNotifOnClick);
        boolean isNotifOnClick = cb.isChecked();

        // Save the values

        savedInstanceState.putBoolean(Config.SP_START_TYPE_DELAYED, isDelayed);
        savedInstanceState.putInt(Config.SP_KEY_DELAY, delayInS);
        savedInstanceState.putInt(Config.SP_KEY_TIME_GAP, timeGapInS);
        savedInstanceState.putInt(Config.SP_KEY_REPEAT, repeatEach);
        savedInstanceState.putBoolean(Config.SP_KEY_REPEAT_ENDLESS, isEndlessRepeat);
        savedInstanceState.putBoolean(Config.SP_KEY_VIBRATE_ON_START, isVibrateOnStart);
        savedInstanceState.putBoolean(Config.SP_KEY_VIBRATE_ON_CLICK , isVibrateOnClick);
        savedInstanceState.putBoolean(Config.SP_KEY_NOTIF_ON_CLICK , isNotifOnClick);

        super.onSaveInstanceState(savedInstanceState);

    }

    /**
     * Triggered to create the options menu
     * @param menu -
     * @return boolean - Always true
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        getMenuInflater().inflate(R.menu.menu_clicker, menu);
        return true;
    }

    /**
     * Triggered when the activity results
     * @param requestCode -
     * @param resultCode -
     * @param data -
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ){

        switch ( requestCode ){
            case SELECT_POINTS_ACTIVITY_RESULT_CODE:
                if ( resultCode == Activity.RESULT_OK ) {
                    ArrayList<Integer> alp = data.getIntegerArrayListExtra(SELECT_POINTS_ACTIVITY_RESULT);
                    handleMultiPointResult(alp);
                }
                break;
            default:
                break;
        }

    }

    /**
     * Triggered when an itemahs been selected on the options menu
     * @param item -
     * @return boolean -
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        int id = item.getItemId();
        switch ( id ){
            case R.id.action_settings:
                startSettingsActivity();
                break;
            case R.id.action_exit:
                handleExit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * To call to finish this main activity and quit the app
     */
    @Override
    public void finish(){
        SplashScreenActivity.sIsFirstLaunch = true;
        stopClickingProcess();
        NotificationsManager.getInstance(this).stopAllNotifications();
        super.finish();
    }

    /**
     * Triggered when the activity is resuming
     */
    @Override
    public void onResume(){
        super.onResume();
        ShakeToClean.getInstance(this).register();
        ShakeToClean.getInstance(this).registerCallback(this);
    }

    /**
     * Triggered when the activity is pausing
     */
    @Override
    public void onPause(){
        super.onPause();
        ShakeToClean.getInstance(this).unregister();
        ShakeToClean.getInstance(this).unregisterCallback();
    }


    /* ************* *
     * OTHER METHODS *
     * ************* */

    /**
     * Gets the configuration from the widgets and backs it up
     * @return ConfigStatus - The state of the config
     */
    private ConfigStatus updateConfig(){

        Logger.d(LOG_TAG, "Updates configuration");

        // Get the defined values
        Switch sTypeOfStart = (Switch) findViewById(R.id.sTypeOfStartDelayed);
        boolean isDelayed = sTypeOfStart.isChecked();

        EditText et = (EditText) findViewById(R.id.etDelay);
        if ( et.getText() == null ) return ConfigStatus.DELAY_NOT_DEFINED;
        int delayInS;
        if ( et.getText().toString().length() <= 0 ) delayInS = 0;
        else delayInS = Integer.parseInt(et.getText().toString());

        et = (EditText) findViewById(R.id.etTimeBeforeEachClick);
        if ( et.getText() == null ) return ConfigStatus.TIME_GAP_NOT_DEFINED;
        int timeGapInS;
        if ( et.getText().toString().length() <= 0 ) timeGapInS = 0;
        else timeGapInS = Integer.parseInt(et.getText().toString());

        et = (EditText) findViewById(R.id.etRepeat);
        if ( et.getText() == null ) return ConfigStatus.REPEAT_NOT_DEFINED;
        int repeatEach;
        if ( et.getText().toString().length() <= 0 ) repeatEach = 0;
        else repeatEach = Integer.parseInt(et.getText().toString());

        CheckBox cb = (CheckBox) findViewById(R.id.cbEndlessRepeat);
        boolean isEndlessRepeat = cb.isChecked();

        cb = (CheckBox) findViewById(R.id.cbVibrateOnStart);
        boolean isVibrateOnStart = cb.isChecked();

        cb = (CheckBox) findViewById(R.id.cbVibrateOnClick);
        boolean isVibrateOnClick = cb.isChecked();

        cb = ( CheckBox) findViewById(R.id.cbNotifOnClick);
        boolean isDisplayNotifs = cb.isChecked();

        // Update the shared preferences
        SharedPreferences sp = getSharedPreferences(Config.SMOOTHCLICKER_SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Config.SP_START_TYPE_DELAYED, isDelayed);
        editor.putInt(Config.SP_KEY_DELAY, delayInS);
        editor.putInt(Config.SP_KEY_TIME_GAP, timeGapInS);
        editor.putInt(Config.SP_KEY_REPEAT, repeatEach);
        editor.putBoolean(Config.SP_KEY_REPEAT_ENDLESS, isEndlessRepeat);
        editor.putBoolean(Config.SP_KEY_VIBRATE_ON_START, isVibrateOnStart);
        editor.putBoolean(Config.SP_KEY_VIBRATE_ON_CLICK, isVibrateOnClick);
        editor.putBoolean(Config.SP_KEY_NOTIF_ON_CLICK, isDisplayNotifs);

        editor.commit();

        return ConfigStatus.TIME_GAP_NOT_DEFINED.READY;

    }

    /**
     * Runs the clicking process
     */
    private void startClickingProcess() {

        // Check if we make an endless repeat...
        SharedPreferences sp = getSharedPreferences(Config.SMOOTHCLICKER_SHARED_PREFERENCES_NAME, Config.SP_ACCESS_MODE);
        boolean isEndlessRepeat = sp.getBoolean(Config.SP_KEY_REPEAT_ENDLESS, Config.DEFAULT_REPEAT_ENDLESS);

        PointsListAdapter pla = (PointsListAdapter) ((Spinner) findViewById(R.id.sPointsToClick)).getAdapter();
        if (pla == null || pla.getList().size() <= 0){
            displayMessage(MessageTypes.NO_CLICK_DEFINED);
            return;
        }

        final List<PointsListAdapter.Point> lp = pla.getList();

        if ( isEndlessRepeat ){

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.warning_hazard_repeat_endless_title))
                    .setMessage(getString(R.string.warning_hazard_repeat_endless_content))
                    .setPositiveButton(R.string.warning_hazard_repeat_endless_yes, new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ){
                            // Go !
                            ATClicker.stop();
                            ATClicker.getInstance(ClickerActivity.this).execute(lp);
                        }
                    })
                    .setNegativeButton(R.string.warning_hazard_repeat_endless_no, new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ){
                            // Do nothing
                            return;
                        }
                    })
                    .show();

        } else {

            // Go !
            ATClicker.stop();
            ATClicker.getInstance(this).execute(lp);

        }

    }

    /**
     * Stops the running process
     */
    private void stopClickingProcess(){
        Logger.d(LOG_TAG, "Stops the clicking process");
        ATClicker.stop();
    }

    /**
     * Initializes the default values
     */
    private void initDefaultValues(){

        Logger.d(LOG_TAG, "Initializes the default values");

        Switch typeOfStart = (Switch) findViewById(R.id.sTypeOfStartDelayed);
        typeOfStart.setChecked(Config.DEFAULT_START_DELAYED);
        EditText et = (EditText) findViewById(R.id.etDelay);
        et.setText(Config.DEFAULT_DELAY);
        et = (EditText) findViewById(R.id.etTimeBeforeEachClick);
        et.setText(Config.DEFAULT_TIME_GAP);
        et = (EditText) findViewById(R.id.etRepeat);
        et.setText(Config.DEFAULT_REPEAT);
        CheckBox cb = (CheckBox) findViewById(R.id.cbVibrateOnStart);
        cb.setChecked(Config.DEFAULT_VIBRATE_ON_START);
        cb = (CheckBox) findViewById(R.id.cbVibrateOnClick);
        cb.setChecked(Config.DEFAULT_VIBRATE_ON_CLICK);
        cb = (CheckBox) findViewById(R.id.cbNotifOnClick);
        cb.setChecked(Config.DEFAULT_NOTIF_ON_CLICK);

        handleMultiPointResult( null ); // Make the spinner of points to click empty

    }

    /**
     * Displays the "about" information, i.e. some information about the app.
     * The display is made in the snackbar.
     */
    private void displayAboutInfo(){

        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch ( PackageManager.NameNotFoundException | NullPointerException e ){
            e.printStackTrace();
        }

        String tag = ConfigVersions.VERSION_TAG_CURRENT;
        String code = pi.versionCode+"";
        String name = pi.versionName;

        StringBuffer sb = new StringBuffer();
        sb.append(tag).append(" - Version code : ").append(code).append(" - Version : ").append(name);
        showInSnackbarWithoutAction(sb.toString());

    }

    /**
     * Displays a message in the snackbar and the logger
     * @param mt - The type of the message to display
     */
    private void displayMessage( MessageTypes mt ){
        String m = null;
        switch ( mt ){
            case START_PROCESS:
                m = ClickerActivity.this.getString(R.string.info_message_start);
                Logger.d("SmoothClicker", m);
                break;
            case STOP_PROCESS:
                m = ClickerActivity.this.getString(R.string.info_message_stop);
                Logger.d("SmoothClicker", m);
                break;
            case SU_GRANTED:
                m = ClickerActivity.this.getString(R.string.info_message_su_granted);
                Logger.i("SmoothClicker", m);
                break;
            case SU_NOT_GRANTED:
                m = ClickerActivity.this.getString(R.string.error_message_su_not_granted);
                Logger.e("SmoothClicker", m);
                break;
            case NOT_IMPLEMENTED:
                m = ClickerActivity.this.getString(R.string.error_not_implemented);
                Logger.e("SmoothClicker", m);
                break;
            case SU_GRANT:
                m = ClickerActivity.this.getString(R.string.info_message_request_su);
                Logger.d("SmoothClicker", m);
                break;
            case NEW_CLICK:
                m = ClickerActivity.this.getString(R.string.info_message_new_point);
                Logger.d("SmoothClicker", m);
                break;
            case NO_CLICK_DEFINED:
                m = ClickerActivity.this.getString(R.string.error_message_no_click_defined);
                Logger.d("SmoothClicker", m);
                break;
            default:
                m = null;
                break;
        }
        if ( m == null || m.length() <= 0 ) return;
        showInSnackbarWithoutAction(m);
    }

    /**
     * Displays a "do you want to exit" pop-up, and if the user clicks on OK, will stop all clicks process and finish.
     */
    private void handleExit(){

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.message_confirm_exit_label))
                .setMessage(getString(R.string.message_confirm_exit_content))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ){
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ){
                        // Do nothing
                        return;
                    }
                })
                .show();

    }

    /**
     * Handles the list of coordinates of the points to click on.
     * Will update the list showing to the user the points.
     * Will update the config so as to allow the ATClicker to click on all these points.
     *
     * @param coords - The list of X/Y values of the points to click on as {x0, y0, x1, y2, ..., xN, yN}
     */
    private void handleMultiPointResult( ArrayList<Integer> coords ){

        final Spinner s = (Spinner) findViewById(R.id.sPointsToClick);
        s.setAdapter(null); // Clean the list view

        if ( coords == null ) return;

        s.setAdapter(new PointsListAdapter(this, coords));
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s.setSelection(0); // The item 0 is a label saying to the user the account of items in the list
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                s.setSelection(0); // The item 0 is a label saying to the user the account of items in the list
            }
        });
    }

    /**
     * Starts the settings activity
     */
    private void startSettingsActivity(){
        startActivity(new Intent(ClickerActivity.this, SettingsActivity.class));
    }

    /**
     * Starts the activity which allows the user to select a point on its screen
     */
    private void startSelectPointActivity(){
        Intent i = new Intent(ClickerActivity.this, SelectMultiPointsActivity.class);
        startActivityForResult(i, SELECT_POINTS_ACTIVITY_RESULT_CODE);
    }

    /**
     * Displays in the snack bar a message
     * @param message - The string to display. Will do nothing if null or empty
     */
    private void showInSnackbarWithoutAction( String message ){
        if ( message == null || message.length() <= 0 ) return;
        View v = findViewById(R.id.myMainLayout);
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction("", null).show();
    }

    /**
     * Requests the SU grant by starting a SU process which will trigger
     * the "SU grant" system window
     */
    private void requestSuGrant(){
        try {
            Logger.d(LOG_TAG, "Get 'su' process...");
            Runtime.getRuntime().exec("su");
        } catch ( IOException e ){
            Logger.e(LOG_TAG, "Exception thrown during 'su' : " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "An error occurs during SU grant : "+e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Did you root your Android before using this app? It is mandatory", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initializes the listeners on the widgets
     */
    private void initInnerListeners(){

        // The button to trigger the click(s) process
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabStart);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayMessage(MessageTypes.START_PROCESS);
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateConfig();
                NotificationsManager.getInstance(ClickerActivity.this).refresh(ClickerActivity.this);
                startClickingProcess();
            }
        });

        // The stop button
        fab = (FloatingActionButton) findViewById(R.id.fabStop);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayMessage(MessageTypes.STOP_PROCESS);
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ){
                stopClickingProcess();
            }
        });

        // The button to add a new point to click on
        fab = (FloatingActionButton) findViewById(R.id.fabSelectPoint);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayMessage(MessageTypes.NEW_CLICK);
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArcMenu fabAction = (ArcMenu) findViewById(R.id.fabAction);
                if ( fabAction.isMenuOpened() ) fabAction.toggleMenu();
                startSelectPointActivity();
            }
        });

        // The button to request SU grant
        fab = (FloatingActionButton) findViewById(R.id.fabRequestSuGrant);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayMessage(MessageTypes.SU_GRANT);
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSuGrant();
            }
        });

        // The switch button about the type of start
        // If checked, enabled the filed for the delay
        Switch sTypeOfStart = (Switch) findViewById(R.id.sTypeOfStartDelayed);
        sTypeOfStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ){
                EditText etDelay = (EditText) findViewById(R.id.etDelay);
                etDelay.setEnabled(isChecked);
                EditText etR = (EditText) findViewById(R.id.etRepeat);
                if ( "666".equals(etR.getText().toString()) ) Toast.makeText(ClickerActivity.this, "✿✿✿✿ ʕ •ᴥ•ʔ/ ︻デ═一 Hotter Than Hell !", Toast.LENGTH_SHORT).show();
            }
        });

        // The endless repeat
        CheckBox cb = (CheckBox) findViewById(R.id.cbEndlessRepeat);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ){
                EditText etRepeat = (EditText) findViewById(R.id.etRepeat);
                etRepeat.setEnabled( ! isChecked );
            }
        });

    }


    /* ********************************* *
     * METHODS FROM ShakeToCleanCallback *
     * ********************************* */

    /**
     * Triggered when a shake to clean event has been thrown
     */
    @Override
    public void shakeToClean() {
        // Check if the shake to clean option is enabled
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_KEY_SHAKE_TO_CLEAN, Config.DEFAULT_SHAKE_TO_CLEAN)) {
            Toast.makeText(this, getString(R.string.message_reinit_config), Toast.LENGTH_SHORT).show();
            initDefaultValues();
            ArcMenu fabAction = (ArcMenu) findViewById(R.id.fabAction);
            if ( fabAction.isMenuOpened() ) fabAction.toggleMenu();
        }
    }

    /* *********** *
     * INNER ENUMS *
     * *********** */

    /**
     * The type of messages to display
     */
    private enum MessageTypes {
        SU_GRANTED,
        SU_NOT_GRANTED,
        START_PROCESS,
        STOP_PROCESS,
        NEW_CLICK,
        NOT_IMPLEMENTED,
        SU_GRANT,
        NO_CLICK_DEFINED
    } // private enum MESSAGE_TYPE

}
