package com.crashbox.malab;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MALConfig
{
    public final static String AUTONOMY_CATEGORY = "autonomy";

    public enum Setting
    {
        MAX_XZ_DISTANCE(AUTONOMY_CATEGORY, "max_xz_distance"),
        MAX_Y_DISTANCE(AUTONOMY_CATEGORY, "max_y_distance"),
        DISTANCE_Y_COEFFICIENT(AUTONOMY_CATEGORY, "distance_y_coefficient"),
        FORESTER_HARVEST_VALUE(AUTONOMY_CATEGORY, "forester_harvest_value"),
        FORESTER_IDLE_HARVEST_VALUE(AUTONOMY_CATEGORY, "forester_idle_harvest_value"),
        QUARRY_HARVEST_PRIORITY(AUTONOMY_CATEGORY, "quarry_harvest_value"),
        QUARRY_IDLE_HARVEST_PRIORITY(AUTONOMY_CATEGORY, "quarry_idle_harvest_value"),
        QUARRY_DEPTH_COEFFICIENT_VALUE(AUTONOMY_CATEGORY, "quarry_depth_coefficient_value");

        private Setting(String category, String key)
        {
            _category = category;
            _key = key;
        }

        public String getCategory()
        {
            return _category;
        }

        public String getKey()
        {
            return _key;
        }

        public String getTranslatedKey()
        {
            if (_translation != null)
                return _translation;
            return _key;
        }

        public String makeChatText(String value)
        {
            return getTranslatedKey() + "=" + value;
        }

        public String getHelp()
        {
            return _help;
        }

        public void initTranslations()
        {
            _translation = getTranslation(getLangFileKey(this));
            _help = getSettingComment(this);
        }

        private final String _category;
        private final String _key;
        private String _translation = null;
        private String _help = "";
    }

    //----------------------------------------------------------------------------------------------

    public MALConfig()
    {
        // This is for the command
        _translationToSetting = new HashMap<String, Setting>();

        for (Setting setting : Setting.values())
        {
            setting.initTranslations();
            _translationToSetting.put(setting.getTranslatedKey(), setting);
        }
    }

    public void loadAndInit(File file)
    {
        _config = new Configuration(file);
        _config.load();

        // Init the config file
        getMaxXZDistanceProperty();
        getMaxYDistanceProperty();
        getDistanceYCoefficientProperty();
        getForesterHarvestValueProperty();
        getForesterIdleHarvestValueProperty();
        getQuarryDepthCoefficientValueProperty();
        getQuarryHarvestValueProperty();
        getQuarryIdleHarvestValueProperty();

        _config.save();
    }

    //----------------------------------------------------------------------------------------------
    public int getMaxXZDistance()
    {
        return getMaxXZDistanceProperty().getInt();
    }

    public void setMaxXZDistance(int val)
    {
        getMaxXZDistanceProperty().set(val);
        _config.save();
    }

    private Property getMaxXZDistanceProperty()
    {
        Setting setting = Setting.MAX_XZ_DISTANCE;
        return _config.get(setting.getCategory(), setting.getKey(), 64, setting.getHelp(), 0, 256);
    }

    //----------------------------------------------------------------------------------------------

    public int getMaxYDistance()
    {
        return getMaxYDistanceProperty().getInt();
    }

    public void setMaxYDistance(int val)
    {
        getMaxYDistanceProperty().set(val);
        _config.save();
    }

    private Property getMaxYDistanceProperty()
    {
        Setting setting = Setting.MAX_Y_DISTANCE;
        return _config.get(setting.getCategory(), setting.getKey(), 256, setting.getHelp(), 0, 512);
    }

    //----------------------------------------------------------------------------------------------

    public double getDistanceYCoefficient()
    {
        return getDistanceYCoefficientProperty().getDouble();
    }

    public void setDistanceYCoefficient(double val)
    {
        getDistanceYCoefficientProperty().set(val);
        _config.save();
    }

    private Property getDistanceYCoefficientProperty()
    {
        Setting setting = Setting.DISTANCE_Y_COEFFICIENT;
        return _config.get(setting.getCategory(), setting.getKey(), 0.25D, setting.getHelp(), 0.0D, 10.0D);
    }

    //----------------------------------------------------------------------------------------------

    public int getForesterHarvestValue()
    {
        return getForesterHarvestValueProperty().getInt();
    }

    public void setForesterHarvestValue(int val)
    {
        getForesterHarvestValueProperty().set(val);
        _config.save();
    }

    private Property getForesterHarvestValueProperty()
    {
        Setting setting = Setting.FORESTER_HARVEST_VALUE;
        return _config.get(setting.getCategory(), setting.getKey(), 0, setting.getHelp());
    }

    //----------------------------------------------------------------------------------------------

    public int getForesterIdleHarvestValue()
    {
        return getForesterIdleHarvestValueProperty().getInt();
    }

    public void setForesterIdleHarvestValue(int val)
    {
        getForesterIdleHarvestValueProperty().set(val);
        _config.save();
    }

    private Property getForesterIdleHarvestValueProperty()
    {
        Setting setting = Setting.FORESTER_IDLE_HARVEST_VALUE;
        return _config.get(setting.getCategory(), setting.getKey(), -5, setting.getHelp());
    }

    //----------------------------------------------------------------------------------------------

    public double getQuarryDepthCoefficientValue()
    {
        return getQuarryDepthCoefficientValueProperty().getDouble();
    }

    public void setQuarryDepthCoefficientValue(double val)
    {
        getQuarryDepthCoefficientValueProperty().set(val);
        _config.save();
    }

    private Property getQuarryDepthCoefficientValueProperty()
    {
        Setting setting = Setting.QUARRY_DEPTH_COEFFICIENT_VALUE;
        return _config.get(setting.getCategory(), setting.getKey(), 0.2D, setting.getHelp(), 0.0D, 10.0D);
    }

    //----------------------------------------------------------------------------------------------

    public int getQuarryHarvestValue()
    {
        return getQuarryHarvestValueProperty().getInt();
    }

    public void setQuarryHarvestValue(int val)
    {
        getQuarryHarvestValueProperty().set(val);
        _config.save();
    }

    private Property getQuarryHarvestValueProperty()
    {
        Setting setting = Setting.QUARRY_HARVEST_PRIORITY;
        return _config.get(setting.getCategory(), setting.getKey(), 0, setting.getHelp());
    }

    //----------------------------------------------------------------------------------------------

    public int getQuarryIdleHarvestValue()
    {
        return getQuarryIdleHarvestValueProperty().getInt();
    }

    public void setQuarryIdleHarvestValue(int val)
    {
        getQuarryIdleHarvestValueProperty().set(val);
        _config.save();
    }

    private Property getQuarryIdleHarvestValueProperty()
    {
        Setting setting = Setting.QUARRY_IDLE_HARVEST_PRIORITY;
        return _config.get(setting.getCategory(), setting.getKey(), 0, setting.getHelp());
    }

    //----------------------------------------------------------------------------------------------

    public ICommand makeCommand()
    {
        return new Command();
    }

    private class Command extends CommandBase
    {
        @Override
        public String getName()
        {
            return "malab";
        }

        @Override
        public String getCommandUsage(ICommandSender iCommandSender)
        {
            return "commands.malab.usage";
        }

        @Override
        public void execute(ICommandSender sender, String[] strings) throws CommandException
        {
            // Design
            // malab set <rule> <value>
            // malab - no args shows all rules with help
            // malab help <no args> - same as above
            if (strings.length == 0)
            {
                // Show all values
                sender.addChatMessage(new ChatComponentText(makeHelpString()));
                return;
            }

            if (strings.length == 1)
            {
                // Show the specified value.
                Setting setting = _translationToSetting.get(strings[0]);
                if (setting == null)
                {
                    addUnknownKeyMessage(sender, strings[0]);
                    return;
                }

                String msg = setting.makeChatText(getValue(setting));
                sender.addChatMessage(new ChatComponentText(msg));
            }

            if (strings.length == 2)
            {
                if (strings[0].equals("help"))
                {
                    Setting setting = _translationToSetting.get(strings[1]);
                    if (setting == null)
                    {
                        addUnknownKeyMessage(sender, strings[1]);
                        return;
                    }
                    sender.addChatMessage(new ChatComponentText(setting.getHelp()));
                    return;
                }

                // Now we are going to try to set it

                Setting setting = _translationToSetting.get(strings[0]);
                if (setting == null)
                {
                    addUnknownKeyMessage(sender, strings[0]);
                    return;
                }

                // Set key and value
                String oldValue = getValue(setting);
                if (setValue(setting, strings[1]))
                {
                    String msg = strings[0] + " =:  " + oldValue + " -> " + strings[1];
                    sender.addChatMessage(new ChatComponentText(msg));
                }
                else
                {
                    addParseFailedMessage(sender, strings[1]);
                }
            }
        }
    }

    // General routine for getting
    private String getValue(Setting setting)
    {
        switch (setting)
        {
            case MAX_XZ_DISTANCE:
                return "" + getMaxXZDistance();
            case MAX_Y_DISTANCE:
                return "" + getMaxYDistance();
            case DISTANCE_Y_COEFFICIENT:
                return "" + getDistanceYCoefficient();
            case FORESTER_HARVEST_VALUE:
                return "" + getForesterHarvestValue();
            case FORESTER_IDLE_HARVEST_VALUE:
                return "" + getForesterIdleHarvestValue();
            case QUARRY_DEPTH_COEFFICIENT_VALUE:
                return "" + getQuarryDepthCoefficientValue();
            case QUARRY_HARVEST_PRIORITY:
                return "" + getQuarryHarvestValue();
            case QUARRY_IDLE_HARVEST_PRIORITY:
                return "" + getQuarryIdleHarvestValue();
        }

        return "Unknown";
    }

    // General routine for setting!
    private boolean setValue(Setting setting, String value)
    {
        try
        {
            switch (setting)
            {
                case MAX_XZ_DISTANCE:
                    setMaxXZDistance(Integer.parseInt(value));
                    break;
                case MAX_Y_DISTANCE:
                    setMaxYDistance(Integer.parseInt(value));
                    break;
                case DISTANCE_Y_COEFFICIENT:
                    setDistanceYCoefficient(Double.parseDouble(value));
                    break;
                case FORESTER_HARVEST_VALUE:
                    setForesterHarvestValue(Integer.parseInt(value));
                    break;
                case FORESTER_IDLE_HARVEST_VALUE:
                    setForesterIdleHarvestValue(Integer.parseInt(value));
                    break;
                case QUARRY_DEPTH_COEFFICIENT_VALUE:
                    setQuarryDepthCoefficientValue(Double.parseDouble(value));
                    break;
                case QUARRY_HARVEST_PRIORITY:
                    setQuarryHarvestValue(Integer.parseInt(value));
                    break;
                case QUARRY_IDLE_HARVEST_PRIORITY:
                    setQuarryIdleHarvestValue(Integer.parseInt(value));
                    break;
            }

            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }


    //----------------------------------------------------------------------------------------------
    private String makeHelpString()
    {
        StringBuilder builder = new StringBuilder();
        boolean separator = false;
        for (Setting setting : Setting.values())
        {
            if (separator)
            {
                builder.append(", ");
            }
            separator = true;
            builder.append(setting.makeChatText(getValue(setting)));
        }
        return builder.toString();
    }

    //----------------------------------------------------------------------------------------------

    private void addUnknownKeyMessage(ICommandSender sender, String key)
    {
        sender.addChatMessage(new ChatComponentText(new ChatComponentTranslation("commands.error.unknownkey").
                getUnformattedText() + " : " + key));
    }

    private void addParseFailedMessage(ICommandSender sender, String value)
    {
        sender.addChatMessage(new ChatComponentText(new ChatComponentTranslation("commands.error.parsefailed").
                getUnformattedText() + " : " + value));
    }

    //----------------------------------------------------------------------------------------------
    // Convenience

    private static String getLangFileKey(Setting setting)
    {
        return "setting." + setting.getCategory() + "." + setting.getKey();
    }

    private static String getSettingComment(Setting setting)
    {
        return getTranslation(getLangFileKey(setting) + ".help");
    }

    private static String getTranslation(String key)
    {
        return new ChatComponentTranslation(key).getUnformattedText();
    }

    //----------------------------------------------------------------------------------------------

    // Where we store all the value
    private Configuration _config;

    // Cache translations
    private static Map<String, Setting> _translationToSetting;

    private static final Logger LOGGER = LogManager.getLogger();
}
