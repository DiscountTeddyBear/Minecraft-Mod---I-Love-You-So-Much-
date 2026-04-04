package iloveyousomuch.examplemod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import com.mojang.blaze3d.platform.InputConstants;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ILoveYouSoMuch.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ILoveYouSoMuch.MODID, value = Dist.CLIENT)
public class ILoveYouSoMuchClient {
    // Keybind for kissing
    public static final KeyMapping KISS_KEY = new KeyMapping("key.iloveyousomuch.kiss", InputConstants.KEY_R, "key.categories.multiplayer");

    public ILoveYouSoMuchClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        // container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ILoveYouSoMuch.LOGGER.info("HELLO FROM CLIENT SETUP");
        ILoveYouSoMuch.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KISS_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null && KISS_KEY.consumeClick()) {
            mc.player.connection.send(new ServerboundCustomPayloadPacket(new KissMessage()));
        }
    }
}
