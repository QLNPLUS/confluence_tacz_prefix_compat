# Confluence（汇流来世）词缀 / 稀有度系统 —— 远程（RANGED）词条全解

- 分析对象：`https://github.com/Magic-team-jvav/confluence`
- 分支 / 提交：`neoforge/1.21.1` @ `5d22de974b364146883bc22e80516a2a7a31c896`（源码在本地 `work/confluence` 稀疏检出，可直接检索）
- 稀有度定义来自同组织的 `Confluence-Magic-Lib`（`org.confluence.lib.common.component.ModRarity`，分支 `neoforge-dev/1.21.1`）
- 注意：**仓库内没有任何 TACZ（Timeless and Classics Zero）集成代码**，枪械体系是自家的 `terra_guns`（`org.confluence.terra_guns`，与 Confluence 同级 jarJar 子项目）

---

## 1. 结论速览

| 问题 | 答案 |
|---|---|
| 远程能滚到多少条词条？ | **36 条** = 通用 universal 14 + 通用武器 common 10 + 远程专属 ranged 12 |
| 词条影响哪些属性？ | 只有 5 个维度：**远程伤害%、攻击速度%、暴击率%、弹速%、击退** |
| 最强词条是哪个？ | **虚幻（Unreal）**：远程伤害 +15%、攻速 +10%、暴击 +5%、弹速 +10%、击退 +0.15（Terraria 原版同名顶级词条） |
| 词条只加属性吗？ | 不。还会**改物品稀有度（最多 ±2 级）**，并把物品价值按 `倍率²` 重算 |
| 负面词条会出现吗？ | 生成（掉落/拾取）时负面词条有 **2/3 概率被"无词条"吞掉**（惹恼 Annoying 例外，必中） |
| 哪些物品算"远程"？ | `Tags.Items.RANGED_WEAPON_TOOLS`（弓/弩类）+ `terra_guns:TGTags.GUN`（所有枪械）+ 原版三叉戟 |

---

## 2. 词条池是怎么组织的

`PrefixType`（`common/component/prefix/PrefixType.java:23`）把词条按"组"组合成池子：

```java
UNIVERSAL("universal"),
MELEE("universal", "common", "melee"),
RANGED("universal", "common", "ranged"),   // ← 远程
MAGIC("universal", "common", "magic"),
ACCESSORY("accessory"),
UNKNOWN
```

判定逻辑（`PrefixUtils.getPrefixType`，`util/PrefixUtils.java:63`）是**纯标签**的，从上到下依次判断：

| 判定标签 | 结果池 | 默认成员 |
|---|---|---|
| `confluence:prefix_universal_only` | UNIVERSAL | 钻头/电锯、回旋镖 |
| `confluence:prefix_melee_only` | MELEE | 剑/斧/镐/锹/锄/链锤 + 原版重锤 |
| **`confluence:prefix_ranged_only`** | **RANGED** | **`Tags.Items.RANGED_WEAPON_TOOLS`（原版/Confluence 弓弩）+ `terra_guns:GUN`（TerraGuns 枪械标签，Confluence 自己的枪也注册进去）+ `Items.TRIDENT`（三叉戟）** |
| `confluence:prefix_magic_only` | MAGIC | `confluence:mana_weapon`、`confluence:summoner_weapon` |
| `confluence:prefix_accessory_only` | ACCESSORY | `curios:accessory` |
| `confluence:unable_to_apply_prefix` | 不可上词条 | 服装商巫毒娃娃、向导巫毒娃娃 |

`RANGED` 池的 36 条 = `Universal.VALUES`(14) + `Common.VALUES`(10) + `Ranged.VALUES`(12)，`PrefixType.randomPrefix()` 是**等概率均匀抽取**（`PrefixType.java:79`）。

---

## 3. 远程专属词条 RANGED（12 条）

对照表头：`远程伤害 = LibAttributes.getRangedDamage()`（乘法）、`攻击速度 = minecraft:attack_speed`（乘法）、`暴击率 = LibAttributes.getCriticalChance()`（加法，+3% 即 +0.03）、`弹速 = LibAttributes.getRangedVelocity()`（乘法）、`击退 = minecraft:attack_knockback`（加法）、`价值倍率` 见 §6、`稀有度` 见 §6。

