package com.redbear.protocol;

import android.util.Log;

import com.redbear.RedBearService.RedBearService;

/**
 * 
 * 
 * RBLProtocol
 * 
 * @author James
 * 
 */
public class RBLProtocol {

	final String TAG = "RBLProtocol";

	IRBLProtocol mIrblProtocol;

	RedBearService mRedBearService;

	String address;

	public RBLProtocol(String address) {
		this.address = address;
	}

	public void setIRBLProtocol(IRBLProtocol mIrblProtocol) {
		this.mIrblProtocol = mIrblProtocol;
	}

	public void setmIRedBearService(RedBearService mRedBearService) {
		this.mRedBearService = mRedBearService;
	}

	public void parseData(int[] data) {

		int i = 0;
		final int length = data.length;
		while (i < length) {
			int type = data[i++];
			Log.e(TAG, "type: " + type);
			switch (type) {
			case IRBLProtocol.MESSAGE_TYPE_PROTOCOL_VERSION: // report protocol
																// version
				if (mIrblProtocol != null) {
					mIrblProtocol.protocolDidReceiveProtocolVersion(data[i++],
							data[i++], data[i++]);
				}
				break;

			case IRBLProtocol.MESSAGE_TYPE_PIN_COUNT: // report total pin count
														// of the board
				if (mIrblProtocol != null) {
					mIrblProtocol.protocolDidReceiveTotalPinCount(data[i++]);
				}
				break;

			case IRBLProtocol.MESSAGE_TYPE_PIN_CAPABILITY: // report pin
															// capability
				if (mIrblProtocol != null) {
					mIrblProtocol.protocolDidReceivePinCapability(data[i++],
							data[i++]);
				}
				break;

			case IRBLProtocol.MESSAGE_TYPE_CUSTOM_DATA: // custom data
				if (mIrblProtocol != null) {
					int[] result = new int[length - 1];
					for (int index = i; index < length; index++) {
						result[index - 1] = data[index];
					}
					mIrblProtocol.protocolDidReceiveCustomData(result,
							length - 1);
				}
				i = length;
				break;

			case IRBLProtocol.MESSAGE_TYPE_READ_PIN_MODE: // report pin mode
				if (mIrblProtocol != null) {
					mIrblProtocol.protocolDidReceivePinMode(data[i++],
							data[i++]);
				}
				break;

			case IRBLProtocol.MESSAGE_TYPE_READ_PIN_DATA: // report pin data
				if (mIrblProtocol != null) {
					if (data[3] > 127 || data[3] < 0) {
						Log.e(TAG, "data[4]: " + data[4]);
					}
					mIrblProtocol.protocolDidReceivePinData(data[i++],
							data[i++], data[i++]);
				}
				break;
			}
		}
	}

	protected void write(char[] data) {
		if (mRedBearService != null) {
			mRedBearService.writeValue(address, data);
		}
	}

	public void setPinMode(int pin, int mode) {
		Log.e(TAG, "RBLPRotocol: setPinMode");

		char buf[] = { 'S', (char) pin, (char) mode };

		write(buf);
		// wirte value
	}

	public void digitalRead(int pin) {
		Log.e(TAG, "RBLProtocol: digitalRead");

		char buf[] = { 'G', (char) pin };

		write(buf);
		// wirte value
	}

	public void digitalWrite(int pin, int value) {
		Log.e(TAG, "RBLProtocol: digitalWrite");

		char buf[] = { 'T', (char) pin, (char) value };

		write(buf);
		// wirte value

	}

	public void queryPinAll() {
		Log.e(TAG, "RBLProtocol: queryPinAll");
		char buf[] = { 'A', 0x0d, 0x0a };
		write(buf);
		// wirte value

	}

	public void queryProtocolVersion() {
		Log.e(TAG, "RBLProtocol: queryProtocolVersion");

		char buf[] = { 'V' };
		write(buf);
		// wirte value
	}

	public void queryTotalPinCount() {
		Log.e(TAG, "RBLProtocol: queryTotalPinCount");
		char buf[] = { 'C' };
		write(buf);
		// wirte value
	}

	public void queryPinCapability(int pin) {
		Log.e(TAG, "RBLProtocol: queryPinCapability");
		char buf[] = { 'P', (char) pin };
		write(buf);

		// wirte value
	}

	public void queryPinMode(int pin) {
		Log.e(TAG, "RBLPRotocol: queryPinMode");
		char buf[] = { 'M', (char) pin };
		write(buf);
		// wirte value
	}

	public void analogWrite(int pin, int value) {
		Log.e(TAG, "RBLPRotocol: analogWrite value: " + value);
		char buf[] = { 'N', (char) pin, (char) value };
		write(buf);
		// write data
	}

	public void servoWrite(int pin, int value) {
		Log.e(TAG, "RBLPRotocol: servoWrite value: " + value);
		char buf[] = { 'O', (char) pin, (char) value };
		write(buf);
		// write data
	}

	public void sendCustomData(int[] data, int length) {
		char[] buf = new char[1 + 1 + length];
		buf[0] = 'Z';
		buf[1] = (char) length;
		int j = 0;
		for (int i = 2; i < length; i++) {
			buf[i] = (char) data[j++];
		}
		write(buf);
		// write data
	}

}
