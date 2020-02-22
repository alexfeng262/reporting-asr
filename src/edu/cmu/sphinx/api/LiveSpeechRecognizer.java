/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.sphinx.api;

/*
 * Copyright 2013 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

import edu.cmu.sphinx.api.AbstractSpeechRecognizer;
//import edu.cmu.sphinx.api.Microphone;


import edu.cmu.sphinx.api.Context;

import java.io.IOException;

import edu.cmu.sphinx.frontend.util.StreamDataSource;


/**
 * High-level class for live speech recognition.
 */
public class LiveSpeechRecognizer extends AbstractSpeechRecognizer {

    private Microphone microphone;

    /**
     * Constructs new live recognition object.
     *
     * @param context
     * @throws IOException if model IO went wrong
     */
    public LiveSpeechRecognizer(Context config) throws IOException
    { 
        super(config);
        microphone = new Microphone(16000, 16, true, false);
        context.getInstance(StreamDataSource.class)
            .setInputStream(microphone.getStream());
        recognizer.allocate();
        
        
    }
    
    public LiveSpeechRecognizer(Configuration config) throws IOException
    { 
        super(config);
        microphone = new Microphone(16000, 16, true, false);
        context.getInstance(StreamDataSource.class)
            .setInputStream(microphone.getStream());
        recognizer.allocate();
        
        
    }
    /**
     * Starts recognition process.
     *
     * @param clear clear cached microphone data
     * @see         LiveSpeechRecognizer#stopRecognition()
     */
    public void startRecognition(boolean clear) {
        microphone.startRecording();
    }
    public void startRecognitionNormal(boolean clear) {
        //recognizer.allocate();
        //recognizer.resetMonitors();
        microphone.openLineConnection();
        //microphone.startRecording();
    }
    
    public void init_start_recognition() {
        recognizer.allocate();
        microphone.openLineConnection();
        microphone.startRecording();
    }


    /**
     * Stops recognition process.
     *
     * Recognition process is paused until the next call to startRecognition.
     *
     * @see LiveSpeechRecognizer#startRecognition(boolean)
     */
    public void stopRecognition() {
        microphone.stopRecording();
        //recognizer.deallocate();
    }


    /**
     * Stops Recognition process.
     *
     * Closes the Microphone Connection and Data Line is made available for other applications.
     */

    public void closeRecognition(){
        //recognizer.allocate();
        //microphone.stopRecording();
        microphone.closeConnection();
        recognizer.deallocate();
        
    }
}