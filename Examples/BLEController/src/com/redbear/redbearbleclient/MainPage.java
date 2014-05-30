package com.redbear.redbearbleclient;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.redbear.RedBearService.IRedBearServiceEventListener;
import com.redbear.RedBearService.RedBearService;
import com.redbear.redbearbleclient.view.PullRefreshListView;
import com.redbear.redbearbleclient.view.PullRefreshListView.OnRefreshListener;
import com.redbear.redbearbleclient.view.listviewanimation.ArrayAdapter;

/**
 * 
 * 
 * MainPage
 * 
 * @author James
 * 
 */
public class MainPage extends Activity {

	final static String TAG = "MainPage";
	public static final int REQUEST_CODE = 1;
	public static final int REQ_CODE = 9;
	public static MainPage instance;
	private TextView version_name;
	public MainPageFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		View actionbar_layout = LayoutInflater.from(this).inflate(
				R.layout.action_bar, null);
		version_name = (TextView) actionbar_layout.findViewById(R.id.version);
		version_name.setText("v"
				+ getResources().getString(R.string.version_name));

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(actionbar_layout);

		setContentView(R.layout.activity_main_page);

		String osVersion = Build.VERSION.RELEASE;
		if ((osVersion.charAt(0)) == '4' && (osVersion.charAt(1) == '.')
				&& (osVersion.charAt(2) >= '3')) {

		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainPage.this);
			dialog.setTitle("Error");
			dialog.setMessage("Support Android 4.3 or above only.");
			dialog.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					});
			dialog.show();

			return;
		}

		instance = this;

		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (!ba.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_CODE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		fragment = new MainPageFragment();
		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.animator.fade_in,
						android.R.animator.fade_out)
				.replace(R.id.container, fragment).commit();
	}

	public RedBearService getReadBearService() {
		return fragment.getReadBearService();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_CODE
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		} else if (requestCode == REQ_CODE
				&& resultCode == StandardViewFragmentForPinsEx.RST_CODE) {
			fragment.mBearService.disconnectDevice(fragment.mDevice.address);
			fragment.mBearService = null;
			unbindService(fragment.conn);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_main_page, menu);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// case R.id.menu_settings:
		// Intent mSetting = new Intent(this, SettingPage.class);
		// startActivity(mSetting);
		// overridePendingTransition(R.anim.slide_in_from_right,
		// R.anim.slide_out_to_left);
		// break;
		case android.R.id.home:
			close();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void close() {
		finish();
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class MainPageFragment extends Fragment {

		MyListAdapter adapter;

		PullRefreshListView listView;

		View loading;

		View resultVIew;

		ArrayList<Device> mArrayList = new ArrayList<Device>();

		private boolean loadingFlag = false;

		Device mDevice = null;

		public RedBearService mBearService;

		public MainPageFragment() {

		}

		public RedBearService getReadBearService() {
			return mBearService;
		}

		ServiceConnection conn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mBearService = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBearService = ((RedBearService.LocalBinder) service)
						.getService();

				if (mBearService != null) {
					loading.setVisibility(View.VISIBLE);
					loadingFlag = true;
					if (mArrayList != null) {
						mArrayList.clear();
					}
					if (adapter != null) {
						adapter.clear();
					}
					mBearService.setListener(mIScanDeviceListener);
					mBearService.startScanDevice();

					listView.postDelayed(new Runnable() {

						@Override
						public void run() {
							listView.onRefreshComplete();
							loading.setVisibility(View.GONE);
							loadingFlag = false;

							if (mBearService != null) {
								mBearService.stopScanDevice();
								addAllToList();
							}

							addAllToList();

						}
					}, 3000);
				}
			}
		};

		IRedBearServiceEventListener mIScanDeviceListener = new IRedBearServiceEventListener() {

			@Override
			public void onDeviceFound(String deviceAddress, String name,
					int rssi, int bondState, byte[] scanRecord,
					ParcelUuid[] uuids) {

				Log.e("onDeviceFound", "address : " + deviceAddress);

				Device mDevice = new Device();

				mDevice.address = deviceAddress;

				mDevice.name = name;

				mDevice.rssi = rssi;

				mDevice.bondState = bondState;

				mDevice.scanReadData = scanRecord;

				mDevice.uuids = uuids;

				addDevice(mDevice);
			}

			@Override
			public void onDeviceRssiUpdate(String deviceAddress, int rssi,
					int state) {

				Log.e(TAG, "deviceAddress : " + deviceAddress + " , rssi : "
						+ rssi);
			}

			@Override
			public void onDeviceConnectStateChange(String deviceAddress,
					int state) {
			}

			@Override
			public void onDeviceReadValue(int[] value) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDeviceCharacteristicFound() {
				Intent intent = new Intent(getActivity(), StandardView.class);
				intent.putExtra("Device", mDevice);
				getActivity().startActivityForResult(intent, 9);
				getActivity().overridePendingTransition(
						R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			}
		};

		OnItemClickListener mItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mArrayList != null && !mArrayList.isEmpty() && position > 0) {
					mDevice = mArrayList.get(position - 1);

					mBearService.connectDevice(mDevice.address, false);
				}

			}
		};

		class RefreshTask extends AsyncTask<Void, Void, Void> {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				adapter.clear();
				mArrayList.clear();
				if (mBearService != null) {
					mBearService.stopScanDevice();
					mBearService.startScanDevice();
				}

			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (mBearService != null) {
					mBearService.stopScanDevice();
				}

				listView.onRefreshComplete();
				addAllToList();
			}
		}

		@Override
		public void onDestroy() {
			// if (mBearService != null) {
			// try {
			// mBearService.stopScanDevice();
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			// }
			try {
				getActivity().unbindService(conn);
			} catch (Exception e) {
				// todo
			}
			super.onDestroy();
		}

		RefreshTask refreshTask = null;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.main_page_layout, null);

			listView = (PullRefreshListView) view
					.findViewById(R.id.refresh_listview);

			listView.setDivider(null);

			listView.setOnItemClickListener(mItemClickListener);

			loading = view.findViewById(R.id.pageloading);

			resultVIew = view.findViewById(R.id.scan_result);

			listView.setonRefreshListener(new OnRefreshListener() {

				public void onRefresh() {
					mArrayList.clear();
					adapter.clear();

					if (loadingFlag == false) {
						resultVIew.setVisibility(View.GONE);
						if (refreshTask != null) {
							refreshTask.cancel(true);
						}
						refreshTask = new RefreshTask();
						refreshTask.execute();
					}
				}
			});
			
			Intent service = new Intent(
					"com.redbear.RedBearService.RedBearService");
			getActivity().bindService(service, conn, Context.BIND_AUTO_CREATE);

			if (mBearService != null) {
				// mBearService.startScanDevice();
				mBearService.setListener(mIScanDeviceListener);
			}

			return view;
		}

		void addDevice(final Device mDevice) {
			for (Device mTemp : mArrayList) {
				if (mTemp.address.equals(mDevice.address)) {
					mTemp.rssi = mDevice.rssi;
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
						}
					});
					return;
				}
			}

			mArrayList.add(mDevice);
		}

		void addAllToList() {
			if (getActivity() != null) {
				adapter = new MyListAdapter(getActivity(), mArrayList);

				// final SuperAdapter swingRightInAnimationAdapter = new
				// SuperAdapter(
				// adapter);
				//
				// swingRightInAnimationAdapter.setAbsListView(listView);
				//
				// listView.setAdapter(swingRightInAnimationAdapter);
				listView.setAdapter(adapter);
			}

			if (mArrayList.size() == 0) {
				resultVIew.setVisibility(View.VISIBLE);
			} else {
				resultVIew.setVisibility(View.GONE);
			}
		}

		private class MyListAdapter extends ArrayAdapter<Device> {

			private Context mContext;

			private LayoutInflater mInflater;

			ArrayList<Device> items;

			public MyListAdapter(Context context, ArrayList<Device> items) {
				super(items);
				mContext = context;
				mInflater = LayoutInflater.from(mContext);
				this.items = items;
			}

			public View getView(int position, View convertView, ViewGroup parent) {

				Device mDevice = items.get(position);

				View v = mInflater.inflate(R.layout.main_page_listview_item,
						null);

				TextView mName = (TextView) v.findViewById(R.id.big_name);

				mName.setText(mDevice.name);

				TextView mLocalName = (TextView) v
						.findViewById(R.id.local_name);

				mLocalName.setText("Local Name: " + mDevice.name);

				TextView mAddress = (TextView) v.findViewById(R.id.name);

				mAddress.setText("Address: " + mDevice.address);

				TextView mRssi = (TextView) v.findViewById(R.id.service);

				mRssi.setText("Rssi: " + mDevice.rssi);

				TextView mUddi = (TextView) v.findViewById(R.id.uuid);

				if (mDevice.uuids == null) {
					mUddi.setText("uuid: null");
				} else {
					mUddi.setText(mDevice.uuids.toString() + "");
				}

				return v;

			}
		}
	}

	public static class Device implements Serializable {
		private static final long serialVersionUID = -5961124837206924220L;

		public String address;

		public String name;

		public int bondState;

		public int rssi;

		public byte[] scanReadData;

		public ParcelUuid[] uuids;
	}

}
