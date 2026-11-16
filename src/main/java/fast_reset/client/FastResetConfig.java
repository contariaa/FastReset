package fast_reset.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import me.contaria.speedrunapi.config.api.SpeedrunConfigParsedMetadata;
import me.contaria.speedrunapi.config.api.annotations.Config;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;

public class FastResetConfig implements SpeedrunConfig {
    public ButtonLocation buttonLocation = ButtonLocation.BOTTOM_RIGHT;

    @Config.Numbers.Whole.Bounds(min = 0, max = 300, enforce = Config.Numbers.EnforceBounds.MIN_ONLY)
    @Config.Text(getter = "getAlwaysSaveAfterText")
    public int alwaysSaveAfter = 150;

    {
        FastReset.config = this;
    }

    @Override
    public String modID() {
        return "fast_reset";
    }

    @Override
    public int getDataVersion() {
        return 1;
    }

    @Override
    public void onLoad(JsonObject jsonObject, SpeedrunConfigParsedMetadata metadata) {
        if (metadata.getDataVersion() < 1) {
            JsonElement buttonLocation = jsonObject.get("buttonLocation");
            if (buttonLocation != null && "REPLACE_SQ".equals(buttonLocation.getAsString())) {
                jsonObject.add("buttonLocation", new JsonPrimitive(ButtonLocation.CENTER.name()));
            }
        }
    }

    @SuppressWarnings("unused")
    public Text getAlwaysSaveAfterText(int value) {
        String baseKey = "speedrunapi.config.fast_reset.option.alwaysSaveAfter";
        int hours = value / (60 * 60);
        LinkedHashMap<String, Integer> times = new LinkedHashMap<>(3);
        times.put("hours", hours);
        times.put("minutes", (value) % (60 * 60) / 60);
        times.put("seconds", value % 60);
        return times.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> TextUtil.translatable(baseKey + "." + entry.getKey(), entry.getValue()))
                .reduce((text, text2) -> text.append(" ").append(text2)).orElse(TextUtil.translatable(baseKey + ".unknown"));
    }

    public enum ButtonLocation {
        BOTTOM_RIGHT,
        CENTER,
        HIDE
    }
}
