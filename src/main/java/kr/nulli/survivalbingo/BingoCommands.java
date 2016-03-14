package kr.nulli.survivalbingo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by horyu1234 on 2015-11-18.
 */
public class BingoCommands implements CommandExecutor {
	private SurvivalBingo plugin;

	public BingoCommands(SurvivalBingo plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equals("빙고판")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(plugin.prefix + "게임 내에서만 사용 가능합니다.");
				return false;
			}
			Player player = (Player) sender;

			if (!plugin.getBingoManager().isPlaying()) {
				sender.sendMessage(plugin.prefix+"게임이 진행중이 아닐때는 사용할 수 없습니다.");
				return false;
			}

			if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
				sender.sendMessage(plugin.prefix+"게임 참여자가 아닙니다.");
				return false;
			}

			plugin.getBingoGUI().openGUI(player);
			return true;
		} else if (label.equals("빙고")) {
			if (args.length > 0) {
				if (args[0].equals("참가")) {
					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix+"게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix+"게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
						plugin.getBingoManager().addParticipant(player);

						sender.sendMessage(plugin.prefix+"게임에 참가하셨습니다.");
						return true;
					} else sender.sendMessage(plugin.prefix+"이미 참가하였습니다.");
				} else if (args[0].equals("참가취소")) {
					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix+"게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix+"게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					if (plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
						plugin.getBingoManager().removeParticipant(player);
						return true;
					} else sender.sendMessage(plugin.prefix+"참가되어 있지 않습니다.");
				} else if (args[0].equals("리로드")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					plugin.reloadConfig();
					sender.sendMessage(plugin.prefix + "콘피그가 리로드 되었습니다.");
					return true;
				} else if (args[0].equals("난이도")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (args.length != 2) {
						sender.sendMessage(plugin.prefix + "/빙고 난이도 <쉬움/어려움/매우어려움>");
						return false;
					}

					if (args[1].equals("쉬움")) {
						plugin.getBingoManager().setDifficulty(SurvivalBingo.Difficulty.EASY);
					} else if (args[1].equals("어려움")) {
						plugin.getBingoManager().setDifficulty(SurvivalBingo.Difficulty.HARD);
					} else if (args[1].equals("매우어려움")) {
						plugin.getBingoManager().setDifficulty(SurvivalBingo.Difficulty.INSANE);
						sender.sendMessage(plugin.prefix + "§c경고, 난이도가 매우 어려움입니다. 본 난이도는 게임 시간이 많이 소요됩니다.");
					} else {
						sender.sendMessage(plugin.prefix + "올바른 난이도를 입력해주세요.");
						return false;
					}

					sender.sendMessage(plugin.prefix + "게임 난이도가 변경되었습니다: §e" + plugin.getBingoManager().getDifficulty().getDisplayName());
					return true;
				} else if (args[0].equals("매치타입")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (args.length != 2) {
						sender.sendMessage(plugin.prefix + "/빙고 매치타입 <팀/서바이벌>");
						return false;
					}

					if (args[1].equals("팀")) {
						plugin.getBingoManager().setMatchType(BingoManager.MatchType.TEAM);
					} else if (args[1].equals("서바이벌")) {
						plugin.getBingoManager().setMatchType(BingoManager.MatchType.SURVIVAL);
					} else {
						sender.sendMessage(plugin.prefix + "올바른 매치 타입을 입력해주세요.");
						return false;
					}

					sender.sendMessage(plugin.prefix + "게임 매치 타입이 변경되었습니다: §e" + plugin.getBingoManager().getMatchType().getDisplayName());
					return true;
				} else if (args[0].equals("팀")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (args[1].equals("정보")) {
						if (plugin.getBingoManager().getPartaicipant().size() == 0) {
							sender.sendMessage(plugin.prefix + "참가자가 없습니다.");
							return false;
						}

						sendTeamInfo(sender, BingoManager.Team.RED);
						sendTeamInfo(sender, BingoManager.Team.BLUE);
						return true;
					}

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (args.length != 4) {
						sender.sendMessage(plugin.prefix + "/빙고 팀 <추가/제거> <레드/블루> <닉네임>");
						return false;
					}

					Player player = getPlayer(args[3]);
					if (player == null) {
						sender.sendMessage(plugin.prefix + "온라인 플레이어가 아닙니다.");
						return false;
					}

					if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
						sender.sendMessage(plugin.prefix+"게임 참여자가 아닙니다.");
						return false;
					}

					BingoManager.Team team;

					if (args[2].equals("레드")) {
						team = BingoManager.Team.RED;
					} else if (args[2].equals("블루")) {
						team = BingoManager.Team.BLUE;
					} else {
						sender.sendMessage(plugin.prefix + "올바른 팀 이름을 입력해주세요.");
						return false;
					}

					if (args[1].equals("추가")) {
						if (!plugin.getBingoManager().getTeam(team).contains(player.getUniqueId())) {
							plugin.getBingoManager().removePlayerFromTeam(player);
							plugin.getBingoManager().addPlayerToTeam(player, team);

							sender.sendMessage(plugin.prefix + String.format("§e%s§r님이 §e%s§r팀에 추가되셨습니다.", player.getName(), team.getDisplayName()));
							return true;
						} else {
							sender.sendMessage(plugin.prefix + "해당 플레이어는 이미 " + team.getDisplayName() + "§r에 가입되어 있습니다.");
							return false;
						}
					} else if (args[1].equals("제거")) {
						if (plugin.getBingoManager().getTeam(team).contains(player.getUniqueId())) {
							plugin.getBingoManager().removePlayerFromTeam(player);

							sender.sendMessage(plugin.prefix + String.format("§e%s§r님이 §e%s§r팀에서 제거되셨습니다.", player.getName(), team.getDisplayName()));
							return true;
						} else {
							sender.sendMessage(plugin.prefix + "해당 플레이어는 §e" + team.getDisplayName() + "§r팀에 가입되어 있지 않습니다.");
							return false;
						}
					} else {
						sender.sendMessage(plugin.prefix + "/빙고 팀 <추가/제거> <레드/블루> <닉네임>");
						return false;
					}
				} else if (args[0].equals("좌표설정")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (args.length != 2) {
						sender.sendMessage(plugin.prefix + "사용법: /빙고 좌표설정 <레드/블루>");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix + "게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					String configName;
					if (args[1].equals("레드")) {
						configName = "red";
					} else if (args[1].equals("블루")) {
						configName = "blue";
					} else {
						sender.sendMessage(plugin.prefix + "올바른 팀 이름을 입력해주세요.");
						return false;
					}

					Location location = player.getLocation();
					plugin.getConfig().set("teleport-location." + configName + ".world", location.getWorld().getName());
					plugin.getConfig().set("teleport-location." + configName + ".x", location.getBlockX());
					plugin.getConfig().set("teleport-location." + configName + ".y", location.getBlockY());
					plugin.getConfig().set("teleport-location." + configName + ".z", location.getBlockZ());
					plugin.getConfig().set("teleport-location." + configName + ".yaw", location.getYaw());
					plugin.getConfig().set("teleport-location." + configName + ".pitch", location.getPitch());
					plugin.saveConfig();

					sender.sendMessage(plugin.prefix + "현재 위치를 §e" + args[1] + "§r팀의 시작 위치로 설정했습니다.");
					return true;
				} else if (args[0].equals("시작")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix + "게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (plugin.getBingoManager().getPartaicipantSize() < 2) {
						sender.sendMessage(plugin.prefix + "게임 참가자는 2명 이상이여야 합니다.");
						return false;
					}

					//if (plugin.getServer().getMon)

					plugin.getServer().broadcastMessage(plugin.prefix + "아이템을 불러오는 중...");
					plugin.loadItems(plugin.getBingoManager().getDifficulty());
					int cnt = plugin.getBingoManager().getItemStackList().size();
					plugin.getServer().broadcastMessage(plugin.prefix + cnt + "개의 아이템이 로드되었습니다.");

					if (cnt < 25) {
						plugin.getServer().broadcastMessage(plugin.prefix + "아이템의 수가 25개 미만이므로 게임 시작이 불가능합니다.");
						return false;
					}

					plugin.getBingoManager().startGame(player);
					return true;
				} else if (args[0].equals("중지")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (!plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임 중이 아닙니다.");
						return false;
					}

					plugin.getBingoManager().stopGame();
					plugin.getBingoManager().broadcastStop(sender.getName());
					return true;
				} else if (args[0].equals("참가자")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.admin"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.admin]");
						return false;
					}

					if (args[1].equals("목록")) {
						Set<UUID> partaicipants = plugin.getBingoManager().getPartaicipant();
						if (partaicipants.size() == 0) {
							sender.sendMessage(plugin.prefix + "참가자가 없습니다.");
							return false;
						}

						StringBuilder stringBuilder = new StringBuilder();
						for (UUID uuid : partaicipants) {
							Player player = Bukkit.getPlayer(uuid);

							if (stringBuilder.toString().equals("")) {
								stringBuilder.append(player.getName());
							} else {
								stringBuilder.append(", ").append(player.getName());
							}
						}

						sender.sendMessage(plugin.prefix + "§a참가자 목록");
						sender.sendMessage(plugin.prefix + stringBuilder.toString());
						return true;
					}

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					Player player = getPlayer(args[2]);
					if (player == null) {
						sender.sendMessage(plugin.prefix + "온라인 플레이어가 아닙니다.");
						return false;
					}

					if (args[1].equals("추가")) {
						if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
							plugin.getBingoManager().addParticipant(player);

							sender.sendMessage(plugin.prefix + String.format("§e%s§r님이 참가자에 추가되셨습니다.", player.getName()));
							return true;
						} else {
							sender.sendMessage(plugin.prefix + "해당 플레이어는 이미 참가자에 추가되어 있습니다.");
							return false;
						}
					} else if (args[1].equals("제거")) {
						if (plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
							plugin.getBingoManager().removeParticipant(player);

							sender.sendMessage(plugin.prefix + String.format("§e%s§c님이 참가자에서 제거되셨습니다.", player.getName()));
							return true;
						} else {
							sender.sendMessage(plugin.prefix + "해당 플레이어는 참가자에 추가되어 있지 않습니다.");
							return false;
						}
					} else {
						sender.sendMessage(plugin.prefix + "/빙고 참가자 <추가/제거> <닉네임>");
						return false;
					}
				} else if (args[0].equals("가입")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.team"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.team]");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix + "게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (args.length != 2) {
						sender.sendMessage(plugin.prefix + "/빙고 가입 <레드/블루>");
						return false;
					}

					if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
						sender.sendMessage(plugin.prefix+"게임 참여자가 아닙니다.");
						return false;
					}

					BingoManager.Team team;

					if (args[1].equals("레드")) {
						team = BingoManager.Team.RED;
					} else if (args[1].equals("블루")) {
						team = BingoManager.Team.BLUE;
					} else {
						sender.sendMessage(plugin.prefix + "올바른 팀 이름을 입력해주세요.");
						return false;
					}

					if (!plugin.getBingoManager().getTeam(team).contains(player.getUniqueId())) {
						plugin.getBingoManager().removePlayerFromTeam(player);
						plugin.getBingoManager().addPlayerToTeam(player, team);

						sender.sendMessage(plugin.prefix + String.format("§e%s§r팀에 가입되셨습니다.", team.getDisplayName()));
						return true;
					} else {
						sender.sendMessage(plugin.prefix + "이미 " + team.getDisplayName() + "§r에 가입되어 있습니다.");
						return false;
					}
				} else if (args[0].equals("탈퇴")) {
					if (!(sender.isOp() || sender.hasPermission("bingo.team"))) {
						sender.sendMessage(plugin.prefix + "권한이 없습니다. [bingo.team]");
						return false;
					}

					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.prefix + "게임 내에서만 사용 가능합니다.");
						return false;
					}
					Player player = (Player) sender;

					if (plugin.getBingoManager().isPlaying()) {
						sender.sendMessage(plugin.prefix + "게임이 진행중일 때는 사용할 수 없습니다.");
						return false;
					}

					if (!plugin.getBingoManager().getPartaicipant().contains(player.getUniqueId())) {
						sender.sendMessage(plugin.prefix+"게임 참여자가 아닙니다.");
						return false;
					}

					if (plugin.getBingoManager().getTeam(player) != null) {
						plugin.getBingoManager().removePlayerFromTeam(player);

						sender.sendMessage(plugin.prefix + "팀에서 탈퇴하셨습니다.");
						return true;
					} else {
						sender.sendMessage(plugin.prefix + "팀에 가입되어 있지 않습니다.");
						return false;
					}
				} else {
					int page = 1;

					try {
						page = Integer.parseInt(args[0]);
					} catch (NumberFormatException ex) {
						// Ignore
					}

					sendHelp(sender, page);
					return true;
				}
			} else {
				sendHelp(sender, 1);
				return true;
			}
		}
		return false;
	}

	private void sendHelp(CommandSender sender, int page) {
		sender.sendMessage("§a§l---- " + plugin.getDescription().getFullName() + " [" + page + "/2] ----");

		switch (page) {
			case 1:
				sender.sendMessage(plugin.prefix + "/빙고 [1~2]");
				sender.sendMessage(plugin.prefix + "/빙고판");
				sender.sendMessage(plugin.prefix + "/빙고 참가");
				sender.sendMessage(plugin.prefix + "/빙고 참가취소");
				sender.sendMessage(plugin.prefix + "/빙고 가입 <레드/블루>");
				sender.sendMessage(plugin.prefix + "/빙고 탈퇴");
				sender.sendMessage("");
				sender.sendMessage("§b[Tip]§r 팀을 정하지 않은 참가자는 게임이 시작될 때 자동으로 팀이 정해집니다.");
				break;
			case 2:
				sender.sendMessage(plugin.prefix + "/빙고 <시작/중지>");
				sender.sendMessage(plugin.prefix + "/빙고 난이도 <쉬움/어려움/매우어려움>");
				sender.sendMessage(plugin.prefix + "/빙고 매치타입 <팀/서바이벌>");
				sender.sendMessage(plugin.prefix + "/빙고 좌표설정 <레드/블루>");
				sender.sendMessage(plugin.prefix + "/빙고 참가자 목록");
				sender.sendMessage(plugin.prefix + "/빙고 참가자 <추가/제거> <닉네임>");
				sender.sendMessage(plugin.prefix + "/빙고 팀 정보");
				sender.sendMessage(plugin.prefix + "/빙고 팀 <추가/제거> <레드/블루> <닉네임>");
				sender.sendMessage(plugin.prefix + "/빙고 리로드");
				break;
		}
	}

	private Player getPlayer(String name) {
		for (Player all : plugin.getServer().getOnlinePlayers()) {
			if (all.getName().equalsIgnoreCase(name))
				return all;
		}
		return null;
	}

	private void sendTeamInfo(CommandSender sender, BingoManager.Team team) {
		List<UUID> teamPlayers = plugin.getBingoManager().getTeam(team);

		if (teamPlayers.size() > 0) {
			StringBuilder stringBuilder = new StringBuilder();

			for (UUID uuid : teamPlayers) {
				Player player = Bukkit.getPlayer(uuid);

				if (stringBuilder.toString().equals("")) {
					stringBuilder.append(player.getName());
				} else {
					stringBuilder.append(", ").append(player.getName());
				}
			}

			sender.sendMessage(plugin.prefix + team.getDisplayName());
			sender.sendMessage(plugin.prefix + stringBuilder.toString());
		}
	}
}