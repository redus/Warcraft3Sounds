package com.redus.warcraft3sounds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UnitActivity extends ActionBarActivity {

    private static final int MAX_CONCURRENT_SOUNDS = 8;
    private static final int REQUEST_SELECT_CONTACT = 1;
    private static final float VOLUME_DEFAULT = 0.9f;
    private static final String TAG = "UnitActivity";
    private SoundPool soundPool;
    private Sound[] sounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int unitId = intent.getIntExtra(MainActivity.EXTRA_UNIT_KEY, 0);
        setContentView(R.layout.activity_unit);

        //text
        UnitDatabase unitDatabase = UnitDatabase.getInstance();
        setTitle(unitDatabase.getName(unitId));
        //images
        ImageView unitImage = (ImageView) findViewById(R.id.unitImage);
        unitImage.setImageBitmap(unitDatabase.getUnitImage(unitId));
        ImageButton abilityButton = (ImageButton) findViewById(R.id.abilityButton);
        abilityButton.setImageBitmap(unitDatabase.getAbilityImage(unitId));

        //sounds
        parseSounds(unitDatabase.getSounds(unitId));
        setSoundPool();
        ListView soundListView = (ListView) findViewById(R.id.soundListView);
        soundListView.setAdapter(new ArrayAdapter<>(this, R.layout.sound_layout, sounds));

        soundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                soundPool.load(getApplicationContext(), sounds[position].getSource(), 1);
            }
        });

        soundListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dialogOptions(position);
                return true;
            }
        });

        // random sound when ability button is clicked
        abilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) (Math.random() * sounds.length);
                soundPool.load(getApplicationContext(), sounds[position].getSource(), 1);
            }
        });
    }

    // REQ: each soundList elt excludes .mp3 ending
    private void parseSounds(String[] soundList){
        sounds = new Sound[soundList.length];
        for (int i = 0; i < soundList.length; i++){
            sounds[i] = new Sound(this, getResources().getIdentifier(
                    soundList[i], "raw", getPackageName()));
        }
    }

    private void setSoundPool(){
        soundPool = new SoundPool(MAX_CONCURRENT_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(final SoundPool soundPool, final int sampleId, int status) {
                final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                AudioManager.OnAudioFocusChangeListener afChangeListener =
                        new AudioManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int focusChange) {
                                if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                                    soundPool.stop(sampleId);
                                    am.abandonAudioFocus(this);
                                }
                            }
                        };

                if (status == 0) {
                    int focus = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

                    if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        soundPool.play(sampleId, VOLUME_DEFAULT, VOLUME_DEFAULT, 1, 0, 1);
                        am.abandonAudioFocus(afChangeListener);
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not gain audio focus.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Loading sound failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // EFF: show dialog option when item was long clicked;
    private void dialogOptions(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        AlertDialog dialog = builder.setItems(R.array.dialog_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            Context c = getApplicationContext();

            switch(which){
                case 0:
                    exportSound(position, c);
                    break;
                case 1:
                    setContactRingtone(position);
                    break;
                default:
                    Toast.makeText(c, "Invalid Option", Toast.LENGTH_LONG).show();
            }
            }
        }).create();
        dialog.show();
    }

    // REQ: file format is mp3
    // EFF: export selected sound to notification sound folder (sd)
    private void exportSound(int position, Context c){
        Log.d(TAG, "Exporting sound file to sdcard notifications folder.");
        String format = ".mp3";

        // check if mounted
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            Log.w(TAG, "Cannot write to sdcard.");
            Toast.makeText(c, "Error writing to sdcard", Toast.LENGTH_LONG).show();
            return;
        } else if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.w(TAG, "Sdcard unavailable.");
            Toast.makeText(c, "Sdcard unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            int resId = sounds[position].getSource();
            InputStream in = getResources().openRawResource(resId);
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_NOTIFICATIONS);
            path.mkdirs();

            File file = new File(path, getResources().getResourceEntryName(resId) + format);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();

            Toast.makeText(c, "Done!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not export file to SD card.");
            Toast.makeText(c, "Error exporting the file", Toast.LENGTH_LONG).show();
        }
    }

    // EFF: open contact app for selecting contact, and
    //     set the contact's ringtone to the selected sound
    private void setContactRingtone(int position) {
        Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
        }
    }

            @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unit, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
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


}
