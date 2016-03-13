package kr.nulli.survivalbingo.listeners;

import kr.nulli.survivalbingo.BingoManager;
import kr.nulli.survivalbingo.SurvivalBingo;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BingoGUI implements Listener {
	private SurvivalBingo plugin;
	private int[][] slots;
	private List<UUID> users = new ArrayList<UUID>();

	public BingoGUI(SurvivalBingo plugin) {
		this.plugin = plugin;

		// 빙고판 GUI의 카드 슬롯을 설정한다.
		slots = new int[5][5];

		int slot = 2;
		slotSetting: for (int i = 0; i < 5; i ++) {
			for (int j = 0; j < 5; j ++) {
				slots[i][j] = slot ++;

				if (slot > 42)
					break slotSetting;

				if (slot % 9 == 7)
					slot += 4;
			}
		}
	}

	public void openGUI(Player player) {
		Inventory inv = plugin.getServer().createInventory(player, 9 * 5, plugin.getDescription().getFullName());

		ItemStack rails = createItemStack(Material.RAILS, 1, (short) 0, " ", null);
		for (int i = 1; i <= 37; i += 9)
			inv.setItem(i, rails);
		for (int i = 7; i <= 43; i += 9)
			inv.setItem(i, rails);

		int goal = plugin.getBingoManager().getGoalBingoCount();

		inv.setItem(8, createItemStack(Material.SIGN, 1, (short) 0, "§b§n게임 방법",
				new String[]{
						"§7빙고판을 채우기 위해", "§7아이템을 먼저 모으세요!", "",
						"§eSurvival:", "§7먼저 " + goal + " 빙고를 달성하면 승리", "",
						"§cTeam:", "§7먼저 " + goal + "빙고를 달성하거나", "§713점을 획득하면 승리"
				}));
		inv.setItem(44, createItemStack(Material.YELLOW_FLOWER, 1, (short) 0, "§eDeveloped by §lTeam Nulli", null));

		if (plugin.getBingoManager().hasEmptyGrid()) {
			inv.setItem(22, createItemStack(Material.BARRIER, 1, (short) 0, "§c게임이 시작되지 않았습니다.", null));
		} else {
			if (plugin.getBingoManager().getMatchType() == BingoManager.MatchType.TEAM) {
				int bingoRed = plugin.getBingoManager().getBingoCount(BingoManager.Team.RED);
				int bingoBlue = plugin.getBingoManager().getBingoCount(BingoManager.Team.BLUE);

				inv.setItem(0, createItemStack(Material.WOOL, bingoRed, (short) 14, "§cRED: " + bingoRed + " bingo", null));
				inv.setItem(9, createItemStack(Material.WOOL, bingoBlue, (short) 11, "§bBLUE: " + bingoBlue + " bingo", null));
			}

			Boolean[][] playerGrid = plugin.getBingoManager().getGrid(player);
			Boolean[][] redGrid = plugin.getBingoManager().getGrid(BingoManager.Team.RED);
			Boolean[][] blueGrid = plugin.getBingoManager().getGrid(BingoManager.Team.BLUE);
			ItemStack[][] itemStacks = plugin.getBingoManager().getBingoGrid();
			BingoManager.MatchType matchType = plugin.getBingoManager().getMatchType();

			for (int i = 0; i < 5; i ++) {
				for (int j = 0; j < 5; j ++) {
					ItemStack itemStack = itemStacks[i][j].clone();

					if (matchType == BingoManager.MatchType.SURVIVAL) {
						// 플레이어가 달성한 아이템이 있는지 확인한다.
						if (playerGrid != null && playerGrid[i][j]) {
							setGrow(itemStack);
							setDisplayName(itemStack, "§e§lACHIEVED!");
						}
					} else if (matchType == BingoManager.MatchType.TEAM) {
						if (redGrid != null && redGrid[i][j]) {
							itemStack.setType(Material.STAINED_GLASS_PANE);
							itemStack.setDurability((short) 14);

							setGrow(itemStack);
							setDisplayName(itemStack, "§c§lRED ACHIEVED!");
						} else if (blueGrid != null && blueGrid[i][j]) {
							itemStack.setType(Material.STAINED_GLASS_PANE);
							itemStack.setDurability((short) 11);

							setGrow(itemStack);
							setDisplayName(itemStack, "§b§lBLUE ACHIEVED!");
						}
					}

					inv.setItem(slots[i][j], itemStack);
				}
			}
		}

		users.add(player.getUniqueId());
		player.openInventory(inv);
	}

	@EventHandler
	public  void onInventoryClick(InventoryClickEvent event) {
		if (users.contains(event.getWhoClicked().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (users.contains(event.getPlayer().getUniqueId())) {
			users.remove(event.getPlayer().getUniqueId());
		}
	}

	public ItemStack createItemStack(Material type, int amount, short durability, String displayName, String[] lore) {
		ItemStack itemStack = new ItemStack(type, amount, durability);
		ItemMeta meta = itemStack.getItemMeta();

		if (displayName != null)
			meta.setDisplayName(displayName);

		if (lore != null)
			meta.setLore(Arrays.asList(lore));

		meta.addItemFlags(ItemFlag.values());
		itemStack.setItemMeta(meta);

		return itemStack;
	}

	public void setGrow(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();

		meta.addItemFlags(ItemFlag.values());
		itemStack.setItemMeta(meta);

		if (itemStack.getType() != Material.GOLDEN_APPLE) {
			itemStack.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 100);
		} else {
			// 황금 사과는 인첸트가 있어도 반짝이지 않으므로
			itemStack.setDurability((short) 1);
		}
	}

	public void setDisplayName(ItemStack itemStack, String displayName) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(displayName);

		itemStack.setItemMeta(meta);
	}
}
