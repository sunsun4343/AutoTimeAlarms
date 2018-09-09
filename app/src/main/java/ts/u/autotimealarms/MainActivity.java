package ts.u.autotimealarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.kakao.adfit.ads.AdListener;
import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String LOGTAG = "BannerTypeXML1";
    private BannerAdView adView = null;

    public static Context context;
    public static Handler handler = new Handler();

    PowerManager.WakeLock wakeLock;
    public static AudioManager audioManager = null;
    public static TextToSpeech mTts;

    int select_tab=0;

    public static Thread oneThread = null;
    public static Thread twoThread = null;
    public static Thread threeThread = null;

    static NowTimeFragment nowTimeFragment;
    static TimerFragment timerFragment;
    static StopWatchFragment stopWatchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        initAdFit();

        //TAB Setting

        nowTimeFragment = new NowTimeFragment();
        timerFragment = new TimerFragment();
        stopWatchFragment = new StopWatchFragment();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(nowTimeFragment, "시계");
        adapter.addFragment(timerFragment, "타이머");
        adapter.addFragment(stopWatchFragment, "스톱워치");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                select_tab = tab.getPosition();

                switch (tab.getPosition()) {
                    case 0:
                        if(oneThread == null) { if(nowTimeFragment != null) nowTimeFragment.StartThread();}
                        if(twoThread != null) twoThread = null;
                        if(threeThread != null) threeThread = null;
                        if(nowTimeFragment != null) nowTimeFragment.resetGUI();
                        break;
                    case 1:
                        if(oneThread != null) oneThread = null;
                        if(threeThread != null) threeThread = null;
                        if(timerFragment != null) timerFragment.resetGUI();
                        break;
                    case 2:
                        if(oneThread != null) oneThread = null;
                        if(twoThread != null) twoThread = null;
                        if(stopWatchFragment != null) stopWatchFragment.resetGUI();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Manager Setting
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mTts = new TextToSpeech(this, this);


    }

    @Override
    public void onResume(){
        super.onResume();
        wakeLock.acquire();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        wakeLock.release();

        SharedPreferences pref = getSharedPreferences("PrefTest",0);
        SharedPreferences.Editor edit = pref.edit();

        if(select_tab == 0){
            Spinner spin = (Spinner)findViewById(R.id.spinner_term);
            int d = spin.getSelectedItemPosition();

            edit.putInt("deal", d);

            CheckBox ckb_meri = (CheckBox)findViewById(R.id.checkBox_ampm);
            edit.putBoolean("meri",ckb_meri.isChecked());

            CheckBox ckb_hour = (CheckBox)findViewById(R.id.CheckBox_hour);
            edit.putBoolean("hour",ckb_hour.isChecked());

            CheckBox ckb_seconds = (CheckBox)findViewById(R.id.CheckBox_second);
            edit.putBoolean("seconds",ckb_seconds.isChecked());
        }

        if(select_tab == 1){
            Spinner spinner_timer_term = (Spinner)findViewById(R.id.spinner_timer_term);
            int spinner_timer_term_index = spinner_timer_term.getSelectedItemPosition();
            edit.putInt("deal_timer", spinner_timer_term_index);

            CheckBox CheckBox_timer_second = (CheckBox)findViewById(R.id.CheckBox_timer_second);
            edit.putBoolean("seconds_timer",CheckBox_timer_second.isChecked());
        }

        if(select_tab == 2){
            Spinner spinner_stopwatch_term = (Spinner)findViewById(R.id.spinner_stopwatch_term);
            int spinner_stopwatch_term_index = spinner_stopwatch_term.getSelectedItemPosition();
            edit.putInt("deal_stopwatch", spinner_stopwatch_term_index);

            CheckBox CheckBox_stopwatch_second = (CheckBox)findViewById(R.id.CheckBox_stopwatch_second);
            edit.putBoolean("seconds_stopwatch",CheckBox_stopwatch_second.isChecked());

            Log.d("TestG", "save call");
        }

        edit.commit();

        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    private void initAdFit() {
        // AdFit sdk 초기화 시작
        adView = (BannerAdView) findViewById(R.id.adview);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(LOGTAG, "onAdLoaded");
            }

            @Override
            public void onAdFailed(int code) {
                Log.d(LOGTAG, "onAdFailed : " + code);
            }

            @Override
            public void onAdClicked() {
                Log.d(LOGTAG, "onAdClicked");
            }
        });

        // 할당 받은 clientId 설정
        adView.setClientId("DAN-s1dyg5u9jp6d");

        // 광고 갱신 시간 : 기본 60초
        // 0 으로 설정할 경우, 갱신하지 않음.
        adView.setRequestInterval(30);

        // 광고 사이즈 설정
        adView.setAdUnitSize("320x50");

        adView.loadAd();
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = mTts.setLanguage(Locale.KOREA);

            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                //Toast
                Log.e("TestG", "Language is not available.");
            }
        }else{
            //Toast
            Log.e("TestG", "Could not initialze TextToSpeech.");
        }
    }


}
