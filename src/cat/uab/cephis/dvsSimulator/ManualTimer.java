/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.uab.cephis.dvsSimulator;

import com.jme3.system.Timer;

/**
 *
 * @author dcr
 */
public class ManualTimer extends Timer
{

    private long time;          // in nanoseconds
    private long resolution = (long) 1E9;
    private final double fps;
    private final long increment;
    private long lastInformedTime;

    
    
    public ManualTimer(double fps)
    {
        this.fps = fps;
        this.increment = (long) (resolution / fps);
    }

    @Override
    public long getTime()
    {
        return time;
    }

    /**
     * Return the number of unit increments per second.
     * We are working in ns, so I will report nanoseconds/second
     * @return 
     */
    @Override
    public long getResolution()
    {
        return resolution;
    }

    @Override
    public float getFrameRate()
    {
        return (float) fps;
    }

    @Override
    public float getTimePerFrame()
    {
        return (float) (1.0 / fps);
    }

    @Override
    public void update()
    {
//        System.out.println("Update time?");
    }

    @Override
    public void reset()
    {
        System.out.println("DVSCam: reset timer");
        time = (long) 0.0;
    }

    void incrementTimer()
    {
        time += increment;

        if ((lastInformedTime + resolution) < time)
        {
            System.out.println("DVSCam time: "+ getTimeInSeconds());
            lastInformedTime = time;
        }
    }

    
    
}
