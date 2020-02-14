/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

/**
 *
 * @author enricosantesarti
 */
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import static weka.core.SerializationHelper.read;
 import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;

public class ClassifierManager {

    private Classifier cls = null;
    private String[] className = null;
    private Instances dataset = null;
    
    public ClassifierManager() {
    }

    public ClassifierManager(File filename, String[] classname) {
        this.className = classname;
        try {
            cls = (Classifier) read(filename.getCanonicalPath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error in loading classifer " + filename + " " + ex.toString() , "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setDataset(Instances dataset) {
        this.dataset = dataset;
    }

    public String[] getClassName() {
        return className;
    }
    
    public String[] classify() {
        int numSamples = dataset.numInstances();
        String[] res = new String[numSamples];
        for (int i = 0; i < numSamples; i++) 
        {
            
            Instance sample = dataset.get(i);
            
            int classIdx = 0;
            try {
                classIdx = (int) cls.classifyInstance(sample);
            } catch (Exception ex) {
                Logger.getLogger(ClassifierManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            res[i] = className[classIdx];
        }
        
        return res;
    }
}