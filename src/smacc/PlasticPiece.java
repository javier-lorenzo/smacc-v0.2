/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;
import boofcv.alg.filter.binary.*;
import georegression.struct.point.Point2D_I32;
import java.util.List;
/**
 *
 * @author enricosantesarti
 */
public class PlasticPiece {
    private Contour contour;
    private List<Point2D_I32> points;
    private int id;
    private GeometricFeature geometricFeatures = null;
    private ColorFeature colorFeatures = null;
    private String particleType = "";
    private Double[] lbp ;

    
    public Double[] getLbp() {
        return lbp;
    }

    public void setLbp(Double[] lbp) {
        this.lbp = lbp;
    }

    public String getParticleType() {
        return particleType;
    }

    public void setParticleType(String particleType) {
        this.particleType = particleType;
    }
    
    
    public ColorFeature getColorFeatures() {
        return colorFeatures;
    }

    public void setColorFeatures(ColorFeature colorFeatures) {
        this.colorFeatures = colorFeatures;
    }
    
    
    public GeometricFeature getGeometricFeatures() {
        return geometricFeatures;
    }

    public void setGeometricFeatures(GeometricFeature geometricFeatures) {
        this.geometricFeatures = geometricFeatures;
    }

    
    
    public void setId(int id) 
    {
        this.id = id;
    }
    public void setContourAndPoints(Contour contour, List<Point2D_I32> points) 
    {
        this.contour = contour;
        this.points = points;
    }
    public int getId()
    {
        return id;
    }
    public Contour getContour() {
        return contour;
    }
    public List<Point2D_I32> getPoints() {
        return points;
    }

}
