package org.digitalmodular.owop.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;

import org.digitalmodular.utilities.FileUtilities;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * @author Zom-G
 */
// Created 2022-02-02
@SuppressWarnings("PublicField")
public class Database {
	public static final int QUERY_TIMEOUT = 1;

	public static final Path DATABASE_FILE_BAK  = Paths.get("OWOP.bak.db");
	public static final Path DATABASE_FILE_BAK2 = Paths.get("OWOP.bak2.db");
	public static final Path DATABASE_FILE_NEW  = Paths.get("OWOP (copy).db");

	private final Connection connection;

	public final PixelChangeTable pixelChangeTable;

	public Database(Path file) throws IOException, SQLException {
		cycleBackups(file);

		SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(false);
		SQLiteDataSource dataSource = new SQLiteDataSource(config);
		dataSource.setUrl("jdbc:sqlite:" + file);
		connection = dataSource.getConnection();
		connection.setAutoCommit(false);

		try {
			PixelChangeTable.createTable(connection);

			pixelChangeTable = new PixelChangeTable(connection);
		} finally {
			connection.commit();
		}
	}

	private static void cycleBackups(Path file) throws IOException {
		file = file.toAbsolutePath();

		Path     dir   = file.getParent();
		String[] parts = FileUtilities.splitDirFileExt(file.toString());

		Path bak1 = dir.resolve(parts[1] + ".bak." + parts[2]);
		Path bak2 = dir.resolve(parts[1] + ".bak2." + parts[2]);

		if (Files.exists(file)) {
			if (Files.exists(bak1)) {
				try {
					Files.delete(bak2);
				} catch (NoSuchFileException ignored) {
				}

				Files.move(bak1, bak2, StandardCopyOption.ATOMIC_MOVE);
			}

			Files.copy(file, bak1);
		}
	}

	public void commit() throws SQLException {
		connection.commit();
	}
}
