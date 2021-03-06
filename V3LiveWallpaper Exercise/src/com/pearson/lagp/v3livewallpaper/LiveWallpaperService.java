package com.pearson.lagp.v3livewallpaper;

import java.util.Arrays;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.BaseParticleEmitter;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.os.Handler;

public class LiveWallpaperService extends BaseLiveWallpaperService {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 320;
	private static final int CAMERA_HEIGHT = 480;
	private static final int VAMP_RATE = 2000;
	private static final int MAX_VAMPS = 10;
	// ===========================================================
	// Fields
	// ===========================================================
	
	private Handler mHandler;
	private Texture mScrumTexture;
	private Texture mBackgroundTexture;
	private TextureRegion mBackgroundTextureRegion;
	private TiledTextureRegion mScrumTextureRegion;

	private AnimatedSprite[] asprVamp = new AnimatedSprite[10];
	private int nVamp;
	private ParticleSystem particleSystem;
	private BaseParticleEmitter particleEmitter;
	
	private ScreenOrientation mScreenOrientation;
	
	private Random gen;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public org.anddev.andengine.engine.Engine onLoadEngine() {
		mHandler = new Handler();
		gen = new Random();
		return new org.anddev.andengine.engine.Engine(new EngineOptions(true, this.mScreenOrientation, new FillResolutionPolicy(), new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT)));
	}

	@Override
	public void onLoadResources() {
		TextureRegionFactory.setAssetBasePath("gfx/Wallpaper/");
		mBackgroundTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mBackgroundTextureRegion = TextureRegionFactory.createFromAsset(this.mBackgroundTexture, getApplicationContext(), "V3Wallpaper.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mBackgroundTexture);
		mScrumTexture = new Texture(512, 256, TextureOptions.DEFAULT);
		mScrumTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mScrumTexture, getApplicationContext(), "scrum_tiled.png", 0, 0, 8, 4);
		mEngine.getTextureManager().loadTexture(this.mScrumTexture);	
		this.getEngine().getTextureManager().loadTexture(this.mScrumTexture);
	}

	@Override
	public Scene onLoadScene() {
		final Scene scene = new Scene(1);
		
		//Load background
		Sprite bg = new Sprite(0,0,mBackgroundTextureRegion);
		scene.getLastChild().attachChild(bg);

       	// Add first vampire (which will add the others)
       	nVamp = 0;
		mHandler.postDelayed(mStartVamp,3000);
		
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() { }

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				for (int i=0; i<nVamp; i++){
					if (asprVamp[i].getX() < 30.0f){
						//Show explosion
						particleEmitter.setCenter(asprVamp[i].getX(), asprVamp[i].getY());
						particleSystem.setParticlesSpawnEnabled(true);
				   		mHandler.postDelayed(mEndPESpawn,2000);						
						//move vampire back to right side of screen
			           	float startY = gen.nextFloat()*(CAMERA_HEIGHT - 50.0f);
			           	asprVamp[i].clearEntityModifiers();
			           	asprVamp[i].registerEntityModifier(
			          		new MoveModifier(40.0f, CAMERA_WIDTH - 30.0f, 0.0f, startY, 340.0f)
			           	);
					}
				}
			}
		});
		
		try {
			final PXLoader pxLoader = new PXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA );
			particleSystem = pxLoader.createFromAsset(this, "gfx/particles/explo.px");
		} catch (final PXLoadException pxle) {
			Debug.e(pxle);
		}
		particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		particleSystem.setParticlesSpawnEnabled(false);
		particleEmitter = (BaseParticleEmitter) particleSystem.getParticleEmitter();
		scene.getLastChild().attachChild(particleSystem);
		return scene;
	}

	@Override
	public void onLoadComplete() {
		
	}

	// ===========================================================
	// Methods
	// ===========================================================
    private Runnable mStartVamp = new Runnable() {
        public void run() {
        	int i = nVamp;
        	Scene scene = LiveWallpaperService.this.mEngine.getScene();
           	float startY = gen.nextFloat()*(CAMERA_HEIGHT - 50.0f);
           	asprVamp[i] = new AnimatedSprite(CAMERA_WIDTH - 30.0f, startY, mScrumTextureRegion.clone()) ;
           	nVamp++;
    		scene.registerTouchArea(asprVamp[i]);
        	final long[] frameDurations = new long[26];
        	Arrays.fill(frameDurations, 500);
            asprVamp[i].animate(frameDurations, 0, 25, true);
           	asprVamp[i].registerEntityModifier(
           			new SequenceEntityModifier (
           						new AlphaModifier(5.0f, 0.0f, 1.0f),
          						new MoveModifier(40.0f, CAMERA_WIDTH - 30.0f, 0.0f, startY, 340.f)
           						));
           	scene.getLastChild().attachChild(asprVamp[i]);
        	if (nVamp < MAX_VAMPS){
        		mHandler.postDelayed(mStartVamp,VAMP_RATE);
        	}
        }
     };

	@Override
	public void onUnloadResources() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGamePaused() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameResumed() {
		// TODO Auto-generated method stub
		
	}
	
    private Runnable mEndPESpawn = new Runnable() {
        public void run() {
    		particleSystem.setParticlesSpawnEnabled(false);
        }
    };

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	
}