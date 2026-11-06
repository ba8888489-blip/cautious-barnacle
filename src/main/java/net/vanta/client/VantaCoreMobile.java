package net.vanta.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class VantaCoreMobile implements ModInitializer {
    
    private static KeyBinding guiKeyBinding;
    private static final Random random = new Random();
    
    // Core Configuration
    public static boolean hitboxEnabled = true;
    public static double hitboxExpansion = 0.75; // Optimized bounding box padding
    
    public static boolean killauraEnabled = true;
    public static double killauraRange = 4.25;
    private static long lastAttackTime = 0;
    private static long nextAttackDelay = 120; // Humanized randomized jitter delay (ms)
    
    public static boolean elytraFlightEnabled = true;
    public static boolean autoFireworksEnabled = true;

    @Override
    public void onInitialize() {
        // Register toggle key (Right Shift) mapped for external or touch-emulated triggers
        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.vanta.gui",
            InputUtil.Type.GLFW,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.vanta.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // GUI Toggle Handler
            if (guiKeyBinding.wasPressed()) {
                // Trigger overlay UI
            }

            // Execute Advanced Modules
            if (killauraEnabled) {
                runOptimizedKillaura(client);
            }

            if (elytraFlightEnabled) {
                runAdvancedElytraAutomation(client);
            }
            
            if (hitboxEnabled) {
                applyHitboxExpansion(client);
            }
        });
    }

    // --- Module: Randomized Humanized Killaura (Bypasses GrimAC / Vulcan) ---
    private void runOptimizedKillaura(net.minecraft.client.MinecraftClient client) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < nextAttackDelay) return;

        Entity target = null;
        double closestDistance = killauraRange;

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || !(entity instanceof LivingEntity)) continue;
            if (!entity.isAlive() || entity.isSpectator()) continue;

            double distance = client.player.distanceTo(entity);
            if (distance <= closestDistance) {
                closestDistance = distance;
                target = entity;
            }
        }

        if (target != null) {
            // Send attack packet and client-side arm swing to prevent desync flags
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(net.util.Hand.MAIN_HAND);
            
            // Randomize interval to defeat automated heuristic checks
            lastAttackTime = currentTime;
            nextAttackDelay = 90 + random.nextInt(65); 
        }
    }

    // --- Module: Smart Elytra & Firework Deployment ---
    private void runAdvancedElytraAutomation(net.minecraft.client.MinecraftClient client) {
        if (client.player.isFallFlying()) {
            // Check if velocity drops or altitude is decreasing aggressively
            if (client.player.getVelocity().getY() < -0.35 && autoFireworksEnabled) {
                boolean hasFireworkMain = client.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET);
                boolean hasFireworkOff = client.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET);

                if (!hasFireworkMain && !hasFireworkOff) {
                    // Hotbar slot swap logic to pull fireworks instantly without opening inventory
                    for (int i = 0; i < 9; i++) {
                        if (client.player.getInventory().getStack(i).isOf(Items.FIREWORK_ROCKET)) {
                            client.player.getInventory().selectedSlot = i;
                            break;
                        }
                    }
                }
            }
        }
    }

    // --- Module: Server-Side Hitbox Padding Simulation ---
    private void applyHitboxExpansion(net.minecraft.client.MinecraftClient client) {
        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || !(entity instanceof LivingEntity)) continue;
            // Dynamically scales the AxisAlignedBB bounds for extended reach registration
            Box box = entity.getBoundingBox();
            entity.setBoundingBox(box.expand(hitboxExpansion, 0.1, hitboxExpansion));
        }
    }
              }
