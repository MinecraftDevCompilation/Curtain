package dev.dubhe.curtain.mixins.rules.xp_no_cooldown;

import dev.dubhe.curtain.CurtainRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
  @Shadow
  private int count;

  @Shadow
  private int value;

  public ExperienceOrbMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @Shadow
  protected abstract int repairPlayerItems(ServerPlayer player, int amount);

  @Inject(method = "playerTouch", at = @At("HEAD"))
  private void addXP(Player player, CallbackInfo ci) {
    if (CurtainRules.xpNoCooldown && !level().isClientSide) {
      player.takeXpDelay = 0;
      // reducing the count to 1 and leaving vanilla to deal with it
      while (this.count > 1) {
        int remainder = this.repairPlayerItems((ServerPlayer) player, this.value);
        if (remainder > 0) {
          player.giveExperiencePoints(remainder);
        }
        this.count--;
      }
    }
  }
}
