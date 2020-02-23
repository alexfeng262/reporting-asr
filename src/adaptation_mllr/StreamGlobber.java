/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_mllr;

import ASR_tfm.app_gui;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author alexf
 */
class StreamGobbler extends Thread
{
    InputStream is;
 
    
    StreamGobbler(InputStream is)
    {
        this.is = is;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                app_gui.print_mllr_process(line);
                //System.out.println(line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}