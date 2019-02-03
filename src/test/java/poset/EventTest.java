package poset;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import org.junit.Test;

import common.RetResult;
import common.error;
import crypto.Utils;

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
		RetResult<byte[]> protoMarshal = body.ProtoMarshal();

		byte[] raw = protoMarshal.result;
		error err = protoMarshal.err;
		assertNull("No Error marshalling EventBody: %s", err);

		EventBody newBody = new EventBody();
		err = newBody.ProtoUnmarshal(raw);
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

//	@Test
//	public void TestIsLoaded() {
//		//null payload
//		Event event = new Event(null, null, null, new String[]{"p1", "p2"}, "creator".getBytes(), 1, null);
//		if (event.IsLoaded()) {
//			t.Fatalf("IsLoaded() should return false for null Body.Transactions and Body.BlockSignatures")
//		}
//
//		//empty payload
//		event.Message.Body.Transactions = [][]byte{}
//		if (event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return false for empty Body.Transactions")
//		}
//
//		event.Message.Body.BlockSignatures = []*BlockSignature{}
//		if (event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return false for empty Body.BlockSignatures")
//		}
//
//		//initial event
//		event.Message.Body.Index = 0
//		if (!event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return true for initial event")
//		}
//
//		//non-empty tx payload
//		event.Message.Body.Transactions = [][]byte{[]byte("abc")}
//		if (!event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return true for non-empty transaction payload")
//		}
//
//		//non-empy signature payload
//		event.Message.Body.Transactions = null
//		event.Message.Body.BlockSignatures = []*BlockSignature{
//			&BlockSignature{Validator: []byte("validator"), Index: 0, Signature: "r|s"},
//		}
//		if (!event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return true for non-empty signature payload")
//		}
//	}

//	@Test
//	public void TestEventFlagTable() {
//		exp := map[string]int64{
//			"x": 1,
//			"y": 0,
//			"z": 2,
//		}
//
//		event := NewEvent(null, null, null, []string{"p1", "p2"}, []byte("creator"), 1, exp)
//		if (event.IsLoaded() {
//			t.Fatalf("IsLoaded() should return false for null Body.Transactions and Body.BlockSignatures")
//		}
//
//		if (len(event.Message.FlagTable) == 0 {
//			t.Fatal("FlagTable is null")
//		}
//
//		res, err := event.GetFlagTable()
//		if (err != null {
//			t.Fatal(err)
//		}
//
//		if (!reflect.DeepEqual(res, exp) {
//			t.Fatalf("expected flag table: %+v, got: %+v", exp, res)
//		}
//	}

//	@Test
//	public void TestMergeFlagTable() {
//		exp := map[string]int64{
//			"x": 1,
//			"y": 1,
//			"z": 1,
//		}
//
//		syncData := []map[string]int64{
//			{
//				"x": 0,
//				"y": 1,
//				"z": 0,
//			},
//			{
//				"x": 0,
//				"y": 0,
//				"z": 1,
//			},
//		}
//
//		start := map[string]int64{
//			"x": 1,
//			"y": 0,
//			"z": 0,
//		}
//
//		ft, _ := proto.Marshal(&FlagTableWrapper { Body: start })
//		event := Event{Message: EventMessage { FlagTable: ft} }
//
//		for _, v := range syncData {
//			flagTable, err := event.MergeFlagTable(v)
//			if ((err != null {
//				t.Fatal(err)
//			}
//
//			raw, _ := proto.Marshal(&FlagTableWrapper { Body: flagTable })
//			event.Message.FlagTable = raw
//		}
//
//		var res FlagTableWrapper
//		proto.Unmarshal(event.Message.FlagTable, &res)
//
//		if (!reflect.DeepEqual(exp, res.Body) {
//			t.Fatalf("expected flag table: %+v, got: %+v", exp, res.Body)
//		}
//	}


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