package poset;

import java.util.Map;

public enum TransactionType {
  PEER_ADD,
  PEER_REMOVE;

  public static Map<Integer,String> TransactionType_nameMap;
  public static Map<String,Integer> TransactionType_valueMap;
  
  static {
	  TransactionType_nameMap.put(0, "PEER_ADD");
	  TransactionType_nameMap.put(1, "PEER_REMOVE");
	  TransactionType_valueMap.put("PEER_ADD", 0);
	  TransactionType_valueMap.put("PEER_REMOVE", 1);
  }
	
 public static String getName(int i) {
	 return TransactionType_nameMap.get(i);
 }
 
 public static int getValue(String v) {
	 return TransactionType_valueMap.get(v);
 }
 
  int value;
  String name;
}