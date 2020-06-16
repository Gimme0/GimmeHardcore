package me.gimme.gimmehardcore.unused;

import org.bukkit.configuration.ConfigurationSection;

public class AdvancementToJsonConverter {

    private static final String INDENTATION = "  ";

    private static String toJson(ConfigurationSection configurationSection) {
        StringBuilder result = new StringBuilder();
        buildJson(configurationSection, result, new StringBuilder());
        return result.toString();
    }

    private static void buildJson(ConfigurationSection configurationSection, StringBuilder builder, StringBuilder indentBuilder) {
        String currentIndent = indentBuilder.toString();
        builder.append("{\n");
        builder.append(indentBuilder.append(INDENTATION));
        for (String key : configurationSection.getKeys(false)) {
            builder.append("\"").append(key).append("\": ");
            buildJson(configurationSection.getConfigurationSection(key), builder, indentBuilder);
        }
        builder.append("\n");
        builder.append(currentIndent).append("},");
    }

}
