package com.matt.forgehax.mods;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.math.AlignHelper.Align;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

@RegisterMod
public class InventoryViewer extends HudMod {

  private final Setting<Boolean> background =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("background")
          .description("show background")
          .defaultTo(true)
          .build();
  
  private final Setting<Float> red =
        getCommandStub()
            .builders()
            .<Float>newSettingBuilder()
            .name("red")
            .description("red in rgb of background")
            .defaultTo(255F)
            .min(0F)
            .max(255F)
            .build();
  private final Setting<Float> blue =
        getCommandStub()
            .builders()
            .<Float>newSettingBuilder()
            .name("blue")
            .description("blue in rgb of background")
            .defaultTo(255F)
            .min(0F)
            .max(255F)
            .build();
  private final Setting<Float> green =
        getCommandStub()
            .builders()
            .<Float>newSettingBuilder()
            .name("green")
            .description("green in rgb of background")
            .defaultTo(255F)
            .min(0F)
            .max(255F)
            .build();
  private final Setting<Float> alpha =
        getCommandStub()
            .builders()
            .<Float>newSettingBuilder()
            .name("alpha")
            .description("alpha (opacity) in rgb of background")
            .defaultTo(150F)
            .min(0F)
            .max(255F)
            .build();

  public InventoryViewer() {
    super(Category.RENDER, "InventoryViewer", false, "Render inventory on screen");
  }
  
  int width = 16;
  int height = 16;
  
  @Override
  protected Align getDefaultAlignment() { return Align.TOPLEFT; }
  @Override
  protected int getDefaultOffsetX() { return 2; }
  @Override
  protected int getDefaultOffsetY() { return 5; }
  @Override
  protected double getDefaultScale() { return 1d; }

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
  	  
	  if(MC.currentScreen != null) {
		  return;
	  }
	  
  	  int xOff = (int)(getPosX(0));
  	  int yOff = (int)(getPosY(0));
  	  
  	  ResourceLocation backgroundImage = new ResourceLocation("background", "background.png");
  	  MC.getTextureManager().bindTexture(backgroundImage);
  	  RenderHelper.enableGUIStandardItemLighting();
  	  GL11.glPushMatrix();
  	  GL11.glScalef(scale.getAsFloat(), scale.getAsFloat(), scale.getAsFloat());
  	  GlStateManager.color(red.get() / 255F, green.get() / 255F, blue.get() / 255F, alpha.get() / 255F);
      if (background.get())
          MC.ingameGUI.drawTexturedModalRect(xOff, yOff, 0, 0, 148, 52); 
      for (int i = 0; i < 27; i++)
      {
          ItemStack itemStack = MC.player.inventory.mainInventory.get(i + 9);
          int offsetX = (int) xOff + 2 + (i % 9) * 16;
          int offsetY = (int) yOff + 2 + (i / 9) * 16;
          MC.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
          MC.getRenderItem().renderItemOverlayIntoGUI(MC.fontRenderer, itemStack, offsetX, offsetY, null);
      }
      width = 16 * 9;
      height = 16 * 3;
      RenderHelper.disableStandardItemLighting();
      MC.getRenderItem().zLevel = 0.0F;
      GlStateManager.color(1F, 1F, 1F, 1F);
  	  GL11.glScalef(1, 1, 1);
      GL11.glPopMatrix();
  }
        
}