package org.briarproject.api.db;

import org.briarproject.api.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper around a database transaction. Transactions are not thread-safe.
 */
public class Transaction {

	private final Object txn;
	private final boolean readOnly;

	private List<Event> events = null;
	private boolean complete = false;

	public Transaction(Object txn, boolean readOnly) {
		this.txn = txn;
		this.readOnly = readOnly;
	}

	/**
	 * Returns the database transaction. The type of the returned object
	 * depends on the database implementation.
	 */
	public Object unbox() {
		return txn;
	}

	/**
	 * Returns true if the transaction can only be used for reading.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Attaches an event to be broadcast when the transaction has been
	 * committed.
	 */
	public void attach(Event e) {
		if (events == null) events = new ArrayList<Event>();
		events.add(e);
	}

	/**
	 * Returns any events attached to the transaction.
	 */
	public List<Event> getEvents() {
		if (events == null) return Collections.emptyList();
		return events;
	}

	/**
	 * Returns true if the transaction is ready to be committed.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Marks the transaction as ready to be committed. This method must not be
	 * called more than once.
	 */
	public void setComplete() {
		if (complete) throw new IllegalStateException();
		complete = true;
	}
}