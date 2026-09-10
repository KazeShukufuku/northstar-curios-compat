package com.createdelight.compat.northstarcurios.compat.kubejs;

import com.createdelight.compat.northstarcurios.api.EquipmentCheckEvent;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraftforge.common.MinecraftForge;

public final class NorthstarCompatKubeJSPlugin extends KubeJSPlugin {
    private static final EventGroup GROUP = EventGroup.of("NorthstarCuriosEvents");
    private static final EventHandler EQUIPMENT_CHECK = GROUP.startup("equipmentCheck", () -> EquipmentCheckEventJS.class);

    @Override
    public void registerEvents() {
        GROUP.register();
        MinecraftForge.EVENT_BUS.addListener(NorthstarCompatKubeJSPlugin::checkEquipment);
    }

    private static void checkEquipment(EquipmentCheckEvent event) {
        if (EQUIPMENT_CHECK.hasListeners()) {
            EQUIPMENT_CHECK.post(ScriptType.STARTUP, new EquipmentCheckEventJS(event));
        }
    }
}
