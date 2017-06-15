
package ecg.android.tool.parser;

import android.bluetooth.BluetoothGattCharacteristic;

public class BodySensorLocationParser {

	public static String parse(final BluetoothGattCharacteristic characteristic) {
		final int value = unsignedByteToInt(characteristic.getValue()[0]);

		switch (value) {
		case 6:
			return "Foot";
		case 5:
			return "Ear Lobe";
		case 4:
			return "Hand";
		case 3:
			return "Finger";
		case 2:
			return "Wrist";
		case 1:
			return "Chest";
		case 0:
		default:
			return "Other";
		}
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	private static int unsignedByteToInt(byte b) {
		return b & 0xFF;
	}
}
