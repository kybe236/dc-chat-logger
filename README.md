# dc chat logger
### A chat logger for RusherHack

#### Also try [RHP](https://github.com/kybe236/rhp) out and use code kybe ;)

## Image
![Image](https://i.imgur.com/5pJPjOP.png) ![Image](https://i.imgur.com/D3iD4hq.png)

## Commands
- `webhooks add` - Add a webhook
- `webhooks list` - List all webhooks with indexes
- `webhooks clear` - Remove all webhooks
- `webhooks remove` - Remove a webhook
- `webhooks dump` - Dump all webhooks

## Options
- `Mode` - If all player or only listed players should be logged
- `Players` - List of players to log seperated by commas
- `Colors` - Discord embed color
- `Webhooks` - List of webhooks to send logs to seperated by commas
- `Ignore Self` - Ignore messages sent by the player running the command
- `Avatar` - NullSetting
  - `Size` - The output size of the avatar
  - `No Helmet` - If the helmet should be removed from the avatar
  - `Type` - The type of the avatar
    - `Head` - Isotomic view of the head
    - `Body` - Full body of the player
      - `Oriantation` - Enum for the orientation of the player
        - `Left` - Looking to the left
        - `Right` - Looking to the right
    - `Player` - Full body of the player with the head from the front
    - `Combo` - Combination of the head and body
    - `Skin` - Raw Skin File
- `WebHooks` - Webhook links comma seperated (hidden until middle-clicked)