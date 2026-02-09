
package com.bbn.openmap.layer.test;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;

public class HelloWorldLayer extends Layer {

	protected OMGraphicList graphics;
	private static final String COORD_FILE = "coordinates.properties";

	public HelloWorldLayer() {
		super();
		graphics = new OMGraphicList(10);
		initializeLayerGraphics();
	}

	private void initializeLayerGraphics() {
		Properties props = loadProperties();
		for (String key : props.stringPropertyNames()) {
			String coordString = props.getProperty(key);
			double[] coordinates = parseCoordinates(coordString);

			if (coordinates.length > 0) {
				addPolygonToLayer(coordinates);
			}
		}
	}

	private Properties loadProperties() {
		Properties props = new Properties();
		try (InputStream input = getClass()
				.getClassLoader()
				.getResourceAsStream(COORD_FILE)) {
			if (input != null) {
				props.load(input);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return props;
	}

	private double[] parseCoordinates(String csv) {
		String[] parts = csv.split(",");
		double[] coords = new double[parts.length];
		for (int i = 0; i < parts.length; i++) {
			coords[i] = Double.parseDouble(parts[i].trim());
		}
		return coords;
	}

	private void addPolygonToLayer(double[] coordinates) {
		OMPoly poly = new OMPoly(coordinates, OMGraphic.DECIMAL_DEGREES,
				OMGraphic.LINETYPE_RHUMB, 32);
		poly.setLinePaint(Color.black);
		poly.setFillPaint(Color.green);
		graphics.add(poly);
	}

	public void setProperties(String prefix, java.util.Properties props) {
		super.setProperties(prefix, props);
	}

	public void projectionChanged(ProjectionEvent e) {
		graphics.generate(e.getProjection());
		repaint();
	}

	public void paint(Graphics g) {
		graphics.render(g);
	}

}