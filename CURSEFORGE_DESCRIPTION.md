# Confluence TACZ Prefix Compat

**Give TACZ guns Confluence: Otherworld prefixes.** Guns can roll ranged prefixes when acquired, be reforged by the Goblin Tinkerer, and have prefix attributes mapped to firearm attributes.

Supports **Minecraft 1.20.1 (Forge)** and **Minecraft 1.21.1 (NeoForge)**. Install the file built for your Minecraft version and mod loader.

## Features

- **Prefixes on acquisition:** TACZ guns can roll a Confluence ranged prefix when obtained, including from loot, pickup, or crafting. The default chance is 75% and can be changed or disabled in the config.
- **Goblin Tinkerer reforging:** Guns are accepted for prefix reforging under Confluence's normal ranged-weapon rules.
- **Firearm attribute mapping:** By default, ranged damage, attack speed, and projectile velocity prefixes map to GunsmithLib's bullet damage, RPM, and bullet speed attributes. Prefix values and modifier operations are preserved.
- **Configurable prefix attributes:** Map any Confluence prefix attribute to another registered attribute. Confluence ranged prefixes use ranged damage, attack speed, critical chance, projectile velocity, and knockback. Critical chance and knockback need a target attribute supplied by another mod; that mod's gun logic must read the target attribute for it to affect shots.
- **Custom gun support:** Add other gun items to the `confluence_tacz_compat:guns` item tag to include them in prefix rolling, reforging, and attribute mapping.

Prefix data is applied through Confluence's own update logic, so the resulting rarity and item value follow Confluence's normal rules.

## Requirements

| Minecraft | Mod loader | Required mods | Optional mod |
|---|---|---|---|
| 1.20.1 | Forge 47.4.20+ | Confluence: Otherworld 1.2.6+, TACZ 1.1.8+ | GunsmithLib 5.x |
| 1.21.1 | NeoForge 21.1.219+ | Confluence: Otherworld 1.2.6+, TACZ 1.1.8+ | GunsmithLib 6.x |

GunsmithLib is optional. Without it, mappings to GunsmithLib attributes are skipped; prefix rolling and reforging remain available.

## Configuration

Edit `config/confluence_tacz_compat-common.toml`:

- `[acquire].enableAutoPrefix` enables or disables automatic prefixes on acquisition. Disabling this does **not** disable Goblin Tinkerer reforging.
- `[acquire].autoPrefixChance` sets the roll chance from `0.0` to `1.0` (default `0.75`).
- `[gunsmithlib].useGunsmithLibAttributes` enables or disables attribute replacement.
- `[gunsmithlib].attributeMappings` is a list of `source_attribute_id=target_attribute_id` entries. The default list maps damage, attack speed, and projectile velocity to GunsmithLib. Add a mapping for critical chance or knockback if another installed mod provides a suitable target attribute.

Only registered source and target attributes are used. Custom target attributes must also be read by the gun implementation to change projectile behavior.

## Adding custom guns

Add your item to the `confluence_tacz_compat:guns` item tag with a data pack or another mod. The tag directory differs by Minecraft version:

- 1.20.1: `data/<namespace>/tags/items/guns.json`
- 1.21.1: `data/<namespace>/tags/item/guns.json`

Example:

```json
{
  "replace": false,
  "values": ["your_mod:your_gun"]
}
```

---

# 中文说明

**让 TACZ 枪械接入《汇流来世》（Confluence: Otherworld）的词缀系统。** 枪械获取时可以随机获得远程词缀，也可以在哥布林工匠处重铸，并可将词缀属性映射到枪械属性。

支持 **Minecraft 1.20.1（Forge）** 与 **Minecraft 1.21.1（NeoForge）**。请按游戏版本和加载器选择对应文件。

## 功能

- **获取时随机词缀：** 枪械在战利品生成、拾取、合成等获取流程中可以获得 Confluence 远程词缀。默认概率为 75%，可在配置中修改或关闭。
- **哥布林工匠重铸：** 枪械可按 Confluence 普通远程武器的规则重铸词缀。
- **枪械属性映射：** 默认将远程伤害、攻击速度和弹速词缀映射到 GunsmithLib 的子弹伤害、RPM 和子弹速度属性，并保留词缀原有数值与运算方式。
- **自定义词缀属性映射：** 可将任意 Confluence 词缀属性映射到其他已注册属性。Confluence 远程词缀涉及远程伤害、攻击速度、暴击率、弹速和击退。暴击率与击退需要其他模组提供目标属性；枪械逻辑也必须读取该目标属性，词缀才会影响射击效果。
- **支持自定义枪械：** 将其他枪械物品加入 `confluence_tacz_compat:guns` 标签，即可参与词缀获取、重铸和属性映射。

词缀通过 Confluence 自身的更新逻辑应用，因此稀有度与物品价值遵循 Confluence 的正常规则。

## 依赖

| Minecraft | 加载器 | 必需模组 | 可选模组 |
|---|---|---|---|
| 1.20.1 | Forge 47.4.20+ | Confluence: Otherworld 1.2.6+、TACZ 1.1.8+ | GunsmithLib 5.x |
| 1.21.1 | NeoForge 21.1.219+ | Confluence: Otherworld 1.2.6+、TACZ 1.1.8+ | GunsmithLib 6.x |

GunsmithLib 为可选依赖。未安装时，指向 GunsmithLib 属性的映射会跳过；随机词缀与重铸功能仍可使用。

## 配置

编辑 `config/confluence_tacz_compat-common.toml`：

- `[acquire].enableAutoPrefix` 控制获取枪械时是否自动生成词缀。关闭后仍可在哥布林工匠处重铸。
- `[acquire].autoPrefixChance` 设置词缀生成概率，范围为 `0.0` 至 `1.0`，默认 `0.75`。
- `[gunsmithlib].useGunsmithLibAttributes` 控制是否启用属性替换。
- `[gunsmithlib].attributeMappings` 使用 `来源属性ID=目标属性ID` 的列表格式。默认映射伤害、攻击速度和弹速；若其他已安装模组提供合适属性，也可以追加暴击率或击退的映射。

只有已注册的来源和目标属性会参与映射。自定义目标属性还必须由枪械逻辑读取，才能改变弹丸效果。

## 添加自定义枪械

通过数据包或其他模组，将物品加入 `confluence_tacz_compat:guns` 标签。数据目录因版本而异：

- 1.20.1：`data/<命名空间>/tags/items/guns.json`
- 1.21.1：`data/<命名空间>/tags/item/guns.json`

示例：

```json
{
  "replace": false,
  "values": ["your_mod:your_gun"]
}
```
