/*
 * Copyright (C) 2019 Universitat Autonoma de Barcelona - David Castells-Rufas <david.castells@uab.cat>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cat.uab.cephis.dvsSimulator;

import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dcr
 */
public class EventWriterPostProcessor implements SceneProcessor
{
    BufferedImage image;
    private ByteBuffer cpuBuf = null;
    private boolean isInitialized = false;
    private Renderer renderer;
    private FrameBuffer offBuffer;
    
    private final int w;
    private final int h;
    private final ChronocamEventsWriter writer;

    double timestamp = 0;

    //private float slowDownFactor;
    private final ManualTimer timer;
    private int[][] lastImage;
    private int EVENT_DIFF_THRESHOLD = 2;
    private int MONOCHROME_DIVIDER = 1;
    
    int numFrames = 0;
    
    private boolean writeEnabled = true;
            
    EventWriterPostProcessor(int w, int h, File file, ManualTimer timer) throws IOException
    {
        this.w = w;
        this.h = h;
        this.timer = timer;
        
        cpuBuf = BufferUtils.createByteBuffer(w * h * 4);
        
                // create offscreen framebuffer
        offBuffer = new FrameBuffer(w, h, 1);


        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Image.Format.Depth);
        offBuffer.setColorBuffer(Image.Format.RGBA8);
//        offBuffer.setColorTexture(offTex);

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
        
        writer = new ChronocamEventsWriter(file);
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp)
    {
        System.out.println("DVSCam: Initializing");

        renderer = rm.getRenderer();
        
        isInitialized = true;
        
        vp.setOutputFrameBuffer(offBuffer);       
        
        lastImage = new int[w][h];
                
//        vp.
    }

    @Override
    public void reshape(ViewPort vp, int w, int h)
    {
        System.out.println("DVSCam: Reshaping");
        
        // nothing
        
        //set viewport to render to offscreen framebuffer

    }

    @Override
    public boolean isInitialized()
    {
        return isInitialized;
    }

    @Override
    public void preFrame(float f)
    {
//        System.out.println("pre frame " + f);
        timestamp += f; //  / slowDownFactor;

    }
    
    

    @Override
    public void postQueue(RenderQueue rq)
    {
        // nothing
        //System.out.println("DVSCam: Post QUEUE");
    }

    @Override
    public void postFrame(FrameBuffer fb)
    {
        updateImageContents(fb);
        
        timer.incrementTimer();
        
        numFrames++;
    }

    public void updateImageContents(FrameBuffer fb)
    {
        if (cpuBuf == null)
            return;
        
        cpuBuf.clear();
//        renderer.readFrameBuffer(offBuffer, cpuBuf);
        renderer.readFrameBuffer(fb, cpuBuf);

        Screenshots.convertScreenShot2(cpuBuf.asIntBuffer(), image);    
        
        newImage(image);
    }
    
    @Override
    public void cleanup()
    {
        System.out.println("DVSCam: cleanup");
        try
        {
            writer.close();
        } catch (IOException ex)
        {
            Logger.getLogger(EventWriterPostProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setProfiler(AppProfiler ap)
    {
        // nothing
    }

    private void newImage(BufferedImage image)
    {
        int[][] nm = toMonochrome(image);
        
        // compute difference
        
        for (int y=0; y < h; y++)
            for (int x=0; x < w; x++)
            {
                int v = nm[x][y] - lastImage[x][y];

                if (Math.abs(v) >= EVENT_DIFF_THRESHOLD)
                {
                    int inc = (v<0)? -1 : 1;
                    int pol = (v<0)? 0 : 1;

//                    System.out.println("Ev: " + x + "," + y + " =" + v);

                    if (numFrames > 0)
                        // Generate events for second frame on
                        for (int i=0; i != v; i += inc)
                        {
                            // generate a train of events when change threshold is
                            // passed
                            if (writeEnabled)
                                writer.onEvent(timestamp, x, y, pol);
                        }   
                    
                    lastImage[x][y] = nm[x][y];
                }
            }
        
    }

    /**
     * Convert captured bufferedimage to a matrix of grey values
     * considering
     * 1) y axis is inverted
     * @param image
     * @return 
     */
    int[][] toMonochrome(BufferedImage image)
    {
        int[][] ret = new int[w][h];
        
        for (int y=0; y < h; y++)
            for (int x = 0; x < w; x++)
            {
                int lc = image.getRGB(x, h-1-y);
                int lm = toMonochrome(lc);
                ret[x][y] = lm; 
            }
        
        return ret;
    }
    /**
     * @deprecated not used anymore
     * Copmute difference considering
     * 1) convert color to monochrome
     * 2) y axis is inverted
     * @param lastImage
     * @param image
     * @return 
     */
    private int[][] computeMonochromeDifference(BufferedImage lastImage, BufferedImage image)
    {
        int[][] ret = new int[w][h];
        
        for (int y=0; y < h; y++)
            for (int x = 0; x < w; x++)
            {
                int lc = lastImage.getRGB(x, h-1-y);
                int lm = toMonochrome(lc);
                
                int nc = image.getRGB(x, h-1-y);
                int nm = toMonochrome(nc);
                
                int pol = nm - lm;
                
                if (pol > 0)
                    pol = pol;
                
                ret[x][y] = pol; 
            }                
        
        return ret;
    }

    private int toMonochrome(int lc)
    {
        if (lc != 0xFF000000)
            lc = lc;
        
        int m = (lc >> 16) & 0xFF;
        m += (lc >> 8) & 0xFF;
        m += (lc & 0xFF);
        
        int r = m/3;
        
        r /= MONOCHROME_DIVIDER;
        
        return r;
    }

    private BufferedImage clone(BufferedImage image)
    {
        BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        
        for (int y = 0; y < image.getHeight(); y++)
           for (int x = 0; x < image.getWidth(); x++)
           {
               int rgb = image.getRGB(x, y);
               
               ret.setRGB(x, y, rgb);
           }
        
        return ret;
    }

    

    
}
