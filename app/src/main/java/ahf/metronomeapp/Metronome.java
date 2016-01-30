package ahf.metronomeapp;

/**
 * Created by Alex on 5/6/2015.
 */

import android.os.Handler;
import android.os.Message;

public class Metronome {


    private double bpm;
    private int beat;
    private int noteValue;
    private int silence;

    private double beatSound;
    private double sound;
    private int sampleRate = 8000;
    private int tick = 1000; // samples of tick

    private boolean play = true;

    private AudioGenerator audioGenerator = new AudioGenerator(8000);
    private Handler mHandler;
    private double[] soundTickArray;
    private double[] soundTockArray;
    private double[] silenceSoundArray;
    private Message msg;
    private int currentBeat = 1;
    private int currentSub = 1;

    public Metronome(Handler handler) {
        audioGenerator.createPlayer();
        this.mHandler = handler;
    }

    public void calcSilence() {
        silence = (int) (((60/bpm)*sampleRate)-tick);
        soundTickArray = new double[this.tick];
        soundTockArray = new double[this.tick];
        silenceSoundArray = new double[this.silence];
        msg = new Message();
        msg.obj = ""+currentBeat;
        double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
        double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
        for(int i=0;i<this.tick;i++) {
            soundTickArray[i] = tick[i];
            soundTockArray[i] = tock[i];
        }
        for(int i=0;i<silence;i++)
            silenceSoundArray[i] = 0;
    }
    /* I need to edit the switch statement. The sampleRate should be consistant across all subdivisions.
    * What should be changing is the duration of tick, tock and silence based on the bpm and note value*/
    public void play() {
        int subdivision = 1;
        switch(noteValue) {
            case 4:
                sampleRate = 8000;
                tick = 1000;
                subdivision = 1;
                break;
            case 8:
                sampleRate = 4000;
                tick = 500;
                subdivision = 2;
                break;
            case 16:
                sampleRate = 2000;
                tick = 250;
                subdivision = 4;
                break;
            default:
                sampleRate = 8000;
                tick = 1000;
                subdivision = 1;
                break;
        }
        calcSilence();
        do {
            msg = new Message();
            msg.obj = ""+currentBeat;
            if(currentBeat == 1)
                audioGenerator.writeSound(soundTockArray);
            else
                audioGenerator.writeSound(soundTickArray);

            audioGenerator.writeSound(silenceSoundArray);
            mHandler.sendMessage(msg);
            if(currentSub == subdivision){
                currentSub = 1;
                currentBeat++;
            }else{
                currentSub++;
            }

            if(currentBeat > beat)
                currentBeat = 1;
        } while(play);
    }

    public void stop() {
        play = false;
        audioGenerator.destroyAudioTrack();
    }

    public double getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(int noteValue) {
        this.noteValue = noteValue;
    }

    public int getBeat() {
        return beat;
    }

    public void setBeat(int beat) {
        this.beat = beat;
    }

    public double getBeatSound() {
        return beatSound;
    }

    public void setBeatSound(double sound1) {
        this.beatSound = sound1;
    }

    public double getSound() {
        return sound;
    }

    public void setSound(double sound2) {
        this.sound = sound2;
    }


}
