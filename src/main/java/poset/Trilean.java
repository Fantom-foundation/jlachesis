package poset;

public enum Trilean {
	Trilean_UNDEFINED (0, "UNDEFINED"),
	Trilean_TRUE (1, "TRUE"),
	Trilean_FALSE (2, "FALSE");
	
	private Trilean(int trilean, String name) {
		this.Trilean = trilean;
		this.name = name;
	}

	int Trilean;
	String name;

//	var Trilean_name = map[int32]string{
//		0: "UNDEFINED",
//		1: "TRUE",
//		2: "FALSE",
//	}
//	
//	var Trilean_value = map[string]int32{
//		"UNDEFINED": 0,
//		"TRUE":      1,
//		"FALSE":     2,
//	}

	public String String() {
		return name;
	}
}
