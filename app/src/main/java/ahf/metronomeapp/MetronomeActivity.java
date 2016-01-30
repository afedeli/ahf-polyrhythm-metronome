package ahf.metronomeapp;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.media.AudioManager;
import android.widget.Spinner;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

//Need to implement my note value into Metronome class

public class MetronomeActivity extends ActionBarActivity {

    private int bpm = 120;
    private int noteValue = 4;
    private int beats = 4;
    private double beatSound = 2440;
    private double sound = 6440;
    private short volume;
    private short initialVolume;

    private AudioManager audio;
    private MetronomeAsyncTask metroTask;
    private Metronome metronome;

    private Button start_stop;
    private NumberPicker bpmPicker;
    private Spinner noteValueSpinner;
    private NumberPicker beatPicker;
    private TextView currentBeat;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metronome_main);

        metroTask = new MetronomeAsyncTask();

        bpmPicker = (NumberPicker)findViewById(R.id.bpmPicker);
        bpmPicker.setMinValue(60);
        bpmPicker.setMaxValue(180);
        bpmPicker.setValue(bpm);
        bpmPicker.setOnValueChangedListener(bpmListener);
        bpmPicker.clearFocus();

        noteValueSpinner = (Spinner)findViewById(R.id.noteValueSpinner);
        ArrayAdapter<NoteValue> arrayBeats = new ArrayAdapter<NoteValue>(this, android.R.layout.simple_spinner_item, NoteValue.values());
        noteValueSpinner.setAdapter(arrayBeats);
        noteValueSpinner.setSelection(NoteValue.quarterNote.ordinal());
        arrayBeats.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        noteValueSpinner.setOnItemSelectedListener(noteValueSpinnerListener);


        beatPicker = (NumberPicker)findViewById(R.id.beatNumPicker);
        beatPicker.setMinValue(numBeats.one.getNum());
        beatPicker.setMaxValue(numBeats.eight.getNum());
        beatPicker.setValue(beats);
        beatPicker.setOnValueChangedListener(beatListener);
        beatPicker.clearFocus();

        currentBeat = (TextView)findViewById(R.id.lblBeatNum);

        start_stop = (Button)findViewById(R.id.btnStartStop);

        start_stop.setOnClickListener(start_stop_listener);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = initialVolume;

        SeekBar volumebar = (SeekBar)findViewById(R.id.volumeBar);
        volumebar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumebar.setProgress(volume);
        volumebar.setOnSeekBarChangeListener(volumeListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_metronome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener start_stop_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String btnText = start_stop.getText().toString();
            if(btnText.equalsIgnoreCase("start")){
                start_stop.setText(R.string.btnStop);

                bpmPicker.clearFocus();
                bpmPicker.setClickable(false);
                bpmPicker.setEnabled(false);
                bpmPicker.setFocusable(false);
                //bpmPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                noteValueSpinner.clearFocus();
                noteValueSpinner.setClickable(false);
                noteValueSpinner.setEnabled(false);
                noteValueSpinner.setFocusable(false);
                beatPicker.clearFocus();
                beatPicker.setClickable(false);
                beatPicker.setEnabled(false);
                beatPicker.setFocusable(false);
                //beatPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
                }else{
                    metroTask.execute();
                }

            }else{
                start_stop.setText(R.string.btnStart);

                bpmPicker.clearFocus();
                bpmPicker.setClickable(true);
                bpmPicker.setEnabled(true);
                bpmPicker.setFocusable(true);
                //bpmPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                noteValueSpinner.clearFocus();
                noteValueSpinner.setClickable(true);
                noteValueSpinner.setEnabled(true);
                noteValueSpinner.setFocusable(true);
                beatPicker.clearFocus();
                beatPicker.setClickable(true);
                beatPicker.setEnabled(true);
                beatPicker.setFocusable(true);
                //beatPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                metroTask.stop();
                metroTask = new MetronomeAsyncTask();
                currentBeat.setTextColor(Color.GREEN);
                currentBeat.setText("1");
                Runtime.getRuntime().gc();
            }
        }
    };

    private OnValueChangeListener beatListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            beats = beatPicker.getValue();
            metroTask.setBeat((short) beats);
        }
    };


    private OnValueChangeListener bpmListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            bpm = bpmPicker.getValue();
            metroTask.setBpm((short) bpm);
        }
    };

    private OnSeekBarChangeListener volumeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            // TODO Auto-generated method stub
            volume = (short) progress;
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    };


    private OnItemSelectedListener noteValueSpinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // TODO Auto-generated method stub
            NoteValue selectedNoteValue = (NoteValue) arg0.getItemAtPosition(arg2);
            noteValue = selectedNoteValue.getNum();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            noteValue = NoteValue.quarterNote.getNum();
        }

    };

    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String)msg.obj;
                if(message.equals("1"))
                    currentBeat.setTextColor(Color.GREEN);
                else
                    currentBeat.setTextColor(Color.BLACK);
                currentBeat.setText(message);
            }
        };
    }

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

}
