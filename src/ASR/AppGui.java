/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR;

import asr_utils.LoggerStatus;
import static adaptation_map.GenerateFiles.*;
import adaptation_map.Bw;
import adaptation_map.Map_adapt;
import adaptation_map.Sphinx_fe;
import asr_utils.Directories;
import asr_utils.ResourceManager;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
import edu.cmu.sphinx.tools.audio.AudioData;
import edu.cmu.sphinx.tools.audio.AudioPanel;
import edu.cmu.sphinx.tools.audio.AudioPlayer;
import edu.cmu.sphinx.tools.audio.CepstrumPanel;
import edu.cmu.sphinx.tools.audio.Downsampler;
import edu.cmu.sphinx.tools.audio.SpectrogramPanel;
import edu.cmu.sphinx.tools.audio.Utils;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import train_sentence_generation.SentenceGenerator;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author alexf
 */
public class AppGui extends javax.swing.JFrame {

    private AppRecognizer recognize;
    public static RecognizerConfiguration recognizerConfig;
    
    private final String relBeamWidthProp = "relativeBeamWidth";
    private final String wipProp = "wordInsertionProbability";
    private final String lwProp = "languageWeight";
    private final String pBeamProp = "phoneticBeam";
    private final String lmProp = "languageModel";
    private final String acousticProp = "acousticModel";
    private static SentenceGenerator generate ;
    private static Timer timer;
    private int seconds;
    private int minutes;
    static final ResourceManager rm = new ResourceManager();
    //*********************************************************
    static final String CONTEXT = "AudioTool";
    static final String PREFS_CONTEXT = "/edu/cmu/sphinx/tools/audio/"+CONTEXT;
    
    static final String FILENAME_PREFERENCE = "filename";
    static final String MICROPHONE = "microphone";
    static final String FRONT_END = "frontEnd";
    static final String CESPTRUM_FRONT_END = "cepstrumFrontEnd";
    static final String DATA_SOURCE = "streamDataSource";
    static final String CEPSTRUM_DATA_SOURCE = "cstreamDataSource";
    static final String WINDOWER = "windower";

    static AudioData audio;
    static JFrame jframe;
    static AudioPanel audioPanel;
    static SpectrogramPanel spectrogramPanel;
    static CepstrumPanel cepstrumPanel;
    static JFileChooser fileChooser;
    static String user_data_filename;
    static String file_text_path;
    static File file;
    static AudioPlayer player;
    static edu.cmu.sphinx.frontend.util.Microphone recorder;
    static boolean recording;
    static Preferences prefs;
    static float zoom = 1.0f;

    public AppGui() {
        
        initComponents();
        jProgressBar2.setVisible(false);
        URL iconURL;
        try {
            iconURL = new URL(rm.getIcon_path()+"\\healthcare-and-medical.png");
            ImageIcon icon = new ImageIcon(iconURL);
            this.setIconImage(icon.getImage());
        } catch (MalformedURLException ex) {
            
        }
        // iconURL is null when not found
        
        seconds = 0;
        minutes = 0;

        generate = new SentenceGenerator();
        recognize = new AppRecognizer();
        recognizerConfig = new RecognizerConfiguration();
        loadRecognizerConfiguration();
        load_audio_interface();
        
    }

