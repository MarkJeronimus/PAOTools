package org.digitalmodular.paotools;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

/**
 * @author Zom-B
 */
// Created 2020-11-27
public class PaoTimelapseMakerMain {
	private static final DateTimeFormatter IMAGE_FILENAME_FORMAT = new DateTimeFormatterBuilder()
			.appendLiteral("PAO_")
			.appendValue(YEAR, 4, 4, SignStyle.EXCEEDS_PAD)
			.appendLiteral('-')
			.appendValue(MONTH_OF_YEAR, 2)
			.appendLiteral('-')
			.appendValue(DAY_OF_MONTH, 2)
			.appendLiteral('_')
			.appendValue(HOUR_OF_DAY, 2)
			.appendLiteral('.')
			.appendValue(MINUTE_OF_HOUR, 2)
			.appendLiteral('.')
			.appendValue(SECOND_OF_MINUTE, 2)
			.appendLiteral(".png")
			.toFormatter();

	public static final int[] DELTA_TREE_INTERVALS = {1, 4, 16, 64};

	private static final Filter<Path> IMAGE_FILENAME_FILTER = file -> {
		if (!Files.isRegularFile(file))
			return false;

		ParsePosition position = new ParsePosition(0);
		IMAGE_FILENAME_FORMAT.parseUnresolved(file.getFileName().toString(), position);
		return position.getErrorIndex() < 0;
	};

	private static final Path CURRENT_DIR = Paths.get(".");

	public static void main(String... args) throws IOException {
		long last = 0;
		while (true) {
			long time = sleepUntilNextTic(last);
			last = time;

			int layer = getLayer(time);

			System.out.println(LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC) + "\t" + layer);
		}
	}

	public static long sleepUntilNextTic(long last) {
		while (true) {
			LocalDateTime now  = LocalDateTime.now();
			long          time = now.toEpochSecond(ZoneOffset.UTC);
			if (time > last && time % DELTA_TREE_INTERVALS[0] == 0)
				return time;

			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static int getLayer(long now) {
		for (int i = DELTA_TREE_INTERVALS.length - 1; i >= 0; i--) {
			int step = DELTA_TREE_INTERVALS[i];
			if (now % step == 0)
				return i;
		}

		throw new AssertionError(now);
	}

	private static PaoFile[] findFiles() throws IOException {
		for (Path file : Files.newDirectoryStream(CURRENT_DIR, IMAGE_FILENAME_FILTER)) {
			LocalDateTime time = LocalDateTime.parse(file.getFileName().toString(), IMAGE_FILENAME_FORMAT);

		}
		return null;
	}

	private static Path downloadImage(LocalDateTime time) throws IOException {
		URL  url      = new URL("https://pixelanarchy.online/2.png");
		Path filename = Paths.get(IMAGE_FILENAME_FORMAT.format(time));

		Requests.TIMEOUT = 15_000;
		Requests.downloadToFile(url, filename);
		return filename;
	}

	static class PaoFile {
		Path file;

	}
}
