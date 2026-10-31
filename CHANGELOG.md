# Changelog

## 未发布

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
