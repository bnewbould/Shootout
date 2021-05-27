package com.example.jc210391.shootout;

import android.graphics.Canvas;

/*
A scene is a collection of objects that represents one screen.
Scenes are really nothing more than a fancy wrapper for Sprite arrays.
*/
public class Scene {

    private Sprite background;
    private Sprite[] sprites;
    private State parentState;

    Scene(int sprites, State state){
        this.sprites = new Sprite[sprites];
        parentState = state;
    }

    void addBackground(Sprite reference){
        background = reference;
    }

    void addSprite(Sprite reference){
        int open = getFreeSpot();
        if(open >= sprites.length){
            //bad times ahead friend
            return;
        }
        sprites[open] = reference;
    }

    Sprite getSprite(int index){
        if(index < sprites.length){
            if(sprites[index] != null){
                return sprites[index];
            }
        }
        return null;
    }

    public void draw(Canvas canvas){
        background.draw(canvas);
        for (Sprite sprite : sprites) {
            if (sprite != null) {
                sprite.draw(canvas);
            }
        }
    }

    State getParentState() {
        return parentState;
    }

    private int getFreeSpot(){
        int open = sprites.length;
        for(int i = 0; i < sprites.length; i++){
            if(sprites[i] == null){
                open = i;
                break;
            }
        }
        return open;
    }


}
