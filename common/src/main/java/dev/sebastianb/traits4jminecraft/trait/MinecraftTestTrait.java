package dev.sebastianb.traits4jminecraft.mixin;

import net.minecraft.core.BlockPos;
import net.terradevelopment.traits4j.annotations.Trait;
import net.terradevelopment.traits4j.data.Var;

@Trait
public interface MinecraftTestTrait {

    default Var<BlockPos> getPos() {
        return new Var<>(BlockPos.ZERO);
    }

}
