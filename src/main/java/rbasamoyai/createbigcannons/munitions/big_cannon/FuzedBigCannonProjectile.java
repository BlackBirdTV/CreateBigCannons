package rbasamoyai.createbigcannons.munitions.big_cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

import java.util.function.Predicate;

public abstract class FuzedBigCannonProjectile extends AbstractBigCannonProjectile {

	private ItemStack fuze = ItemStack.EMPTY;
	
	protected FuzedBigCannonProjectile(EntityType<? extends FuzedBigCannonProjectile> type, Level level) {
		super(type, level);
	}
	
	public void setFuze(ItemStack stack) { this.fuze = stack == null ? ItemStack.EMPTY : stack; }
	
	@Override
	public void tick() {
		super.tick();
		if (this.canDetonate(fz -> fz.onProjectileTick(this.fuze, this))) this.detonate();
	}

	@Override
	protected boolean onClip(ProjectileContext ctx, BlockPos pos) {
		if (super.onClip(ctx, pos)) return true;
		if (this.canDetonate(fz -> fz.onProjectileClip(this.fuze, this, Vec3.atCenterOf(pos), ctx))) {
			this.detonate();
			return true;
		}
		return false;
	}

	@Override
	protected void onFinalImpact(HitResult result) {
		super.onHit(result);
		if (this.canDetonate(fz -> fz.onProjectileImpact(this.fuze, this, result))) this.detonate();
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("Fuze", this.fuze.serializeNBT());
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.fuze = ItemStack.of(tag.getCompound("Fuze"));
	}

	protected final boolean canDetonate(Predicate<FuzeItem> cons) {
		return !this.level.isClientSide && this.level.hasChunkAt(this.blockPosition()) && this.fuze.getItem() instanceof FuzeItem fuzeItem && cons.test(fuzeItem);
	}
	
	protected abstract void detonate();

}
