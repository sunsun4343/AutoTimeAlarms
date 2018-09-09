package ts.u.autotimealarms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
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

import java.util.Calendar;
import java.util.GregorianCalendar;

import static ts.u.autotimealarms.MainActivity.audioManager;

public class NowTimeFragment extends Fragment {

    ImageView imageView_hour0;
    ImageView imageView_hour1;
    ImageView imageView_minute0;
    ImageView imageView_minute1;
    ImageView imageView_scond0;
    ImageView imageView_scond1;
    ImageView imageView_ampm;

    int deal=10;
    boolean SW_Meri=false;
    boolean SW_Hour=false;
    boolean SW_Seconds=false;
    boolean SW_Alram = false;

    int old_meridiem=99;
    int old_hour=99;
    int old_min=99;
    int old_seconds=99;

    SeekBar seekBar;

    ArrayAdapter<CharSequence> adspin;

    int Cooldowntime = 10000;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_nowtime, container, false);

        imageView_hour0 = (ImageView)rootView.findViewById(R.id.ImageView_hour0);
        imageView_hour1 = (ImageView)rootView.findViewById(R.id.imageView_hour1);
        imageView_minute0 = (ImageView)rootView.findViewById(R.id.ImageView_minute0);
        imageView_minute1 = (ImageView)rootView.findViewById(R.id.ImageView_minute1);
        imageView_scond0 = (ImageView)rootView.findViewById(R.id.ImageView_scond0);
        imageView_scond1 = (ImageView)rootView.findViewById(R.id.ImageView_second1);
        imageView_ampm = (ImageView)rootView.findViewById(R.id.ImageView_ampm);

        Spinner spin = (Spinner)rootView.findViewById(R.id.spinner_term);
        spin.setPrompt("Time Select");


        adspin = ArrayAdapter.createFromResource(MainActivity.context, R.array.dealy, R.layout.style_spinner);
        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adspin);

        SharedPreferences pref = MainActivity.context.getSharedPreferences("PrefTest", 0);
        int dealy = pref.getInt("deal", 1);

        spin.setSelection(dealy);

        CheckBox checkBox_ampm = (CheckBox)rootView.findViewById(R.id.checkBox_ampm);
        SW_Meri = pref.getBoolean("meri", false);
        checkBox_ampm.setChecked(SW_Meri);

        checkBox_ampm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                SW_Meri = isChecked;
            }
        });

        CheckBox CheckBox_hour = (CheckBox)rootView.findViewById(R.id.CheckBox_hour);
        SW_Hour = pref.getBoolean("hour", false);
        CheckBox_hour.setChecked(SW_Hour);

        CheckBox_hour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                SW_Hour = isChecked;
            }
        });

        CheckBox CheckBox_second = (CheckBox)rootView.findViewById(R.id.CheckBox_second);
        SW_Seconds = pref.getBoolean("seconds", false);
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

        Button button_help = (Button)rootView.findViewById(R.id.button_help);
        button_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                marketLaunch.setData(Uri.parse("market://details?id=com.google.android.tts"));
                startActivity(marketLaunch);
            }
        });

        StartThread();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void resetGUI() {
        MainActivity.handler.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                seekBar.setProgress(volume);
            }
        });
    }

    public void StartThread(){
        if(MainActivity.oneThread == null){
            //Thread start
            Alarm thread = new Alarm();
            thread.setDaemon(true);
            MainActivity.oneThread = thread;
            thread.start();
        }
    }

    class Alarm extends Thread{
        public void run(){
            while (MainActivity.oneThread == this){//oneThread.equals(this)

                imageChange();
                speechTime();

                try {Thread.sleep(1000);} catch (InterruptedException e) {}
            }
        }

        private void imageChange(){
            MainActivity.handler.post(new Runnable() {
                @Override
                public void run() {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int hour = calendar.get(Calendar.HOUR);
                    int minute = calendar.get(Calendar.MINUTE);
                    int second = calendar.get(Calendar.SECOND);
                    int ampm = calendar.get(Calendar.AM_PM);

                    imageView_hour0.setImageResource(Asset.NUMBER[hour/10]);
                    imageView_hour1.setImageResource(Asset.NUMBER[hour%10]);
                    imageView_minute0.setImageResource(Asset.NUMBER[minute/10]);
                    imageView_minute1.setImageResource(Asset.NUMBER[minute%10]);
                    imageView_scond0.setImageResource(Asset.NUMBER[second/10]);
                    imageView_scond1.setImageResource(Asset.NUMBER[second%10]);
                    imageView_ampm.setImageResource(Asset.AMPM[ampm]);
                }
            });
        }

        private void speechTime(){

            Cooldowntime ++;

            if(Cooldowntime > deal-1){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                int ampm = calendar.get(Calendar.AM_PM);

                StringBuffer timeText = new StringBuffer();
                if(!SW_Meri){
                    if(ampm == 0){timeText.append("오전 ");}else{timeText.append("오후 ");}
                }
                if(!SW_Hour){
                    timeText.append(hour + "시.");
                }
                timeText.append(minute + "분 ");
                if (!SW_Seconds) {
                    timeText.append(second + "초");
                }
                MainActivity.mTts.speak(timeText.toString(), TextToSpeech.QUEUE_FLUSH,null);

                Cooldowntime=0;
            }

        }
    }

}
