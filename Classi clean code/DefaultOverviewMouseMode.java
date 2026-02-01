package com.bbn.openmap.event;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.gui.OverviewMapHandler;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * Consente di ridimensionare e riposizionare la mappa principale
 * in base alle interazioni dell'utente con la mappa di panoramica.
 */
public class DefaultOverviewMouseMode extends NavMouseMode2 {

   OverviewMapHandler overviewMapHandler;
   private static final int DRAG_THRESHOLD = 5;

   public DefaultOverviewMouseMode(OverviewMapHandler omh) {
      super(true);
      overviewMapHandler = omh;
   }

   public void mouseReleased(MouseEvent event) {
      logDebugInfo();

      if (isInvalidInteraction(event)) {
         return;
      }

      synchronized (this) {
         processMouseRelease(event);
      }

      cleanUp();
   }

   private void logDebugInfo() {
      if (Debug.debugging("mousemode")) {
         System.out.println(getID() + "|DefaultOverviewMouseMode.mouseReleased()");
      }
   }

   private boolean isInvalidInteraction(MouseEvent event) {
      boolean consumed = mouseSupport.fireMapMouseReleased(event);
      boolean validState = (event.getSource() == theMap) && autoZoom && (point1 != null);

      return consumed || !validState;
   }

   private void processMouseRelease(MouseEvent event) {
      point2 = getRatioPoint(theMap, point1, event.getPoint());
      int deltaX = Math.abs(point2.x - point1.x);
      int deltaY = Math.abs(point2.y - point1.y);

      if (isClickOrSmallDrag(deltaX, deltaY)) {
         recenterMap(event.getPoint());
      } else {
         performZoom(deltaX, deltaY);
      }
   }

   protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {
      return ProjMath.getRatioPoint(overviewMapHandler.getSourceMap().getProjection(), pt1, pt2);
   }

   private boolean isClickOrSmallDrag(int dx, int dy) {
      return (dx < DRAG_THRESHOLD) && (dy < DRAG_THRESHOLD);
   }

   private void recenterMap(Point2D clickPoint) {
      Projection projection = theMap.getProjection();
      Point2D newCenter = projection.inverse(clickPoint);
      overviewMapHandler.getControlledMapListeners().setCenter(newCenter);
      theMap.repaint();
   }

   private void performZoom(int deltaX, int deltaY) {
      Projection projection = theMap.getProjection();

      float newScale = calculateNewScale(projection, deltaX, deltaY);
      Point2D newCenter = projection.inverse(point1.x, point1.y);

      if (newScale > 0) {
         overviewMapHandler.getControlledMapListeners().setScale(newScale);
      }
      overviewMapHandler.getControlledMapListeners().setCenter(newCenter);
   }

   private float calculateNewScale(Projection projection, int dx, int dy) {
      if (!(projection instanceof GeoProj)) {
         return -1f; // non gestito nel codice originale
      }

      float deltaDegrees = calculateDeltaDegrees(projection, dx, dy);
      int referencePixelSize = (dx < dy) ? projection.getHeight() : projection.getWidth();

      double pixPerDegree = ((GeoProj) projection).getPlanetPixelCircumference() / 360;
      return (float) (pixPerDegree / (referencePixelSize / deltaDegrees));
   }

   private float calculateDeltaDegrees(Projection projection, int dx, int dy) {
      Point2D startGeo = projection.inverse(point1);
      Point2D endGeo = projection.inverse(point2);

      if (dx < dy) {
         float dLat = (float) Math.abs(startGeo.getY() - endGeo.getY());
         return dLat * 2;
      }

      return calculateLongitudeDifference((float) startGeo.getX(), (float) endGeo.getX());
   }

   private float calculateLongitudeDifference(float lon1, float lon2) {
      if (point1.x > point2.x) {
         float temp = lon1;
         lon1 = lon2;
         lon2 = temp;
      }

      float deltaLongitude;
      if (lon1 > lon2) {
         deltaLongitude = (180 - lon1) + (180 + lon2);
      } else {
         deltaLongitude = lon2 - lon1;
      }
      return deltaLongitude * 2;
   }

}