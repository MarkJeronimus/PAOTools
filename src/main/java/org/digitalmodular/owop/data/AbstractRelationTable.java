package org.digitalmodular.owop.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireThat;

/**
 * @param <V> (from Value) Type of the 'relation' object.
 * @param <L> (from Left) Type of the first of the two foreign keys that this table links together.
 * @param <R> (from Right) Type of the second of the two foreign keys that this table links together.
 * @author Mark Jeronimus
 */
// Created 2022-02-02
public abstract class AbstractRelationTable<V, L, R> extends AbstractTable<V> {
	private final PreparedStatement selectStatementLeftRight;
	private final PreparedStatement selectStatementLeft;
	private final PreparedStatement selectStatementRight;
	private final PreparedStatement deleteStatementLeft;
	private final PreparedStatement deleteStatementRight;
	private final PreparedStatement deleteStatementRelation;

	@SuppressWarnings({"AbstractMethodCallInConstructor", "JDBCPrepareStatementWithNonConstantString",
	                   "OverridableMethodCallDuringObjectConstruction", "OverriddenMethodCallDuringObjectConstruction"})
	protected AbstractRelationTable(Connection connection, String tableName) throws SQLException {
		super(connection, tableName);

		selectStatementLeftRight = connection.prepareStatement("SELECT * FROM " + getTableName() + " WHERE " +
		                                                       getLeftKeyName() + "=? and " + getRightKeyName() + "=?");
		selectStatementLeftRight.setQueryTimeout(Database.QUERY_TIMEOUT);
		selectStatementLeft = connection.prepareStatement("SELECT * FROM " + getTableName() + " WHERE " +
		                                                  getLeftKeyName() + "=?");
		selectStatementLeft.setQueryTimeout(Database.QUERY_TIMEOUT);
		selectStatementRight = connection.prepareStatement("SELECT * FROM " + getTableName() + " WHERE " +
		                                                   getRightKeyName() + "=?");
		selectStatementRight.setQueryTimeout(Database.QUERY_TIMEOUT);
		deleteStatementLeft = connection.prepareStatement("DELETE FROM " + getTableName() + " WHERE " +
		                                                  getLeftKeyName() + "=?");
		deleteStatementLeft.setQueryTimeout(Database.QUERY_TIMEOUT);
		deleteStatementRight = connection.prepareStatement("DELETE FROM " + getTableName() + " WHERE " +
		                                                   getRightKeyName() + "=?");
		deleteStatementRight.setQueryTimeout(Database.QUERY_TIMEOUT);
		deleteStatementRelation = connection.prepareStatement("DELETE FROM " + getTableName() + " WHERE " +
		                                                      getLeftKeyName() + "=? AND " + getRightKeyName() + "=?");
		deleteStatementRelation.setQueryTimeout(Database.QUERY_TIMEOUT);
	}

	protected abstract String getValueObjectName(boolean plural);

	protected abstract String getLeftKeyName();

	protected abstract String getRightKeyName();

	protected abstract L extractLeftKey(V value);

	protected abstract R extractRightKey(V value);

	protected @Nullable V getRelation(L leftKey, R rightKey) throws SQLException {
		return getTableEntry(selectStatementLeftRight, leftKey, rightKey);
	}

	protected List<V> getRelationsForLeftKey(L leftKey) throws SQLException {
		return getTableEntries(selectStatementLeft, leftKey);
	}

	protected List<V> getRelationsForRightKey(R rightKey) throws SQLException {
		return getTableEntries(selectStatementRight, rightKey);
	}

	protected void removeRelationsWithLeftKey(L leftKey) throws SQLException {
		deleteStatementLeft.setObject(1, leftKey);
		deleteStatementLeft.executeUpdate();
	}

	protected void removeRelationsWithRightKey(R rightKey) throws SQLException {
		deleteStatementRight.setObject(1, rightKey);
		deleteStatementRight.executeUpdate();
	}

	protected void removeRelation(L leftKey, R rightKey) throws SQLException {
		deleteStatementRelation.setObject(1, leftKey);
		deleteStatementRelation.setObject(2, rightKey);
		deleteStatementRelation.executeUpdate();
	}

	public void replaceValuesWithLeftKey(L leftKey, List<V> values)
			throws SQLException {
		for (int i = 0; i < values.size(); i++) {
			V   value = values.get(i);
			int index = i;
			requireThat(extractLeftKey(value).equals(leftKey), () ->
					getValueObjectName(true) + '[' + index + "]." + getLeftKeyName() + " != " +
					getLeftKeyName() + ": " + extractLeftKey(value) + " vs " + leftKey);
		}

		Set<R> rightKeys = values.stream()
		                         .map(this::extractRightKey)
		                         .collect(Collectors.toUnmodifiableSet());

		List<V> existingValues = getRelationsForLeftKey(leftKey);

		Set<R> existingRightKeys = existingValues.stream()
		                                         .map(this::extractRightKey)
		                                         .collect(Collectors.toUnmodifiableSet());

		Collection<R> toAdd = new HashSet<>(rightKeys);
		toAdd.removeAll(existingRightKeys);

		Collection<R> toRemove = new ArrayList<>(existingRightKeys);
		toRemove.removeAll(rightKeys);

		if (toAdd.isEmpty()) {
			if (!toRemove.isEmpty()) {
				removeRelationsWithLeftKey(leftKey);
			}
		} else {
			for (R rightKey : toRemove) {
				removeRelation(leftKey, rightKey);
			}

			for (V value : values) {
				if (toAdd.contains(extractRightKey(value))) {
					addValue(value);
				}
			}
		}
	}

	public void replaceValuesWithRightKey(R rightKey, List<V> values)
			throws SQLException {
		for (int i = 0; i < values.size(); i++) {
			V   value = values.get(i);
			int index = i;
			requireThat(extractRightKey(value).equals(rightKey), () ->
					getValueObjectName(true) + '[' + index + "]." + getRightKeyName() + " != " +
					getRightKeyName() + ": " + extractRightKey(value) + " vs " + rightKey);
		}

		Set<L> leftKeys = values.stream()
		                        .map(this::extractLeftKey)
		                        .collect(Collectors.toUnmodifiableSet());

		List<V> existingValues = getRelationsForRightKey(rightKey);

		Set<L> existingLeftKeys = existingValues.stream()
		                                        .map(this::extractLeftKey)
		                                        .collect(Collectors.toUnmodifiableSet());

		Collection<L> toAdd = new HashSet<>(leftKeys);
		toAdd.removeAll(existingLeftKeys);

		Collection<L> toRemove = new ArrayList<>(existingLeftKeys);
		toRemove.removeAll(leftKeys);

		if (toAdd.isEmpty()) {
			if (!toRemove.isEmpty()) {
				removeRelationsWithRightKey(rightKey);
			}
		} else {
			for (L leftKey : toRemove) {
				removeRelation(leftKey, rightKey);
			}

			for (V value : values) {
				if (toAdd.contains(extractLeftKey(value))) {
					addValue(value);
				}
			}
		}
	}
}
