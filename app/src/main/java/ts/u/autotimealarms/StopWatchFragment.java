package ts.u.autotimealarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;

public class StopWatchFragment extends Fragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    ImageView imageView_hour0;
    ImageView imageView_hour1;
    ImageView imageView_minute0;
    ImageView imageView_minute1;
    ImageView imageView_scond0;
    ImageView imageView_scond1;

    Button button;
    SeekBar seekBar;

    int deal=10;
    boolean SW_Seconds=false;
    boolean SW_Exe = false;

    int old_seconds=99;

    ArrayAdapter<CharSequence> adspin;

    int Cooldowntime = 10000;

    int hour =0;
    int minute = 0;
    int second = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_stopwatch, container, false);

        imageView_hour0 = (ImageView)rootView.findViewById(R.id.ImageView_hour0);
        imageView_hour1 = (ImageView)rootView.findViewById(R.id.imageView_hour1);
        imageView_minute0 = (ImageView)rootView.findViewById(R.id.ImageView_minute0);
        imageView_minute1 = (ImageView)rootView.findViewById(R.id.ImageView_minute1);
        imageView_scond0 = (ImageView)rootView.findViewById(R.id.ImageView_scond0);
        imageView_scond1 = (ImageView)rootView.findViewById(R.id.ImageView_second1);

        Spinner spin = (Spinner)rootView.findViewById(R.id.spinner_stopwatch_term);
        spin.setPrompt("Time Select");


        adspin = ArrayAdapter.createFromResource(MainActivity.context, R.array.dealy, R.layout.style_spinner);
        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adspin);

        SharedPreferences pref = MainActivity.context.getSharedPreferences("PrefTest", 0);
        int dealy = pref.getInt("deal_stopwatch", 1);

        spin.setSelection(dealy);

        CheckBox CheckBox_second = (CheckBox)rootView.findViewById(R.id.CheckBox_stopwatch_second);
        SW_Seconds = pref.getBoolean("seconds_stopwatch", false);
        CheckBox_second.setChecked(SW_Seconds);

        CheckBox_second.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                SW_Seconds = isChecked;
            }
        });

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                switch (position) {
                    case 0: deal = 5;	break;
                    case 1:deal = 10;	break;
                    case 2:deal = 15;	break;
                    case 3:deal = 20;	break;
                    case 4:deal = 30;	break;
                    case 5:deal = 60;	break;
                    case 6:deal = 60*2;	break;
                    case 7:deal = 60*3;	break;
                    case 8:deal = 60*5;	break;
                    case 9:deal = 60*10;	break;
                    case 10:deal = 60*15;	break;
                    default: deal = 5; break;
                }
            }
            public void onNothingSelected(AdapterView<?> parent){}
        });

        final AudioManager audioManager = (AudioManager) MainActivity.context.getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        seekBar = (SeekBar)rootView.findViewById(R.id.seekBar_volume);
        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(volume);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {	}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), AudioManager.FLAG_ALLOW_RINGER_MODES);
            }
        });

        button = (Button)rootView.findViewById(R.id.button_exe);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!SW_Exe) {
                    if(MainActivity.threeThread == null){
                        //Thread start
                        Alarm thread = new Alarm();
                        thread.setDaemon(true);
                        MainActivity.threeThread = thread;
                        thread.start();
                    }
                }else{
                    if(MainActivity.threeThread != null){
                        MainActivity.threeThread = null;
                    }
                }
            }
        });

        Button button_reset = (Button)rootView.findViewById(R.id.button_reset);
        button_reset.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!SW_Exe){
                    hour = 0;
                    minute = 0;
                    second = 0;
                    Cooldowntime =0;

                    MainActivity.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView_hour0.setImageResource(Asset.NUMBER[0]);
                            imageView_hour1.setImageResource(Asset.NUMBER[0]);
                            imageView_minute0.setImageResource(Asset.NUMBER[0]);
                            imageView_minute1.setImageResource(Asset.NUMBER[0]);
                            imageView_scond0.setImageResource(Asset.NUMBER[0]);
                            imageView_scond1.setImageResource(Asset.NUMBER[0]);
                        }
                    });
                }
            }

        });

        return rootView;
    }

    public void resetGUI() {
        MainActivity.handler.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setMax(MainActivity.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                int volume = MainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                seekBar.setProgress(volume);
            }
        });
    }

    class Alarm extends Thread{
        public void run(){
            while (MainActivity.threeThread == this){//oneThread.equals(this)

                imageChange();
                speechTime();

                SW_Exe = true;
                setButtonText(getResources().getString(R.string.stop));

                try {Thread.sleep(1000);} catch (InterruptedException e) {}
            }
            SW_Exe = false;
            setButtonText(getResources().getString(R.string.start));
        }

        public void setButtonText(final String str){
            MainActivity.handler.post(new Runnable() {
                @Override
                public void run() {
                    button.setText(str);
                }
            });
        }

        private void imageChange(){
            MainActivity.handler.post(new Runnable() {
                @Override
                public void run() {
                    if(hour != 0){
                        imageView_hour0.setImageResource(Asset.NUMBER[hour/10]);
                        imageView_hour1.setImageResource(Asset.NUMBER[hour%10]);
                    }else{
                        imageView_hour0.setImageResource(Asset.NUMBER[0]);
                        imageView_hour1.setImageResource(Asset.NUMBER[0]);
                    }
                    if(minute != 0){
                        imageView_minute0.setImageResource(Asset.NUMBER[minute/10]);
                        imageView_minute1.setImageResource(Asset.NUMBER[minute%10]);
                    }else{
                        imageView_minute0.setImageResource(Asset.NUMBER[0]);
                        imageView_minute1.setImageResource(Asset.NUMBER[0]);
                    }
                    if(second != 0){
                        imageView_scond0.setImageResource(Asset.NUMBER[second/10]);
                        imageView_scond1.setImageResource(Asset.NUMBER[second%10]);
                    }else{
                        imageView_scond0.setImageResource(Asset.NUMBER[0]);
                        imageView_scond1.setImageResource(Asset.NUMBER[0]);
                    }
                }
            });
        }

        private void speechTime(){

            Cooldowntime ++;

            second++;
            if(second >= 60){
                minute++;
                second-=60;
            }
            if(minute >=60){
                hour++;
                minute-=60;
            }

            if(Cooldowntime > deal-1){

                StringBuffer timeText = new StringBuffer();
                if(hour > 0){
                    timeText.append(hour + "시간 ");
                }
                if(minute > 0){
                    timeText.append(minute + "분 ");
                }
                if (!SW_Seconds) {
                    timeText.append(second + "초");
                }
                MainActivity.mTts.speak(timeText.toString(), TextToSpeech.QUEUE_FLUSH,null);

                Cooldowntime=0;
            }

        }
    }

}
