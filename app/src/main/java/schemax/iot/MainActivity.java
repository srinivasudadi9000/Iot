package schemax.iot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import schemax.iot.model.Weather;
import schemax.iot.model.WeatherData;
import schemax.iot.rest.ApiClient;
import schemax.iot.rest.ApiInterface;

import static schemax.iot.DBHelper.db;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    EditText spell_et;
    Button speak_btn, visible_bt;
    TextToSpeech tts;
    RecyclerView voice_recyler;
    List<Weather> weather;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spell_et = (EditText) findViewById(R.id.spell_et);
        speak_btn = (Button) findViewById(R.id.speak_btn);
        visible_bt = (Button) findViewById(R.id.visible_bt);
        voice_recyler = (RecyclerView) findViewById(R.id.voices_listview);
        tts = new TextToSpeech(this, this);

        givepermissions();
        weather = new ArrayList<Weather>();

        speak_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startService(new Intent(MainActivity.this, MyService.class));
                Toast.makeText(getBaseContext(), "speaker clicked ", Toast.LENGTH_SHORT).show();
                speakOut();
                String xx = String.valueOf(isNetworkAvailable());
                //Log.d("internecte",xx);
                if (isNetworkAvailable()) {
                    //Log.d("internet", "internet conneccted");
                         Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //Log.d("running run","Thread Running");
                                try {
                                    Thread.sleep(10000);
                                  //  clearPreferences();
                                    IOTData();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        t.start();
                    if (t.isAlive()){
                        //Log.d("running alive", String.valueOf(t.isAlive()));
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.d("running destroy", String.valueOf(t.isAlive()));
                    }
                        //  Thread.sleep(10000);

                } else {
                    //Log.d("internet", "intaernet disconnected");
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("running run","Thread Running");
                            try {
                                Thread.sleep(10000);
                                looprunning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                    if (t.isAlive()){
                        //Log.d("running alive", String.valueOf(t.isAlive()));
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.d("running destroy", String.valueOf(t.isAlive()));
                    }

                }
                //looprunning();
                //timecondition();

            }
        });

    }


    public void looprunning()
    {
        final String[] verification = {""};

        final ArrayList<Weather> install_offline = new ArrayList<Weather>();
        db = openOrCreateDatabase("IOT_schemax", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM myvoice ", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String voice_id = c.getString(c.getColumnIndex("voice_id"));
                String voice_des = c.getString(c.getColumnIndex("voice_des"));
                String voice_date = c.getString(c.getColumnIndex("voice_date"));
                String voice_time = c.getString(c.getColumnIndex("voice_time"));
                String team = c.getString(c.getColumnIndex("team"));
                String flag = c.getString(c.getColumnIndex("flag"));

                install_offline.add(new Weather(voice_id, voice_des, voice_date, voice_time, team, flag));

                c.moveToNext();
            }
        }
        db.close();
        //Log.d("installoffline", String.valueOf(install_offline.size()));
        for (int i = 0; i < install_offline.size(); i++) {

            final String temp_date = install_offline.get(i).getDate().toString().concat(" " + install_offline.get(i).getTime() + " ");
            //Log.d("tempdate", temp_date);

            Calendar cc = Calendar.getInstance();
            cc.add(Calendar.MINUTE, 0);
            final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            final String Datetime = df.format(cc.getTime());
            final Date[] current_time = {null};
            final int finalI = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("running run","Thread Running");
                    try {
                        current_time[0] = df.parse(Datetime);

                        //Log.d("datetime", String.valueOf(current_time[0].getTime()));

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                        // Date d2 = format.parse("03/12/2017 10:50 AM ");
                        Date d2 = format.parse(temp_date);
                        Double hoursDifference = Double.valueOf((current_time[0].getTime() - d2.getTime()) / 60000);
                        //int hoursDifference = (Double)((current_time.getTime() - d2.getTime()) / 3600000L);
                        //Log.d("difference in time", String.valueOf(hoursDifference));
                        if (hoursDifference < 0) {
                            //Log.d("date2 greater", "date2");
                            verification[0] = "verification";

                            tts.speak("..Main "+install_offline.get(finalI).getVoice_des(), TextToSpeech.QUEUE_FLUSH, null);
                            tts.setLanguage(Locale.ENGLISH);
                            //tts.setPitch(9);
                            tts.setSpeechRate(1);
                            Log.d("sppech",install_offline.get(finalI).getVoice_des().toString());
                            Thread.sleep(20000);
                            // TimeUnit.MINUTES.sleep(1);
                        } else {
                            //Log.d("date1 greater", "date1");
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            if (t.isAlive()){
                //Log.d("running alive", String.valueOf(t.isAlive()));
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d("running destroy", String.valueOf(t.isAlive()));
            }




            if (i == install_offline.size() - 1) {

                    Thread tt = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("running run","Thread Running");
                            try {
                                TimeUnit.MINUTES.sleep(1);
                                if (isNetworkAvailable()){
                                    IOTData();
                                }else {
                                    if (isNetworkAvailable()) {
                                        Log.d("internet", "internet conneccted");
                                        clearPreferences();
                                        IOTData();
                                    } else {
                                        Log.d("internet", "internet disconnected looprunning");
                                        clearPreferences();
                                        looprunning();
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    tt.start();
                    if (tt.isAlive()){
                        Log.d("running alive", String.valueOf(t.isAlive()));
                        try {
                            tt.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.d("running destroy", String.valueOf(t.isAlive()));
                    }
              }
        }

    }

    private void givepermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.BLUETOOTH
                    , Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_PRIVILEGED}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Log.v("permission", "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //Log.e("TTS", "This Language is not supported");
            } else {
                speak_btn.setEnabled(true);
                speakOut();
            }

        } else {
            //Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String text = spell_et.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        tts.setLanguage(Locale.ENGLISH);
        //tts.setPitch(9);
        tts.setSpeechRate(1);
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        Toast.makeText(getBaseContext(), "completed this is utteranceid", Toast.LENGTH_SHORT).show();
    }


    public   void IOTData() {
        ApiInterface apiService = ApiClient.getWeatherDetails().create(ApiInterface.class);
        Call<WeatherData> call = apiService.getWeatherDetails();
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                String result = String.valueOf(response.body().getresult());

                String res = String.valueOf(response.code());
                if (res.equals("200")) {
                    Toast.makeText(getBaseContext(), result.toString(), Toast.LENGTH_SHORT).show();
                    weather = response.body().getresult();

                    new DBHelper(weather, MainActivity.this);

                    // mAdapter = ;
                  /*  RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    voice_recyler.setLayoutManager(mLayoutManager);
                    voice_recyler.setItemAnimator(new DefaultItemAnimator());
                    voice_recyler.setAdapter(new VoiceAdapter(weather, MainActivity.this));
*/                 // clearPreferences();
                    looprunning();

                    //weather_recyler.setAdapter(new WeatherAdapter(weather, R.layout.weather_single, getApplicationContext()));*/

                } else {
                    Toast.makeText(getBaseContext(), "Server Not Found ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable throwable) {
                Toast.makeText(getBaseContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
                if (isNetworkAvailable()){
                   // clearPreferences();
                    IOTData();
                }else {
                  //  clearPreferences();
                    looprunning();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear YOUR_APP_PACKAGE_GOES HERE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
