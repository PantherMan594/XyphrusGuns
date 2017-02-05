/*
 * Copyright (c) 2016 David Shen. All Rights Reserved.
 * Created by PantherMan594.
 */

package pw.xyphrusrealms.XyphrusGuns;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * Created by david on 7/04.
 */
class Recipes {

    Recipes() {
        for (XyphrusGuns.Gun gun : XyphrusGuns.Gun.values()) {
            ItemStack gunItem = new ItemStack(Material.DIAMOND_HOE, 1, (short) gun.getId());
            ItemMeta gunMeta = gunItem.getItemMeta();

            gunMeta.setDisplayName(gun.getName());
            gunMeta.spigot().setUnbreakable(true);

            gunItem.setItemMeta(gunMeta);

            ShapedRecipe gunRecipe = new ShapedRecipe(gunItem);

            switch (gun) {
                case REVOLVER:
                    gunRecipe.shape("  I", "GIO", "RO ");
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    break;
                case GLOCK:
                    gunRecipe.shape("  I", "GIO", "RO ");
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    break;
                case CROSSBOW:
                    gunRecipe.shape("  I", "GIO", "RO ");
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    break;
                case M16:
                    gunRecipe.shape("  I", "GIO", "RO ");
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    break;
                case AS50:
                    gunRecipe.shape("  I", "GIO", "RO ");
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    MaterialData stainedPane = new MaterialData(Material.STAINED_GLASS_PANE, (byte) 5);
                    gunRecipe.setIngredient('G', stainedPane);
                    gunRecipe.setIngredient('O', Material.OBSIDIAN);
                    gunRecipe.setIngredient('R', Material.BLAZE_ROD);
                    break;
                case M32:
                    gunRecipe.shape(" TI", "TFT", "RT ");
                    gunRecipe.setIngredient('T', Material.TNT);
                    gunRecipe.setIngredient('I', Material.IRON_BLOCK);
                    gunRecipe.setIngredient('F', Material.FIREBALL);
                    gunRecipe.setIngredient('R', Material.BLAZE_ROD);
                    break;
                case SPAS12:
                    gunRecipe.shape("  I", "CI ", "RC ");
                    gunRecipe.setIngredient('I', Material.IRON_INGOT);
                    gunRecipe.setIngredient('C', Material.COAL_BLOCK);
                    gunRecipe.setIngredient('R', Material.BLAZE_ROD);
                    break;
                case BLUNDERBUSS:
                    gunRecipe.shape("  W", " I ", "R  ");
                    gunRecipe.setIngredient('I', Material.IRON_INGOT);
                    gunRecipe.setIngredient('R', Material.BLAZE_ROD);
                    MaterialData wood = new MaterialData(Material.WOOD, (byte) 0);
                    gunRecipe.setIngredient('W', wood);
                    XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
                    wood.setData((byte) 1);
                    gunRecipe.setIngredient('W', wood);
                    XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
                    wood.setData((byte) 2);
                    gunRecipe.setIngredient('W', wood);
                    XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
                    wood.setData((byte) 3);
                    gunRecipe.setIngredient('W', wood);
                    XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
                    wood.setData((byte) 4);
                    gunRecipe.setIngredient('W', wood);
                    XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
                    wood.setData((byte) 5);
                    gunRecipe.setIngredient('W', wood);
                    break;
                case MUSKET:
                    gunRecipe.shape("  I", " I ", "R  ");
                    gunRecipe.setIngredient('I', Material.IRON_INGOT);
                    gunRecipe.setIngredient('R', Material.BLAZE_ROD);
                    break;
            }

            XyphrusGuns.getInstance().getServer().addRecipe(gunRecipe);
        }
    }
}
