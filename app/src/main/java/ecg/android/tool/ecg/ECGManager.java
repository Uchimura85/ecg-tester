
package ecg.android.tool.ecg;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.R;
import ecg.android.tool.parser.BodySensorLocationParser;
import ecg.android.tool.parser.HeartRateMeasurementParser;
import ecg.android.tool.profile.BleManager;

/**
 * HRSManager class performs BluetoothGatt operations for connection, service discovery, enabling notification and reading characteristics. All operations required to connect to device with BLE HR
 * Service and reading heart rate values are performed here. HRSActivity implements HRSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class ECGManager extends BleManager<ECGManagerCallbacks> {
    public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    private static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mHRCharacteristic, mHRLocationCharacteristic;

    private static ECGManager managerInstance = null;

    /**
     * singleton implementation of HRSManager class
     */
    public static synchronized ECGManager getInstance(final Context context) {
        if (managerInstance == null) {
            managerInstance = new ECGManager(context);
        }
        return managerInstance;
    }

    public ECGManager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Deque<Request> initGatt(final BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            if (mHRLocationCharacteristic != null)
                requests.add(Request.newReadRequest(mHRLocationCharacteristic));
            requests.add(Request.newEnableNotificationsRequest(mHRCharacteristic));
            return requests;
        }

        @Override
        protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
            if (service != null) {
                mHRCharacteristic = service.getCharacteristic(HR_CHARACTERISTIC_UUID);
            }
            return mHRCharacteristic != null;
        }

        @Override
        protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
            if (service != null) {
                mHRLocationCharacteristic = service.getCharacteristic(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
            }
            return mHRLocationCharacteristic != null;
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Logger.a(mLogSession, "\"" + BodySensorLocationParser.parse(characteristic) + "\" received");

            final String sensorPosition = getBodySensorPosition(characteristic.getValue()[0]);
            //This will send callback to HRSActivity when HR sensor position on body is found in HR device
            mCallbacks.onHRSensorPositionFound(gatt.getDevice(), sensorPosition);
        }

        @Override
        protected void onDeviceDisconnected() {
            mHRLocationCharacteristic = null;
            mHRCharacteristic = null;
        }

        long beforeTime = -1;

        @Override
        public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Logger.a(mLogSession, "\"" + HeartRateMeasurementParser.parse(characteristic) + "\" received");
            if (beforeTime == -1) beforeTime = System.currentTimeMillis();

            int hrValue;
            boolean isSensorDetected;
            isSensorDetected = isSensorDetected(characteristic.getValue()[0]);

            int hrsCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            Log.d("packetTime", "" + (System.currentTimeMillis() - beforeTime)+", ----------------- "+hrsCount);
            beforeTime = System.currentTimeMillis();
            if (hrsCount == 0) return;
            Log.d("hrvcount", "" + hrsCount);
            for (int i = 0; i < hrsCount; i++) {
                hrValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + i * 2);
                //This will send callback to HRSActivity when new HR value is received from HR device
                mCallbacks.onHRValueReceived(gatt.getDevice(), hrValue, isSensorDetected);
            }
            int batteryAmount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2);
            Log.d("hrt", "" + hrsCount + ":" + batteryAmount);
            mCallbacks.onBatteryReceived(batteryAmount);
            int accX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 2);
            int accY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 4);
            int accZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 6);
            Log.d("accel", "" + accX + ":" + accY + ":" + accZ);
            mCallbacks.onAccDataReceived(new AccData(accX, accY, accZ), isSensorDetected);

        }
    };

    /**
     * This method will decode and return Heart rate sensor position on body
     */
    private String getBodySensorPosition(final byte bodySensorPositionValue) {
        final String[] locations = getContext().getResources().getStringArray(R.array.hrs_locations);
        if (bodySensorPositionValue > locations.length)
            return getContext().getString(R.string.hrs_location_other);
        return locations[bodySensorPositionValue];
    }

    /**
     * This method will check if Heart rate value is in 8 bits or 16 bits
     */
    private boolean isHeartRateInUINT16(final byte value) {
        return ((value & 0x01) != 0);
    }

    private boolean isHeartRateInArray(final byte value) {
        return ((value & 0x01) != 0);
    }

    private boolean isSensorDetected(final byte value) {
        return ((value & 0x01) != 0);
    }
}
