package com.example.jc210391.shootout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

public class GameOver extends Scene {

    private int finalScore = 0;
    private Paint paint;
    private Context context;
    private int titleSize = 46;
    private int scoreSize = 24;
    private DatabaseManager dbManager;
    private SQLiteDatabase db;
    private int[] scores;

    GameOver(Context context, int sprites, State state, int finalScore, DatabaseManager dbManager) {
        super(sprites, state);
        this.context = context;
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(TextPaint.Align.CENTER);

        scores = new int[3];
        this.finalScore = finalScore;

        this.dbManager = dbManager;
        Cursor cursor = null;
        try{
            db = dbManager.getWritableDatabase();
            insertScore(finalScore);
            cursor = db.query("SCORES",new String[]{"SCORE"},null,null,null,null, "SCORE DESC", "3");
            int i = 0;
            while (cursor.moveToNext()){
                scores[i] = cursor.getInt(0);
                i++;
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null){
                cursor.close();
            }
            db.close();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect canvasBounds = canvas.getClipBounds();
        paint.setColor(Color.WHITE);

        paint.setTextSize(titleSize);
        canvas.drawText(context.getString(R.string.game_over), canvasBounds.right/2f, canvasBounds.bottom/2f,paint);

        paint.setTextSize(scoreSize);
        float scoreY = (canvasBounds.bottom/2f)+scoreSize*1.5f;
        canvas.drawText(context.getString(R.string.score) + finalScore, canvasBounds.right/2f, scoreY, paint);

        if(scores[0] != 0){
            canvas.drawText(context.getString(R.string.highscores), canvasBounds.right/2f, scoreY+scoreSize*2, paint);
            paint.setColor(Color.parseColor("#FFD700"));
            canvas.drawText(context.getString(R.string.first) + scores[0], canvasBounds.right/2f, scoreY+scoreSize*3, paint);
        }
        if(scores[1] != 0){
            paint.setColor(Color.parseColor("#C0C0C0"));
            canvas.drawText(context.getString(R.string.second) + scores[1], canvasBounds.right/2f, scoreY+scoreSize*4, paint);
        }
        if(scores[2] != 0){
            paint.setColor(Color.parseColor("#CD7F32"));
            canvas.drawText(context.getString(R.string.third) + scores[2], canvasBounds.right/2f, scoreY+scoreSize*5, paint);
        }


    }

    private void insertScore(int score){
        if(db != null && score != 0){
            ContentValues scoreEntry = new ContentValues();
            scoreEntry.put("SCORE", score);
            db.insert("SCORES", null, scoreEntry);
        }
    }
}
