/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zlian.netgap.mtool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import zlian.netgap.R;
import zlian.netgap.util.CommonFun;


/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
//    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private TextView tv_title;
    private ProgressBar pb;

//    Handler handler=new Handler();
//
//    Runnable runnable=new Runnable() {
//        @Override
//        public void run() {
//            // Get a set of currently paired devices
//            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
//
//            if (pairedDevices.size() != mPairedDevicesArrayAdapter.getCount()) {
//                mPairedDevicesArrayAdapter.clear();
//                if (pairedDevices.size() > 0) {
//                    findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
//                    for (BluetoothDevice device : pairedDevices) {
//                        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//                    }
//                } else {
//                    String noDevices = getResources().getText(R.string.none_paired).toString();
//                    mPairedDevicesArrayAdapter.add(noDevices);
//                }
//            }
//            else {
//                if (pairedDevices.size()==0){
//                    mPairedDevicesArrayAdapter.clear();
//                    String noDevices = getResources().getText(R.string.none_paired).toString();
//                    mPairedDevicesArrayAdapter.add(noDevices);
//                }
//                else {
//                    String devname = "";
//                    String item = "";
//                    boolean bhave = false;
//                    for (BluetoothDevice device : pairedDevices) {
//                        bhave = false;
//                        devname = device.getName() + "\n" + device.getAddress();
//                        for(int i=0;i<mPairedDevicesArrayAdapter.getCount();i++){
//                            item = mPairedDevicesArrayAdapter.getItem(i).toString();
//                            if (devname.equals(item)) {
//                                bhave = true;
//                                break;
//                            }
//                        }
//                        if (!bhave) {
//                            break;
//                        }
//                    }
//                    if (!bhave) {
//                        mPairedDevicesArrayAdapter.clear();
//                        findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
//                        for (BluetoothDevice device : pairedDevices) {
//                            mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//                        }
//                    }
//                }
//            }
//            // Find and set up the ListView for paired devices
//            ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
//            pairedListView.setAdapter(mPairedDevicesArrayAdapter);
//
//            handler.postDelayed(this, 2000);
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        tv_title = (TextView) findViewById(R.id.tv_title);
        pb = (ProgressBar) findViewById(R.id.pb);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
//                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
//        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
//        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
//        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
//        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
//        if (pairedDevices.size() > 0) {
//            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
//            for (BluetoothDevice device : pairedDevices) {
//                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//            }
//        } else {
//            String noDevices = getResources().getText(R.string.none_paired).toString();
//            mPairedDevicesArrayAdapter.add(noDevices);
//        }
//
//        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // stop timer
//        handler.removeCallbacks(runnable);

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        // 判断蓝牙是否支持和开启
        // 如果没有开启，就不连接和登陆
        CommonFun.enable_bluethooth();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(DeviceListActivity.this, R.string.please_turn_on_bt, Toast.LENGTH_LONG).show();
            return;
        }

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        pb.setVisibility(View.VISIBLE);
        tv_title.setText(R.string.scanning);

        // Turn on sub-title for new devices
//        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mNewDevicesArrayAdapter.clear();
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if(info.length()<=18)
            {
            	return;
            }
            String name = info.substring(0,info.length() - 18);
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_NAME, name);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//            if (findViewById(R.id.title_new_devices).getVisibility() != View.VISIBLE) {
//                return;
//            }
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    boolean bSame = false;

                    for (int i = 0; i < mNewDevicesArrayAdapter.getCount(); i++) {
                        String temp = mNewDevicesArrayAdapter.getItem(i);

                        int nPosition = temp.indexOf("\n");
                        String macAddr = temp.substring(nPosition + 1);
                        if (device.getAddress().equals(macAddr)) {
                            bSame = true;
                            break;
                        }
                    }
                    if (!bSame) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                pb.setVisibility(View.GONE);
                tv_title.setText(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
