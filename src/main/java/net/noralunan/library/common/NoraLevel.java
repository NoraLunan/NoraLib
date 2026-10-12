package net.noralunan.library.common;

import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.noralunan.library.NoraLib;

public interface NoraLevel extends NoraWorld {
    NoraLogger LOGGER = NoraLib.logger();

    default ServerLevel level() {
        return (ServerLevel) this;
    }

    default ServerLevelAccessor levelAccessor() {
        return (ServerLevelAccessor) this;
    }

    default MinecraftServer server() {
        return level().getServer();
    }

    default ServerLevelData levelData() {
        return (ServerLevelData) level().getLevelData();
    }

    default void worldTime(long target) {
        level().setDayTime(target);
    }

    default void dayTime(int target) {
        level().setDayTime(day() * 24000L + target);
    }

    default void playTime(long target) {
        levelData().setGameTime(target);
    }

    default void day(int target) {
        level().setDayTime(target * 24000L);
    }

    default void setSunny(int time) {
        level().setWeatherParameters(time, 0, false, false);
    }

    default void setRain(int time) {
        level().setWeatherParameters(0, time, true, false);
    }

    default void setThunder(int time) {
        level().setWeatherParameters(0, time, true, true);
    }

    default boolean isFlat() {
        return level().isFlat();
    }

    default boolean booleanRule(GameRules.Key<GameRules.BooleanValue> rule) {
        return levelData().getGameRules().getBoolean(rule);
    }

    default void booleanRule(GameRules.Key<GameRules.BooleanValue> rule, boolean value) {
        levelData().getGameRules().getRule(rule).set(value, levelAccessor().getServer());
    }

    default int intRule(GameRules.Key<GameRules.IntegerValue> rule) {
        return levelData().getGameRules().getInt(rule);
    }

    default void intRule(GameRules.Key<GameRules.IntegerValue> rule, int value) {
        levelData().getGameRules().getRule(rule).set(value, levelAccessor().getServer());
    }

    default boolean allowCommand() {
        ServerLevelData data = (ServerLevelData) levelAccessor().getLevelData();
        return data.getAllowCommands();
    }

    default void command(String command) {
        command(command, Vec3.ZERO, 4, true);
    }

    default void command(String command, int permission) {
        command(command, Vec3.ZERO, permission, true);
    }

    default void command(String command, double x, double y, double z) {
        command(command, new Vec3(x, y, z), 4, true);
    }

    default void command(String command, double x, double y, double z, int permission) {
        command(command, new Vec3(x, y, z), permission, true);
    }

    default void command(String command, boolean suppress) {
        command(command, Vec3.ZERO, 4, suppress);
    }

    default void command(String command, int permission, boolean suppress) {
        command(command, Vec3.ZERO, permission, suppress);
    }

    default void command(String command, double x, double y, double z, boolean suppress) {
        command(command, new Vec3(x, y, z), 4, suppress);
    }

    default void
    command(String command, double x, double y, double z, int permission, boolean suppress) {
        command(command, new Vec3(x, y, z), permission, suppress);
    }

    default void command(String command, Vec3 pos) {
        command(command, pos, 4, true);
    }

    default void command(String command, Vec3 pos, int permission) {
        command(command, pos, permission, true);
    }

    default void command(String command, Vec3 pos, boolean suppress) {
        command(command, pos, 4, suppress);
    }

    default void command(String command, Vec3 pos, int permission, boolean suppress) {
        CommandSourceStack source;
        MinecraftServer server = levelAccessor().getServer();
        if (server == null) return;
        if (suppress) {
            source = getCmdSource().withPosition(pos).withPermission(permission).withSuppressedOutput();
        } else {
            source = getCmdSource().withPosition(pos).withPermission(permission);
        }
        try {
            server.getCommands().performPrefixedCommand(source, command);
        } catch (CommandRuntimeException exception) {
            LOGGER.error("Failed to execute command: {}", command, exception);
        }
    }

    private CommandSourceStack getCmdSource() {
        return server().createCommandSourceStack();
    }

    default void sendMessage(String message) {
        sendMessage(message, false);
    }

    default void sendMessage(String message, boolean actionbar) {
        sendMessage(Component.literal(message), actionbar);
    }

    default void sendMessage(Component content) {
        sendMessage(content, false);
    }

    default void sendMessage(Component content, boolean actionbar) {
        server().getPlayerList().broadcastSystemMessage(content, actionbar);
    }

    default void sendTitle(String content) {
        sendTitle(content, false);
    }

    default void sendTitle(String content, boolean sub) {
        sendTitle(Component.literal(content), sub);
    }

    default void sendTitle(Component content) {
        sendTitle(content, false);
    }

    default void sendTitle(Component content, boolean sub) {
        Component title;
        Component subtitle;
        if (sub) {
            title = Component.empty();
            subtitle = content;
        } else {
            title = content;
            subtitle = Component.empty();
        }
        sendTitle(title, subtitle);
    }

    default void sendTitle(String title, String subtitle) {
        sendTitle(Component.literal(title), Component.literal(subtitle));
    }

    default void sendTitle(Component title, String subtitle) {
        sendTitle(title, Component.literal(subtitle));
    }

    default void sendTitle(String title, Component subtitle) {
        sendTitle(Component.literal(title), subtitle);
    }

