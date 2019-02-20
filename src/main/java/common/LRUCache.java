package common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple LRU cache implementation.
 * TODO consider to use other LRU cache impl
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K,V> extends LinkedHashMap<K, V> {
	public static <K,V> RResult<LRUCache<K,V>> New(int i) {
		// TBD implement it
		LRUCache<K,V> lruCache = new LRUCache<K,V>(i);

		return new RResult<LRUCache<K,V>>(lruCache, null);
	}

	private int cacheSize;

	public LRUCache(int cacheSize) {
	    super(cacheSize);
	    this.cacheSize = cacheSize;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	    return size() >= cacheSize;
	}
}
