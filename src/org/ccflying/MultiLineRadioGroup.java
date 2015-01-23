package org.ccflying;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

// org.ccflying.MultiLineRadioGroup
public class MultiLineRadioGroup extends ViewGroup implements OnClickListener {
	private int mX, mY;
	private List<CheckBox> viewList;
	private int childMarginHorizontal = 0;
	private int childMarginVertical = 0;
	private int childResId = 0;
	private int childCount = 0;
	private int childValuesId = 0;
	private boolean singleChoice = false;
	private int mLastCheckedPosition = -1;
	private OnCheckedChangedListener listener;
	private List<String> childValues;
	private boolean forceLayout;

	public MultiLineRadioGroup(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		viewList = new ArrayList<CheckBox>();
		childValues = new ArrayList<String>();
		TypedArray arr = context.obtainStyledAttributes(attrs,
				R.styleable.MultiLineRadioGroup);
		childMarginHorizontal = arr.getDimensionPixelSize(
				R.styleable.MultiLineRadioGroup_child_margin_horizontal, 15);
		childMarginVertical = arr.getDimensionPixelSize(
				R.styleable.MultiLineRadioGroup_child_margin_vertical, 5);
		childResId = arr.getResourceId(
				R.styleable.MultiLineRadioGroup_child_layout, 0);
		childCount = arr.getInt(R.styleable.MultiLineRadioGroup_child_count, 0);
		singleChoice = arr.getBoolean(
				R.styleable.MultiLineRadioGroup_single_choice, true);
		childValuesId = arr.getResourceId(
				R.styleable.MultiLineRadioGroup_child_values, 0);
		if (childResId == 0) {
			throw new RuntimeException(
					"The attr 'child_layout' must be specified!");
		}
		if (childValuesId != 0) {
			String[] childValues_ = getResources()
					.getStringArray(childValuesId);
			for (String str : childValues_) {
				childValues.add(str);
			}
		}
		if (childCount > 0) {
			boolean hasValues = childValues != null;
			for (int i = 0; i < childCount; i++) {
				View v = LayoutInflater.from(context).inflate(childResId, null);
				if (!(v instanceof CheckBox)) {
					throw new RuntimeException(
							"The attr child_layout's root must be a CheckBox!");
				}
				CheckBox cb = (CheckBox) v;
				viewList.add(cb);
				addView(cb);
				if (hasValues && i < childValues.size()) {
					cb.setText(childValues.get(i));
				} else {
					childValues.add(cb.getText().toString());
				}
				cb.setTag(i);
				cb.setOnClickListener(this);
			}
		} else {
			Log.d("tag", "childCount is 0");
		}
		arr.recycle();
	}

