package com.example.mobiledevergasia;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

/**
 * TODO
 */
public class VoiceRecordActivity extends AppCompatActivity implements SaveDialog.SaveDialogListener {


    private ImageButton recordButton,stopPlayingButton;
    private File folder,temporaryFile,finalFile;

    private CustomListHandler customListHandler;
    private Recorder recorder;

    private String filename;

    private Chronometer chronometer;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);
        myToolbar=findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        //αρικοποιηση
        recordButton =findViewById(R.id.recordButton);
        stopPlayingButton=findViewById(R.id.stopPlayingButton);

        chronometer=findViewById(R.id.simpleChronometer);

        recorder=new Recorder();

        recordButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               buttonClick();
           }
        });

        stopPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customListHandler.stop();
                stopPlayingButton.setVisibility(View.GONE);
            }
        });

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                if (chronometer.getText().toString().endsWith("15")){//το endswith("15") σημαινει
                    stopClock();                                     //οταν το χρονομετρο γραψει "00:15" θα σταματησει η ηχογραφηση
                    //stopRecording();
                    stopRecording();
                }

            }
        });

        folder=new File(getExternalFilesDir(null) + "/MyRecording/");
        if (!folder.exists()){
            folder.mkdir(); //αν δεν υπαρχει το δημιουργει
        }

        //κλαση που χειριζεται τα αντικειμενα της λιστας με της ηχογραφησεις
        customListHandler = new CustomListHandler(findViewById(R.id.voice_record_activity),getApplicationContext());
        customListHandler.setCustomListListener(new CustomListHandler.CustomListListener() {
            @Override
            public void onStartPlaying() {
                showStopButton();
            }

            @Override
            public void onStopPlaying() {
                if(!customListHandler.areItemsPlaying()){
                    hideStopButton();

                }
            }
        });

    }

    /**
     * Την πρωτη φορα που θα πατηθει το κουμπι ξεκιναει η ηχογραφηση
     * Την δευτερη σταματαει και αποθηκευεται.
     */
    private void buttonClick(){
        if (!recorder.isRecording()){
            recordButton.setImageResource(R.drawable.stop_recording_image);
            startClock();
            startRecording();
        }else{
            recordButton.setImageResource(R.drawable.start_recording_image);

            stopClock();
            stopRecording();
        }

    }

    /**
     * Ξεκιναει το χρονομετρο
     */
    private void startClock(){
        chronometer.setBase(SystemClock.elapsedRealtime());

        chronometer.start();
    }

    /**
     * Σταματαει το χρονομετρο
     */
    private void stopClock(){
        chronometer.setText("00:00");

        chronometer.stop();
    }

    /**
     * Ξεκιναει η ηχογραφηση
     * Οταν ξεκινησει η ηχογραφηση θελουμε να σταματησουν
     * να παιζουν τυχον αλλες ηχογραφησεις
     * Δινεται ενα dummy ονομα στο αρχειο το οποιο χρησιμοποιειται για την αποθηκευση
     * Στην συνεχεια αυτο μπορει να αλλαξει απο τον χρηστη
     * Χρησιμοποιειται η startRecording(File temporaryFile) του Recorder
     */
    private void startRecording(){
        customListHandler.stop();

        Toast.makeText(VoiceRecordActivity.this,R.string.started_recording, Toast.LENGTH_SHORT).show();

        String dummy = "dummy";
        filename=folder+ "/" + dummy + ".mp3"; //δινεται ενα dummy μοναδικο ονομα το οποιο στην συνεχεια αλλαζει.

        temporaryFile=new File(filename );

        recorder.startRecording(temporaryFile);
    }

    /**
     * Σταματαει η ηχογραφηση και καλειται η showSaveDialog για να δωσει ο χρηστης
     * το ονομα που επιθυμει ή να ακυρωσει την αποθηκευση της
     * Χρησιμοποιειται η stopRecording() του Recorder
     */
    private void stopRecording(){
        Toast.makeText(VoiceRecordActivity.this, R.string.stopped_recording , Toast.LENGTH_SHORT).show();

        recorder.stopRecording();

        showSaveDialog();
    }

    /**
     * Αναδρομικη συναρτηση που ελεγχει αν το αρχειο υπαρχει ηδη
     * αν υπαρχει προσθετει το (1) και ξανα ελεγχει κοκ
     * @param oldFile το αρχειο το οποιο ελεγχουμε
     * @param found ποσες φορες εχει βρεθει αρχειο με το ιδιο ονομα,χρησιμοποιειται στην δημιουργια του καινουριου
     * @return επιστρεφει το αρχειο.
     */
    private  File check(File oldFile, int found){
        File newFile=new File(oldFile.getAbsolutePath());

        if (oldFile.exists()){
            String temporaryName=oldFile.getAbsolutePath();

            if (found==0){
                temporaryName=temporaryName.replaceFirst("\\.mp3","(1).mp3");

            }else{
                temporaryName= temporaryName.replaceFirst("(\\(\\d\\))","(" + (found+1) + ")");
                //αν πχ εχουμε το αρχειο με ονομα myRecording(2),αντικαθιστα το (2) με το (3)
            }
            newFile=new File(temporaryName);
            newFile=check(newFile,++found);
        }else{
            return newFile;
        }
        return newFile;
    }

    /**
     * Εμφανιζει το dialog για να δωθει ονομα στην ηχογραφηση
     */
    private void showSaveDialog(){
        SaveDialog saveDialog=new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "saveDialog");
    }

    /**
     * Αν πατηθει το cancel στο SaveDialog ακυρωνεται η ηχογραφηση
     */
    @Override
    public void cancelled() {
        temporaryFile.delete();
    }

    /**
     * Ελεγχεται αν ειναι ενεργοποιημενη η επιλογη πολλαπλων στοιχειων
     *      Αν ειναι καλειται η backPressed() του CustomListHandler
     */
    @Override
    public void onBackPressed() {
        if(customListHandler.isToCheck()){
            customListHandler.backPressed();
        }else{
            super.onBackPressed();
        }
    }

    /**
     * Αλλαζει το ονομα της ηχογραφησης στο επιθυμητο
     * και το προσθετει στο GridView
     * @param name το επιθυμητο ονομα
     */
    @Override
    public void saveFileAs(String name) {
        finalFile=new File(folder + "/" + name + ".mp3");
        finalFile=check(finalFile,0);
        temporaryFile.renameTo(finalFile);
        customListHandler.addToList(finalFile.getPath(),name); //προσθηκη ηχογραφησης στην λιστα αν αλλαξει το name

    }


    /**
     * Οταν σταματαει η εφαρμογη,πχ παει στο background,ελευθερονεται ο mediaRecorder
     * μεσω της recorded.clear() και ο mediaPlayer μεσω της
     * customListHandler.clear() για να μην σπαταλιζονται ποροι
     */
    @Override
    public void onStop() {
        super.onStop();
        recorder.clear();
        customListHandler.stop();

    }

    private void hideStopButton(){
        stopPlayingButton.setVisibility(View.GONE);
    }

    private void showStopButton() {
        stopPlayingButton.setVisibility(View.VISIBLE);
    }
}