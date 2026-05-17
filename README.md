<div align="center">

# 📏 AliienResize
**A free, high-performance player scaling plugin for Minecraft Servers.**

[![Paper](https://img.shields.io/badge/Paper-1.21-blue?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://adoptium.net/)
[![Folia](https://img.shields.io/badge/Folia-Supported-success?style=flat-square)](https://github.com/PaperMC/Folia)

</div>

---

## 🚀 Overview

**AliienResize** is a lightweight, professional cosmetic plugin that allows players to physically change their size seamlessly. Built specifically for modern Minecraft (1.21+), it prioritizes extreme performance, strict Folia compatibility, and bulletproof safety checks.

Whether you want to sell sizes through your server's economy, grant them as VIP perks, or let your admins have fun, AliienResize provides a flawless, lag-free experience.

### ✨ Key Features

* **Smart Collision Detection:** Mathematical bounding-box checks prevent players from clipping through ceilings or glitching into walls when growing.
* **Folia & Highly Optimized:** All parts of this plugin have been highly optimized to ensure I will never be the reason your server TPS drops, as well as make sure the plugin will run fine in your Folia servers!
* **Massive Economy Support:** Built-in adapter pattern supporting 10+ economy plugins, including *Vault, PlayerPoints, UltraEconomy, ExcellentEconomy, EcoBits, RoyaleEconomy, RedisEconomy, CoinsEngine, and Experience levels.*
* **Custom Economies:** Natively build your own custom currency hooks directly in the config using PlaceholderAPI.
* **World Blacklists:** Automatically reverts players to their default size (1.0x) upon entering restricted worlds.
* **MiniMessage Support:** Full MiniMessage (`<gradient>`, `<bold>`) support across all menus, items, and messages, as well as legacy codes (`&f`, `&#ffffff`)

## 📦 Requirements

* **Server Software:** Paper, Purpur, Spigot or Folia (1.21+)
* **Java Version:** Java 21 or higher
* **Soft-Dependencies:** Vault, PlaceholderAPI (For economies & placeholders)

## 🛠️ Installation

1. Download the latest release from the [Releases Tab](https://aliien.gitbook.io/aliien-docs/aliienresize-soon/getting-started).
2. Drop `AliienResize-x.x.x.jar` (and the dependencies) into your server's `plugins/` folder.
3. Restart your server to generate the configuration files.
4. Configure your sizes, prices, and settings in `plugins/AliienResize/`.
5. Run `/resize admin reload` to apply config changes in-game!

## 💻 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/resize menu` | Opens the main size selection GUI. | `aliien.resize.menu` |
| `/resize set <id>` | Sets your size to a specific configured ID. | `aliien.resize.set` |
| `/resize clear` | Reverts your size back to default (1.0x). | `aliien.resize.clear` |
| `/resize admin reload` | Reloads all configuration files and menus. | `aliien.resize.admin.reload` |
| `/resize admin set <player> <id>` | Force-sets a size on a target player. | `aliien.resize.admin.set` |
| `/resize admin clear <player>` | Clears a target player's size. | `aliien.resize.admin.clear` |

*Note: Use the `-f` flag on admin commands to bypass collision checks!*

## 📚 Documentation

For a comprehensive guide on configuring the `sizes.yml`, setting up custom economy hooks, and using PlaceholderAPI, please visit our **[Official Wiki](https://aliien.gitbook.io/aliien-docs/aliienresize-soon/getting-started)**.

## 🤝 Contributing

The reason this project is open source is just so you can directly contribute with your own ideas, therefore PRs are more than welcome!

If you are a developer looking to contribute:
1. Clone the repository.
2. Ensure you have Java 21 installed.
3. This project uses **Maven**. Run `mvn clean package` to build the jar.
4. Code should stick to modern Java practices (Records, Streams, Immutable Collections, Functional Programming, etc as much as possible) and must maintain strict Folia compatibility (failure to stand by these rules will result in PRs being denied)

## 📄 License

This project is licensed under the MIT License. See the license file for details.