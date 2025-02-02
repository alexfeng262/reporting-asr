/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_map;

import ASR.AppGui;
import asr_utils.ResourceManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexf
 */


public class Sphinx_fe {
    
    private static String[] cmd;
    
    public Sphinx_fe(String name){
        cmd = new String[23];
        ResourceManager rm = new ResourceManager();
        
        cmd[0] = rm.getSphinx_fe_tool_path();
        cmd[1] = "-argfile";
        cmd[2] = rm.getDefault_acoustic_model_dir_path()+"\\feat.params";
        cmd[3] = "-samprate";
        cmd[4] = "16000";
        cmd[5] = "-c";
        cmd[6] = rm.getWav_dir_path()+"\\"+name+"\\test.fileids";
        cmd[7] = "-eo";
        cmd[8] = "mfc";
        cmd[9] = "-mswav";
        cmd[10] = "yes";
        cmd[11] = "-ei";
        cmd[12] = "wav";
        cmd[13] = "-nfilt";
        cmd[14] = "25";
        cmd[15] = "-transform";
        cmd[16] = "dct";
        cmd[17] = "-lifter";
        cmd[18] = "22";
        cmd[19] = "-lowerf";
        cmd[20] = "130";
        cmd[21] = "-upperf";
        cmd[22] = "6800";
        
        
    }
    
    public void exec_sphinx_fe(){
        System.out.println(String.join(" ", cmd));
        Runtime command_prompt = Runtime.getRuntime();
        try {
            
            Process shell = command_prompt.exec(cmd);
            StreamGobbler errorGobbler = new 
                StreamGobbler(shell.getErrorStream());
             StreamGobbler outputGobbler = new 
                StreamGobbler(shell.getInputStream());
             
            errorGobbler.start(); 
            outputGobbler.start();
            int exitVal = shell.waitFor();
            System.out.println("ExitValue: " + exitVal);
            
            if(exitVal == 0)
                System.out.println("sphinx_fe finished");
            else
               System.out.println("somethings occured in sphinx_fe");
        } catch (IOException | InterruptedException  ex) {
            AppGui.showMessageGUI("Excepcion de tipo: "+ ex.getMessage(), "error");
        }
    
    }
    
   
    
   
    
}
