package me.zeroeightsix.kami.module;

import me.zeroeightsix.kami.KamiMod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.zeroeightsix.kami.util.Macro.readFileToMemory;
import static me.zeroeightsix.kami.util.Macro.writeMemoryToFile;

/**
 * @author dominikaaaa
 */
public class MacroManager {

    /*
     * Map of all the macros.
     * KeyCode, Actions
     */
    public static Map<String, List<String>> macros = new LinkedHashMap<>();

    /**
     * Registers macros
     */
    public static void register() {
        KamiMod.log.info("Registering macros...");
        readFileToMemory();
        KamiMod.log.info("Macros registered");
    }

    /**
     * Saves macros
     */
    public static void saveMacros() {
        KamiMod.log.info("Saving macros...");
        writeMemoryToFile();
        KamiMod.log.info("Macros saved");
    }
}