| ID | 词条 | 英文 | 远程伤害 | 攻击速度 | 暴击率 | 弹速 | 击退 | tier | 价值倍率 | 稀有度 |
|---:|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| 16 | 精准 | Sighted | +10% | — | +3% | — | — | 1 | 1.166 | +1 |
| 17 | 迅速 | Rapid | — | +15% | — | +10% | — | 2 | 1.265 | +2 |
| 18 | 急速 | Hasty | — | +10% | — | +15% | — | 2 | 1.265 | +2 |
| 19 | 恐怖 | Intimidating | — | — | — | +5% | +0.15 | 2 | 1.2075 | +2 |
| 20 | 致命 | Deadly | +10% | +5% | +2% | +5% | +0.05 | 2 | 1.3243 | +2 |
| 21 | 可靠 | Staunch | +10% | — | — | — | +0.15 | 2 | 1.265 | +2 |
| 22 | 可畏 | Awful | -15% | — | — | -10% | -0.1 | -2 | 0.6885 | -2 |
| 23 | 无力 | Lethargic | — | -15% | — | -10% | — | -2 | 0.765 | -2 |
| 24 | 粗笨 | Awkward | — | -10% | — | — | -0.2 | -2 | 0.72 | -2 |
| 25 | 强大 | Powerful | +15% | -10% | +1% | — | — | 1 | 1.0557 | +1 |
| 58 | 暴怒 | Frenzying | -15% | +15% | — | — | — | 0 | 0.9775 | ±0 |
| 82 | 虚幻 | Unreal | +15% | +10% | +5% | +10% | +0.15 | 2 | 1.7602 | +2 |

源码：`ModPrefix.java:316-380`（记录定义 + `createComponent`），ID 映射 `ModPrefix.java:470-536`。

> 两个容易看错的点：
> 1. **暴怒（Frenzying）是全池唯一 tier==0 的词条**（其余非负面词条都是 1 或 2），它用 -15% 伤害换 +15% 攻速，价值倍率 0.9775 → 稀有度不变。
> 2. **强大（Powerful）虽 tier=1，却带 -10% 攻速**，是同组里唯一"正面 tier + 负面属性"的词条。
> 另外 `canBeMercy()` 里写了 `this != FRENZYING`，但因为 FRENZYING 的 tier 是 0，那个判断实际永不生效（死代码）。

---

## 4. 通用词条 UNIVERSAL（14 条，远程也能滚到）

该组**没有弹速和攻速**，只有伤害/暴击/击退；伤害词条会按池子类型自动落到 `ranged_damage`（`ModPrefix.java:144-152`）。

| ID | 词条 | 英文 | 远程伤害 | 暴击率 | 击退 | tier | 价值倍率 | 稀有度 |
|---:|---|---|---:|---:|---:|---:|---:|---:|
| 36 | 锐利 | Keen | — | +3% | — | 1 | 1.06 | +1 |
| 37 | 高端 | Superior | +10% | +3% | +0.1 | 2 | 1.2826 | +2 |
| 38 | 强力 | Forceful | — | — | +0.15 | 1 | 1.15 | +1 |
| 39 | 碎裂 | Broken | -30% | — | -0.2 | -2 | 0.56 | -2 |
| 40 | 破损 | Damaged | -15% | — | — | -1 | 0.85 | -1 |
| 41 | 粗劣 | Shoddy | -10% | — | -0.15 | -2 | 0.765 | -2 |
| 53 | 致伤 | Hurtful | +10% | — | — | 1 | 1.1 | +1 |
| 54 | 强劲 | Strong | — | — | +0.15 | 1 | 1.15 | +1 |
| 55 | 粗鲁 | Unpleasant | +5% | — | +0.15 | 2 | 1.2075 | +2 |
| 56 | 虚弱 | Weak | — | — | -0.2 | -2 | 0.8 | -2 |
| 57 | 无情 | Ruthless | +18% | — | -0.1 | 1 | 1.062 | +1 |
| 59 | 神级 | Godly | +15% | +5% | +0.15 | 2 | 1.4548 | +2 |
| 60 | 恶魔 | Demonic | +15% | +5% | — | 2 | 1.265 | +2 |
| 61 | 狂热 | Zealous | — | +5% | — | 1 | 1.1 | +1 |

源码：`ModPrefix.java:115-177`。

---

## 5. 通用武器词条 COMMON（10 条，远程也能滚到）

这一组**有攻速、没有弹速**。

