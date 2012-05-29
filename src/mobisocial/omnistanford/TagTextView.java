package mobisocial.omnistanford;

import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class TagTextView extends TextView {
	public static final int[] COLORS = {
		android.R.color.holo_blue_dark,
		android.R.color.holo_green_dark,
		android.R.color.holo_orange_dark,
		android.R.color.holo_purple,
		android.R.color.holo_red_dark
	};
	private String mTag;

	public TagTextView(Context context, String tag) {
		super(context);
		mTag = tag;
		init();
	}

	public TagTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public TagTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		Resources res = getResources();
		setText(mTag);
		PaintDrawable shape = new PaintDrawable(res.getColor(COLORS[Math.abs(mTag.hashCode())%COLORS.length]));
		shape.setCornerRadius(6.0f);
		shape.setAlpha(255);
		setBackgroundDrawable(shape);
		setWidth(getResources().getDisplayMetrics().widthPixels/5);
		setHeight(getResources().getDisplayMetrics().heightPixels/10);
		setGravity(Gravity.CENTER);
		setTextColor(res.getColor(android.R.color.white));
		setPadding(8, 2, 8, 5);
		setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipData dragData = ClipData.newPlainText("activity", mTag);
				v.startDrag(dragData,  // the data to be dragged
						new DragShadowBuilder(v),  // the drag shadow builder
						null,      // no need to use local data
						0          // flags (not currently used, set to 0)
				);
				return true;
			}
		});
	}
}
