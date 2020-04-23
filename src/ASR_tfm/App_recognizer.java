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
import java.io.UnsupportedEncodingException;
import java.util.Map;
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
    //private final String language_model_path1 = "file:C:\\Users\\alexf\\Documents\\GitHub\\clean-repo\\LM\\lm_4gram.bin";
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
    
    public void loadConfig(Map<String, String> global_prop){
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
            
            //System.out.println("Config reloaded");
            recognizer.loadConfig(context);
            System.out.println("Config reloaded");
            //recognizer.loadTransform("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\wav\\Alex\\mllr_matrix", 1); // Load MLLR
        }
        catch(Exception ex){
             System.out.println(ex.getMessage());
        }
        
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
        Logger_status.Log("Preparado para escuchar. Presiona PLAY para empezar.",Logger_status.LogType.INFO);
        
    }
    
    @Override
    public void run(){
        
        while(!StopThread){
            
            Logger_status.Log("Reconociendo....",Logger_status.LogType.INFO);
            String utf = "holis";
            try {
                String utterance = new String(recognizer.getResult().getHypothesis());
                utf = new String(utterance.getBytes("ISO-8859-1"), "UTF-8");
                
                app_gui.report_txt.setCaretPosition(app_gui.report_txt.getDocument().getLength());
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(App_recognizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch(NullPointerException ex){
                //Logger_status.Log("No se detecta micrófono",Logger_status.LogType.ERROR);
                StopThread = true;
            }
            
        }
        
    }


    public void stopRecognition(){
        //this.IsStart = false;
        recognizer.stopRecognition();
        //app_gui.report_txt.setEnabled(false);
        Logger_status.Log("Reconocedor detenido.",Logger_status.LogType.INFO);
    }
    
    public void closeRecognition(){
        recognizer.closeRecognition();
    }
    
    public void initRecognition(){
        
        recognizer.initRecognition();
    }
    public void startRecognition(){
      
        recognizer.startRecognition(true);
        Logger_status.Log("Reconociendo....",Logger_status.LogType.INFO);            
    }
    
    public void loadSpeakerMLLR(String name){
        Resource_manager rm = new Resource_manager();
        
        try {
            recognizer.loadTransform(rm.getWav_dir_path()+"\\"+name+"\\mllr_matrix", 1); // Load MLLR
        } catch (Exception ex) {
            Logger.getLogger(App_recognizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
}

