package com.redbear.redbearbleclient;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.redbear.RedBearService.IRedBearServiceEventListener;
import com.redbear.RedBearService.RedBearService;
import com.redbear.protocol.IRBLProtocol;
import com.redbear.protocol.RBLProtocol;
import com.redbear.redbearbleclient.MainPage.Device;
import com.redbear.redbearbleclient.data.PinInfo;

public class StandardViewFragmentForPins extends Fragment implements
		IRBLProtocol {

	final String TAG = "StandardViewFragmentForPins";
	Device mDevice;
	TextView textRssi;
	TextView textName;
	ProgressBar mLoading;
	ListView listView;
	boolean isFirstReadRssi = true;
	boolean isFirstReadPin = true;
	RedBearService mRedBearService;
	RBLProtocol mProtocol;
	SparseArray<PinInfo> pins;
	HashMap<String, PinInfo> changeValues; // to init value
	PinAdapter mAdapter;

	public StandardViewFragmentForPins() {

	}

	public StandardViewFragmentForPins(Device mDevice) {
		this.mDevice = mDevice;
		pins = new SparseArray<PinInfo>();
		changeValues = new HashMap<String, PinInfo>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_standardview_pins, null);

		textRssi = (TextView) view.findViewById(R.id.text_rssi);
		textName = (TextView) view.findViewById(R.id.text_devicename);
		listView = (ListView) view.findViewById(R.id.io_list);
		listView.setEnabled(false);
		mLoading = (ProgressBar) view.findViewById(R.id.pin_loading);
		if (mDevice != null) {
			textName.setText(mDevice.name);

			mDevice.rssi = 0;

			textRssi.setText("Rssi : " + mDevice.rssi);

			mProtocol = new RBLProtocol(mDevice.address);
			mProtocol.setIRBLProtocol(this);
		}

		return view;
	}

	@Override
	public void onDestroy() {
		mHandler.removeMessages(0);
		disconnectDevice();
		getActivity().unbindService(mServiceConnection);
		super.onDestroy();
	}

	public void disconnectDevice() {
		if (mRedBearService != null) {
			mRedBearService.disconnectDevice(mDevice.address);
		}
	}

	@Override
	public void onResume() {

		Intent service = new Intent("com.redbear.RedBearService.RedBearService");

		getActivity().bindService(service, mServiceConnection,
				Context.BIND_AUTO_CREATE);

		super.onResume();
	}

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

			Log.e("onServiceConnected", "onServiceDisconnected");

			mRedBearService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			mRedBearService = ((RedBearService.LocalBinder) service).getService();
			Log.e("onServiceConnected", "mBearService : " + mRedBearService);

			if (mRedBearService != null) {
				if (mDevice != null) {
					if (mProtocol != null) {
						mProtocol.setmIRedBearService(mRedBearService);
					}
					mRedBearService
							.setListener(mIRedBearServiceEventListener);
					textName.post(new Runnable() {

						@Override
						public void run() {
							mRedBearService.connectDevice(
									mDevice.address, false);
						}
					});
				}
			}
		}
	};

	final IRedBearServiceEventListener mIRedBearServiceEventListener = new IRedBearServiceEventListener() {

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
					if (isFirstReadRssi) {
						mHandler.sendEmptyMessageDelayed(0, 1000);
						isFirstReadRssi = false;
					} else {
						mHandler.sendEmptyMessageDelayed(0, 300);
					}
				}
			});

		}

		@Override
		public void onDeviceConnectStateChange(final String deviceAddress,
				final int state) {

			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {

						deviceConnectStateChange(deviceAddress, state);

					}
				});
			}
		}

		@Override
		public void onDeviceReadValue(int[] value) {

			if (mProtocol != null) {
				mProtocol.parseData(value);
			}
		}

		@Override
		public void onDeviceCharacteristicFound() {
			// TODO Auto-generated method stub
			
		}
	};

	Handler.Callback mHandlerCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {

			if (mRedBearService != null) {
				if (mDevice != null) {
					mRedBearService.readRssi(mDevice.address);
				}
			}
			return true;
		}
	};

	Handler mHandler = new Handler(mHandlerCallback);

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

			if (textRssi != null) {
				textRssi.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (mProtocol != null) {
							mProtocol.queryProtocolVersion();
						}
					}
				}, 300);
			}

		} else if (state == BluetoothProfile.STATE_DISCONNECTED) {

			if (getActivity() != null) {
				Toast.makeText(getActivity(), "Disconnected",
						Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}

		}
	}

	@Override
	public void protocolDidReceiveCustomData(int[] data, int length) {
		Log.e(TAG, "protocolDidReceiveCustomData data : " + data
				+ ", length : " + length);

		final int count = data.length;

		char[] chars = new char[count];

		for (int i = 0; i < count; i++) {
			chars[i] = (char) data[i];
		}

		String temp = new String(chars);
		Log.e(TAG, "temp : " + temp);
		if (temp.startsWith("ABC")) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() { // removed
							// loading
							// and let
							// the
							// listview
							// working

							@Override
							public void run() {
								if (mLoading != null) {
									mLoading.setVisibility(View.GONE);
								}
								if (changeValues != null) {
									final int count = pins.size();
									for (int i = 0; i < count; i++) {
										int key = pins.keyAt(i);
										PinInfo pInfo = pins.get(key);
										PinInfo changedPinInfo = changeValues
												.get(key + "");

										if (changedPinInfo != null) {
											pInfo.mode = changedPinInfo.mode;
											pInfo.value = changedPinInfo.value;
										}
									}
									changeValues = null;
									updateData();
									isFirstReadPin = false;
								}
								listView.setEnabled(true);
							}
						});
			}
		}
	}

	@Override
	public void protocolDidReceiveProtocolVersion(int major, int minor,
			int bugfix) {
		Log.e(TAG, "major : " + major + ", minor : " + minor + ", bugfix : "
				+ bugfix);
		if (mProtocol != null) {
			int[] data = { 'B', 'L', 'E' };
			mProtocol.sendCustomData(data, 3);

			if (textRssi != null) {
				textRssi.postDelayed(new Runnable() {

					@Override
					public void run() {
						mProtocol.queryTotalPinCount();
					}
				}, 300);
			}

		}
	}

	@Override
	public void protocolDidReceiveTotalPinCount(int count) {
		Log.e(TAG, "protocolDidReceiveTotalPinCount count : " + count);
		if (mProtocol != null) {
			mProtocol.queryPinAll();
		}
	}

	@Override
	public void protocolDidReceivePinCapability(int pin, int value) {
		Log.e(TAG, "protocolDidReceivePinCapability pin : " + pin
				+ ", value : " + value);

		if (value == 0) {
			Log.e(TAG, " - Nothing");
		} else {
			if (pins == null) {
				return;
			}
			PinInfo pinInfo = new PinInfo();
			pinInfo.pin = pin;

			ArrayList<Integer> modes = new ArrayList<Integer>();

			modes.add(INPUT);

			if ((value & PIN_CAPABILITY_DIGITAL) == PIN_CAPABILITY_DIGITAL) {
				Log.e(TAG, " - DIGITAL (I/O)");
				modes.add(OUTPUT);
			}

			if ((value & PIN_CAPABILITY_ANALOG) == PIN_CAPABILITY_ANALOG) {
				Log.e(TAG, " - ANALOG");
				modes.add(ANALOG);
			}

			if ((value & PIN_CAPABILITY_PWM) == PIN_CAPABILITY_PWM) {
				Log.e(TAG, " - PWM");
				modes.add(PWM);
			}

			if ((value & PIN_CAPABILITY_SERVO) == PIN_CAPABILITY_SERVO) {
				Log.e(TAG, " - SERVO");
				modes.add(SERVO);
			}

			final int count = modes.size();
			pinInfo.modes = new int[count];
			for (int i = 0; i < count; i++) {
				pinInfo.modes[i] = modes.get(i);
			}

			pins.put(pin, pinInfo);
			modes.clear();

			updateData();
		}

	}

	@Override
	public void protocolDidReceivePinMode(int pin, int mode) {
		Log.e(TAG, "protocolDidReceivePinCapability pin : " + pin + ", mode : "
				+ mode);
		if (pins == null) {
			return;
		}

		PinInfo pinInfo = pins.get(pin);
		pinInfo.mode = mode;

		updateData();
	}

	@Override
	public void protocolDidReceivePinData(int pin, int mode, int value) {
		Log.e(TAG, "protocolDidReceivePinData pin : " + pin + ", mode : "
				+ mode + ", value : " + value);

		if (pins == null) {
			return;
		}
		//
		if (isFirstReadPin) {
			PinInfo pinInfo = new PinInfo();
			pinInfo.pin = pin;
			pinInfo.mode = mode;
			pinInfo.value = value;
			changeValues.put(pin + "", pinInfo);
		} else {
			PinInfo pinInfo = pins.get(pin);
			pinInfo.mode = mode;
			pinInfo.value = value;
			updateData();
		}
	}

	protected void updateData() {
		if (textRssi != null) {
			textRssi.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mAdapter == null) {
						mAdapter = new PinAdapter(getActivity(), pins);
						listView.setAdapter(mAdapter);
					} else {
						mAdapter.notifyDataSetChanged();
					}
				}
			}, 50);

		}
	}

	class PinAdapter extends BaseAdapter {

		SparseArray<PinInfo> data = null;
		Context context;
		LayoutInflater mInflater;

		public PinAdapter(Context context, SparseArray<PinInfo> data) {
			this.data = data;
			this.context = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (data != null) {
				return data.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {

			int key = data.keyAt(position);
			PinInfo pinInfo = data.get(key);

			ViewHolder holder = null;
			if (contentView == null) {
				contentView = mInflater.inflate(R.layout.standardview_item,
						null);
				holder = new ViewHolder();
				holder.pin = (TextView) contentView.findViewById(R.id.pin);
				holder.mode = (Button) contentView.findViewById(R.id.io_mode);
				holder.servo = (SeekBar) contentView
						.findViewById(R.id.progressbar);
				holder.analog = (TextView) contentView
						.findViewById(R.id.number);
				holder.digitol = (Switch) contentView
						.findViewById(R.id.switcher);
				contentView.setTag(holder);
			} else {
				holder = (ViewHolder) contentView.getTag();
			}

			String fix = "";
			if (pinInfo.pin < 10) {
				fix = "0";
			}
			holder.pin.setText("Pin:\t" + fix + pinInfo.pin);
			holder.mode.setText(getStateStr(pinInfo.mode));
			holder.mode.setTag(key);
			holder.mode.setOnClickListener(mModeClickListener);
			setModeAction(holder, pinInfo);
			return contentView;
		}

		private void setModeAction(ViewHolder holder, PinInfo pinInfo) {

			holder.analog.setVisibility(View.GONE);
			holder.analog.setTag(pinInfo.pin);

			holder.digitol.setVisibility(View.GONE);
			holder.digitol.setTag(pinInfo.pin);

			holder.servo.setVisibility(View.GONE);
			holder.servo.setTag(pinInfo.pin);

			switch (pinInfo.mode) {
			case IRBLProtocol.INPUT:
				holder.digitol.setVisibility(View.VISIBLE);
				holder.digitol.setEnabled(false);
				if (pinInfo.value == 1) {
					holder.digitol.setChecked(true);
				} else {
					holder.digitol.setChecked(false);
				}
				break;
			case IRBLProtocol.OUTPUT:
				holder.digitol.setVisibility(View.VISIBLE);
				holder.digitol.setEnabled(true);
				if (pinInfo.value == 1) {
					holder.digitol.setChecked(true);
				} else {
					holder.digitol.setChecked(false);
				}
				holder.digitol
						.setOnCheckedChangeListener(mDigitolValueChangeListener);
				break;
			case IRBLProtocol.ANALOG:
				holder.analog.setVisibility(View.VISIBLE);
				holder.analog.setText("" + pinInfo.value);
				break;
			case IRBLProtocol.SERVO:
			case IRBLProtocol.PWM:
				holder.servo.setVisibility(View.VISIBLE);
				holder.servo.setProgress(pinInfo.value);
				holder.servo.setOnSeekBarChangeListener(mServoChangeListener);
				break;
			}
		}

		OnCheckedChangeListener mDigitolValueChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton view, boolean value) {

				if (view.isEnabled()) {
					Byte key = (Byte) view.getTag();
					if (key != null) {
						if (mProtocol != null) {
							mProtocol.digitalWrite(key.byteValue(),
									value ? (byte) 1 : 0);
						}
					}
				}

			}
		};

		OnSeekBarChangeListener mServoChangeListener = new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onProgressChanged(SeekBar view, int value,
					boolean isChangeByUser) {

				if (isChangeByUser) {
					Byte key = (Byte) view.getTag();
					if (key != null) {
						if (mProtocol != null) {
							PinInfo pinInfo = data.get(key);
							if (pinInfo.mode == PWM) {
								mProtocol.analogWrite(key.byteValue(),
										(byte) value);
							} else {
								mProtocol.servoWrite(key.byteValue(),
										(byte) value);
							}
						}
					}
				}

			}
		};
		OnClickListener mModeClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {

				Integer index = (Integer) view.getTag();
				if (index != null) {
					int key = index;
					PinInfo pinInfo = data.get(key);
					showModeSelect(pinInfo);
				}
			}
		};

		class ViewHolder {
			TextView pin;
			Button mode;
			Switch digitol;
			SeekBar servo;
			TextView analog;
		}
	}

	RelativeLayout select_window;

	protected void showModeSelect(PinInfo pinInfo) {
		if (getActivity() != null) {
			LinearLayout modes_area = null;
			final int modes_area_id = 0x123ff;
			if (select_window == null) {
				select_window = new RelativeLayout(getActivity());
				select_window.setBackgroundColor(0x4f000000);
				select_window.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						select_window.setVisibility(View.INVISIBLE);

					}
				});

				modes_area = new LinearLayout(getActivity());
				modes_area.setId(modes_area_id);
				modes_area.setBackgroundColor(Color.WHITE);
				modes_area.setOrientation(LinearLayout.VERTICAL);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
						RelativeLayout.TRUE);
				select_window.addView(modes_area, params);

				getActivity().addContentView(
						select_window,
						new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
			} else {

				modes_area = (LinearLayout) select_window
						.findViewById(modes_area_id);
			}

			select_window.setVisibility(View.INVISIBLE);
			modes_area.removeAllViews();

			for (int b : pinInfo.modes) {
				String text = getStateStr(b);
				if (text != null) {
					final int btn_mode = b;
					final int btn_pin = pinInfo.pin;
					Button btn = createModeButton(text);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {

							if (mProtocol != null) {
								mProtocol.setPinMode(btn_pin, btn_mode);
							}

							select_window.setVisibility(View.INVISIBLE);

						}
					});
					modes_area.addView(btn, params);
				}
			}

			AlphaAnimation animation = new AlphaAnimation(0, 1);
			animation.setDuration(350);
			animation.setInterpolator(new DecelerateInterpolator());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {

					select_window.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {

				}

				@Override
				public void onAnimationEnd(Animation arg0) {

				}
			});
			select_window.startAnimation(animation);
		}
	}

	protected Button createModeButton(String text) {
		Button btn = new Button(getActivity());

		btn.setPadding(20, 5, 20, 5);

		btn.setText(text);

		return btn;
	}

	protected String getStateStr(int mode) {
		switch (mode) {
		case IRBLProtocol.INPUT:
			return STR_INPUT;
		case IRBLProtocol.OUTPUT:
			return STR_OUTPUT;
		case IRBLProtocol.ANALOG:
			return STR_ANALOG;
		case IRBLProtocol.SERVO:
			return STR_SERVO;
		case IRBLProtocol.PWM:
			return STR_PWM;
		}
		return null;
	}
}
