# SonicAdvanceMobirevProject60Fps

Sketchware Pro port of Sonic Advance with a 60 FPS tick.

## GPU rendering

On Android 6+ the game draws with `SurfaceHolder.lockHardwareCanvas()`:

- Sprites and tiles go through the GPU instead of a software framebuffer.
- The internal game resolution is scaled to the screen on the GPU.
- Clip (`setClip`) uses a save/restore path that hardware Canvas accepts.
- Bitmaps call `prepareToDraw()` so they can be uploaded as textures.
- If a hardware canvas cannot be locked, the old CPU buffer blit is used.
- Text still uses the `fontImage` overlay; that bitmap is erased every frame so loading tips do not stay after the tip window closes.

Set `MFDevice.useGpu = false` to force the software path.

## Audio

Sound effects go through Android `SoundPool` (preloaded, no extra threads).
BGM stays on a small `MediaPlayer` cache. Loop/sequence SEs share one stream so
ziplines, 1-ups and score tally cannot spawn MediaPlayer storms.
