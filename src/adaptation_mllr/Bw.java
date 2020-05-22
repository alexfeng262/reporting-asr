/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_mllr;

import ASR.AppGui;
import asr_utils.ResourceManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author alexf
 */
public class Bw {
    private static String[] cmd;
    
    public Bw(String name){
        cmd = new String[23];
        ResourceManager rm = new ResourceManager();
        cmd[0] = rm.getBw_tool_path();
        cmd[1] = "-hmmdir";
        cmd[2] = rm.getDefault_acoustic_model_dir_path();
        cmd[3] = "-moddeffn";
        cmd[4] = rm.getDefault_acoustic_model_dir_path() + "\\mdef";
        cmd[5] = "-ts2cbfn";
        cmd[6] = ".ptm.";
        cmd[7] = "-feat";
        cmd[8] = "1s_c_d_dd";
        cmd[9] = "-svspec";
        cmd[10] = "0-12/13-25/26-38";
        cmd[11] = "-cmn";
        cmd[12] = "current";
        cmd[13] = "-agc";
        cmd[14] = "none";
        cmd[15] = "-dictfn";
        cmd[16] = rm.getDefault_dictionary_file_path();
        //cmd[16] = rm.getWav_dir_path()+"\\"+name+"\\vocab.dict";
        cmd[17] = "-ctlfn";
        cmd[18] = rm.getWav_dir_path()+"\\"+name+"\\test.fileids";
        cmd[19] = "-lsnfn";
        cmd[20] = rm.getWav_dir_path()+"\\"+name+"\\test.transcription";
        cmd[21] = "-accumdir";
        cmd[22] = rm.getWav_dir_path()+"\\"+name;
    }
    
    public void exec_bw(){
        
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
                System.out.println("bw.exe finished");
            else
               System.out.println("somethings occured in bw.exe");
        } catch (IOException | InterruptedException  ex) {
            AppGui.showMessageGUI("Excepcion de tipo: "+ ex.getMessage(), "error");
        }
    
    }
}
