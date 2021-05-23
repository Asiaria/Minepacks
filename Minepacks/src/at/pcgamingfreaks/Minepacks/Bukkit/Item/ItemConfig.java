/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Item;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Util.HeadUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;

@Getter
public final class ItemConfig
{
	private final @NotNull String name;
	private final @NotNull Material material;
	private final @NotNull String displayName;
	private final @Nullable List<String> lore;
	private final int amount, model;
	private final @Nullable String value;
	private final @NotNull IItemProducer producer;
	@Getter @Setter private @Nullable Object databaseKey;

	public ItemConfig(final @NotNull String name, final @NotNull String material, final int amount, final @NotNull String displayName, final @Nullable List<String> lore, int model, final @Nullable String value) throws IllegalArgumentException
	{
		assert model < 0;
		this.name = name;
		this.amount = amount;
		if(material.equalsIgnoreCase("player_head"))
		{
			this.material = HeadUtils.HEAD_MATERIAL;
			model = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) ? model : -1;
			producer = (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_8) && value != null) ?  ItemProducerHead.INSTANCE : ItemProducerLegacy.INSTANCE;
		}
		else
		{
			this.material = getMaterialFromString(material);
			producer = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) ?  ItemProducerModelId.INSTANCE : ItemProducerLegacy.INSTANCE;
		}
		this.displayName = displayName;
		this.lore = lore;
		this.model = model;
		this.value = value;
	}

	public ItemConfig(final @NotNull String name, final @NotNull ItemConfig item)
	{
		this.name = name;
		this.material = item.material;
		this.displayName = item.displayName;
		this.lore = item.lore;
		this.amount = item.amount;
		this.model = item.model;
		this.value = item.value;
		this.producer = item.producer;
	}

	public @NotNull ItemStack make() { return producer.make(this); }

	public @NotNull ItemStack make(final int amount)
	{
		return producer.make(this, amount);
	}

	private static @NotNull Material getMaterialFromString(String name) throws IllegalArgumentException
	{
		name = name.toUpperCase(Locale.ENGLISH);
		Material mat = Material.getMaterial(name);
		if(mat == null && MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13)) mat = Material.getMaterial(name, true);
		//TODO from id
		if(mat == null) throw new IllegalArgumentException("Unable to find material: " + name);
		return mat;
	}
}