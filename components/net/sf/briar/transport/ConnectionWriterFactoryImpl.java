package net.sf.briar.transport;

import java.io.OutputStream;

import javax.crypto.Cipher;

import net.sf.briar.api.crypto.CryptoComponent;
import net.sf.briar.api.crypto.ErasableKey;
import net.sf.briar.api.crypto.IvEncoder;
import net.sf.briar.api.transport.ConnectionWriter;
import net.sf.briar.api.transport.ConnectionWriterFactory;
import net.sf.briar.util.ByteUtils;

import com.google.inject.Inject;

class ConnectionWriterFactoryImpl implements ConnectionWriterFactory {

	private final CryptoComponent crypto;

	@Inject
	public ConnectionWriterFactoryImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	public ConnectionWriter createConnectionWriter(OutputStream out,
			long capacity, byte[] secret, boolean initiator) {
		// Derive the keys and erase the secret
		ErasableKey tagKey = crypto.deriveTagKey(secret, initiator);
		ErasableKey frameKey = crypto.deriveFrameKey(secret, initiator);
		ByteUtils.erase(secret);
		// Create the writer
		Cipher tagCipher = crypto.getTagCipher();
		Cipher frameCipher = crypto.getFrameCipher();
		IvEncoder frameIvEncoder = crypto.getFrameIvEncoder();
		FrameWriter encryption = new OutgoingEncryptionLayerImpl(
				out, capacity, tagCipher, frameCipher, frameIvEncoder, tagKey,
				frameKey);
		return new ConnectionWriterImpl(encryption);
	}
}
