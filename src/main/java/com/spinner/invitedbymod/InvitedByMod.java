package com.spinner.invitedbymod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InvitedByMod implements ModInitializer {

	private static final Gson GSON = new Gson();
	private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
	private static final Type CONFIG_TYPE = new TypeToken<Config>() {}.getType();

	private Map<String, String> invitedMap = new HashMap<>();
	private File dataFile;
	private File configFile;
	private Config config;

	@Override
	public void onInitialize() {
		File configDir = new File("config");
		if (!configDir.exists()) {
			configDir.mkdirs();
		}

		dataFile = new File(configDir, "invitedby.json");
		configFile = new File(configDir, "invitedby-config.json");

		loadData();
		loadConfig();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					literal("invitedby")
							.requires(src -> Permissions.check(src, "invitedby.use", 0))
							.then(argument("inviter", player())
									.executes(context -> {
										ServerCommandSource source = context.getSource();
										ServerPlayerEntity invitedPlayer = source.getPlayer();
										String invitedUUID = invitedPlayer.getUuidAsString();

										ServerPlayerEntity inviterPlayer = context.getArgument("inviter", ServerPlayerEntity.class);
										String inviterUUID = inviterPlayer.getUuidAsString();

										if (invitedMap.containsKey(invitedUUID)) {
											source.sendError(Text.literal("You have already declared who invited you."));
											return 0;
										}

										if (invitedUUID.equals(inviterUUID)) {
											source.sendError(Text.literal("You cannot invite yourself."));
											return 0;
										}

										invitedMap.put(invitedUUID, inviterUUID);
										saveData();

										source.sendFeedback(() -> Text.literal("Invitation registered. Rewards granted!"), false);

										String invitedName = invitedPlayer.getName().getString();
										String inviterName = inviterPlayer.getName().getString();

										for (String cmd : config.invitedCommands) {
											String commandToRun = cmd.replace("%player%", invitedName);
											source.getServer().getCommandManager().executeWithPrefix(source, commandToRun);
										}

										for (String cmd : config.inviterCommands) {
											String commandToRun = cmd.replace("%player%", inviterName);
											source.getServer().getCommandManager().executeWithPrefix(source, commandToRun);
										}

										return 1;
									})
							)
							.then(literal("reload")
									.requires(src -> Permissions.check(src, "invitedby.admin", 2))
									.executes(context -> {
										loadConfig();
										context.getSource().sendFeedback(() -> Text.literal("InvitedBy config reloaded."), false);
										return 1;
									})
							)
							.then(literal("reset")
									.requires(src -> Permissions.check(src, "invitedby.admin", 2))
									.then(argument("player", player())
											.executes(context -> {
												ServerPlayerEntity target = context.getArgument("player", ServerPlayerEntity.class);
												String targetUUID = target.getUuidAsString();

												if (invitedMap.remove(targetUUID) != null) {
													saveData();
													context.getSource().sendFeedback(() ->
															Text.literal("Reset invite status for " + target.getName().getString()), false);
												} else {
													context.getSource().sendError(Text.literal("Player " + target.getName().getString() + " has no invite status."));
												}
												return 1;
											})
									)
							)
							.then(literal("info")
									.requires(src -> Permissions.check(src, "invitedby.admin", 2))
									.then(argument("player", player())
											.executes(context -> {
												ServerPlayerEntity target = context.getArgument("player", ServerPlayerEntity.class);
												String targetUUID = target.getUuidAsString();

												if (invitedMap.containsKey(targetUUID)) {
													try {
														UUID inviterUUID = UUID.fromString(invitedMap.get(targetUUID));
														ServerPlayerEntity inviter = context.getSource().getServer().getPlayerManager().getPlayer(inviterUUID);
														String inviterName = inviter != null ? inviter.getName().getString() : "Unknown (offline)";
														context.getSource().sendFeedback(() ->
																Text.literal(target.getName().getString() + " was invited by " + inviterName), false);
													} catch (IllegalArgumentException e) {
														context.getSource().sendFeedback(() -> Text.literal("Invalid inviter UUID format."), false);
													}
												} else {
													context.getSource().sendFeedback(() ->
															Text.literal(target.getName().getString() + " has not declared an inviter."), false);
												}
												return 1;
											})
									)
							)
			);
		});
	}

	private synchronized void loadData() {
		try {
			if (dataFile.exists()) {
				try (FileReader reader = new FileReader(dataFile)) {
					invitedMap = GSON.fromJson(reader, MAP_TYPE);
					if (invitedMap == null) invitedMap = new HashMap<>();
				}
			} else {
				invitedMap = new HashMap<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			invitedMap = new HashMap<>();
		}
	}

	private synchronized void saveData() {
		try {
			if (!dataFile.getParentFile().exists()) {
				dataFile.getParentFile().mkdirs();
			}
			try (FileWriter writer = new FileWriter(dataFile)) {
				GSON.toJson(invitedMap, writer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void loadConfig() {
		try {
			if (configFile.exists()) {
				try (FileReader reader = new FileReader(configFile)) {
					config = GSON.fromJson(reader, CONFIG_TYPE);
				}
			}
			if (config == null) {
				setDefaultConfig();
				saveConfig();
			} else {
				if (config.invitedCommands == null) config.invitedCommands = List.of();
				if (config.inviterCommands == null) config.inviterCommands = List.of();
			}
		} catch (Exception e) {
			e.printStackTrace();
			setDefaultConfig();
		}
	}

	private synchronized void saveConfig() {
		try {
			if (!configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}
			try (FileWriter writer = new FileWriter(configFile)) {
				GSON.toJson(config, writer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setDefaultConfig() {
		config = new Config();
		config.inviterCommands = List.of("give %player% minecraft:diamond 1");
		config.invitedCommands = List.of("give %player% minecraft:emerald 3");
	}

	private static class Config {
		List<String> inviterCommands = Collections.emptyList();
		List<String> invitedCommands = Collections.emptyList();
	}
}
