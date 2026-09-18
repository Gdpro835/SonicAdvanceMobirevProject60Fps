package com.sega.mobile.framework.device;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import com.sega.mobile.framework.MFMain;
import com.sega.mobile.framework.utility.MFUtility;
import java.util.Hashtable;
import java.util.Vector;
import State.TitleState;

public final class MFSound {
    public static int seLimit = 16;
    public static final int LEVEL_MAX = 15;
    public static final int VOLUME_MAX = 100;

    private static final Object LOCK = new Object();

    private static MFPlayer bgm;
    private static MFPlayer lastBgm;
    private static MFPlayer nextBgm;

    private static boolean bgmFlag;
    private static boolean bgmPlaying;
    private static boolean bgmStarted;

    private static boolean seFlag;
    private static boolean resumeFlag;
    private static boolean suspendFlag;
    private static boolean deviceInterrupted;

    private static int soundVolume;

    private static AudioManager mManager;

    private static SoundPool sePool;
    private static final Hashtable<String, Integer> seIds = new Hashtable<>();
    private static final Hashtable<Integer, Boolean> seReady = new Hashtable<>();
    private static final Vector<PendingSe> pendingSe = new Vector<>();
    private static final Hashtable<String, MFPlayer> bgmCache = new Hashtable<>();
    private static final int[] recentStreams = new int[32];
    private static int recentPos;

    private static final class PendingSe {
        String url;
        int priority;
        int loop;

        PendingSe(String url, int priority, int loop) {
            this.url = url;
            this.priority = priority;
            this.loop = loop;
        }
    }

    protected static void init() {
        bgmFlag = true;
        seFlag = true;
        deviceInterrupted = false;
        resumeFlag = false;
        suspendFlag = false;
        bgmPlaying = false;
        bgmStarted = false;
        soundVolume = 100;
        mManager = (AudioManager) MFMain.getInstance().getSystemService("audio");
        createSePool();
    }

