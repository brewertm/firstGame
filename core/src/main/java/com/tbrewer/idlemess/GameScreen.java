package com.tbrewer.idlemess;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    final Drop game;

    private final Texture backgroundTexture;
    private final Texture ship1Texture;
    private final Texture laser1Texture;
    private final Sound dropSound;
    private final Music music;
    private final Sprite ship1Sprite;
    private final Vector2 touchPos;
    private final Array<Sprite> laser1Sprites;
    private float dropTimer;
    private final Rectangle ship1Rectangle;
    private final Rectangle laserRectangle;
    private int dropsGathered;

    public GameScreen(final Drop game){
        this.game = game;

        backgroundTexture = new Texture("images/Background1.png");
        ship1Texture = new Texture("images/Ship1.png");
        laser1Texture = new Texture("images/laser1.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);

        ship1Sprite = new Sprite(ship1Texture);
        ship1Sprite.setSize(1,.5f);

        touchPos = new Vector2();

        ship1Rectangle = new Rectangle();
        laserRectangle = new Rectangle();

        laser1Sprites = new Array<>();

    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        music.play();

    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    private void input(){
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            ship1Sprite.translateY(speed * delta); // Move the bucket right
        }else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            ship1Sprite.translateY(-speed * delta);
        }

        if(Gdx.input.isTouched()){
            // If the user has clicked of tapped the screen
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on screen
            game.viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            ship1Sprite.setCenterY(touchPos.y); // Change the Vertical centered position of the bucket
        }
    }

    private void logic(){
        // Store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Store the bucket size for brevity
        float bucketWidth = ship1Sprite.getWidth();
        float bucketHeight = ship1Sprite.getHeight();

        // Clamp x to values between 0 and worldWidth
        ship1Sprite.setY(MathUtils.clamp(ship1Sprite.getY(), 0, worldHeight - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        // Apply the bucket position and size to the bucketRectangle
        ship1Rectangle.set(ship1Sprite.getX(), ship1Sprite.getY(), bucketWidth, bucketHeight);

        // Loop through the sprites backwards to prevent out of bounds errors
        for(int i = laser1Sprites.size -1; i >= 0; i--) {
            Sprite laserSprite = laser1Sprites.get(i); // Get the sprite from the list
            float laserWidth = laserSprite.getWidth();
            float laserHeight = laserSprite.getHeight();

            laserSprite.translateX(-2f * delta);

            //Apply the drop position and size to the dropRectangle
            laserRectangle.set(laserSprite.getX(), laserSprite.getY(), laserWidth, laserHeight);

            // if the top of the drop goes below the bottom of the view, remove it
            if (laserSprite.getY() < -laserHeight) {
                laser1Sprites.removeIndex(i);
            }else if(ship1Rectangle.overlaps(laserRectangle)) {
                dropsGathered++;
                laser1Sprites.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer += delta;     // Adds the current delta to the timer
        if(dropTimer > 1f) {    // Check if it has been more than a second
            dropTimer = 0;      // Reset the timer
            createLaser();    // Create the droplet
        }
    }

    private void createLaser(){
        // create local variables for convenience
        float dropWidth = 1;
        float dropHeight = .5f;
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // create the drop sprite
        Sprite laser1Sprite = new Sprite(laser1Texture);
        laser1Sprite.setSize(dropWidth, dropHeight);
        laser1Sprite.setX(worldWidth); // randomize the drop's x position
        laser1Sprite.setY(MathUtils.random(0f, worldHeight - dropHeight));
        laser1Sprites.add(laser1Sprite);
    }

    private void draw(){
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        // store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background
        //spriteBatch.draw(bucketTexture, 0, 0, 1, 1); // draw the bucket with width/height of 1 meter
        ship1Sprite.draw(game.batch); // Sprites have their own draw method

        game.font.draw(game.batch, "Number of lasers hit: " + dropsGathered, 0, worldHeight);

        // draw each sprite
        for( Sprite dropSprite : laser1Sprites) {
            dropSprite.draw(game.batch);
        }

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        dropSound.dispose();
        music.dispose();
        laser1Texture.dispose();
        ship1Texture.dispose();
    }
}
