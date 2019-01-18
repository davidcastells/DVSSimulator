/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.uab.cephis.dvsSimulator;

import java.awt.Point;

/**
 *
 * @author dcr
 */
public class PolarityEvent implements TimestampedEvent
{
    public final double timeStamp;
    public final int x;
    public final int y;
    public int pol;

    public static double distance(PolarityEvent le, PolarityEvent re)
    {
        return Point.distance(le.x, le.y, re.x, re.y);
    }
    
    public PolarityEvent(double timeStamp, int x, int y, int pol)
    {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.pol = pol;
    }

    public Point toPoint()
    {
        return new Point(x, y);
    }

    @Override
    public String toString()
    {
        return "(" + x + "," + y + ") @ " + String.format("%.10f", timeStamp) + " = " + pol;
    }

    @Override
    public double getTimestamp()
    {
        return timeStamp;
    }

    
}
