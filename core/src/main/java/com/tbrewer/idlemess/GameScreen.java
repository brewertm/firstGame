package com.tbrewer.idlemess;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    final Drop game;

    private final Texture backgroundTexture;
    private final Texture bucketTexture;
    private final Texture dropTexture;
    private final Sound dropSound;
    private final Music music;
    private final Sprite bucketSprite;
    private final Vector2 touchPos;
    private final Array<Sprite> dropSprites;
    private float dropTimer;
    private final Rectangle bucketRectangle;
    private final Rectangle dropRectangle;
    private int dropsGathered;

    public GameScreen(final Drop game){
        this.game = game;

        backgroundTexture = new Texture("images/background.png");
        bucketTexture = new Texture("images/bucket.png");
        dropTexture = new Texture("images/drop.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);

        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1,1);
        bucketSprite.rotate90(true);

        touchPos = new Vector2();

        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        dropSprites = new Array<>();

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
            bucketSprite.translateY(speed * delta); // Move the bucket right
        }else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            bucketSprite.translateY(-speed * delta);
        }

        if(Gdx.input.isTouched()){
            // If the user has clicked of tapped the screen
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on screen
            game.viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            bucketSprite.setCenterY(touchPos.y); // Change the Vertical centered position of the bucket
        }
    }

    private void logic(){
        // Store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Store the bucket size for brevity
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // Clamp x to values between 0 and worldWidth
        bucketSprite.setY(MathUtils.clamp(bucketSprite.getY(), 0, worldHeight - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        // Apply the bucket position and size to the bucketRectangle
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        // Loop through the sprites backwards to prevent out of bounds errors
        for(int i = dropSprites.size -1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i); // Get the sprite from the list
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateX(-2f * delta);

            //Apply the drop position and size to the dropRectangle
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            // if the top of the drop goes below the bottom of the view, remove it
            if (dropSprite.getY() < -dropHeight) {
                dropSprites.removeIndex(i);
            }else if(bucketRectangle.overlaps(dropRectangle)) {
                dropsGathered++;
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer += delta;     // Adds the current delta to the timer
        if(dropTimer > 1f) {    // Check if it has been more than a second
            dropTimer = 0;      // Reset the timer
            createDroplet();    // Create the droplet
        }
    }

    private void createDroplet(){
        // create local variables for convenience
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // create the drop sprite
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(worldWidth); // randomize the drop's x position
        dropSprite.setY(MathUtils.random(0f, worldHeight - dropHeight));
        dropSprite.rotate90(true);
        dropSprites.add(dropSprite);
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
        bucketSprite.draw(game.batch); // Sprites have their own draw method

        game.font.draw(game.batch, "Drops collected: " + dropsGathered, 0, worldHeight);

        // draw each sprite
        for( Sprite dropSprite : dropSprites) {
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
        dropTexture.dispose();
        bucketTexture.dispose();
    }
}
