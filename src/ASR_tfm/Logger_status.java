/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR_tfm;

/**
 *
 * @author alexf
 */
public class Logger_status {
     public static enum LogType { INFO, WARNING, ERROR }
     
     public static void Log(String msg, LogType type){
        String log = "";
        if(null == type){
            log = "No message of type "+ type;
        }
        else switch (type) {
            case INFO:
                log = "INFO:: "+msg;
                break;
            case WARNING:
                log = "WARNING:: "+msg;
                break;
            case ERROR:
                log = "ERROR:: "+msg;
                break;
            default:
                log = "No message of type "+ type;
                break;
        }
        AppGui.status_bar.setText(log);
    }
}
