package org.briarproject.plugins.tor;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.briarproject.api.TransportId;
import org.briarproject.api.lifecycle.ShutdownManager;
import org.briarproject.api.plugins.duplex.DuplexPlugin;
import org.briarproject.api.plugins.duplex.DuplexPluginCallback;
import org.briarproject.api.plugins.duplex.DuplexPluginFactory;
import org.briarproject.api.system.LocationUtils;

import android.content.Context;
import android.os.Build;

public class TorPluginFactory implements DuplexPluginFactory {

	private static final Logger LOG =
			Logger.getLogger(TorPluginFactory.class.getName());

	private static final int MAX_FRAME_LENGTH = 1024;
	private static final long MAX_LATENCY = 60 * 1000; // 1 minute
	private static final long POLLING_INTERVAL = 3 * 60 * 1000; // 3 minutes

	private final Executor pluginExecutor;
	private final Context appContext;
	private final LocationUtils locationUtils;
	private final ShutdownManager shutdownManager;

	public TorPluginFactory(Executor pluginExecutor, Context appContext,
			LocationUtils locationUtils, ShutdownManager shutdownManager) {
		this.pluginExecutor = pluginExecutor;
		this.appContext = appContext;
		this.locationUtils = locationUtils;
		this.shutdownManager = shutdownManager;
	}

	public TransportId getId() {
		return TorPlugin.ID;
	}

	public DuplexPlugin createPlugin(DuplexPluginCallback callback) {
		// Check that we have a Tor binary for this architecture
		if(!Build.CPU_ABI.startsWith("armeabi")) {
			LOG.info("Tor is not supported on this architecture");
			return null;
		}
		return new TorPlugin(pluginExecutor,appContext, locationUtils,
				shutdownManager, callback, MAX_FRAME_LENGTH, MAX_LATENCY,
				POLLING_INTERVAL);
	}
}
