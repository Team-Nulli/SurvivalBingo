package kr.nulli.survivalbingo;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class BingoManager {
	private SurvivalBingo plugin;
	private Random random = new Random();
	private List<ItemStack> itemStackList = new ArrayList<ItemStack>();

	private boolean isPlaying;
	private MatchType matchType;
	private SurvivalBingo.Difficulty difficulty;
	private ItemStack[][] bingoGrid = null;
	private Map<Object, Boolean[][]> bingoMap = new HashMap<Object, Boolean[][]>();
	private Set<UUID> participants = new HashSet<UUID>();
	private Map<Team, List<UUID>> teamMap = new HashMap<Team, List<UUID>>();
	private Map<UUID, Location> respawnMap = new HashMap<UUID, Location>();

	private boolean onStartHeal, onStartFeed, onStartRemovePotionEffects, onStartClearInventory;

	public BingoManager(SurvivalBingo plugin) {
		this.plugin = plugin;
		this.matchType = MatchType.SURVIVAL;
		this.difficulty = SurvivalBingo.Difficulty.EASY;

		this.teamMap.put(Team.RED, new ArrayList<UUID>());
		this.teamMap.put(Team.BLUE, new ArrayList<UUID>());

		this.onStartHeal = plugin.getConfig().getBoolean("on-start.heal");
		this.onStartFeed = plugin.getConfig().getBoolean("on-start.feed");
		this.onStartRemovePotionEffects = plugin.getConfig().getBoolean("on-start.remove-potion-effects");
		this.onStartClearInventory = plugin.getConfig().getBoolean("on-start.clear-inventory");
	}

	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public void setDifficulty(SurvivalBingo.Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public SurvivalBingo.Difficulty getDifficulty() {
		return difficulty;
	}

	public void setItemStackList(List<ItemStack> itemStackList) {
		this.itemStackList = itemStackList;
	}

	public void startGame(Player starter) {
		if (isPlaying)
			return;

		broadcastStart();

		if (matchType == MatchType.SURVIVAL) {
			for (UUID uuid : participants) {
				Player player = plugin.getServer().getPlayer(uuid);

				if (onStartHeal) {
					player.setHealth(player.getMaxHealth());
				}
				if (onStartFeed) {
					player.setFoodLevel(20);
					player.setSaturation(10);
				}
				if (onStartRemovePotionEffects) {
					for (PotionEffect potionEffect : player.getActivePotionEffects()) {
						player.removePotionEffect(potionEffect.getType());
					}
				}
				if (onStartClearInventory) {
					player.getInventory().clear();
				}

				bingoMap.put(player.getUniqueId(), createGrid());

				if (getTeleportLevel() != 0) {
					Location location = getRandomLocation(starter.getLocation());

					// 텔레포트 좌표가 너무 멀 경우 빠짐 방지를 위해 반복적으로 텔레포트한다.
					if (player.getLocation().distance(location) >= 100.0D) {
						teleportRepeatedly(player, location);
					}

					player.teleport(location);
					respawnMap.put(player.getUniqueId(), location);
				}
			}
		} else if (matchType == MatchType.TEAM) {
			bingoMap.put(Team.RED, createGrid());
			bingoMap.put(Team.BLUE, createGrid());

			// 팀이 정해지지 않은 참가자들은 자동으로 팀이 정해진다.
			sortTeam();

			// 팀 인원수 차이가 설정된 값보다 클 경우 게임 시작이 중지된다.
			int maxDiff = getMaxTeamDifference();
			if (maxDiff != -1) {
				int diff = Math.abs(teamMap.get(Team.RED).size() - teamMap.get(Team.BLUE).size());

				if (diff > maxDiff) {
					bingoMap.clear();
					teamMap.clear();

					plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
					plugin.getServer().broadcastMessage(plugin.prefix + "");
					plugin.getServer().broadcastMessage(plugin.prefix + "팀 인원의 차이가 " + maxDiff + "명 이상입니다. 게임이 시작되지 않습니다.");
					plugin.getServer().broadcastMessage(plugin.prefix + "");
					plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
					return;
				}
			}

			if (getTeleportLevel() != 0) {
				Location redLoc = getRandomLocation(starter.getLocation());
				Location blueLoc = getRandomLocation(starter.getLocation());

				for (UUID uuid : participants) {
					Player player = plugin.getServer().getPlayer(uuid);
					Location location;

					if (getTeam(player) == Team.RED) {
						location = redLoc;
					} else if (getTeam(player) == Team.BLUE) {
						location = blueLoc;
					} else {
						continue;
					}

					// 텔레포트 좌표가 너무 멀 경우 빠짐 방지를 위해 반복적으로 텔레포트한다.
					if (player.getLocation().distance(location) >= 100.0D) {
						teleportRepeatedly(player, location);
					}

					player.teleport(location);
					respawnMap.put(player.getUniqueId(), location);
				}
			}
		}

		generateBingoGrid();
		isPlaying = true;
	}

	public void stopGame() {
		if (!isPlaying)
			return;

		isPlaying = false;
		bingoGrid = null;
		bingoMap.clear();
		participants.clear();
		teamMap.put(Team.RED, new ArrayList<UUID>());
		teamMap.put(Team.BLUE, new ArrayList<UUID>());
		respawnMap.clear();
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public List<ItemStack> getItemStackList() {
		return this.itemStackList;
	}

	public boolean hasEmptyGrid() {
		return bingoGrid == null;
	}

	public ItemStack[][] getBingoGrid() {
		return bingoGrid;
	}

	public void generateBingoGrid() {
		bingoGrid = new ItemStack[5][5];

		Collections.shuffle(itemStackList);

		int index = 0;
		for (int i = 0; i < 5; i ++) {
			for (int j = 0; j < 5; j ++) {
				bingoGrid[i][j] = itemStackList.get(index);
				index ++;
			}
		}
	}

	public Boolean[][] createGrid() {
		Boolean[][] grid = new Boolean[5][5];

		for (int i = 0; i < 5; i ++) {
			for (int j = 0; j < 5; j ++) {
				grid[i][j] = false;
			}
		}

		return grid;
	}

	public Boolean[][] getGrid(OfflinePlayer player) {
		if (matchType == MatchType.SURVIVAL)
			return (bingoMap.containsKey(player.getUniqueId())) ? bingoMap.get(player.getUniqueId()) : null;
		else if (matchType == MatchType.TEAM)
			return getGrid(getTeam(player));

		return null;
	}

	public Boolean[][] getGrid(Team team) {
		return (bingoMap.containsKey(team)) ? bingoMap.get(team) : null;
	}

	private void setGrid(OfflinePlayer player, Boolean[][] grid) {
		if (matchType == MatchType.SURVIVAL)
			bingoMap.put(player.getUniqueId(), grid);
		else if (matchType == MatchType.TEAM)
			bingoMap.put(getTeam(player), grid);
	}

	public int getBingoCount(OfflinePlayer player) {
		Boolean[][] grid = getGrid(player);
		return getBingoCount(grid);
	}

	public int getBingoCount(Team team) {
		Boolean[][] grid = getGrid(team);
		return getBingoCount(grid);
	}

	private int getBingoCount(Boolean[][] grid) {
		if (grid == null) {
			throw new IllegalArgumentException("The bingo grid cannot be null.");
		}

		int bingoCount = 0;

		// 가로, 세로로 해당되는 빙고를 센다.
		for (int i = 0; i < 5; i ++) {
			int widthCount = 0;
			int heightCount = 0;
			
			for (int j = 0; j < 5; j ++) {
				if (grid[i][j]) {
					widthCount ++;
				}

				if (grid[j][i]) {
					heightCount ++;
				}
			}

			if (widthCount == 5)
				bingoCount ++;
			if (heightCount == 5)
				bingoCount ++;
		}


		// 대각선으로 해당되는 빙고를 센다.
		int diagonalLeftCount = 0;
		int diagonalRightCount = 0;
		for (int i = 0; i < 5; i ++) {
			if (grid[i][4 - i]) {
				diagonalLeftCount ++;
			}

			if (grid[4 - i][i]) {
				diagonalRightCount ++;
			}
		}

		if (diagonalLeftCount == 5)
			bingoCount ++;
		if (diagonalRightCount == 5)
			bingoCount ++;

		return bingoCount;
	}

	public int getBingoScore(Team team) {
		Boolean[][] grid = getGrid(team);

		if (grid == null) {
			throw new IllegalArgumentException("The bingo grid cannot be null.");
		}

		int score = 0;
		for (int i = 0; i < 5; i ++) {
			for (int j = 0; j < 5; j ++) {
				if (grid[i][j]) {
					score ++;
				}
			}
		}

		return score;
	}

	public void addParticipant(OfflinePlayer player) {
		participants.add(player.getUniqueId());
	}

	public void removeParticipant(OfflinePlayer player) {
		participants.remove(player.getUniqueId());
	}

	public boolean isParticipant(OfflinePlayer player) {
		return participants.contains(player.getUniqueId());
	}

	public int getPartaicipantSize() {
		return participants.size();
	}

	public Set<UUID> getPartaicipant() {
		return participants;
	}

	public void addPlayerToTeam(OfflinePlayer player, Team team) {
		if (!teamMap.containsKey(team)) {
			teamMap.put(team, new ArrayList<UUID>());
		}

		teamMap.get(team).add(player.getUniqueId());
	}

	public void removePlayerFromTeam(OfflinePlayer player) {
		Team team = getTeam(player);

		if (team == null)
			return;

		teamMap.get(team).remove(player.getUniqueId());
	}

	public void setTeam(Team team, List<UUID> players) {
		teamMap.remove(team);
		teamMap.put(team, players);
	}

	public Team getTeam(OfflinePlayer player) {
		UUID id = player.getUniqueId();
		return (teamMap.get(Team.RED).contains(id)) ? Team.RED : (teamMap.get(Team.BLUE).contains(id)) ? Team.BLUE : null;
	}

	public List<UUID> getTeam(Team team) {
		return teamMap.get(team);
	}

	public Location getRespawnLocation(OfflinePlayer player) {
		return respawnMap.get(player.getUniqueId());
	}

	public int getMaxTeamDifference() {
		return plugin.getConfig().getInt("max-team-difference");
	}

	public int getGoalBingoCount() {
		return plugin.getConfig().getInt("goal-bingo-count");
	}

	public void achieveItem(Player player, ItemStack itemStack) {
		Boolean[][] grid = getGrid(player);

		if (grid == null)
			return;

		// 빙고판에 해당 아이템이 존재하는지 확인한다.
		boolean containsItemStack = false;
		int width = 0, height = 0;
		checkItemStack:
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (bingoGrid[i][j].getType().equals(itemStack.getType()) && bingoGrid[i][j].getDurability() == itemStack.getDurability()) {
					containsItemStack = true;
					width = i;
					height = j;
					break checkItemStack;
				}
			}
		}

		if (!containsItemStack)
			return;

		// 이미 해당 아이템 획득이 기록되어 있다면 처리하지 않는다.
		if (getMatchType() == MatchType.TEAM) {
			for (Team team : Team.values()) {
				Boolean[][] teamGrid = getGrid(team);

				if (teamGrid[width][height]) {
					return;
				}
			}
		} else {
			if (grid[width][height]) {
				return;
			}
		}

		// 빙고판에 아이템 획득을 기록한다.
		grid[width][height] = true;
		setGrid(player, grid);

		String name = ((matchType == MatchType.TEAM) ? ((getTeam(player) == Team.RED) ? "§c" : "§b") : "§e")
				+ player.getName();
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + name + "§r님께서 빙고판의 아이템을 획득했습니다.");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");


		// 빙고 개수가 일정량을 초과할 경우 게임에서 승리한다.
		int bingoCount = getBingoCount(player);
		int goal = getGoalBingoCount();

		if (bingoCount >= goal) {
			endGame(player);
			return;
		}

		// 팀전에서 획득한 아이템의 개수가 13개 이상일 경우 승리한다.
		if (matchType == MatchType.TEAM) {
			int score = getBingoScore(getTeam(player));

			if (score >= 13) {
				endGame(player);
			}
		}
	}

	private void sortTeam() {
		for (UUID id : participants) {
			OfflinePlayer player = plugin.getServer().getOfflinePlayer(id);

			if (getTeam(player) != null)
				continue;

			int red = teamMap.get(Team.RED).size();
			int blue = teamMap.get(Team.BLUE).size();

			Team team = ((red > blue) ? Team.BLUE : ((red < blue) ? Team.RED : Team.getRandomTeam()));
			addPlayerToTeam(player, team);

			if (player.isOnline()) {
				player.getPlayer().sendMessage(plugin.prefix + "당신은 " + team.getDisplayName() + "§r입니다!");
			}
		}
	}

	private int getTeleportLevel() {
		return plugin.getConfig().getInt("teleport-level");
	}

	private Location getRandomLocation(Location centerLoc) {
		int level = getTeleportLevel();
		int maxDistance = 0;

		switch (level) {
			case 0:
				return null;
			case 1:
				maxDistance = 100;
				break;
			case 2:
				maxDistance = 1000;
				break;
			case 3:
				maxDistance = 10000;
				break;
		}

		int x = maxDistance - random.nextInt(maxDistance * 2);
		int z = maxDistance - random.nextInt(maxDistance * 2);

		Location loc = centerLoc.clone().add(x, 0, z);

		return loc.getWorld().getHighestBlockAt(loc).getLocation();
	}

	private void teleportRepeatedly(final Player player, final Location location) {
		for (int i = 1; i <= 3; i ++) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					player.teleport(location);
				}
			}, i * 20L);
		}
	}

	private void endGame(Player winner) {
		broadcastWinner(winner);
		stopGame();

		final Location loc = winner.getLocation();
		for (int i = 1; i <= 10; i ++) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					int x = 2 - random.nextInt(5);
					int z = 2 - random.nextInt(5);

					spawnFireworks(loc.clone().add(x, 0, z));
				}
			}, i * 5L);
		}
	}

	private void spawnFireworks(Location loc) {
		Firework firework = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta meta = firework.getFireworkMeta();
		FireworkEffect effect = FireworkEffect.builder()
				.with(FireworkEffect.Type.BALL)
				.withColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
				.trail(random.nextBoolean())
				.flicker(random.nextBoolean())
				.build();
		meta.addEffect(effect);
		meta.setPower(random.nextInt(3));
		firework.setFireworkMeta(meta);
	}

	public void broadcastStop(String name) {
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + name + "님이 빙고 게임이 중지시키셨습니다!");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
	}

	private void broadcastStart() {
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "§a§l>> Bingo Game has started!");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "빙고 게임이 시작되었습니다!");
		plugin.getServer().broadcastMessage(plugin.prefix + "§e/빙고판§r의 아이템을 모아 빙고를 완성하세요.");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "§7Plugin Developed by Nulli Team");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
	}

	private void broadcastWinner(Player player) {
		String winner = "";

		if (matchType == MatchType.SURVIVAL)
			winner = "§e§l" + player.getName();
		else if (matchType == MatchType.TEAM)
			winner = getTeam(player).getDisplayName();

		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "§a§l>> "+winner+"§a§l has won the game!");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "빙고 게임이 종료되었습니다.");
		plugin.getServer().broadcastMessage(plugin.prefix + "플레이 해주셔서 감사합니다!");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "§7Plugin Developed by Nulli Team");
		plugin.getServer().broadcastMessage(plugin.prefix + "");
		plugin.getServer().broadcastMessage(plugin.prefix + "===============================");
	}

	public enum MatchType {
		SURVIVAL("서바이벌"), TEAM("팀");

		private final String displayName;

		MatchType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum Team {
		RED("§c레드§r", "§c§lRED Team§r", "§c[RED]§r "),
		BLUE("§b블루§r", "§b§lBLUE Team§r", "§b[BLUE]§r ");

		private static Random random = new Random();
		private final String name;
		private final String displayName;
		private final String prefix;

		Team(String name, String displayName, String prefix) {
			this.name = name;
			this.displayName = displayName;
			this.prefix = prefix;
		}

		public String getName() {
			return name;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getPrefix() {
			return prefix;
		}

		public static Team getRandomTeam() {
			return values()[random.nextInt(values().length)];
		}
	}
}
