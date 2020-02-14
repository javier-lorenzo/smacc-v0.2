/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import boofcv.alg.color.ColorRgb;
import boofcv.alg.filter.binary.BinaryImageOps;
import static boofcv.alg.filter.binary.BinaryImageOps.labelToClusters;
import static boofcv.alg.filter.binary.BinaryImageOps.removePointNoise;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_I32;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author enricosantesarti
 */
public class SegmenterManager {
    
    private int BORDER_SIZE = 20;
    private List<PlasticPiece> plasticsArray = null;
    private double minSize = 150;
    //private final static int BORDER_SIZE = 30;
    private GrayU8 imgSegmented = null;
    private Boolean finito = false;
      public SegmenterManager()
    {
       this.plasticsArray = new ArrayList<>();
    }

    public GrayU8 getImgSegmented() {
        return imgSegmented;
    }

    public void setImgSegmented(GrayU8 imgSegmented) 
    {
        this.imgSegmented = imgSegmented;
    }

    public List<PlasticPiece> getPlasticsArray() 
    {
        return plasticsArray;
    }

    public void setPlasticsArray(List<PlasticPiece> plasticsArray) 
    {
        this.plasticsArray = plasticsArray;
    }

    public double getMinSize() {
        return minSize;
    }

    public void setMinSize(double minSize) 
    {
        this.minSize = minSize;
    }
    
    public int getBORDER_SIZE() 
    {
        return BORDER_SIZE;
    }

    public void setBORDER_SIZE(int BORDER_SIZE) 
    {
        this.BORDER_SIZE = BORDER_SIZE;
    }
    public void segmentImage(Planar<GrayU8> img) 
    {
        if (img.getNumBands() != 3) 
        {
            JOptionPane.showMessageDialog(null, "WARNING: Only RGB images can be processed", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<PlasticPiece> allDetectedBlobs = initialSegmentation(img);
        this.plasticsArray = allDetectedBlobs;
       // removeInutilPieces();

    }
    
    private  List<PlasticPiece> initialSegmentation(Planar<GrayU8> img)
    {
        GrayU8 imgGray = new GrayU8(img.width, img.height);
        ColorRgb.rgbToGray_Weighted(img, imgGray);
        imgSegmented = new GrayU8(img.width, img.height);
        GThresholdImageOps.localSauvola(imgGray, imgSegmented, 20, 0.15f, true);
        removePointNoise(imgSegmented, imgSegmented);
        GrayS32 imgLabeled = new GrayS32(img.width, img.height);
        List<Contour> contours = BinaryImageOps.contour(imgSegmented, ConnectRule.EIGHT, imgLabeled);
        List<List<Point2D_I32>> blobs = labelToClusters(imgLabeled, contours.size(), null);
        imgLabeled = null;
        int i = 1;
        List<PlasticPiece> nonFilteredBlobs = new ArrayList<>();
        Iterator<List<Point2D_I32>> it = blobs.iterator();
        for (Contour c : contours) {
            List<Point2D_I32> b = it.next();
            PlasticPiece tmp = new PlasticPiece();
            tmp.setId(i);
            tmp.setContourAndPoints(c, b);
            nonFilteredBlobs.add(tmp);
            i++;
        }
         for (PlasticPiece b : nonFilteredBlobs) {
          //  b.setColorFeatures(ColorFeatureManager.ComputeColorFeatures(img, imgGray, imgRGB, imgHSV, b.getPoints()));
            b.setGeometricFeatures(GeometricFeature.ComputeGeometricFeatures(b.getContour(), b.getPoints()));
         }
         
         return nonFilteredBlobs;
    }
    public void removeInutilPieces(Planar<GrayU8> imgSegmentedNew, List<PlasticPiece> nonFilteredBlobs) 
    {
        Rectangle innerArea = new Rectangle(BORDER_SIZE, BORDER_SIZE,
                imgSegmentedNew.width - 2 * BORDER_SIZE, imgSegmentedNew.height - 2 * BORDER_SIZE);
        List<PlasticPiece> toRemove = new ArrayList<>();
        List<PlasticPiece> nuova = new ArrayList<>();
        Iterator<PlasticPiece> it = nonFilteredBlobs.iterator();
        int i = 1;
        int num = 0;
        while (it.hasNext()) 
        {
            PlasticPiece b = it.next();
            double area = b.getContour().external.size();
            if ((area <= minSize) || !innerArea.contains(b.getGeometricFeatures().boundingBox)) 
            {    
               toRemove.add(b);
            }
            else
            {
                nuova.add(b);
            }

        }
     
        
        this.setPlasticsArray(nuova);
    }
    public void removeOneInAnother(List<PlasticPiece> nonFilteredBlobs)
    {
        List<PlasticPiece> toRemove = new ArrayList<>();
        List<PlasticPiece> nuova = new ArrayList<>();
       
        for(PlasticPiece p1 : nonFilteredBlobs)
        { 
            boolean trovat1 = false;
            for(PlasticPiece p2 : nonFilteredBlobs)
            {
                
                if(p1.getId()!=p2.getId() && p2.getGeometricFeatures().boundingBox.contains(p1.getGeometricFeatures().boundingBox))
                {
                    trovat1 = true;
                }

            }
            if(trovat1)
            {
                toRemove.add(p1);
            }
            else
            {
                nuova.add(p1);
            }
        }
        this.setPlasticsArray(nuova);
        int count=1;
        for(PlasticPiece a : this.getPlasticsArray())
        {
            a.setId(count);
            count++;
        }
    }
       
}
