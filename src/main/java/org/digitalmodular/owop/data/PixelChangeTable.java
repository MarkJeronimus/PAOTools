package org.digitalmodular.owop.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-02-27
public final class PixelChangeTable extends AbstractTable<PixelChange> {
	public static final String TABLE_NAME = "PixelChange";

	private final PreparedStatement insertStatement;
	private final PreparedStatement selectStatementMaxTimestamp;

	private long lastTimestamp;

	public static void createTable(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.setQueryTimeout(Database.QUERY_TIMEOUT);

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS PixelChange (" +
			                        "timestamp INTEGER PRIMARY KEY," +
			                        "x         INTEGER NOT NULL," +
			                        "y         INTEGER NOT NULL," +
			                        "color     INTEGER NOT NULL," +
			                        "id        INTEGER NOT NULL)");
		}
	}

	public PixelChangeTable(Connection connection) throws SQLException {
		super(connection, TABLE_NAME);
		insertStatement = connection.prepareStatement("REPLACE INTO PixelChange VALUES (?,?,?,?,?)");
		insertStatement.setQueryTimeout(Database.QUERY_TIMEOUT);
		selectStatementMaxTimestamp = connection.prepareStatement("SELECT MAX(timestamp) FROM PixelChange");
		selectStatementMaxTimestamp.setQueryTimeout(Database.QUERY_TIMEOUT);

		lastTimestamp = getMaxTimestamp();
	}

	@Override
	protected PixelChange constructValue(ResultSet resultSet) throws SQLException {
		long timestamp = resultSet.getLong(1);
		int  x         = resultSet.getInt(2);
		int  y         = resultSet.getInt(3);
		int  color     = resultSet.getInt(4);
		int  id        = resultSet.getInt(5);
		return new PixelChange(x, y, color, id, timestamp);
	}

	@Override
	protected void addValue(PixelChange value) throws SQLException {
		long timestamp = value.timestamp();
		if (timestamp <= lastTimestamp) {
			timestamp = lastTimestamp + 1;
		}

		insertStatement.setObject(1, timestamp);
		insertStatement.setObject(2, value.x());
		insertStatement.setObject(3, value.y());
		insertStatement.setObject(4, value.color());
		insertStatement.setObject(5, value.id());
		insertStatement.executeUpdate();

		lastTimestamp = timestamp;
	}

	public void addPixelChange(PixelChange chunkMetadata) throws SQLException {
		requireNonNull(chunkMetadata, "chunkMetadata");

		addValue(chunkMetadata);
	}

	public long getMaxTimestamp() throws SQLException {
		@Nullable Long maxTimestamp = getLong(selectStatementMaxTimestamp);
		return maxTimestamp == null ? System.currentTimeMillis() - 1 : maxTimestamp.intValue();
	}
}
