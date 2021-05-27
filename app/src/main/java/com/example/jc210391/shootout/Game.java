package com.example.jc210391.shootout;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.graphics.Rect;

public class Game extends Scene {

    GameState state = GameState.STANDOFF;
    private Difficulty difficulty;
    private int bullets = 6;
    private Bitmap bullet;
    private TextPaint paint, scorePaint;
    private Paint flashPaint;
    private String question = "", answer = "", scoreDisplay = "";
    private boolean flash = false;
    private int questionSize = 24, answerSize = 16, scoreSize = 12;

    private int score = 0;

    private Game(int sprites, State state) {
        super(sprites, state);
        this.difficulty = Difficulty.NORMAL;

        this.paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(TextPaint.Align.CENTER);

        this.scorePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setTextSize(scoreSize);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextAlign(Paint.Align.LEFT);

        this.flashPaint = new Paint();
        flashPaint.setColor(Color.WHITE);
    }

    private Game(int sprites, State state, Difficulty diffLevel) {
        this(sprites, state);
        this.difficulty = diffLevel;
        if(diffLevel == Difficulty.EASY){
            bullets = 9;
        }
        if(diffLevel == Difficulty.HARD){
            bullets = 3;
        }
    }

    Game(int sprites, State state, Difficulty diffLevel, String scoreDisplay){
        this(sprites,state,diffLevel);
        this.scoreDisplay = scoreDisplay;
    }

    void getBulletImage(Bitmap bullet){
        this.bullet = bullet;
    }

    void loseLife(){
        if(bullets-1 <= 0){
            bullets = 0;
        } else {
            bullets--;
        }
    }

    void setQuestion(String question) {
        this.question = question;
    }

    void setAnswer(String answer) {
        this.answer = answer;
    }

    void setFlash(boolean flash) {
        this.flash = flash;
    }

    boolean getGameOver(){
        return bullets <= 0;
    }

    void addScore(int add){
        if(difficulty == Difficulty.EASY){
            add *= 0.5;
        }
        if(difficulty == Difficulty.HARD){
            add*=3;
        }
        this.score += add;
    }

    int getScore() {
        return score;
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        Rect canvasBounds = canvas.getClipBounds();

        if(question.length() > 1){
            paint.setTextSize(questionSize);
            StaticLayout questionLayout = new StaticLayout(question, paint, (int) (canvasBounds.right*2/3), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            canvas.save();
            canvas.translate(canvasBounds.right * 2f/3f, canvasBounds.bottom / 3f);
            questionLayout.draw(canvas);
            canvas.restore();
        }
        if(answer.length() > 1){
            paint.setTextSize(answerSize);
            StaticLayout answerLayout = new StaticLayout(answer, paint, (int) (canvasBounds.right*2/3), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            canvas.save();
            canvas.translate(canvasBounds.right * 2f/3f, canvasBounds.bottom * 3f/7f);
            answerLayout.draw(canvas);
            canvas.restore();
        }

        //draw score display
        canvas.drawText(scoreDisplay + score, 2, 2+scorePaint.getTextSize(), scorePaint);

        //draw bullets remaining
        int xOffset, yOffset;
        xOffset = bullet.getWidth()+2;
        yOffset = bullet.getHeight()+2;
        int x, y;
        x = canvasBounds.right - xOffset;
        y = canvasBounds.bottom - yOffset;

        for(int i = 0; i < bullets; i++){
            canvas.drawBitmap(bullet, x-(xOffset/2f*i), y, null);
        }
        //draw gunshot flash
        if(flash){
            canvas.drawRect(canvasBounds, flashPaint);
        }
    }
}
