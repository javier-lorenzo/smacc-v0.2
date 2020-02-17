/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enricosantesarti
 */
public class ResultManager {

    void saveFeaturesToCSV(List<PlasticPiece> blobs, File filename) {
        FileWriter writerCSV = null;
        int numeroTotalePEzz = blobs.size();
        int numeroFRA = 0;
        int numeroORG = 0;
        int numeroPEL = 0;
        int numeroLINE = 0;
        int numeroTAR = 0;
        for (PlasticPiece i : blobs) {
            if (null != i.getParticleType()) {
                switch (i.getParticleType()) {
                    case "FRA":
                        numeroFRA++;
                        break;
                    case "PEL":
                        numeroPEL++;
                        break;
                    case "TAR":
                        numeroTAR++;
                        break;
                    case "ORG":
                        numeroORG++;
                        break;
                    default:
                        numeroLINE++;
                        break;
                }
            }
        }
        try {
            writerCSV = new FileWriter(filename.getAbsolutePath());

            Iterator<PlasticPiece> iterBlob = blobs.iterator();

//            write header line
            PlasticPiece b = iterBlob.next();
            //String featureNames[] = b.getGeometricFeatures().featureNames;
            String line = "Id" + ";area" + ";perimeter"
                    + ";avgR" + ";avgG" + ";avgB" + ";type";

            line += "\n";
            writerCSV.write(line);
//            writerCSV.write("\n");

            iterBlob = blobs.iterator();
            while (iterBlob.hasNext()) {
                b = iterBlob.next();
                GeometricFeature geometricFeatures = b.getGeometricFeatures();
                ColorFeature colorFeatures = b.getColorFeatures();

                line = String.valueOf(b.getId());
                line += ";" + String.format("%.1f", (double) geometricFeatures.area).replace(",", ".");
                line += ";" + String.format("%.1f", (double) geometricFeatures.perimeter).replace(",", ".");

                for (int i = 1; i < 4; i++) {
                    line += ";" + String.format("%.5f", colorFeatures.avgRGB[i]).replace(",", ".");
                }

                line += ";" + b.getParticleType() + "\n";
//                System.out.println("linea->" + line);
                writerCSV.write(line);
            }
            String lineaFinale = "Total number of plastic pieces: " + numeroTotalePEzz + "\nTAR: " + numeroTAR + "\nFRA: " + numeroFRA + "\nPEL: " + numeroPEL + "\nLINE: " + numeroLINE + "\nORG: " + numeroORG;
            writerCSV.write(lineaFinale);
        } catch (IOException ex) {
            Logger.getLogger(ResultManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writerCSV.close();
            } catch (IOException ex) {
                Logger.getLogger(ResultManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void saveFeaturesToCSVVEro(List<PlasticPiece> blobs, File filename) {
        FileWriter writerCSV = null;
        try {
            writerCSV = new FileWriter(filename.getAbsolutePath());

            Iterator<PlasticPiece> iterBlob = blobs.iterator();

//            write header line
            PlasticPiece b = iterBlob.next();
            //String featureNames[] = b.getGeometricFeatures().featureNames;
            String line = "Id" + ",area" + ",perimeter" + ",compactness" + ",radiusRatio"
                    + ",widthHeightRatio" + ",BBtoAreaRatio"
                    + ",ellipseAxisRatio"
                    + ",avgR" + ",avgG" + ",avgB"
                    + ",varR" + ",varG" + ",varB"
                    + ",avgH" + ",avgS" + ",avgV"
                    + ",varH" + ",varS" + ",varV";
            for (int i = 0; i < b.getLbp().length; i++) {
                line += ",lbp" + (i + 1);
            }
            line += ",class\n";
            writerCSV.write(line);
//            writerCSV.write("\n");

            iterBlob = blobs.iterator();
            while (iterBlob.hasNext()) {
                b = iterBlob.next();
                GeometricFeature geometricFeatures = b.getGeometricFeatures();
                ColorFeature colorFeatures = b.getColorFeatures();

                line = String.valueOf(b.getId());
                line += "," + String.format("%.1f", (double) geometricFeatures.area).replace(",", ".");
                line += "," + String.format("%.1f", (double) geometricFeatures.perimeter).replace(",", ".");
                line += "," + String.format("%.5f", geometricFeatures.compactness).replace(",", ".");
                line += "," + String.format("%.5f", geometricFeatures.radiusRatio).replace(",", ".");
                line += "," + String.format("%.5f", geometricFeatures.widthHeightRatio).replace(",", ".");
                line += "," + String.format("%.5f", geometricFeatures.BBtoAreaRatio).replace(",", ".");
                line += "," + String.format("%.5f", geometricFeatures.ellipseAxisRatio).replace(",", ".");

                for (int i = 1; i < 4; i++) {
                    line += "," + String.format("%.5f", colorFeatures.avgRGB[i]).replace(",", ".");
                }
                for (int i = 1; i < 4; i++) {
                    line += "," + String.format("%.5f", colorFeatures.varRGB[i]).replace(",", ".");
                }
                for (int i = 0; i < 3; i++) {
                    line += "," + String.format("%.5f", colorFeatures.avgHSV[i]).replace(",", ".");
                }
                for (int i = 0; i < 3; i++) {
                    line += "," + String.format("%.5f", colorFeatures.varHSV[i]).replace(",", ".");
                }
                for (int i = 0; i < b.getLbp().length; i++) {
                    line += "," + String.format("%.5f", b.getLbp()[i]).replace(",", ".");
                }
                line += ",PEL\n";
//                System.out.println("linea->" + line);
                writerCSV.write(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writerCSV.close();
            } catch (IOException ex) {
                Logger.getLogger(ResultManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
