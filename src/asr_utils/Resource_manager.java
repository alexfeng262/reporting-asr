/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asr_utils;

import java.net.URISyntaxException;

/**
 *
 * @author alexf
 */
public class Resource_manager {

    private final String acoustic_model_dir_path;
    private final String language_model_path;
    private final String dictionary_path;
    private final String config_xml_path;
    private final String corpus_path;
    private final String fileids_path;
    private final String transcription_path;
    private final String base_dir_path;
    
    private final String bw_tool_path;
    private final String sphinx_fe_tool_path;
    private final String mllr_solve_tool_path;

    public String getSphinx_fe_tool_path() {
        return sphinx_fe_tool_path;
    }

    public String getMllr_solve_tool_path() {
        return mllr_solve_tool_path;
    }
    
    public String getBase_dir_path() {
        return base_dir_path;
    }
    
    public String getTranscription_path() {
        return transcription_path;
    }

    public String getCorpus_path() {
        return corpus_path;
    }
    public String getConfig_xml_path() {
        return config_xml_path;
    }
   
    public String getAcoustic_model_dir_path() {
        return acoustic_model_dir_path;
    }

    public String getLanguage_model_path() {
        return language_model_path;
    }

    public String getDictionary_path() {
        return dictionary_path;
    }
    

    public Resource_manager(){
        acoustic_model_dir_path = "C:\\Users\\alexf\\Documents\\GitHub\\ASR_SPHINX4\\src\\main\\resources\\es_acoustic_model";
        language_model_path = "file:C:\\Users\\alexf\\Documents\\GitHub\\ASR_SPHINX4\\src\\main\\resources\\linguist\\lm_3gram.bin";
        dictionary_path = "C:\\Users\\alexf\\Documents\\GitHub\\ASR_SPHINX4\\src\\main\\resources\\linguist\\vocab_phoneme.dict";
        config_xml_path = "C:\\Users\\alexf\\Documents\\GitHub\\ASR_SPHINX4\\src\\main\\resources\\config_xml\\config.xml";
        corpus_path = "C:\\Users\\alexf\\Documents\\GitHub\\ASR_SPHINX4\\src\\main\\resources\\corpus\\result.txt";
        fileids_path = "C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\Alex\\test.fileids";
        transcription_path = "C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\Alex\\test.transcription";
        base_dir_path = "C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\Alex";
        
        bw_tool_path = "C:\\sphinx2\\sphinxtrain\\bin\\Release\\Win32\\bw.exe";
        sphinx_fe_tool_path = "C:\\sphinx2\\sphinxbase\\bin\\Release\\Win32\\sphinx_fe.exe";
        mllr_solve_tool_path = "C:\\sphinx2\\sphinxtrain\\bin\\Release\\Win32\\mllr_solve.exe";
        
    }

    public String getBw_tool_path() {
        return bw_tool_path;
    }

    public String getFileids_path() {
        return fileids_path;
    }
    

}