    public static void showMessageGUI(String msg, String type){
        
        String log = "";
        int option_type = JOptionPane.ERROR_MESSAGE ;
        if(null == type){
            log = "ERROR";
            msg = "tipo no existe";
            option_type = JOptionPane.ERROR_MESSAGE;
        }
        else switch (type) {
            case "info":
                log = "INFORMACIÓN";
                option_type = JOptionPane.INFORMATION_MESSAGE;
                break;
            case "warning":
                log = "ALERTA";
                option_type = JOptionPane.WARNING_MESSAGE;
                break;
            case "error":
                log = "ERROR";
                option_type = JOptionPane.ERROR_MESSAGE;
                break;
        }
        JOptionPane.showMessageDialog(null, msg, 
                log, 
                option_type);
    }
     static public void getAudioFromFile(String filename) throws IOException {
        /* Supports alignment data.  The format of the alignment file
         * is as follows:
         *
         * input user_data_filename                String
         * number of (time tag) lines    int
         * time tag                      float String
         * time tag                      float String
         * time tag                      float String
         * ...
         *
         * Times are in seconds.
         */
        if (filename.endsWith(".align")) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename)));

            populateAudio(reader.readLine());

            int numPoints = Integer.parseInt(reader.readLine());
            float[] times = new float[numPoints];
            String[] labels = new String[numPoints];
            for (int i = 0; i < numPoints; i++) {
                StringTokenizer tokenizer = new StringTokenizer(
                        reader.readLine());
                while (tokenizer.hasMoreTokens()) {
                    times[i] = Float.parseFloat(tokenizer.nextToken());
                    labels[i] = tokenizer.nextToken();
                }
            }
            audioPanel.setLabels(times, labels);

            reader.close();
        } else {
            populateAudio(filename);
        }
    }
     
    static public void populateAudio(String filename) {
        
        try {
            AudioData newAudio = Utils.readAudioFile(filename);
            if (newAudio == null) {
                newAudio = Utils.readRawFile(filename);
            }
            //zoomReset();
            audio.setAudioData(newAudio.getAudioData());
            /*
            * Play only if user requests. Auto play is annoying if
            * the audio is too long
            *
            * player.play(audioPanel.getSelectionStart(),
            * audioPanel.getSelectionEnd());
           */
        } catch (IOException e) {
            /* just ignore bad files. */
        }
    }
     /* Gets a user_data_filename. */
    static public void getFilename(String title, int type) {
        
        int returnVal;
        user_data_filename = "";
        fileChooser.setDialogTitle(title);
        if(file_text_path != null)
            fileChooser.setCurrentDirectory(new File(file_text_path));
        fileChooser.setDialogType(type);

        if (type == JFileChooser.OPEN_DIALOG) {
            returnVal = fileChooser.showOpenDialog(jframe);
        } else {
           
            returnVal = fileChooser.showSaveDialog(jframe);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            file_text_path = file.getAbsolutePath(); 
            user_data_filename = file.getName();
        }
    }
    private static void load_audio_interface(){
        
        prefs = Preferences.userRoot().node(PREFS_CONTEXT);
        user_data_filename = prefs.get(FILENAME_PREFERENCE, "untitled.raw");

        try {
            
            ConfigurationManager cm = new ConfigurationManager(rm.getDefault_audio_config_xml_file_path());
            fileChooser = new JFileChooser(rm.getWav_dir_path());
            recorder = (edu.cmu.sphinx.frontend.util.Microphone) cm.lookup(MICROPHONE);
            recorder.initialize();
            audio = new AudioData();

            PropertySheet ps = cm.getPropertySheet(WINDOWER);
            float windowShiftInMs = ps.getFloat(RaisedCosineWindower.PROP_WINDOW_SHIFT_MS);
            
            float windowShiftInSamples = windowShiftInMs
                    * audio.getAudioFormat().getSampleRate() / 1000.0f;
            audioPanel = new AudioPanel(audio,
                    1.0f / windowShiftInSamples,
                    0.004f);
            
            audio_player_scroll.setViewportView(audioPanel);
            audioPanel.setAlignmentX(0.0f);
            player = new AudioPlayer(audio);
            player.start();
        }
        catch (PropertyException e) {
            showMessageGUI("No se puede leer Default_audio_config_xml", "error");
        }
    }
    private static void saveAudioFile(){
        
        getFilename("Save As...", JFileChooser.SAVE_DIALOG);
                if (user_data_filename == null || user_data_filename.isEmpty()) {
                    return;
                }
                try {
                    String ext = "";
     
                    int i = user_data_filename.lastIndexOf('.');
                    System.out.println(user_data_filename);
                    if (i > 0 &&  i < user_data_filename.length() - 1) {
                        ext = user_data_filename.substring(i+1).toLowerCase();
                    }

                    if(ext.equals("wav")){
                        user_data_filename = file.getAbsolutePath(); 
                    }
                    else
                        user_data_filename = file.getAbsolutePath() + ".wav";
                    Utils.writeWavFile(audio,user_data_filename,
                            audioPanel.getSelectionStart(),
                            audioPanel.getSelectionEnd());
                    FileOutputStream output_stream = new FileOutputStream(file_text_path + ".txt");
                    OutputStreamWriter stream_writer = new OutputStreamWriter(output_stream,"utf-8");
                    BufferedWriter out = new BufferedWriter(stream_writer);
                    
                    out.write(adapt_text_area.getText());
                    out.close();
                    save_as_menu_item.setEnabled(false);
                    adapt_text_area.setText(generate.generateSentences());

                } catch (IOException e) {
                    showMessageGUI("Archivo no encontrado.", "error");
                }
    
    }

    private static void open_corpus_file(){
        
        getFilename("Select corpus...", JFileChooser.OPEN_DIALOG);
        if (user_data_filename == null || user_data_filename.isEmpty()) {
            return;
        }

        String ext = "";

        int i = user_data_filename.lastIndexOf('.');
        System.out.println(user_data_filename);
        if (i > 0 &&  i < user_data_filename.length() - 1) {
            ext = user_data_filename.substring(i+1).toLowerCase();
        }

        if(!ext.equals("txt")){
            lm_text_area.append("File "+ file_text_path + " not valid.\n");
            lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
            System.out.println("extension not valid"); 
        }
        else{
            lm_text_area.append("File "+ file_text_path + " loaded.\n");
            lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
            execute_lm_btn.setEnabled(true);
        }     
    
    }
    /* Gets the audio that's in the recorder.  This should only be called after recorder.stopRecording is called. */
    static private short[] getRecordedAudio(edu.cmu.sphinx.frontend.util.Microphone recorder) {
        
        short[] shorts = new short[0];
        int sampleRate = 16000;

        /* [[[WDW - TODO: this is not the most efficient way
         * to do this, but it at least works for now.]]]
         */
        while (recorder.hasMoreData()) {
            try {
                Data data = recorder.getData();
                if (data instanceof DoubleData) {
                    sampleRate =
                            ((DoubleData) data).getSampleRate();
                    double[] values =
                            ((DoubleData) data).getValues();
                    short[] newShorts = Arrays.copyOf(shorts, shorts.length + values.length);
                    for (int i = 0; i < values.length; i++) {
                        newShorts[shorts.length + i] = (short)values[i];
                    }
                    shorts = newShorts;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sampleRate > 16000) {
            System.out.println("Downsampling from " +
                    sampleRate + " to 8000.");
            shorts = Downsampler.downsample(
                    shorts,
                    sampleRate / 1000,
                    16);
        }

        return shorts;
    }
    private static void loadRecognizerConfiguration(){
        
        NumberFormat formatter, formatter_dec;
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        
        formatter = new DecimalFormat("0.#E0",formatSymbols);
        formatter_dec = new DecimalFormat("0.#",formatSymbols);
       
        beam_value_lbl.setText(formatter.format(recognizerConfig.getRelBeamWidth()));
        
        //WIP
        wip_slider.setValue((int) (recognizerConfig.getWip()*10.0));
        wip_value_lbl.setText(formatter_dec.format(recognizerConfig.getWip()));
        
        //Language Weight
        lw_slider.setValue((int) (recognizerConfig.getLw()));
        lw_value_lbl.setText(formatter_dec.format(recognizerConfig.getLw()));
        
        //Phonetic beam
        pbeam_value_lbl.setText(formatter.format(recognizerConfig.getPbeam()));
        
        update_init_speakers();
        update_init_lm();
    }
    
    public static void enable_reload_model(){
        
        play_pause_btn.setSelected(false);
        play_pause_btn.setEnabled(false);
        reload_model_btn.setEnabled(true);
        beam_slider.setEnabled(true);
        wip_slider.setEnabled(true);
        lw_slider.setEnabled(true);
        pbeam_slider.setEnabled(true);
    
    }
    
    public static void print_mllr_process(String log){
        
        mllr_log_txt_area.append(log+"\n");
        mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
    }
    
    private void disableView(int view){
        
        switch(view){
            case 0: //report
                create_report_menu_item.setEnabled(false);
                speaker_adapt_menu_item.setEnabled(true);
                train_mllr_menu_item.setEnabled(true);
                lang_model_menu_item.setEnabled(true);
                jProgressBar2.setVisible(false);
                break;
            case 1: // user adaptation
                create_report_menu_item.setEnabled(true);
                speaker_adapt_menu_item.setEnabled(false);
                train_mllr_menu_item.setEnabled(true);
                lang_model_menu_item.setEnabled(true);
                jProgressBar2.setVisible(false);
                break;
            case 2: // MLLR
                create_report_menu_item.setEnabled(true);
                speaker_adapt_menu_item.setEnabled(true);
                train_mllr_menu_item.setEnabled(false);
                lang_model_menu_item.setEnabled(true);
                jProgressBar2.setVisible(false);
                break;
            case 3: // language model
                create_report_menu_item.setEnabled(true);
                speaker_adapt_menu_item.setEnabled(true);
                train_mllr_menu_item.setEnabled(true);
                lang_model_menu_item.setEnabled(false);
                jProgressBar2.setVisible(false);
                break;
        }
    }
    
    private static void update_init_speakers(){
        
        String[] speakers = Directories.getAllSpeakers();
        //Report combo box
        init_speaker_combo_box.removeAllItems();
        init_speaker_combo_box.addItem("Default");
        init_speaker_combo_box.setSelectedIndex(0);
        
        //MLLR combo box
        speakers_combo_box.removeAllItems();
        speakers_combo_box.addItem("(None)");
        speakers_combo_box.setSelectedIndex(0);
        
        if(speakers.length != 0){
            for(String s : speakers){
                init_speaker_combo_box.addItem(s);
                speakers_combo_box.addItem(s);
            }
        }
        
    }
    
    private static void update_init_lm(){
        
        String[] lms = Directories.getAllLm();
        lm_init_combo_box.removeAllItems();
        lm_init_combo_box.addItem("Default");
        lm_init_combo_box.setSelectedIndex(0);
        if(lms.length != 0){
            for(String s : lms){
                lm_init_combo_box.addItem(s);
            }
        }
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        status_jpanel = new javax.swing.JPanel();
        status_bar = new javax.swing.JLabel();
        jProgressBar2 = new javax.swing.JProgressBar();
        card_layout_panel = new javax.swing.JPanel();
        principal_card_panel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        report_txt = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        relativeBeamWidth_lbl = new javax.swing.JLabel();
        beam_slider = new javax.swing.JSlider();
        beam_value_lbl = new javax.swing.JLabel();
        wip_lbl = new javax.swing.JLabel();
        wip_slider = new javax.swing.JSlider();
        wip_value_lbl = new javax.swing.JLabel();
        lw_lbl = new javax.swing.JLabel();
        lw_slider = new javax.swing.JSlider();
        lw_value_lbl = new javax.swing.JLabel();
        phoneticBeam_lbl = new javax.swing.JLabel();
        pbeam_slider = new javax.swing.JSlider();
        pbeam_value_lbl = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        play_pause_btn = new javax.swing.JToggleButton();
        reload_model_btn = new javax.swing.JButton();
        clear_btn = new javax.swing.JButton();
        copy_btn = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        init_speaker_combo_box = new javax.swing.JComboBox<>();
        lm_init_combo_box = new javax.swing.JComboBox<>();
        adaptation_card_panel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        adapt_text_area = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        record_btn = new javax.swing.JToggleButton();
        playback_btn = new javax.swing.JButton();
        save_btn = new javax.swing.JButton();
        sent_gen_btn = new javax.swing.JButton();
        timer_label = new javax.swing.JLabel();
        audio_player_scroll = new javax.swing.JScrollPane();
        mllr_card_panel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        mllr_log_txt_area = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        speakers_combo_box = new javax.swing.JComboBox<>();
        create_mllr_btn = new javax.swing.JButton();
        lm_panel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lm_text_area = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        select_corpus_btn = new javax.swing.JButton();
        execute_lm_btn = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();
        new_speaker_menu_item = new javax.swing.JMenuItem();
        del_speaker_menu_item = new javax.swing.JMenuItem();
        save_as_menu_item = new javax.swing.JMenuItem();
        edit_ab_item = new javax.swing.JMenuItem();
        edit_menu = new javax.swing.JMenu();
        selectAll_menu_item = new javax.swing.JMenuItem();
        crop_menu_item = new javax.swing.JMenuItem();
        view_menu = new javax.swing.JMenu();
        create_report_menu_item = new javax.swing.JMenuItem();
        speaker_adapt_menu_item = new javax.swing.JMenuItem();
        train_mllr_menu_item = new javax.swing.JMenuItem();
        lang_model_menu_item = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        command_item = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Reconocimiento automático del habla para reportes médicos");
        setPreferredSize(new java.awt.Dimension(300, 300));

        status_jpanel.setBackground(new java.awt.Color(204, 204, 204));
        status_jpanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        status_jpanel.setLayout(new java.awt.BorderLayout());

        status_bar.setBackground(new java.awt.Color(204, 204, 204));
        status_bar.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        status_bar.setText("Status bar");
        status_jpanel.add(status_bar, java.awt.BorderLayout.CENTER);

        jProgressBar2.setForeground(new java.awt.Color(92, 184, 92));
        jProgressBar2.setValue(50);
        jProgressBar2.setFocusable(false);
        jProgressBar2.setName(""); // NOI18N
        jProgressBar2.setStringPainted(true);
        jProgressBar2.setVerifyInputWhenFocusTarget(false);
        status_jpanel.add(jProgressBar2, java.awt.BorderLayout.LINE_END);

        getContentPane().add(status_jpanel, java.awt.BorderLayout.PAGE_END);

        card_layout_panel.setBackground(new java.awt.Color(255, 255, 255));
        card_layout_panel.setLayout(new java.awt.CardLayout());

        principal_card_panel.setLayout(new java.awt.GridBagLayout());

        report_txt.setColumns(20);
        report_txt.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        report_txt.setLineWrap(true);
        report_txt.setRows(5);
        report_txt.setWrapStyleWord(true);
        report_txt.setBorder(null);
        report_txt.setEnabled(false);
        jScrollPane1.setViewportView(report_txt);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
        principal_card_panel.add(jScrollPane1, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Configuración", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 14))); // NOI18N
        jPanel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanel4.setLayout(new java.awt.GridLayout(4, 4, 20, 5));

        relativeBeamWidth_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        relativeBeamWidth_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        relativeBeamWidth_lbl.setText("Relative Beam Width");
        jPanel4.add(relativeBeamWidth_lbl);

        beam_slider.setMaximum(50);
        beam_slider.setMinimum(-50);
        beam_slider.setMinorTickSpacing(5);
        beam_slider.setPaintTicks(true);
        beam_slider.setToolTipText("");
        beam_slider.setValue(0);
        beam_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                beam_sliderStateChanged(evt);
            }
        });
        jPanel4.add(beam_slider);

        beam_value_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        beam_value_lbl.setText("50");
        jPanel4.add(beam_value_lbl);

        wip_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        wip_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        wip_lbl.setText("Word Insertion Probability");
        jPanel4.add(wip_lbl);

        wip_slider.setMaximum(10);
        wip_slider.setMinorTickSpacing(1);
        wip_slider.setPaintTicks(true);
        wip_slider.setToolTipText("");
        wip_slider.setValue(5);
        wip_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                wip_sliderStateChanged(evt);
            }
        });
        jPanel4.add(wip_slider);

        wip_value_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        wip_value_lbl.setText("50");
        jPanel4.add(wip_value_lbl);

        lw_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lw_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lw_lbl.setText("Language Weight");
        jPanel4.add(lw_lbl);

        lw_slider.setMaximum(20);
        lw_slider.setMinimum(1);
        lw_slider.setMinorTickSpacing(1);
        lw_slider.setPaintLabels(true);
        lw_slider.setPaintTicks(true);
        lw_slider.setValue(10);
        lw_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lw_sliderStateChanged(evt);
            }
        });
        jPanel4.add(lw_slider);

        lw_value_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lw_value_lbl.setText("50");
        jPanel4.add(lw_value_lbl);

        phoneticBeam_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        phoneticBeam_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        phoneticBeam_lbl.setText("Phonetic Beam");
        jPanel4.add(phoneticBeam_lbl);

        pbeam_slider.setMaximum(50);
        pbeam_slider.setMinimum(-50);
        pbeam_slider.setMinorTickSpacing(5);
        pbeam_slider.setPaintTicks(true);
        pbeam_slider.setValue(0);
        pbeam_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pbeam_sliderStateChanged(evt);
            }
        });
        jPanel4.add(pbeam_slider);

        pbeam_value_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        pbeam_value_lbl.setText("50");
        jPanel4.add(pbeam_value_lbl);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 15, 13);
        principal_card_panel.add(jPanel4, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        play_pause_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        play_pause_btn.setText("Play");
        play_pause_btn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                play_pause_btnItemStateChanged(evt);
            }
        });
        jPanel1.add(play_pause_btn);

        reload_model_btn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        reload_model_btn.setText("Cargar modelo");
        reload_model_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reload_model_btnActionPerformed(evt);
            }
        });
        jPanel1.add(reload_model_btn);

        clear_btn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        clear_btn.setText("Borrar texto");
        clear_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_btnActionPerformed(evt);
            }
        });
        jPanel1.add(clear_btn);

        copy_btn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        copy_btn.setText("Copiar reporte");
        copy_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copy_btnActionPerformed(evt);
            }
        });
        jPanel1.add(copy_btn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 15);
        principal_card_panel.add(jPanel1, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridLayout(2, 2, 10, 0));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Usuario");
        jPanel6.add(jLabel1);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Modelo de lenguaje");
        jPanel6.add(jLabel2);

        init_speaker_combo_box.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        init_speaker_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default" }));
        init_speaker_combo_box.setToolTipText("");
        init_speaker_combo_box.setPreferredSize(new java.awt.Dimension(200, 30));
        init_speaker_combo_box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                init_speaker_combo_boxActionPerformed(evt);
            }
        });
        jPanel6.add(init_speaker_combo_box);

        lm_init_combo_box.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lm_init_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default" }));
        jPanel6.add(lm_init_combo_box);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(15, 15, 0, 15);
        principal_card_panel.add(jPanel6, gridBagConstraints);

        card_layout_panel.add(principal_card_panel, "principal_card");

        adaptation_card_panel.setLayout(new java.awt.GridBagLayout());

        adapt_text_area.setColumns(20);
        adapt_text_area.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        adapt_text_area.setLineWrap(true);
        adapt_text_area.setRows(5);
        adapt_text_area.setText("visualizamos aumento en la captación del radiotrazador en región fronto temporal derecha soma de la vértebra dorsal soma de la vértebra lumbar y foco lineal en región intertrocantérea del fémur derecho sugestivos de afectación metastásica ósea ");
        adapt_text_area.setWrapStyleWord(true);
        jScrollPane2.setViewportView(adapt_text_area);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(23, 23, 23, 23);
        adaptation_card_panel.add(jScrollPane2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(1, 4, 10, 5));

        record_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        record_btn.setText("Grabar");
        record_btn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                record_btnItemStateChanged(evt);
            }
        });
        jPanel2.add(record_btn);

        playback_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        playback_btn.setText("Play");
        playback_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playback_btnActionPerformed(evt);
            }
        });
        jPanel2.add(playback_btn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.ipady = 17;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        adaptation_card_panel.add(jPanel2, gridBagConstraints);

        save_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        save_btn.setText("Guardar");
        save_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_btnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.ipady = 17;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        adaptation_card_panel.add(save_btn, gridBagConstraints);

        sent_gen_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        sent_gen_btn.setText("Generar");
        sent_gen_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sent_gen_btnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.ipady = 17;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        adaptation_card_panel.add(sent_gen_btn, gridBagConstraints);

        timer_label.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        timer_label.setText("00:00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        adaptation_card_panel.add(timer_label, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 23, 0, 23);
        adaptation_card_panel.add(audio_player_scroll, gridBagConstraints);

        card_layout_panel.add(adaptation_card_panel, "adaptation_card");

        mllr_card_panel.setLayout(new java.awt.GridBagLayout());

        mllr_log_txt_area.setEditable(false);
        mllr_log_txt_area.setColumns(20);
        mllr_log_txt_area.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        mllr_log_txt_area.setLineWrap(true);
        mllr_log_txt_area.setRows(5);
        mllr_log_txt_area.setText("*****************Instrucciones de uso********************\n\nPaso 1: Seleccionar usuario.\nPaso 2: Pulsar botón \"Crear modelo\" para iniciar proceso.\n\n********************** Logs *****************************\n");
        jScrollPane3.setViewportView(mllr_log_txt_area);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(23, 23, 23, 23);
        mllr_card_panel.add(jScrollPane3, gridBagConstraints);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));

        speakers_combo_box.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        speakers_combo_box.setMinimumSize(new java.awt.Dimension(71, 10));
        speakers_combo_box.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel3.add(speakers_combo_box);

        create_mllr_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        create_mllr_btn.setText("Crear modelo");
        create_mllr_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_mllr_btnActionPerformed(evt);
            }
        });
        jPanel3.add(create_mllr_btn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        mllr_card_panel.add(jPanel3, gridBagConstraints);

        card_layout_panel.add(mllr_card_panel, "mllr_card");

        lm_panel.setLayout(new java.awt.GridBagLayout());

        lm_text_area.setEditable(false);
        lm_text_area.setColumns(20);
        lm_text_area.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        lm_text_area.setRows(5);
        lm_text_area.setText("*****************Instrucciones de uso********************\n\nPaso 1: Seleccionar corpus de texto.\nPaso 2: Pulsar botón \"Ejecutar\" para iniciar modelo de lenguaje.\nPaso 3: Introducir un nombre para el modelo de lenguaje.\n\nConfiguración por defecto:\nSmoothing: \t\tModified Kneser-Ney\nN-grams:\t\t3\nFormato del modelo:\tARPA\n*********************************************************\n********************** Logs *****************************\n\n");
        jScrollPane4.setViewportView(lm_text_area);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(23, 23, 23, 23);
        lm_panel.add(jScrollPane4, gridBagConstraints);

        select_corpus_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        select_corpus_btn.setText("Seleccionar corpus");
        select_corpus_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_corpus_btnActionPerformed(evt);
            }
        });
        jPanel5.add(select_corpus_btn);

        execute_lm_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        execute_lm_btn.setText("Ejecutar");
        execute_lm_btn.setEnabled(false);
        execute_lm_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                execute_lm_btnActionPerformed(evt);
            }
        });
        jPanel5.add(execute_lm_btn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        lm_panel.add(jPanel5, gridBagConstraints);

        card_layout_panel.add(lm_panel, "lm_card");

        getContentPane().add(card_layout_panel, java.awt.BorderLayout.CENTER);

        file_menu.setText("Archivo");
        file_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        new_speaker_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        new_speaker_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        new_speaker_menu_item.setText("Nuevo usuario");
        new_speaker_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new_speaker_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(new_speaker_menu_item);

        del_speaker_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        del_speaker_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        del_speaker_menu_item.setText("Eliminar usuario");
        del_speaker_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del_speaker_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(del_speaker_menu_item);

        save_as_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        save_as_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        save_as_menu_item.setText("Guardar como...");
        save_as_menu_item.setEnabled(false);
        save_as_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_as_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(save_as_menu_item);

        edit_ab_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        edit_ab_item.setText("Editar abreviaturas");
        edit_ab_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_ab_itemActionPerformed(evt);
            }
        });
        file_menu.add(edit_ab_item);

        jMenuBar1.add(file_menu);

        edit_menu.setText("Editar");
        edit_menu.setEnabled(false);
        edit_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        selectAll_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAll_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        selectAll_menu_item.setText("Seleccionar todo");
        selectAll_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAll_menu_itemActionPerformed(evt);
            }
        });
        edit_menu.add(selectAll_menu_item);

        crop_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        crop_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        crop_menu_item.setText("Cortar");
        crop_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crop_menu_itemActionPerformed(evt);
            }
        });
        edit_menu.add(crop_menu_item);

        jMenuBar1.add(edit_menu);

        view_menu.setText("Vistas");
        view_menu.setToolTipText("");
        view_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        create_report_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        create_report_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        create_report_menu_item.setText("Crear reporte");
        create_report_menu_item.setEnabled(false);
        create_report_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_report_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(create_report_menu_item);

        speaker_adapt_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        speaker_adapt_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        speaker_adapt_menu_item.setText("Adaptación al usuario");
        speaker_adapt_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speaker_adapt_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(speaker_adapt_menu_item);

        train_mllr_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        train_mllr_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        train_mllr_menu_item.setText("Entrenar MLLR");
        train_mllr_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                train_mllr_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(train_mllr_menu_item);

        lang_model_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        lang_model_menu_item.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lang_model_menu_item.setText("Modelar lenguaje");
        lang_model_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lang_model_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(lang_model_menu_item);

        jMenuBar1.add(view_menu);

        jMenu1.setText("Ayuda");
        jMenu1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        command_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        command_item.setText("Listado de comandos");
        command_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                command_itemActionPerformed(evt);
            }
        });
        jMenu1.add(command_item);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void beam_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_beam_sliderStateChanged
        
        NumberFormat formatter;
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        
        formatter = new DecimalFormat("0.#E0",formatSymbols);
        double rbw = recognizerConfig.getRelBeamWidth();
        double exp = beam_slider.getValue();
        rbw = rbw/Math.pow(10, exp);

        beam_value_lbl.setText(formatter.format(rbw) );
    }//GEN-LAST:event_beam_sliderStateChanged

    private void wip_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_wip_sliderStateChanged
        
        NumberFormat formatter;
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        
        formatter = new DecimalFormat("0.#",formatSymbols);
        wip_value_lbl.setText(formatter.format(wip_slider.getValue()/10.0));
    }//GEN-LAST:event_wip_sliderStateChanged

    private void lw_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lw_sliderStateChanged
        
        NumberFormat formatter;
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        
        formatter = new DecimalFormat("0.#",formatSymbols);
        lw_value_lbl.setText(formatter.format(lw_slider.getValue()));
    }//GEN-LAST:event_lw_sliderStateChanged

    private void pbeam_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pbeam_sliderStateChanged
        
        NumberFormat formatter;
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        
        formatter = new DecimalFormat("0.#E0",formatSymbols);
        
        double rbw = recognizerConfig.getPbeam();
        double exp = pbeam_slider.getValue();

        rbw = rbw/Math.pow(10, exp);

        pbeam_value_lbl.setText(formatter.format(rbw) );
    }//GEN-LAST:event_pbeam_sliderStateChanged

    private void speaker_adapt_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speaker_adapt_menu_itemActionPerformed
       
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "adaptation_card");
        recognize.closeRecognition();
        disableView(1);
        edit_menu.setEnabled(true);
        adapt_text_area.setText(generate.generateSentences());
        LoggerStatus.Log("Modo adaptación del usuario", LoggerStatus.LogType.INFO);
    }//GEN-LAST:event_speaker_adapt_menu_itemActionPerformed

    private void play_pause_btnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_play_pause_btnItemStateChanged
        
        if(evt.getStateChange()==ItemEvent.SELECTED){
            recognize.startRecognition();
            play_pause_btn.setText("Pause");
            report_txt.setEnabled(true);
            
        } 
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            recognize.stopRecognition();
            play_pause_btn.setText("Play");
            report_txt.setEnabled(false);
        }
    }//GEN-LAST:event_play_pause_btnItemStateChanged

    private void reload_model_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reload_model_btnActionPerformed
        
        Map<String, String> global_prop = new HashMap<>();
        global_prop.put(relBeamWidthProp, beam_value_lbl.getText());
        global_prop.put(wipProp, wip_value_lbl.getText());
        global_prop.put(lwProp, lw_value_lbl.getText());
        global_prop.put(pBeamProp, pbeam_value_lbl.getText());
        
        String lm_item = (String) lm_init_combo_box.getSelectedItem();
        String adapt_item = (String) init_speaker_combo_box.getSelectedItem();
        global_prop.put(lmProp, lm_item);
        global_prop.put(acousticProp, adapt_item);
        recognize.loadConfig(global_prop);
        
        play_pause_btn.setEnabled(true);
        init_speaker_combo_box.setEnabled(true);

    }//GEN-LAST:event_reload_model_btnActionPerformed

    private void create_report_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create_report_menu_itemActionPerformed
        
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "principal_card");
       
        edit_menu.setEnabled(false);
        disableView(0);
        update_init_speakers();
        update_init_lm();
        recognize.initRecognition();
        report_txt.setEnabled(false);
        play_pause_btn.setSelected(false);
        
        LoggerStatus.Log("Modo reconocimiento.", LoggerStatus.LogType.INFO);
    }//GEN-LAST:event_create_report_menu_itemActionPerformed

    private void sent_gen_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sent_gen_btnActionPerformed
        
        adapt_text_area.setText(generate.generateSentences());
    }//GEN-LAST:event_sent_gen_btnActionPerformed

    private void record_btnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_record_btnItemStateChanged
        
        if(evt.getStateChange()==ItemEvent.SELECTED){
            timer_label.setText(String.format("%02d:%02d",minutes,seconds));
            record_btn.setText("Stop");
            sent_gen_btn.setEnabled(false);
            playback_btn.setEnabled(false);
            save_btn.setEnabled(false);
            timer = new Timer(1000,new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    seconds++;
                    if(seconds == 60){
                        minutes++;
                        seconds = 0;
                    }
                    timer_label.setText(String.format("%02d:%02d",minutes,seconds));
                }
            });
            timer.start();
            recorder.startRecording();
            LoggerStatus.Log("Grabando audio...", LoggerStatus.LogType.INFO);
        } 
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            recorder.stopRecording();
            audio.setAudioData(getRecordedAudio(recorder));
            record_btn.setText("Record");
            seconds = 0;
            minutes = 0;
            timer.stop();
            save_as_menu_item.setEnabled(true);
            sent_gen_btn.setEnabled(true);
            playback_btn.setEnabled(true);
            save_btn.setEnabled(true);
            LoggerStatus.Log("Grabación de audio detenido.", LoggerStatus.LogType.INFO);
        }
    }//GEN-LAST:event_record_btnItemStateChanged

    private void playback_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playback_btnActionPerformed
        
        LoggerStatus.Log("Reproduciendo audio...", LoggerStatus.LogType.INFO);
        player.play(audioPanel.getSelectionStart(),
                       audioPanel.getSelectionEnd());

    }//GEN-LAST:event_playback_btnActionPerformed

    private void save_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_btnActionPerformed
        
        saveAudioFile();
    }//GEN-LAST:event_save_btnActionPerformed

    private void save_as_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_as_menu_itemActionPerformed
        
        saveAudioFile();
    }//GEN-LAST:event_save_as_menu_itemActionPerformed

    private void crop_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crop_menu_itemActionPerformed
        
        audioPanel.crop();
        LoggerStatus.Log("Audio cortado.", LoggerStatus.LogType.INFO);
    }//GEN-LAST:event_crop_menu_itemActionPerformed

    private void selectAll_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAll_menu_itemActionPerformed
        
        audioPanel.selectAll();
    }//GEN-LAST:event_selectAll_menu_itemActionPerformed

    private void clear_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_btnActionPerformed
        
        report_txt.setText("");
    }//GEN-LAST:event_clear_btnActionPerformed

    private void train_mllr_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_train_mllr_menu_itemActionPerformed
        
        recognize.closeRecognition();
        speakers_combo_box.removeAllItems();
        speakers_combo_box.addItem("(None)");
        speakers_combo_box.setSelectedIndex(0);
        
        disableView(2);
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "mllr_card");
        for(String s:Directories.getAllSpeakers())
            speakers_combo_box.addItem(s);
        LoggerStatus.Log("Training MLLR mode.", LoggerStatus.LogType.INFO);
    }//GEN-LAST:event_train_mllr_menu_itemActionPerformed

    private void del_speaker_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del_speaker_menu_itemActionPerformed
       
        String[] speakers = Directories.getAllSpeakers();
        if(speakers.length != 0){
            String name = (String) JOptionPane.showInputDialog(this, "Selecciona usuario a eliminar.",
                                                        "Eliminar usuario",
                                                        JOptionPane.QUESTION_MESSAGE, 
                                                        null, 
                                                        speakers,
                                                        speakers[0]);
            if (name!= null){
                Directories.deleteSpeakerDir(name);
                update_init_speakers();
                showMessageGUI("Usuario "+name+" eliminado correctamente.", "info");
            }
        }
        else{
            showMessageGUI("No hay usuarios para eliminar.", "info");
            
        }
        
    }//GEN-LAST:event_del_speaker_menu_itemActionPerformed

    private void create_mllr_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create_mllr_btnActionPerformed
        
        String name = (String) speakers_combo_box.getSelectedItem();
        
        if(!Directories.isEmptyDir(name) && name != null ){

            mllr_log_txt_area.setText("");
            
            Sphinx_fe acoustic_feature = new Sphinx_fe(name);
            Bw acum_count = new Bw(name);
            Map_adapt map_adapt = new Map_adapt(name);
            jProgressBar2.setVisible(true);
            
            SwingWorker sw1 = new SwingWorker<Boolean, Integer>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    publish(0);
                    
                    create_fileid_file(name);
                    Thread.sleep(100);
                    
                    publish(1);
                    
                    create_transcription_file(name);
                    Thread.sleep(100);                 
                    
                    publish(3);
                    
                    acoustic_feature.exec_sphinx_fe();
                    
                    publish(4);
                    
                    acum_count.exec_bw();
                    
                    publish(5);
                    
                    map_adapt.exec_map_adapt();
                    
                    File source_noisedict = new File(rm.getDefault_acoustic_model_dir_path()+"\\noisedict");
                    File dest_noisedict = new File(rm.getWav_dir_path()+"\\"+name+"\\noisedict");
                    
                    File source_featparams = new File(rm.getDefault_acoustic_model_dir_path()+"\\feat.params");
                    File dest_featparams = new File(rm.getWav_dir_path()+"\\"+name+"\\feat.params");
                    
                    File source_mdef = new File(rm.getDefault_acoustic_model_dir_path()+"\\mdef");
                    File dest_mdef = new File(rm.getWav_dir_path()+"\\"+name+"\\mdef");
                    
                    Files.copy(source_noisedict.toPath(),dest_noisedict.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(source_featparams.toPath(),dest_featparams.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(source_mdef.toPath(),dest_mdef.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
                
                @Override
                protected void process(List<Integer> chunks) {
                    int value = chunks.get(0);
                    switch(value){
                        case 0:
                            mllr_log_txt_area.append("\n\n********CREATING ID FILES************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                        case 1:
                            mllr_log_txt_area.append("\n\n********CREATING TRANSCRIPTION FILE************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                        case 2:
                            mllr_log_txt_area.append("\n\n********CREATING VOCABULARY FILE************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                        case 3:
                            mllr_log_txt_area.append("\n\n********GENERATING ACOUSTIC FEATURES************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                        case 4:
                            mllr_log_txt_area.append("\n\n********ACUMULATING STATISTIC COUNTS************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                        case 5:
                            //mllr_log_txt_area.append("\n\n********GENERATING MLLR MATRIX************\n");
                            mllr_log_txt_area.append("\n\n********GENERATING MAP FILES************\n");
                            mllr_log_txt_area.setCaretPosition(mllr_log_txt_area.getDocument().getLength());
                            break;
                    }
                    jProgressBar2.setValue(value*100/5);
                }
                @Override
                protected void done() {
                    
                    mllr_log_txt_area.append("\n\n********FINISHED************\n");
                    showMessageGUI("Entrenamiento MAP terminado.", "info");
                    
                } 
            };
            
            sw1.execute();
        }
        else{
            showMessageGUI("No se pudo completar el entrenamiento MLLR.", "error");
        }
               
        
    }//GEN-LAST:event_create_mllr_btnActionPerformed

    private void init_speaker_combo_boxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_init_speaker_combo_boxActionPerformed
       
        String item = (String) init_speaker_combo_box.getSelectedItem();
        int id_item = init_speaker_combo_box.getSelectedIndex();
        
    }//GEN-LAST:event_init_speaker_combo_boxActionPerformed

    private void lang_model_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lang_model_menu_itemActionPerformed
        
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "lm_card");
        recognize.closeRecognition();
        LoggerStatus.Log("Language modeling mode.", LoggerStatus.LogType.INFO);
        disableView(3);
    }//GEN-LAST:event_lang_model_menu_itemActionPerformed

    private void select_corpus_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_corpus_btnActionPerformed
       
        open_corpus_file();
    }//GEN-LAST:event_select_corpus_btnActionPerformed
    
    private void execute_lm_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execute_lm_btnActionPerformed
        
        jProgressBar2.setVisible(true);
            String name = JOptionPane.showInputDialog(this, "Introduce el nombre del modelo de lenguaje");
            if(!name.isBlank()){
                Instant start = Instant.now();
                SwingWorker sw1 = new SwingWorker<String, Integer>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        try{
                            publish(0);
                            LanguageModelBuilder lm = new LanguageModelBuilder(name, file_text_path);
                            Thread.sleep(100);

                            publish(1);
                            List<String> sentences = lm.cleanCorpus();

                            publish(2);
                            lm.buildVocab(sentences);

                            publish(3);
                            lm.buildLm();
                        }catch(Exception ex){
                            
                            return ex.getMessage();
                        }
                        
                        return null;
                    }
                    @Override
                    protected void process(List<Integer> chunks) {
                        int value = chunks.get(0);
                        switch(value){
                            case 0:
                                lm_text_area.append("Cargando datos...\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());  
                                break;
                            case 1:
                                lm_text_area.append("Procesando corpus. Este proceso puede tardar mucho tiempo en finalizar. Por favor espere.....\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                                break;
                            case 2:
                                lm_text_area.append("Construyendo vocabulario. Este proceso puede tardar mucho tiempo en finalizar. Por favor espere.....\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                                break;
                            case 3:
                                lm_text_area.append("Estimando modelo de lenguaje. Este proceso puede tardar mucho tiempo en finalizar. Por favor espere.....\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                                break;
                        }
                        
                        jProgressBar2.setValue(value*100/4);
                    }
                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            if(result == null){
                                showMessageGUI("El modelo de lenguaje ha sido creado exitosamente.", "info");
                                lm_text_area.append("Finalizado!\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                                jProgressBar2.setValue(100);
                            }
                            else{
                                lm_text_area.append("*********************************Exception error*******************************\n");
                                lm_text_area.append(result + "\n");
                                lm_text_area.append("*******************************************************************************\n");
                                lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                                showMessageGUI("Algo inesperado ha ocurrido.", "error");
                            }
                            Instant end = Instant.now();
                            Duration timeElapsed = Duration.between(start, end); 
                            lm_text_area.append("Tiempo transcurrido: "+timeElapsed.toMinutes()+" minutos \n");
                            lm_text_area.setCaretPosition(lm_text_area.getDocument().getLength());
                        } catch (InterruptedException | ExecutionException ex) {
                            showMessageGUI("Excepción de tipo: " +ex.getMessage() , "error");
                        }

                    }
                };
                sw1.execute();
                
            }   
        
    }//GEN-LAST:event_execute_lm_btnActionPerformed

    private void copy_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copy_btnActionPerformed
        
        String med_report = report_txt.getText();
        StringSelection stringSelection = new StringSelection(med_report);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        showMessageGUI("El reporte se ha copiado al portapapeles.","info");
    }//GEN-LAST:event_copy_btnActionPerformed

    private void new_speaker_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_new_speaker_menu_itemActionPerformed
  
        String name = JOptionPane.showInputDialog(this, "Introduce el nombre del usuario");
        if(name != null ){
            int confirm = Directories.createSpeakerDir(name);
            switch(confirm){
                case 0:
                update_init_speakers();
                showMessageGUI("Usuario "+name+" creado correctamente.", "info");
                break;
                case 1:
                showMessageGUI("No se ha podido crear el usuario "+name, "error");
                break;
                case 2:
                showMessageGUI("El usuario "+name+" ya existe.", "error");
                break;
            }
        }
    }//GEN-LAST:event_new_speaker_menu_itemActionPerformed

    private void command_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_command_itemActionPerformed
        
        if (Desktop.isDesktopSupported()) {
            try {
                File myFile = new File("etc\\docs\\help.pdf");
                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                showMessageGUI("no application registered for PDFs", "error");
            }
        }
    }//GEN-LAST:event_command_itemActionPerformed

    private void edit_ab_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_ab_itemActionPerformed
        
        EditAbrev editWindows = new EditAbrev(this, false);
        editWindows.setVisible(true);
    }//GEN-LAST:event_edit_ab_itemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AppGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AppGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AppGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AppGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AppGui app = new AppGui();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int height = screenSize.height * 8 / 10;
                int width = screenSize.width * 2 / 3;
                app.setMinimumSize(new Dimension(width, height));
                
                app.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private static javax.swing.JTextArea adapt_text_area;
    private javax.swing.JPanel adaptation_card_panel;
    private static javax.swing.JScrollPane audio_player_scroll;
    public static javax.swing.JSlider beam_slider;
    public static javax.swing.JLabel beam_value_lbl;
    private javax.swing.JPanel card_layout_panel;
    public static javax.swing.JButton clear_btn;
    private javax.swing.JMenuItem command_item;
    private javax.swing.JButton copy_btn;
    private javax.swing.JButton create_mllr_btn;
    private javax.swing.JMenuItem create_report_menu_item;
    private javax.swing.JMenuItem crop_menu_item;
    private javax.swing.JMenuItem del_speaker_menu_item;
    private javax.swing.JMenuItem edit_ab_item;
    private static javax.swing.JMenu edit_menu;
    private static javax.swing.JButton execute_lm_btn;
    private static javax.swing.JMenu file_menu;
    private static javax.swing.JComboBox<String> init_speaker_combo_box;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    public static javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JMenuItem lang_model_menu_item;
    public static javax.swing.JComboBox<String> lm_init_combo_box;
    private static javax.swing.JPanel lm_panel;
    public static javax.swing.JTextArea lm_text_area;
    private javax.swing.JLabel lw_lbl;
    public static javax.swing.JSlider lw_slider;
    public static javax.swing.JLabel lw_value_lbl;
    private javax.swing.JPanel mllr_card_panel;
    private static javax.swing.JTextArea mllr_log_txt_area;
    private javax.swing.JMenuItem new_speaker_menu_item;
    public static javax.swing.JSlider pbeam_slider;
    public static javax.swing.JLabel pbeam_value_lbl;
    private javax.swing.JLabel phoneticBeam_lbl;
    public static javax.swing.JToggleButton play_pause_btn;
    private javax.swing.JButton playback_btn;
    private javax.swing.JPanel principal_card_panel;
    private javax.swing.JToggleButton record_btn;
    private javax.swing.JLabel relativeBeamWidth_lbl;
    public static javax.swing.JButton reload_model_btn;
    public static javax.swing.JTextArea report_txt;
    private static javax.swing.JMenuItem save_as_menu_item;
    private static javax.swing.JButton save_btn;
    private javax.swing.JMenuItem selectAll_menu_item;
    private javax.swing.JButton select_corpus_btn;
    private javax.swing.JButton sent_gen_btn;
    private javax.swing.JMenuItem speaker_adapt_menu_item;
    private static javax.swing.JComboBox<String> speakers_combo_box;
    public static javax.swing.JLabel status_bar;
    private javax.swing.JPanel status_jpanel;
    private javax.swing.JLabel timer_label;
    private javax.swing.JMenuItem train_mllr_menu_item;
    private javax.swing.JMenu view_menu;
    private javax.swing.JLabel wip_lbl;
    public static javax.swing.JSlider wip_slider;
    public static javax.swing.JLabel wip_value_lbl;
    // End of variables declaration//GEN-END:variables
}
