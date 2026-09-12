package Lib;

/**
 * Project 60fps -- central time-scaling helpers.
 *
 * The game logic was authored for ~15 logic ticks per second (FRAME_SKIP 63ms).
 * Running the loop at 60fps (FRAME_SKIP 16ms) multiplies the number of logic
 * ticks per second by SCALE (=4), so every per-tick quantity has to be scaled
 * down by the same factor to keep the game running at its intended speed:
 *
 *   - velocities   : applied as v / 4 per tick        -> use the step*() helpers
 *   - accelerations: constants divided by 4           -> patched at the constant
 *   - timers/counts: multiplied by 4                  -> t()
 *   - animations   : played 4x slower                 -> AnimationDrawer.setSpeed
 *
 * Naive integer division (v >> 2) throws away the remainder, which makes slow
 * objects (|v| < 4) freeze completely and makes everything else drift slightly
 * slower than the original. The step helpers below therefore carry the
 * remainder over to the next tick, so that the movement summed over any 4
 * consecutive ticks is exactly equal to the original 15fps movement of 1 frame.
 */
public final class FPS {

    /** Logic ticks at 60fps per original 15fps frame. */
    public static final int SCALE = 4;

    /** log2(SCALE), for shift-based division. */
    public static final int SHIFT = 2;

    private FPS() {
    }

    /**
     * Scale an original per-frame timer/counter value to 60fps ticks.
     * A counter that used to run for 30 frames must now run for 120 ticks.
     */
    public static int t(int framesAt15fps) {
        return framesAt15fps * SCALE;
    }

    /**
     * Scale a value that is expressed "per second" and therefore must NOT change,
     * kept for readability at call sites where the distinction matters.
     */
    public static int perSecond(int value) {
        return value;
    }

    /**
     * Divide a per-frame amount by SCALE, carrying the remainder in acc.
     * Returns the integer amount to apply this tick.
     *
     * Usage:  this.posX += FPS.step(accHolder, 0, this.velX);
     * where accHolder is an int[] owned by the moving object.
     */
    public static int step(int[] acc, int index, int perFrameAmount) {
        acc[index] += perFrameAmount;
        int applied = acc[index] >> SHIFT;      // arithmetic shift: floors
        acc[index] -= applied << SHIFT;         // remainder stays in [0, SCALE)
        return applied;
    }
}
