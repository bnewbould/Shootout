package com.example.jc210391.shootout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public class Sprite {
    private Bitmap sprite;
    private int width, height;
    private float x, y;
    private float dx, dy;

    Sprite(Bitmap bmp){
        this(bmp, 0, 0, 0, 0);
    }

    Sprite(Bitmap bmp, float x, float y){
        this(bmp, x, y, 0 ,0);
    }

    private Sprite(Bitmap bmp, float x, float y, float dx, float dy){
        this.sprite = bmp;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
    }

    void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    void setPosition(PointF pos){
        this.x = pos.x;
        this.y = pos.y;
    }

    PointF getPosition(){
        return new PointF(x,y);
    }

    Bitmap getSprite() {
        return sprite;
    }

    int getHeight() {
        return height;
    }

    int getWidth() {
        return width;
    }

    Rect getHitbox(){
        return new Rect((int) x, (int) y, (int) x+width, (int) y+height);
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(sprite, x, y, null);
    }
}