    default void sendTitle(Component title, Component subtitle) {
        ClientboundSetTitleTextPacket titlePacket =
                new ClientboundSetTitleTextPacket(title);
        ClientboundSetSubtitleTextPacket subtitlePacket =
                new ClientboundSetSubtitleTextPacket(subtitle);
        level().getServer().getPlayerList().getPlayers().forEach(player -> {
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        });
    }

    default void function(String function) {
        function(function, Vec3.ZERO, 4, true);
    }

    default void function(String function, int permission) {
        function(function, Vec3.ZERO, permission, true);
    }

    default void function(String function, double x, double y, double z) {
        function(function, new Vec3(x, y, z), 4, true);
    }

    default void function(String function, double x, double y, double z, int permission) {
        function(function, new Vec3(x, y, z), permission, true);
    }

    default void function(String function, boolean suppress) {
        function(function, Vec3.ZERO, 4, suppress);
    }

    default void function(String function, int permission, boolean suppress) {
        function(function, Vec3.ZERO, permission, suppress);
    }

    default void function(String function, double x, double y, double z, boolean suppress) {
        function(function, new Vec3(x, y, z), 4, suppress);
    }

    default void
    function(String function, double x, double y, double z, int permission, boolean suppress) {
        function(function, new Vec3(x, y, z), permission, suppress);
    }

    default void function(String function, Vec3 pos) {
        function(function, pos, 4, true);
    }

    default void function(String function, Vec3 pos, int permission) {
        function(function, pos, permission, true);
    }

    default void function(String function, Vec3 pos, boolean suppress) {
        function(function, pos, 4, suppress);
    }

    default void function(String function, Vec3 pos, int permission, boolean suppress) {
        CommandSourceStack source;
        if (suppress) {
            source = getCmdSource().withPosition(pos).withPermission(permission).withSuppressedOutput();
        } else {
            source = getCmdSource().withPosition(pos).withPermission(permission);
        }
        ServerFunctionManager sfm = server().getFunctions();
        ResourceLocation resource = ResourceLocation.tryParse(function);
        if (resource == null) return;
        sfm.get(resource).ifPresent(func -> {
            try {
                sfm.execute(func, source);
            } catch (CommandRuntimeException exception) {
                LOGGER.error("Failed to execute function: {}", function, exception);
            }
        });
    }

    default Explode explode(double x, double y, double z, float power) {
        return explode(x, y, z, power, false, null, null);
    }

    default Explode explode(double x, double y, double z, float power, boolean fire) {
        return explode(x, y, z, power, fire, null, null);
    }

    default Explode explode(double x, double y, double z, float power, Entity source) {
        return explode(x, y, z, power, false, source, null);
    }

    default Explode explode(double x, double y, double z, float power, DamageSource damage) {
        return explode(x, y, z, power, false, null, damage);
    }

    default Explode explode(double x, double y, double z, float power, boolean fire, Entity source) {
        return explode(x, y, z, power, fire, source, null);
    }

    default Explode
    explode(double x, double y, double z, float power, boolean fire, DamageSource damage) {
        return explode(x, y, z, power, fire, null, damage);
    }

    default Explode
    explode(double x, double y, double z, float power, Entity source, DamageSource damage) {
        return explode(x, y, z, power, false, source, damage);
    }

    default Explode
    explode(double x, double y, double z, float power, boolean fire, Entity source, DamageSource damage) {
        return new Explode(level(), x, y, z, power, fire, source, damage);
    }

    class Explode {
        private final ServerLevel level;
        private final double x;
        private final double y;
        private final double z;
        private final float power;
        private final boolean fire;
        private final Entity source;
        private final DamageSource damage;

        private Explode(ServerLevel level, double x, double y, double z, float power, boolean fire, Entity source, DamageSource damage) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
            this.fire = fire;
            this.source = source;
            this.damage = damage;
        }

        private Explode() {
            this.level = null;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.power = 0;
            this.fire = false;
            this.source = null;
            this.damage = null;
        }

        public void none() {
            if (level == null) return;
            level.explode(source, damage, null, x, y, z, power, fire, ServerLevel.ExplosionInteraction.NONE);
        }

        public void block() {
            if (level == null) return;
            level.explode(source, damage, null, x, y, z, power, fire, ServerLevel.ExplosionInteraction.BLOCK);
        }

        public void mob() {
            if (level == null) return;
            level.explode(source, damage, null, x, y, z, power, fire, ServerLevel.ExplosionInteraction.MOB);
        }

        public void tnt() {
            if (level == null) return;
            level.explode(source, damage, null, x, y, z, power, fire, ServerLevel.ExplosionInteraction.TNT);
        }
    }

    default void lightning(Vec3 pos) {
        lightning(pos, false);
    }

    default void lightning(Vec3 pos, boolean visual) {
        lightning(pos.x, pos.y, pos.z, visual);
    }

    default void lightning(double x, double y, double z) {
        lightning(x, y, z, false);
    }

    default void lightning(double x, double y, double z, boolean visual) {
        LightningBolt entity = EntityType.LIGHTNING_BOLT.create(level());
        if (entity == null) return;
        entity.setPos(x, y, z);
        entity.setVisualOnly(visual);
        level().addFreshEntity(entity);
    }
    
}
