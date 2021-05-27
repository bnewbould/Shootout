package com.example.jc210391.shootout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Display display;
    SoundManager soundManager;
    MediaPlayer mediaPlayer;
    boolean mediaResume = false;
    private SensorManager sensorManager;
    private Sensor sensor;
    SensorEventListener sensorListener;
    private Choreographer.FrameCallback frameCallback = null;
    private boolean callbackPending = false;

    int blip, shot, correct, wrong;
    State state = State.TITLE;

    Difficulty difficulty = Difficulty.NORMAL;

    int frameWaitForQuestion = 0, frameTimeToAnswer = 0, frameTimeToFlash = 0, frameTimeInterlude = 0;
    boolean setWait = false, setTime = false, correctAnswer = false, setFlash = false;

    int stateLife;

    int finalScore = 0;

    BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

    private static final float SHAKE_GFORCE = 2.7F;
    private static final int SHAKE_COOLDOWN = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.display);
        soundManager = new SoundManager(this);
        blip = soundManager.addSound(R.raw.blip_select);
        shot = soundManager.addSound(R.raw.shot);
        correct = soundManager.addSound(R.raw.correct);
        wrong = soundManager.addSound(R.raw.wrong);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mP) {
                mP.start();
            }
        });
        bmpOptions.inScaled = false;
        stateLife = 0;
        hideSystemUI();

        //shake listener
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorListener = new SensorEventListener() {
                float LAST_SHAKE = 0f;
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    float gX = x / SensorManager.GRAVITY_EARTH;
                    float gY = y / SensorManager.GRAVITY_EARTH;
                    float gZ = z / SensorManager.GRAVITY_EARTH;

                    float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                    if (gForce > SHAKE_GFORCE) {
                        final long now = System.currentTimeMillis();
                        if (LAST_SHAKE + SHAKE_COOLDOWN > now) {
                            return;
                        }
                        LAST_SHAKE = now;
                        doShake();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i){}
            };
        }

        ViewTreeObserver viewTree = display.getViewTreeObserver();
        viewTree.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(display.getCurrentScene() == null) {
                    display.setCurrentScene(createScene());
                }
            }
        });
        gameLoop();
    }

    protected Scene createScene() {
        Scene currentScene = display.getCurrentScene();
        Scene newScene = null;
        if(currentScene != null){
            if(state == currentScene.getParentState()) {
                return currentScene;
            }
        }
        switch (state){
            case TITLE:
                newScene = generateTitleScene();
                break;
            case TUTORIAL:
                newScene = generateTutorialScene();
                break;
            case GAME:
                newScene = generateGameScene();
                break;
            case SETTINGS:
                newScene = generateSettingsScene();
                break;
            case GAME_OVER:
                newScene = generateGameOverScene();
                break;
        }
        return newScene;
    }

    protected Scene generateTitleScene(){
        Title scene = new Title(2, state);
        Bitmap titleBmp = BitmapFactory.decodeResource(getResources(),R.drawable.title_screen, bmpOptions);
        Bitmap cardBmp = BitmapFactory.decodeResource(getResources(),R.drawable.title_card, bmpOptions);
        Bitmap settingsBmp = BitmapFactory.decodeResource(getResources(),R.drawable.settings, bmpOptions);
        Bitmap exitBmp = BitmapFactory.decodeResource(getResources(),R.drawable.back, bmpOptions);
        Sprite background = new Sprite(titleBmp);
        Sprite titleCard = new Sprite(cardBmp);
        Sprite settingsButton = new Sprite(settingsBmp);
        Sprite exitButton = new Sprite(exitBmp);

        PointF center = display.getGraphicalCenter();
        float offsetX, offsetY;
        offsetX = titleCard.getWidth()/2f;
        offsetY = titleCard.getHeight()/2f;
        titleCard.setPosition(center.x-offsetX, center.y-offsetY);
        settingsButton.setPosition(display.getNthWidth(1)-(settingsButton.getWidth()+2), 2);
        exitButton.setPosition(2, display.getNthHeight(1)-(exitButton.getHeight()+2));

        if(!mediaPlayer.isPlaying()) {
            Uri title_screen = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.title_screen);
            try {
                mediaPlayer.setDataSource(this, title_screen);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scene.addBackground(background);
        scene.addSprite(titleCard);
        scene.addSprite(exitButton);
        scene.setSettingsButton(settingsButton);
        return scene;
    }

    protected Scene generateGameScene(){
        Game scene = new Game(5, state, difficulty,getString(R.string.score));
        Bitmap bgBmp = BitmapFactory.decodeResource(getResources(),R.drawable.game_background, bmpOptions);
        Bitmap charBmp = BitmapFactory.decodeResource(getResources(),R.drawable.char_stand, bmpOptions);
        Bitmap shootBmp = BitmapFactory.decodeResource(getResources(),R.drawable.char_shoot, bmpOptions);
        Bitmap bulletBmp = BitmapFactory.decodeResource(getResources(),R.drawable.bullet, bmpOptions);
        Sprite background = new Sprite(bgBmp);
        Chara character = new Chara(charBmp, shootBmp);

        PointF center = new PointF(display.getNthWidth(3),display.getNthHeight(3)*2);
        float offsetX, offsetY;
        offsetX = character.getWidth()/2f;
        offsetY = character.getHeight()/2f;
        character.setPosition(center.x-offsetX, center.y-offsetY);

        if(mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }

        scene.addBackground(background);
        scene.addSprite(character);
        scene.getBulletImage(bulletBmp);
        return scene;
    }

    protected Scene generateSettingsScene(){
        Settings scene = new Settings(this, 5, state);
        Bitmap titleBmp = BitmapFactory.decodeResource(getResources(),R.drawable.title_screen, bmpOptions);
        Bitmap lightBmp = BitmapFactory.decodeResource(getResources(),R.drawable.light, bmpOptions);
        Bitmap easyBmp = BitmapFactory.decodeResource(getResources(),R.drawable.easy, bmpOptions);
        Bitmap mediumBmp = BitmapFactory.decodeResource(getResources(),R.drawable.medium, bmpOptions);
        Bitmap hardBmp = BitmapFactory.decodeResource(getResources(),R.drawable.hard, bmpOptions);
        Bitmap backBmp = BitmapFactory.decodeResource(getResources(),R.drawable.back, bmpOptions);
        Sprite background = new Sprite(titleBmp);
        Sprite light = new Sprite(lightBmp);
        Sprite easy = new Sprite(easyBmp);
        Sprite medium = new Sprite(mediumBmp);
        Sprite hard = new Sprite(hardBmp);
        Sprite back = new Sprite(backBmp);

        PointF center = display.getGraphicalCenter();
        float offsetX, offsetY;
        offsetX = easy.getWidth()/2f;
        offsetY = easy.getHeight()/2f; //all three difficulties are the same size

        float xPos = display.getNthWidth(4);

        easy.setPosition(xPos-offsetX, center.y-offsetY);
        medium.setPosition(xPos*2-offsetX, center.y-offsetY);
        hard.setPosition(xPos*3-offsetX, center.y-offsetY);

        Sprite getPos;
        switch(difficulty){
            case EASY:
                getPos = easy;
                break;
            case NORMAL:
                getPos = medium;
                break;
            case HARD:
                getPos = hard;
                break;
            default:
                getPos = medium;
        }

        PointF backLight = getPos.getPosition();
        backLight.offset(-2,-2);
        light.setPosition(backLight);

        offsetX = 2;
        offsetY = back.getHeight()+2;

        back.setPosition(offsetX, display.getNthHeight(1)-offsetY);

        scene.addBackground(background);
        scene.addSprite(light);
        scene.addSprite(easy);
        scene.addSprite(medium);
        scene.addSprite(hard);
        scene.addSprite(back);
        return scene;
    }

    public Scene generateGameOverScene(){
        DatabaseManager dbManager = new DatabaseManager(this);
        GameOver scene = new GameOver(this,1, state,finalScore,dbManager);
        Bitmap titleBmp = BitmapFactory.decodeResource(getResources(),R.drawable.gameover_screen, bmpOptions);
        Sprite background = new Sprite(titleBmp);

        if(!mediaPlayer.isPlaying()) {
            Uri game_over = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sting);
            try {
                mediaPlayer.setDataSource(this, game_over);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scene.addBackground(background);
        return scene;
    }

    public Scene generateTutorialScene(){
        Tutorial scene = new Tutorial(this,1, state);
        Bitmap bgBmp = BitmapFactory.decodeResource(getResources(),R.drawable.game_background, bmpOptions);
        Sprite background = new Sprite(bgBmp);

        scene.addBackground(background);

        return scene;
    }


    public void gameLoop() { //do game logic, call drawing
        if (!callbackPending) {
            callbackPending = true;
            if (frameCallback == null) {
                frameCallback = new Choreographer.FrameCallback() {
                    @Override
                    public void doFrame(long frameTimeNanos) {
                        callbackPending = false;
                        if (display != null) {
                            Scene currentScene = display.getCurrentScene();
                            if (currentScene != null) {
                                gameLogic(currentScene);
                                if(state != currentScene.getParentState()){
                                    display.setCurrentScene(createScene());
                                }
                            }
                            stateLife++;
                            display.invalidate();
                            gameLoop();
                        }
                    }
                };
            }
            Choreographer.getInstance().postFrameCallback(frameCallback);
        }
    }

    public void gameLogic(Scene scene){
        State state = scene.getParentState();
        switch(state){
            case TITLE:
                break;
            case GAME:
                Game game = (Game) scene;
                switch (game.state){
                    case STANDOFF:
                        if(game.getGameOver()){
                            changeState(State.GAME_OVER);
                            finalScore = game.getScore();
                            break;
                        }
                        if(!setWait){
                            Random rand = new Random();
                            frameWaitForQuestion = stateLife + rand.nextInt(5*60)+31; //make it wait at least 30 frames between questions
                            setWait = true;
                        }
                        if(stateLife >= frameWaitForQuestion){
                            setWait = false;
                            Random rand = new Random();
                            int question, answer;
                            question = rand.nextInt(3)+1;
                            String[] questionArray = getResources().getStringArray(getResources().getIdentifier("question"+question, "array", this.getPackageName()));
                            game.setQuestion(questionArray[0]);
                            answer = rand.nextInt(3)+1;
                            game.setAnswer(questionArray[answer]);
                            if(answer != 1){
                                game.state = GameState.FAKEOUT;
                                frameWaitForQuestion = 0;
                            } else {
                                game.state = GameState.SHOOTOUT;
                                frameWaitForQuestion = 0;
                            }
                        }

                        break;
                    case FAKEOUT:
                        if(!setTime){
                            frameTimeToAnswer = stateLife + (3*60); //roughly 3 seconds to answer
                            setTime = true;
                        }
                        if(stateLife >= frameTimeToAnswer){
                            setTime = false;
                            game.setQuestion("");
                            game.setAnswer("");
                            game.state = GameState.STANDOFF;
                            frameTimeToAnswer = 0;
                        }
                        break;
                    case SHOOTOUT:
                        if(!setTime){
                            frameTimeToAnswer = stateLife + (3*60); //roughly 3 seconds to answer
                            setTime = true;
                        }
                        if(stateLife >= frameTimeToAnswer){
                            setTime = false;
                            game.setQuestion("");
                            game.setAnswer("");
                            game.loseLife();
                            game.state = GameState.STANDOFF;
                        }
                        break;
                    case POSTSHOT:
                        if(correctAnswer){
                            game.addScore((frameTimeToAnswer - stateLife) * 5);
                            correctAnswer = false;
                            frameTimeToAnswer = 0;
                        }
                        if(!setFlash){
                            setFlash = true;
                            Chara chara = (Chara) game.getSprite(0);
                            chara.toggleShooting();
                            frameTimeToFlash = stateLife + 2;
                            frameTimeInterlude = stateLife + 60;
                        }
                        if(stateLife <= frameTimeToFlash){
                            game.setFlash(true);
                        } else {
                            game.setFlash(false);
                        }
                        if(stateLife >= frameTimeInterlude){
                            frameTimeInterlude = 0;
                            setFlash = false;
                            Chara chara = (Chara) game.getSprite(0);
                            chara.toggleShooting();
                            game.state = GameState.STANDOFF;
                        }
                        break;
                }
                break;
            case SETTINGS:
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        showSystemUI();
        if(sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                mediaResume = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if(sensorManager != null) {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
        }
        if(mediaPlayer != null && mediaResume){
            mediaPlayer.start();
            mediaResume = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        showSystemUI();
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        float scale = display.getScale();
        x = (int) (x/scale);
        y = (int) (y/scale);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                doInput(x,y);
                break;
        }
        return false;
    }

    public void doInput(int x, int y){
        Scene scene = display.getCurrentScene();
        switch(state){
            case TITLE:
                Title title = (Title) scene;
                if(title.getSettingsButton() != null){
                    if(title.getSettingsButton().getHitbox().contains(x, y)){
                        soundManager.play(blip);
                        changeState(State.SETTINGS);
                    } else if(title.getSprite(1).getHitbox().contains(x,y)) {
                        finish();
                        moveTaskToBack(true);
                    } else {
                        changeState(State.TUTORIAL);
                    }
                }
                break;
            case TUTORIAL:
                changeState(State.GAME);
                break;
            case GAME:
                Game game = (Game) scene;
                doGameInput(game);
                break;
            case SETTINGS:
                //easy medium hard back
                Rect easyHitbox, mediumHitbox, hardHitbox, backHitbox;
                Sprite light = scene.getSprite(0);
                PointF backLight;
                easyHitbox = scene.getSprite(1).getHitbox();
                mediumHitbox = scene.getSprite(2).getHitbox();
                hardHitbox = scene.getSprite(3).getHitbox();
                backHitbox = scene.getSprite(4).getHitbox();

                if(easyHitbox.contains(x,y)){
                    difficulty = Difficulty.EASY;
                    soundManager.play(blip);
                    backLight = scene.getSprite(1).getPosition();
                    backLight.offset(-2,-2);
                    light.setPosition(backLight);
                }
                if(mediumHitbox.contains(x,y)){
                    difficulty = Difficulty.NORMAL;
                    soundManager.play(blip);
                    backLight = scene.getSprite(2).getPosition();
                    backLight.offset(-2,-2);
                    light.setPosition(backLight);
                }
                if(hardHitbox.contains(x,y)){
                    difficulty = Difficulty.HARD;
                    soundManager.play(blip);
                    backLight = scene.getSprite(3).getPosition();
                    backLight.offset(-2,-2);
                    light.setPosition(backLight);
                }
                if(backHitbox.contains(x,y)){
                    soundManager.play(blip);
                    changeState(State.TITLE);
                }
                break;
            case GAME_OVER:
                changeState(State.TITLE);
                mediaPlayer.reset();
        }
    }

    public void doShake(){
        Scene scene = display.getCurrentScene();
        switch(state){
            case TITLE:
                break;
            case TUTORIAL:
                soundManager.play(blip);
                changeState(State.TITLE);
                break;
            case GAME:
                Game game = (Game) scene;
                doGameInput(game);
                break;
            case SETTINGS:
                break;
            case GAME_OVER:
                break;
        }

    }

    public void doGameInput(Game game){
        switch(game.state){
            case STANDOFF:
                break;
            case SHOOTOUT:
                //good shot
                correctAnswer = true;
                soundManager.play(correct);
            case FAKEOUT:
                //bad shot
                setTime = false;
                game.setQuestion("");
                game.setAnswer("");
                game.loseLife();
                if(game.state == GameState.FAKEOUT){
                    soundManager.play(wrong);
                }
                soundManager.play(shot);
                game.state = GameState.POSTSHOT;
                break;
            case POSTSHOT:
                break;
        }
    }

    public void changeState(State state){
        this.state = state;
        this.stateLife = 0;
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
