

package ecg.android.tool.dfu.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.R;

public class AboutDfuPreference extends Preference {

	public AboutDfuPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AboutDfuPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onClick() {
		final Context context = getContext();
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://infocenter.nordicsemi.com/index.jsp?topic=%2Fcom.nordic.infocenter.sdk52.v0.9.1%2Fexamples_ble_dfu.html&cp=4_0_0_4_2"));
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// is browser installed?
		if (intent.resolveActivity(context.getPackageManager()) != null)
			context.startActivity(intent);
		else {
			Toast.makeText(getContext(), R.string.no_application, Toast.LENGTH_LONG).show();
		}
	}
}
