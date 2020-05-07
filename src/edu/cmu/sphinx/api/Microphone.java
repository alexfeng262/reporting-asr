/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

package edu.cmu.sphinx.api;

import ASR_tfm.AppGui;
import ASR_tfm.Logger_status;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.*;

/**
 * InputStream adapter
 */
public class Microphone {

    private final TargetDataLine line;
    private final InputStream inputStream;

    public Microphone (
            float sampleRate,
            int sampleSize,
            boolean signed,
            boolean bigEndian) throws LineUnavailableException, IllegalArgumentException {
        AudioFormat format =
            new AudioFormat(sampleRate, sampleSize, 1, signed, bigEndian);
        
            line = AudioSystem.getTargetDataLine(format);
            line.open();

        inputStream = new AudioInputStream(line);
    }

    public void startRecording() {
        line.start();
    }

    public void stopRecording() {
        line.stop();
    }

    public void closeConnection() {
        line.close();
    }
    public void openLineConnection() {
        try {
            line.open();
        } catch (LineUnavailableException ex) {
            
            Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InputStream getStream() {
        return inputStream;
    }
}
