package kr.nulli.survivalbingo;

import kr.nulli.survivalbingo.listeners.BingoGUI;
import kr.nulli.survivalbingo.listeners.PlayerListener;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SurvivalBingo extends JavaPlugin {
	public final String prefix = "§9[SurvivalBingo]§r ";
	private BingoManager bingoManager;
	private BingoGUI bingoGUI;

	@Override
	public void onEnable() {
		bingoManager = new BingoManager(this);

		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(bingoGUI = new BingoGUI(this), this);
		manager.registerEvents(new PlayerListener(this), this);

		initCommands();

		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
			reloadConfig();
		}
		getLogger().info(getDescription().getFullName() + " has been enabled successfully!");
	}

	@Override
	public void onDisable() {
		getLogger().info(getDescription().getFullName() + " has been disabled.");
	}

	public void initCommands() {
		getCommand("빙고").setExecutor(new BingoCommands(this));
		getCommand("빙고판").setExecutor(new BingoCommands(this));
	}

	public BingoManager getBingoManager() {
		return bingoManager;
	}
	public BingoGUI getBingoGUI() {
		return bingoGUI;
	}

	public void loadItems(Difficulty difficulty) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		List<String> itemStringList = getConfig().getStringList("bingo-item-list." + difficulty.getConfigName());

		for (String sitem : itemStringList) {
			ItemStack item;

			String item_name = sitem.contains(":") ? sitem.split(":")[0] : sitem;
			int item_name_int;
			try { item_name_int = Integer.parseInt(item_name); }
			catch (Exception e) { item_name_int = -1; }

			if (item_name_int == -1)
				item = new ItemStack(Material.getMaterial(item_name));
			else
				item = new ItemStack(Material.getMaterial(item_name_int));

			if (sitem.contains(":")) {
				Short durability = Short.parseShort(sitem.split(":")[1]);
				item.setDurability(durability);
			}

			items.add(item);
		}

		getBingoManager().setItemStackList(items);
	}

	public static enum Difficulty {
		EASY("EASY", "easy"), HARD("HARD", "hard"), INSANE("INSANE", "insane");

		private final String displayName, configName;

		private Difficulty(String displayName, String configName) {
			this.displayName = displayName;
			this.configName = configName;
		}

		public String getConfigName() {
			return configName;
		}
		public String getDisplayName() {
			return displayName;
		}
	}
}
