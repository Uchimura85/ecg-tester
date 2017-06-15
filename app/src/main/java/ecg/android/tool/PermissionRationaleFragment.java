
package ecg.android.tool;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import no.nordicsemi.android.nrftoolbox.R;

public class PermissionRationaleFragment extends DialogFragment {
	private static final String ARG_PERMISSION = "ARG_PERMISSION";
	private static final String ARG_TEXT = "ARG_TEXT";

	private PermissionDialogListener mListener;

	public interface PermissionDialogListener {
		public void onRequestPermission(final String permission);
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		if (context instanceof PermissionDialogListener) {
			mListener = (PermissionDialogListener) context;
		} else {
			throw new IllegalArgumentException("The parent activity must impelemnt PermissionDialogListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public static PermissionRationaleFragment getInstance(final int aboutResId, final String permission) {
		final PermissionRationaleFragment fragment = new PermissionRationaleFragment();

		final Bundle args = new Bundle();
		args.putInt(ARG_TEXT, aboutResId);
		args.putString(ARG_PERMISSION, permission);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
    @NonNull
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final StringBuilder text = new StringBuilder(getString(args.getInt(ARG_TEXT)));
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.permission_title).setMessage(text)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mListener.onRequestPermission(args.getString(ARG_PERMISSION));
					}
				}).create();
	}
}
