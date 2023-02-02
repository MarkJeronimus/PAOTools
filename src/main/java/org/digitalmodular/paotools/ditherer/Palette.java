package org.digitalmodular.paotools.ditherer;

/**
 * @author Zom-B
 */
// Created 2020-11-09
public class Palette {
	public Object[] Combinations;
	public KDTree CombinationTree;

//	public VectorID FindClosestCombinationIndex(ColorInfo test_lab) {
//		switch (Globals.ColorComparing) {
//			case Compare_RGB: {
//				Vectort4i q=new Vectort4i(test_lab.R, test_lab.G, test_lab.B, test_lab.A);
//				return CombinationTree.nearest_info(q);
//			}
//			case Compare_CIE76_DeltaE: {
//				Vectort4i q=new Vectort4i(test_lab.lab.L, test_lab.lab.a, test_lab.lab.b, test_lab.A );
//				return CombinationTree.nearest_info(q);
//			}
//			default: {
//				VectorID result=new VectorID(0, -1.0);
//				for (int limit = Combinations.length, index = 0; index < limit; ++index) {
//					double penalty = ColorCompare(test_lab, GetCombinationMeta(index));
//					if (penalty < result.second || result.second == -1.0) {
//						result.first = index;
//						result.second = penalty;
//					}
//				}
//				return result;
//			}
//		}
//	}
}
