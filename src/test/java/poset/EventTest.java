package poset;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import common.RetResult;
import common.error;

/**
 * Event tests
 *
 * @author qn
 *
 */
public class EventTest {
	//@Test
	public void TestMarshallBody() {
		EventBody body = createDummyEventBody();
		RetResult<byte[]> protoMarshal = body.marshaller().protoMarshal();

		byte[] raw = protoMarshal.result;
		error err = protoMarshal.err;
		assertNull("No Error marshalling EventBody: %s", err);

		EventBody newBody = new EventBody();
		err = newBody.marshaller().protoUnmarshal(raw);
		assertNull("No Error unmarshalling EventBody", err);

		assertArrayEquals("Transactions should match", body.Transactions, newBody.Transactions);
		assertArrayEquals("Internal Transactions should match", body.InternalTransactions, newBody.InternalTransactions);
		assertArrayEquals("BlockSigneclipse-javadoc:%E2%98%82=jlachesis/%5C/home%5C/qn%5C/.m2%5C/repository%5C/junit%5C/junit%5C/4.12%5C/junit-4.12.jar%3Corg.junitatures should match", body.BlockSignatures, newBody.BlockSignatures);
		assertArrayEquals("Parents should match", body.Parents, newBody.Parents);
		assertArrayEquals("Creators should match", body.Creator, newBody.Creator);
	}

	//@Test
	public void TestSignEvent() {
		KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
		byte[] publicKeyBytes = crypto.Utils.FromECDSAPub(key.getPublic());

		EventBody body = createDummyEventBody();
		body.Creator = publicKeyBytes;

		Event event = new Event(new EventMessage(body));
		error err = event.Sign(key.getPrivate());
		assertNull("No Error signing Event", err);

		RetResult<Boolean> verify = event.Verify();
		boolean res = verify.result;
		err = verify.err;
		assertNull("Node Error verifying signature", err);
		assertTrue("Verify returned true", res);
	}

	//@Test
	public void TestMarshallEvent() {
		KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
		byte[] publicKeyBytes = crypto.Utils.FromECDSAPub(key.getPublic());

		EventBody body = createDummyEventBody();
		body.Creator = publicKeyBytes;

		Event event = new Event(new EventMessage(body));
		error err = event.Sign(key.getPrivate());
		assertNull("No Error signing Event", err);

		RetResult<byte[]> protoMarshal = event.ProtoMarshal();
		byte[] raw = protoMarshal.result;
		err = protoMarshal.err;
		assertNull("No Error marshalling Event", err);


		Event newEvent = new Event();
		err = newEvent.ProtoUnmarshal(raw);
		assertNull("No Error unmarshalling Event", err);
		assertEquals("Events should equal", newEvent.Message, event.Message);
	}

	//@Test
	public void TestWireEvent() {
		KeyPair key = crypto.Utils.GenerateECDSAKeyPair().result;
		byte[] publicKeyBytes = crypto.Utils.FromECDSAPub(key.getPublic());

		EventBody body = createDummyEventBody();
		body.Creator = publicKeyBytes;

		Event event = new Event(new EventMessage(body));
		error err = event.Sign(key.getPrivate());
		assertNull("No Error signing Event", err);

		event.SetWireInfo(1, 66, 2, 67);

		InternalTransaction[] internalTransactions = Arrays.copyOf(event.Message.Body.InternalTransactions, event.Message.Body.InternalTransactions.length);

		WireEvent expectedWireEvent = new WireEvent(
			new WireBody(
				/* Transactions: */         event.Message.Body.Transactions,
				/* InternalTransactions: */ internalTransactions,
				/* BlockSignatures: */     event.WireBlockSignatures(),
				/* SelfParentIndex: */      1,
				/* OtherParentCreatorID: */ 66,
				/* OtherParentIndex: */    2,
				/* CreatorID: */           67,
				/* Index: */  event.Message.Body.Index
			),
			event.Message.Signature
		);

		WireEvent wireEvent = event.ToWire();
		assertEquals("WireEvent should equal", expectedWireEvent, wireEvent);
	}

