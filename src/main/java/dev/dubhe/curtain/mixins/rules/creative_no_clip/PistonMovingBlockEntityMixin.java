package dev.dubhe.curtain.mixins.rules.creative_no_clip;

import dev.dubhe.curtain.CurtainRules;
import dev.dubhe.curtain.features.player.patches.EntityPlayerMPFake;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonTileEntity.class)
public abstract class PistonMovingBlockEntityMixin {
    @Shadow
    private BlockState movedState;

    @Shadow
    public abstract Direction getMovementDirection();

    @Inject(method = "moveEntityByPiston", at = @At("HEAD"), cancellable = true)
    private static void dontPushSpectators(Direction direction, Entity entity, double d, Direction direction2, CallbackInfo ci) {
        if(!CurtainRules.creativeNoClip) return;
        if(!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity)entity;
        if(!player.isCreative()) return;
        if(!player.abilities.flying) return;
        ci.cancel();
    }

    @Redirect(method = "moveCollidedEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setDeltaMovement(DDD)V"))
    private void ignoreAccel(Entity entity, double x, double y, double z) {
        if (CurtainRules.creativeNoClip && entity instanceof PlayerEntity && ((PlayerEntity)entity).isCreative() && ((PlayerEntity)entity).abilities.flying) {
            return;
        }
        entity.setDeltaMovement(x, y, z);
    }

    @Redirect(
            method = "moveCollidedEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getPistonPushReaction()Lnet/minecraft/block/material/PushReaction;"
            )
    )
    private PushReaction moveFakePlayers(Entity entity) {
        if (entity instanceof EntityPlayerMPFake && movedState.is(Blocks.SLIME_BLOCK)) {
            Vector3d vec3d = entity.getDeltaMovement();
            Direction direction = getMovementDirection();
            double x = direction.getAxis() == Direction.Axis.X ? direction.getStepX() : vec3d.x;
            double y = direction.getAxis() == Direction.Axis.Y ? direction.getStepY() : vec3d.y;
            double z = direction.getAxis() == Direction.Axis.Z ? direction.getStepZ() : vec3d.z;
            entity.setDeltaMovement(x, y, z);
        }
        return entity.getPistonPushReaction();
    }
}