    private static void createSePool() {
        synchronized (LOCK) {
            try {
                if (sePool != null) {
                    sePool.release();
                }
            } catch (Exception e) {
            }
            sePool = new SoundPool(seLimit, AudioManager.STREAM_MUSIC, 0);
            seIds.clear();
            seReady.clear();
            pendingSe.removeAllElements();
            sePool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    synchronized (LOCK) {
                        if (status == 0) {
                            seReady.put(Integer.valueOf(sampleId), Boolean.TRUE);
                        }
                    }
                }
            });
        }
    }

    private static String assetPath(String url) {
        if (url == null) {
            return "";
        }
        if (url.startsWith("/")) {
            return url.substring(1);
        }
        return url;
    }

    private static int ensureSeLoaded(String url) {
        Integer id = seIds.get(url);
        if (id != null) {
            return id.intValue();
        }
        if (sePool == null) {
            return 0;
        }
        try {
            AssetManager assets = MFMain.getInstance().getAssets();
            AssetFileDescriptor afd = assets.openFd(assetPath(url));
            int loaded = sePool.load(afd, 1);
            afd.close();
            if (loaded > 0) {
                seIds.put(url, Integer.valueOf(loaded));
                seReady.put(Integer.valueOf(loaded), Boolean.FALSE);
            }
            return loaded;
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isSeReady(int soundId) {
        if (soundId <= 0) {
            return false;
        }
        Boolean ready = seReady.get(Integer.valueOf(soundId));
        return ready != null && ready.booleanValue();
    }

    private static float seGain() {
        if (!seFlag || soundVolume <= 0) {
            return 0.0f;
        }
        return 1.0f;
    }

    private static int playSeStream(String url, int priority, int loop) {
        if (!seFlag || sePool == null) {
            return 0;
        }
        int soundId = ensureSeLoaded(url);
        if (soundId <= 0) {
            return 0;
        }
        if (!isSeReady(soundId)) {
            pendingSe.addElement(new PendingSe(url, priority, loop));
            return 0;
        }
        float vol = seGain();
        if (vol <= 0.0f) {
            return 0;
        }
        try {
            int streamId = sePool.play(soundId, vol, vol, priority + 1, loop, 1.0f);
            if (streamId > 0) {
                recentStreams[recentPos] = streamId;
                recentPos = (recentPos + 1) % recentStreams.length;
            }
            return streamId;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void flushPendingSe() {
        if (pendingSe.isEmpty() || sePool == null || !seFlag) {
            return;
        }
        for (int i = 0; i < pendingSe.size(); ) {
            PendingSe p = pendingSe.elementAt(i);
            Integer id = seIds.get(p.url);
            if (id == null || !isSeReady(id.intValue())) {
                i++;
                continue;
            }
            pendingSe.removeElementAt(i);
            playSeStream(p.url, p.priority, p.loop);
        }
    }

    protected static void tick() {
        synchronized (LOCK) {
            try {
                flushPendingSe();
                tickBgmLocked();
            } catch (Exception e) {
            }
        }
    }

    private static boolean updateBgm(MFPlayer player) {
        switch (player.getState()) {
            case 0:
            case 4:
                player.realize();
                return false;
            case 1:
                player.prefetch();
                return false;
            case 2:
                player.start();
                return true;
            case 3:
            default:
                return false;
        }
    }

    private static void tickBgmLocked() {
        if (!bgmFlag) {
            return;
        }
        if (suspendFlag) {
            if (!deviceInterrupted) {
                deviceInterrupted = true;
                stopBgmLocked();
            }
            suspendFlag = false;
        }
        if (resumeFlag) {
            if (deviceInterrupted) {
                deviceInterrupted = false;
                resumeBgmLocked();
            }
            resumeFlag = false;
        }
        if (nextBgm != null) {
            if (bgm != null && bgm != nextBgm) {
                try {
                    bgm.stop();
                } catch (Exception e) {
                }
            }
            bgm = nextBgm;
            nextBgm = null;
            bgmStarted = false;
        }
        if (bgm != null && (bgm.isLoop() || !bgmStarted)) {
            bgmStarted = updateBgm(bgm);
        }
        if (bgm != null && !bgm.isLoop() && bgmStarted && bgm.getState() == 2) {
            bgmPlaying = false;
            bgmStarted = false;
        }
    }

    protected static void deviceInterrupt() {
        suspendFlag = true;
    }

    protected static void deviceResume() {
        resumeFlag = true;
    }

    public static void setVolume(int volume) {
        soundVolume = MFUtility.getValueInRange(volume, 0, 100);
        if (mManager != null) {
            int maxVolume = mManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mManager.setStreamVolume(AudioManager.STREAM_MUSIC, (soundVolume * maxVolume) / 100, 0);
        }
    }

    public static int getVolume() {
        if (mManager == null) {
            return soundVolume;
        }
        int max = mManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundVolume = MFUtility.getValueInRange((current * 100) / max, 0, 100);
        return soundVolume;
    }

    public static void setLevel(int level) {
        if (mManager != null) {
            mManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
        }
    }

    public static int getLevel() {
        if (mManager == null) {
            return 0;
        }
        return mManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setBgmFlag(boolean enabled) {
        bgmFlag = enabled;
        if (!enabled) {
            stopBgm();
        }
    }

    public static boolean getBgmFlag() {
        return bgmFlag;
    }

    public static void setSeFlag(boolean enabled) {
        seFlag = enabled;
        if (!enabled) {
            stopAllSe();
        }
    }

    public static boolean getSeFlag() {
        return seFlag;
    }

    public static void preloadSound(String url) {
        synchronized (LOCK) {
            ensureSeLoaded(url);
        }
    }

    public static void preloadAllSe(String path, String[] names) {
        synchronized (LOCK) {
            if (sePool == null) {
                createSePool();
            }
            if (names == null) {
                return;
            }
            for (int i = 0; i < names.length; i++) {
                ensureSeLoaded(path + names[i]);
            }
        }
    }

    private static void evictBgmCache(String keepUrl) {
        if (bgmCache.size() < 6) {
            return;
        }
        Vector<String> drop = new Vector<>();
        java.util.Enumeration<String> keys = bgmCache.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.equals(keepUrl)) {
                continue;
            }
            if (bgm != null && key.equals(bgm.soundUrl)) {
                continue;
            }
            if (lastBgm != null && key.equals(lastBgm.soundUrl)) {
                continue;
            }
            drop.addElement(key);
        }
        for (int i = 0; i < drop.size() && bgmCache.size() >= 6; i++) {
            MFPlayer old = bgmCache.remove(drop.elementAt(i));
            if (old != null) {
                try {
                    old.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static MFPlayer getCachedBgm(String url) {
        MFPlayer player = bgmCache.get(url);
        if (player != null && player.getState() != 4) {
            return player;
        }
        player = MFPlayer.createMFPlayer(url, false);
        bgmCache.put(url, player);
        evictBgmCache(url);
        return player;
    }

    public static void playBgm(String url, boolean loop) {
        synchronized (LOCK) {
            MFPlayer player = getCachedBgm(url);
            player.setLoop(loop);
            nextBgm = player;
            bgmPlaying = true;
            lastBgm = player;
        }
    }

    public static void stopBgm() {
        synchronized (LOCK) {
            stopBgmLocked();
        }
    }

    private static void stopBgmLocked() {
        try {
            if (bgm != null) {
                bgm.stop();
            }
        } catch (Exception ignored) {
        } finally {
            bgmPlaying = false;
            bgmStarted = false;
            nextBgm = null;
        }
    }

    public static void stopBgmNoResume() {
        stopBgm();
        lastBgm = null;
    }

    public static long getBgmMediaTime() {
        synchronized (LOCK) {
            return (bgm != null) ? bgm.getMediaTime() : 0L;
        }
    }

    public static void resumeBgm() {
        synchronized (LOCK) {
            resumeBgmLocked();
        }
    }

    private static void resumeBgmLocked() {
        if (lastBgm != null && lastBgm.isLoop()) {
            bgmPlaying = true;
            nextBgm = lastBgm;
        }
    }

    public static boolean isBgmPlaying() {
        return bgmPlaying;
    }

    public static int playSe(String url) {
        return playSe(url, 0);
    }

    public static int playSe(String url, int priority) {
        if (!seFlag) {
            return 0;
        }
        if (url != null && url.contains("se_103") && TitleState.characterslots == 2 && MFMain.tails == 7) {
            url = "/se/shc.ogg";
            MFMain.tails++;
            priority = 1;
        }
        synchronized (LOCK) {
            try {
                return playSeStream(url, priority, 0);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public static int playLoopSe(String url) {
        if (!seFlag) {
            return 0;
        }
        synchronized (LOCK) {
            try {
                return playSeStream(url, 1, -1);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public static void stopStream(int streamId) {
        if (streamId <= 0 || sePool == null) {
            return;
        }
        synchronized (LOCK) {
            try {
                sePool.stop(streamId);
            } catch (Exception e) {
            }
        }
    }

    public static void stopAllSe() {
        synchronized (LOCK) {
            pendingSe.removeAllElements();
            if (sePool != null) {
                try {
                    sePool.autoPause();
                    sePool.autoResume();
                } catch (Exception e) {
                }
                try {
                    java.util.Enumeration<Integer> ids = seIds.elements();
                    while (ids.hasMoreElements()) {
                        Integer id = ids.nextElement();
                        if (id != null) {
                            sePool.stop(id.intValue());
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public static void releaseAllSound() {
        synchronized (LOCK) {
            try {
                java.util.Enumeration<MFPlayer> players = bgmCache.elements();
                while (players.hasMoreElements()) {
                    MFPlayer p = players.nextElement();
                    if (p != null) {
                        try {
                            p.close();
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
            bgmCache.clear();
            bgm = null;
            nextBgm = null;
            lastBgm = null;
            bgmPlaying = false;
            bgmStarted = false;
            createSePool();
        }
    }

    public static void setBgm(MFPlayer player, boolean loop) {
        synchronized (LOCK) {
            if (player != null) {
                player.setLoop(loop);
                if (player.soundUrl != null) {
                    bgmCache.put(player.soundUrl, player);
                }
            }
            nextBgm = player;
            bgmPlaying = true;
            lastBgm = nextBgm;
        }
    }

    public static MFPlayer getCurrentBgm() {
        synchronized (LOCK) {
            return (nextBgm != null) ? nextBgm : bgm;
        }
    }
}
