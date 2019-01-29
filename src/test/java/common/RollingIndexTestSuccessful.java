package common;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import autils.Appender;

public class RollingIndexTestSuccessful {
	@Test
	public void TestRollingIndex() {
		int size = 10;
		long testSize = 3 * size;
		RollingIndex RollingIndex = new RollingIndex("test", size);
		String[] items = null;
		for (long i = 0L; i < testSize; i++) {
			String item = String.format("item%d", i);
			RollingIndex.Set(item, i);
			items = Appender.append(items, item);
		}

		RResult2<Object[], Long> getLastWindow = RollingIndex.GetLastWindow();
		Object[] cached = getLastWindow.result1;
		long lastIndex = getLastWindow.result2;

		long expectedLastIndex = testSize - 1;
		assertEquals("lastIndex should be %d, not %d", expectedLastIndex, lastIndex);

		int start = (int) (testSize / (2 * size)) * size;
		int count = (int) testSize - start;

		for (int i = 0; i < count; i++) {
			assertEquals(String.format("cached[%d]", i), items[start+i], cached[i].toString());
		}

		error err = RollingIndex.Set("ErrSkippedIndex", expectedLastIndex+2);

		assertTrue("Should return ErrSkippedIndex", StoreErr.Is(err, StoreErrType.SkippedIndex));

		err = RollingIndex.GetItem(9).err;
		assertTrue("Should return ErrTooLate", StoreErr.Is(err, StoreErrType.TooLate));

		Object item;

		long[] indexes = new long[]{10, 17, 29};
		for (long i : indexes) {
			RetResult<Object> getItem = RollingIndex.GetItem(i);
			item  = getItem.result;
			err = getItem.err;

			assertNull(String.format("GetItem(%d) err", i), err);

			assertEquals("GetItem error", item, items[(int)i]);
		}

		err = RollingIndex.GetItem(lastIndex + 1).err;
		assertTrue("Should return KeyNotFound", StoreErr.Is(err, StoreErrType.KeyNotFound));

		//Test updating an item in place
		long updateIndex = 26L;
		String updateValue = "Updated Item";

		err = RollingIndex.Set(updateValue, updateIndex);

		assertNull(String.format("GetItem(%d) err", updateIndex), err);


		RetResult<Object> getItem = RollingIndex.GetItem(updateIndex);
		item = getItem.result;
		err = getItem.err;
		assertNull(String.format("GetItem(%d) err", updateIndex), err);

		assertEquals(String.format("Updated item %d", updateIndex),
				 item.toString(), updateValue);
	}

	@Test
	public void TestRollingIndexSkip() {
		int size = 10;
		int testSize = 25;
		RollingIndex RollingIndex = new RollingIndex("test", size);

		error err = RollingIndex.Get(-1).err;

		assertNull("retrieve item at -1:", err);

		String[] items = null;
		for (int i = 0; i < testSize; i++) {
			String item = String.format("item%d", i);
			RollingIndex.Set(item, i);
			items = Appender.append(items, item);
		}

		err = RollingIndex.Get(0).err;
		assertTrue("Skipping index 0 should return ErrTooLate", StoreErr.Is(err, StoreErrType.TooLate));

		int skipIndex1 = 9;
		String[] expected1 = Appender.slice(items, skipIndex1 + 1, items.length);
		RetResult<Object[]> getItems = RollingIndex.Get(skipIndex1);
		Object[] cached1 = getItems.result;
		err = getItems.err;

		assertNull(String.format("GetItem(%d) err", skipIndex1), err);

		String[] convertedItems = null;
		for (Object item : cached1) {
			convertedItems = Appender.append(convertedItems, item.toString());
		}

		assertEquals("expected and cached should be equal", expected1, convertedItems);


		int skipIndex2 = 15;
		String[] expected2 = Appender.slice(items, skipIndex2+1, items.length);
		RetResult<Object[]> getItems2 = RollingIndex.Get(skipIndex2);
		Object[] cached2 = getItems2.result;
		err = getItems2.err;

		assertNull(String.format("GetItem(%d) err", skipIndex2), err);


		convertedItems = null;
		for (Object item : cached2) {
			convertedItems = Appender.append(convertedItems, item.toString());
		}

		assertEquals("expected and cached should be equal", expected2, convertedItems);

		int skipIndex3 = 27;
		String expected3 = null;

		RetResult<Object[]> getItems3 = RollingIndex.Get(skipIndex3);
		Object[] cached3 = getItems3.result;
		err = getItems3.err;

		assertNull(String.format("GetItem(%d) err", skipIndex3), err);


		convertedItems = null;
		for (Object item : cached3) {
			convertedItems = Appender.append(convertedItems, item.toString());
		}

		assertEquals("expected and cached should be equal", expected3, convertedItems);
	}
}