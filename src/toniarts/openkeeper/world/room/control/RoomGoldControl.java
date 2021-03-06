/*
 * Copyright (C) 2014-2016 OpenKeeper
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
package toniarts.openkeeper.world.room.control;

import com.jme3.math.Vector2f;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import toniarts.openkeeper.world.MapLoader;
import toniarts.openkeeper.world.ThingLoader;
import toniarts.openkeeper.world.creature.CreatureControl;
import toniarts.openkeeper.world.object.GoldObjectControl;
import toniarts.openkeeper.world.room.GenericRoom;

/**
 * Not really a JME control currently. Manages how the gold places in the room.
 * Should be generalized to provide a control for any type of object owned by
 * the room. Either these controls or decorator pattern, lets see...
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public abstract class RoomGoldControl extends RoomObjectControl<GoldObjectControl> {

    private int storedGold = 0;

    public RoomGoldControl(GenericRoom parent) {
        super(parent);
    }

    @Override
    public int addItem(int sum, Point p, ThingLoader thingLoader, CreatureControl creature) {
        if (p != null) {
            sum = putGold(sum, p, thingLoader);
        }
        if (sum > 0) {
            List<Point> coordinates = parent.getRoomInstance().getCoordinates();
            for (Point coordinate : coordinates) {
                if (parent.isTileAccessible(coordinate.x, coordinate.y)) {
                    sum = putGold(sum, coordinate, thingLoader);
                    if (sum == 0) {
                        break;
                    }
                }
            }
        }
        return sum;
    }

    private int putGold(int sum, Point p, ThingLoader thingLoader) {
        int pointStoredGold = 0;
        GoldObjectControl goldPile = objects.get(p);
        if (goldPile != null) {
            pointStoredGold = goldPile.getGold();
        }
        if (pointStoredGold < getObjectsPerTile()) {
            int goldToStore = Math.min(sum, getObjectsPerTile() - pointStoredGold);
            pointStoredGold += goldToStore;
            sum -= goldToStore;
            storedGold += goldToStore;

            // Add the visuals
            if (goldPile == null) {
                GoldObjectControl object = thingLoader.addRoomGold(p, parent.getRoomInstance().getOwnerId(), goldToStore, getObjectsPerTile());
                goldPile = object;
                objects.put(p, goldPile);
                object.setRoomObjectControl(this);
            } else {

                // Adjust the gold sum
                goldPile.setGold(pointStoredGold);
            }

            // Add gold to player
            parent.getWorldState().getGameState().getPlayer(parent.getRoomInstance().getOwnerId()).getGoldControl().addGold(goldToStore);
        }
        return sum;
    }

    @Override
    public int getCurrentCapacity() {
        return storedGold;
    }

    @Override
    public GenericRoom.ObjectType getObjectType() {
        return GenericRoom.ObjectType.GOLD;
    }

    @Override
    public void destroy() {
        List<Entry<Point, GoldObjectControl>> storedGoldList = new ArrayList<>(objects.entrySet());

        // Delete all gold
        removeAllObjects();

        // Create the loose gold
        if (!storedGoldList.isEmpty()) {
            ThingLoader thingLoader = parent.getWorldState().getThingLoader();
            for (Entry<Point, GoldObjectControl> entry : storedGoldList) {
                thingLoader.addLooseGold(entry.getKey(), new Vector2f(MapLoader.TILE_WIDTH / 2, MapLoader.TILE_WIDTH / 2), parent.getRoomInstance().getOwnerId(), entry.getValue().getGold());
            }
        }
    }

    @Override
    public void removeItem(GoldObjectControl object) {
        super.removeItem(object);

        // Substract the gold from the player
        parent.getWorldState().getGameState().getPlayer(parent.getRoomInstance().getOwnerId()).getGoldControl().subGold(object.getGold());
        storedGold -= object.getGold();
        if (object.getGold() == 0) {
            object.removeObject();
        }
    }

    /**
     * Remove amount of gold from this room
     *
     * @param amount the amount
     * @return the amount that can't be removed
     */
    public int removeGold(int amount) {
        List<GoldObjectControl> objectsToRemove = new ArrayList<>();
        for (GoldObjectControl goldObjectControl : objects.values()) {
            int goldToRemove = Math.min(goldObjectControl.getGold(), amount);
            amount -= goldToRemove;
            goldObjectControl.setGold(goldObjectControl.getGold() - goldToRemove);

            // Substract the gold from the player
            parent.getWorldState().getGameState().getPlayer(parent.getRoomInstance().getOwnerId()).getGoldControl().subGold(goldToRemove);
            storedGold -= goldToRemove;

            // Add to removal list if empty item
            if (goldObjectControl.getGold() == 0) {
                objectsToRemove.add(goldObjectControl);
            }

            if (amount == 0) {
                break;
            }
        }

        // Clean up, the amount of gold is already 0, so
        for (GoldObjectControl goldObjectControl : objectsToRemove) {
            removeItem(goldObjectControl);
        }

        return amount;
    }

}
