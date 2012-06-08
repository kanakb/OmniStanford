package mobisocial.omnistanford;

import java.util.Set;

import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Tag extends FrameLayout {
	private String mTagStr;
	private LinearLayout mTagContainer;
	private TagTextView mTagTextView;
	private ImageView mCrossImage;

	public Tag(Context context, String tag, boolean showCross) {
		super(context);
		mTagStr = tag;
		init(context, tag, showCross);
	}

	public Tag(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTagStr = "";
		init(context, "", false);
	}
	
	public Tag(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTagStr = "";
		init(context, "", false);
	}
	
	void init(Context context, String tag, boolean showCross) {
		mTagTextView = new TagTextView(context, tag, showCross);
		
		if(showCross) {
			mCrossImage = new ImageView(getContext());
			// Dismiss the dialog when user click on the 'x'
			mCrossImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Set<String> tags = ScheduleActivity.getTagSet(v.getContext());
					tags.remove(mTagStr);
					ScheduleActivity.saveTagSet(v.getContext(), tags);
					
					// TODO: remove child from parent causes warning, see logcat
					ViewGroup parent = (ViewGroup) v.getParent().getParent();
					parent.removeView((View) v.getParent());
				}
			});
			Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.close);
			mCrossImage.setImageDrawable(crossDrawable);
			
			mTagContainer = new LinearLayout(getContext());
			mTagContainer.addView(mTagTextView);
			int padding = mCrossImage.getDrawable().getIntrinsicWidth() / 2;
			mTagContainer.setPadding(padding, padding, 0, 0);
			
			addView(mTagContainer);
			addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		} else {
			addView(mTagTextView);
		}
	}
	
}

class TagTextView extends TextView {
	public static final int[] COLORS = {
		android.R.color.holo_blue_dark,
		android.R.color.holo_green_dark,
		android.R.color.holo_orange_dark,
		android.R.color.holo_purple,
		android.R.color.holo_red_dark
	};
	private String mTag;

	public TagTextView(Context context, String tag, boolean showCross) {
		super(context);
		mTag = tag;
		init(showCross);
	}

	public TagTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(false);
	}
	
	public TagTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(false);
	}
	
	private void init(boolean showCross) {
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
		if(showCross) {
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
}
