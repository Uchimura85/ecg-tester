package ecg.android.tool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ecg.android.tool.ecg.MenuActivity;
import no.nordicsemi.android.nrftoolbox.R;
import ecg.android.tool.ecg.ECGActivity;

public class SplashscreenActivity extends Activity {
	/** Splash screen duration time in milliseconds */
	private static final int DELAY = 3000;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);

		// Jump to SensorsActivity after DELAY milliseconds 
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				final Intent newIntent = new Intent(SplashscreenActivity.this,MenuActivity.class);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

				startActivity(newIntent);
				finish();
			}
		}, DELAY);
	}

	@Override
	public void onBackPressed() {
		// do nothing. Protect from exiting the application when splash screen is shown
	}
}
