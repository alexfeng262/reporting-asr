/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR_tfm;

import static adaptation_mllr.Generate_files.*;
import adaptation_mllr.Bw;
import adaptation_mllr.Mllr_solve;
import adaptation_mllr.Sphinx_fe;
import asr_utils.Directories;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
import edu.cmu.sphinx.tools.audio.AudioData;
import edu.cmu.sphinx.tools.audio.AudioPanel;
import edu.cmu.sphinx.tools.audio.AudioPlayer;
import edu.cmu.sphinx.tools.audio.AudioTool;
import edu.cmu.sphinx.tools.audio.CepstrumPanel;
import edu.cmu.sphinx.tools.audio.Downsampler;
import edu.cmu.sphinx.tools.audio.SpectrogramPanel;
import edu.cmu.sphinx.tools.audio.Utils;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertySheet;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import train_sentence_generation.Sentence_generation;

/**
 *
 * @author alexf
 */
public class app_gui extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    private App_recognizer recognize;
    public static RecognizerConfiguration recognizerConfig;
    
    private final String relBeamWidthProp = "relativeBeamWidth";
    private final String wipProp = "wordInsertionProbability";
    private final String lwProp = "languageWeight";
    private final String pBeam = "phoneticBeam";
    private static Sentence_generation generate ;
    private static Timer timer;
    private int seconds;
    private int minutes;

    //*********************************************************8
    static final String CONTEXT = "AudioTool";
    static final String PREFS_CONTEXT = "/edu/cmu/sphinx/tools/audio/"
            + CONTEXT;
    
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
    static String filename;
    static String file_text_path;
    static File file;
    static AudioPlayer player;
    static edu.cmu.sphinx.frontend.util.Microphone recorder;
    static boolean recording;
    static Preferences prefs;
    static float zoom = 1.0f;

    public app_gui() {
        initComponents();
        
        seconds = 0;
        minutes = 0;

        generate = new Sentence_generation();
        recognize = new App_recognizer();
        recognizerConfig = new RecognizerConfiguration();
        loadRecognizerConfiguration();
        load_audio_interface();
        String home = System.getProperty("user.dir");
        System.out.println(home);
        
    }
     static public void getAudioFromFile(String filename) throws IOException {
        /* Supports alignment data.  The format of the alignment file
         * is as follows:
         *
         * input filename                String
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
     /* Gets a filename. */
    static public void getFilename(String title, int type) {
        int returnVal;
        filename = "";
        fileChooser.setDialogTitle(title);
        if(file_text_path != null)
            fileChooser.setCurrentDirectory(new File(file_text_path));
        fileChooser.setDialogType(type);

        if (type == JFileChooser.OPEN_DIALOG) {
            returnVal = fileChooser.showOpenDialog(jframe);
        } else {
            //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            returnVal = fileChooser.showSaveDialog(jframe);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            file_text_path = file.getAbsolutePath(); 
            //filename = file.getAbsolutePath();
            String ext = "";
            String s = file.getName();
            int i = s.lastIndexOf('.');
            System.out.println(s);
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            System.out.println(ext);
            if(ext.equals("wav")){
                filename = file.getAbsolutePath(); 
            }
            else
                filename = file.getAbsolutePath() + ".wav";
            
            //System.out.println(filename);
            //prefs.put(FILENAME_PREFERENCE, filename);
        }
    }
    private static void load_audio_interface(){
        //FrontEnd frontEnd;
        //FrontEnd cepstrumFrontEnd;
        //StreamDataSource dataSource;
        //StreamDataSource cepstrumDataSource;

        prefs = Preferences.userRoot().node(PREFS_CONTEXT);
        filename = prefs.get(FILENAME_PREFERENCE, "untitled.raw");
        file = new File("C:\\Users\\alexf\\Desktop\\ASR\\training2\\audio1.wav");
        
        
        try {
            URL url;
           
            url = AudioTool.class.getClassLoader().getResource("config_xml/spectrogram.config.xml");
            ConfigurationManager cm = new ConfigurationManager(url);
            
            fileChooser = new JFileChooser("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt");
            
            
            recorder = (edu.cmu.sphinx.frontend.util.Microphone) cm.lookup(MICROPHONE);
            recorder.initialize();
            audio = new AudioData();

            //frontEnd = (FrontEnd) cm.lookup(FRONT_END);
            //dataSource = (StreamDataSource) cm.lookup(DATA_SOURCE);
            //cepstrumFrontEnd = (FrontEnd) cm.lookup(CESPTRUM_FRONT_END);
            //cepstrumDataSource = (StreamDataSource) cm.lookup(CEPSTRUM_DATA_SOURCE);


            PropertySheet ps = cm.getPropertySheet(WINDOWER);
            float windowShiftInMs = ps.getFloat(RaisedCosineWindower.PROP_WINDOW_SHIFT_MS);
            
            float windowShiftInSamples = windowShiftInMs
                    * audio.getAudioFormat().getSampleRate() / 1000.0f;
            audioPanel = new AudioPanel(audio,
                    1.0f / windowShiftInSamples,
                    0.004f);
            audio_player_scroll.setViewportView(audioPanel);
            //jPanel3.add(audioPanel);
            audioPanel.setAlignmentX(0.0f);
            player = new AudioPlayer(audio);
            player.start();
            getAudioFromFile("C:\\Users\\alexf\\Desktop\\ASR\\training2\\audio1.wav");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    
    }
    private static void save_file(){
        getFilename("Save As...", JFileChooser.SAVE_DIALOG);
                if (filename == null || filename.isEmpty()) {
                    return;
                }
                try {
                    Utils.writeWavFile(audio,filename,
                            audioPanel.getSelectionStart(),
                            audioPanel.getSelectionEnd());
                    FileOutputStream output_stream = new FileOutputStream(file_text_path + ".txt");
                    OutputStreamWriter stream_writer = new OutputStreamWriter(output_stream,"utf-8");
                    BufferedWriter out = new BufferedWriter(stream_writer);
                    
                    out.write(adapt_text_area.getText());
                    out.close();
                    save_as_menu_item.setEnabled(false);
                    adapt_text_area.setText(generate.generate_sentence());
                    //Logger_status.Log("Audio file saved.", Logger_status.LogType.INFO);
                } catch (IOException e) {
                    e.printStackTrace();
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
        formatter = new DecimalFormat("0.#E0");
        formatter_dec = new DecimalFormat("0.#");
       
        //beam_slider.setValue((int) (Math.log10(recognizerConfig.getRelBeamWidth())*-1)-50);
        beam_value_lbl.setText(formatter.format(recognizerConfig.getRelBeamWidth()));
        
        //WIP
        wip_slider.setValue((int) (recognizerConfig.getWip()*10.0));
        wip_value_lbl.setText(formatter_dec.format(recognizerConfig.getWip()));
        
        //Language Weight
        lw_slider.setValue((int) (recognizerConfig.getLw()));
        lw_value_lbl.setText(formatter_dec.format(recognizerConfig.getLw()));
        
        //Phonetic beam
        //pbeam_slider.setValue((int) (recognizerConfig.getLw()));
        pbeam_value_lbl.setText(formatter.format(recognizerConfig.getPbeam()));
        
        String[] speakers = Directories.getAllSpeakers();
        if(speakers.length != 0){
            for(String s : speakers){
                init_speaker_combo_box.addItem(s);
            }
        }
    }
    
    public static void enable_reload_model(){
        //recognize.Stop_recognition();
        play_pause_btn.setSelected(false);
        play_pause_btn.setEnabled(false);
        //stop_btn.setEnabled(false);
        reload_model_btn.setEnabled(true);
        beam_slider.setEnabled(true);
        wip_slider.setEnabled(true);
        lw_slider.setEnabled(true);
        pbeam_slider.setEnabled(true);
    
    }
    
    public static void print_mllr_process(String log){
        mllr_log_txt_area.append(log+"\n");
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
        init_speaker_combo_box = new javax.swing.JComboBox<>();
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
        jMenuBar1 = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();
        new_speaker_menu_item = new javax.swing.JMenuItem();
        del_speaker_menu_item = new javax.swing.JMenuItem();
        save_as_menu_item = new javax.swing.JMenuItem();
        edit_menu = new javax.swing.JMenu();
        selectAll_menu_item = new javax.swing.JMenuItem();
        crop_menu_item = new javax.swing.JMenuItem();
        view_menu = new javax.swing.JMenu();
        create_report_menu_item = new javax.swing.JMenuItem();
        speaker_adapt_menu_item = new javax.swing.JMenuItem();
        train_mllr_menu_item = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1000, 800));

        status_jpanel.setBackground(new java.awt.Color(204, 204, 204));
        status_jpanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        status_jpanel.setLayout(new java.awt.BorderLayout());

        status_bar.setBackground(new java.awt.Color(204, 204, 204));
        status_bar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        status_bar.setText("Status bar");
        status_jpanel.add(status_bar, java.awt.BorderLayout.CENTER);

        getContentPane().add(status_jpanel, java.awt.BorderLayout.PAGE_END);

        card_layout_panel.setLayout(new java.awt.CardLayout());

        principal_card_panel.setLayout(new java.awt.GridBagLayout());

        report_txt.setColumns(20);
        report_txt.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
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

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Configuration"));
        jPanel4.setLayout(new java.awt.GridLayout(4, 4, 20, 5));

        relativeBeamWidth_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        relativeBeamWidth_lbl.setText("Relative Beam Width");
        jPanel4.add(relativeBeamWidth_lbl);

        beam_slider.setMaximum(50);
        beam_slider.setMinimum(-50);
        beam_slider.setMinorTickSpacing(5);
        beam_slider.setPaintTicks(true);
        beam_slider.setToolTipText("");
        beam_slider.setValue(0);
        beam_slider.setEnabled(false);
        beam_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                beam_sliderStateChanged(evt);
            }
        });
        jPanel4.add(beam_slider);

        beam_value_lbl.setText("50");
        jPanel4.add(beam_value_lbl);

        wip_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        wip_lbl.setText("Word Insertion Probability");
        jPanel4.add(wip_lbl);

        wip_slider.setMaximum(10);
        wip_slider.setMinorTickSpacing(1);
        wip_slider.setPaintTicks(true);
        wip_slider.setToolTipText("");
        wip_slider.setValue(5);
        wip_slider.setEnabled(false);
        wip_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                wip_sliderStateChanged(evt);
            }
        });
        jPanel4.add(wip_slider);

        wip_value_lbl.setText("50");
        jPanel4.add(wip_value_lbl);

        lw_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lw_lbl.setText("Language Weight");
        jPanel4.add(lw_lbl);

        lw_slider.setMaximum(20);
        lw_slider.setMinimum(1);
        lw_slider.setMinorTickSpacing(1);
        lw_slider.setPaintLabels(true);
        lw_slider.setPaintTicks(true);
        lw_slider.setValue(10);
        lw_slider.setEnabled(false);
        lw_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lw_sliderStateChanged(evt);
            }
        });
        jPanel4.add(lw_slider);

        lw_value_lbl.setText("50");
        jPanel4.add(lw_value_lbl);

        phoneticBeam_lbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        phoneticBeam_lbl.setText("Phonetic Beam");
        jPanel4.add(phoneticBeam_lbl);

        pbeam_slider.setMaximum(50);
        pbeam_slider.setMinimum(-50);
        pbeam_slider.setMinorTickSpacing(5);
        pbeam_slider.setPaintTicks(true);
        pbeam_slider.setValue(0);
        pbeam_slider.setEnabled(false);
        pbeam_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pbeam_sliderStateChanged(evt);
            }
        });
        jPanel4.add(pbeam_slider);

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

        play_pause_btn.setText("Play");
        play_pause_btn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                play_pause_btnItemStateChanged(evt);
            }
        });
        jPanel1.add(play_pause_btn);

        reload_model_btn.setText("Reload Model");
        reload_model_btn.setEnabled(false);
        reload_model_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reload_model_btnActionPerformed(evt);
            }
        });
        jPanel1.add(reload_model_btn);

        clear_btn.setText("Clear");
        clear_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_btnActionPerformed(evt);
            }
        });
        jPanel1.add(clear_btn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 15);
        principal_card_panel.add(jPanel1, gridBagConstraints);

        init_speaker_combo_box.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        init_speaker_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "(None)" }));
        init_speaker_combo_box.setToolTipText("");
        init_speaker_combo_box.setPreferredSize(new java.awt.Dimension(200, 30));
        init_speaker_combo_box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                init_speaker_combo_boxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(7, 15, 0, 0);
        principal_card_panel.add(init_speaker_combo_box, gridBagConstraints);

        card_layout_panel.add(principal_card_panel, "principal_card");

        adaptation_card_panel.setLayout(new java.awt.GridBagLayout());

        adapt_text_area.setColumns(20);
        adapt_text_area.setFont(new java.awt.Font("Monospaced", 0, 18)); // NOI18N
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
        record_btn.setText("Record");
        record_btn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                record_btnItemStateChanged(evt);
            }
        });
        jPanel2.add(record_btn);

        playback_btn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        playback_btn.setText("Playback");
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
        save_btn.setText("Save");
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
        sent_gen_btn.setText("Generate");
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

        mllr_log_txt_area.setColumns(20);
        mllr_log_txt_area.setRows(5);
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
        create_mllr_btn.setText("Create MLLR");
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

        getContentPane().add(card_layout_panel, java.awt.BorderLayout.CENTER);

        file_menu.setText("File");
        file_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        new_speaker_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        new_speaker_menu_item.setText("New speaker");
        new_speaker_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new_speaker_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(new_speaker_menu_item);

        del_speaker_menu_item.setText("Delete speaker");
        del_speaker_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del_speaker_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(del_speaker_menu_item);

        save_as_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        save_as_menu_item.setText("Save As...");
        save_as_menu_item.setEnabled(false);
        save_as_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_as_menu_itemActionPerformed(evt);
            }
        });
        file_menu.add(save_as_menu_item);

        jMenuBar1.add(file_menu);

        edit_menu.setText("Edit");
        edit_menu.setEnabled(false);
        edit_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        selectAll_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAll_menu_item.setText("Select All");
        selectAll_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAll_menu_itemActionPerformed(evt);
            }
        });
        edit_menu.add(selectAll_menu_item);

        crop_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        crop_menu_item.setText("Crop");
        crop_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crop_menu_itemActionPerformed(evt);
            }
        });
        edit_menu.add(crop_menu_item);

        jMenuBar1.add(edit_menu);

        view_menu.setText("View");
        view_menu.setToolTipText("");
        view_menu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        create_report_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        create_report_menu_item.setText("Create report");
        create_report_menu_item.setEnabled(false);
        create_report_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_report_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(create_report_menu_item);

        speaker_adapt_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        speaker_adapt_menu_item.setText("Speaker adaptation");
        speaker_adapt_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speaker_adapt_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(speaker_adapt_menu_item);

        train_mllr_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        train_mllr_menu_item.setText("Train MLLR");
        train_mllr_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                train_mllr_menu_itemActionPerformed(evt);
            }
        });
        view_menu.add(train_mllr_menu_item);

        jMenuBar1.add(view_menu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void beam_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_beam_sliderStateChanged
        NumberFormat formatter;
        formatter = new DecimalFormat("0.#E0");
        double rbw = recognizerConfig.getRelBeamWidth();
        //double rbw = Double.parseDouble(beam_value_lbl.getText());
        double exp = beam_slider.getValue();
        rbw = rbw/Math.pow(10, exp);

        beam_value_lbl.setText(formatter.format(rbw) );
    }//GEN-LAST:event_beam_sliderStateChanged

    private void wip_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_wip_sliderStateChanged
        NumberFormat formatter;
        formatter = new DecimalFormat("0.#");
        wip_value_lbl.setText(formatter.format(wip_slider.getValue()/10.0));
    }//GEN-LAST:event_wip_sliderStateChanged

    private void lw_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lw_sliderStateChanged
        NumberFormat formatter;
        formatter = new DecimalFormat("0.#");
        lw_value_lbl.setText(formatter.format(lw_slider.getValue()));
    }//GEN-LAST:event_lw_sliderStateChanged

    private void pbeam_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pbeam_sliderStateChanged
        NumberFormat formatter;
        formatter = new DecimalFormat("0.#E0");
        double rbw = recognizerConfig.getPbeam();
        //double rbw = Double.parseDouble(beam_value_lbl.getText());
        double exp = pbeam_slider.getValue();

        rbw = rbw/Math.pow(10, exp);

        pbeam_value_lbl.setText(formatter.format(rbw) );
    }//GEN-LAST:event_pbeam_sliderStateChanged

    private void speaker_adapt_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speaker_adapt_menu_itemActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "adaptation_card");
        recognize.Stop_recognition();
        
        edit_menu.setEnabled(true);
        
        speaker_adapt_menu_item.setEnabled(false);
        create_report_menu_item.setEnabled(true);
        train_mllr_menu_item.setEnabled(true);
        
        Logger_status.Log("Adaptation mode.", Logger_status.LogType.INFO);
    }//GEN-LAST:event_speaker_adapt_menu_itemActionPerformed

    private void play_pause_btnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_play_pause_btnItemStateChanged
        //System.out.println(evt.getStateChange());
        if(evt.getStateChange()==ItemEvent.SELECTED){
            recognize.Start_recognition();
            play_pause_btn.setText("Pause");
            report_txt.setEnabled(true);
            
        } 
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            recognize.Pause_recognition();
            play_pause_btn.setText("Play");
            report_txt.setEnabled(false);
        }
    }//GEN-LAST:event_play_pause_btnItemStateChanged

    private void reload_model_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reload_model_btnActionPerformed
        // TODO add your handling code here:
        Map<String, String> global_prop = new HashMap<>();
        global_prop.put(relBeamWidthProp, beam_value_lbl.getText());
        global_prop.put(wipProp, wip_value_lbl.getText());
        global_prop.put(lwProp, lw_value_lbl.getText());
        global_prop.put(pBeam, pbeam_value_lbl.getText());
        recognize = new App_recognizer(global_prop);
        
        //recognize.Start_recognition_reload(global_prop);
        play_pause_btn.setEnabled(true);
        reload_model_btn.setEnabled(false);
        //play_pause_btn.setSelected(false);
        //stop_btn.setEnabled(false);
        beam_slider.setEnabled(false);
        wip_slider.setEnabled(false);
        lw_slider.setEnabled(false);
        pbeam_slider.setEnabled(false);
    
       
    }//GEN-LAST:event_reload_model_btnActionPerformed

    private void create_report_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create_report_menu_itemActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "principal_card");
       
        edit_menu.setEnabled(false);
        
        speaker_adapt_menu_item.setEnabled(true);
        create_report_menu_item.setEnabled(false);
        train_mllr_menu_item.setEnabled(true);
        
        String[] speakers = Directories.getAllSpeakers();
        init_speaker_combo_box.removeAllItems();
        init_speaker_combo_box.addItem("(None)");
        init_speaker_combo_box.setSelectedIndex(0);
        if(speakers.length != 0){
            for(String s : speakers){
                init_speaker_combo_box.addItem(s);
            }
        }
        
        recognize.Init_start_recognition();
    }//GEN-LAST:event_create_report_menu_itemActionPerformed

    private void sent_gen_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sent_gen_btnActionPerformed
        // TODO add your handling code here:
        
        adapt_text_area.setText(generate.generate_sentence());
        
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
            Logger_status.Log("Audio recording...", Logger_status.LogType.INFO);
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
            Logger_status.Log("Audio recording stopped.", Logger_status.LogType.INFO);
        }
    }//GEN-LAST:event_record_btnItemStateChanged

    private void playback_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playback_btnActionPerformed
        // TODO add your handling code here:
        Logger_status.Log("Playing audio...", Logger_status.LogType.INFO);
        player.play(audioPanel.getSelectionStart(),
                       audioPanel.getSelectionEnd());

    }//GEN-LAST:event_playback_btnActionPerformed

    private void save_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_btnActionPerformed
        
        save_file();
    }//GEN-LAST:event_save_btnActionPerformed

    private void save_as_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_as_menu_itemActionPerformed
        
        save_file();
    }//GEN-LAST:event_save_as_menu_itemActionPerformed

    private void crop_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crop_menu_itemActionPerformed
        
        audioPanel.crop();
        Logger_status.Log("Audio cropped.", Logger_status.LogType.INFO);
    }//GEN-LAST:event_crop_menu_itemActionPerformed

    private void selectAll_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAll_menu_itemActionPerformed
        
        audioPanel.selectAll();
    }//GEN-LAST:event_selectAll_menu_itemActionPerformed

    private void clear_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_btnActionPerformed
        // TODO add your handling code here:
        report_txt.setText("");
    }//GEN-LAST:event_clear_btnActionPerformed

    private void train_mllr_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_train_mllr_menu_itemActionPerformed
        // TODO add your handling code here:
        recognize.Stop_recognition();
        speakers_combo_box.removeAllItems();
        speakers_combo_box.addItem("(None)");
        speakers_combo_box.setSelectedIndex(0);
        
        speaker_adapt_menu_item.setEnabled(true);
        create_report_menu_item.setEnabled(true);
        train_mllr_menu_item.setEnabled(false);
        
        CardLayout cl = (CardLayout)(card_layout_panel.getLayout());
        cl.show(card_layout_panel, "mllr_card");
        for(String s:Directories.getAllSpeakers())
            speakers_combo_box.addItem(s);
    }//GEN-LAST:event_train_mllr_menu_itemActionPerformed

    private void new_speaker_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_new_speaker_menu_itemActionPerformed
        // TODO add your handling code here:
        String name = JOptionPane.showInputDialog(this, "Write the new speaker name");
        if(name != null ){
            int confirm = Directories.create_speaker_dir(name);
            switch(confirm){
                case 0:
                    JOptionPane.showMessageDialog(this,"Speaker "+name+" created succesfully");
                    break;
                case 1:
                    JOptionPane.showMessageDialog(this,"Something when wrong.");
                    break;
                case 2:
                    JOptionPane.showMessageDialog(this,"Is not a valid speaker name");
                    break;
            }
        
        }
         
    }//GEN-LAST:event_new_speaker_menu_itemActionPerformed

    private void del_speaker_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del_speaker_menu_itemActionPerformed
        // TODO add your handling code here:
        
        String[] speakers = Directories.getAllSpeakers();
        if(speakers.length != 0){
            String name = (String) JOptionPane.showInputDialog(this, "Select speaker to delete",
                                                        "Delete speaker",
                                                        JOptionPane.QUESTION_MESSAGE, 
                                                        null, 
                                                        speakers,
                                                        speakers[0]);
            if (name!= null){
                Directories.delete_speaker_dir(name);
                JOptionPane.showMessageDialog(this,"Speaker "+name+" deleted succesfully!");
            }
        }
        else{
            JOptionPane.showMessageDialog(this,"No speakers available to delete");
        }
        
    }//GEN-LAST:event_del_speaker_menu_itemActionPerformed

    private void create_mllr_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create_mllr_btnActionPerformed
        // TODO add your handling code here:
        String name = (String) speakers_combo_box.getSelectedItem();
        if(!Directories.is_empty_dir(name) && name != null ){
            mllr_log_txt_area.setText("");
            
            Sphinx_fe acoustic_feature = new Sphinx_fe(name);
            Bw acum_count = new Bw(name);
            Mllr_solve mllr_matrix = new Mllr_solve(name);

            mllr_log_txt_area.append("\n\n********CREATING ID FILES************\n");
            create_fileid_file(name);

            mllr_log_txt_area.append("\n\n********CREATING TRANSCRIPTION FILE************\n");
            create_transcription_file(name);
            
            mllr_log_txt_area.append("\n\n********GENERATING ACOUSTIC FEATURES************\n");
            acoustic_feature.exec_sphinx_fe();
            
            mllr_log_txt_area.append("\n\n********ACUMULATING STATISTIC COUNTS************\n");
            acum_count.exec_bw();
            
            mllr_log_txt_area.append("\n\n********GENERATING MLLR MATRIX************\n");
            mllr_matrix.exec_mllr_solve();
            
            mllr_log_txt_area.append("\n\n********FINISHED************\n");
            JOptionPane.showMessageDialog(this,"MLLR finished");
        }
        else{
            JOptionPane.showMessageDialog(this,"Cannot perform MLLR. Must select a speaker");
        }
               
        
    }//GEN-LAST:event_create_mllr_btnActionPerformed

    private void init_speaker_combo_boxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_init_speaker_combo_boxActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_init_speaker_combo_boxActionPerformed

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(app_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(app_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(app_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(app_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new app_gui().setVisible(true);
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
    private javax.swing.JButton create_mllr_btn;
    private javax.swing.JMenuItem create_report_menu_item;
    private javax.swing.JMenuItem crop_menu_item;
    private javax.swing.JMenuItem del_speaker_menu_item;
    private static javax.swing.JMenu edit_menu;
    private static javax.swing.JMenu file_menu;
    private static javax.swing.JComboBox<String> init_speaker_combo_box;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
