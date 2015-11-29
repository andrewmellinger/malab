package com.crashbox.malab.util;

import joptsimple.internal.Strings;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MALEventHandler
{
    @SubscribeEvent
    public void commandEventHandler(CommandEvent commandEvent)
    {
        LOGGER.debug("command=" + commandEvent.command + ", parameters=" +
                Strings.join(commandEvent.parameters, ":"));
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
