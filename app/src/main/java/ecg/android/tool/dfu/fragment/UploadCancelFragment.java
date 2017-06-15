
package ecg.android.tool.dfu.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import no.nordicsemi.android.nrftoolbox.R;
import ecg.android.tool.dfu.DfuService;

/**
 * When cancel button is pressed during uploading this fragment shows uploading cancel dialog
 */
public class UploadCancelFragment extends DialogFragment {
	private static final String TAG = "UploadCancelFragment";

	private CancelFragmentListener mListener;

	public interface CancelFragmentListener {
		public void onCancelUpload();
	}

	public static UploadCancelFragment getInstance() {
		return new UploadCancelFragment();
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (CancelFragmentListener) activity;
		} catch (final ClassCastException e) {
			Log.d(TAG, "The parent Activity must implement CancelFragmentListener interface");
		}
	}

	@NonNull
    @Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.dfu_confirmation_dialog_title).setMessage(R.string.dfu_upload_dialog_cancel_message).setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int whichButton) {
						final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
						final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
						pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
						manager.sendBroadcast(pauseAction);

						mListener.onCancelUpload();
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.cancel();
					}
				}).create();
	}

	@Override
	public void onCancel(final DialogInterface dialog) {
		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
		final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
		pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_RESUME);
		manager.sendBroadcast(pauseAction);
	}
}
