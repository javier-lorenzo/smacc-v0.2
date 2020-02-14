/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import static de.lmu.ifi.dbs.jfeaturelib.Descriptor.DOES_ALL;
import de.lmu.ifi.dbs.jfeaturelib.LibProperties;
import de.lmu.ifi.dbs.jfeaturelib.Progress;
import de.lmu.ifi.dbs.utilities.Arrays2;
import ij.process.ImageProcessor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This abstract class provides some convenient base functionalities for feature descriptors like the getter for the
 * data (including null check) and the propery change support.
 *
 * @author Franz
 */
public abstract class AbstractFeatureDescriptor implements FeatureDescriptor {

    /**
     * holds the most recent progress event that was fired using the
     * {@link #firePropertyChange(de.lmu.dbs.jfeaturelib.Progress)} method.
     */
    private Progress previous = null;
    /**
     * Property change support that can be used by the implementing class to inform listeners about updates.
     */
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    /**
     * The data arrays which hold the calculated features.
     *
     * Most of the descriptors will only return a single array. Yet there are some descriptors like Sift which return a
     * bag of features.
     */
    private List<double[]> data = new ArrayList<>(1);
    /**
     * Stores the mask of the passed image processor or NULL if the image processor did not have a mask applied.
     *
     * Pixels outside the mask have a value of zero.
     */
    private ImageProcessor mask = null;

    /**
     * Returns a reference to the data calculated by the according descriptor.
     *
     * The list will most likely just containa single double array holding the computed values. In cases where a
     * descriptor computes mutliple features (for example SIFT, where a vector is calculated for each point of
     * interest), the list will contain several double arrays.
     *
     * @return list of feature vectors.
     */
    @Override
    public List<double[]> getFeatures() {
        return data;
    }

    /**
     * Adds a Property change listener for this feature vector.
     *
     * During the computation of the descriptor, at least 2 progress events should be fired (Start/End).
     *
     * @see Progress#START
     * @see Progress#END
     * @see Progress
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Adds the double array to the list. Keep in mind that the array is NOT copied but directly put into the list.
     *
     * @see #data
     */
    protected void addData(double[] data) {
        this.data.add(data);
    }

    /**
     * Converts the given int array to double[] and adds this array to the list.
     *
     * @see #data
     */
    protected void addData(int[] data) {
        this.data.add(Arrays2.convertToDouble(data));
    }

    /**
     * Adds the double array list to the list of data arrays. Keep in mind that the array is NOT copied but directly put
     * into the list.
     *
     * @param data
     * @see #data
     */
    protected void addData(List<double[]> data) {
        this.data.addAll(data);
    }

    /**
     * Propagates the given progress event using property change support.
     *
     * The old value of the firePropertyChange is the most recent progress event that was propagated by this method
     * (null in case of the first event).
     *
     * @param event
     */
    protected void firePropertyChange(Progress event) {
        pcs.firePropertyChange(Progress.getName(), previous, event);
        previous = event;
    }

    @Override
    public void setProperties(LibProperties properties) throws IOException {
    }

    @Override
    public EnumSet<de.lmu.ifi.dbs.jfeaturelib.Descriptor.Supports> supports() {
        return DOES_ALL;
    }

    protected void startProgress() {
        pcs.firePropertyChange(Progress.getName(), null, Progress.START);
    }

    protected void endProgress() {
        pcs.firePropertyChange(Progress.getName(), null, Progress.END);
    }

    /**
     * Sets / stores the mask applied to the referenced image processor.
     *
     * @param ip the image processor from which the mask should be taken
     */
    protected void setMask(ImageProcessor ip) {
        if (ip == null) {
            throw new NullPointerException("passed imageprocessor must not be null");
        }
        this.mask = ip.getMask();
    }

    /**
     * Check wether a pixel is inside a set mask. If no mask is applied, then the method always returns true.
     *
     * If a pixel is in the mask, this means that it should be processed.
     *
     * @param x
     * @param y
     * @return true if the pixel is in the mask and should thus be processed.
     */
    protected boolean inMask(int x, int y) {
        return mask == null || mask.get(x, y) != 0;
    }

    /**
     * @return the current mask
     */
    public ImageProcessor getMask() {
        return mask;
    }
}
