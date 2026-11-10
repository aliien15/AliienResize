<div align="center">

# 📏 AliienResize
**A free, high-performance player scaling plugin for Minecraft Servers.**

[![Paper](https://img.shields.io/badge/Paper-1.21-blue?style=flat-square)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-Supported-success?style=flat-square)](https://github.com/PaperMC/Folia)

</div>

---

## 🚀 Overview

**AliienResize** is a lightweight, professional cosmetic plugin that allows players to physically change their size seamlessly. Built specifically for modern Minecraft (1.21+), it prioritizes extreme performance, strict Folia compatibility, and bulletproof safety checks.

Whether you want to sell sizes through your server's economy, grant them as VIP perks, or let your admins have fun, AliienResize provides a flawless, lag-free experience.

### ✨ Key Features

* **Smart Collision Detection:** Mathematical bounding-box checks prevent players from clipping through ceilings or glitching into walls when growing.
* **Folia & Highly Optimized:** All parts of this plugin have been highly optimized to ensure I will never be the reason your server TPS drops, as well as make sure the plugin will run fine in your Folia servers!
* **Massive Economy Support:** Built-in adapter pattern supporting 10+ economy plugins, including *Vault, PlayerPoints, UltraEconomy, ExcellentEconomy, EcoBits, RoyaleEconomy, RedisEconomy, and Experience levels.*
* **Custom Economies:** Natively build your own custom currency hooks directly in the config using PlaceholderAPI.
* **World Blacklists:** Automatically reverts players to their default size (1.0x) upon entering restricted worlds.
* **MiniMessage Support:** Full MiniMessage (`<gradient>`, `<bold>`) support across all menus, items, and messages, as well as legacy codes (`&f`, `&#ffffff`)

## 📦 Requirements

* **Server Software:** Paper, Folia or any of their forks (1.21+)
* **Soft-Dependencies:** Vault, PlaceholderAPI (For economies & placeholders) and any economy plugin you might want to you (check the documentation for available options)

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
2. Ensure everything you do is in kotlin (while you could technically shove java code in here, I'd rather keep everything the same language)
3. Use **Java 21** and **Maven**. First build and install the current [AliienCore](https://github.com/aliien15/AliienCore) checkout with `mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true clean install` in that repository. Then run `mvn clean verify` in this project. AliienCore `1.0.0` must be a current shaded build with JDBC drivers in provided scope; the version number alone does not identify its source revision.
   AliienCore is bundled and relocated into AliienResize. It does not need to be installed as a server plugin. Keep service-resource merging enabled and JAR minimization disabled so JDBC providers and classes loaded through reflection survive packaging. Kotlin and the four JDBC drivers are loaded through the plugin descriptor's `libraries` list.
4. Make sure your code is actually clean, safe, optimized and with no bugs or memory leaks (basically use common sense lol)

## CI and local checks

GitHub Actions runs file checks and a Java 21 Maven build on pull requests, pushes to `main`/`master`, and manual runs. CI installs AliienCore from the commit pinned in `.github/workflows/ci.yml`, verifies the final shaded JAR, and uploads that JAR. Update the pin when adopting a new framework revision. AliienCore must be accessible to the runner.

Install [pre-commit](https://pre-commit.com/#install), then enable both hooks:

```sh
pre-commit install --hook-type pre-commit --hook-type pre-push
pre-commit run --all-files
```

Commit checks validate whitespace, file endings, YAML/XML, merge conflicts, case conflicts, large additions, and private keys. Pre-push builds a temporary source copy and checks the packaged framework relocation, external driver declarations, plugin metadata, and default resources. Java 21, Maven, Python 3, and the current locally installed AliienCore are required. These checks do not launch Paper/Folia or test server runtime library loading.

To run the same build and packaging checks manually:

```sh
python3 tests/run-checks.py
# Preserve the current target directory:
python3 tests/run-checks.py --isolated
```

## 📄 License

This project is licensed under the MIT License. See the license file for details.
