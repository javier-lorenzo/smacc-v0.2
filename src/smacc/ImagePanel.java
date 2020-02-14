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

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_I32;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.util.List;

import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage img = null;
    private double scale = 1;

    public void setImg(BufferedImage img) {
       
        this.img = scale(img);
        setPreferredSize(new Dimension(this.img.getWidth(), this.img.getHeight()));
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public BufferedImage getImg() {
        return img;
    }
    public Planar<GrayU8> getImgColor() 
    {
        return ConvertBufferedImage.convertFrom(img, true, ImageType.pl(3, GrayU8.class));
    }
    public Planar<GrayF32> getImgColorRGB() 
    {
        return ConvertBufferedImage.convertFrom(img, true, ImageType.pl(3, GrayF32.class));
    }
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, this); // see javadoc for more info on the parameters            
    }
    
    private BufferedImage scale(BufferedImage before) 
    {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp
                = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage after = scaleOp.filter(before, null);
        BufferedImage afterRGB = convert(after, TYPE_INT_RGB);
        return afterRGB;
    }
    
        public BufferedImage ridemension(BufferedImage before, double scale1) 
    {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale1, scale1);
        AffineTransformOp scaleOp
                = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage after = scaleOp.filter(before, null);
        BufferedImage afterRGB = convert(after, TYPE_INT_RGB);
        return afterRGB;
    }
    
        private BufferedImage convert(BufferedImage src, int bufImgType) {
        BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return image;
    }
        
        
    void writeParticlesId(List<PlasticPiece> blobs, boolean drawBB) {
        Graphics graphics = img.getGraphics();
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
        int GREEN = Color.GREEN.getRGB();
           int count = 0;
       for (PlasticPiece b : blobs) {

            List<Point2D_I32> puntosContorno = b.getContour().external;
            for (Point2D_I32 p : puntosContorno) {
                img.setRGB((int) (p.x ), (int) (p.y ), GREEN);
            }
            String text = String.valueOf(b.getId());
            g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);

        }
    }
    
     void writeLineOrNot(List<PlasticPiece> blobs, boolean drawBB) {
        Graphics graphics = img.getGraphics();
        Graphics2D g = (Graphics2D) graphics;
       
        
        int GREEN = Color.GREEN.getRGB();
        int RED = Color.RED.getRGB();
        int YELLOW = Color.YELLOW.getRGB();
        int BLUE = Color.BLUE.getRGB();
        int BLACK = Color.BLACK.getRGB();
                int ORANGE = Color.ORANGE.getRGB();

        int count = 0;
        for (PlasticPiece b : blobs) {
            if(b.getParticleType() == "LINE"){
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), GREEN);
                }
                 
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                String text = String.valueOf(b.getId() + " Line");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
            else if(b.getParticleType() == "FRA"){
                 
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), RED);
                }
                String text = String.valueOf(b.getId() + " Fra");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
            else if(b.getParticleType() == "ORG"){
                 
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), YELLOW);
                }
                String text = String.valueOf(b.getId() + " Org");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
            else if(b.getParticleType() == "TAR"){
                 
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), BLUE);
                }
                String text = String.valueOf(b.getId() + " Tar");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
            else if(b.getParticleType() == "PEL"){
                 
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), BLACK);
                }
                String text = String.valueOf(b.getId() + " Pel");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
            else if(b.getParticleType() == "REST"){
                 
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (50 * scale)));
                List<Point2D_I32> puntosContorno = b.getContour().external;
                for (Point2D_I32 p : puntosContorno) {
                    img.setRGB((int) (p.x), (int) (p.y ), ORANGE);
                }
                String text = String.valueOf(b.getId() + " REST");
                g.drawString(text, b.getPoints().get(0).x,b.getPoints().get(0).y);
            }
        }
    }

    
    void setImg(BufferedImage iamgeColored, BufferedImage iamgeColored0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
