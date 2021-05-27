package com.example.jc210391.shootout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;

public class Tutorial extends Scene {

    private TextPaint paint;
    private Context context;
    private int titleSize = 32;
    private int tutorialSize = 16;

    Tutorial(Context context, int sprites, State state) {
        super(sprites, state);
        this.context = context;
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(TextPaint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect canvasBounds = canvas.getClipBounds();

        paint.setTextSize(titleSize);
        canvas.drawText(context.getString(R.string.tutorial), canvasBounds.right / 2f, titleSize, paint);

        paint.setTextSize(tutorialSize);
        StaticLayout tutorialLayout = new StaticLayout(context.getString(R.string.tutorial_text), paint, (int) (canvasBounds.right*2/3), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        canvas.translate(canvasBounds.right / 2f, canvasBounds.bottom / 2f);
        tutorialLayout.draw(canvas);
        canvas.restore();
    }
}
