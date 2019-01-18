/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.uab.cephis.dvsSimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dcr
 */
public class ChronocamEventsWriter implements PolarityEventsListener
{
    private final RandomAccessFile raf;
    
    double timeStampDivisor = 1000000.0;


    public ChronocamEventsWriter(File file) throws IOException
    {
        this.raf = new RandomAccessFile(file, "rw");
        
        writeHeader();
    }
    
    private int endianSwap(int v)
    {
        int a = v & 0xFF;
        int b = (v>>8) & 0xFF;
        int c = (v>>16) & 0xFF;
        int d = (v>>24) & 0xFF;
        
        return (a << 24) | (b<<16) | (c<<8) | d;
    }

    @Override
    public void onEvent(double timeStamp, int x, int y, int pol)
    {
        int timeStampReading = (int) (timeStamp * timeStampDivisor);        
        timeStampReading = endianSwap(timeStampReading);

        int event = x | (y << 9) | (pol << 17);
        event = endianSwap(event);
        
        try
        {
            raf.writeInt(timeStampReading);
            raf.writeInt(event);
        } 
        catch (IOException ex)
        {
            Logger.getLogger(ChronocamEventsWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private void writeHeader() throws IOException
    {
        writeComments();        
        writeEventsType();
    }

    private void writeComments() throws IOException
    {
        raf.writeBytes("% produced by Cephis \n");
    }

    private void writeEventsType() throws IOException
    {
        int eventsType = 0;
        int eventsSize = 8;
        
        raf.writeByte(eventsType);
        raf.writeByte(eventsSize);
    }

    public void close() throws IOException
    {
        raf.close();
    }
}
