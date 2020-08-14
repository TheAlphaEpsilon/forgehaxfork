package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class CoordsHud extends HudMod {
  
  public CoordsHud() {
    super(Category.RENDER, "CoordsHUD", false, "Display world coords");
  }
  
  private final Setting<Boolean> translate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("translate")
          .description("show corresponding Nether or Overworld coords")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> multiline =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("multiline")
          .description("show translated coords above")
          .defaultTo(true)
          .build();
  
  @Override
  protected Align getDefaultAlignment() { return Align.BOTTOMRIGHT; }
  @Override
  protected int getDefaultOffsetX() { return 1; }
  @Override
  protected int getDefaultOffsetY() { return 1; }
  @Override
  protected double getDefaultScale() { return 1d; }
  
  double thisX;
  double thisY;
  double thisZ;
  double otherX;
  double otherZ;
  double yaw;
  Rotation yawEnum = Rotation.NEGX;
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent ev) {
    if (getWorld() == null) return;
  
    EntityPlayerSP player = getLocalPlayer();
    thisX = player.posX;
    thisY = player.posY;
    thisZ = player.posZ;
    
    yaw = Math.abs(player.rotationYaw) % 360;
    	
	if(yaw < 22.5) {
		yawEnum = Rotation.POSZ;
	} else if(yaw >= 22.5 && yaw < 67.5) {
		yawEnum = Rotation.NEGXPOSZ;
	} else if(yaw >= 67.5 && yaw < 112.5) {
		yawEnum = Rotation.NEGX;
	} else if(yaw >= 112.5 && yaw < 157.5) {
		yawEnum = Rotation.NEGXNEGZ;
	} else if(yaw >= 157.5 && yaw < 202.5) {
		yawEnum = Rotation.NEGZ;
	} else if(yaw >= 202.5 && yaw < 247.5) {
		yawEnum = Rotation.POSXNEGZ;
	} else if(yaw >= 247.5 && yaw < 292.5) {
		yawEnum = Rotation.POSX;
	} else if(yaw >= 292.5 && yaw < 337.5) {
		yawEnum = Rotation.POSXPOSZ;
	} else if(yaw >= 337.5 && yaw < 360) {
		yawEnum = Rotation.POSZ;
	}
    
    double thisFactor = getWorld().provider.getMovementFactor();
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }
  
  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();
    
    if (!translate.get() || (translate.get() && multiline.get())) {
      text.add(String.format("%01.1f, %01.0f, %01.1f, (%01.0f %s)", thisX, thisY, thisZ, yaw, yawEnum.toString()));
    }
    if (translate.get()) {
      if (multiline.get()) {
        text.add(String.format("(%01.1f, %01.1f)", otherX, otherZ));
      } else {
        text.add(String.format(
            "%01.1f, %01.0f, %01.1f (%01.1f, %01.1f), (%01.0f %s)", thisX, thisY, thisZ, otherX, otherZ, yaw, yawEnum.toString()));
      }
    }
    
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());
  }
  
  static enum Rotation {
	  POSX, POSXPOSZ, POSZ, NEGXPOSZ, NEGX, NEGXNEGZ, NEGZ, POSXNEGZ;
	  
	  @Override
	  public String toString() {
		  switch(this) {
		  case POSX:
			  return "X+";
		  case POSXPOSZ:
			  return "X+/Z+";
		  case POSZ:
			  return "Z+";
		  case NEGXPOSZ:
			  return "X-/Z+";
		  case NEGX:
			  return "X-";
		  case NEGXNEGZ:
			  return "X-/Z-";
		  case NEGZ:
			  return "Z-";
		  case POSXNEGZ:
			  return "X+/Z-";
		  default:
			  return "";
		  }
	  }
	  
  }
  
}
