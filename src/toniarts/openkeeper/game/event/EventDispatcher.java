/*
 * Copyright (C) 2014-2020 OpenKeeper
 *
 * OpenKeeper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenKeeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenKeeper.  If not, see <http://www.gnu.org/licenses/>.
 */
package toniarts.openkeeper.game.event;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ArchDemon
 */
public class EventDispatcher implements IEventDispatcher, IListenerProvider {

    private boolean enabled = true;
    private final Queue<IEvent> messages = new ArrayDeque<>();
    private final Map<Class, List<IListener>> listeners = new HashMap<>();

    private final static Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());

    public void addListener(IListener listener, Class... types) {
        for (Class type : types) {
            synchronized (listeners) {
                if (!listeners.containsKey(type)) {
                    listeners.put(type, new ArrayList<>());
                }

                listeners.get(type).add(listener);
            }
        }
    }

    public boolean addMessage(IEvent message) {
        synchronized (messages) {
            return messages.add(message);
        }
    }

    public void update(float tpf) {
        if (!enabled || messages.isEmpty()) {
            return;
        }

        synchronized (messages) {
            IEvent message = messages.poll();
            dispatch(message);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void dispatch(IEvent event) {

        for (IListener listener : getListenersForEvent(event)) {
            listener.onEvent(event);

            if (event instanceof IStoppableEvent
                    && ((IStoppableEvent) event).isPropagationStopped()) {
                break;
            }
        }
    }

    @Override
    public List<IListener> getListenersForEvent(IEvent event) {
        synchronized (listeners) {
            if (listeners.containsKey(event.getClass())) {
                return listeners.get(event.getClass());
            }

            LOGGER.log(Level.WARNING, "Unprocessed message {0}", event);
            return new ArrayList<>();
        }
    }

}
