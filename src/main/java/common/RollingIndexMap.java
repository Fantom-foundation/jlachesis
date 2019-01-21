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
	public RetResult<Object[]> Get(long key, long skipIndex) {
		RollingIndex items = mapping.get(key);
		if (items == null) {
			return new RetResult<Object[]>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(key, 10)));
		}

		RetResult<Object[]> skipItems = items.Get(skipIndex);
		Object[] cached = skipItems.result;
		error err = skipItems.err;
		if (err != null) {
			return new RetResult<Object[]>(null, err);
		}

		return new RetResult<Object[]>(cached, null);
	}

	public RetResult<Object> GetItem(long key, long index) {
		return mapping.get(key).GetItem(index);
	}

	public RetResult<Object> GetLast(long key) {
		RollingIndex pe = mapping.get(key);
		if (pe == null) {
			return new RetResult<Object>(null,
					StoreErr.newStoreErr(name, StoreErrType.KeyNotFound, Long.toString(key, 10)));
		}
		Object[] cached = pe.GetLastWindow().result1;
		if (cached.length == 0) {
			return new RetResult<Object>("", StoreErr.newStoreErr(name, StoreErrType.Empty, ""));
		}
		return new RetResult<Object>(cached[cached.length - 1], null);
	}

	public error Set(long key, Object item, long index) {
		RollingIndex items = mapping.get(key);
		if (items == null) {
			items = new RollingIndex(String.format("%s[%d]", name, key), size);
			mapping.put(key, items);
		}
		return items.Set(item, index);
	}

	// returns [key] => lastKnownIndex
	public Map<Long, Long> Known() {
		Map<Long, Long> known = new HashMap<Long, Long>();
		for (long key : mapping.keySet()) {
			RollingIndex items = mapping.get(key);
			Long lastIndex = items.GetLastWindow().result2;
			known.put(key, lastIndex);
		}
		return known;
	}

	public error Reset() {
		Map<Long, RollingIndex> items = new HashMap<Long, RollingIndex>();
		for (long key : keys) {
			items.put(key, new RollingIndex(String.format("%s[%d]", name, key), size));
		}
		mapping = items;
		return null;
	}

	public void Import(RollingIndexMap other) {
		for (long key : other.keys) {
			RollingIndex rollingIndex = new RollingIndex(String.format("%s[%d]", name, key), size);
			mapping.put(key, rollingIndex);
			rollingIndex.lastIndex = other.mapping.get(key).lastIndex;
			rollingIndex.items = other.mapping.get(key).items;

			// copy(mapping[key].items[:len(other.mapping[key].items)],
			// other.mapping[key].items)
		}
	}
}