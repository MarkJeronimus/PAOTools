/*
 * This file is part of PAO.
 *
 * Copyleft 2023 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalmodular.paotools.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An {@link ArrayList} replacement that doesn't allow duplicates and has O(1) time complexity for
 * {@link #contains(Object)}.
 *
 * @author Mark Jeronimus
 * @param <E> the type of elements in this collection
 */
// Created 2020-10-25
public class HashArrayList<E> implements Collection<E>, Iterable<E> {
	private final List<E>   list;
	private final Map<E, E> map;

	public HashArrayList(int initialCapacity) {
		list = new ArrayList<>(initialCapacity);
		map = new HashMap<>(initialCapacity);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public void clear() {
		list.clear();
		map.clear();
	}

	@Override
	public boolean add(E t) {
		if (map.containsKey(t))
			return false;

		map.put(t, t);
		list.add(t);
		return true;
	}

	public E get(int index) {
		return list.get(index);
	}

	public @Nullable E get(Object o) {
		return map.get(o);
	}

	public @Nullable E remove(int index) {
		E removed = list.remove(index);
		map.remove(removed);
		return removed;
	}

	@Override
	public boolean remove(Object o) {
		@Nullable E oldValue = map.remove(o);
		if (oldValue != null)
			list.remove(o);
		return oldValue != null;
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public void sort(Comparator<E> comparator) {
		list.sort(comparator);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public Stream<E> stream() {
		return list.stream();
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("retainAll");
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		throw new UnsupportedOperationException("retainAll");
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("retainAll");
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("retainAll");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof HashArrayList))
			return false;

		HashArrayList<?> other = (HashArrayList<?>)o;
		return list.equals(other.list);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public String toString() {
		return list.toString();
	}
}
