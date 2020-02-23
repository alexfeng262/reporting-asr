/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_mllr;

import asr_utils.Resource_manager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexf
 */
public class Mllr_solve {
    private static String[] cmd;
    
    public Mllr_solve(String name){
        cmd = new String[9];
        Resource_manager rm = new Resource_manager();
        
        cmd[0] = rm.getMllr_solve_tool_path();
        cmd[1] = "-meanfn";
        cmd[2] = rm.getAcoustic_model_dir_path()+"\\means";
        cmd[3] = "-varfn";
        cmd[4] = rm.getAcoustic_model_dir_path()+"\\variances";
        cmd[5] = "-outmllrfn";
        cmd[6] = rm.getWav_dir_path()+"\\"+name+"\\mllr_matrix";
        cmd[7] = "-accumdir";
        cmd[8] = rm.getWav_dir_path()+"\\"+name;
    }
    
    public void exec_mllr_solve(){
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
                System.out.println("mllr_solve.exe finished");
            else
               System.out.println("somethings occured in mllr_solve.exe");
        } catch (IOException | InterruptedException  ex) {
            Logger.getLogger(Sphinx_fe.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