| ID | 词条 | 英文 | 远程伤害 | 攻击速度 | 暴击率 | 击退 | tier | 价值倍率 | 稀有度 |
|---:|---|---|---:|---:|---:|---:|---:|---:|---:|
| 42 | 迅捷 | Quick | — | +10% | — | — | 1 | 1.1 | +1 |
| 43 | 致命 | Deadly | +10% | +10% | — | — | 2 | 1.21 | +2 |
| 44 | 灵活 | Agile | — | +10% | +3% | — | 1 | 1.166 | +1 |
| 45 | 灵巧 | Nimble | — | +5% | — | — | 1 | 1.05 | +1 |
| 46 | 残暴 | Murderous | +7% | +6% | +3% | — | 2 | 1.2023 | +2 |
| 47 | 缓慢 | Slow | — | -15% | — | — | -1 | 0.85 | -1 |
| 48 | 迟钝 | Sluggish | — | -20% | — | — | -2 | 0.8 | -2 |
| 49 | 呆滞 | Lazy | — | -8% | — | — | -1 | 0.92 | -1 |
| 50 | 惹恼 | Annoying | -20% | -15% | — | — | -2 | 0.68 | -2 |
| 51 | 凶险 | Nasty | +5% | +10% | +2% | -0.1 | 1 | 1.0811 | +1 |

源码：`ModPrefix.java:179-243`。

**重名提示**：`致命`（Deadly）在 RANGED 与 COMMON 各有一条，中英文名相同但数值完全不同（RANGED 版多了弹速 +5% 与击退 +0.05，价值倍率 1.3243 vs 1.21）；`急速/迅捷/快速/灵活` 等名字也同时出现在饰品池里。做兼容或做数据统计时**必须用 `(type, name)` 作为唯一键**，不能只用 `name`。

---

## 6. 词条如何改写稀有度与价值

代码：`PrefixUtils.setAndUpdate`（`util/PrefixUtils.java:104-406`）。流程分三步：

### 6.1 先按 Terraria 原版规则算一个"价值倍率" `num14`

```
num14 = num2 × (2 − num4) × (2 − num7) × num5 × num3 × num6 × (1 + num8 × 0.02)
       伤害      使用速度        魔力消耗     尺寸    击退    弹速       暴击%
```

对远程池：`num2`=伤害、`num4`=攻速（<1 表示更快）、`num3`=击退、`num6`=弹速、`num8`=暴击百分点（`num5` 尺寸与 `num7` 魔力对远程恒为 1）。上面的"价值倍率"列就是这个值（源码里的 `value` 字段是另一套遗留数值，**代码里没有任何地方读它**，别拿来算价值）。

### 6.2 用 num14 挪动稀有度（最多 ±2 级）

| num14 | 稀有度变化 |
|---|---|
| ≥ 1.2 | **+2** |
| ≥ 1.05 | **+1** |
| 0.95 < x < 1.05 | 不变 |
| ≤ 0.95 | **−1** |
| ≤ 0.8 | **−2** |

- 结果被夹在 **[−1, 11]** 之间（最低灰色、最高紫色）。
- 若物品当前稀有度是**任务/专家/大师**（`-11/-12/-13`）则**完全不参与**调整（`if (rarity > -11)`）。
- 若物品没有 Confluence 稀有度组件、只有原版稀有度，会先把 COMMON/UNCOMMON/RARE/EPIC 折算成 0/2/4/6 再算。
- 组件不存在的物品按 0（白色）起算。

### 6.3 稀有度等级表（`ModRarity`，Confluence-Magic-Lib）

| ID | 名称 | 颜色 | 说明 |
|---:|---|---|---|
| -13 | master | 动态火红 | 大师模式专属 |
| -12 | expert | 动态彩虹 | 专家模式专属/宝藏袋 |
| -11 | quest | `#FFAF00` | 任务物品 |
| -10 / -9 / -8 / -7 | common / uncommon / rare / epic | 原版四色 | 仅作为原版稀有度的折算来源 |
| -1 | gray | `#828282` | 最低 |
| 0 | white | `#FFFFFF` | |
| 1 | blue | `#9696FF` | |
| 2 | green | `#96FF96` | |
| 3 | orange | `#FFC896` | |
| 4 | light_red | `#FF9696` | |
| 5 | pink | `#FF96FF` | |
| 6 | light_purple | `#D2A0FF` | |
| 7 | lime | `#96FF0A` | |
| 8 | yellow | `#FFFF0A` | |
| 9 | cyan | `#05C8FF` | |
| 10 | red | `#FF2864` | |
| 11 | purple | `#B428FF` | 只有被词条顶上去才会出现 |

