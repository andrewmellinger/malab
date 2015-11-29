package com.crashbox.malab.task;

import com.crashbox.malab.messaging.IMessager;
import com.crashbox.malab.messaging.MessageTaskRequest;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright Andrew O. Mellinger 2015.
 */
public interface ITask
{
    enum Resolving { UNRESOLVED, RESOLVING, RESOLVED, CANT_RESOLVE }
    enum UpdateResult { CONTINUE, RETARGET, DONE }

    /** @return True if resolved */
    boolean resolve();

    void linkupResponses(List<MessageTaskRequest> responses);

    int getValue(double speed);

    void sendHeartbeat(long expire);

    void start();

    IMessager getRequester();

    BlockPos getWorkCenter();

    BlockPos getWorkTarget(List<BlockPos> exclusions);

    UpdateResult updateTask();
}
