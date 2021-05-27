package com.example.jc210391.shootout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Display extends View {

    private Scene currentScene = null;
    private Paint paint;

    private final float ART_WIDTH = 640f;
    private final float ART_HEIGHT = 360f;
    private float width, height;
    private float scale = 1;
    private float xOffset = 0, yOffset = 0;

    public Display(Context context) {
        this(context, null);
    }

    public Display(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        paint = new Paint();
        paint.setAntiAlias(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        scale = setScale();
        super.onSizeChanged(w, h, oldw, oldh);
    }




    public PointF getGraphicalCenter(){
        return new PointF(ART_WIDTH/2, ART_HEIGHT/2);
    }

    public float getNthHeight(int nth){
        return nth != 0 ? ART_HEIGHT/nth : 0;
    }

    public float getNthWidth(int nth){
        return nth != 0 ? ART_WIDTH/nth : 0;
    }

    public float getScale() {
        return scale;
    }

    public float getRatio() {
        return height/width;
    }

    public float setScale() {
        float ratio = getRatio();
        float scl = 0;
        final float RESOLUTION_RATIO = ART_WIDTH / ART_HEIGHT;
        if(ratio == RESOLUTION_RATIO){
            scl = width/ART_WIDTH;
            xOffset = 0;
            yOffset = 0;
        } else if(ratio > RESOLUTION_RATIO) { //screen is wider
            scl = height/ART_HEIGHT;
            xOffset = (width/2) - (ART_WIDTH*scl/2);
            yOffset = 0;
        } else { //screen is taller
            scl = width/ART_WIDTH;
            xOffset = 0;
            yOffset = (height/2) - (ART_HEIGHT*scl/2);
        }
        return scl;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //the canvas of sadness
        //alternately, why I should've learnt how to use SurfaceView or OpenGL

        canvas.save();
        if(currentScene != null){
            canvas.translate(xOffset,yOffset);
            canvas.scale(scale,scale);
            currentScene.draw(canvas);
        }
        canvas.restore();
        //consider filling letterboxing areas with black if they exist
        //in order to correctly cut off sprites half way out
        if(xOffset > 0 || yOffset > 0){
            paint.setColor(Color.BLACK);
            if(xOffset > 0){
                canvas.drawRect(0,0,xOffset,ART_HEIGHT,paint);
                canvas.drawRect(ART_WIDTH,0,ART_WIDTH+xOffset,ART_HEIGHT,paint);
            }
            if(yOffset > 0){
                canvas.drawRect(0,0,ART_WIDTH, yOffset, paint);
                canvas.drawRect(0,ART_HEIGHT,ART_WIDTH, ART_HEIGHT+yOffset, paint);
            }
        }
    }
}
