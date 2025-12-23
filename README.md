# 📬 KawaiiMailbox

**Professional Offline Mailbox System for Minecraft Paper 1.21.8+**

A fully-featured, production-ready mailbox plugin with MongoDB integration that allows players to send messages and items to offline players.

## ✨ Features

### Core Features
- **Offline Mail System**: Send messages and items to players even when they're offline
- **MongoDB Integration**: Persistent, scalable storage with automatic indexing
- **Interactive GUIs**: Fully customizable inventory interfaces for inbox, mail details, and item attachment
- **Real-time Notifications**: Sound, particles, and clickable chat messages when receiving new mail
- **Auto-open Inbox**: Configurable auto-open feature when players join with unread mail
- **Pagination Support**: Navigate through large mailboxes with ease
- **Item Attachments**: Attach up to 27 items per mail message
- **Read Status Tracking**: Visual distinction between read and unread messages
- **Mail Clearing**: Remove read messages to keep inbox organized

### Technical Excellence
- **No Deprecated Methods**: Uses only Paper API 1.21.8+ modern methods
- **Asynchronous Operations**: All database operations are non-blocking
- **Graceful Error Handling**: Robust error handling prevents server crashes
- **Connection Pooling**: Optimized MongoDB connection management
- **Spam Protection**: Prevents rapid-fire duplicate operations
- **Industrial-Grade Code**: Comprehensive Javadoc documentation throughout

## 📋 Requirements

- **Minecraft Server**: Paper 1.21.8 or higher
- **Java**: Java 21+
- **MongoDB**: 4.0+ (local installation or MongoDB Atlas)
- **Dependencies**: MongoDB Java Driver (bundled)

## 🚀 Installation

1. **Download the Plugin**
    - Place `KawaiiMailbox.jar` in your server's `plugins` folder

2. **Install MongoDB**

   **Option A: Local MongoDB**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install mongodb
   
   # macOS
   brew install mongodb-community
   
   # Windows
   # Download from https://www.mongodb.com/try/download/community
   ```

   **Option B: MongoDB Atlas (Cloud)**
    - Sign up at https://www.mongodb.com/cloud/atlas
    - Create a free cluster
    - Get your connection string

3. **Configure the Plugin**
    - Start your server to generate config files
    - Stop the server
    - Edit `plugins/KawaiiMailbox/config.yml`
    - Set your MongoDB connection string:
      ```yaml
      database:
        connection-string: "mongodb://localhost:27017"
        # Or for Atlas:
        # connection-string: "mongodb+srv://user:pass@cluster.mongodb.net"
        database-name: "kawaii_mailbox"
      ```

4. **Start Your Server**
    - The plugin will automatically create collections and indexes

## 📖 Commands

| Command                         | Description           | Permission            |
|---------------------------------|-----------------------|-----------------------|
| `/mail send <player> <message>` | Send mail to a player | `kawaiimailbox.send`  |
| `/mail inbox`                   | Open your mailbox     | `kawaiimailbox.inbox` |
| `/mail clear`                   | Clear read messages   | `kawaiimailbox.clear` |
| `/mail help`                    | Show help information | -                     |

## 🎮 Usage Examples

### Sending a Simple Message
```
/mail send Steve Hello! Hope you're doing well!
```

### Sending Mail with Items
1. Execute: `/mail send Alex Check out these items!`
2. Click the `[Click here to add items]` link in chat
3. Place items in the GUI
4. Click the green **Confirm and Send** button

### Checking Your Inbox
- Type `/mail inbox` or
- Click the notification link when you join with unread mail

### Clearing Old Messages
```
/mail clear
```

## ⚙️ Configuration

### config.yml
```yaml
database:
  connection-string: "mongodb://localhost:27017"
  database-name: "kawaii_mailbox"

mail:
  max-message-length: 500
  max-items-per-mail: 27

inbox:
  messages-per-page: 27
  auto-open-on-join: true
  auto-open-delay-ticks: 40

notifications:
  sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
  sound-volume: 1.0
  sound-pitch: 1.0
  particles-enabled: true
  particle-type: "VILLAGER_HAPPY"
  particle-count: 10
```

### messages.yml
All plugin messages support **MiniMessage** format:
- `<red>`, `<green>`, `<blue>` - Standard colors
- `<#FF69B4>` - Hex colors
- `<gradient:red:blue>` - Gradients
- `<bold>`, `<italic>` - Formatting

### gui.yml
Customize every aspect of the GUIs:
- Titles and sizes
- Button positions
- Materials and lore
- Filler items

## 🔧 Permissions

| Permission            | Description     | Default |
|-----------------------|-----------------|---------|
| `kawaiimailbox.send`  | Send mail       | `true`  |
| `kawaiimailbox.inbox` | View inbox      | `true`  |
| `kawaiimailbox.clear` | Clear messages  | `true`  |
| `kawaiimailbox.admin` | All permissions | `op`    |

## 🗄️ Database Structure

### Collection: mailbox
```json
{
  "_id": "uuid-string",
  "sender": "sender-uuid",
  "senderName": "PlayerName",
  "recipient": "recipient-uuid",
  "recipientName": "PlayerName",
  "message": "Message content",
  "timestamp": 1234567890,
  "read": false,
  "itemsClaimed": false,
  "items": ["base64-encoded-items"]
}
```

### Indexes
- `recipient` (ascending)
- `recipient + read` (compound)
- `timestamp` (descending)

## 🛡️ Error Handling

The plugin includes comprehensive error handling:

- **MongoDB Connection Failure**: Plugin disables gracefully, logs clear error
- **Network Timeouts**: Operations fail gracefully with user notifications
- **Rapid Join/Quit**: Prevents duplicate processing
- **Corrupted Data**: Skips invalid items, continues operation
- **Full Inventory**: Warns player before claiming items

## 🎨 Customization Examples

### Custom Notification Sound
```yaml
notifications:
  sound: "BLOCK_NOTE_BLOCK_PLING"
  sound-pitch: 2.0
```

### Custom Inbox Title with Gradient
```yaml
inbox:
  title: "<gradient:#FF1493:#FF69B4>✉ My Mailbox ✉</gradient>"
```

### Disable Auto-Open
```yaml
inbox:
  auto-open-on-join: false
```

## 🔍 Troubleshooting

### Plugin Won't Enable
- Check MongoDB connection string in config.yml
- Verify MongoDB is running: `mongosh` or `mongo`
- Check server logs for detailed error messages

### Can't Send Mail
- Verify target player has played before
- Check permission `kawaiimailbox.send`
- Ensure MongoDB connection is active

### GUI Not Opening
- Check for inventory conflicts with other plugins
- Verify GUI size is multiple of 9 in gui.yml
- Check console for errors

## 📊 Performance

- **Asynchronous database operations** prevent server lag
- **Connection pooling** (5-50 connections)
- **Indexed queries** for fast retrieval
- **Efficient pagination** with limit/skip

## 🤝 Support

For issues, suggestions, or contributions:
- GitHub: https://github.com/4K1D3V/KawaiiMailbox
- Discord: [Discord Server](https://discord.gg/wqKyGMbXSN)
- Email: support@oumaimaa.dev

## 📄 License

This plugin is provided as-is for use on Minecraft servers. All rights reserved.

## 🙏 Credits

- **Author**: oumaimaa
- **Paper API**: https://papermc.io
- **MongoDB**: https://www.mongodb.com
- **Adventure API**: https://docs.advntr.dev

---

**Made with 💖 by a developer with 25 years of Java experience**