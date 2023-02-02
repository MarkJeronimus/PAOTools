package org.digitalmodular.paotools.ditherer;

/**
 * @author Zom-B
 */
// Created 2020-11-14
public class VectorID {
	int    first;
	double second;

	public VectorID(int first, double second) {
		this.first = first;
		this.second = second;
	}

	public int first()     { return first; }

	public double second() { return second; }
}