	public MultiLineRadioGroup(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MultiLineRadioGroup(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		childCount = getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				measureChild(v, widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!changed && !forceLayout) {
			Log.d("tag", "onLayout:unChanged");
			return;
		}
		mX = mY = 0;
		childCount = getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View v = getChildAt(i);
				if (v.getMeasuredWidth() + childMarginHorizontal * 2 + mX > getWidth()) {
					mY++;
					mX = 0;
				}
				int startX = mX + childMarginHorizontal;
				int startY = mY * v.getMeasuredHeight() + (mY + 1)
						* childMarginVertical;
				v.layout(startX, startY, startX + v.getMeasuredWidth(), startY
						+ v.getMeasuredHeight());
				mX += v.getMeasuredWidth() + childMarginHorizontal * 2;
			}
		}
		forceLayout = false;
	}

	@Override
	public void onClick(View v) {
		if (singleChoice) {
			try {
				int i = (Integer) v.getTag();
				if (mLastCheckedPosition == i) {
					return;
				}
				if (mLastCheckedPosition >= 0
						&& mLastCheckedPosition < viewList.size()) {
					viewList.get(mLastCheckedPosition).setChecked(false);
				}
				mLastCheckedPosition = i;
				if (listener != null) {
					listener.onItemChecked(this, i);
				}
			} catch (Exception e) {
			}
		} else { // multiChoice

		}
	}

	public void setOnCheckChangedListener(OnCheckedChangedListener l) {
		this.listener = l;
	}

	public boolean setItemChecked(int position) {
		if (position >= 0 && position < viewList.size()) {
			if (singleChoice) {
				if (position == mLastCheckedPosition) {
					return true;
				}
				if (mLastCheckedPosition >= 0
						&& mLastCheckedPosition < viewList.size()) {
					viewList.get(mLastCheckedPosition).setChecked(false);
				}
			}
			viewList.get(position).setChecked(true);
			return true;
		}
		return false;
	}

	public boolean isSingleChoice() {
		return singleChoice;
	}

	public void setChoiceMode(boolean isSingle) {
		this.singleChoice = isSingle;
		if (singleChoice) {
			if (getCheckedValues().size() > 1) {
				clearChecked();
			}
		}
	}

	public int[] getCheckedItems() {
		if (singleChoice && mLastCheckedPosition >= 0
				&& mLastCheckedPosition < viewList.size()) {
			return new int[] { mLastCheckedPosition };
		}
		SparseIntArray arr = new SparseIntArray();
		for (int i = 0; i < viewList.size(); i++) {
			if (viewList.get(i).isChecked()) {
				arr.put(i, i);
			}
		}
		if (arr.size() != 0) {
			int[] r = new int[arr.size()];
			for (int i = 0; i < arr.size(); i++) {
				r[i] = arr.keyAt(i);
			}
			return r;
		}
		return null;
	}

	public List<String> getCheckedValues() {
		List<String> list = new ArrayList<String>();
		if (singleChoice && mLastCheckedPosition >= 0
				&& mLastCheckedPosition < viewList.size()) {
			list.add(viewList.get(mLastCheckedPosition).getText().toString());
			return list;
		}
		for (int i = 0; i < viewList.size(); i++) {
			if (viewList.get(i).isChecked()) {
				list.add(viewList.get(i).getText().toString());
			}
		}
		return list;
	}

	public int append(String str) {
		View v = LayoutInflater.from(getContext()).inflate(childResId, null);
		if (!(v instanceof CheckBox)) {
			throw new RuntimeException(
					"The attr child_layout's root must be a CheckBox!");
		}
		CheckBox cb = (CheckBox) v;
		cb.setText(str);
		cb.setTag(childCount);
		cb.setOnClickListener(this);
		viewList.add(cb);
		addView(cb);
		childValues.add(str);
		childCount++;
		forceLayout = true;
		postInvalidate();
		return childCount - 1;
	}

	public void addAll(List<String> list) {
		if (list != null && list.size() > 0) {
			for (String str : list) {
				append(str);
			}
		}
	}

	public boolean remove(int position) {
		if (position >= 0 && position < viewList.size()) {
			CheckBox cb = viewList.remove(position);
			removeView(cb);
			childValues.remove(cb.getText().toString());
			childCount--;
			forceLayout = true;
			if (position <= mLastCheckedPosition) { // before LastCheck
				if (mLastCheckedPosition == position) {
					mLastCheckedPosition = -1;
				} else {
					mLastCheckedPosition--;
				}
			}
			for (int i = position; i < viewList.size(); i++) {
				viewList.get(i).setTag(i);
			}
			postInvalidate();
			return true;
		}
		return false;
	}

	public boolean insert(int position, String str) {
		if (position < 0 || position > viewList.size()) {
			return false;
		}
		View v = LayoutInflater.from(getContext()).inflate(childResId, null);
		if (!(v instanceof CheckBox)) {
			throw new RuntimeException(
					"The attr child_layout's root must be a CheckBox!");
		}
		CheckBox cb = (CheckBox) v;
		cb.setText(str);
		cb.setTag(position);
		cb.setOnClickListener(this);
		viewList.add(position, cb);
		addView(cb, position);
		childValues.add(position, str);
		childCount++;
		forceLayout = true;
		if (position <= mLastCheckedPosition) { // before LastCheck
			mLastCheckedPosition++;
		}
		for (int i = position; i < viewList.size(); i++) {
			viewList.get(i).setTag(i);
		}
		postInvalidate();
		return true;
	}

	public void clearChecked() {
		if (singleChoice) {
			if (mLastCheckedPosition >= 0
					&& mLastCheckedPosition < viewList.size()) {
				viewList.get(mLastCheckedPosition).setChecked(false);
				mLastCheckedPosition = -1;
				return;
			}
		}
		for (CheckBox cb : viewList) {
			if (cb.isChecked()) {
				cb.setChecked(false);
			}
		}
	}

	public interface OnCheckedChangedListener {
		public void onItemChecked(MultiLineRadioGroup group, int position);
	}
}