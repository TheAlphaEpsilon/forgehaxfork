package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.events.PlayerTravelEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@RegisterMod
public class ElytraFlight extends ToggleMod {
  
	private final Setting<Double> glideSpeed =
		  getCommandStub()
		  	.builders()
		  	.<Double>newSettingBuilder()
		  	.name("glideSpeed")
		  	.description("How fast you fall")
		  	.defaultTo(0.1)
		  	.build();
  
	private final Setting<Double> flySpeed =
		  getCommandStub()
		  	.builders()
		  	.<Double>newSettingBuilder()
		  	.name("flySpeed")
		  	.description("How fast you fly")
		  	.defaultTo(1.8)
		  	.build();
  
	private final Setting<Boolean> ncpStrict =
			getCommandStub()
				.builders()
				.<Boolean>newSettingBuilder()
				.name("ncpStrict")
				.defaultTo(true)
				.build();
	
	private final Setting<Integer> boostPitchControl =
			getCommandStub()
				.builders()
				.<Integer>newSettingBuilder()
				.name("boostPitchControl")
				.min(0)
				.max(90)
				.defaultTo(20)
				.build();
	
	private final Setting<Integer> forwardPitch = 
			getCommandStub()
				.builders()
				.<Integer>newSettingBuilder()
				.name("forwardPitch")
				.min(-90)
				.max(90)
				.defaultTo(0)
				.build();
	
	private int boostingTick = 0;
	private float packetPitch = 0;
	private float packetYaw = 0;
	
