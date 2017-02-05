/*
 * Copyright (c) 2017 David Shen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
