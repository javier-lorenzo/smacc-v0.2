/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;


import ij.process.ColorProcessor;
import java.awt.image.BufferedImage;

/**
 *
 * @author enricosantesarti
 */
public class LocalBinaryPatternsTest {
    private LocalBinaryPatterns m_lbp;
    public void setUp() {
        m_lbp = new LocalBinaryPatterns();
    }
    
public Double[] smallRadiuslargeNeighbours2(BufferedImage a ) throws Exception {
        ColorProcessor ip = new ColorProcessor(a);
        LocalBinaryPatterns lbp = new LocalBinaryPatterns();
        lbp.setNumPoints(8);
        lbp.setRadius(1);
        lbp.setNumberOfHistogramBins(256);
        lbp.run(ip);
        lbp.getFeatures();
        Double[] lbp_set = new Double[256];
	for(int i = 0; i<lbp_set.length ; i++)
        {
            lbp_set[i] = 0.0;
        }
        for(double[] temp : lbp.getFeatures())
        {
            for(int i = 2; i< temp.length ; i++)
            {   
                if(temp[i-2] == 1)
                {
                    lbp_set[i-2] = lbp_set[i-2] + 1;
                }
            }
        }
        
        //Normalizzo l array
       for(int i = 0; i<lbp_set.length ; i++)
       {
           lbp_set[i]= lbp_set[i]/lbp.getFeatures().size();
       }  
        return lbp_set;
    }
}
