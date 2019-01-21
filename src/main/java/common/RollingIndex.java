package common;

import java.util.Arrays;

public class RollingIndex {
	String name;
	int size;
	long lastIndex;
	Object[] items; // []interface{}

	public RollingIndex(String name, int size) {
		this.name = name;
		this.size = size;
		this.items = new Object[2 * size]; // TBD: items: make([]interface{}, 0, 2*size),
		lastIndex = -1;
	}

	public RResult2<Object[], Long> GetLastWindow() {
		return new RResult2<Object[], Long>(items, lastIndex);
	}

	public RetResult<Object[]> Get(long skipIndex) {
		Object[] res = new Object[] {};

		if (skipIndex > lastIndex) {
			return new RetResult<Object[]>(res, null);
		}

		long cachedItems = items.length;
		// assume there are no gaps between indexes
		long oldestCachedIndex = lastIndex - cachedItems + 1;
		if (skipIndex + 1 < oldestCachedIndex) {
			return new RetResult<Object[]>(res,
					StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(skipIndex, 10)));
		}

		// index of 'skipped' in RollingIndex
		long start = skipIndex - oldestCachedIndex + 1;

		// TBD : equivalent with items[start:]?
		res = Arrays.copyOfRange(items, (int) start, items.length - 1);
		return new RetResult<Object[]>(res, null);
	}

	public RetResult<Object> GetItem(long index) {
		long numitems = items.length;
		long oldestCached = lastIndex - numitems + 1;
		if (index < oldestCached) {
			return new RetResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(index, 10)));
		}
		int findex = (int) (index - oldestCached);
		if (findex >= numitems) {
			return new RetResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(index, 10)));
		}
		return new RetResult<Object>(items[findex], null);
	}

	public error Set(Object item, long index) {

		// only allow to set items with index <= lastIndex + 1
		// so that we may assume there are no gaps between items
		if (0 <= lastIndex && index > lastIndex + 1) {
			return StoreErr.newStoreErr(name, StoreErrType.SkippedIndex, Long.toString(index, 10));
		}

		// adding a new item
		if (lastIndex < 0 || index == lastIndex + 1) {
			if (items.length >= 2 * size) {
				Roll();
			}
			// TBD: is it correct?
//			items = append(items, item);
			items[(int) index] = item;

			lastIndex = index;
			return null;
		}

		// replace and existing item
		// make sure index is also greater or equal than the oldest cached item's index
		long cachedItems = items.length;
		long oldestCachedIndex = lastIndex - cachedItems + 1;

		if (index < oldestCachedIndex) {
			return StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(index, 10));
		}

		// replacing existing item
		int position = (int) (index - oldestCachedIndex); // position of 'index' in RollingIndex
		items[position] = item;

		return null;
	}

	public void Roll() {
		Object[] newList = new Object[2 * size];
		newList = Arrays.copyOfRange(items, size, items.length - 1);
//		TBD: the above statement is convereted correctly?
//		newList = append(newList, items[size:]...);
		items = newList;
	}
}