# Changelog

## 未发布

### 修复

- **兼容 TACZ 1.2.0**：TACZ 1.2.0 移除了 `GunHudOverlay.handleInventoryAmmo`，
  HUD 备弹改由 `ClientAmmoCount` / `LocalPlayerDataHolder` 统计，导致
  `GunHudOverlayMixin` 在 `onRegisterGuiOverlays` 阶段注入失败并崩溃。
  现在旧版注入通过 `require = 0` 声明为可选，1.2.0 改在 `handleCacheCount`
  返回时把 Confluence 弹药槽并入 HUD 备弹缓存，两个版本都能启动并显示弹药槽备弹。
  `hud.fixBackpackAmmoDoubleCount` 的去重逻辑仅对 1.1.x 及更早版本生效
  （1.2.0 原生只统计玩家物品栏，不存在该重复来源）。

- **与 TACZ-addon 的精妙背包弹药支持冲突**：此前为了优先消耗 Confluence 弹药槽，
  直接取消了 TACZ `consumeAmmoFromPlayer` 的整个方法体，导致 TACZ-addon 在该方法
  RETURN 处追加的精妙背包 / Create Storage / Curios 弹药来源被跳过，HUD 统计与实际消耗脱节。
  现在改为把弹药槽包装成玩家物品栏视图交给 TACZ 原生流程，弹药槽仍然优先扣除，
  第三方在 RETURN 处的补充逻辑继续生效，并且不会重复扣弹。
  涉及 `ModernKineticGunScriptAPI` 与 `LivingEntityShoot` 两条消耗路径。

- **HUD 备弹重复统计修复（可开关）**：新增 `hud.fixBackpackAmmoDoubleCount`，默认开启。
  TACZ-addon 重建 HUD 备弹视图时，同一精妙背包可能经 Create Storage 与
  Sophisticated Backpacks 两个兼容层各统计一次（背包 2498 时 HUD 显示 4996）。
  开启后本模组在 TACZ-addon 的虚拟物品栏结果上对非主物品栏部分按
  `物品 + NBT + 数量` 去重，再补上 Confluence 弹药槽；关闭后完全保留原统计。
- **HUD 备弹上限**：移除 TACZ 在背包直读模式下对弹药数的 9999 硬截断，
  使弹药槽与背包总和超过 9999 时仍显示真实数量。

### 新增

- **暴击率 tooltip 着色开关**：新增 `tooltip.colorCriticalChanceName`，默认开启。
  关闭后不再按 `attribute_display_style.json` 的 `name_color` 改写纯文本暴击率行末尾的属性名颜色，
  避免与 KubeJS 或其它 tooltip 模组自己设置的颜色冲突。

## 1.0.0

首个版本，同时适用于 1.20.1 Forge 与 1.21.1 NeoForge。

### 新增

- **枪械获取时随机获得词条**：TACZ 枪械会像汇流来世的远程武器一样，在掉落、拾取、合成等获取时机
  从远程词条池中随机获得词条，并按同样的规则写入稀有度与物品价值。
  可在配置文件中关闭，或调整获得概率。
- **哥布林工匠处重铸枪械词条**：枪械可以放入哥布林工匠的重铸界面，花费钱币重掷词条。
- **词条属性接入 GunsmithLib**：远程伤害、攻击速度、远程弹速三项词条改为作用在
  `gunsmithlib:bullet_damage`、`gunsmithlib:rpm`、`gunsmithlib:bullet_speed` 上，
  使词条真正影响子弹伤害、射速与弹速。三项可分别关闭。

### 说明

- GunsmithLib 为可选依赖。未安装时词条保留汇流来世原本的属性，其余功能不受影响。
- 暴击率与击退词条没有对应的 GunsmithLib 属性，对枪械不产生实际效果。
- 整合包可以通过数据包标签 `confluence_tacz_compat:guns` 追加自定义枪械物品。
