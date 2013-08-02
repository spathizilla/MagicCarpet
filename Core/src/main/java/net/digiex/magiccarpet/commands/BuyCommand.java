package net.digiex.magiccarpet.commands;

import java.util.Map.Entry;
import java.util.logging.Logger;

import net.digiex.magiccarpet.Carpets;
import net.digiex.magiccarpet.Config;
import net.digiex.magiccarpet.MagicCarpet;
import net.digiex.magiccarpet.lib.Vault;
import net.digiex.magiccarpet.lib.Vault.TimePackage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
 * Magic Carpet 2.3 Copyright (C) 2012-2013 Android, Celtic Minstrel, xzKinGzxBuRnzx
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
public class BuyCommand implements CommandExecutor {

	private final MagicCarpet plugin;

	public BuyCommand(MagicCarpet plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			if (!getVault().isEnabled()) {
				getLogger()
						.info("You have not enabled economy support in the config or the required dependencies are missing.");
				return true;
			}
			if (args.length == 2) {
				Player who = null;
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().toLowerCase()
							.contains(args[0].toLowerCase())
							|| p.getName().equalsIgnoreCase(args[0])) {
						who = p;
						break;
					}
				}
				try {
					long time = Long.valueOf(args[1]);
					getVault().addTime(who, time);
					getLogger().info("Time sent to " + who.getName() + ".");
					return true;
				} catch (NumberFormatException e) {
					getLogger().info("/mcb [player] [time]");
					return true;
				}
			}
			getLogger().info("/mcb [player] [time]");
			return true;
		} else {
			Player player = (Player) sender;
			if (!getVault().isEnabled()) {
				sender.sendMessage("Economy support is not enabled.");
				return true;
			}
			if (plugin.canNotPay(player)) {
				player.sendMessage("You don't need to use this. You have unlimited time to use MagicCarpet.");
				return true;
			}
			if (args.length == 0) {
				if (getConfig().getDefaultChargeTimeBased()) {
					player.sendMessage("You have " + getVault().getTime(player)
							+ " of time left.");
					if (getCarpets().canAutoRenew(player)) {
						player.sendMessage("You have auto-renew enabled for plan "
								+ getCarpets().getAutoPackage(player) + ".");
						return true;
					}
					if (getVault().get(player) == 0L) {
						player.sendMessage("You have ran out of time. Take a look a /mcb -l for a list of available plans you can purchase.");
					} else if (getVault().get(player) <= 300L) {
						player.sendMessage("You are running low on time. Take a look a /mcb -l for a list of available plans you can purchase.");
					}
				} else {
					if (!getCarpets().hasPaidFee(player)) {
						player.sendMessage("You need to pay a one time fee of "
								+ String.valueOf(getConfig()
										.getDefaultChargeAmount())
								+ " "
								+ getVault().getCurrencyNamePlural()
								+ " before you can use Magic Carpet. Use /mcb -b to accept this charge.");
						return true;
					}
				}
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("-l")) {
				if (getConfig().getDefaultChargeTimeBased()) {
					player.sendMessage("Here are some of the time packages currently available.");
					for (Entry<String, TimePackage> set : getVault()
							.getPackages().entrySet()) {
						TimePackage tp = set.getValue();
						player.sendMessage("Plan '" + set.getKey() + "' gives "
								+ getVault().getTime(tp.getTime())
								+ " and costs "
								+ getVault().format(tp.getAmount()) + ".");
					}
					player.sendMessage("Use /mcb to purchase a plan by typing its name in.");
				}
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) {
				if (getConfig().getDefaultChargeTimeBased()) {
					if (getVault().getPackage(
							getCarpets().getAutoPackage(player)) == null) {
						player.sendMessage("You have not activated auto-renew yet");
						return true;
					}
					if (getCarpets().canAutoRenew(player)) {
						getCarpets().setAutoRenew(player, false);
						player.sendMessage("You have disabled auto-renew.");
					} else {
						getCarpets().setAutoRenew(player, true);
						player.sendMessage("You have re-activated auto-renew.");
					}
				}
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("-b")) {
				if (getConfig().getDefaultChargeTimeBased()) {
					return true;
				}
				if (getCarpets().hasPaidFee(player)) {
					player.sendMessage("You've already paid the fee, use /mc!");
					return true;
				}
				if (getVault().hasEnough(player.getName(),
						getConfig().getDefaultChargeAmount())) {
					getVault().subtract(player.getName(),
							getConfig().getDefaultChargeAmount());
					getCarpets().setPaidFee(player, true);
					player.sendMessage("You have successfully paid the one time fee. Use /mc!");
					return true;
				} else {
					player.sendMessage("You don't have enough "
							+ getVault().getCurrencyNamePlural() + ".");
					return true;
				}
			} else if (args.length == 2 && args[1].equalsIgnoreCase("-a")) {
				if (getConfig().getDefaultChargeTimeBased()) {
					if (getVault().getPackage(args[0]) == null) {
						player.sendMessage("That plan doesn't exist");
						return true;
					}
					if (getCarpets().canAutoRenew(player)
							&& getCarpets().getAutoPackage(player) == args[0]) {
						player.sendMessage("You've already activated auto-renew for the "
								+ args[0] + " plan.");
						return true;
					}
					if (!getCarpets().canAutoRenew(player)) {
						getCarpets().setAutoRenew(player, true);
						getCarpets().setAutoPackage(player, args[0]);
						player.sendMessage("Auto-renew activated for plan "
								+ args[0] + ".");
						return true;
					}
				}
			} else if (args.length == 1) {
				if (getConfig().getDefaultChargeTimeBased()) {
					TimePackage tp = getVault().getPackage(args[0]);
					if (tp != null) {
						if (getVault().addTime(player, tp.getTime(),
								tp.getAmount())) {
							player.sendMessage("You have purchased plan "
									+ tp.getName() + " with "
									+ getVault().getTime(tp.getTime())
									+ " of time and was charged "
									+ getVault().format(tp.getAmount()) + ".");
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private Carpets getCarpets() {
		return plugin.getCarpets();
	}

	private Config getConfig() {
		return plugin.getMCConfig();
	}

	private Vault getVault() {
		return plugin.getVault();
	}

	private Logger getLogger() {
		return plugin.getLogger();
	}
}
