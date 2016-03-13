package kr.nulli.survivalbingo.listeners;

import kr.nulli.survivalbingo.BingoManager;
import kr.nulli.survivalbingo.SurvivalBingo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Created by horyu1234 on 2015-11-19.
 */
public class PlayerListener implements Listener {
	private SurvivalBingo plugin;
	private boolean chatPrefix, freezeFoodLevel;

	public PlayerListener(SurvivalBingo plugin) {
		this.plugin = plugin;
		this.chatPrefix = plugin.getConfig().getBoolean("chat-prefix");
		this.freezeFoodLevel = plugin.getConfig().getBoolean("freeze-food-level");
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		if (plugin.getBingoManager().isPlaying() && plugin.getBingoManager().isParticipant(event.getPlayer())) {
			Location location = plugin.getBingoManager().getRespawnLocation(event.getPlayer());
			event.setRespawnLocation(location);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (chatPrefix &&
				plugin.getBingoManager().getPartaicipant().contains(e.getPlayer().getUniqueId()) &&
				plugin.getBingoManager().getMatchType().equals(BingoManager.MatchType.TEAM)) {
			BingoManager.Team team = plugin.getBingoManager().getTeam(e.getPlayer());

			e.setFormat(team.getPrefix() + e.getFormat());
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		plugin.getBingoManager().achieveItem(e.getPlayer(), e.getItem().getItemStack().clone());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		InventoryType type = e.getInventory().getType();

		if (plugin.getBingoManager().isPlaying() && type != InventoryType.CREATIVE && type != InventoryType.PLAYER) {
			if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
				plugin.getBingoManager().achieveItem((Player) e.getWhoClicked(), e.getCurrentItem().clone());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() != null && !e.getItem().getType().equals(Material.AIR)) {
			plugin.getBingoManager().achieveItem(e.getPlayer(), e.getItem().clone());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();

		if (freezeFoodLevel && plugin.getBingoManager().isPlaying() && plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
			player.setFoodLevel(20);
			player.setSaturation(10);
		}
	}
}