游戏内文档（`assets/confluence/ageratum/zh_cn/detailed_information/mechanic/rarity.md`）也写了同一句话：**"物品的修饰语会影响其稀有度，可以提升或降低多达 2 级别"**。

### 6.4 价值

```java
VALUE = ValueComponent.getValue(stack, 50, true) × num14 × num14   // 注意是平方
```

所以虚幻（1.7602）会把物品价值抬到约 **3.10 倍**，碎裂（0.56）压到约 **0.31 倍**。重铸花费 `getReforgeCost` = 价值/3，再乘哥布林工匠的心情系数（`PrefixUtils.java:418`）。

---

## 7. 属性具体怎么落地

词条本身只是一个数据组件 `confluence:prefix`（`PrefixComponent`，`ModDataComponentTypes.java:20`，**persistent 但不同步**，客户端从物品栈自行读取），属性通过两个事件注入：

| 事件 | 位置 | 作用 |
|---|---|---|
| `ItemAttributeModifierEvent` | `ItemEvents.java:66-81` | 非饰品词条 → 以 **`EquipmentSlotGroup.MAINHAND`** 加入属性修饰符（**必须拿在主手才生效**；UNKNOWN 类型则不加） |
| `CurioAttributeModifierEvent` | `GameEvents.java:36-46` | 饰品词条 → 加到 Curios 槽位 |

各属性的消费端（`Confluence-Magic-Lib` 的 `LibAttributes`）：

- **远程伤害** `RangedDamage`：`applyRangedDamage()` 只在 `damageSource.is(DamageTypeTags.IS_PROJECTILE)` 时把伤害 `× 属性值`。
- **弹速** `RangedVelocity`：`applyToArrow()` 对 `AbstractArrow` 缩放 `deltaMovement`；TerraGuns 侧在 `GunEvent.AmmoDataEvent` 里 `velocity × 属性值`、`knockback × attack_knockback 属性值`（`ItemEvents.java:154-171`）。
- **暴击率** `CriticalChance`：`applyToArrow()` 用 `checkChance` 决定箭矢是否 `setCritArrow`。
- **击退** `minecraft:attack_knockback`：箭矢走 `original × (1 + 属性值)`，枪械走 `knockback × 属性值`（武器自身通过 `ModItems.attributes(damage, knockback)` 提供基础击退）。
- **攻击速度** `minecraft:attack_speed`：原版使用动画速度。

UI 表现：物品名会变成 `词条名 + 空格 + 原名` 并**沿用该物品的稀有度颜色**（`GameClientEvents.java:291-308`，分隔符 `confluence.prefix_separator`），例如"虚幻 手炮"整串按紫色渲染。

---

## 8. 词条什么时候产生

| 触发点 | 位置 | 说明 |
|---|---|---|
| 战利品表生成 | `mixin/.../LootTableMixin.java:18` | 宝箱/掉落物 |
| 拾取 / 进入背包 | `mixin/.../InventoryMixin.java:70,77` | 玩家背包新增物品 |
| 实体装备 | `LivingEntityEvents.java:566` | |

`PrefixUtils.initPrefix`（`PrefixUtils.java:38`）：

1. **75%** 概率才尝试赋词条，否则给 `unknown`（无词条）；
2. 从池中等概率抽一条；
3. 若该词条 `canBeMercy()==true`（即 `tier < 0`，例外：通用组除惹恼、近战除可耻、魔法除威猛、远程除暴怒）再掷 **2/3** 概率把它变成"无词条"——这就是负面词条在实战中少见的机制。

远程池里**会被 mercy 过滤**的：可畏、无力、粗笨、碎裂、破损、粗劣、虚弱、缓慢、迟钝、呆滞（10 条）；**不会被 mercy 过滤**：惹恼（`Common.canBeMercy()` 显式排除）。

手动/管理员接口：

```
/confluence reforge random   # 主手物品随机重铸
/confluence reforge best     # 直接给最优词条（远程 → 虚幻 Unreal，PrefixType.java:87）
/confluence reforge clear    # 清除词缀并还原稀有度/价值组件
/confluence reforge set ...  # 指定组与词条
```

`best` 对远程写死为 `Ranged.UNREAL`；NPC 侧有哥布林工匠的重铸界面（`NPCReforgeScreen` / `WithForgeTradeScreen`）。

---

## 9. 给 TACZ（或其它枪械模组）做兼容时的要点

