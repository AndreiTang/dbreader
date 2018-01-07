package com.moss.dbreader.fragment.events;

/**
 * Created by tangqif on 1/5/2018.
 */

public class StatusBarVisibleEvent {
    public StatusBarVisibleEvent(boolean isVisible) {
        this.isVisible = isVisible;
    }
    public boolean isVisible;
}
