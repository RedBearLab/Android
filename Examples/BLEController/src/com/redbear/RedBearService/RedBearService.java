package com.redbear.RedBearService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RedBearService extends Service {

	static final String TAG = RedBearService.class.getName();

	public static final UUID RBL_SERVICE = UUID
			.fromString("713D0000-503E-4C75-BA94-3148F18D941E");

	public static final UUID RBL_DEVICE_RX_UUID = UUID
			.fromString("713D0002-503E-4C75-BA94-3148F18D941E");

	public static final UUID RBL_DEVICE_TX_UUID = UUID
			.fromString("713D0003-503E-4C75-BA94-3148F18D941E");

	public static final UUID CCC = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");

	public static final UUID SERIAL_NUMBER_STRING = UUID
			.fromString("00002A25-0000-1000-8000-00805f9b34fb");

	private BluetoothAdapter mBtAdapter = null;

	public BluetoothGatt mBluetoothGatt = null;

	private IRedBearServiceEventListener mIRedBearServiceEventListener;

	HashMap<String, BluetoothDevice> mDevices = null;

	private BluetoothGattCharacteristic txCharc = null;

	public void startScanDevice() {
		if (mDevices != null) {
			mDevices.clear();
		} else {
			mDevices = new HashMap<String, BluetoothDevice>();
		}

		startScanDevices();
	}

	public void stopScanDevice() {
		stopScanDevices();
	}

	public void setListener(IRedBearServiceEventListener mListener) {
		mIRedBearServiceEventListener = mListener;
	}

	public boolean isBLEDevice(String address) {
		BluetoothDevice mBluetoothDevice = mDevices.get(address);
		if (mBluetoothDevice != null) {
			return isBLEDevice(address);
		}
		return false;
	}

	public void connectDevice(String address, boolean autoconnect) {
		BluetoothDevice mBluetoothDevice = mDevices.get(address);
		if (mBluetoothDevice != null) {
			connect(mBluetoothDevice, autoconnect);
		}
	}

	public void disconnectDevice(String address) {
		BluetoothDevice mBluetoothDevice = mDevices.get(address);
		if (mBluetoothDevice != null) {
			disconnect(mBluetoothDevice);
		}
	}

	public void readRssi(String deviceAddress) {
		readDeviceRssi(deviceAddress);
	}

	public void writeValue(String deviceAddress, char[] data) {
		if (txCharc != null) {
			String value = new String(data);

			if (txCharc.setValue(value)) {
				if (!mBluetoothGatt.writeCharacteristic(txCharc)) {
					Log.e(TAG, "Error: writeCharacteristic!");
				}
			} else {
				Log.e(TAG, "Error: setValue!");
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public RedBearService getService() {
			return RedBearService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = bluetoothManager.getAdapter();

		if (mBtAdapter == null)
			return;

		if (mDevices == null) {
			mDevices = new HashMap<String, BluetoothDevice>();
		}
	}

	public boolean isBLEDevice(BluetoothDevice device) {
		if (mBluetoothGatt != null) {
			return true;
		} else {
			return false;
		}
	}

	private void startScanDevices() {
		if (mBtAdapter == null)
			return;

		mBtAdapter.startLeScan(mLeScanCallback);
	}

	protected void stopScanDevices() {
		if (mBtAdapter == null)
			return;

		mBtAdapter.stopLeScan(mLeScanCallback);
	}

	protected void readDeviceRssi(String address) {
		BluetoothDevice mDevice = mDevices.get(address);
		if (mDevice != null) {
			readDeviceRssi(mDevice);
		}
	}

	protected void readDeviceRssi(BluetoothDevice device) {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.readRemoteRssi();
		}
	}

	protected void connect(BluetoothDevice device, boolean autoconnect) {
		mBluetoothGatt = device.connectGatt(this, autoconnect, mGattCallback);
	}

	protected void disconnect(BluetoothDevice device) {
		mBluetoothGatt.disconnect();
		mBluetoothGatt.close();
	}

	@Override
	public void onDestroy() {
		if (mBluetoothGatt == null)
			return;

		mBluetoothGatt.close();
		mBluetoothGatt = null;

		super.onDestroy();
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			Log.d(TAG, "onScanResult (device : " + device.getName() + ")");

			if (mIRedBearServiceEventListener != null) {
				Log.d(TAG, "mIScanDeviceListener (device : " + device.getName()
						+ ")");
				addDevice(device);
				mIRedBearServiceEventListener.onDeviceFound(
						device.getAddress(), device.getName(), rssi,
						device.getBondState(), scanRecord, device.getUuids());
			}
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {

			Log.d(TAG, "onCharacteristicChanged ( characteristic : "
					+ characteristic + ")");
			int i = 0;
			Integer temp = characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, i++);
			ArrayList<Integer> values = new ArrayList<Integer>();
			while (temp != null) {
				Log.e(TAG, "temp: " + temp);
				values.add(temp);
				temp = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, i++);
			}

			int[] received = new int[i];
			i = 0;
			for (Integer integer : values) {
				received[i++] = integer.intValue();
			}

			if (mIRedBearServiceEventListener != null) {

				mIRedBearServiceEventListener.onDeviceReadValue(received);
			}

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.d(TAG, "onCharacteristicRead ( characteristic :"
						+ characteristic + " ,status, : " + status + ")");
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.d(TAG, "onCharacteristicWrite ( characteristic :"
						+ characteristic + " ,status : " + status + ")");
			}
		};

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			BluetoothDevice device = gatt.getDevice();

			Log.d(TAG, "onConnectionStateChange (device : " + device
					+ ", status : " + status + " , newState :  " + newState
					+ ")");

			if (mIRedBearServiceEventListener != null) {
				mIRedBearServiceEventListener.onDeviceConnectStateChange(
						device.getAddress(), newState);
			}

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mBluetoothGatt.discoverServices();
				readDeviceRssi(device);
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor device, int status) {
			Log.d(TAG, "onDescriptorRead (device : " + device + " , status :  "
					+ status + ")");
			super.onDescriptorRead(gatt, device, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor arg0, int status) {
			Log.d(TAG, "onDescriptorWrite (arg0 : " + arg0 + " , status :  "
					+ status + ")");
			super.onDescriptorWrite(gatt, arg0, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			Log.d(TAG, "onReliableWriteCompleted (gatt : " + status
					+ " , status :  " + status + ")");
			super.onReliableWriteCompleted(gatt, status);
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			BluetoothDevice device = gatt.getDevice();

			Log.d(TAG, "onReadRemoteRssi (device : " + device + " , rssi :  "
					+ rssi + " , status :  " + status + ")");

			if (mIRedBearServiceEventListener != null) {
				mIRedBearServiceEventListener.onDeviceRssiUpdate(
						device.getAddress(), rssi, status);
			}

		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			BluetoothGattService rblService = mBluetoothGatt
					.getService(RBL_SERVICE);

			if (rblService == null) {
				Log.e(TAG, "RBL service not found!");
				return;
			}

			List<BluetoothGattCharacteristic> Characteristic = rblService
					.getCharacteristics();

			for (BluetoothGattCharacteristic a : Characteristic) {
				Log.e(TAG, " a =  uuid : " + a.getUuid() + "");
			}

			BluetoothGattCharacteristic rxCharc = rblService
					.getCharacteristic(RBL_DEVICE_RX_UUID);
			if (rxCharc == null) {
				Log.e(TAG, "RBL RX Characteristic not found!");
				return;
			}

			txCharc = rblService.getCharacteristic(RBL_DEVICE_TX_UUID);
			if (txCharc == null) {
				Log.e(TAG, "RBL RX Characteristic not found!");
				return;
			}

			enableNotification(true, rxCharc);

			if (mIRedBearServiceEventListener != null)
				mIRedBearServiceEventListener.onDeviceCharacteristicFound();
		}
	};

	public boolean enableNotification(boolean enable,
			BluetoothGattCharacteristic characteristic) {
		if (mBluetoothGatt == null) {
			return false;
		}
		if (!mBluetoothGatt.setCharacteristicNotification(characteristic,
				enable)) {
			return false;
		}

		BluetoothGattDescriptor clientConfig = characteristic
				.getDescriptor(CCC);
		if (clientConfig == null) {
			return false;
		}

		if (enable) {
			Log.i(TAG, "enable notification");
			clientConfig
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		} else {
			Log.i(TAG, "disable notification");
			clientConfig
					.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}

		return mBluetoothGatt.writeDescriptor(clientConfig);
	}

	void addDevice(BluetoothDevice mDevice) {
		String address = mDevice.getAddress();

		mDevices.put(address, mDevice);
	}
}
