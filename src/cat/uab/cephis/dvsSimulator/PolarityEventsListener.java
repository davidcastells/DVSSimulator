/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.uab.cephis.dvsSimulator;

/**
 *
 * @author dcr
 */
public interface PolarityEventsListener
{

    public void onEvent(double timeStamp, int x, int y, int pol);
    
}
