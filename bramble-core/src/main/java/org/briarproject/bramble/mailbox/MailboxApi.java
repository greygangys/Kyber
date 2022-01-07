package org.briarproject.bramble.mailbox;

import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.mailbox.MailboxProperties;

import java.io.IOException;

import javax.annotation.concurrent.Immutable;

interface MailboxApi {

	/**
	 * Sets up the mailbox with the setup token.
	 *
	 * @param properties MailboxProperties with the setup token
	 * @return the owner token
	 * @throws ApiException for 401 response.
	 */
	String setup(MailboxProperties properties)
			throws IOException, ApiException;

	/**
	 * Checks the status of the mailbox.
	 *
	 * @return true if the status is OK, false otherwise.
	 * @throws ApiException for 401 response.
	 */
	boolean checkStatus(MailboxProperties properties)
			throws IOException, ApiException;

	/**
	 * Adds a new contact to the mailbox.
	 *
	 * @throws TolerableFailureException if response code is 409
	 * (contact was already added).
	 */
	void addContact(MailboxProperties properties, MailboxContact contact)
			throws IOException, ApiException,
			TolerableFailureException;

	@Immutable
	class MailboxContact {
		public final int contactId;
		public final String token, inboxId, outboxId;

		MailboxContact(ContactId contactId,
				String token,
				String inboxId,
				String outboxId) {
			this.contactId = contactId.getInt();
			this.token = token;
			this.inboxId = inboxId;
			this.outboxId = outboxId;
		}
	}

	@Immutable
	class ApiException extends Exception {
	}

	/**
	 * A failure that does not need to be retried,
	 * e.g. when adding a contact that already exists.
	 */
	@Immutable
	class TolerableFailureException extends Exception {
	}
}