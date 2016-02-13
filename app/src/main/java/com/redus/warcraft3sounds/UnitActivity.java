package com.redus.warcraft3sounds;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class UnitActivity extends ActionBarActivity {

    private static final int MAX_CONCURRENT_SOUNDS = 8;
    private static final float VOLUME_DEFAULT = 0.9f;
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
        ImageButton abilityButton = (ImageButton) findViewById(R.id.abilityButton);
        abilityButton.setImageBitmap(unitDatabase.getAbilityImage(unitId));
        ImageView unitImage = (ImageView) findViewById(R.id.unitImage);
        unitImage.setImageBitmap(unitDatabase.getUnitImage(unitId));

        //sounds
        parseSounds(unitDatabase.getSounds(unitId));
        setSoundPool();
        ListView soundListView = (ListView) findViewById(R.id.soundListView);
        soundListView.setAdapter(new ArrayAdapter<>(this, R.layout.sound_layout, sounds));

        soundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
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
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    soundPool.play(sampleId, VOLUME_DEFAULT, VOLUME_DEFAULT, 1, 0, 1);
                } else {
                    Toast.makeText(getApplicationContext(), "Loading sound failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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
