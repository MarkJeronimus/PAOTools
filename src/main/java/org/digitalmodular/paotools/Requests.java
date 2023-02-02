package org.digitalmodular.paotools;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import static java.util.logging.Level.WARNING;

import org.digitalmodular.paotools.common.HTTPResponseStream;

/**
 * @author Zom-B
 */
// Created 2020-11-28
public final class Requests {
	private Requests() { throw new AssertionError(); }

	/** Similar to: {@code "Mozilla/5.0 (compatible; Java/1.8.0_144)"}. */
	private static final String USER_AGENT =
			"Mozilla/5.0 (compatible; Java/" + System.getProperty("java.version") + ')';

	public static int TIMEOUT = 4_000;

	public static void downloadToFile(URL url, Path file) throws IOException {
		Logger.getGlobal().info("Downloading: " + file + " from " + url);

		try (HTTPResponseStream stream = openConnection(url);
		     OutputStream out = Files.newOutputStream(file)) {
			if (stream.getResponseCode() == 302) {
				List<String> locationHeaderResponse = stream.getResponseHeaders().get("Location");
				if (locationHeaderResponse == null)
					throw new IOException(
							"\"HTTP/302 Found\" without \"Location\" response header: " + stream.getResponseHeaders());

				String newLocation = locationHeaderResponse.get(0);
				URL    redirect    = new URL(url, newLocation);

				downloadToFile(redirect, file);
				return;
			}

			if (stream.getResponseCode() / 100 != 2)
				throw new IOException("Received " + stream.getResponseHeaders().get(null) + " for " + url);

			stream.transferTo(out);
		} catch (IOException ex) {
			if (Files.exists(file)) {
				Logger.getGlobal().log(WARNING, "Removing partially downloaded file because of exception");
				Files.delete(file);
			}

			throw ex;
		}
	}

	/**
	 * Requests a URL and if successful (response 200), returns the open stream. Closing this stream closes the
	 * connection.
	 */
	public static HTTPResponseStream openConnection(URL url) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)url.openConnection();
			connection.addRequestProperty("Host", url.getHost());
			connection.addRequestProperty("User-Agent", USER_AGENT);
			connection.addRequestProperty("Accept", "*/*");
			connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
			connection.addRequestProperty("Connection", "close");

			connection.setInstanceFollowRedirects(false);

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);

			int responseCode = connection.getResponseCode();
			if (responseCode >= 400)
				throw new EOFException("HTTP response code " + connection.getResponseCode());

			InputStream in;
			try {
				in = connection.getInputStream();
			} catch (IOException ex) {
				in = connection.getErrorStream();
				if (in == null)
					throw ex;
			}

			in = Requests.getDecompressedStream(connection, in);

			int                       contentLength   = Requests.attemptGetStreamLength(connection);
			Map<String, List<String>> responseHeaders = connection.getHeaderFields();

			HTTPResponseStream stream = new HTTPResponseStream(url, in, responseCode, responseHeaders, contentLength);

			return stream;
		} catch (IOException ex) {
			if (connection != null)
				connection.disconnect();

			throw ex;
		}
	}

	private static InputStream getDecompressedStream(URLConnection connection, InputStream in) throws IOException {
		if ("gzip".equals(connection.getHeaderField("Content-Encoding")))
			in = new GZIPInputStream(in);
		else if ("deflate".equals(connection.getHeaderField("Content-Encoding")))
			in = new DeflaterInputStream(in);
		return in;
	}

	private static int attemptGetStreamLength(URLConnection connection) {
		try {
			return Integer.parseInt(connection.getHeaderField("Content-Length"));
		} catch (NumberFormatException ignored) {
			return -1;
		}
	}
}
