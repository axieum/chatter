package me.axieum.mcmod.chatter.impl.styling.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.axieum.mcmod.chatter.impl.styling.ChatterStyling;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import static me.axieum.mcmod.chatter.impl.styling.ChatterStyling.CONFIG;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Nickname command that allows players to set their display name.
 */
public final class NicknameCommand implements Command<ServerCommandSource>
{
    /**
     * Registers the command.
     *
     * @param dispatcher command dispatcher
     * @param dedicated  true if running on a dedicated server
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
    {
        // Commands
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(
                literal("nickname")
                        .requires(source -> source.hasPermissionLevel(CONFIG.nickname.permissionLevel))
                        .then(argument("player", EntityArgumentType.player())
                                      .then(argument("alias", StringArgumentType.string())
                                                    .executes(new NicknameCommand()))));

        // Aliases
        dispatcher.register(literal("alias")
                                    .requires(source -> source.hasPermissionLevel(CONFIG.nickname.permissionLevel))
                                    .redirect(node));
        dispatcher.register(literal("nick")
                                    .requires(source -> source.hasPermissionLevel(CONFIG.nickname.permissionLevel))
                                    .redirect(node));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        final ServerPlayerEntity player = getPlayer(context, "player");

        // Are they trying to change someone else's nickname?
        if (player != context.getSource().getPlayer())
            if (!context.getSource().hasPermissionLevel(3))
                throw new SimpleCommandExceptionType(new TranslatableText("command.chatter.nickname.denied")).create();

        // Okay, we've established that they can change this player's nickname
        // Now, let's check that the new alias is not already in use by someone else
        String nickname = context.getArgument("alias", String.class);

        // Conditionally apply colour
        if (CONFIG.nickname.color)
            nickname = ChatterStyling.COLOR_CODE_PATTERN.matcher(nickname).replaceAll("\u00A7$1");
        Text text = new LiteralText(nickname);

        // Set the player's custom name
        player.setCustomName(text);

        // Propagate the name change to clients
        // NB: This will only affect tab lists
        context.getSource()
               .getMinecraftServer()
               .getPlayerManager()
               .sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));

        // Send success feedback
        context.getSource().sendFeedback(new TranslatableText("command.chatter.nickname.success", text), true);
        return 0;
    }
}
