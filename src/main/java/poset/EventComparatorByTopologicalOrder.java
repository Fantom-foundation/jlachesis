package poset;

import java.util.Comparator;

public class EventComparatorByTopologicalOrder implements Comparator<Event> {

	public EventComparatorByTopologicalOrder() {
	}

	public int compare(Event o1, Event o2) {
		return Long.compare(o1.message.TopologicalIndex, o2.message.TopologicalIndex);
	}
}
