package iloveyousomuch.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.chat.Component;

import java.util.List;

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
                ILoveYouSoMuch.LOGGER.info("Kiss message received from {}", player.getName().getString());
                Level level = player.level();
                double reach = 3.0;
                Vec3 start = player.getEyePosition();
                Vec3 direction = player.getLookAngle();
                Vec3 end = start.add(direction.scale(reach));
                AABB box = new AABB(start, end).inflate(1.0);
                List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box, p -> p != player);
                ServerPlayer target = null;
                double minDist = Double.MAX_VALUE;
                for (ServerPlayer p : players) {
                    Vec3 pos = p.getBoundingBox().getCenter();
                    Vec3 vec = pos.subtract(start);
                    double dist = vec.length();
                    if (dist <= reach && dist > 0) {
                        Vec3 normalizedVec = vec.normalize();
                        double dot = direction.dot(normalizedVec);
                        if (dot > 0.8) { // angle < ~36 degrees
                            if (dist < minDist) {
                                minDist = dist;
                                target = p;
                            }
                        }
                    }
                }
                if (target != null) {
                    ILoveYouSoMuch.LOGGER.info("Kissing player: {}", target.getName().getString());
                    player.sendSystemMessage(Component.literal("You kissed ♥" + target.getName().getString() + "♥"));
                    target.sendSystemMessage(Component.literal("You were kissed by ♥" + player.getName().getString() + "♥"));
                    ((ServerLevel)level).sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 3, 1.0, 1.0, 1.0, 0.0);
                    ((ServerLevel)level).sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1, target.getZ(), 3, 1.0, 1.0, 1.0, 0.0);
                    level.playSound(null, player.blockPosition(), SoundEvents.CAT_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);
                    level.playSound(null, target.blockPosition(), SoundEvents.CAT_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    ILoveYouSoMuch.LOGGER.info("No valid target found for kiss");
                }
            }
        });
    }
}