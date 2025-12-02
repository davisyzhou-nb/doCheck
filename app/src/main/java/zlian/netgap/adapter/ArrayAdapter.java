package zlian.netgap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ArrayAdapter<T> extends BaseAdapter {

	// 数据
	protected ArrayList<T> mObjects;
	protected LayoutInflater mInflater;
	protected final Object mLock = new Object();
	protected Context mContext;

	public ArrayAdapter(final Context ctx, final ArrayList<T> l) {
		mObjects = l == null ? new ArrayList<T>() : l;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = ctx;
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void add(T item) {
		this.mObjects.add(item);
	}

	public void replace(ArrayList<T> newObjects) {
		if (newObjects == null)
			newObjects = new ArrayList<T>();
		this.mObjects = newObjects;
	}

	/**
	 * Adds the specified items at the end of the array.
	 * 
	 * @param items
	 *            The items to add at the end of the array.
	 */
	public void addAll(T... items) {
		ArrayList<T> values = this.mObjects;
		for (T item : items) {
			values.add(item);
		}
		this.mObjects = values;
	}

	/**
	 * 
	 * @param collection
	 */
	public void addAll(Collection<? extends T> collection) {
		if (collection != null) {
			mObjects.addAll(collection);
		}
		notifyDataSetChanged();
	}

	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		mObjects.clear();
		notifyDataSetChanged();
	}

	/**
	 * 获取所有数据
	 * 
	 * @return
	 */
	public final ArrayList<T> getAll() {
		return mObjects;
	}
}
