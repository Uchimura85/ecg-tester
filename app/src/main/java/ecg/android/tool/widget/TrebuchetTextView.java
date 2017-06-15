
package ecg.android.tool.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

public class TrebuchetTextView extends TextView {

	public TrebuchetTextView(Context context) {
		super(context);

		init();
	}

	public TrebuchetTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public TrebuchetTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private void init() {
		if (!isInEditMode()) {
			final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(R.string.normal_font_path));
			setTypeface(typeface);
		}
	}
}
