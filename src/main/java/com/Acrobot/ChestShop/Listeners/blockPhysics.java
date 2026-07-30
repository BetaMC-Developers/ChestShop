package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.Utils.uSign;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Sign;

import java.util.Arrays;
import java.util.List;

public class blockPhysics extends BlockListener {
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.SAND && block.getType() != Material.GRAVEL) {
            return;
        }

        if (shouldCancel(event.getBlock().getRelative(BlockFace.UP), BlockFace.DOWN)) {
            event.setCancelled(true);

            return;
        }

        List<BlockFace> horizontals = Arrays.asList(
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
        );
        for (BlockFace face: horizontals) {
            Block neighbor = event.getBlock().getRelative(face);
            if (neighbor.getState() != null && (neighbor.getState().getData() instanceof Attachable)) {
                Attachable data = (Attachable) neighbor.getState().getData();
                if (data.getAttachedFace() == face.getOppositeFace() && shouldCancel(neighbor, face.getOppositeFace())) {
                    event.setCancelled(true);

                    return;
                }
            }
        }
    }

    private boolean shouldCancel(Block block, BlockFace expectedAttachedTo) {
        if (!uSign.isSign(block)) {
            return false;
        }

        CraftSign state = (CraftSign) block.getState();
        if (!uSign.isValid(state)) {
            return false;
        }

        return ((Sign) state.getData()).getAttachedFace() == expectedAttachedTo;
    }
}
