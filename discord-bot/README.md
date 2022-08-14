# edina/discord-bot

This is a simple discord bot that will run Edina code in a sandbox and output the result.

![Img](https://i.imgur.com/ijF9K8M.png)

## Official bot

[Invite url](https://discord.com/api/oauth2/authorize?client_id=1008158666366660738&permissions=2147871808&scope=applications.commands%20bot)

## Using the bot

Simply ping the bot and attach a code block:

````
@Edina ```
import "stdlib/io/std"
"Nice" :std.println_out
```
````

## Safety

The bot doesn't do real sandboxing, so depending on your setup this could lead to a lot of potential problems. Always run the bot in a container and
make sure to specify the "restricted" flag in the command setting.