1. **入口就一个标签**：把枪物品加进 `confluence:prefix_ranged_only` 即可进入远程池（数据包路径 `data/confluence/tags/item/prefix_ranged_only.json`，因为该标签由 `ModTags.Items.register` → `Confluence.asTagKey(Registries.ITEM, id)` 注册为 `confluence:` 命名空间）。现有成员是 `Tags.Items.RANGED_WEAPON_TOOLS` + `terra_guns:GUN` + `minecraft:trident`。
2. **属性注入依赖主手**：`ItemAttributeModifierEvent` 用的是 `EquipmentSlotGroup.MAINHAND`；如果枪的伤害不走玩家属性、而是走自己的弹丸实体，那么这个 +15% 远程伤害需要你在弹丸上自行读取 `LibAttributes.getRangedDamage()`。
3. **`applyRangedDamage()` 有前置条件**：只有 `DamageSource` 命中 `DamageTypeTags.IS_PROJECTILE` 才会乘算。**如果 TACZ 的子弹伤害类型没有打上 `is_projectile` 标签，Confluence 的远程伤害词条对它是完全无效的**——这是最容易踩的坑（需要在数据包里给对应 damage_type 打标签，或自行在伤害事件里乘算）。
4. **`applyToArrow()` 只管 `AbstractArrow`**：TACZ 的子弹不是箭，弹速与"箭矢暴击"不会自动生效，必须自己接 `GunEvent.AmmoDataEvent` 那样的钩子或读属性。
5. **组件是不同步的**：`confluence:prefix` 只 persistent。若你要在客户端做 HUD/伤害预估，需要自己同步；`PrefixComponent` 虽然写了 `STREAM_CODEC`，但注册时没有挂 `networkSynchronized`。
6. **代码坑**：`ModPrefix.Ranged.createComponent`（`ModPrefix.java:354`）里弹速的判空写成了 `if (value != 0.0F)`（用的是遗留的 `value` 字段而不是 `velocity`）。当前 12 条 RANGED 词条的 `value` 恰好都非 0，所以行为正常；**但如果你照抄这段代码添加自定义词条（比如 value=0 的），弹速加成就不会生效**。
7. **名字不是唯一键**：`deadly`(致命)、`quick`/`hasty` 在不同组里重复，`PrefixComponent.name()` 只有字符串名字 + `type`，翻译键是 `prefix.confluence.<name>`——同名跨组词条共用同一条翻译，做自定义词条时注意别撞名。
8. **仓库确实没有 TACZ 相关代码**（全仓 grep `tacz|timeless` 无命中），所以兼容层需要从零写。

---

## 10. 关键源码索引

| 内容 | 文件:行 |
|---|---|
| 远程词条定义与数值 | `ConfluenceOtherworld/src/main/java/org/confluence/mod/common/component/prefix/ModPrefix.java:316-380` |
| 词条 ID 映射表 | 同上 `:454-539` |
| 池子组成（RANGED = universal+common+ranged） | `.../component/prefix/PrefixType.java:23-104` |
| 数据组件定义 | `.../component/prefix/PrefixComponent.java:15-48` |
| 生成 / 价值 / 稀有度改写 | `.../util/PrefixUtils.java:38-406` |
| num2..num8 与 num14 的 switch 表 | 同上 `:117-383` |
| 远程物品标签成员 | `.../data/gen/tag/ModItemTagsProvider.java:516-528` |
| 属性注入（主手 / 饰品） | `.../event/game/entity/ItemEvents.java:66-81`、`.../event/game/GameEvents.java:36-46` |
| 枪械弹速/击退读取属性 | `.../event/game/entity/ItemEvents.java:154-171` |
| 词条名渲染 | `.../client/event/GameClientEvents.java:291-308` |
| 中文译名 | `.../data/gen/ModChineseProvider.java:957-1042` |
| 稀有度等级与颜色 | `Confluence-Magic-Lib: src/main/java/org/confluence/lib/common/component/ModRarity.java` |
| 属性消费端 | `Confluence-Magic-Lib: src/main/java/org/confluence/lib/common/LibAttributes.java:91-138` |
| 游戏内稀有度说明文档 | `assets/confluence/ageratum/zh_cn/detailed_information/mechanic/rarity.md` |

> 附：`work/prefix_calc.json` 是由源码自动解析出的 36 条远程池词条的原始数值（含 num14 与稀有度增减），`work/tables.md` 是上表的生成结果，可用于二次加工。
