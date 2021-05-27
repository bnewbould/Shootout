package com.example.jc210391.shootout;


import android.graphics.Canvas;

public class Title extends Scene {
    private Sprite settingsButton;

    Title(int sprites, State state) {
        super(sprites, state);
    }

    void setSettingsButton(Sprite settingsButton) {
        this.settingsButton = settingsButton;
    }

    Sprite getSettingsButton() {
        return settingsButton;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        settingsButton.draw(canvas);
    }
}
