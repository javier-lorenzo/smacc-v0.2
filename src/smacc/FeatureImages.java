/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.gui.feature.AssociationPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageGray;
import georegression.struct.point.Point2D_F64;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.ddogleg.struct.FastQueue;

/**
 *
 * @author enricosantesarti
 */
public class FeatureImages <T extends ImageGray<T>, TD extends TupleDesc> {
    Class<T> imageType;
    public List<Point2D_F64> pointsA;
	public List<Point2D_F64> pointsB;
        // algorithm used to detect and describe interest points
	DetectDescribePoint<T, TD> detDesc;
	// Associated descriptions together by minimizing an error metric
	AssociateDescription<TD> associate;

    FeatureImages(DetectDescribePoint detDesc, AssociateDescription associate, Class imageType) {
                 this.detDesc = detDesc;
		this.associate = associate;
		this.imageType = imageType;
    }
        
    public void associate( BufferedImage imageA , BufferedImage imageB )
	{
		T inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
		T inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);

		// stores the location of detected interest points
		pointsA = new ArrayList<>();
		pointsB = new ArrayList<>();

		// stores the description of detected interest points
		FastQueue<TD> descA = UtilFeature.createQueue(detDesc,100);
		FastQueue<TD> descB = UtilFeature.createQueue(detDesc,100);

		// describe each image using interest points
		describeImage(inputA,pointsA,descA);
		describeImage(inputB,pointsB,descB);

		// Associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();

		// display the results
		AssociationPanel panel = new AssociationPanel(20);
		panel.setAssociation(pointsA,pointsB,associate.getMatches());
		panel.setImages(imageA,imageB);

		//ShowImages.showWindow(panel,"Associated Features",true);
	}
    public void associate1( BufferedImage imageA , BufferedImage imageB )
	{
		T inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
		T inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);

		// stores the location of detected interest points
		pointsA = new ArrayList<>();
		pointsB = new ArrayList<>();

		// stores the description of detected interest points
		FastQueue<TD> descA = UtilFeature.createQueue(detDesc,100);
		FastQueue<TD> descB = UtilFeature.createQueue(detDesc,100);

		// describe each image using interest points
		describeImage(inputA,pointsA,descA);
		describeImage(inputB,pointsB,descB);

		// Associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();

		// display the results
		AssociationPanel panel = new AssociationPanel(20);
		panel.setAssociation(pointsA,pointsB,associate.getMatches());
		panel.setImages(imageA,imageB);

		ShowImages.showWindow(panel,"Associated Features",true);
	}
    private void describeImage(T input, List<Point2D_F64> points, FastQueue<TD> descs )
	{
		detDesc.detect(input);

		for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
			points.add( detDesc.getLocation(i).copy() );
			descs.grow().setTo(detDesc.getDescription(i));
		}
	}
}
