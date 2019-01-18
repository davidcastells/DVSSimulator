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
