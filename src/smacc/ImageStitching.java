/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.geo.Estimate1ofEpipolar;
import boofcv.abst.geo.fitting.DistanceFromModelResidual;
import boofcv.abst.geo.fitting.GenerateEpipolarMatrix;
import boofcv.abst.geo.fitting.ModelManagerEpipolarMatrix;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.geo.f.FundamentalResidualSampson;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.core.image.border.BorderType;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.EnumFundamental;
import boofcv.factory.geo.EpipolarError;
import boofcv.factory.geo.FactoryMultiView;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.transform.homography.HomographyPointOps_F64;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.FastQueue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static smacc.ExampleFundamentalMatrix.robustFundamental;
import org.ddogleg.fitting.modelset.ModelFitter;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ejml.data.DMatrixRMaj;
/**
 *
 * @author enricosantesarti
 */
public class ImageStitching {
    public static<T extends ImageGray<T>, FD extends TupleDesc> Homography2D_F64
	computeTransform( T imageA , T imageB ,
					  DetectDescribePoint<T,FD> detDesc ,
					  AssociateDescription<FD> associate ,
					  ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher )
	{
		// get the length of the description
		List<Point2D_F64> pointsA = new ArrayList<>();
		FastQueue<FD> descA = UtilFeature.createQueue(detDesc,100);
		List<Point2D_F64> pointsB = new ArrayList<>();
		FastQueue<FD> descB = UtilFeature.createQueue(detDesc,100);

		// extract feature locations and descriptions from each image
		describeImage(imageA, detDesc, pointsA, descA);
		describeImage(imageB, detDesc, pointsB, descB);

		// Associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();

		// create a list of AssociatedPairs that tell the model matcher how a feature moved
		FastQueue<AssociatedIndex> matches = associate.getMatches();
		List<AssociatedPair> pairs = new ArrayList<>();
                        List<Double> inputx = new ArrayList<Double>();
                        List<Double> inputy = new ArrayList<Double>();
                        List <Double> outputx = new ArrayList<Double>();
                        List <Double> outputy = new ArrayList<Double>();
                for( int i = 0; i < matches.size(); i++ ) 
                {        
			AssociatedIndex match = matches.get(i);
			Point2D_F64 a = pointsA.get(match.src);
			Point2D_F64 b = pointsB.get(match.dst);
                        //quiiiiiiiiiiiii
			pairs.add( new AssociatedPair(a,b,false));
		}
                
                List<AssociatedPair> inliers = new ArrayList<>();
                DMatrixRMaj F;
		// estimate and print the results using a robust estimator
		// Also note that the fundamental matrix is only defined up to a scale factor.
		F = robustFundamental(pairs, inliers);
                
                
                for(AssociatedPair p : inliers)
                {
                   inputx.add(Math.abs(p.p1.x - p.p2.x));
                   inputy.add(Math.abs(p.p1.y - p.p2.y)); 
                }
                Collections.sort(inputx);
                Collections.sort(inputy);
                outputx = getOutliers(inputx);
                outputy = getOutliers(inputy);
                List<AssociatedPair> toRemove = new ArrayList<>();
                for(AssociatedPair p : inliers)
                {
                    if(outputx.contains(Math.abs(p.p1.x - p.p2.x)) || outputy.contains(Math.abs(p.p1.y - p.p2.y)))
                    {
                        toRemove.add(p);
                    }
                }
                inliers.removeAll(toRemove);
		// find the best fit model to describe the change between these images
		if( !modelMatcher.process(inliers) )
			throw new RuntimeException("Model Matcher failed!");
               
		// return the found image transform
		return modelMatcher.getModelParameters().copy();
	}
        public static DMatrixRMaj robustFundamental( List<AssociatedPair> matches ,List<AssociatedPair> inliers ) {

		// used to create and copy new instances of the fit model
		ModelManager<DMatrixRMaj> managerF = new ModelManagerEpipolarMatrix();
		// Select which linear algorithm is to be used.  Try playing with the number of remove ambiguity points
		Estimate1ofEpipolar estimateF = FactoryMultiView.computeFundamental_1(EnumFundamental.LINEAR_7, 2);
		// Wrapper so that this estimator can be used by the robust estimator
		GenerateEpipolarMatrix generateF = new GenerateEpipolarMatrix(estimateF);

		// How the error is measured
		DistanceFromModelResidual<DMatrixRMaj,AssociatedPair> errorMetric =
				new DistanceFromModelResidual<>(new FundamentalResidualSampson());

		// Use RANSAC to estimate the Fundamental matrix
		ModelMatcher<DMatrixRMaj,AssociatedPair> robustF =
				new Ransac<>(123123, managerF, generateF, errorMetric, 6000, 0.1);

		// Estimate the fundamental matrix while removing outliers
		if( !robustF.process(matches) )
			throw new IllegalArgumentException("Failed");

		// save the set of features that were used to compute the fundamental matrix
		inliers.addAll(robustF.getMatchSet());
                
		// Improve the estimate of the fundamental matrix using non-linear optimization
		DMatrixRMaj F = new DMatrixRMaj(3,3);
		ModelFitter<DMatrixRMaj,AssociatedPair> refine =
				FactoryMultiView.refineFundamental(1e-8, 400, EpipolarError.SAMPSON);
		if( !refine.fitModel(inliers, robustF.getModelParameters(), F) )
			throw new IllegalArgumentException("Failed");
                
		// Return the solution
		return F;
	}
	/**
	 * Detects features inside the two images and computes descriptions at those points.
	 */
	private static <T extends ImageGray<T>, FD extends TupleDesc>
	void describeImage(T image,
					   DetectDescribePoint<T,FD> detDesc,
					   List<Point2D_F64> points,
					   FastQueue<FD> listDescs) {
		detDesc.detect(image);

		listDescs.reset();
		for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
			points.add( detDesc.getLocation(i).copy() );
			listDescs.grow().setTo(detDesc.getDescription(i));
		}
	}

	/**
	 * Given two input images create and display an image where the two have been overlayed on top of each other.
	 */
	public static <T extends ImageGray<T>>
	BufferedImage stitch( BufferedImage imageA , BufferedImage imageB , Class<T> imageType )
	{
		T inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
		T inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);

		// Detect using the standard SURF feature descriptor and describer
		DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, imageType);
		ScoreAssociation<BrightFeature> scorer = FactoryAssociation.scoreEuclidean(BrightFeature.class,true);
		AssociateDescription<BrightFeature> associate = FactoryAssociation.greedy(scorer,2,true);
		// fit the images using a homography.  This works well for rotations and distant objects.

		ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher =
				FactoryMultiViewRobust.homographyRansac(null,new ConfigRansac(60,3));
                Planar<GrayF32> colorB =
				ConvertBufferedImage.convertFromPlanar(imageB, null,true, GrayF32.class);
                Planar<GrayF32> work = colorB.createSameShape();
		Homography2D_F64 H = computeTransform(inputA, inputB, detDesc, associate, modelMatcher);
                
                PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
                InterpolatePixelS<GrayF32> interp = FactoryInterpolation.bilinearPixelS(GrayF32.class, BorderType.ZERO);
                ImageDistort<Planar<GrayF32>,Planar<GrayF32>> distort =
				DistortSupport.createDistortPL(GrayF32.class, model, interp, false);
		distort.setRenderAll(false);
                model.set(H);
		distort.apply(colorB,work);
                

		// Convert the rendered image into a BufferedImage
		BufferedImage output = new BufferedImage(work.width,work.height,imageB.getType());
		ConvertBufferedImage.convertTo(work,output,true);
                return output;
                //renderStitching(imageB,H);
	}
        
        
        
    public static List<Double> getOutliers(List<Double> input) {
        
        List<Double> output = new ArrayList<Double>();
        List<Double> data1 = new ArrayList<Double>();
        List<Double> data2 = new ArrayList<Double>();
        if (input.size() % 2 == 0) {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2, input.size());
        } else {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2 + 1, input.size());
        }

        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) < lowerFence || input.get(i) > upperFence)
                output.add(input.get(i));
        }
        return output;
    }

    private static double getMedian(List<Double> data) {
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

	/**
	 * Renders and displays the stitched together images
	 
	public static void renderStitching(  BufferedImage imageB, Homography2D_F64 fromAtoB )
	{
		// specify size of output image
		double scale = 0.5;

		// Convert into a BoofCV color format
		
		Planar<GrayF32> colorB =
				ConvertBufferedImage.convertFromPlanar(imageB, null,true, GrayF32.class);

		// Where the output images are rendered into
		Planar<GrayF32> work = colorB.createSameShape();

		// Adjust the transform so that the whole image can appear inside of it
		Homography2D_F64 fromAToWork = new Homography2D_F64(scale,0,colorB.width/4,0,scale,colorB.height/4,0,0,1);
		Homography2D_F64 fromWorkToA = fromAToWork.invert(null);

		// Used to render the results onto an image
		PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
		InterpolatePixelS<GrayF32> interp = FactoryInterpolation.bilinearPixelS(GrayF32.class, BorderType.ZERO);
		ImageDistort<Planar<GrayF32>,Planar<GrayF32>> distort =
				DistortSupport.createDistortPL(GrayF32.class, model, interp, false);
		distort.setRenderAll(false);

		// Render first image
		//model.set(fromWorkToA);
		//distort.apply(colorA,work);

		// Render second image
		Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB,null);
		model.set(fromWorkToB);
		distort.apply(colorB,work);

		// Convert the rendered image into a BufferedImage
		BufferedImage output = new BufferedImage(work.width,work.height,imageB.getType());
		ConvertBufferedImage.convertTo(work,output,true);

		Graphics2D g2 = output.createGraphics();

		// draw lines around the distorted image to make it easier to see
		Homography2D_F64 fromBtoWork = fromWorkToB.invert(null);
		Point2D_I32 corners[] = new Point2D_I32[4];
		corners[0] = renderPoint(0,0,fromBtoWork);
		corners[1] = renderPoint(colorB.width,0,fromBtoWork);
		corners[2] = renderPoint(colorB.width,colorB.height,fromBtoWork);
		corners[3] = renderPoint(0,colorB.height,fromBtoWork);

		g2.setColor(Color.ORANGE);
		g2.setStroke(new BasicStroke(4));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawLine(corners[0].x,corners[0].y,corners[1].x,corners[1].y);
		g2.drawLine(corners[1].x,corners[1].y,corners[2].x,corners[2].y);
		g2.drawLine(corners[2].x,corners[2].y,corners[3].x,corners[3].y);
		g2.drawLine(corners[3].x,corners[3].y,corners[0].x,corners[0].y);

		ShowImages.showWindow(output,"Stitched Images", true);
	}

	private static Point2D_I32 renderPoint( int x0 , int y0 , Homography2D_F64 fromBtoWork )
	{
		Point2D_F64 result = new Point2D_F64();
		HomographyPointOps_F64.transform(fromBtoWork, new Point2D_F64(x0, y0), result);
		return new Point2D_I32((int)result.x,(int)result.y);
	}*/

}
