# 🎉 Recruit A Friend (InvitedBy) – Minecraft Fabric Mod

Recruit A Friend is a server-side-only Fabric mod for Minecraft 1.21.1 designed to reward players for inviting others to your server. It supports persistent tracking, configurable reward systems, permission-based command control, and administrative tools to manage invites.

This mod is perfect for Minecraft servers looking to grow organically by incentivizing player referrals, all without needing external plugins or database setups.

## 📦 Features
✅ **Invite Tracking:** Players can register who invited them using `/invitedby <player>`.
🎁 **Custom Rewards:** Trigger commands for both the inviter and invited when a referral is successful.
💾 **Persistent Data:** Uses local JSON files to store invite data and configuration.
🔧 **Reloadable Config:** Change rewards without restarting your server via `/invitedby reload`.
🔐 **Permission Nodes:** Fine-grained access control using LuckPerms-compatible permission API.
🛠️ **Admin Tools:** View and reset player invite status.

## 🚀 Getting Started

### ✅ Requirements
- Minecraft 1.21.1
- Fabric Loader 0.16.10+
- Fabric API
- Lucko’s Fabric Permissions API (optional, for permission support)
- Java 21

### ⚙️ Configuration (`invitedby-config.json`)
Customize reward commands using this file, located in the `config/` folder.


{
    "inviterCommands": [
        "give %player% minecraft:diamond 1",
        "say Thank you %player% for inviting a friend!"
    ],
    "invitedCommands": [
        "give %player% minecraft:emerald 3",
        "say Welcome %player% to our server!"
    ]
}


### 🔄 Reload Config
After editing the config, run: `/invitedby reload`


## 💬 Commands
All commands use `/invitedby` as the base.

### 🧍 `/invitedby <inviter>`
- Registers who invited the current player.
- Can only be used once per player.
- Inviter and invited must be different players.
- Both players receive rewards (configurable).
- Must be online when used.
📌 **Permissions:** `invitedby.use`

### ♻️ `/invitedby reload`
- Reloads the config file (`invitedby-config.json`) without restarting the server.
📌 **Permissions:** `invitedby.admin`

### 🧹 `/invitedby reset <player>`
- Resets a player’s invite status so they can be invited again.
📌 **Permissions:** `invitedby.admin`

### 🔍 `/invitedby info <player>`
- Displays who invited a player (if anyone).
📌 **Permissions:** `invitedby.admin`

## 🔐 Permissions
The mod integrates with Fabric Permissions API and supports any compatible permission mod (e.g., LuckPerms).

| Node | Description |
|------|------------|
| `invitedby.use` | Allows players to use `/invitedby` |
| `invitedby.admin` | Access to admin commands |

## 💾 Data Storage
- **Invite Map:** Stored in `config/invitedby.json` as a mapping of invited UUIDs → inviter UUIDs.
- **Offline Support:** Even if the inviter is offline, their UUID will still be stored and credited.
- **Safe Storage:** Uses Gson to read/write data with fallback for missing or invalid files.

## ❓ Example Use Case
1. Player A invites Player B to the server.
2. Player B logs in and runs `/invitedby PlayerA`.
3. The mod checks if:
   - Player B has not already been invited.
   - Player A ≠ Player B.
4. If valid:
   - Runs `invitedCommands` for Player B.
   - Runs `inviterCommands` for Player A.
   - Saves the mapping.
   - Player B cannot run the command again unless reset.

## 🛠️ Development Notes
- Uses `CommandRegistrationCallback` to register all subcommands.
- Replaces placeholders `%player%` in reward commands with the player’s name.
- Supports UUID storage for accuracy and to prevent name spoofing.
- Follows modern Minecraft 1.21.1 standards (e.g., `getName().getString()`, `sendFeedback(Supplier<Text>, boolean)`).

## 📚 Future Ideas (Optional Extensions)
- 📈 **Referral Levels:** Reward players for inviting multiple others.
- ⏱️ **Time Limits:** Only allow invite registrations within a configurable timeframe after joining.
- 📜 **GUI:** Let players choose their inviter from a list or use a book interface.
- 🧾 **Invite Codes:** Replace usernames with generated codes.

## 🤝 Credit
- **Created by:** FreeSpinner
- **Mod Name:** InvitedBy   Mod
- **GitHub:** https://github.com/FreeSpinner09
- **Fabric API:** [fabricmc.net](https://fabricmc.net)
- **Permissions API:** [github.com/lucko/fabric-permissions-api](https://github.com/lucko/fabric-permissions-api)

## 📝 License
This project uses the Creative Commons Legal Code License

🙏 **Designed with community growth and engagement in mind — because friends make Minecraft better.**
Feel free to use and modify this document as needed for your project!
