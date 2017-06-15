
package ecg.android.tool.ecg;

import android.bluetooth.BluetoothDevice;

import ecg.android.tool.profile.BleManagerCallbacks;

public interface ECGManagerCallbacks extends BleManagerCallbacks {

	/**
	 * Called when the sensor position information has been obtained from the sensor
	 *
	 * @param device  the bluetooth device from which the value was obtained
	 * @param position
	 *            the sensor position
	 */
	void onHRSensorPositionFound(final BluetoothDevice device, String position);

	/**
	 * Called when new Heart Rate value has been obtained from the sensor
	 *
	 * @param device  the bluetooth device from which the value was obtained
	 * @param value
	 *            the new value
	 */
	void onHRValueReceived(final BluetoothDevice device, int value,boolean isSensorDeteted);
	void onBatteryReceived(int value);

	void onAccDataReceived(AccData data,boolean isSensorDeteted);
}
