package com.example.jc210391.shootout;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

public class Settings extends Scene {
    private TextPaint paint, stroke;
    private String text;
    private int titleSize = 46;

    Settings(Context context, int sprites, State state) {
        super(sprites, state);
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(TextPaint.Align.CENTER);
        paint.setTextSize(titleSize);
        stroke = new TextPaint(paint);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(Color.BLACK);
        stroke.setStrokeWidth(2f);
        text = context.getResources().getString(R.string.diff);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect clipBounds = canvas.getClipBounds();
        canvas.drawText(text, clipBounds.right/2f,clipBounds.bottom/3f,stroke);
        canvas.drawText(text, clipBounds.right/2f,clipBounds.bottom/3f,paint);
    }
}
