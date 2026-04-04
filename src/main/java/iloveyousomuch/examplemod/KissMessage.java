package iloveyousomuch.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.chat.Component;

public record KissMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KissMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ILoveYouSoMuch.MODID, "kiss"));
    public static final StreamCodec<FriendlyByteBuf, KissMessage> STREAM_CODEC = StreamCodec.unit(new KissMessage());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(KissMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                Level level = player.level();
                Vec3 start = player.getEyePosition();
                Vec3 direction = player.getLookAngle();
                double reach = 3.0;
                HitResult hit = level.clip(new ClipContext(start, start.add(direction.scale(reach)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (hit.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hit;
                    Entity entity = entityHit.getEntity();
                    if (entity instanceof ServerPlayer target) {
                        player.sendSystemMessage(Component.literal("You kissed " + target.getName().getString()));
                        target.sendSystemMessage(Component.literal("You were kissed by " + player.getName().getString()));
                        level.addParticle(ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 0, 0, 0);
                        level.addParticle(ParticleTypes.HEART, target.getX(), target.getY() + 1, target.getZ(), 0, 0, 0);
                        level.playSound(null, player.blockPosition(), SoundEvents.CAT_PURR, SoundSource.PLAYERS, 1.0f, 1.0f);
                        level.playSound(null, target.blockPosition(), SoundEvents.CAT_PURR, SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }
        });
    }
}