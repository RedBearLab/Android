package com.redbear.bleselect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BLESelect extends Activity {
	private final static String TAG = BLESelect.class.getSimpleName();

	private RBLService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	public static List<BluetoothDevice> mDevice = new ArrayList<BluetoothDevice>();

	Button lastDeviceBtn = null;
	Button scanAllBtn = null;
	TextView uuidTv = null;
	TextView lastUuid = null;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 3000;
	public static final int REQUEST_CODE = 30;
	private String mDeviceAddress;
	private String mDeviceName;
	private boolean flag = true;
	private boolean connState = false;

	String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	String fname = "flash.txt";

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((RBLService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_CONNECTED.equals(action)) {
				flag = true;
				connState = true;

				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_SHORT).show();
				writeToFile(mDeviceName + " ( " + mDeviceAddress + " )");
				lastUuid.setText(mDeviceName + " ( " + mDeviceAddress + " )");
				lastDeviceBtn.setVisibility(View.GONE);
				scanAllBtn.setText("Disconnect");
				startReadRssi();
			} else if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
				flag = false;
				connState = false;

				Toast.makeText(getApplicationContext(), "Disconnected",
						Toast.LENGTH_SHORT).show();
				scanAllBtn.setText("Scan All");
				uuidTv.setText("");
				lastDeviceBtn.setVisibility(View.VISIBLE);
			} else if (RBLService.ACTION_GATT_RSSI.equals(action)) {
				displayData(intent.getStringExtra(RBLService.EXTRA_DATA));
			}
		}
	};

	private void writeToFile(String flash) {
		File sdfile = new File(path, fname);
		try {
			FileOutputStream out = new FileOutputStream(sdfile);
			out.write(flash.getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	private String readConnDevice() {
		String filepath = path + "/" + fname;
		String line = null;

		File file = new File(filepath);
		try {
			FileInputStream f = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(f, "GB2312");
			BufferedReader dr = new BufferedReader(isr);
			line = dr.readLine();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return line;
	}

	private void displayData(String data) {
		if (data != null) {
			uuidTv.setText("RSSI:\t" + data);
		}
	}

	private void startReadRssi() {
		new Thread() {
			public void run() {

				while (flag) {
					mBluetoothLeService.readRssi();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		uuidTv = (TextView) findViewById(R.id.uuid);

		lastUuid = (TextView) findViewById(R.id.lastDevice);
		String connDeviceInfo = readConnDevice();
		if (connDeviceInfo == null) {
			lastUuid.setText("");
		} else {
			mDeviceName = connDeviceInfo.split("\\( ")[0].trim();
			String str = connDeviceInfo.split("\\( ")[1];
			mDeviceAddress = str.substring(0, str.length() - 2);
			lastUuid.setText(connDeviceInfo);
		}

		lastDeviceBtn = (Button) findViewById(R.id.ConnLastDevice);
		lastDeviceBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDevice.clear();
				String connDeviceInfo = readConnDevice();
				if (connDeviceInfo == null) {
					Toast toast = Toast.makeText(BLESelect.this,
							"No Last connect device!", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();

					return;
				}

				String str = connDeviceInfo.split("\\( ")[1];
				final String mDeviceAddress = str.substring(0, str.length() - 2);

				scanLeDevice();

				Timer mNewTimer = new Timer();
				mNewTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						for (BluetoothDevice device : mDevice)
							if ((device.getAddress().equals(mDeviceAddress))) {
								mBluetoothLeService.connect(mDeviceAddress);

								return;
							}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast toast = Toast.makeText(BLESelect.this,
										"No Last connect device!",
										Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
						});

					}
				}, SCAN_PERIOD);
			}
		});

		scanAllBtn = (Button) findViewById(R.id.ScanAll);
		scanAllBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (connState == false) {
					scanLeDevice();

					try {
						Thread.sleep(SCAN_PERIOD);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent intent = new Intent(getApplicationContext(),
							Device.class);
					startActivityForResult(intent, REQUEST_CODE);
				} else {
					mBluetoothLeService.disconnect();
					mBluetoothLeService.close();
					scanAllBtn.setText("Scan All");
					uuidTv.setText("");
					lastDeviceBtn.setVisibility(View.VISIBLE);
				}
			}
		});

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		Intent gattServiceIntent = new Intent(BLESelect.this, RBLService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_RSSI);

		return intentFilter;
	}

	private void scanLeDevice() {
		new Thread() {

			@Override
			public void run() {
				mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}.start();
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (device != null) {
						if (mDevice.indexOf(device) == -1)
							mDevice.add(device);
					}
				}
			});
		}
	};

	@Override
	protected void onStop() {
		super.onStop();

		flag = false;

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mServiceConnection != null)
			unbindService(mServiceConnection);

		System.exit(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		} else if (requestCode == REQUEST_CODE
				&& resultCode == Device.RESULT_CODE) {
			mDeviceAddress = data.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
			mDeviceName = data.getStringExtra(Device.EXTRA_DEVICE_NAME);
			mBluetoothLeService.connect(mDeviceAddress);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
