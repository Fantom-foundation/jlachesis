package poset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import autils.Appender;
import common.RetResult;
import common.error;
import peers.Peer;
import peers.Peers;

/**
 * Blank test
 * @author qn
 *
 */
public class InmemStoreTest {
	InmemStore store;
	pub[] participants;

	public void initInmemStore(int cacheSize) {
		long n = 3L;
		pub[]  participantPubs = null;
		Peers participants = new Peers();
		for (int i = 0; i < n; i++) {
			KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
			byte[] pubKey = crypto.Utils.FromECDSAPub(key.getPublic());
//			peer := new Peer(fmt.Sprintf("0x%X", pubKey), "")
			Peer peer = new Peer(crypto.Utils.toHexString(pubKey), "");
			participantPubs = Appender.append(participantPubs,
				new pub(i, key, pubKey, peer.getPubKeyHex()));
			participants.addPeer(peer);
			participantPubs[participantPubs.length-1].id = peer.getID();
		}

		this.store = new InmemStore(participants, cacheSize);
		this.participants = participantPubs;
	}

	@Test
	public void TestInmemEvents() {
		int cacheSize = 100;
		int testSize = 15;
		initInmemStore(cacheSize);

		Map<String,Event[]> events = new HashMap<String,Event[]>();

		// "Store Events"
		for (pub p : participants) {
			Event[] items = null;
			for (int k = 0; k < testSize; k++) {
				Event event = new Event(
						new byte[][]{
							String.format("%s_%d", p.hex.substring(0,5), k).getBytes()},
					null,
					new BlockSignature[]{
							new BlockSignature("validator".getBytes(), 0, "r|s")},
					new String[]{"", ""},
					p.pubKey,
					k, null);
				event.hex(); //just to set private variables
				items = Appender.append(items, event);
				error err = store.SetEvent(event);
				assertNull("No error when setEvent", err);
			}
			events.put(p.hex, items);
		}

		Event ev;
		error err;
		for (String p : events.keySet()) {
			Event[] evs = events.get(p);
			for (int k = 0; k< evs.length; ++k) {
				ev = evs[k];
				RetResult<Event> getEvent = store.GetEvent(ev.hex());
				Event rev = getEvent.result;
				err = getEvent.err;
				assertNull("No error when GetEvent", err);
				assertEquals(String.format("events[%s][%d] should be %s, not %s", p, k, ev, rev),
					 ev.message.Body, rev.message.Body);
			}
		}

		// "Check ParticipantEventsCache"
		long skipIndex = -1L; //do not skip any indexes
		for (pub p : participants) {
			RetResult<String[]> pEventsCall = store.ParticipantEvents(p.hex, skipIndex);
			String[] pEvents = pEventsCall.result;
			err = pEventsCall.err;
			assertNull("No error when ParticipantEvents", err);
			assertEquals(String.format("%s should match size", p.hex), testSize, pEvents.length);

			Event[] eventsArray = events.get(p.hex);
			Event[] expectedEvents = Appender.sliceFromToEnd(eventsArray, (int) skipIndex+1);
			for (int k = 0; k < expectedEvents.length; ++k) {
				Event e = expectedEvents[k];
				assertEquals(String.format("ParticipantEvents[%s][%d] should be match",
						p.hex, k), e.hex(), pEvents[k]);
			}
		}


		//"Check KnownEvents"
		HashMap<Long, Long> expectedKnown = new HashMap<Long,Long>();
		for (pub p : participants) {
			expectedKnown.put(p.id, (long) (testSize - 1));
		}
		Map<Long, Long> known = store.KnownEvents();
		assertEquals("Known events should match", expectedKnown, known);

		//"Add ConsensusEvents"
		for (pub p : participants) {
			Event[] evs = events.get(p.hex);
			for (Event ev1 : evs) {
				err = store.AddConsensusEvent(ev1);
				assertNull("No error when AddConsensusEvent", err);
			}
		}
	}

	@Test
	public void TestInmemRounds() {
		initInmemStore(10);

		RoundInfo round = new RoundInfo();
		HashMap<String, Event> events = new HashMap<String,Event>();
		for (pub p : participants) {
			Event event = new Event(new byte[][]{},
				null,
				new BlockSignature[]{},
				new String[]{"", ""},
				p.pubKey,
				0, null);
			events.put(p.hex, event);
			round.AddEvent(event.hex(), true);
		}

		//"Store Round"
		error err = store.SetRound(0, round);
		assertNull("No error when SetRound", err);

		RetResult<RoundInfo> getRound = store.GetRound(0);
		RoundInfo storedRound = getRound.result;
		err = getRound.err;
		assertNull("No error when GetRound", err);
		assertEquals("Round and StoredRound should match", round, storedRound);

		//"Check LastRound"
		long c = store.LastRound();
		assertEquals("Store LastRound should be 0", 0, c);

		// "Check witnesses"
		String[] witnesses = store.RoundWitnesses(0);
		String[] expectedWitnesses = round.Witnesses();
		assertEquals("There should be matching witnesses", expectedWitnesses.length, witnesses.length);

		for (String w : expectedWitnesses) {
			boolean containWitness = Arrays.asList(expectedWitnesses).contains(w);
			assertTrue(String.format("Witnesses should contain %s", w), containWitness);
		}
	}

	@Test
	public void TestInmemBlocks() {
		initInmemStore(10);

		long index = 0L;
		long roundReceived = 7L;
		byte[][] transactions = new byte[][]{
			"tx1".getBytes(),
			"tx2".getBytes(),
			"tx3".getBytes(),
			"tx4".getBytes(),
			"tx5".getBytes()
		};
		byte[] frameHash = "this is the frame hash".getBytes();
		Block block = new Block(index, roundReceived, frameHash, transactions);

		RetResult<BlockSignature> signCall = block.sign(participants[0].privKey);
		BlockSignature sig1 = signCall.result;
		error err = signCall.err;
		assertNull("No error when sign", err);

		RetResult<BlockSignature> signCall2 = block.sign(participants[1].privKey);
		BlockSignature sig2 = signCall2.result;
		err = signCall2.err;
		assertNull("No error when sign", err);

		block.setSignature(sig1);
		block.setSignature(sig2);

		//"Store Block"
		err = store.SetBlock(block);
		assertNull("No error when SetBlock", err);

		RetResult<Block> getBlockCall = store.GetBlock(index);
		Block storedBlock = getBlockCall.result;
		err = getBlockCall.err;
		assertNull("No error when GetBlock", err);
		assertEquals("Block and StoredBlock should match", storedBlock, block);

		// "Check signatures in stored Block"
		RetResult<Block> getBlock = store.GetBlock(index);
		storedBlock = getBlock.result;
		err = getBlock.err;
		assertNull("No error when GetBlock", err);

		String val1Sig = storedBlock.getSignatures().get(participants[0].hex);
		assertNotNull("Validator1 signature is stored in block", val1Sig);
		assertEquals("Validator1 block signatures differ", sig1.signature, val1Sig);

		String val2Sig = storedBlock.getSignatures().get(participants[1].hex);
		assertNotNull("Validator2 signature is stored in block", val2Sig);
		assertEquals("Validator2 block signatures differ", sig2.signature, val2Sig);
	}
}