package com.lavanya.bobble_drivemode;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;


import com.lavanya.bobble_drivemode.R;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordingAudio extends Activity {
    String name, response;
    private String outputFile;
    private TextView textClock, text;
    MediaRecorder myAudioRecorder;
    public static boolean ACTION_SEND = false;
    public static final int RECORD_AUDIO_REQUEST_CODE = 0;

    public static final String TAG = "RecordingAudio_Activity";

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_audio);
        getSpeechInput();
        registerReceiver(close, new IntentFilter("kill"));
        textClock = findViewById(R.id.textClock);
        text = findViewById(R.id.text);
        //textClock.setSelected(true);
        /*response="yes";
        if (response.trim().contains("yes")){
            getPermission();
        }*/
    }

    private void recordAudio() {


        Log.e(TAG, "IN recordAudio: ----");

        String file_path = getApplicationContext().getFilesDir().getPath();

        File file = new File(file_path);
        final long t = System.currentTimeMillis();
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {


                String ms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - t) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - t) % TimeUnit.MINUTES.toSeconds(1));


                Log.e(TAG, "run: recording time -----" + ms);


                textClock.setText(ms);
//                handler.postDelayed(this, 1000);
            }
        };

//Start
        handler.postDelayed(runnable, 1000);    //was 100
        Long date = new Date().getTime();
        Date current_time = new Date(Long.valueOf(date));
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.mp3";
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT);
        myAudioRecorder.setOutputFile(outputFile);
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();

            Log.e(TAG, "recordAudio: started");
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
        }


        final Handler h = new Handler();

        Runnable r = new Runnable() {

            public void run() {



                try{
                    myAudioRecorder.stop();

                    Log.e(TAG, "run:--- AudioRecorderStop");
                    myAudioRecorder.release();
                    myAudioRecorder = null;
                    Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
                    sendIntentToWhatsApp(outputFile);

                }catch (Exception e)
                {

                    e.printStackTrace();
                    Log.e(TAG, "run:--- exception caught in catch " );
                }


            }
        };

        h.postDelayed(r, 10000);


///**
// * Runs the specified action on the UI thread. If the current thread is the UI
// * thread, then the action is executed immediately. If the current thread is
// * not the UI thread, the action is posted to the event queue of the UI thread.
// *
// * @param action the action to run on the UI thread
// */
//        public final void runOnUiThread(Runnable action) {
//            if (Thread.currentThread() != mUiThread) {
//                mHandler.post(action);
//            } else {
//                action.run();
//            }
//        }
//

    }

    public void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {

            Log.e(TAG, "getSpeechInput: ---");
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult: " + requestCode);
        switch (requestCode) {
            case 10:

                getPermission();
                recordAudio();
//                if (resultCode == RESULT_OK && data != null) {
//                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    //text.setText(result.get(0));
//                    text.setText("Recording Message...");
//                    for (int i = 0; i < result.size(); i++) {
//                        response = result.get(0);
//                        if (response.trim().equalsIgnoreCase("yes")) {
//
//
////                            recordAudio();
//
//
//                            //getPermission();
//                        }
//                    }
//                }
        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            RECORD_AUDIO_REQUEST_CODE);
                    recordAudio();
                    //sendIntentToWhatsApp("/storage/sdcard0/QieZi/audios/01 - Dheere Dheere - DownloadMing.SE.mp3");
                }
            } else {
                recordAudio();
                //sendIntentToWhatsApp("/storage/sdcard0/QieZi/audios/01 - Dheere Dheere - DownloadMing.SE.mp3");
            }

        }

    }

    private void sendIntentToWhatsApp(String file_name) {

        Uri uri = Uri.parse(file_name);
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/.mp3");
            share.setPackage("com.whatsapp");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share"));
            ACTION_SEND = true;

            Log.e(TAG, "sendIntentToWhatsApp: done");
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String num) {
        if (num != null) {
            num = num.replace("+", "").replace(" ", "");
            PackageManager packageManager = getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);

            try {
                String url = "https://api.whatsapp.com/send?phone=" + num + "&text=" + URLEncoder.encode("Hey", "UTF-8");
                i.setPackage("com.whatsapp");
                i.setData(Uri.parse(url));
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                Toast.makeText(this, "Until you grant the permission, we canot play audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final BroadcastReceiver close = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(RecordingAudio.this).unregisterReceiver(close);


    }
}