	@Test
	public void TestIsLoaded() {
		//null payload
		Event event = new Event(null, null, null, new String[]{"p1", "p2"}, "creator".getBytes(), 1, null);
		assertFalse("IsLoaded() should return false for null Body.Transactions and Body.BlockSignatures", event.IsLoaded());

		//empty payload
		event.Message.Body.Transactions = new byte[][] {};
		assertFalse("IsLoaded() should return false for empty Body.Transactions", event.IsLoaded());

		event.Message.Body.BlockSignatures = new BlockSignature[]{};
		assertFalse("IsLoaded() should return false for empty Body.BlockSignatures", event.IsLoaded());

		//initial event
		event.Message.Body.Index = 0;
		assertTrue("IsLoaded() should return true for initial event", event.IsLoaded());

		//non-empty tx payload
		event.Message.Body.Transactions = new byte[][]{"abc".getBytes()};
		assertTrue("IsLoaded() should return true for non-empty transaction payload", event.IsLoaded());

		//non-empy signature payload
		event.Message.Body.Transactions = null;
		event.Message.Body.BlockSignatures = new BlockSignature[]{
			new BlockSignature("validator".getBytes(), 0, "r|s")
		};
		assertTrue("IsLoaded() should return true for non-empty signature payload", event.IsLoaded() );
	}

	@Test
	public void TestEventFlagTable() {
		HashMap<String, Long> exp = new HashMap<String,Long>();
		exp.put("x", 1L);
		exp.put("y", 0L);
		exp.put("z", 2L);

		Event event = new Event(null, null, null, new String[]{"p1", "p2"}, "creator".getBytes(), 1, exp);
		assertFalse("IsLoaded() should return false for null Body.Transactions and Body.BlockSignatures", event.IsLoaded());

		assertTrue("FlagTable is null", event.Message.FlagTable.length != 0);

		RetResult<Map<String, Long>> getFlagTable = event.GetFlagTable();
		Map<String, Long> res = getFlagTable.result;
		error err = getFlagTable.err;
		assertNull("No error", err);
		assertEquals("expected flag table should match", exp, res);
	}

	@Test
	public void TestMergeFlagTable() {
		HashMap<String, Long> exp = new HashMap<String,Long>();
		exp.put("x", 1L);
		exp.put("y", 1L);
		exp.put("z", 1L);

		HashMap<String, Long>[] syncData = new HashMap[2];
		exp = new HashMap<String,Long>();
		exp.put("x", 0L);
		exp.put("y", 1L);
		exp.put("z", 0L);
		syncData[0] = exp;

		exp = new HashMap<String,Long>();
		exp.put("x", 0L);
		exp.put("y", 0L);
		exp.put("z", 1L);
		syncData[1] = exp;

		HashMap<String, Long> start = new HashMap<String,Long>();
		start.put("x", 1L);
		start.put("y", 0L);
		start.put("z", 0L);

		byte[] ft = new FlagTableWrapper(start).marshaller().protoMarshal().result;
		EventMessage eventMessage = new  EventMessage();
		eventMessage.FlagTable = ft;
		Event event = new Event(eventMessage );

		for (HashMap<String, Long> v : syncData) {
			RetResult<Map<String, Long>> mergeFlagTable = event.MergeFlagTable(v);
			Map<String, Long> flagTable = mergeFlagTable.result;
			error err = mergeFlagTable.err;
			assertNull("No error", err);

			byte[] raw = new FlagTableWrapper(flagTable).marshaller().protoMarshal().result;
			event.Message.FlagTable = raw;
		}

		FlagTableWrapper res = new FlagTableWrapper();
		res.marshaller().protoUnmarshal(event.Message.FlagTable);
		assertEquals("expected flag table should match", exp, res.Body);
	}


	private EventBody createDummyEventBody() {
		EventBody body = new EventBody();
		body.Transactions = new byte[][]{ "abc".getBytes(), "def".getBytes()};
		body.InternalTransactions = new InternalTransaction[]{};
		body.Parents = new String[]{"self", "other"};
		body.Creator = "public key".getBytes();
		body.BlockSignatures = new BlockSignature[]{
			new BlockSignature(body.Creator, 0, "r|s")
			};
		return body;
	}
}