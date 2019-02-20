package common;

import java.util.HashMap;
import java.util.Map;

public class RollingIndexMap {
	String name;
	int size;
	long[] keys;
	Map<Long, RollingIndex> mapping;

	public RollingIndexMap(String name, int size, long[] keys) {
		this.name = name;
		this.size = size;
		this.keys = keys;
		this.mapping = new HashMap<Long, RollingIndex>();

		for (long key : keys) {
			mapping.put(key, new RollingIndex(String.format("%s[%d]", name, key), size));
		}
	}

	// return key items with index > skip
	public RResult<Object[]> get(long key, long skipIndex) {
		RollingIndex items = mapping.get(key);
		if (items == null) {
			return new RResult<Object[]>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(key, 10)));
		}

		RResult<Object[]> skipItems = items.get(skipIndex);
		Object[] cached = skipItems.result;
		error err = skipItems.err;
		if (err != null) {
			return new RResult<Object[]>(null, err);
		}

		return new RResult<Object[]>(cached, null);
	}

	public RResult<Object> getItem(long key, long index) {
		return mapping.get(key).getItem(index);
	}

	public RResult<Object> getLast(long key) {
		RollingIndex pe = mapping.get(key);
		if (pe == null) {
			return new RResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(key, 10)));
		}
		Object[] cached = pe.getLastWindow().result1;
		if (cached.length == 0) {
			return new RResult<Object>("", StoreErr.newStoreErr(name, StoreErrType.Empty, ""));
		}
		return new RResult<Object>(cached[cached.length - 1], null);
	}

	public error set(long key, Object item, long index) {
		RollingIndex items = mapping.get(key);
		if (items == null) {
			items = new RollingIndex(String.format("%s[%d]", name, key), size);
			mapping.put(key, items);
		}
		return items.set(item, index);
	}

	// returns [key] => lastKnownIndex
	public Map<Long, Long> known() {
		Map<Long, Long> known = new HashMap<Long, Long>();
		for (long key : mapping.keySet()) {
			RollingIndex items = mapping.get(key);
			Long lastIndex = items.getLastWindow().result2;
			known.put(key, lastIndex);
		}
		return known;
	}

	public error reset() {
		Map<Long, RollingIndex> items = new HashMap<Long, RollingIndex>();
		for (long key : keys) {
			items.put(key, new RollingIndex(String.format("%s[%d]", name, key), size));
		}
		mapping = items;
		return null;
	}

	public void copy(RollingIndexMap other) {
		for (long key : other.keys) {
			RollingIndex rollingIndex = new RollingIndex(String.format("%s[%d]", name, key), size);
			mapping.put(key, rollingIndex);
			rollingIndex.lastIndex = other.mapping.get(key).lastIndex;
			rollingIndex.items = other.mapping.get(key).items;
		}
	}
}