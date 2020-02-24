/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR_tfm;

import asr_utils.Resource_manager;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import edu.cmu.sphinx.api.LiveSpeechRecognizer;



/**
 *
 * @author alexf
 */
public class App_recognizer extends Thread{
    private volatile Configuration configuration;
    private volatile Boolean IsStart = false;
    private volatile LiveSpeechRecognizer recognizer;
    private volatile Boolean flag = false;

    private volatile Boolean StopThread = false;
    
    private final String mllr_path = "resource:/mllr_matrix/alex_mllr";
    private final String acoustic_model_path  ;
    private final String language_model_path ;
    private final String language_model_path1 = "file:C:\\Users\\alexf\\Documents\\GitHub\\clean-repo\\LM\\lm_4gram.bin";
    private final String dictionary_path ;
   
    private final String config_xml ;
    
    //public  Context context;
  
    //private App_gui Gui;


    public App_recognizer(){
        Resource_manager props = new Resource_manager();
        acoustic_model_path = "file:"+props.getAcoustic_model_dir_path();
        language_model_path = "file:"+props.getLanguage_model_path();
        dictionary_path = "file:"+props.getDictionary_path();
        config_xml = "file:"+props.getConfig_xml_path();
        Config();
        
    }
    
    public App_recognizer(Map<String, String> global_prop){
        Resource_manager props = new Resource_manager();
        acoustic_model_path = "file:"+ props.getAcoustic_model_dir_path();
        language_model_path = "file:"+props.getLanguage_model_path();
        dictionary_path = "file:"+props.getDictionary_path();
        config_xml = "file:"+props.getConfig_xml_path();
        Config_reloaded(global_prop);
       
    }
    private void Config_reloaded(Map<String, String> global_prop){
        Logger_status.Log("Loading Model",Logger_status.LogType.INFO);
        configuration = new Configuration();
        
        configuration.setAcousticModelPath(acoustic_model_path);
        configuration.setDictionaryPath(dictionary_path);
        configuration.setLanguageModelPath(language_model_path);
        configuration.setSampleRate(16000);
        configuration.setNewConfig(global_prop);
        Context context;
      
        try{
            context = new Context(config_xml,configuration);
            //context.setGlobalProperty("languageWeight",12);
            //@TODO:
            //create configuration manager from xml
            //set new global properties
            //export to xml with configuration manager utils and replace de old.
            
            System.out.println("Config reloaded");
            recognizer = new LiveSpeechRecognizer(context);
            //recognizer.loadTransform("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\wav\\Alex\\mllr_matrix", 1); // Load MLLR
        }
        catch(Exception ex){
             System.out.println(ex.getMessage());
        }
        this.start();
        //app_gui.report_txt.append("Models ready....\n");
        Logger_status.Log("Ready to listen. Press PLAY to start.",Logger_status.LogType.INFO);
        
    }
    
    private void Config(){
        Logger_status.Log("Loading Model",Logger_status.LogType.INFO);
        configuration = new Configuration();
        
        configuration.setAcousticModelPath(acoustic_model_path);
        configuration.setDictionaryPath(dictionary_path);
        configuration.setLanguageModelPath(language_model_path);
        configuration.setSampleRate(16000);
        Context context;
        
      
        try{
            context = new Context(config_xml,configuration);
            recognizer = new LiveSpeechRecognizer(context);
            //recognizer.loadTransform("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\wav\\Alex\\mllr_matrix", 1); // Load MLLR
            
        }
        catch(Exception ex){
             System.out.println(ex.getMessage());
        }
        this.start();
        //app_gui.report_txt.append("Models ready....\n");
        Logger_status.Log("Ready to listen. Press PLAY to start.",Logger_status.LogType.INFO);
        
    }
    
    @Override
    public void run(){
        
        while(!StopThread){
            
            Logger_status.Log("Recognizing.",Logger_status.LogType.INFO);
            String utf = "holis";
            String utterance = new String(recognizer.getResult().getHypothesis());
            
            try {
                utf = new String(utterance.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(App_recognizer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(utterance.equals("salir")){
                StopThread = true;
                Stop_recognition();
                app_gui.enable_reload_model();
                Logger_status.Log("Parameters tuning enabled.",Logger_status.LogType.INFO);
            }
            else{
                app_gui.report_txt.append(utf+" ");
            }
            app_gui.report_txt.setCaretPosition(app_gui.report_txt.getDocument().getLength());
        }
        
    }
//    @Override
//    public void run(){
//       while(!StopThread){
//            //System.out.println("running");
//            while(IsStart) {
//                //System.out.println("running");
//                //App_gui.report_txt.append(recognizer.toString()+"\n");
//                
//                Logger_status.Log("Recognizing.",Logger_status.LogType.INFO);
//                String utterance = recognizer.getResult().getHypothesis();
//                System.out.println("cancel");
//                //String utterance = recognizer.getResult().getBestFinalToken();
//                /*try {
//                    recognizer.getResult().getLattice().dumpSlf(new FileWriter(new File("C:\\Users\\alexf\\Desktop\\ASR\\lattice.slf")));
//                } catch (IOException ex) {
//                     Logger_status.Log("Cannot dump lattice",Logger_status.LogType.INFO);
//                }*/
//                if(utterance.equals("salir") || !IsStart){
//                    flag = true;
//                    this.IsStart = false;
//                    this.StopThread = true;
//                    break;
//                }
//                else{
//                    app_gui.report_txt.append(utterance+"\n");
//                    app_gui.report_txt.setCaretPosition(app_gui.report_txt.getDocument().getLength());
//                }
//            }
//            if(flag){
//    
//                Logger_status.Log("Stopping recognizer.",Logger_status.LogType.INFO);
//                recognizer.closeRecognizer();
//                app_gui.play_pause_btn.setEnabled(false);
//                app_gui.reload_model_btn.setEnabled(true);
//                app_gui.play_pause_btn.setText("Play");
//                app_gui.report_txt.setEnabled(false);
//                Logger_status.Log("Recognizer stopped. Reload configuration.",Logger_status.LogType.INFO);
//                flag = false;
//            }
//                
//            
//        }
//      
//    }

    public void Pause_recognition(){
        //this.IsStart = false;
        recognizer.stopRecognition();
        //app_gui.report_txt.setEnabled(false);
        Logger_status.Log("Recognizer stopped.",Logger_status.LogType.INFO);
  

   
    }
    public void Stop_recognition(){
        recognizer.closeRecognition();
    }
    public void Start_recognition_reload(Map<String, String> global_prop){
        Config_reloaded(global_prop);
        
        recognizer.startRecognitionNormal(true);
    }
    
    public void Init_start_recognition(){
        
        recognizer.init_start_recognition();
    }
    public void Start_recognition(){
      
        recognizer.startRecognition(true);
        Logger_status.Log("Recognizing.",Logger_status.LogType.INFO);            
    }
    
    public void load_speaker_mllr(String name){
        Resource_manager rm = new Resource_manager();
        
        try {
            recognizer.loadTransform(rm.getWav_dir_path()+"\\"+name+"\\mllr_matrix", 1); // Load MLLR
        } catch (Exception ex) {
            Logger.getLogger(App_recognizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
}

