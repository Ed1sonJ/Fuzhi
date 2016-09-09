package com.smartfarm.event;

import de.greenrobot.event.EventBus;

/**
 * Created by june on 2016/1/4.
 */
public class GlobalEvent {
    public static EventBus bus;

    public static void init() {
        bus = new EventBus();
    }
}
