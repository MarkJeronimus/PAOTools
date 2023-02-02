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

package org.digitalmodular.gis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.digitalmodular.utilities.NumberUtilities;
import org.digitalmodular.utilities.graphics.GraphicsUtilities;
import org.digitalmodular.utilities.graphics.color.Color3fConst;
import org.digitalmodular.utilities.graphics.svg.core.SVGElement;
import org.digitalmodular.utilities.graphics.svg.core.SVGFillRule;
import org.digitalmodular.utilities.graphics.svg.core.SVGLineCap;
import org.digitalmodular.utilities.graphics.svg.core.SVGLineJoin;
import org.digitalmodular.utilities.graphics.svg.core.fill.SVGNoFill;
import org.digitalmodular.utilities.graphics.svg.core.fill.SVGSolidColor;
import org.digitalmodular.utilities.graphics.svg.element.SVGDrawing;
import org.digitalmodular.utilities.graphics.svg.element.SVGLayer;
import org.digitalmodular.utilities.graphics.svg.element.SVGPath;
import org.digitalmodular.utilities.graphics.svg.element.SVGRect;
import org.digitalmodular.utilities.math.interpolation.XYCubicSpline;
import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * @author zom-b
 */
// Created 2023-01-22
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class WorldMapBorderMakerMain extends JPanel implements MouseMotionListener {
	private static final int SVG_WIDTH  = 2576;
	private static final int SVG_HEIGHT = 1440;

	private static int   worldWidth  = 2560;
	private static int   worldHeight = 1440;
	private        int   limit       = Integer.MAX_VALUE;
	private static float slope1      = 1.5f;
	private static float slope2      = 1.5f;

	private static final double[]      latitudeDeg = {83.7, 65.0, 35.0, -35.0, -65.0, -83.7}; // 83.63339
	private static final double[]      latitudeY   = {-720.0, -600.0, -250.0, 250.0, 600.0, 720.0};
	private static       XYCubicSpline latitudeMap = new XYCubicSpline(latitudeDeg, latitudeY, Double.NaN, Double.NaN);

//	private static final String shorelinesFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/gshhs_c.b";
//	private static final String bordersFilename    = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_borders_c.b";
//	private static final String riversFilename     = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_rivers_c.b";
//	private static final double LINE_WIDTH         = 1.0;

//	private static final String shorelinesFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/gshhs_l.b";
//	private static final String bordersFilename    = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_borders_l.b";
//	private static final String riversFilename     = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_rivers_l.b";
//	private static final double LINE_WIDTH         = 0.5;

//	private static final String shorelinesFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/gshhs_i.b";
//	private static final String bordersFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_borders_i.b";
//	private static final String riversFilename  = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_rivers_i.b";
//	private static final double LINE_WIDTH      = 1.2;

	private static final String shorelinesFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/gshhs_h.b";
	private static final String bordersFilename    = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_borders_h.b";
	private static final String riversFilename     = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_rivers_h.b";
	private static final double LINE_WIDTH         = 0.1;

//	private static final String shorelinesFilename = "/home/zom-b/Downloads/gshhg-bin-2.3.7/gshhs_f.b";
//	private static final String bordersFilename    = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_borders_f.b";
//	private static final String riversFilename     = "/home/zom-b/Downloads/gshhg-bin-2.3.7/wdb_rivers_f.b";
//	private static final double LINE_WIDTH         = 0.05;

	private static final int MODIFIERS_MASK = InputEvent.SHIFT_DOWN_MASK |
	                                          InputEvent.CTRL_DOWN_MASK |
	                                          InputEvent.ALT_DOWN_MASK |
	                                          InputEvent.META_DOWN_MASK;

	private final List<Polygon> shorelines;
	private final List<Polygon> borders;
	private final List<Polygon> rivers;

	public static void main(String... args) throws IOException {
		List<Polygon> shorelines = readGisFile(Paths.get(shorelinesFilename));
		List<Polygon> borders    = readGisFile(Paths.get(bordersFilename));
		List<Polygon> rivers     = readGisFile(Paths.get(riversFilename));

		System.out.println("Shorelines: " + shorelines.size());
		System.out.println("Borders: " + borders.size());
		System.out.println("Rivers: " + rivers.size());

		saveSVG(shorelines, borders, rivers);
		showGUI(shorelines, borders, rivers);
	}

	private static List<Polygon> transformPolygons(Collection<Polygon> polygons) {
		List<Polygon> transformed = new ArrayList<>(polygons.size());

		int n = 0;

		for (Polygon polygon : polygons) {
			transformed.add(transformPolygons(polygon));

			n += polygon.getPoints().size();
		}

		return transformed;
	}

	private static Polygon transformPolygons(Polygon polygon) {
		GSHHG         header = polygon.getHeader();
		List<Point2D> points = polygon.getPoints();
		int           id     = header.getId();
		int           level  = header.getLevel();

		List<Point2D> transformed = new ArrayList<>(points.size());

		boolean isAntarcticaLand = level == 5 && id == 4;
		boolean isAntarcticaIce  = level == 6 && id == 5;

		double wrapPoint = 0;
		if (id == 0) {
			wrapPoint = 50.0; // Russia
		} else if (id == 125) {
			wrapPoint = -50.0; // America
		}

		for (int i = 0; i < points.size(); i++) {
			Point2D point = points.get(i);
			double  x     = NumberUtilities.floorMod(point.getX() + 171.0 - wrapPoint, 360.0) + wrapPoint;
			x = x / 360.0 * worldWidth + 6.5;

			double y = latitudeMap.applyAsDouble(point.getY()) + worldHeight * 0.5;

			transformed.add(new Point2D.Double(x, y));

			if ((isAntarcticaLand && i == 26128) || (isAntarcticaIce && i == 36228)) {
				transformed.add(new Point2D.Double(0, y));
				transformed.add(new Point2D.Double(0, SVG_HEIGHT));
				transformed.add(new Point2D.Double(SVG_WIDTH, SVG_HEIGHT));
				transformed.add(new Point2D.Double(SVG_WIDTH, y));
//				System.out.println(i + "\t" + point + "\t" + x + ", " + y);
//			} else if ((isAntarcticaLand && i == 26129) || (isAntarcticaIce && i == 36229)) {
//				System.out.println(i + "\t" + point + "\t" + x + ", " + y);
			}
		}

		return new Polygon(header, transformed);
	}

	private static void showGUI(List<Polygon> shorelines, List<Polygon> borders, List<Polygon> rivers) {
		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new WorldMapBorderMakerMain(shorelines, borders, rivers));

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	private static List<Polygon> readGisFile(Path file) throws IOException {
		List<Polygon> polygons = new ArrayList<>(188612);

		try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
			do {
				GSHHG header = readHeader(in);

				List<Point2D> path = new ArrayList<>(header.getN());

				for (int i = 0; i < header.getN(); i++) {
					path.add(readPoint(in));
				}

				polygons.add(new Polygon(header, path));
			} while (in.available() > 0);
		}

//		polygons.sort(Comparator.<Polygon>comparingInt(polygon -> polygon.getHeader().getLevel()).thenComparing(
//				Comparator.<Polygon>comparingInt(polygon -> polygon.getPoints().size()).reversed()));
//		polygons.sort(Comparator.<Polygon>comparingInt(polygon -> polygon.getHeader().getLevel()).thenComparing(
//				Comparator.<Polygon>comparingDouble(polygon -> polygon.getPoints().get(0).getY()).reversed()));

		System.out.println("Loaded polygons (initial capacity): " + polygons.size());
		return polygons;
	}

	private static GSHHG readHeader(DataInput in) throws IOException {
		int    id        = in.readInt();
		int    n         = in.readInt();
		int    flag      = in.readInt();
		double west      = in.readInt() / 1000000.0;
		double east      = in.readInt() / 1000000.0;
		double south     = in.readInt() / 1000000.0;
		double north     = in.readInt() / 1000000.0;
		int    area      = in.readInt();
		int    areaFull  = in.readInt();
		int    container = in.readInt();
		int    ancestor  = in.readInt();
		return new GSHHG(id,
		                 n,
		                 flag,
		                 west,
		                 east,
		                 south,
		                 north,
		                 area,
		                 areaFull,
		                 container,
		                 ancestor);
	}

	private static Point2D readPoint(DataInput in) throws IOException {
		double x = in.readInt() / 1000000.0;
		double y = in.readInt() / 1000000.0;
		return new Point2D.Double(x, y);
	}

	private static void saveSVG(Collection<Polygon> shorelines, List<Polygon> borders, List<Polygon> rivers)
			throws IOException {
		SVGDrawing svg = new SVGDrawing(4);
		svg.setDimension(new Rectangle2D.Double(0, 0, worldWidth + 16, SVG_HEIGHT));

		SVGElement bg = new SVGRect(0, 0, SVG_WIDTH, SVG_HEIGHT);
		bg.getAttributes().setFill(Color.WHITE);
		svg.add(bg);

		SVGLayer shorelinesLayer = new SVGLayer("shorelines", 6);
		SVGLayer bordersLayer    = new SVGLayer("borders", 3);
		SVGLayer riversLayer     = new SVGLayer("rivers", 14);
		svg.add(shorelinesLayer);
		svg.add(riversLayer);
		svg.add(bordersLayer);

		SVGPath land = new SVGPath(9453074);
		land.getAttributes().setID("land");
		land.getAttributes().setFill(Color.BLACK);
		land.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
//		land.getAttributes().setVisible(false);
		shorelinesLayer.add(land);

		SVGPath lakes = new SVGPath(717339);
		lakes.getAttributes().setID("lakes");
		lakes.getAttributes().setFill(SVGSolidColor.WHITE);
		lakes.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		shorelinesLayer.add(lakes);

		SVGPath islandsInLakes = new SVGPath(55821);
		islandsInLakes.getAttributes().setFill(Color.BLACK);
		islandsInLakes.getAttributes().setID("islandsInLakes");
		islandsInLakes.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		shorelinesLayer.add(islandsInLakes);

		SVGPath pondsInIslandsInLakes = new SVGPath(569);
		pondsInIslandsInLakes.getAttributes().setID("pondsInIslandsInLakes");
		pondsInIslandsInLakes.getAttributes().setFill(SVGSolidColor.WHITE);
		pondsInIslandsInLakes.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		shorelinesLayer.add(pondsInIslandsInLakes);

		SVGPath antarcticaIce = new SVGPath(243351);
		antarcticaIce.getAttributes().setID("antarcticaIce");
		antarcticaIce.getAttributes().setFill(SVGSolidColor.BLACK);
		antarcticaIce.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		shorelinesLayer.add(antarcticaIce);

		SVGPath antarcticaLand = new SVGPath(468653);
		antarcticaLand.getAttributes().setID("antarcticaLand");
		antarcticaLand.getAttributes().setFill(SVGNoFill.INSTANCE);
		antarcticaLand.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		antarcticaLand.getAttributes().setStroke(Color.CYAN);
		antarcticaLand.getAttributes().setStrokeWidth(LINE_WIDTH);
		antarcticaLand.getAttributes().setLineCap(SVGLineCap.ROUND);
		antarcticaLand.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		antarcticaLand.getAttributes().setVisible(false);
		shorelinesLayer.add(antarcticaLand);

		SVGPath internationalBorders = new SVGPath(384462);
		internationalBorders.getAttributes().setID("National boundaries");
		internationalBorders.getAttributes().setFill(SVGNoFill.INSTANCE);
		internationalBorders.getAttributes().setStroke(Color.RED);
		internationalBorders.getAttributes().setStrokeWidth(LINE_WIDTH);
		internationalBorders.getAttributes().setLineCap(SVGLineCap.ROUND);
		internationalBorders.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		bordersLayer.add(internationalBorders);

		SVGPath secondaryBorders = new SVGPath(342740);
		secondaryBorders.getAttributes().setID("State boundaries within the Americas");
		secondaryBorders.getAttributes().setFill(SVGNoFill.INSTANCE);
		secondaryBorders.getAttributes().setStroke(Color.GREEN);
		secondaryBorders.getAttributes().setStrokeWidth(LINE_WIDTH);
		secondaryBorders.getAttributes().setLineCap(SVGLineCap.ROUND);
		secondaryBorders.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		bordersLayer.add(secondaryBorders);

		SVGPath maritimeBorders = new SVGPath(24211);
		maritimeBorders.getAttributes().setID("Marine boundaries");
		maritimeBorders.getAttributes().setFill(SVGNoFill.INSTANCE);
		maritimeBorders.getAttributes().setStroke(Color.BLUE);
		maritimeBorders.getAttributes().setStrokeWidth(LINE_WIDTH);
		maritimeBorders.getAttributes().setLineCap(SVGLineCap.ROUND);
		maritimeBorders.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		maritimeBorders.getAttributes().setVisible(false);
		bordersLayer.add(maritimeBorders);

		SVGPath riverLakes = new SVGPath(116647);
		riverLakes.getAttributes().setID("Double-lined rivers (river-lakes)");
		riverLakes.getAttributes().setFill(SVGSolidColor.WHITE);
		riverLakes.getAttributes().setFillRule(SVGFillRule.NONZERO);
		riversLayer.add(riverLakes);

		SVGPath rivers1 = new SVGPath(254679);
		rivers1.getAttributes().setID("0.02 Permanent major rivers");
		rivers1.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers1.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers1.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers1.getAttributes().setStrokeWidth(0.02);
		rivers1.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers1.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		riversLayer.add(rivers1);

		SVGPath rivers2 = new SVGPath(472104);
		rivers2.getAttributes().setID("0.01 Additional major rivers");
		rivers2.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers2.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers2.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers2.getAttributes().setStrokeWidth(0.01);
		rivers2.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers2.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		riversLayer.add(rivers2);

		SVGPath rivers3 = new SVGPath(744411);
		rivers3.getAttributes().setID("0.01 Additional rivers");
		rivers3.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers3.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers3.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers3.getAttributes().setStrokeWidth(0.01);
		rivers3.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers3.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		riversLayer.add(rivers3);

		SVGPath rivers4 = new SVGPath(694853);
		rivers4.getAttributes().setID("0.005 Minor rivers");
		rivers4.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers4.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers4.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers4.getAttributes().setStrokeWidth(0.005);
		rivers4.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers4.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers4.getAttributes().setVisible(false);
		riversLayer.add(rivers4);

		SVGPath rivers5 = new SVGPath(0);
		rivers5.getAttributes().setID("0.005 empty");
		rivers5.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers5.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers5.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers5.getAttributes().setStrokeWidth(0.005);
		rivers5.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers5.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers5.getAttributes().setVisible(false);
		riversLayer.add(rivers5);

		SVGPath rivers6 = new SVGPath(29695);
		rivers6.getAttributes().setID("0.005 Intermittent rivers - additional");
		rivers6.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers6.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers6.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers6.getAttributes().setStrokeWidth(0.005);
		rivers6.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers6.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers6.getAttributes().setVisible(false);
		riversLayer.add(rivers6);

		SVGPath rivers7 = new SVGPath(93586);
		rivers7.getAttributes().setID("0.005 Intermittent rivers - minor");
		rivers7.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers7.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers7.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers7.getAttributes().setStrokeWidth(0.005);
		rivers7.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers7.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers7.getAttributes().setVisible(false);
		riversLayer.add(rivers7);

		SVGPath rivers8 = new SVGPath(84180);
		rivers8.getAttributes().setID("0.005 Major canals");
		rivers8.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers8.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers8.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers8.getAttributes().setStrokeWidth(0.005);
		rivers8.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers8.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers8.getAttributes().setVisible(false);
		riversLayer.add(rivers8);

		SVGPath rivers9 = new SVGPath(0);
		rivers9.getAttributes().setID("0.005 empty");
		rivers9.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers9.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers9.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers9.getAttributes().setStrokeWidth(0.005);
		rivers9.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers9.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers9.getAttributes().setVisible(false);
		riversLayer.add(rivers9);

		SVGPath rivers10 = new SVGPath(9683);
		rivers10.getAttributes().setID("0.005 Irrigation canals");
		rivers10.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers10.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers10.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers10.getAttributes().setStrokeWidth(0.005);
		rivers10.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers10.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers10.getAttributes().setVisible(false);
		riversLayer.add(rivers10);

		SVGPath rivers11 = new SVGPath(8225);
		rivers11.getAttributes().setID("0.005 rivers11");
		rivers11.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers11.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers11.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers11.getAttributes().setStrokeWidth(0.005);
		rivers11.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers11.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers11.getAttributes().setVisible(false);
		riversLayer.add(rivers11);

		SVGPath rivers12 = new SVGPath(0);
		rivers12.getAttributes().setID("0.005 empty");
		rivers12.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers12.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers12.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers12.getAttributes().setStrokeWidth(0.005);
		rivers12.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers12.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers12.getAttributes().setVisible(false);
		riversLayer.add(rivers12);

		SVGPath rivers13 = new SVGPath(21392);
		rivers13.getAttributes().setID("0.005 rivers13");
		rivers13.getAttributes().setFill(SVGNoFill.INSTANCE);
		rivers13.getAttributes().setFillRule(SVGFillRule.EVEN_ODD);
		rivers13.getAttributes().setStroke(SVGSolidColor.WHITE);
		rivers13.getAttributes().setStrokeWidth(0.005);
		rivers13.getAttributes().setLineCap(SVGLineCap.ROUND);
		rivers13.getAttributes().setLineJoin(SVGLineJoin.ROUND);
		rivers13.getAttributes().setVisible(false);
		riversLayer.add(rivers13);

		latitudeMap = new XYCubicSpline(latitudeDeg, latitudeY, Double.NaN, Double.NaN);
		shorelines = transformPolygons(shorelines);
		borders = transformPolygons(borders);
		rivers = transformPolygons(rivers);

		for (Polygon shoreline : shorelines) {
			int level = shoreline.getHeader().getLevel();

			SVGPath path = (SVGPath)shorelinesLayer.get(level - 1);

			addPolygonToPSVGPath(shoreline, path);
		}

		for (Polygon border : borders) {
			int level = border.getHeader().getLevel();

			SVGPath path = (SVGPath)bordersLayer.get(level - 1);

			addPolygonToPSVGPath(border, path);
		}

		for (Polygon river : rivers) {
			int level = river.getHeader().getLevel();

			SVGPath path = (SVGPath)riversLayer.get(level);

			addPolygonToPSVGPath(river, path);
		}

//		System.out.println("land                  size (initial capacity): " + land.size());
//		System.out.println("lakes                 size (initial capacity): " + lakes.size());
//		System.out.println("islandsInLakes        size (initial capacity): " + islandsInLakes.size());
//		System.out.println("pondsInIslandsInLakes size (initial capacity): " + pondsInIslandsInLakes.size());
//		System.out.println("antarcticaIce         size (initial capacity): " + antarcticaIce.size());
//		System.out.println("antarcticaLand        size (initial capacity): " + antarcticaLand.size());
//		System.out.println("internationalBorders  size (initial capacity): " + internationalBorders.size());
//		System.out.println("secondaryBorders      size (initial capacity): " + secondaryBorders.size());
//		System.out.println("maritimeBorders       size (initial capacity): " + maritimeBorders.size());
//		System.out.println("riverLakes            size (initial capacity): " + riverLakes.size());
//		System.out.println("rivers1               size (initial capacity): " + rivers1.size());
//		System.out.println("rivers2               size (initial capacity): " + rivers2.size());
//		System.out.println("rivers3               size (initial capacity): " + rivers3.size());
//		System.out.println("rivers4               size (initial capacity): " + rivers4.size());
//		System.out.println("rivers5               size (initial capacity): " + rivers5.size());
//		System.out.println("rivers6               size (initial capacity): " + rivers6.size());
//		System.out.println("rivers7               size (initial capacity): " + rivers7.size());
//		System.out.println("rivers8               size (initial capacity): " + rivers8.size());
//		System.out.println("rivers9               size (initial capacity): " + rivers9.size());
//		System.out.println("rivers10              size (initial capacity): " + rivers10.size());
//		System.out.println("rivers11              size (initial capacity): " + rivers11.size());
//		System.out.println("rivers12              size (initial capacity): " + rivers12.size());
//		System.out.println("rivers13              size (initial capacity): " + rivers13.size());

		svg.removeEmptyElements();
		try (Writer out = Files.newBufferedWriter(Paths.get("/home/zom-b/Projects/Javascript/pixelywars/maps/grid1.svg"))) {
			svg.encode(out, 0);
		}
	}

	private static void addPolygonToPSVGPath(Polygon shoreline, SVGPath path) {
		List<Point2D> points = shoreline.getPoints();

		int numPoints = points.size();

		boolean closedLoop = points.get(0).equals(points.get(numPoints - 1));
		if (closedLoop) {
			numPoints--;
		}

		for (int i = 0; i < numPoints; i++) {
			Point2D point = points.get(i);
			if (i == 0) {
				path.moveTo((float)point.getX(), (float)point.getY());
			} else {
				path.lineTo((float)point.getX(), (float)point.getY());
			}
		}

		if (closedLoop) {
			path.closePath();
		}
	}

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WorldMapBorderMakerMain(List<Polygon> shorelines, List<Polygon> borders, List<Polygon> rivers) {
		super(null);
		this.shorelines = requireNonNull(shorelines, "shorelines");
		this.borders = requireNonNull(borders, "borders");
		this.rivers = requireNonNull(rivers, "rivers");

		setPreferredSize(new Dimension(SVG_WIDTH, SVG_HEIGHT));
		setBackground(Color.WHITE);

		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		GraphicsUtilities.setAntialiased(g2, true);

		latitudeMap = new XYCubicSpline(latitudeDeg, latitudeY, Double.NaN, Double.NaN);

		drawShorelines(g2, 1, Color.BLACK);
		drawShorelines(g2, 2, Color.WHITE);
		drawShorelines(g2, 3, Color.BLACK);
		drawShorelines(g2, 4, Color.WHITE);
		drawShorelines(g2, 5, Color.BLACK);
//		drawShorelines(g2, 6, Color.BLACK);
//		drawRivers(g2, 0);
//		drawRivers(g2, 1);
//		drawRivers(g2, 2);
//		drawRivers(g2, 3);
		drawBorders(g2, 1, Color.RED);
//		drawBorders(g2, 2, Color.GRAY);
//		drawBorders(g2, 3, Color.LIGHT_GRAY);

//		drawGrid(g2);
	}

	private void drawGrid(Graphics2D g2) {
		g2.setPaint(Color.GRAY);

		for (int i = 5; i < 360; i += 5) {
			g2.setPaint(
					i == 190 - 65 || i == 190 - 35 || i == 190 + 35 || i == 190 + 65 ? Color.GRAY : Color.LIGHT_GRAY);

			double x = i / 360.0 * worldWidth + 6.5;
			g2.draw(new Line2D.Double(x, 0, x, worldHeight));
		}

		for (int i = -90; i <= 90; i += 5) {
			g2.setPaint(
					i == -90 || i == -65 || i == -35 || i == 35 || i == 65 || i == 90 ? Color.GRAY : Color.LIGHT_GRAY);

			double y = latitudeMap.applyAsDouble(i) + worldHeight * 0.5;
			g2.draw(new Line2D.Double(0, y, worldWidth, y));
		}

		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD, 181);

		for (int i = -90; i <= 90; i++) {
			double y = (latitudeMap.applyAsDouble(i) + 0.5) * worldHeight;

			if (i == -90) {
				path.moveTo(0, y);
			} else {
				path.lineTo((i + 90) / 181.0 * worldWidth, y);
			}
		}

		g2.setPaint(Color.RED);
		g2.draw(path);

		g2.setPaint(Color.GREEN);
		double x = 190 / 360.0 * worldWidth + 6.5;
		double r = latitudeDeg[2] / 360.0 * worldWidth;
		g2.draw(new Ellipse2D.Double(x - r, worldHeight * 0.5 - r, r * 2, r * 2));
	}

	private void drawShorelines(Graphics2D g2, int level, Color color) {
		List<Polygon> transformedShorelines = transformPolygons(shorelines);

		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD, 1481708);
		int    n    = 0;

		for (int j = 0; j < transformedShorelines.size(); j++) {
			if (j > limit) {
				continue;
			}

			Polygon shoreline = transformedShorelines.get(j);
			if (shoreline.getHeader().getLevel() != level) {
				continue;
			}

			List<Point2D> points = shoreline.getPoints();

			int numPoints = points.size();

			boolean closedLoop = points.get(0).equals(points.get(numPoints - 1));
			if (closedLoop) {
				numPoints--;
			}

			for (int i = 0; i < numPoints; i++) {
				Point2D point = points.get(i);

				if (i == 0) {
					path.moveTo(point.getX(), point.getY());
				} else {
					path.lineTo(point.getX(), point.getY());
				}
			}

			path.closePath();
			n += numPoints;
		}

		g2.setPaint(color);
		g2.fill(path);

