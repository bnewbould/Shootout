package com.example.jc210391.shootout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

public class Chara extends Sprite {
    private Bitmap shootSprite;
    private boolean shooting = false;

    private Chara(Bitmap bmp) {
        super(bmp);
    }

    Chara(Bitmap base, Bitmap alternate){
        this(base);
        this.shootSprite = alternate;
    }

    private Bitmap getStandSprite(){
        return getSprite();
    }

    private Bitmap getShootSprite() {
        return shootSprite;
    }

    void toggleShooting(){
        shooting ^= true;
    }

    @Override
    public void draw(Canvas canvas) {
        PointF pos = getPosition();
        canvas.drawBitmap(shooting ? getShootSprite() : getStandSprite(), pos.x, pos.y, null);
    }
}
