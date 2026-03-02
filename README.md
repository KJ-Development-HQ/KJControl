KJControl [![GitHub Release](https://img.shields.io/github/v/release/KJ-Development-HQ/KJControl?display_name=release)](https://github.com/KJ-Development-HQ/KJControl/releases/tag/v1.0.0)
[![Discord](https://img.shields.io/discord/1469373726679236875.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/KRksfGhQEH)
[![GitHub License](https://img.shields.io/github/license/KJ-Development-HQ/KJControl)](https://github.com/KJ-Development-HQ/KJControl?tab=GPL-3.0-1-ov-file#readme)
[![GitHub Sponsors](https://img.shields.io/github/sponsors/kjchambers?label=GitHub%20Sponsors)](https://github.com/sponsors/kjchambers)
===========

A modern, lightweight Paper plugin that provides:

- **Custom chat formatting** using MiniMessage
- **Join & quit message customisation**
- A simple **GUI control panel**
- A Brigadier-based command system
- Clean configuration with version checks

Built for modern Paper servers using Adventure components and PlaceholderAPI support.

---

## ✨ Features

### 💬 Chat Formatting

- Fully configurable `chat-format.yml`
- MiniMessage support (`<gradient>`, `<hover>`, `<click>`, etc.)
- PlaceholderAPI support (`%player_name%`, etc.)
- Optional hover & click events on player names
- Safe fallback if formatting fails

Example:

```yaml
format:
  prefix: "<#767676>[<gradient:#0bdfc9:#470bdf>KJControl</gradient><#767676>]<reset> "
  name: "<username>"
  name_hover: "Change me in KJControl/chat-format.yml"
  name_click: ""
  suffix: "<gray>: <white>"
```

---

### 👋 Join & Quit Messages

- Fully configurable `messages.yml`
- Supports MiniMessage
- Supports PlaceholderAPI
- Can be toggled independently from chat formatting

Example:

```yaml
join-message: "<green>+ <grey><username>"
quit-message: "<red>- <grey><username>"
```

---

### 📜 Commands

| Command              | Description               | Permission          |
|----------------------|---------------------------|---------------------|
| `/kjcontrol`         | Opens the plugin menu     | `kjcontrol.admin`   |
| `/kjcontrol preview` | Preview your chat format  | `kjcontrol.preview` |
| `/kjcontrol reload`  | Reload all plugin configs | `kjcontrol.reload`  |
| `/kjcontrol help`    | Shows the help menu       | `kjcontrol.admin`   |

---

## ⚙ Configuration

### config.yml

Controls feature toggles and global settings.

```yaml
# DO NOT CHANGE VERSION
config-version: 1

# Choose whether to enable or disable each feature
features:

  # chat-format.yml
  enable-chat-format: true

  # messages.yml
  messages:

    # Enables all messages. If false, messages.yml will be ignored.
    # If true, you can then enable/disable individual messages below
    enabled: true

    # Enables join and quit messages - configurable in messages.yml
    enable-join-quit: true
```

### chat-format.yml

Controls how chat messages appear.

```yaml

# -----------------------------------------------
# - Supports PlaceholderAPI
# - Formatting MUST be in MiniMessage.
# - Internal placeholders:
#   - <username>
#   - <displayname>
#
# MiniMessage: https://docs.papermc.io/adventure/minimessage/format/
# PlaceholderAPI: https://wiki.placeholderapi.com/
# -----------------------------------------------

# DO NOT CHANGE VERSION
chat-format-version: 1

# This will change the format of chat messages.
format:

  # This will go before the name
  prefix: "<#767676>[<gradient:#0bdfc9:#470bdf>KJControl</gradient><#767676>]<reset> "

  # This is who sent the message
  name: "<username>"

  #This will be shown when people hover the name
  name_hover: "Change me in KJControl/chat-format.yml"

  #This command will be run when people click the name
  name_click: ""

  #This will go after the name, before the message
  suffix: "<gray>: <white>"
```

### messages.yml

Controls plugin messages

```yaml

# -----------------------------------------------
# - Supports PlaceholderAPI
# - Formatting MUST be in MiniMessage.
# - Internal placeholders:
#   - <username>
#   - <displayname>
#
# MiniMessage: https://docs.papermc.io/adventure/minimessage/format/
# PlaceholderAPI: https://wiki.placeholderapi.com/
# -----------------------------------------------

# DO NOT CHANGE VERSION
messages-version: 1

# Set the join message
join-message: "<green>+ <grey><username>"

# Set the quit message
quit-message: "<red>- <grey><username>"
```

---

## 🔧 How It Works

### Chat System

1. Player sends message
2. `AsyncChatEvent` is intercepted
3. `ChatFormat` resolves:
   - PlaceholderAPI placeholders
   - MiniMessage tags
   - Hover & click events
4. A `ResolvedChatFormat` is returned
5. Final formatted message is rendered per viewer

The suffix and message are combined during resolution to ensure MiniMessage colour inheritance works correctly.

---

### Command System

KJControl uses Paper's **experimental bootstrap API** to register Brigadier commands during the correct lifecycle phase.

- Commands are defined declaratively
- Execution logic lives in `CommandUtil`
- Permissions are enforced at the Brigadier level

---

## 📦 Requirements

- **Paper 1.21.11**
- **PlaceholderAPI** (optional, but recommended)

## 🛠 Installation

1. Download the latest release from the **Releases** tab
2. Place the `.jar` in your server's `/plugins` folder.
3. Start or restart your server
4. Configure `config.yml`, `chat-format.yml`, and `messages.yml`

---

## 📜 License

GNU GENERAL PUBLIC LICENSE
