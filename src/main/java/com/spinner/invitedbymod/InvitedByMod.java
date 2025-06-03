package com.example.invitedby;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.lucko.fabric.api.permissions.v0.Permissions;

import static net.minecraft.command.arguments.EntityArgumentType.player;
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
		dataFile = new File("config/invitedby.json");
		configFile = new File("config/invitedby-config.json");

		loadData();
		loadConfig();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					literal("invitedby")
							// Normal user command requires invitedby.use permission
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

										source.sendFeedback(Text.literal("Invitation registered. Rewards granted!"), false);

										// Run rewards for invited
										for (String cmd : config.invitedCommands) {
											String commandToRun = cmd.replace("%player%", invitedPlayer.getEntityName());
											source.getMinecraftServer().getCommandManager().execute(source, commandToRun);
										}

										// Run rewards for inviter
										String inviterName = inviterPlayer.getEntityName();
										for (String cmd : config.inviterCommands) {
											String commandToRun = cmd.replace("%player%", inviterName);
											source.getMinecraftServer().getCommandManager().execute(source, commandToRun);
										}

										return 1;
									})
							)
							// Admin subcommands require invitedby.admin permission
							.then(literal("reload")
									.requires(src -> Permissions.check(src, "invitedby.admin", 2))
									.executes(context -> {
										loadConfig();
										context.getSource().sendFeedback(Text.literal("InvitedBy config reloaded."), false);
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
													context.getSource().sendFeedback(Text.literal("Reset invite status for " + target.getEntityName()), false);
												} else {
													context.getSource().sendError(Text.literal("Player " + target.getEntityName() + " has no invite status."));
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
													String inviterUUID = invitedMap.get(targetUUID);
													ServerPlayerEntity inviter = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(inviterUUID);
													String inviterName = inviter != null ? inviter.getEntityName() : "Unknown (offline)";
													context.getSource().sendFeedback(Text.literal(target.getEntityName() + " was invited by " + inviterName), false);
												} else {
													context.getSource().sendFeedback(Text.literal(target.getEntityName() + " has not declared an inviter."), false);
												}
												return 1;
											})
									)
							)
			);
		});
	}

	private void loadData() {
		try {
			if (dataFile.exists()) {
				try (FileReader reader = new FileReader(dataFile)) {
					invitedMap = GSON.fromJson(reader, MAP_TYPE);
					if (invitedMap == null) invitedMap = new HashMap<>();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			invitedMap = new HashMap<>();
		}
	}

	private void saveData() {
		try {
			dataFile.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(dataFile)) {
				GSON.toJson(invitedMap, writer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadConfig() {
		try {
			if (configFile.exists()) {
				try (FileReader reader = new FileReader(configFile)) {
					config = GSON.fromJson(reader, CONFIG_TYPE);
				}
			}
			if (config == null) {
				setDefaultConfig();
				saveConfig();
			}
		} catch (Exception e) {
			e.printStackTrace();
			setDefaultConfig();
		}
	}

	private void saveConfig() {
		try {
			configFile.getParentFile().mkdirs();
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