//		System.out.println("Shorelines path2D (initial capacity): " + n);
	}

	private void drawRivers(Graphics2D g2, int level) {
		List<Polygon> transformedRivers = transformPolygons(rivers);

		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD, 2514);
		int    n    = 0;

		for (Polygon river : transformedRivers) {
			if (river.getHeader().getLevel() != level) {
				continue;
			}

			List<Point2D> points = river.getPoints();

			int numPoints = points.size();

			boolean closedLoop = points.get(0).equals(points.get(numPoints - 1));
			if (closedLoop) {
				numPoints--;
			}

			for (int i = 0; i < numPoints; i++) {
				Point2D point = points.get(i);

				if (i == 0) {
					path.moveTo(point.getX(), point.getY());
				} else {
					path.lineTo(point.getX(), point.getY());
				}
			}

			g2.setPaint(new Color(Color3fConst.DOS_PALETTE[river.getHeader().getLevel() + 1]));

			if (closedLoop) {
				path.closePath();
				g2.fill(path);
			} else {
				g2.draw(path);
			}

			path.reset();
			n = Math.max(n, points.size());
		}

//		System.out.println("Rivers     path2D (initial capacity): " + n);
	}

	private void drawBorders(Graphics2D g2, int level, Paint color) {
		List<Polygon> transformedBorders = transformPolygons(borders);

		Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD, 2043);
		int    n    = 0;

		for (int j = 0; j < transformedBorders.size(); j++) {
			Polygon border = transformedBorders.get(j);
			if (border.getHeader().getLevel() != level) {
				continue;
			}

			List<Point2D> points = border.getPoints();

			int numPoints = points.size();

			boolean closedLoop = points.get(0).equals(points.get(numPoints - 1));
			if (closedLoop) {
				numPoints--;
			}

			for (int i = 0; i < numPoints; i++) {
				Point2D point = points.get(i);

				if (i == 0) {
					path.moveTo(point.getX(), point.getY());
				} else {
					path.lineTo(point.getX(), point.getY());
				}
			}

			if (closedLoop) {
				path.closePath();
			}

			n = Math.max(n, points.size());
		}

		g2.setPaint(color);
		g2.draw(path);

