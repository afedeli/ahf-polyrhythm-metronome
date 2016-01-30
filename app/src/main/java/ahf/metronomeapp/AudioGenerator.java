package ahf.metronomeapp;

/**
 * Created by Alex on 5/6/2015.
 *
 * This file was created by Periklis Ntanasis. The reason I am utilizing his work
 * because of a deficiency within the Android Operating system regarding threads
 * in conjunction with timers, Which was my initial approach and failed. I also
 * attempted to implement a multithreaded approach with failed as well. After
 * thorough research, I found this mans work and implemented it in my metronome
 *
 */

/* This class needs to be dismantled. getSineWave(), get16BitPcm() and the AudioTrack should be removed from this
*  writeSound() should not be always converting the sample, it's far to memory intensive
*  createPlayer() shouldn't be calling play directly after*/

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
public class AudioGenerator {

    private int sampleRate;
    private AudioTrack audioTrack;

    public AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
        }
        return sample;
    }

    public byte[] get16BitPcm(double[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples) {
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }

    public void createPlayer(){
        //FIXME sometimes audioTrack isn't initialized
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sampleRate,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void writeSound(double[] samples) {
        byte[] generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }

    public void destroyAudioTrack() {
        audioTrack.stop();
        audioTrack.release();
    }

}
