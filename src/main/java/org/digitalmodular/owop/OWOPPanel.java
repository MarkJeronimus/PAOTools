/*
 * This file is part of PAO.
 *
 * Copyleft 2022 Mark Jeronimus. All Rights Reversed.
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

package org.digitalmodular.owop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import javax.swing.JButton;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.graphics.swing.TextShape;
import org.digitalmodular.utilities.graphics.swing.tilepanel.TileProvider;
import org.digitalmodular.utilities.graphics.swing.tilepanel.TiledScrollPanel;
import static org.digitalmodular.utilities.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

import org.digitalmodular.owop.data.OWOPRegionDataModel;
import org.digitalmodular.owop.data.Region;
import org.digitalmodular.owop.data.Tile;
import org.digitalmodular.owop.data.TileKey;
import static org.digitalmodular.owop.OWOPClientMain.REGION_SIZE;

/**
 * @author Mark Jeronimus
 */
// Created 2022-12-23
public class OWOPPanel extends TiledScrollPanel {
	private final OWOPClientMain main;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public OWOPPanel(OWOPClientMain main) {
		super(new OWOPTileProvider(requireNonNull(main, "main").getDataModel(), REGION_SIZE));

		this.main = main;

		main.getDataModel().addElementUpdatedListener(this::tileUpdated);

		setPreferredSize(new Dimension(2000, 1000));
		setLayout(new FlowLayout());

		JButton connectButton = new JButton("Connect");
		add(connectButton);
		connectButton.addActionListener(this::connectAction);
		JButton disconnectButton = new JButton("Disconnect");
		add(disconnectButton);
		disconnectButton.addActionListener(this::disconnect);
		JButton saveButton = new JButton("Save All");
		add(saveButton);
		saveButton.addActionListener(OWOPPanel::saveAll);

		setZoomLimits(-20, 4);

	}

	private void connectAction(ActionEvent actionEvent) {
		ForkJoinPool.commonPool().execute(main::connect);
	}

	private void disconnect(ActionEvent actionEvent) {
		ForkJoinPool.commonPool().execute(main::disconnect);
	}

	private static void saveAll(ActionEvent actionEvent) {
		OWOPClientMain.saveAll();
	}

	public void tileUpdated(TileKey key, Tile tile) {
		updateTile(key.getX() / REGION_SIZE, key.getY() / REGION_SIZE);
	}

	private static final class OWOPTileProvider implements TileProvider {
		private final OWOPRegionDataModel dataModel;
		private final Integer             tileSize;

		private final BufferedImage defaultImage;

		private OWOPTileProvider(OWOPRegionDataModel dataModel, int tileSize) {
			this.dataModel = requireNonNull(dataModel, "dataModel");
			this.tileSize = requireAtLeast(1, tileSize, "tileSize");
			defaultImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);

			for (int y = 0; y < tileSize; y++) {
				for (int x = 0; x < tileSize; x++) {
					if (x == 0 || y == 0 || x == tileSize - 1 || y == tileSize - 1) {
						defaultImage.setRGB(x, y, 0xBBBBBB);
					} else {
						defaultImage.setRGB(x, y, 0x000000);
					}
				}
			}
		}

		@Override
		public int getTileSize() {
			return tileSize;
		}

		@Override
		public int getTileShift() {
			return Integer.bitCount(tileSize - 1);
		}

		@Override
		public @Nullable BufferedImage getTile(int x, int y, int zoom) {
			int areaSize = zoom < 0 ? tileSize << (-zoom) : tileSize >> zoom;
			System.out.println("Showing tile (" + x * areaSize + ", " + y * areaSize + ", " + zoom + ')');

			TileKey          key    = new TileKey(x, y, zoom, REGION_SIZE);
			@Nullable Region region = dataModel.getRegion(key);
			BufferedImage    image  = region == null ? null : region.getImage();

			return image;
		}

		@Override
		public BufferedImage getDefaultTile(int x, int y, int zoom) {
			int areaSize = zoom < 0 ? tileSize << (-zoom) : tileSize >> zoom;

			BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D    g     = image.createGraphics();
			try {
				g.drawImage(defaultImage, 0, 0, null);

				TextShape t = new TextShape("(" + x * areaSize + ", " + y * areaSize + ", " + zoom + ')',
				                            tileSize >> 1,
				                            tileSize >> 1,
				                            0.5,
				                            0.5);

				g.setPaint(new Color(0xBBBBBB));
				g.fill(t.calculateShape(g));
			} finally {
				g.dispose();
			}

			return image;
		}
	}
}
