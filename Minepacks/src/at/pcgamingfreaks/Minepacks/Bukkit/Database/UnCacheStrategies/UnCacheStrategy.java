/*
 *   Copyright (C) 2023 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies;

import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

public abstract class UnCacheStrategy
{
	protected Database cache;

	public UnCacheStrategy(Database cache)
	{
		this.cache = cache;
	}

	public static UnCacheStrategy getUnCacheStrategie(Database cache)
	{
		switch(Minepacks.getInstance().getConfiguration().getUnCacheStrategy())
		{
			case "ondisconnect": return new OnDisconnect(cache);
			case "ondisconnectdelayed": return new OnDisconnectDelayed(cache);
			case "intervalChecked": return new IntervalChecked(cache);
			case "interval": default: return new Interval(cache);
		}
	}

	public void close()
	{
		cache = null;
	}
}