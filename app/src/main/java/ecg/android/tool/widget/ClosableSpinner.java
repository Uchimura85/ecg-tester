package ecg.android.tool.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class ClosableSpinner extends Spinner {
	public ClosableSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void close() {
		super.onDetachedFromWindow();
	}
}
