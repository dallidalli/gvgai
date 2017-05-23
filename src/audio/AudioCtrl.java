package audio;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

/**
 * Created by Carlotes(User) on 23/05/2017.
 *
 * Handles the audio implementation. Only WAV files supported at the moment
 */
public class AudioCtrl {

    private String audioSource;

    private Sound soundObject;

    private Music musicObject;

    public AudioCtrl(String audioSource) {
        this.audioSource = audioSource;
    }

    public void PlaySound(Sound soundToPlay, String soundSource) {
        // We check that the audiosystem is initialized
        if (CheckInit()) {
            // We load the sound
            soundToPlay = TinySound.loadSound(soundSource);
            // We play it
            PlaySound(soundToPlay);
        }
    }

    public void PlaySound(Sound soundToPlay) {
        if (soundToPlay == null) {
            System.out.println("soundToPlay = " + soundToPlay);
            return;
        }

        if (CheckInit()) {
            soundToPlay.play();

            // Sleep little for the sound
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

        }

    }

    public void PlaySound() {
        PlaySound(this.soundObject);
    }

    public void PlayMusic(Music musicToPlay, String musicSource) {
        // We check that the audiosystem is initialized
        if (CheckInit()) {
            // We load the music
            musicToPlay = TinySound.loadMusic(musicSource, true);
            // We play it
            PlayMusic(musicToPlay);
        }
    }

    public void PlayMusic (Music musicToPlay) {
        if (musicToPlay == null) {
            System.out.println("musicToPlay = " + musicToPlay);
            return;
        }

        if (CheckInit()) {
            musicToPlay.play(true);

            // Sleep a lot for music
		    try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
        }
    }

    public void PlayMusic () {
        PlayMusic(this.musicObject);
    }

    /**
     *  Check if the audio system is initialized, and if not, initializes it
     * @return false if the system was not initialized
     */
    private boolean CheckInit() {

        if (!TinySound.isInitialized()) {
            InitializeSystem();
        }
        return TinySound.isInitialized();

    }

    /**
     * Initializes the entire system. Call on begin
     */
    public void InitializeSystem() {
        //initialize TinySound
        TinySound.init();
    }

    /**
     * Terminates the entire system. Call on end
     */
    public void ShutDownSystem() {
        //be sure to shutdown TinySound when done
        TinySound.shutdown();
    }





}
