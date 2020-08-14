package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.LocalPlayerUpdateMovementEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class PacketFly extends ToggleMod {
	
	public PacketFly() {
		super(Category.PLAYER, "PacketFly", false, "Packet Fly");
	}
	
	@SubscribeEvent
	public void onPlayerUpdate(LocalPlayerUpdateMovementEvent.Pre event) {
		event.setCanceled(true);
		MC.player.setVelocity(0, 0, 0);
		
        MC.player.motionY = -0.04f;
        MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX + MC.player.motionX, MC.player.posY + MC.player.motionY, MC.player.posZ + MC.player.motionZ, MC.player.rotationYaw, MC.player.rotationPitch, MC.player.onGround));
        
        double x = MC.player.posX + MC.player.motionX;
        double y = MC.player.posY + MC.player.motionY;
        double z = MC.player.posZ + MC.player.motionZ;
        y -= -1337.0;
        
        MC.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, MC.player.onGround));
	}
	
}
