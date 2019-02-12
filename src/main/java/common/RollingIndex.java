package common;

import java.util.ArrayList;
import java.util.List;

import autils.Logger;

public class RollingIndex {
	private static Logger logger = Logger.getLogger(RollingIndex.class);

	String name;
	int size;
	long lastIndex;
	List<Object> items; // []interface{}

	public RollingIndex(String name, int size) {
		this.name = name;
		this.size = size;
		this.items = new ArrayList(2 * size); // TBD: items: make([]interface{}, 0, 2*size),
		lastIndex = -1;
	}

	public RResult2<Object[], Long> GetLastWindow() {
		return new RResult2<Object[], Long>(items.toArray(), lastIndex);
	}

	public RetResult<Object[]> Get(long skipIndex) {
		logger.field("skipIndex", skipIndex)
			.field("items", items).debug("Get()");

		Object[] res = new Object[] {};

		if (skipIndex > lastIndex) {
			return new RetResult<Object[]>(res, null);
		}

		long cachedItems = items.size();
		// assume there are no gaps between indexes
		long oldestCachedIndex = lastIndex - cachedItems + 1;

		logger.field("cachedItems", cachedItems)
			.field("oldestCachedIndex", oldestCachedIndex).debug("RollingIndex.Get()");

		if (skipIndex + 1 < oldestCachedIndex) {
			return new RetResult<Object[]>(res,
					StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(skipIndex, 10)));
		}

		// index of 'skipped' in RollingIndex
		long start = skipIndex - oldestCachedIndex + 1;

		// TBD : equivalent with items[start:]?
		res = items.subList((int) start, items.size()).toArray();
//		res = Appender.sliceFromToEnd(items.toArray(), (int) start);

		logger.field("start", start).field("res",  res).debug("RollingIndex.Get()");

		return new RetResult<Object[]>(res, null);
	}

	public RetResult<Object> GetItem(long index) {
		long numitems = items.size();
		long oldestCached = lastIndex - numitems + 1;

		logger.field("index", index)
			.field("items", items)
			.field("numitems", numitems)
			.field("oldestCached", oldestCached).debug("GetItem()");

		if (index < oldestCached) {
			return new RetResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(index, 10)));
		}
		int findex = (int) (index - oldestCached);

		logger.field("findex", findex).debug("GetItem()");

		if (findex >= numitems) {
			return new RetResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(index, 10)));
		}

		logger.field("found item", items.get(findex)).debug("GetItem()");
		return new RetResult<Object>(items.get(findex), null);
	}

	public error Set(Object item, long index) {

		// only allow to set items with index <= lastIndex + 1
		// so that we may assume there are no gaps between items
		if (0 <= lastIndex && index > lastIndex + 1) {
			return StoreErr.newStoreErr(name, StoreErrType.SkippedIndex, Long.toString(index, 10));
		}

		// adding a new item
		if (lastIndex < 0 || index == lastIndex + 1) {
			if (items.size() >= 2 * size) {
				Roll();
			}

			items.add(item);
			lastIndex = index;
			return null;
		}

		// replace and existing item
		// make sure index is also greater or equal than the oldest cached item's index
		long cachedItems = items.size();
		long oldestCachedIndex = lastIndex - cachedItems + 1;

		if (index < oldestCachedIndex) {
			return StoreErr.newStoreErr(name, StoreErrType.TooLate, Long.toString(index, 10));
		}

		// replacing existing item
		int position = (int) (index - oldestCachedIndex); // position of 'index' in RollingIndex
		items.set(position, item);

		return null;
	}

	public void Roll() {
//		Object[] newList = new Object[2 * size];
//		TBD: the above statement is converted correctly?
//		newList = append(newList, items[size:]...);

		List<Object> newList = new ArrayList(2 * size);
		newList.addAll(items.subList(size, items.size()));

		items = newList;
	}
}