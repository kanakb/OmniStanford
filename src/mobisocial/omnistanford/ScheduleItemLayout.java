package mobisocial.omnistanford;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class ScheduleItemLayout extends RelativeLayout implements Checkable {
	public static final String TAG = "ScheduleItemLayout";
    private boolean mChecked = false;


	public ScheduleItemLayout(Context context) {
		super(context);
	}
	
	public ScheduleItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScheduleItemLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		if(mChecked) {
			setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_light)));
		} else {
			setBackgroundDrawable(null);
		}
		 
	}

	@Override
	public void toggle() {
		mChecked = !mChecked;
		if(mChecked) {
			 setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_light)));
		 } else {
				setBackgroundDrawable(null);
		 }
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			return true;
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP){
			return true;
		}
		return false;
	}

}
