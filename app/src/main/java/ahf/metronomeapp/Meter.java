package ahf.metronomeapp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Alex on 10/18/2015.
 */
public class Meter extends AsyncTask<Void,Void,String> { //might makes this a runnable, I'm not sure how I want to implement this yet

    //Constants
    private final double beatSound = 2440;
    private final double sound = 6440;
    private final int sampleRate = 8000; //8000 samples "blocks" per second
    private final int milisInMin = 60000;
    private final int milisInSec = 1000; // number of miliseconds in a second

    private int beats = 4;
    private int bpm = 120;
    private int noteValue = 4;


    private boolean play = true;
    private int subdivisions;

    private Handler handler;
    private Message message;

    //Ouput Sound
    private int beat;
    private int silence;
    private int totalBeatLength;
    private byte[] oneBeatOutput;
    private byte[] otherBeatOutput;
    private byte[] silenceOutput;
    //private AudioManager audio;
    private AudioTrack audioTrack;

    /*
        I need to convert Meter into a base class. From there I will create a normal measure class and a polyrythm measure class that will extend from base
    */

    /* Need to split normal metronome & polyrythm into seperate classes: (singleNome & polyNome)*/
    public Meter(Handler handle, int bpm, int noteValue, int beats)
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
        this.handler = handle;
        this.bpm = bpm;
        this.noteValue = noteValue;
        this.beats = beats;


        totalBeatLength = calculateBeatLength(bpm, noteValue);
        beat = totalBeatLength / 2;
        silence = totalBeatLength - beat;

        //Step 3
        oneBeatOutput = get16BitPcm(beat, beatSound);
        otherBeatOutput = get16BitPcm(beat, sound);
        silenceOutput = new byte[silence];

        //Step 4
        createPlayer();
    }

    public Meter(int bpm, int noteValue, int beats, int polyValue)
    {
        /* Steps to complete
            1. Get Values from UI
                -get bpm
                -get note value
                -polyrythm: Yes|No
            Note: Should I only consider polyrythms in quarter notes? No, but you will need to base it off them
            2. Calculate lengths (totalBeat, beat and silence)
            3. Generate 16bitPCM arrays
            4. Initalize AudioTrack (should be in constructor for runnable)
        */
        this.bpm = bpm;
        this.noteValue = noteValue;
        this.beats = polyValue;

        totalBeatLength = calculateBeatLength(bpm, noteValue);
        totalBeatLength = (totalBeatLength * beats) / polyValue;

        beat = totalBeatLength / 2;
        silence = totalBeatLength - beat;
        //Step 3
        oneBeatOutput = get16BitPcm(beat, beatSound);
        otherBeatOutput = get16BitPcm(beat, sound);
        silenceOutput = new byte[silence];

        //Step 4
        createPlayer();
    }

    private int calculateBeatLength(int bpm, int noteValue)
    {
        int beatLength = (int) (milisInMin/bpm);
        switch(noteValue)
        {
            case 3: //Eight Note Triplet
                beatLength = beatLength / 3;
                break;
            case 5: //Quintuplet
                beatLength = beatLength / 5;
                break;
            case 6: //Sixtenth Note Triplet
                beatLength = beatLength / 6;
                break;
            case 7: //Septuplets
                beatLength = beatLength / 7;
                break;
            case 8: //Eigth Notes
                beatLength = beatLength / 2;
                break;
            case 16: //Sixteenth Notes
                beatLength = beatLength / 4;
                break;
            case 32: //ThirtySecond Notes
                beatLength = beatLength / 8;
                break;
            default: //if here, then noteValue = 4, which means quarter note
                break;
        }
        return beatLength;
    }

    public double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
        }
        return sample;
    }

    public byte[] get16BitPcm(int numSamples, double frequencyOfTone) {
        double[] samples = getSineWave(numSamples, sampleRate, frequencyOfTone);
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

    public void writeSound(byte[] sound) {
        audioTrack.write(sound, 0, sound.length);
    }

    public void createPlayer(){

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate, //need to review
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                sampleRate, //need to review
                AudioTrack.MODE_STREAM);
    }

    public void destroyAudioTrack() {
        audioTrack.stop();
        audioTrack.release();
    }


    @Override
    protected String doInBackground(Void... params) {

        message = new Message();
        int currentSub = 1;
        int currentBeat = 1;
        message.obj = String.valueOf(currentBeat);

        do{
            handler.sendMessage(message);
            if(currentBeat == 1 && currentSub == 1)
            {
                writeSound(oneBeatOutput);
            }else
            {
                writeSound(otherBeatOutput);
            }
            writeSound(silenceOutput);

            if(currentSub == subdivisions)
            {
                currentSub = 0;
                currentBeat++;
            }else
            {
                currentSub++;
            }

            if(currentBeat > beats)
            {
                currentBeat = 1;
            }
            message.obj = String.valueOf(currentBeat);
        }while(play);

        return null;
    }
    //Allow toggle for silence between beats of rhythm or playing subdivisions

    //public Meter(Handler handle, int bpm, int noteValue, int beats)
    //Meter polyMeter = new Meter(int bpm, int subdivision, int beats);
    //Meter polyMeter = new Meter(new Handler(), bpm, secondaryPulse, primaryPulse);
    protected String doInBackgroundPolyrhythm(Void... params) {
        int primaryPulse = 3; //denominator of fraction
        int secondaryPulse = 4; //numerator of fraction, this number dictates what subdivision the polyrhythm uses
        int currentPolySub = 1;
        int currentPoly = 1;
        int polyInterval = primaryPulse;
        byte[] fullSilenceOutput = new byte[totalBeatLength]; //example: 120 bpm: quarter note is 500 ms, silence will need to take up 500 ms
        message = new Message();
        message.obj = String.valueOf(currentPoly);
        do{
            if(currentPolySub == 1){
                writeSound(oneBeatOutput);
                handler.sendMessage(message);
                currentPolySub++;
            }else if(currentPolySub == polyInterval){
                writeSound(fullSilenceOutput);
                currentPolySub = 1;
            }
            else{
                writeSound(fullSilenceOutput);
                currentPolySub++;
            }
        }while(play);

        return null;
    }
}

/*
    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        Metronome metronome;

        MetronomeAsyncTask() {
            mHandler = getHandler();
            metronome = new Metronome(mHandler);
        }

        protected String doInBackground(Void... params) {
            metronome.setBeat(beats);
            metronome.setNoteValue(noteValue);
            metronome.setBpm(bpm);
            metronome.setBeatSound(beatSound);
            metronome.setSound(sound);

            metronome.play();

            return null;
        }

        public void stop() {
            metronome.stop();
            metronome = null;
        }

        public void setBpm(short bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }

        public void setBeat(short beat) {
            if(metronome != null)
                metronome.setBeat(beat);
        }

        public void setNoteValue(int noteValue){
            if(metronome != null){
                metronome.setNoteValue(noteValue);
            }
        }

    }

 */