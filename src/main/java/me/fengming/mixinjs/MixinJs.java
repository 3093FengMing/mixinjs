package me.fengming.mixinjs;

import me.fengming.mixinjs.commands.MixinJsCommand;
import me.fengming.mixinjs.config.MixinJsConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MixinJs.MODID)
public class MixinJs {
    public static final String MODID = "mixinjs";
    public static final Logger LOGGER = LoggerFactory.getLogger("MixinJs");

    public static MixinJsConfig config = null;

    public MixinJs() {

    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
    public static class NeoEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            MixinJsCommand.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
