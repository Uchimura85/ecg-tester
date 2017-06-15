
package ecg.android.tool.parser;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Calendar;
import java.util.Locale;

public class DateTimeParser {
	/**
	 * Parses the date and time info.
	 * 
	 * @param characteristic
	 * @return time in human readable format
	 */
	public static String parse(final BluetoothGattCharacteristic characteristic) {
		return parse(characteristic, 0);
	}

	/**
	 * Parses the date and time info. This data has 7 bytes
	 * 
	 * @param characteristic
	 * @param offset
	 *            offset to start reading the time
	 * @return time in human readable format
	 */
	/* package */static String parse(final BluetoothGattCharacteristic characteristic, final int offset) {
		final int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
		final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
		final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
		final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
		final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
		final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hours, minutes, seconds);

		return String.format(Locale.US, "%1$te %1$tb %1$tY, %1$tH:%1$tM:%1$tS", calendar);
	}
}
