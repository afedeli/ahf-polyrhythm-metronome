package ahf.metronomeapp;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Alex on 10/4/2015.
 */
public class MetronomeRetainFrag extends Fragment {

    //State info
    private int bpm = 120;
    private int noteValue = 4;
    private int beats = 4;
    private double beatSound = 2440;
    private double sound = 6440;
    private final int sampleRate = 8000; //8000 samples "blocks" per second
    private final int milisInMin = 60000;
    private final int milsInSec = 1000; // number of miliseconds in a second
    private short volume;
    private short initialVolume;


    //Ouput Sound
    int beat;
    int silence;
    int totalBeatLength;
    private byte[] oneBeatOutput;
    private byte[] otherBeatOutput;
    private byte[] silenceOutput;
    private AudioManager audio;
    private AudioTrack audioTrack;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate((savedInstanceState));

        setRetainInstance(true);
    }

    private void thinkingFunction()
    {
        /* Steps to complete
            1. Get Values from UI
                -get bpm
                -get note value
                -polyrythm: Yes|No
            Note: Should I only consider polyrythms in quarter notes?
            2. Calculate lengths (totalBeat, beat and silence)
            3. Generate 16bitPCM arrays
            4. Initalize AudioTrack (should be in constructor for runnable)
        * */
        //Step 2
         //Calculate lengths for sounds
        totalBeatLength = (int) (milisInMin/bpm);
        beat = totalBeatLength / 2;
        silence = totalBeatLength - beat;

        //Step 3
        //Create sound output arrays
        //this current implementation will change, need to refactor
        oneBeatOutput = get16BitPcm(getSineWave(beat, sampleRate, beatSound));
        otherBeatOutput = get16BitPcm(getSineWave(beat, sampleRate, sound));
        silenceOutput = new byte[silence];

        //Step 4

    }

    private void thinkingFunctionPolyrhythm()
    {
        /* Steps to complete
            1. Get Values from UI
                -get bpm
                -get note value
                -polyrhythm: Yes|No
            Note: Should I only consider polyrythms in quarter notes?
            2. Calculate lengths (totalBeat, beat and silence)
            3. Generate 16bitPCM arrays
            4. Initalize AudioTrack (should be in constructor for runnable)
        */
        int primaryPulse = 3; //denominator of fraction
        int secondaryPulse = 4; //numerator of fraction, this number dictates what subdivision the polyrhythm uses
        //the fraction is secondary/primary
        int totalNotes = primaryPulse * secondaryPulse; //total number of 'clicks' in a polyrhythm
        //Meter polyMeter = new Meter(int bpm, int subdivision, int beats);
        Meter polyMeter = new Meter(new Handler(), bpm, secondaryPulse, primaryPulse);

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
        //audioTrack.play();
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
