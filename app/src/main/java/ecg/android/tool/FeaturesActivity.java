
package ecg.android.tool;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.R;
import ecg.android.tool.adapter.AppAdapter;
import ecg.android.tool.ecg.ECGActivity;

public class FeaturesActivity extends AppCompatActivity {
	private static final String NRF_CONNECT_CATEGORY = "no.nordicsemi.android.nrftoolbox.LAUNCHER";
	private static final String UTILS_CATEGORY = "no.nordicsemi.android.nrftoolbox.UTILS";
	private static final String NRF_CONNECT_PACKAGE = "no.nordicsemi.android.mcp";
	private static final String NRF_CONNECT_CLASS = NRF_CONNECT_PACKAGE + ".DeviceListActivity";
	private static final String NRF_CONNECT_MARKET_URI = "market://details?id=no.nordicsemi.android.mcp";

	// Extras that can be passed from NFC (see SplashscreenActivity)
	public static final String EXTRA_APP = "application/vnd.no.nordicsemi.type.app";
	public static final String EXTRA_ADDRESS = "application/vnd.no.nordicsemi.type.address";

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_features);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

		// ensure that Bluetooth exists
		if (!ensureBLEExists())
			finish();

		final DrawerLayout drawer = mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Set the drawer toggle as the DrawerListener
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(final View drawerView, final float slideOffset) {
                // Disable the Hamburger icon animation
                super.onDrawerSlide(drawerView, 0);
            }
        };
		drawer.addDrawerListener(mDrawerToggle);

		//setupPluginsInDrawer((ViewGroup) drawer.findViewById(R.id.plugin_container));

		final GridView grid = (GridView) findViewById(R.id.grid);
		grid.setAdapter(new AppAdapter(this));
		grid.setEmptyView(findViewById(android.R.id.empty));

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		final Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_APP) && intent.hasExtra(EXTRA_ADDRESS)) {
			final String app = intent.getStringExtra(EXTRA_APP);
			switch (app) {
				case "HRM":
					final Intent newIntent = new Intent(this, ECGActivity.class);
					newIntent.putExtra(EXTRA_ADDRESS, intent.getByteArrayExtra(EXTRA_ADDRESS));
					newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(newIntent);
					break;
				default:
					// other are not supported yet
					break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_about:
			final AppHelpFragment fragment = AppHelpFragment.getInstance(R.string.about_text, true);
			fragment.show(getSupportFragmentManager(), null);
			break;
		}
		return true;
	}


	private boolean ensureBLEExists() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
}
