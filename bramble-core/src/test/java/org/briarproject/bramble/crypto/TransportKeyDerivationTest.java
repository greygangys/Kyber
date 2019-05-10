package org.briarproject.bramble.crypto;

import org.briarproject.bramble.api.crypto.CryptoComponent;
import org.briarproject.bramble.api.crypto.SecretKey;
import org.briarproject.bramble.api.crypto.TransportCrypto;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.transport.TransportKeys;
import org.briarproject.bramble.test.BrambleTestCase;
import org.briarproject.bramble.test.TestSecureRandomProvider;
import org.junit.Test;

import java.util.Arrays;

import static org.briarproject.bramble.crypto.KeyDerivationTestUtils.assertAllDifferent;
import static org.briarproject.bramble.crypto.KeyDerivationTestUtils.assertMatches;
import static org.briarproject.bramble.test.TestUtils.getSecretKey;
import static org.briarproject.bramble.test.TestUtils.getTransportId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class TransportKeyDerivationTest extends BrambleTestCase {

	private final CryptoComponent crypto =
			new CryptoComponentImpl(new TestSecureRandomProvider(), null);
	private final TransportCrypto transportCrypto =
			new TransportCryptoImpl(crypto);
	private final TransportId transportId = getTransportId();
	private final SecretKey rootKey = getSecretKey();

	@Test
	public void testKeysAreDistinct() {
		TransportKeys kA = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys kB = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, false, true);
		assertAllDifferent(kA);
		assertAllDifferent(kB);
	}

	@Test
	public void testKeysAreNotRotatedToPreviousPeriod() {
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.rotateTransportKeys(k, 122);
		assertSame(k, k1);
	}

	@Test
	public void testKeysAreNotRotatedToCurrentPeriod() {
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.rotateTransportKeys(k, 123);
		assertSame(k, k1);
	}

	@Test
	public void testKeysAreRotatedByOnePeriod() {
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.rotateTransportKeys(k, 124);
		assertSame(k.getCurrentIncomingKeys(), k1.getPreviousIncomingKeys());
		assertSame(k.getNextIncomingKeys(), k1.getCurrentIncomingKeys());
	}

	@Test
	public void testKeysAreRotatedByTwoPeriods() {
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.rotateTransportKeys(k, 125);
		assertSame(k.getNextIncomingKeys(), k1.getPreviousIncomingKeys());
	}

	@Test
	public void testKeysAreRotatedByThreePeriods() {
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.rotateTransportKeys(k, 126);
		assertAllDifferent(k, k1);
	}

	@Test
	public void testCurrentKeysMatchContact() {
		// Start in time period 123
		TransportKeys kA = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys kB = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, false, true);
		// Alice's incoming keys should equal Bob's outgoing keys
		assertMatches(kA.getCurrentIncomingKeys(), kB.getCurrentOutgoingKeys());
		// Bob's incoming keys should equal Alice's outgoing keys
		assertMatches(kB.getCurrentIncomingKeys(), kA.getCurrentOutgoingKeys());
		// Rotate into the future
		kA = transportCrypto.rotateTransportKeys(kA, 456);
		kB = transportCrypto.rotateTransportKeys(kB, 456);
		// Alice's incoming keys should equal Bob's outgoing keys
		assertMatches(kA.getCurrentIncomingKeys(), kB.getCurrentOutgoingKeys());
		// Bob's incoming keys should equal Alice's outgoing keys
		assertMatches(kB.getCurrentIncomingKeys(), kA.getCurrentOutgoingKeys());
	}

	@Test
	public void testPreviousKeysMatchContact() {
		// Start in time period 123
		TransportKeys kA = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys kB = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, false, true);
		// Compare Alice's previous keys in period 456 with Bob's current keys
		// in period 455
		kA = transportCrypto.rotateTransportKeys(kA, 456);
		kB = transportCrypto.rotateTransportKeys(kB, 455);
		// Alice's previous incoming keys should equal Bob's current
		// outgoing keys
		assertMatches(kA.getPreviousIncomingKeys(),
				kB.getCurrentOutgoingKeys());
		// Compare Alice's current keys in period 456 with Bob's previous keys
		// in period 457
		kB = transportCrypto.rotateTransportKeys(kB, 457);
		// Bob's previous incoming keys should equal Alice's current
		// outgoing keys
		assertMatches(kB.getPreviousIncomingKeys(),
				kA.getCurrentOutgoingKeys());
	}

	@Test
	public void testNextKeysMatchContact() {
		// Start in time period 123
		TransportKeys kA = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys kB = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, false, true);
		// Compare Alice's current keys in period 456 with Bob's next keys in
		// period 455
		kA = transportCrypto.rotateTransportKeys(kA, 456);
		kB = transportCrypto.rotateTransportKeys(kB, 455);
		// Bob's next incoming keys should equal Alice's current outgoing keys
		assertMatches(kB.getNextIncomingKeys(), kA.getCurrentOutgoingKeys());
		// Compare Alice's next keys in period 456 with Bob's current keys
		// in period 457
		kB = transportCrypto.rotateTransportKeys(kB, 457);
		// Alice's next incoming keys should equal Bob's current outgoing keys
		assertMatches(kA.getNextIncomingKeys(), kB.getCurrentOutgoingKeys());
	}

	@Test
	public void testRootKeyAffectsOutput() {
		SecretKey rootKey1 = getSecretKey();
		assertFalse(Arrays.equals(rootKey.getBytes(), rootKey1.getBytes()));
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.deriveTransportKeys(transportId,
				rootKey1, 123, true, true);
		assertAllDifferent(k, k1);
	}

	@Test
	public void testTransportIdAffectsOutput() {
		TransportId transportId1 = getTransportId();
		assertNotEquals(transportId.getString(), transportId1.getString());
		TransportKeys k = transportCrypto.deriveTransportKeys(transportId,
				rootKey, 123, true, true);
		TransportKeys k1 = transportCrypto.deriveTransportKeys(transportId1,
				rootKey, 123, true, true);
		assertAllDifferent(k, k1);
	}
}