/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_mllr;

import ASR.AppGui;
import asr_utils.ResourceManager;
import java.io.IOException;

/**
 *
 * @author alexf
 */
public class Map_adapt {
    private static String[] cmd;
    
    public Map_adapt(String name){
        cmd = new String[23];
        ResourceManager rm = new ResourceManager();
        
        cmd[0] = rm.getMap_adapt_tool_path();
        cmd[1] = "-moddeffn";
        cmd[2] = rm.getDefault_acoustic_model_dir_path()+"\\mdef";
        cmd[3] = "-ts2cbfn";
        cmd[4] = ".ptm.";
        cmd[5] = "-meanfn";
        cmd[6] = rm.getDefault_acoustic_model_dir_path()+"\\means";
        cmd[7] = "-varfn";
        cmd[8] = rm.getDefault_acoustic_model_dir_path()+"\\variances";
        cmd[9] = "-mixwfn";
        cmd[10] = rm.getDefault_acoustic_model_dir_path()+"\\mixture_weights";
        cmd[11] = "-tmatfn";
        cmd[12] = rm.getDefault_acoustic_model_dir_path()+"\\transition_matrices";
        cmd[13] = "-accumdir";
        cmd[14] = rm.getWav_dir_path()+"\\"+name;
        cmd[15] = "-mapmeanfn";
        cmd[16] = rm.getWav_dir_path()+"\\"+name+"\\means";
        cmd[17] = "-mapvarfn";
        cmd[18] = rm.getWav_dir_path()+"\\"+name+"\\variances";
        cmd[19] = "-mapmixwfn";
        cmd[20] = rm.getWav_dir_path()+"\\"+name+"\\mixture_weights";
        cmd[21] = "-maptmatfn";
        cmd[22] = rm.getWav_dir_path()+"\\"+name+"\\transition_matrices";
        
    }
    
    public void exec_map_adapt(){
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
                System.out.println("map_adapt.exe finished");
            else
               System.out.println("somethings occured in map_adapt.exe");
        } catch (IOException | InterruptedException  ex) {
            AppGui.showMessageGUI("Excepcion de tipo: "+ ex.getMessage(), "error");
        }
        
    }
    
}
