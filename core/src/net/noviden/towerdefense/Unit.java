/**
 TowerDefense : Infinite Tower Defense Game With User Created Maps
 Copyright (C) 2015 Tristan Kernan

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.noviden.towerdefense;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Unit {
    private static final float BASE_RADIUS = 10.0f;
    private static final float BASE_SPEED = 100.0f;
    private static final int BASE_WORTH = 20;

    private float health, maxHealth;
    private float damage;
    public Point location;
    public float radius;

    private Path path;
    private int currentDestinationIndex;
    private float xVelocity, yVelocity;
    private float percentSlowed, timeSlowed;

    private int worth;

    private float rotation;

    public Unit(float health, float damage, Path path) {
        this.health = this.maxHealth = health;
        this.damage = damage;
        this.path = path;

        // initial location is equal to 1st position in path set, and destination the 2nd in set
        this.location = path.set.get(0).clone();
        this.currentDestinationIndex = 1;
        this.radius = BASE_RADIUS;
        this.worth = BASE_WORTH;

        this.rotation = 0.0f;

        // set initial xVel and yVel based on destination
        Point destination = path.set.get(currentDestinationIndex);

        float distanceBetween = (float) Math.sqrt(
                Math.pow(location.x - destination.x, 2) + Math.pow(location.y - destination.y, 2));

        this.xVelocity = (destination.x - location.x) / distanceBetween;
        this.yVelocity = (destination.y - location.y) / distanceBetween;
    }

    public void act(float deltaTime, Player player) {
        // move along line from by currentLocation -> currentDestination until arrive at
        // destination, then set new destination

        // set the rotation factor for our little guys
        rotation += deltaTime * 108.0f;
        if (rotation > 360.0f) {
            rotation = 0.0f;
        }

        // count down slow timer
        if (timeSlowed >= 0.0f) {
            timeSlowed -= deltaTime;
        } else {
            percentSlowed = 0.0f;
        }

        Point destination = path.set.get(currentDestinationIndex);

        // base case: time to find a new destination
        if (Math.abs(location.x - destination.x) < 1.2f &&
                Math.abs(location.y - destination.y) < 1.2f) {
            // close enough! on to the next destination
            currentDestinationIndex++;

            // check for end of the line
            if (currentDestinationIndex >= path.set.size()) {
                player.decreaseHealth(this.damage);
                this.health = -1.0f;
            } else {
                // recalculate xVel and yVel
                destination = path.set.get(currentDestinationIndex);

                float distanceBetween = (float) Math.sqrt(
                        Math.pow(location.x - destination.x, 2) + Math.pow(location.y - destination.y, 2));

                this.xVelocity = (destination.x - location.x) / distanceBetween;
                this.yVelocity = (destination.y - location.y) / distanceBetween;
            }
        }

        location.x += xVelocity * deltaTime * BASE_SPEED * (1.0f - percentSlowed);
        location.y += yVelocity * deltaTime * BASE_SPEED * (1.0f - percentSlowed);
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // draw each unit's health as a percent of its circle
        float percentHealthMissing = this.health / this.maxHealth;
        float degrees = percentHealthMissing * 360.0f;

        shapeRenderer.setColor(Color.PURPLE);
        shapeRenderer.circle(location.x, location.y, BASE_RADIUS);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.arc(location.x, location.y, BASE_RADIUS, rotation, degrees);

    }

    public void takeDamage(float amount) {
        this.health -= amount;
    }

    public void slowDown(float timeSlowed, float percentSlowed) {
        this.timeSlowed = timeSlowed;

        // always slow unit by higher factor, never decrease
        if (this.percentSlowed < percentSlowed) {
            this.percentSlowed = percentSlowed;
        }
    }

    public boolean isDead() {
        return (health < 0.0f);
    }

    public int getWorth() {
        return this.worth;
    }
}