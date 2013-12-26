package net.digiex.magiccarpet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;

/*
 * Magic Carpet 2.4 Copyright (C) 2012-2014 Android, Celtic Minstrel, xzKinGzxBuRnzx
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class Storage implements Serializable {
	private static final long serialVersionUID = -1853121983419602226L;

	private class CarpetEntry implements Serializable {
		private static final long serialVersionUID = -365252387220826161L;

		public transient Carpet carpet;

		public boolean hasCarpet = false;
		public boolean given = false;
		public boolean tools = false;
		public boolean autoRenew = false;
		public boolean oneTimeFee = false;
		public String autoPackage = null;
		public byte data = (byte) 0;

		public boolean crouch = MagicCarpet.getMagicConfig().getCrouch();
		public int lastSize = MagicCarpet.getMagicConfig().getCarpSize();
		public Material light = MagicCarpet.getMagicConfig().getLightMaterial();
		public boolean lightsOn = MagicCarpet.getMagicConfig().getGlowing();
		public Material thread = MagicCarpet.getMagicConfig()
				.getCarpetMaterial();
		public long time = MagicCarpet.getMagicConfig().getChargeTime();
	}

	private HashMap<String, CarpetEntry> carpets = new HashMap<String, CarpetEntry>();

	private CarpetEntry getEntry(Player player) {
		if (carpets.containsKey(player.getName())) {
			return carpets.get(player.getName());
		}
		return null;
	}

	public Iterable<Carpet> all() {
		return new Iterable<Carpet>() {
			@Override
			public Iterator<Carpet> iterator() {
				return new Iterator<Carpet>() {
					private Iterator<CarpetEntry> iter = carpets.values()
							.iterator();
					private CarpetEntry toRemove = null;

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public Carpet next() {
						toRemove = iter.next();
						return toRemove.carpet;
					}

					@Override
					public void remove() {
						if (toRemove == null) {
							throw new IllegalStateException();
						}
						if (toRemove.carpet != null) {
							toRemove.carpet.removeCarpet();
						}
						toRemove.carpet = null;
					}
				};
			}
		};
	}

	public void assign(Player player, Carpet carpet) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			entry = new CarpetEntry();
			carpets.put(player.getName(), entry);
		}
		if (entry.carpet != null) {
			entry.carpet.removeCarpet();
		}
		entry.carpet = carpet;
	}

	public Carpet getCarpet(Player player) {
		if (carpets.containsKey(player.getName())) {
			return carpets.get(player.getName()).carpet;
		}
		return null;
	}

	public void remove(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		if (entry.carpet != null) {
			entry.carpet.removeCarpet();
			entry.carpet = null;
		}
	}

	public void clear() {
		for (CarpetEntry entry : carpets.values()) {
			if (entry.carpet == null || !entry.carpet.isVisible()) {
				continue;
			}
			entry.carpet.removeCarpet();
		}
		carpets.clear();
	}

	public void update(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		if (entry.carpet == null) {
			entry.hasCarpet = false;
			return;
		}
		entry.lastSize = entry.carpet.getSize();
		entry.hasCarpet = entry.carpet.isVisible();
		entry.lightsOn = entry.carpet.hasLight();
		entry.thread = entry.carpet.getThread();
		entry.light = entry.carpet.getShine();
		entry.tools = entry.carpet.hasTools();
		entry.data = entry.carpet.getData();
	}

	public void checkCarpets() {
		for (CarpetEntry entry : carpets.values()) {
			if (!Helper.getHandler().getAcceptableCarpetMaterial()
					.contains(entry.thread)) {
				entry.thread = MagicCarpet.getMagicConfig().getCarpetMaterial();
			}
			if (!Helper.getHandler().getAcceptableLightMaterial()
					.contains(entry.light)) {
				entry.light = MagicCarpet.getMagicConfig().getLightMaterial();
			}
			if (entry.lastSize > MagicCarpet.getMagicConfig()
					.getMaxCarpetSize()) {
				entry.lastSize = MagicCarpet.getMagicConfig().getCarpSize();
			}
			if (entry.thread != MagicCarpet.getMagicConfig()
					.getCarpetMaterial()
					&& !MagicCarpet.getMagicConfig().getCustomCarpets()) {
				entry.thread = MagicCarpet.getMagicConfig().getCarpetMaterial();
			}
			if (entry.light != MagicCarpet.getMagicConfig().getLightMaterial()
					&& !MagicCarpet.getMagicConfig().getCustomLights()) {
				entry.light = MagicCarpet.getMagicConfig().getLightMaterial();
			}
			if (entry.lightsOn && !MagicCarpet.getMagicConfig().getLights()) {
				entry.lightsOn = false;
			}
			if (entry.tools && !MagicCarpet.getMagicConfig().getTools()) {
				entry.tools = false;
			}
			if (MagicCarpet.getVault().isEnabled()
					&& MagicCarpet.getMagicConfig().getCharge()) {
				if (MagicCarpet.getMagicConfig().getChargeTimeBased()) {
					if (entry.hasCarpet && entry.time <= 0L && !entry.given) {
						entry.hasCarpet = false;
					}
					if (MagicCarpet.getVault().getPackage(entry.autoPackage) == null) {
						entry.autoPackage = null;
						entry.autoRenew = false;
					}
				} else {
					if (entry.hasCarpet && !entry.oneTimeFee && !entry.given) {
						entry.hasCarpet = false;
					}
				}
			}
		}
	}

	public boolean crouches(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return MagicCarpet.getMagicConfig().getCrouch();
		}
		return entry.crouch;
	}

	public int getLastSize(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return MagicCarpet.getMagicConfig().getCarpSize();
		}
		return entry.lastSize;
	}

	public Material getMaterial(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return MagicCarpet.getMagicConfig().getCarpetMaterial();
		}
		return entry.thread;
	}

	public Material getLightMaterial(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return MagicCarpet.getMagicConfig().getLightMaterial();
		}
		return entry.light;
	}

	public boolean has(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.hasCarpet;
	}

	public boolean hasLight(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.lightsOn;
	}

	public void toggleCrouch(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		entry.crouch = !entry.crouch;
	}

	public boolean wasGiven(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.given;
	}

	public void setGiven(Player player, Boolean given) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			entry = new CarpetEntry();
			carpets.put(player.getName(), entry);
		}
		entry.given = given;
	}

	public boolean hasTools(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.tools;
	}

	public void setTime(Player player, Long time) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		entry.time = time;
	}

	public long getTime(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return MagicCarpet.getMagicConfig().getChargeTime();
		}
		return entry.time;
	}

	public boolean canAutoRenew(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.autoRenew;
	}

	public void setAutoRenew(Player player, Boolean renew) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		entry.autoRenew = renew;
	}

	public String getAutoPackage(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return null;
		}
		return entry.autoPackage;
	}

	public void setAutoPackage(Player player, String auto) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		entry.autoPackage = auto;
	}

	public boolean hasPaidFee(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return false;
		}
		return entry.oneTimeFee;
	}

	public void setPaidFee(Player player, Boolean paid) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			entry = new CarpetEntry();
			carpets.put(player.getName(), entry);
		}
		entry.oneTimeFee = paid;
	}

	public byte getData(Player player) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return (byte) 0;
		}
		return entry.data;
	}

	public void setData(Player player, byte data) {
		CarpetEntry entry = getEntry(player);
		if (entry == null) {
			return;
		}
		entry.data = data;
	}
}
