/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import boofcv.alg.filter.binary.Contour;
import boofcv.alg.shapes.FitData;
import boofcv.alg.shapes.ShapeFittingOps;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.EllipseRotated_F64;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author enricosantesarti
 */
public class GeometricFeature {
    private static final double pixelsToMmRatio = 23.17;
    
    double area;
    double metric_area;

    double perimeter;
    double metric_perimeter;

    double compactness;

    Point2D_I32 centroid;

    double maxRadius;
    double minRadius;
    double radiusRatio;

    Rectangle boundingBox = new Rectangle();
    double widthHeightRatio;

    double BBtoAreaRatio;

    FitData<EllipseRotated_F64> ellipse;
    double ellipseAxisRatio;
    
    String featureNames[] = new String[]{"area", "perimeter", "compacity", "radiusRatio",
        "widthHeightRatio", "BBtoAreaRatio"};
    List<Double> featuresList = null;

    public GeometricFeature() {
        this.featuresList = new ArrayList<>();
    }
    
     static GeometricFeature ComputeGeometricFeatures(Contour contour, List<Point2D_I32> points) {
        GeometricFeature features = new GeometricFeature();
        features.perimeter = (double) contour.external.size();
        features.metric_perimeter = (double) features.perimeter / pixelsToMmRatio;

        features.area = (double) points.size();
        features.metric_area = (double) features.area / (pixelsToMmRatio * pixelsToMmRatio);

        features.compactness = (features.perimeter * features.perimeter) / features.area;

        // Centroid and the Bounding box
        double sumx = 0.0;
        double sumy = 0.0;
        int minx = 200000;
        int miny = 200000;
        int maxx = 0;
        int maxy = 0;

        for (Iterator<Point2D_I32> it = contour.external.iterator(); it.hasNext();) {
            Point2D_I32 p = it.next();
            int x = p.x;
            int y = p.y;

            sumx += (double) x;
            sumy += (double) y;

            if (x < minx) {
                minx = x;
            }
            if (x > maxx) {
                maxx = x;
            }
            if (y < miny) {
                miny = y;
            }
            if (y > maxy) {
                maxy = y;
            }
        }

        features.centroid = new Point2D_I32();
        features.centroid.x = (int) (sumx / (double) features.perimeter);
        features.centroid.y = (int) (sumy / (double) features.perimeter);

        features.boundingBox.x = minx;
        features.boundingBox.y = miny;
        features.boundingBox.width = maxx - minx;
        features.boundingBox.height = maxy - miny;

        // Maximum and minimum radii of the contour
        double minR = 1e12;
        double maxR = 0.0;

        for (Iterator<Point2D_I32> it = contour.external.iterator(); it.hasNext();) {
            Point2D_I32 p = it.next();

            double d = features.centroid.distance(p);

            if (d > maxR) {
                maxR = d;
            }

            if (d < minR) {
                minR = d;
            }
        }

        features.maxRadius = maxR;
        features.minRadius = minR;

        // Radius, width-height and BB-area ratios
        features.radiusRatio = features.minRadius / features.maxRadius;
        features.widthHeightRatio = (double) features.boundingBox.width / (double) features.boundingBox.height;
        features.BBtoAreaRatio = (features.boundingBox.width * features.boundingBox.height) / (double) features.area;

        // fit ellipse
        features.ellipse = ShapeFittingOps.fitEllipse_I32(contour.external, 0, false, null);
        features.ellipseAxisRatio = features.ellipse.shape.a / features.ellipse.shape.b;
        
        return features;
    }

    @Override
    public String toString() {
        String res = "(area metric_area perimeter metric_perimeter  centroid compacity "
                + "max_ radius min_radius radii_ratio) "
                + area + " " + String.format("%.3f", metric_area) + " " + perimeter + " " + String.format("%.3f", metric_perimeter) + " " + centroid + " " + String.format("%.3f", compactness) + " "
                + String.format("%.3f", maxRadius) + " " + String.format("%.3f", minRadius)
                + " " + String.format("%.3f", radiusRatio);
        return res;
    }

}
