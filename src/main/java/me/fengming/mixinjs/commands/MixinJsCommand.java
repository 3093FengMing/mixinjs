package me.fengming.mixinjs.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.fengming.mixinjs.MixinJs;
import me.fengming.mixinjs.script.MixinScriptManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MixinJsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("mixinjs")
                        .requires(p -> p.hasPermission(2))
                        .then(
                                Commands.literal("reload")
                                        .executes(
                                                ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    try {
                                                        MixinScriptManager.loadScript();
                                                        source.sendSuccess(() -> Component.translatable("commands.mixinjs.reload.success"), true);
                                                        return MixinScriptManager.mixinScriptFiles.size();
                                                    } catch (Exception e) {
                                                        source.sendFailure(Component.translatable("commands.mixinjs.reload.error"));
                                                        MixinJs.LOGGER.error("Failed to reload mixinjs scripts:", e);
                                                        return 0;
                                                    }
                                                }
                                        )
                        )
        );
    }

}
