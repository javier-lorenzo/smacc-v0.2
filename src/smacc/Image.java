 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smacc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author enricosantesarti
 */
public class Image 
{
    
        private BufferedImage ImageColored;
        private String title;
        
        public Image(File filename) throws IOException
        {
        this.ImageColored = null;
            this.ImageColored = ImageIO.read(filename);
            this.title = filename.getName();            
        }
        
        public int getWidth()
        {
            return ImageColored.getWidth();
        }
         public int getHeight()
        {
            return ImageColored.getHeight();
        }
        
        public void setTitle(String title)
        {
            this.title = title;
        }
        public String getTitle()
        {
            return title;
        }
        public BufferedImage getIamgeColored() 
        {
            return ImageColored;
        }
}
