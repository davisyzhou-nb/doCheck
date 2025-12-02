package zlian.netgap.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import zlian.netgap.R;

public abstract class BaseOptionAdapter<T> extends ArrayAdapter<T> {

	protected int currentindex;
	protected Context mContext;

	public BaseOptionAdapter(Context ctx, ArrayList<T> l) {
		super(ctx, l);
		mContext = ctx;
	}

	public int getCurrentindex() {
		return currentindex;
	}

	public void setCurrentindex(int currentindex) {
		this.currentindex = currentindex;
		notifyDataSetChanged();
	}

	@Override
	public void clear() {
		super.clear();
		currentindex = 0;
	}

	protected  View newView(ViewGroup parent) {
		return mInflater.inflate(R.layout.item_mlist, parent, false);
	}

	protected void setIndexColor(int position, TextView tv) {
//		if (currentindex == position) {
//			tv.setTextColor(mContext.getResources().getColor(R.color.black));
//		} else {
//			tv.setTextColor((mContext.getResources()
//					.getColorStateList(R.color.checkoption_text_color)));
//		}
	}

	public boolean available() {
		return currentindex < getCount();
	}

	public T getCurrentItem() {
		if (available()) {
			return getItem(currentindex);
		}
		return null;

	}

}
