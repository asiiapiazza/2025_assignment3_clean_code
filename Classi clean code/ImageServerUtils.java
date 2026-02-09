
package com.bbn.openmap.image;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Contiene funzioni di utilità per l'analisi delle richieste di immagini delle
 * mappe.
 */
public class ImageServerUtils implements ImageServerConstants {

    public static Proj createOMProjection(Properties props, Projection defaultProj) {

        float scale = PropUtils.floatFromProperties(props, SCALE, defaultProj.getScale());
        int width = PropUtils.intFromProperties(props, WIDTH, defaultProj.getWidth());
        int height = PropUtils.intFromProperties(props, HEIGHT, defaultProj.getHeight());

        Point2D centerPoint = calculateCenterPoint(props, defaultProj);
        Class<? extends Projection> projClass = getProjectionClassFromProperties(props, defaultProj);

        logProjectionDebug(projClass, height, width, centerPoint, scale);

        return (Proj) ProjectionFactory.loadDefaultProjections().makeProjection(
                projClass,
                centerPoint,
                scale,
                width,
                height);
    }

    private static Point2D calculateCenterPoint(Properties props, Projection defaultProj) {
        Point2D defaultCenter = defaultProj.getCenter();
        float longitude = PropUtils.floatFromProperties(props, LON, (float) defaultCenter.getX());
        float latitude = PropUtils.floatFromProperties(props, LAT, (float) defaultCenter.getY());

        return new Point2D.Float(longitude, latitude);
    }

    private static Class<? extends Projection> getProjectionClassFromProperties(Properties props,
            Projection defaultProj) {
        String projType = props.getProperty(PROJTYPE);

        if (projType != null) {
            Class<? extends Projection> loadedClass = ProjectionFactory
                    .loadDefaultProjections()
                    .getProjClassForName(projType);
            if (loadedClass != null) {
                return loadedClass;
            }
        }
        return defaultProj.getClass();
    }

    private static void logProjectionDebug(Class<?> projClass, int height,
            int width, Point2D center, float scale) {
        if (Debug.debugging("imageserver")) {
            Debug.output("ImageServerUtils.createOMProjection: projection "
                    + projClass.getName() + ", with HEIGHT = " + height
                    + ", WIDTH = " + width + ", lat = " + center.getY() + ", lon = "
                    + center.getX() + ", scale = " + scale);
        }
    }

    public static Color getBackground(Properties props) {
        return (Color) getBackground(props, Color.white);
    }

    public static Paint getBackground(Properties props, Paint defPaint) {
        Paint basePaint = determineBasePaint(props, defPaint);
        boolean isTransparent = PropUtils.booleanFromProperties(props, TRANSPARENT, false);
        Paint finalPaint = applyTransparency(basePaint, isTransparent);

        logBackgroundDetails(finalPaint, isTransparent);
        return finalPaint;
    }

    private static Paint determineBasePaint(Properties props, Paint defPaint) {
        Paint paint = PropUtils.parseColorFromProperties(props, BGCOLOR, defPaint);
        return (paint != null) ? paint : Color.white;
    }

    private static Paint applyTransparency(Paint paint, boolean isTransparent) {
        if (!isTransparent) {
            return paint;
        }

        if (paint instanceof Color) {
            Color color = (Color) paint;
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        }

        return OMColor.clear;
    }

    private static void logBackgroundDetails(Paint paint, boolean isTransparent) {
        if (Debug.debugging("imageserver")) {
            String colorString = (paint instanceof Color)
                    ? Integer.toHexString(((Color) paint).getRGB())
                    : paint.toString();

            Debug.output(String.format(
                    "ImageServerUtils.getBackground: projection color: %s, transparent(%b)",
                    colorString, isTransparent));
        }
    }
}