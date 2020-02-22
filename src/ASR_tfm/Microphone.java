/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR_tfm;

import java.io.InputStream;

import javax.sound.sampled.*;

/**
 * InputStream adapter
 */
public class Microphone {

    private final TargetDataLine line;

    public TargetDataLine getLine() {
        return line;
    }
    private final InputStream inputStream;

    public Microphone(
            float sampleRate,
            int sampleSize,
            boolean signed,
            boolean bigEndian) {
        AudioFormat format =
            new AudioFormat(sampleRate, sampleSize, 1, signed, bigEndian);
        try {
            line = AudioSystem.getTargetDataLine(format);
            line.open();
        } catch (LineUnavailableException e) {
            
            throw new IllegalStateException(e);
        }
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

    public InputStream getStream() {
        return inputStream;
    }
}