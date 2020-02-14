/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.color.ColorRgb;
import boofcv.core.image.ConvertImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_I32;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author enricosantesarti
 */
public class ColorFeature {

    double[] avgRGB = new double[4];
    double[] varRGB = new double[4];
    double[] avgHSV = new double[3];
    double[] varHSV = new double[3];

    public List<PlasticPiece> ComputeColorFeatures(BufferedImage img, List<PlasticPiece> nonFilteredBlobs) 
    {
        for (PlasticPiece b : nonFilteredBlobs)
        {
            ColorFeature res = new ColorFeature();
           // res = GrayFeatures(res, imgGray, b.getPoints());
            List<Point2D_I32> points = b.getPoints();
            //res = ColorFeatures(res, img, imgRGB, imgHSV, b.getPoints());
            res = ComputeColorFeatures1(res, img, points);
            b.setColorFeatures(res);
        }
        return nonFilteredBlobs;
    }
    
    public ColorFeature ComputeColorFeatures1(ColorFeature res, BufferedImage image, List<Point2D_I32> points) 
    {
        
        double sumRGB, sumR,sumG, sumB, sumH,sumV, sumS;
        sumRGB = 0;
        sumR = 0;
        sumG = 0;
        sumB = 0;
        sumH = 0;
        sumV = 0;
        sumS = 0;
        double numPoints = (double)points.size();
        for (Point2D_I32 p :  points)
        {
            float[] hsv = new float[3];
            int rgbValue = image.getRGB(p.x, p.y);
            int red = (rgbValue >> 16) & 0xff;
            int green = (rgbValue >> 8) & 0xff;
            int blue = (rgbValue) & 0xff;
            Color.RGBtoHSB(red,green,blue,hsv);
            if((red!= 0 && green!=255 && blue!=0))
            {
                sumRGB += rgbValue;
                sumR += red;
                sumG += green;
                sumB += blue;
                sumH += hsv[0];
                sumS += hsv[1];
                sumV += hsv[2];
            }
                     
        }
        res.avgRGB[0] = sumRGB / numPoints;
        res.avgRGB[1] = sumR / numPoints;
        res.avgRGB[2] = sumG / numPoints;
        res.avgRGB[3] = sumB / numPoints;

        res.avgHSV[0] = sumH / numPoints;
        res.avgHSV[1] = sumS / numPoints;
        res.avgHSV[2] = sumV / numPoints;
            
        double sumNRGB = 0;
        double sumNR = 0;
        double sumNG =0;
        double sumNB = 0;
        double sumNH= 0;
        double sumNS=0;
        double sumNV=0;

        for (Point2D_I32 p :  points)
        {
            float[] hsv = new float[3];
            int rgbValue = image.getRGB(p.x, p.y);
            int red = (rgbValue >> 16) & 0xff;
            int green = (rgbValue >> 8) & 0xff;
            int blue = (rgbValue) & 0xff;
            Color.RGBtoHSB(red,green,blue,hsv);
            if((red!= 0 && green!=255 && blue!=0))
            {
                    //sumRGB += ((res.avgRGB[ch] - rgbValue) * (res.avgRGB[ch] - vRGB));
                sumNRGB += ((res.avgRGB[0] - rgbValue) * (res.avgRGB[0] - rgbValue));
                sumNR += ((res.avgRGB[1] - red) * (res.avgRGB[1] - red));
                sumNG += ((res.avgRGB[2] - green) * (res.avgRGB[2] - green));
                sumNB += ((res.avgRGB[3] - blue) * (res.avgRGB[3] - blue));
                sumNH += ((res.avgHSV[0] - hsv[0]) * (res.avgHSV[0] - hsv[0]));
                sumNS += ((res.avgHSV[1] - hsv[1]) * (res.avgHSV[1] - hsv[1]));
                sumNV += ((res.avgHSV[2] - hsv[2]) * (res.avgHSV[2] - hsv[2]));
            }        
        }
        res.varRGB[0] = sumNRGB / numPoints;
        res.varRGB[1] = sumNR / numPoints;
        res.varRGB[2] = sumNG / numPoints;
        res.varRGB[3] = sumNB / numPoints;
        res.varHSV[0] = sumNH / numPoints;
        res.varHSV[1] = sumNS / numPoints;
        res.varHSV[2] = sumNV / numPoints;
        return res;
    }
}
