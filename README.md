# VotingMatters - Minecraft Voting Plugin

[![Build Status](https://github.com/clxrityy/VotingMatters/workflows/Build%20and%20Package/badge.svg)](https://github.com/clxrityy/VotingMatters/actions/workflows/build.yml)
[![Tests](https://github.com/clxrityy/VotingMatters/workflows/Tests/badge.svg)](https://github.com/clxrityy/VotingMatters/actions/workflows/tests.yml)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20%2B-green)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A comprehensive Spigot/Paper Minecraft plugin that allows players to vote for your server through various voting sites and receive flexible, customizable rewards.

- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Commands](#commands)

## Features

- [Voting Integration](#-voting-integration)
- [Flexible Reward System](#-flexible-reward-system)
- [Statistics & Tracking](#-statistics--tracking)
- [Admin Features](#-admin-features)

### 🗳️ Voting Integration

- Support for multiple popular voting sites:
  - MinecraftServers.org
  - Minecraft-MP.com
  - PlanetMinecraft.com
  - TopMinecraftServers.org
- Automatic vote detection via APIs
- Offline vote claiming system
- Vote cooldown management

### 🎁 Flexible Reward System

- **Default Rewards**: Given for every vote
- **Streak Rewards**: Bonus rewards for consecutive daily voting
- **Milestone Rewards**: Special rewards for reaching vote count milestones
- **Site-Specific Rewards**: Different rewards per voting site
- **VIP Rewards**: Extra rewards for VIP players
- **Weekend Bonuses**: Enhanced rewards on weekends
- **Time-Based Rewards**: Different rewards based on time of day
- **Chance-Based Rewards**: Random bonus rewards with configurable chances

### 💰 Reward Types

- **Money**: Economy integration via Vault
- **Items**: Custom items with enchantments, names, and lore
- **Commands**: Execute any server command
- **Experience**: Give experience points
- **Potion Effects**: Apply potion effects to players

### 📊 Statistics & Tracking

- Total vote counts
- Voting streaks (current and best)
- Offline vote tracking
- Comprehensive leaderboards
- PlaceholderAPI integration

### 🔧 Admin Features

- Real-time configuration reloading
- Vote checking and debugging
- Player vote management
- Comprehensive logging
- Database support (SQLite & MySQL)

## Installation

1. Download the latest release
2. Place the JAR file in your server's `plugins` folder
3. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) (required)
4. Optional: Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder support
5. Restart your server
6. Configure the plugin (see Configuration section)

## Configuration

### Main Config (`config.yml`)

```yaml
# Database settings
database:
  type: "sqlite" # sqlite or mysql
  host: "localhost"
  port: 3306
  database: "votingmatters"
  username: "root"
  password: ""

# Vote site configurations
vote-sites:
  minecraftservers:
    enabled: false
    api-key: "your-api-key-here"
    server-id: "your-server-id"
    url: "https://minecraftservers.org/server/YOUR_SERVER_ID"
```

### Rewards Config (`rewards.yml`)

```yaml
# Default rewards given for every vote
default-rewards:
  - type: "money"
    amount: 100
    chance: 100
  - type: "item"
    material: "GOLDEN_APPLE"
    amount: 2
    name: "&6Vote Reward"
    chance: 75

# Streak rewards - given based on consecutive voting days
streak-rewards:
  7: # 7 day streak
    - type: "money"
      amount: 500
    - type: "item"
      material: "DIAMOND_SWORD"
      enchantments:
        - "SHARPNESS:3"
```

## Commands

| Command                           | Permission            | Description                       |
| --------------------------------- | --------------------- | --------------------------------- |
| `/vote`                           | `votingmatters.vote`  | View voting information and links |
| `/vote claim`                     | `votingmatters.vote`  | Claim offline vote rewards        |
| `/vote stats`                     | `votingmatters.vote`  | View your voting statistics       |
| `/votetop [page]`                 | `votingmatters.vote`  | View voting leaderboard           |
| `/votestats [player]`             | `votingmatters.check` | View voting stats for a player    |
| `/votecheck [player]`             | `votingmatters.check` | Check voting status for a player  |
| `/votereward <add\|remove\|list>` | `votingmatters.admin` | Manage voting rewards             |
| `/votereload`                     | `votingmatters.admin` | Reload plugin configuration       |

## Permissions

| Permission             | Default | Description                        |
| ---------------------- | ------- | ---------------------------------- |
| `votingmatters.vote`   | `true`  | Use basic voting commands          |
| `votingmatters.check`  | `op`    | Check other players' voting status |
| `votingmatters.admin`  | `op`    | Full admin access                  |
| `votingmatters.vip`    | `false` | Receive VIP voting rewards         |
| `votingmatters.notify` | `true`  | Receive vote notifications         |

## PlaceholderAPI Placeholders

| Placeholder                        | Description                            |
| ---------------------------------- | -------------------------------------- |
| `%votingmatters_total_votes%`      | Player's total vote count              |
| `%votingmatters_current_streak%`   | Player's current voting streak         |
| `%votingmatters_best_streak%`      | Player's best voting streak            |
| `%votingmatters_offline_votes%`    | Player's unclaimed offline votes       |
| `%votingmatters_last_vote%`        | Player's last vote time                |
| `%votingmatters_hours_until_next%` | Hours until player can vote again      |
| `%votingmatters_can_vote%`         | Whether player can vote (true/false)   |
| `%votingmatters_streak_at_risk%`   | Whether streak is at risk (true/false) |

## API Integration

To integrate with voting sites, you'll need to:

1. Register your server on the voting sites
2. Obtain API keys from each site
3. Configure the API keys in `config.yml`
4. Set up webhooks or polling based on the site's API

### Supported APIs

- **MinecraftServers.org**: Supports vote checking via API
- **Minecraft-MP.com**: Webhook and API support
- **PlanetMinecraft.com**: API integration
- **TopMinecraftServers.org**: Vote API support

## Database Schema

The plugin creates three main tables:

- `player_vote_data`: Stores player voting statistics
- `vote_records`: Logs individual vote records
- `vote_site_status`: Tracks voting site API status

## Development

### Building

```bash
git clone https://github.com/farlanders-cc/VotingMatters.git
cd VotingMatters
mvn clean package
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Version 1.0.0

- Initial release
- Multi-site voting support
- Flexible reward system
- Database integration
- PlaceholderAPI support
- Comprehensive command system