	public ElytraFlight() {
		super(Category.PLAYER, "ElytraFlight", false, "Elytra Flight");
	}
  
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onSendPacket(PacketEvent.Outgoing.Pre event) {
		if(MC.player != null && MC.player.isElytraFlying() && event.getPacket() instanceof CPacketPlayer) {
			CPacketPlayer packetPlayer = event.getPacket();
			if(!PacketHelper.isIgnored(packetPlayer) && packetPlayer instanceof CPacketPlayer.Rotation) {
				
				event.setCanceled(true);
				CPacketPlayer.Rotation toSend = new CPacketPlayer.Rotation(
						packetYaw, MC.gameSettings.keyBindJump.isKeyDown() ? packetPitch : 90, packetPlayer.isOnGround());
				
				PacketHelper.ignoreAndSend(toSend);
				
			} else if(!PacketHelper.isIgnored(packetPlayer) && packetPlayer instanceof CPacketPlayer.PositionRotation) {
				
				event.setCanceled(true);
				CPacketPlayer.PositionRotation toSend = new CPacketPlayer.PositionRotation(
						packetPlayer.getX(0), packetPlayer.getY(0), packetPlayer.getZ(0),
						packetYaw, MC.gameSettings.keyBindJump.isKeyDown() ? packetPitch : 90, packetPlayer.isOnGround());
				
				PacketHelper.ignoreAndSend(toSend);
				
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onReceivePacket(PacketEvent.Incoming.Pre event) {
		if(MC.player != null && MC.player.isElytraFlying() && event.getPacket() instanceof SPacketPlayerPosLook) {
			SPacketPlayerPosLook packet = event.getPacket();
			if(!PacketHelper.isIgnored(packet)) {
				event.setCanceled(true);
				SPacketPlayerPosLook toReceive = new SPacketPlayerPosLook(
						packet.getX(), packet.getY(), packet.getZ(),
						packet.getYaw(), MC.player.rotationPitch,
						packet.getFlags(), packet.getTeleportId());
				PacketHelper.ignoreAndReceive(toReceive);
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onLocalPlayerUpdate(PlayerTravelEvent event) {
		
		if(MC.player != null && MC.player.isElytraFlying()) {
		  		  
			event.setCanceled(true);

			double rads = Math.toRadians(MC.player.rotationYaw);
		  
			boolean forward = MC.gameSettings.keyBindForward.isKeyDown();
			boolean left = MC.gameSettings.keyBindLeft.isKeyDown();
			boolean back = MC.gameSettings.keyBindBack.isKeyDown();
			boolean right = MC.gameSettings.keyBindRight.isKeyDown();
		  
			boolean up = MC.gameSettings.keyBindJump.isKeyDown();
			boolean down = MC.gameSettings.keyBindSneak.isKeyDown();
		  
			if(forward && right) {
				rads += Math.toRadians(45);
			} else if(right && back) {
				rads += Math.toRadians(135);
			} else if(back && left) {
				rads += Math.toRadians(225);
			} else if(left && forward) {
				rads += Math.toRadians(315);
			} else if(right) {
				rads += Math.toRadians(90);
			} else if(back) {
				rads += Math.toRadians(180);
			} else if(left) {
				rads += Math.toRadians(270);
			} 
		  
			double negXPortion = Math.sin(rads);
		  
			double zPortion = Math.cos(rads);
		  
			if(!up && (forward || left || back || right)) {
				MC.player.motionX = flySpeed.getAsDouble() * -negXPortion;
				MC.player.motionY = -glideSpeed.getAsDouble() / 10000f;
				MC.player.motionZ = flySpeed.getAsDouble() * zPortion;
			}
		  
			if(up) {
				
				double currentSpeed = Math.sqrt(MC.player.motionX * MC.player.motionX + MC.player.motionZ * MC.player.motionZ);
				
				if (currentSpeed >= 0.8 || MC.player.motionY > 1.0) {
					goUp(currentSpeed, getYaw());
	            } else {
	                packetPitch = forwardPitch.getAsFloat();
	                MC.player.motionX = flySpeed.getAsDouble() * Math.sin(-getYaw());
					MC.player.motionY = -glideSpeed.getAsDouble() / 10000f;
					MC.player.motionZ = flySpeed.getAsDouble() * Math.cos(getYaw());
	                boostingTick = 0;
	            }
								
			} else if(down) {
				MC.player.motionX = -flySpeed.getAsDouble() / 1000f;
				MC.player.motionY = -flySpeed.getAsDouble() / 2f;
				MC.player.motionZ = -flySpeed.getAsDouble() / 1000f;
			} 
			
			if(!(up || down || left || right || forward || back)) {
				MC.player.motionX = 0;
				MC.player.motionY = -glideSpeed.getAsDouble() / 10000f;
				MC.player.motionZ = 0;
			}
			
			MC.player.move(MoverType.SELF, MC.player.motionX, MC.player.motionY, MC.player.motionZ);
		  
		}
	}
	
	private void goUp(double speed, double yaw) {
		
		double multipliedSpeed = 0.128 * Math.min(flySpeed.getAsDouble(), 2);
		double strictPitch = Math.toDegrees(Math.asin((multipliedSpeed - Math.sqrt(multipliedSpeed * multipliedSpeed - 0.0348)) / 0.12));
		
		double basePitch = (ncpStrict.get() && strictPitch < boostPitchControl.getAsDouble() && !Double.isNaN(strictPitch)) 
				? -strictPitch : -boostPitchControl.getAsDouble();
		
		double targetPitch = (MC.player.rotationPitch < 0) ?
				Math.max(MC.player.rotationPitch * (90D - boostPitchControl.getAsDouble()) / 90 - boostPitchControl.getAsDouble(), -90) :
				-boostPitchControl.getAsDouble();
				
		if(packetPitch <= basePitch && boostingTick > 2) {
			
			if(packetPitch < targetPitch) {
				packetPitch += 17;
			}
			if(packetPitch > targetPitch) {
				packetPitch -= 17;
			}
			
			packetPitch = (float) Math.max(packetPitch, targetPitch);
			
		} else {
			
			packetPitch = (float)basePitch;
			
		}
		
		boostingTick++;
		
		
		double pitch = Math.toRadians(packetPitch);
		
		double targetMotionX = Math.sin(-yaw) * Math.sin(-pitch);
		
		double targetMotionZ = Math.cos(yaw) * Math.sin(-pitch);
		
		double targetSpeed = Math.sqrt(targetMotionX * targetMotionX + targetMotionZ * targetMotionZ);
		
		double upSpeed = speed * Math.sin(-pitch) * 0.04;
		
		double fallSpeed = Math.cos(pitch) * Math.cos(pitch) * 0.06 - 0.08;
		
		MC.player.motionX -= upSpeed * targetMotionX / targetSpeed - (targetMotionX / targetSpeed * speed - MC.player.motionX) * 0.1;
		MC.player.motionY += upSpeed * 3.2 + fallSpeed;
		MC.player.motionZ -= upSpeed * targetMotionZ / targetSpeed - (targetMotionZ / targetSpeed * speed - MC.player.motionZ) * 0.1;
		
		MC.player.motionX *= 0.99;
		MC.player.motionY *= 0.98;
		MC.player.motionZ *= 0.99;
		
	}
	
	private double getYaw() {
        float strafeYawDeg = 90.0f * MC.player.movementInput.moveStrafe;
        strafeYawDeg *= (MC.player.movementInput.moveForward != 0.0f) ? MC.player.movementInput.moveForward * 0.5f : 1.0f;
        float yawDeg = MC.player.rotationYaw - strafeYawDeg;

        yawDeg -= (MC.player.movementInput.moveForward < 0.0f) ? 180 : 0;
        packetYaw = yawDeg;

        return Math.toRadians(yawDeg);
    }
}
