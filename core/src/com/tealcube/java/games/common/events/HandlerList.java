package com.tealcube.java.games.common.events;

import java.util.*;

public class HandlerList {

    private volatile RegisteredListener[] handlers = null;
    private final EnumMap<EventPriority, List<RegisteredListener>> listenerSlots;
    private static final List<HandlerList> ALL_LISTS = new ArrayList<HandlerList>();

    public static void bakeAll() {
        synchronized (ALL_LISTS) {
            for (HandlerList h : ALL_LISTS) {
                h.bake();
            }
        }
    }

    public static void unregisterAll() {
        synchronized (ALL_LISTS) {
            for (HandlerList h : ALL_LISTS) {
                synchronized (h) {
                    for (List<RegisteredListener> list : h.listenerSlots.values()) {
                        list.clear();
                    }
                    h.handlers = null;
                }
            }
        }
    }

    public static void unregisterAll(Listener listener) {
        synchronized (ALL_LISTS) {
            for (HandlerList h : ALL_LISTS) {
                h.unregister(listener);
            }
        }
    }

    public HandlerList() {
        this.listenerSlots = new EnumMap<EventPriority, List<RegisteredListener>>(EventPriority.class);
        for (EventPriority ep : EventPriority.values()) {
            this.listenerSlots.put(ep, new ArrayList<RegisteredListener>());
        }
        synchronized (ALL_LISTS) {
            ALL_LISTS.add(this);
        }
    }

    public synchronized void register(RegisteredListener listener) {
        if (listenerSlots.get(listener.getPriority()).contains(listener)) {
            throw new IllegalStateException();
        }
        handlers = null;
        listenerSlots.get(listener.getPriority()).add(listener);
    }

    public void registerAll(Collection<RegisteredListener> listeners) {
        for (RegisteredListener listener : listeners) {
            register(listener);
        }
    }

    public synchronized void unregister(Listener listener) {
        boolean changed = false;
        for (List<RegisteredListener> list : listenerSlots.values()) {
            for (ListIterator<RegisteredListener> i = list.listIterator(); i.hasNext(); ) {
                if (i.next().getListener().equals(listener)) {
                    i.remove();
                    changed = true;
                }
            }
        }
        if (changed) {
            handlers = null;
        }
    }

    public void unregisterAll(Collection<Listener> listeners) {
        for (Listener listener : listeners) {
            unregister(listener);
        }
    }

    public void unregister(RegisteredListener listener) {
        if (listenerSlots.get(listener.getPriority()).remove(listener)) {
            handlers = null;
        }
    }

    public synchronized void bake() {
        if (handlers != null) {
            return;
        }
        List<RegisteredListener> entries = new ArrayList<RegisteredListener>();
        for (Map.Entry<EventPriority, List<RegisteredListener>> entry : listenerSlots.entrySet()) {
            entries.addAll(entry.getValue());
        }
        handlers = entries.toArray(new RegisteredListener[entries.size()]);
    }

    public RegisteredListener[] getRegisteredListeners() {
        RegisteredListener[] handlers;
        while ((handlers = this.handlers) == null) {
            bake();
        }
        return handlers;
    }

}
