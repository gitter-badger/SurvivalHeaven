/**
 * This file is part of survivalheaven.org, licensed under the MIT License (MIT).
 *
 * Copyright (c) SurvivalHeaven.org <http://www.survivalheaven.org>
 * Copyright (c) NordByen.info <http://www.nordbyen.info>
 * Copyright (c) l0lkj.info <http://www.l0lkj.info>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.nordbyen.survivalheaven.api.util;

import org.bukkit.Achievement;
import org.bukkit.entity.Player;

/**
 * The Class PlayerAPI.
 */
public class PlayerAPI {

	/**
	 * Adds the health.
	 * 
	 * @param player
	 *            the player
	 * @param amount
	 *            the amount
	 * @return true, if successful
	 */
	public boolean addHealth(final Player player, final int amount) {
		final double health = player.getHealth();
		if (health == 20.0D) {
			player.setHealth(20.0D);
			return true;
		}
		if (health > (20 - amount)) {
			player.setHealth(20.0D);
			return true;
		}
		if (health < (20 - amount)) {
			player.setHealth(health + amount);
			return true;
		}
		return false;
	}

	/**
	 * Award.
	 * 
	 * @param player
	 *            the player
	 * @param achievement
	 *            the achievement
	 */
	public void award(final Player player, final Achievement achievement) {
		player.awardAchievement(achievement);
	}

	/**
	 * Burn.
	 * 
	 * @param player
	 *            the player
	 */
	public void burn(final Player player) {
		player.setFireTicks(20);
	}

	/**
	 * Burn.
	 * 
	 * @param player
	 *            the player
	 * @param ticks
	 *            the ticks
	 */
	public void burn(final Player player, final int ticks) {
		player.setFireTicks(ticks);
	}

	/**
	 * Extinguish.
	 * 
	 * @param player
	 *            the player
	 */
	public void extinguish(final Player player) {
		player.setFireTicks(0);
	}

	/**
	 * Kill.
	 * 
	 * @param player
	 *            the player
	 */
	public void kill(final Player player) {
		player.setHealth(0.0D);
	}

	/**
	 * Removes the health.
	 * 
	 * @param player
	 *            the player
	 * @param amount
	 *            the amount
	 * @return true, if successful
	 */
	public boolean removeHealth(final Player player, final int amount) {
		final double health = player.getHealth();
		if (health > (health - amount)) {
			player.setHealth(health - amount);
			return true;
		}
		if (health < (health - amount)) {
			player.setHealth(0.0D);
			return true;
		}
		return false;
	}
}
