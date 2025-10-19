package nl.sniffiandros.bren.common.config;

import com.google.gson.*;
import nl.sniffiandros.bren.common.Bren;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

/*
 * Credits : Khajiitos
 * Git : https://github.com/Khajiitos/ChestedCompanions/blob/master/Common/src/main/java/me/khajiitos/chestedcompanions/common/config/CCConfig.java
 */

public class MConfig {
    private static final File file = new File("config/bren_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Entry(clientOnly = true)
    public static final ConfigHelper.BooleanValue renderGunOnBack = new ConfigHelper.BooleanValue(true,
            "Renders the gun on backs");

    @Entry(clientOnly = true)
    public static final ConfigHelper.BooleanValue spawnCasingParticles = new ConfigHelper.BooleanValue(true,
            "Spawns empty casings when a gun is fired");

    @Entry(clientOnly = true)
    public static final ConfigHelper.BooleanValue showAmmoGui = new ConfigHelper.BooleanValue(true,
            "Shows the ammo GUI");

    @Entry
    public static final ConfigHelper.BooleanValue bulletsBreakGlass = new ConfigHelper.BooleanValue(true,
            "Breaks glass on bullet impact");

    @Entry
    public static final ConfigHelper.FloatValue recoilMultiplier = new ConfigHelper.FloatValue(1f,
            "The recoil multiplier, so 0 is no recoil");
    
    @Entry
    public static final ConfigHelper.FloatValue sneakingRecoilMultiplier = new ConfigHelper.FloatValue(0.75f,
            "Only applies when the player is sneaking");
    
    @Entry
    public static final ConfigHelper.FloatValue damageMultiplier = new ConfigHelper.FloatValue(1f,
            "General damage multiplier for all guns");

    @Entry
    public static final ConfigHelper.FloatValue headshotMultiplier = new ConfigHelper.FloatValue(2f,
            "1 to keep the visuals, less than 1 to disable completely");
    
    @Entry
    public static final ConfigHelper.FloatValue strongHeadshotMultiplier = new ConfigHelper.FloatValue(3f,
            "Used by revolvers and the Skull Crusher enchantment");
    
    @Entry
    public static final ConfigHelper.FloatValue fireRateMultiplier = new ConfigHelper.FloatValue(1f,
            "The actual cooldown length are rounded to ticks");

    @Entry
    public static final ConfigHelper.FloatValue ammoCapacityMultiplier = new ConfigHelper.FloatValue(1f,
            "Maximum level of Overflow always gives twice the base capacity");

    public static void init() {
        if (!file.exists()) {
            save();
        } else {
            load();
        }
    }

    public static void save() {
        if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
            Bren.LOGGER.error("Failed to create config directory");
            return;
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            JsonObject root = new JsonObject();

            for (Field field : MConfig.class.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Entry.class)) {
                    continue;
                }

                Object object = field.get(null);

                if (!(object instanceof ConfigHelper.Value<?> configValue)) {
                    continue;
                }

                JsonObject entry = new JsonObject();

                entry.add("value", configValue.write());
                entry.addProperty("description", configValue.getComment());

                root.add(field.getName(), entry);
            }

            GSON.toJson(root, fileWriter);
        } catch (IOException e) {
            Bren.LOGGER.error("Failed to save the Bren config", e);
        } catch (IllegalAccessException e) {
            Bren.LOGGER.error("Error while saving the Bren config", e);
        }
    }

    public static void load() {
        if (!file.exists()) {
            return;
        }

        try (FileReader fileReader = new FileReader(file)) {
            JsonObject jsonObject = GSON.fromJson(fileReader, JsonObject.class);

            for (Field field : MConfig.class.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Entry.class)) {
                    continue;
                }

                String fieldName = field.getName();

                if (!jsonObject.has(fieldName)) {
                    continue;
                }

                Object object = field.get(null);

                if (!(object instanceof ConfigHelper.Value<?> configValue)) {
                    continue;
                }

                JsonElement jsonElement = jsonObject.getAsJsonObject(fieldName).get("value");
                configValue.setUnchecked(configValue.read(jsonElement));
            }
        } catch (IOException e) {
            Bren.LOGGER.error("Failed to read the Bren config", e);
        } catch (IllegalAccessException e) {
            Bren.LOGGER.error("Error while reading the Bren config", e);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Entry {
        String category() default "general";
        boolean clientOnly() default false;
    }
}
