package ecg.android.tool.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;

public class TrebuchetBoldTextView extends TextView {

	public TrebuchetBoldTextView(Context context) {
		super(context);

		init();
	}

	public TrebuchetBoldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public TrebuchetBoldTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private void init() {
		if (!isInEditMode()) {
			final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(R.string.font_path));
			setTypeface(typeface);
		}
	}
}
