package com.example.jc210391.shootout;

import android.content.Context;
import android.media.SoundPool;

class SoundManager {
    private SoundPool pool;
    private Context context;

    SoundManager(Context context){
        this.context = context;
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(10);
        pool = builder.build();
    }

    int addSound(int resource){
        return pool.load(context, resource, 1);
    }

    void play(int soundID){
        play(soundID, 1, 1, 1, false, 1);
    }

    private void play(int soundID, float left, float right, int priority, boolean loop, float rate){
        pool.play(soundID, left, right, priority, loop ? 1 : 0, rate);
    }
}
