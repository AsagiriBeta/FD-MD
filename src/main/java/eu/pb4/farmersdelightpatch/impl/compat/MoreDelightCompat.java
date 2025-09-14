package eu.pb4.farmersdelightpatch.impl.compat;

import eu.pb4.farmersdelightpatch.impl.item.PolyBaseItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 与 More Delight 模组的兼容：
 * 1. 资源桥接到服务器端资源包（便于无客户端安装玩家也能看到模型/贴图）。
 * 2. 为所有 moredelight 物品注册 overlay，使其在客户端以服务端提供的替代模型显示。
 * 3. 为所有 moredelight 状态效果注册空同步对象，避免 vanilla 客户端收到未知状态效果导致断线。
 */
public final class MoreDelightCompat {
    public static final String MOD_ID = "moredelight";
    private static final Logger LOGGER = LoggerFactory.getLogger("FD-MD|MoreDelightCompat");

    private MoreDelightCompat() {}

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            return; // 未加载则跳过
        }

        // 1) 资源桥接（把 moredelight 的资源并入服务器资源包）
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        // 需要物品与方块模型（部分食物可能放置为方块）
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(MOD_ID, "item"));
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(MOD_ID, "block"));

        // 2) 物品 overlay（客户端把所有 moredelight 物品“看作”原版可识别物品）
        int itemCount = 0;
        for (var id : Registries.ITEM.getIds()) {
            if (id.getNamespace().equals(MOD_ID)) {
                var item = Registries.ITEM.get(id);
                PolymerItem.registerOverlay(item, new PolyBaseItem(item));
                itemCount++;
            }
        }

        // 3) 状态效果：为 vanilla 客户端隐藏（返回 null 即不向客户端同步具体对象）
        int effectCount = 0;
        for (var id : Registries.STATUS_EFFECT.getIds()) {
            if (id.getNamespace().equals(MOD_ID)) {
                var effect = Registries.STATUS_EFFECT.get(id);
                PolymerSyncedObject.setSyncedObject(Registries.STATUS_EFFECT, effect, (s, c) -> null);
                effectCount++;
            }
        }

        if (itemCount > 0 || effectCount > 0) {
            LOGGER.info("MoreDelight 兼容: 已处理 {} 个物品 overlay, 隐藏 {} 个状态效果", itemCount, effectCount);
        }
    }
}
