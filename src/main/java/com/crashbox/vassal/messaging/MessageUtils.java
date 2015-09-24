package com.crashbox.vassal.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright CMU 2015.
 */
public class MessageUtils
{
    @SuppressWarnings("unchecked")
    public static <T extends MessageTaskRequest> List<T> extractMessages(Object transactionID,
                                                                   List<MessageTaskRequest> responses,
                                                                   Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        Iterator<MessageTaskRequest> iter = responses.iterator();
        while (iter.hasNext())
        {
            MessageTaskRequest next =  iter.next();
            if (clazz.isInstance(next) && next.getTransactionID() == transactionID)
            {
                result.add((T) next);
                iter.remove();
            }
        }
        return result;
    }

}
