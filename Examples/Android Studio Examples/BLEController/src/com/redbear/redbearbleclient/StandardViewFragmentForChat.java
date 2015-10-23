package com.redbear.redbearbleclient;

import android.app.Fragment;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redbear.RedBearService.IRedBearServiceEventListener;
import com.redbear.RedBearService.RedBearService;
import com.redbear.redbearbleclient.MainPage.Device;

public class StandardViewFragmentForChat extends Fragment {

	Device mDevice;

	TextView textName = null;

	TextView textRssi = null;

	EditText editOutput = null;

	EditText editInput = null;

	Button btnSend = null;

	boolean isFirst = true;

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	public StandardViewFragmentForChat() {
	}

	public StandardViewFragmentForChat(Device mDevice) {
		this.mDevice = mDevice;
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

			if (mBearService != null) {
				if (mDevice != null) {
					mBearService.readRssi(mDevice.address);
				}
			}
		}
	};

	final IRedBearServiceEventListener mIScanDeviceListener = new IRedBearServiceEventListener() {

		@Override
		public void onDeviceFound(String deviceAddress, String name, int rssi,
				int bondState, byte[] scanRecord, ParcelUuid[] uuids) {
		}

		@Override
		public void onDeviceRssiUpdate(final String deviceAddress,
				final int rssi, final int state) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					deviceRssiStateChange(deviceAddress, rssi, state);
					if (isFirst) {
						mHandler.sendEmptyMessageDelayed(0, 1000);
						isFirst = false;
					} else {
						mHandler.sendEmptyMessageDelayed(0, 300);
					}
				}
			});

		}

		@Override
		public void onDeviceConnectStateChange(final String deviceAddress,
				final int state) {

			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {

					deviceConnectStateChange(deviceAddress, state);

				}
			});

		}

		@Override
		public void onDeviceReadValue(final int[] value) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {

					final int count = value.length;

					char[] chars = new char[count];

					for (int i = 0; i < count; i++) {
						chars[i] = (char) value[i];
					}

					String text = new String(chars);

					editInput.setText(text);

				}
			});
		}

		@Override
		public void onDeviceCharacteristicFound() {
			// TODO Auto-generated method stub
			
		}
	};

	protected void deviceRssiStateChange(String deviceAddress, int rssi,
			int state) {
		if (state == 0) {
			if (deviceAddress.equals(mDevice.address)) {
				mDevice.rssi = rssi;
				textRssi.setText("Rssi : " + rssi);
			}
		}
	}

	protected void deviceConnectStateChange(String deviceAddress, int state) {
		if (state == BluetoothProfile.STATE_CONNECTED) {
			Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT)
					.show();
		} else if (state == BluetoothProfile.STATE_DISCONNECTED) {

			if (getActivity() != null) {
				Toast.makeText(getActivity(), "Disconnected",
						Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}

		}
	}

	OnClickListener mSendClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!editOutput.getText().toString().isEmpty()) {
				String value = editOutput.getText().toString();

				if (mBearService != null) {
					char[] chars = value.toCharArray();
					mBearService.writeValue(mDevice.address, chars);
				}
			}

		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.activity_standardview, null);

		textName = (TextView) view.findViewById(R.id.text_devicename);

		textRssi = (TextView) view.findViewById(R.id.text_rssi);

		editInput = (EditText) view.findViewById(R.id.edit_input);

		editOutput = (EditText) view.findViewById(R.id.edit_output);

		btnSend = (Button) view.findViewById(R.id.btn_send);

		btnSend.setOnClickListener(mSendClick);

		if (mDevice != null) {
			textName.setText(mDevice.name);

			mDevice.rssi = 0;

			textRssi.setText("Rssi : " + mDevice.rssi);
		}

		return view;
	}

	@Override
	public void onResume() {

		Intent service = new Intent("com.redbear.RedBearService.RedBearService");

		getActivity().bindService(service, conn, Context.BIND_AUTO_CREATE);

		super.onResume();
	}

	public void disconnectDevice() {
		if (mBearService != null) {
			mBearService.disconnectDevice(mDevice.address);
		}
	}

	@Override
	public void onDestroy() {
		mHandler.removeMessages(0);
		disconnectDevice();
		getActivity().unbindService(conn);
		super.onDestroy();
	}

	RedBearService mBearService;

	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

			Log.e("onServiceConnected", "onServiceDisconnected");

			mBearService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			mBearService = ((RedBearService.LocalBinder) service).getService();
			Log.e("onServiceConnected", "mBearService : " + mBearService);

			if (mBearService != null) {
				if (mDevice != null) {
					mBearService.setListener(mIScanDeviceListener);
					textName.post(new Runnable() {

						@Override
						public void run() {
							mBearService.connectDevice(mDevice.address, false);
						}
					});
				}
			}
		}
	};
}
