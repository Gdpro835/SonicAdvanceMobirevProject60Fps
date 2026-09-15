# SonicAdvanceMobirevProject60Fps

Sketchware Pro port of Sonic Advance with a 60 FPS tick.

## GPU rendering

On Android 6+ the game draws with `SurfaceHolder.lockHardwareCanvas()`:

- Sprites and tiles go through the GPU instead of a software framebuffer.
- The internal game resolution is scaled to the screen on the GPU.
- Clip (`setClip`) uses a save/restore path that hardware Canvas accepts.
- Bitmaps call `prepareToDraw()` so they can be uploaded as textures.
- If a hardware canvas cannot be locked, the old CPU buffer blit is used.

Set `MFDevice.useGpu = false` to force the software path.
