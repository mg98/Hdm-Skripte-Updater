package de.gregoriadis;

import com.sun.jna.Library;
import com.sun.jna.Native;

interface INsUserNotificationsBridge extends Library {
    INsUserNotificationsBridge instance = (INsUserNotificationsBridge)
            Native.loadLibrary("/usr/local/lib/NsUserNotificationsBridge.dylib", INsUserNotificationsBridge.class);

    public int sendNotification(String title, String subtitle, String text, int timeoffset);
}