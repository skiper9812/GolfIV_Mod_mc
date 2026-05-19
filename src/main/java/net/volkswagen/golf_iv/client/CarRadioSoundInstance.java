package net.volkswagen.golf_iv.client;

import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Manages looping sound instances that track the spatial position of a driving car entity.
 */
public class CarRadioSoundInstance extends AbstractTickableSoundInstance {
    private final Golf4CarEntity car;

    private static final net.minecraft.sounds.SoundEvent[] DISCS = {
        SoundEvents.MUSIC_DISC_CAT.value(),
        SoundEvents.MUSIC_DISC_BLOCKS.value(),
        SoundEvents.MUSIC_DISC_CHIRP.value(),
        SoundEvents.MUSIC_DISC_FAR.value(),
        SoundEvents.MUSIC_DISC_MALL.value(),
        SoundEvents.MUSIC_DISC_MELLOHI.value(),
        SoundEvents.MUSIC_DISC_STAL.value(),
        SoundEvents.MUSIC_DISC_STRAD.value(),
        SoundEvents.MUSIC_DISC_WARD.value(),
        SoundEvents.MUSIC_DISC_WAIT.value()
    };

    /**
     * Constructs a ticking sound instance bound to the vehicle entity and plays a random track.
     *
     * @param car The Golf 4 car entity.
     */
    public CarRadioSoundInstance(Golf4CarEntity car) {
        super(DISCS[Math.abs(car.getId()) % DISCS.length], SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.car = car;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.5F;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.x = car.getX();
        this.y = car.getY();
        this.z = car.getZ();
    }

    /**
     * Ticks the sound, shifting coordinates to track the entity, or stopping if the radio turns off.
     */
    @Override
    public void tick() {
        if (car.isRemoved() || !car.isRadioPlaying()) {
            this.stop();
            return;
        }
        this.x = car.getX();
        this.y = car.getY();
        this.z = car.getZ();
    }

    /**
     * Programmatically terminates sound playback.
     */
    public void stopSound() {
        this.stop();
    }
}