//		System.out.println("Borders    path2D (initial capacity): " + n);
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if ((mouseEvent.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			int i;
			if ((mouseEvent.getModifiersEx() & MODIFIERS_MASK) == 0) {
				i = 0;
			} else if ((mouseEvent.getModifiersEx() & MODIFIERS_MASK) == InputEvent.SHIFT_DOWN_MASK) {
				i = 1;
			} else if ((mouseEvent.getModifiersEx() & MODIFIERS_MASK) == InputEvent.CTRL_DOWN_MASK) {
				i = 2;
			} else {
				return;
			}

			latitudeY[i] = mouseEvent.getY() - worldHeight * 0.5;
			latitudeY[5 - i] = -latitudeY[i];
			System.out.print("latitudeY:");
			for (int j = 0; j < 6; j++) {
				System.out.printf(" %7.4f", latitudeY[j]);
			}
			System.out.println();
		} else if ((mouseEvent.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			latitudeDeg[0] = 90.0 + mouseEvent.getY() / 10.0;
			latitudeDeg[5] = -latitudeDeg[0];
			System.out.println("latitudeDeg[0]: " + latitudeDeg[0]);
		} else if ((mouseEvent.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			worldHeight = mouseEvent.getY();
			System.out.println("worldHeight: " + worldHeight);
		}

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
	}
}
