package com.redbear.protocol;

public interface IRBLProtocol {

	public void protocolDidReceiveCustomData(int[] data, int length);

	public void protocolDidReceiveProtocolVersion(int major, int minor,
			int bugfix);

	public void protocolDidReceiveTotalPinCount(int count);

	public void protocolDidReceivePinCapability(int pin, int value);

	public void protocolDidReceivePinMode(int pin, int mode); /*
															 * mode:
															 * I/O/Analog/PWM
															 * /Servo
															 */

	public void protocolDidReceivePinData(int pin, int mode, int value);

	public final static char MESSAGE_TYPE_CUSTOM_DATA = 'Z';
	public final static char MESSAGE_TYPE_PROTOCOL_VERSION = 'V';
	public final static char MESSAGE_TYPE_PIN_COUNT = 'C';
	public final static char MESSAGE_TYPE_PIN_CAPABILITY = 'P';
	public final static char MESSAGE_TYPE_READ_PIN_MODE = 'M';
	public final static char MESSAGE_TYPE_READ_PIN_DATA = 'G';
	public final static char COMMAND_ANALOG_WRITE = 'N';

	// Pin modes.
	// except from UNAVAILABLE taken from Firmata.h
	public final static int UNAVAILABLE = 0xFF;
	public final static int INPUT = 0x00;
	public final static int OUTPUT = 0x01;
	public final static int ANALOG = 0x02;
	public final static int PWM = 0x03;
	public final static int SERVO = 0x04;

	public final static String STR_INPUT = "INPUT";
	public final static String STR_OUTPUT = "OUTPUT";
	public final static String STR_PWM = "PWM";
	public final static String STR_SERVO = "SERVO";
	public final static String STR_ANALOG = "ANALOG";

	// Pin types
	public final static byte DIGITAL = OUTPUT; // same as OUTPUT below
	// ANALOG is already defined above

	public final static int HIGH = 0x01;
	public final static int LOW = 0x00;

	public final static int PIN_CAPABILITY_NONE = 0x00;
	public final static int PIN_CAPABILITY_DIGITAL = 0x01;
	public final static int PIN_CAPABILITY_ANALOG = 0x02;
	public final static int PIN_CAPABILITY_PWM = 0x04;
	public final static int PIN_CAPABILITY_SERVO = 0x08;
	public final static int PIN_CAPABILITY_I2C = 0x7f;

	public final static int PIN_ERROR_INVALID_PIN = 0x01;
	public final static int PIN_ERROR_INVALID_MODE = 0x02;
}
