
package ecg.android.tool.dfu.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

/**
 * This adapter displays some file browser applications that can be used to select HEX file. It is used when there is no such app already installed on the device. The hardcoded apps and Google Play
 * URLs are specified in res/values/strings_dfu.xml.
 */
public class FileBrowserAppsAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;
	private final Resources mResources;

	public FileBrowserAppsAdapter(final Context context) {
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
	}

	@Override
	public int getCount() {
		return mResources.getStringArray(R.array.dfu_app_file_browser).length;
	}

	@Override
	public Object getItem(int position) {
		return mResources.getStringArray(R.array.dfu_app_file_browser_action)[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.app_file_browser_item, parent, false);
		}

		final TextView item = (TextView) view;
		item.setText(mResources.getStringArray(R.array.dfu_app_file_browser)[position]);
		item.getCompoundDrawablesRelative()[0].setLevel(position);
		return view;
	}
}
