package de.ihrigb.fwla.edpmailadapter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import lombok.Getter;

@Getter
class ValuesConsumer implements Consumer<Set<Value>> {
	private int timesCalled = 0;
	private Queue<Set<Value>> values = new LinkedList<>();

	@Override
	public void accept(Set<Value> t) {
		this.timesCalled++;
		this.values.add(t);
	}

	Set<Value> next() {
		return this.values.poll();
	}
}
