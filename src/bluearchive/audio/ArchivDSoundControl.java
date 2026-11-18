package bluearchive.audio;

import arc.Core;
import arc.Events;
import arc.audio.Music;
import arc.audio.Sound;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Nullable;
import bluearchive.ui.overrides.ArchivDLoadingFragment;
import mindustry.game.EventType;
import mindustry.gen.Musics;
import mindustry.gen.Sounds;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static bluearchive.ArchiveDustry.recollectionMusic;
import static bluearchive.audio.ArchivDMusic.*;
import static bluearchive.ui.ArchivDUI.*;
import static mindustry.Vars.*;

public class ArchivDSoundControl {
    private static boolean previousLoadFragShow = false;
    private static @Nullable Music cachedMenuCm, cachedMenuReAoh;
    private static @Nullable Music activeCustomUiMusic = null;

    public static void loadSoundControl() {
        if (headless || control == null || control.sound == null) return;

        Events.on(EventType.MusicRegisterEvent.class, e -> registerCustomMusics());
        registerCustomMusics();

        // UIバスの安全な割り当て
        for (Sound sound : Core.assets.getAll(Sound.class, new Seq<>())) {
            String path = Core.assets.getAssetFileName(sound);
            if (path != null && Fi.get(path).parent().name().equals("ui")) {
                // getterやパブリックな仕組みを経由するか、安全にフォールバック
                try {
                    sound.setBus(Core.audio.soundBus);
                } catch (Exception ignored) {}
            }
        }

        Events.run(EventType.Trigger.update, ArchivDSoundControl::updateCustomUiMusic);
    }

    private static void registerCustomMusics() {
        if (headless || control == null || control.sound == null) return;

        // 本体側のプレイリストに追加（安全なプロパティ確認）
        try {
            addUnique(control.sound.ambientMusic, dawn, cat, bunny, game10, honey, amplify, t171, theme220);
            addUnique(control.sound.darkMusic, aira, sugar, hare, oriental, dreamer, game11, moment, somedaySometime, theme228);
            addUnique(control.sound.bossMusic, boss3, boss4);
        } catch (Exception ignored) {}
    }

    private static void addUnique(Seq<Music> target, Music... musics) {
        if (target == null) return;
        for (Music m : musics) {
            if (m != null && !target.contains(m)) {
                target.add(m);
            }
        }
    }

    private static void updateCustomUiMusic() {
        if (headless || state == null || ui == null || control == null || control.sound == null) return;

        if (ArchivDLoadingFragment.loadFragShow && !previousLoadFragShow) {
            if (Sounds.uiChat != null) Sounds.uiChat.play();
        }
        previousLoadFragShow = ArchivDLoadingFragment.loadFragShow;

        Music desiredUiMusic = null;

        if (state.isMenu()) {
            if (ui.planet != null && ui.planet.isShown()) {
                if (ui.research != null && ui.research.isShown()) {
                    desiredUiMusic = research;
                } else if (ui.database != null && ui.database.isShown()) {
                    desiredUiMusic = database;
                } else if (ui.planet.loadouts != null && ui.planet.loadouts.isShown()) {
                    desiredUiMusic = loadout;
                }
            } else if (ui.editor == null || !ui.editor.isShown()) {
                if (ui.database != null && ui.database.isShown()) {
                    desiredUiMusic = database;
                } else if (creditsDialog != null && creditsDialog.isShown()) {
                    desiredUiMusic = re_aoh;
                }
            }
        } else if (!state.rules.editor) {
            if (ui.database != null && ui.database.isShown()) {
                desiredUiMusic = database;
            } else if (ui.research != null && ui.research.isShown()) {
                desiredUiMusic = research;
            } else if (ui.schematics != null && ui.schematics.isShown()) {
                desiredUiMusic = loadout;
            }
        }

        if (desiredUiMusic != null) {
            if (activeCustomUiMusic != desiredUiMusic) {
                stopAllCustomUiMusic();
                control.sound.stop();
                activeCustomUiMusic = desiredUiMusic;
            }
            if (!desiredUiMusic.isPlaying()) {
                control.sound.stop();
                desiredUiMusic.play();
            }
        } else if (activeCustomUiMusic != null) {
            stopAllCustomUiMusic();
            activeCustomUiMusic = null;
        }
    }

    private static void stopAllCustomUiMusic() {
        Music[] uiTracks = {database, research, loadout, re_aoh};
        for (Music m : uiTracks) {
            if (m != null && m.isPlaying()) {
                m.stop();
            }
        }
    }

    public static void replaceMainMenu() {
        if (headless) return;

        boolean isAprilFools = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM")).equals("01-04");
        if (isAprilFools && ArchivDMusic.funnyAhh != null) {
            Musics.menu = ArchivDMusic.funnyAhh;
            return;
        }

        String selected = Core.settings.getString("selectedSong", "menucm");
        switch (selected) {
            case "menucm":
                if (cachedMenuCm == null) cachedMenuCm = tree.loadMusic("menucm");
                if (cachedMenuCm != null) Musics.menu = cachedMenuCm;
                break;
            case "menure-aoh":
                if (cachedMenuReAoh == null) cachedMenuReAoh = tree.loadMusic("menure-aoh");
                if (cachedMenuReAoh != null) Musics.menu = cachedMenuReAoh;
                break;
            case "recollection":
                if (recollectionMusic != null) Musics.menu = recollectionMusic;
                break;
        }
    }
}
