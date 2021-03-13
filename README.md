# Maven

```xml
<repositories>
    <repository>
        <id>nukkitx-repo</id>
        <url>http://repo.nukkitx.com/main/</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>com.creeperface.nukkit.placeholderapi</groupId>
        <artifactId>PlaceholderAPI</artifactId>
        <version>1.4-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

# How to use

### Obtaining PlaceholderAPI instance

```java
PlaceholderAPI api = PlaceholderAPI.getInstance();
```

```kotlin
val api = PlaceholderAPI.getInstance()
```

### Getting placeholder value

```java
//player can be omitted unless it's a visitor based placeholder
api.getValue("placeholder_name", player);
```

To automatically replace all placeholder in a string use translateString() method

```java
String result=api.translateString(inputString, player);
```

For kotlin there's an extension function

```kotlin
val result = inputString.translatePlaceholders(player)
```

### Registering a new placeholder

There are two ways how placeholders are being processed Placeholders registered as static have the same value for all
players which allows PAPI to effectively cache its value and increase overall performance

The second one is visitor based placeholder which depends on the player it is showing to In this case we always need to
supply player instance to get the value

Simple example of static placeholder that returns current server tick:

```java
api.builder("tick", Integer.class)
        .loader(entry -> Server.getInstance().getTick())
        .build();
```

In kotlin we use a slightly different syntax using DSL

```kotlin
api.build<Int>("tick") {
    loader {
        Server.getInstance().tick
    }
}
```

Simple example of visitor based placeholder that returns player name

```java
api.builder("player_name", String.class)
        .visitorLoader(entry -> {
            return entry.getPlayer().getName();
        });
```

Kotlin DSL makes it easier so we can access player instance directly through the lambda receiver

```kotlin
api.build<String>("player_name") {
    visitorLoader {
        player.name
    }
}
```

Builder contains a few other options

```java
api.builder("tick", Integer.class)
        .aliases("server_tick", "servertick") //placeholder aliases
        .autoUpdate(true) //enables auto update that triggers registered update listeners, disabled by default
        .updateInterval(10) //interval of auto update or cache clearing, disabled by default - no cache
        .loader(entry -> Server.getInstance().getTick())
        .build();
```

### Parameters

Since 1.3 it is possible to use parameters in placeholder

For example consider placeholder which returns play time of a player called `player_time`
Usage will look like this: `%player_time<player_name>%` where `player_name` is the name of player we want to get the
time of

Simple example that returns player name and optionally converts it to lowercase The only difference is
the `processParameters()` method that enables parameter processing

```java
api.builder("player_name", String.class)
        .processParameters(true)
        .visitorLoader(entry -> {
            String name = entry.getPlayer().getName();
    
            Parameter parameter = entry.getParameters().single();
            if (parameter != null && parameter.getValue().equals("lc")) { //lowercase
                name = name.toLowerCase();
            }
    
            return name;
        });
```

```kotlin
api.build<String>("player_name") {
    processParameters(true)
    visitorLoader {
        parameters.single()?.let {
            if (it.value == "lc") { //lowercase
                return@visitorLoader player.name.toLowerCase()
            }
        }

        player.name
    }
}
```

### Placeholder scopes

By default all placeholders belong to global scope The option of "subscopes" brings possibility of providing some
specific data to placeholder which are used to output the value

By default PAPI contains a few scopes such as `MessageScope` and `ChatScope`
and a few scoped placeholders such as `%message%` or `%message_sender%` where both are available only in `MessageScope`

Scope class is a singleton that represent scope in general Scope.Context class is something like a scope instance
holding an actual scope argument

To get value from scoped placeholder we need to provide context instance. All methods including `translateString()`
or `getValue()` contains optional vararg argument for contexts which defaults to GlobalScope It's possible to provide
multiple different contexts since it's not restricted that message must contain placeholders only from a single scope
The only current limitation is that placeholder can't use more than one scope, so the only solution to this is to extend
another scope, or to create multiple placeholders

Example usage of ChatScope:

```kotlin
val messageTemplate = "%message_sender%: %message%"

fun onChat(e: PlayerChatEvent) {
    e.message = messageTemplate.translatePlaceholders(
        e.player,
        e.context //extension property for PlayerChatEvent
    )
}
```

This replaces both placeholders in message template These placeholders don't exist in GlobalScope, however they do in
ChatScope

Another example of scopes is one I used in BedWars where I use 2 scopes ArenaScope - provides various placeholders
related to arena TeamScope - provides placeholders related to team such as color, name, status

Both of them can be used in the configuration (armor color in team shop) as well, which makes everything easier imo :)