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
public class ResourceManager {

    private final String default_acoustic_model_dir_path;
    private final String default_language_model_file_path;
    private final String default_dictionary_file_path;
    private final String default_config_xml_file_path;
    private final String default_corpus_file_path;
    private final String fileids_path;
    private final String transcription_path;
    private final String base_dir_path;
    private final String wav_dir_path;
    private final String default_audio_config_xml_file_path;
    private final String icon_path;
    private final String default_vocab_correction_file_path;
    private final String corpus_dir_path;
    

    public String getCorpus_dir_path() {
        return corpus_dir_path;
    }
    public String getDefault_vocab_correction_file_path() {
        return default_vocab_correction_file_path;
    }
    
    private final String lm_dir_path;

    public String getLm_dir_path() {
        return lm_dir_path;
    }

    public String getIcon_path() {
        return icon_path;
    }

    public String getDefault_audio_config_xml_file_path() {
        return default_audio_config_xml_file_path;
    }
    public String getWav_dir_path() {
        return wav_dir_path;
    }
    
    private final String bw_tool_path;
    private final String sphinx_fe_tool_path;
    private final String mllr_solve_tool_path;
    private final String map_adapt_tool_path;

    public String getMap_adapt_tool_path() {
        return map_adapt_tool_path;
    }

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

    public String getDefault_corpus_file_path() {
        return default_corpus_file_path;
    }
    public String getDefault_config_xml_file_path() {
        return default_config_xml_file_path;
    }
   
    public String getDefault_acoustic_model_dir_path() {
        return default_acoustic_model_dir_path;
    }

    public String getDefault_language_model_file_path() {
        return default_language_model_file_path;
    }

    public String getDefault_dictionary_file_path() {
        return default_dictionary_file_path;
    }
  
  
    public ResourceManager(){
        // Default files
        default_acoustic_model_dir_path = "etc\\es_acoustic_model";
        default_language_model_file_path = "etc\\linguist\\lm_3gram.lm";
        default_dictionary_file_path = "etc\\linguist\\vocab_phoneme.dict";
        default_config_xml_file_path = "etc\\config_xml\\config.xml";
        default_corpus_file_path = "etc\\corpus\\lm3gram.txt";
        fileids_path = "wav\\Alex\\test.fileids"; //remember to omit
        transcription_path = "wav\\Alex\\test.transcription"; //remember to omit
        base_dir_path = "wav\\Alex"; //remember to omit
        
        wav_dir_path = "wav";
        icon_path = "file:etc\\icon";
        default_audio_config_xml_file_path = "etc\\config_xml\\spectrogram.config.xml";
        
        // Sphinx tools path
        bw_tool_path = "etc\\tools\\bw.exe";
        sphinx_fe_tool_path = "etc\\tools\\sphinx_fe.exe";
        mllr_solve_tool_path = "etc\\tools\\mllr_solve.exe";
        map_adapt_tool_path = "etc\\tools\\map_adapt.exe";
        
        //Language model path
        lm_dir_path = "lm";
        default_vocab_correction_file_path = "etc\\word_correction\\vocab_correction.json";
        corpus_dir_path = "etc\\corpus";
        
    }

    public String getBw_tool_path() {
        return bw_tool_path;
    }

    public String getFileids_path() {
        return fileids_path;
    }
    

}
