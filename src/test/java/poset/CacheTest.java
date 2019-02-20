package poset;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import autils.Appender;
import common.RResult;
import common.StoreErr;
import common.StoreErrType;
import common.error;
import peers.Peer;
import peers.Peers;

/**
 * Test of cache
 *
 * @author qn
 *
 */
public class CacheTest {

	@Test
	public void TestParticipantEventsCache() {
		int size = 10;
		long testSize = 25L;
		Peers participants = Peers.newPeersFromSlice(new peers.Peer[]{
			new Peer("0xaa", ""),
			new Peer("0xbb", ""),
			new Peer("0xcc", "")
		});

		ParticipantEventsCache pec = new ParticipantEventsCache(size, participants);

		HashMap<String, String[]> items = new HashMap<>();
		for (String pk : participants.getByPubKey().keySet()) {
			items.put(pk,  new String[]{});
		}

		for (long i = 0L; i < testSize; i++) {
			for (String pk : participants.getByPubKey().keySet()) {
				String item = String.format("%s%d", pk, i);
				pec.Set(pk, item, i);

				String[] pitems = items.get(pk);
				pitems = Appender.append(pitems, item);
				items.put(pk, pitems);
			}
		}

		// GET ITEM ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		for (String pk : participants.getByPubKey().keySet()) {
			System.out.println("pk = " + pk);
			long index1 = 9L;
			RResult<String> GetItemCall = pec.GetItem(pk, index1);
			error err = GetItemCall.err;
			assertTrue("Expected ErrTooLate", StoreErr.Is(err, StoreErrType.TooLate));

			long index2 = 15L;
			String expected2 = items.get(pk)[(int)index2];

			RResult<String> GetItemCall2 = pec.GetItem(pk, index2);
			String actual2 = GetItemCall2.result;
			err = GetItemCall2.err;
			assertNull("No error when get item from cache", err);
			assertEquals("expected and cached equal", expected2, actual2);

			long index3 = 27L;
			RResult<String[]> GetItemCall3 = pec.Get(pk, index3);
			String[] actual3 = GetItemCall3.result;
			err = GetItemCall3.err;
			assertNull("No error when get item from cache", err);
			assertArrayEquals("expected and cached equal", new String[0], actual3);
		}

		//KNOWN ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		Map<Long, Long> known = pec.Known();
		for (long p : known.keySet()) {
			long k = known.get(p);
			long expectedLastIndex = testSize - 1;

			assertEquals(String.format("Known[%d] should be equal to the expected last index", p), expectedLastIndex, k);
		}

		//GET ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		for (String pk : participants.getByPubKey().keySet()) {
			RResult<String[]> GetItemCall = pec.Get(pk, 0);
			error err = GetItemCall.err;
			assertTrue("Expected ErrTooLate", StoreErr.Is(err, StoreErrType.TooLate));

			long skipIndex = 9L;
			String[] expected = Appender.sliceFromToEnd(items.get(pk), (int) skipIndex+1);
			RResult<String[]> GetItemCallSkip = pec.Get(pk,skipIndex);
			String[] cached = GetItemCallSkip.result;
			err = GetItemCallSkip.err;
			assertNull("No error when get item from cache", err);
			assertArrayEquals("expected and cached equal", expected, cached);

			long skipIndex2 = 15L;
			String[] expected2 = Appender.sliceFromToEnd(items.get(pk), (int) skipIndex2+1);
			RResult<String[]> GetItemCallSkip2 = pec.Get(pk,skipIndex2);
			String[] cached2 = GetItemCallSkip2.result;
			err = GetItemCallSkip2.err;
			assertNull("No error when get item from cache", err);
			assertArrayEquals("expected and cached equal", expected2, cached2);

			long skipIndex3 = 27L;
			RResult<String[]> GetItemCallSkip3 = pec.Get(pk,skipIndex3);
			String[] cached3 = GetItemCallSkip3.result;
			err = GetItemCallSkip3.err;
			assertNull("No error when get item from cache", err);
			assertArrayEquals("expected and cached equal", new String[0], cached3);
		}
	}

	@Test
	public void TestParticipantEventsCacheEdge() {
		int size = 10;
		long testSize = 11L;
		Peers participants = Peers.newPeersFromSlice(new peers.Peer[]{
			new Peer("0xaa", ""),
			new Peer("0xbb", ""),
			new Peer("0xcc", ""),
		});

		ParticipantEventsCache pec = new ParticipantEventsCache(size, participants);

		HashMap<String, String[]> items = new HashMap<>();
		for (String pk : participants.getByPubKey().keySet()) {
			items.put(pk,  new String[]{});
		}

		for (long i = 0L; i < testSize; i++) {
			for (String pk : participants.getByPubKey().keySet()) {
				String item = String.format("%s%d", pk, i);
				pec.Set(pk, item, i);

				String[] pitems = items.get(pk);
				pitems = Appender.append(pitems, item);
				items.put(pk, pitems);
			}
		}

		for (String pk : participants.getByPubKey().keySet()) {
			String[] expected = Appender.sliceFromToEnd(items.get(pk), size);

			RResult<String[]> GetItemCallSkip = pec.Get(pk, (long) (size-1));
			String[] cached = GetItemCallSkip.result;
			error err = GetItemCallSkip.err;
			assertNull("No error when get item from cache", err);
			assertArrayEquals("expected and cached equal", expected, cached);
		}
	}
}