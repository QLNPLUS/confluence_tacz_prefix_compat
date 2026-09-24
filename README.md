# Confluence TACZ Prefix Compat

让 **TACZ** 的枪械接入 **汇流来世（Confluence: Otherworld）** 的装备词条系统，
并把词条对枪械无效的原版属性替换为 **GunsmithLib** 的枪械属性。

## 功能

1. **获取时随机词条**：TACZ 枪械在获取（掉落、拾取、合成等）时按 Confluence 的远程词条池
   随机获得词条，可通过配置开关与概率调整。
2. **哥布林工匠重铸**：枪械可以放进 Confluence 哥布林工匠的重铸界面重掷词条，规则与普通远程武器一致。
3. **词条属性映射到 GunsmithLib**：

   | Confluence 词条属性 | 替换为 | 说明 |
   |---|---|---|
   | 远程伤害 `confluence_magic_lib:generic.ranged_damage` | `gunsmithlib:bullet_damage` | +N% 子弹伤害 |
   | 攻击速度 `minecraft:attack_speed` | `gunsmithlib:rpm` | +N% 射速 |
   | 远程弹速 `confluence_magic_lib:generic.ranged_velocity` | `gunsmithlib:bullet_speed` | +N% 子弹飞行速度 |

   GunsmithLib 在结算时会把属性的 base 临时设为枪械自身数值再套用修饰符，
   因此 Confluence 的百分比词条语义可以原样平移。其它远程词缀属性也能在配置中指定映射。

## 依赖

| 依赖 | 1.20.1 Forge | 1.21.1 NeoForge | 必需 |
|---|---|---|---|
| Minecraft | 1.20.1 | 1.21.1 | 是 |
| 加载器 | Forge 47.4.20+ | NeoForge 21.1.219+ | 是 |
| Confluence: Otherworld | 1.2.6+ | 1.2.6+ | 是 |
| TACZ | 1.1.8+ | 1.1.8+ | 是 |
| GunsmithLib | 5.x | 6.x | 否（缺失时词条保留 Confluence 原属性） |

## 配置

`config/confluence_tacz_compat-common.toml`：

```toml
[acquire]
	# 关闭后，TACZ 枪械在获取时不会自动获得 Confluence 词条。
	# 注意：关闭只影响“自动获取”，哥布林工匠处的重铸仍然可用。
	enableAutoPrefix = true
	# 自动获得词条的概率，取值 [0,1]。Confluence 默认值为 0.75。
	autoPrefixChance = 0.75

[gunsmithlib]
	# 总开关。关闭后不会执行任何属性映射。
	useGunsmithLibAttributes = true
	# 每项格式："来源属性 ID=目标属性 ID"。默认值沿用已有的伤害、射速和弹速映射。
	attributeMappings = [
		"confluence_magic_lib:generic.ranged_damage=gunsmithlib:bullet_damage",
		"minecraft:attack_speed=gunsmithlib:rpm",
		"confluence_magic_lib:generic.ranged_velocity=gunsmithlib:bullet_speed",
		# 需要时，追加已注册的暴击率和击退目标属性：
		# "confluence_magic_lib:generic.critical_chance=your_mod:bullet_critical_chance",
		# "minecraft:attack_knockback=your_mod:bullet_knockback",
	]
	# 目标属性必须由已加载模组注册，且枪械逻辑需要读取它才会影响弹丸效果。
	replaceRangedDamage = true
	replaceAttackSpeed = true
	replaceRangedVelocity = true
```

配置为 COMMON 类型，客户端与服务端都会读取同一份文件；专用服务器上以服务端文件为准。

## 枪械判定与数据包扩展

TACZ 的所有枪械共用同一个物品 `tacz:modern_kinetic_gun`，且 TACZ 本身没有“全部枪械”的物品标签。
本模组提供标签 `confluence_tacz_compat:guns`（默认包含该物品），并把它并入
`confluence:prefix_ranged_only`，因此枪械会走 Confluence 的远程词条池。

整合包若要让自定义枪械物品也参与，只需向标签追加：

```json
// data/<你的命名空间>/tags/item/guns.json （1.20.1 为 tags/items/）
{
  "replace": false,
  "values": ["你的模组:你的枪械物品"]
}
```

## 构建

```powershell
# 1.20.1 Forge（JDK 17）
.\gradlew build
# 1.21.1 NeoForge（JDK 21）
cd neoforge-1.21.1
.\gradlew build
```

两个分支都会从 CurseForge 坐标自动取得编译所需的 Confluence 及其内置库，
无需手动准备依赖 jar。

## 验证状态

| 项目 | 1.20.1 Forge | 1.21.1 NeoForge |
|---|---|---|
| 编译（真实 Confluence 产物） | 通过 | 通过 |
| 发布 jar 内容检查（元数据、Mixin 配置、数据包标签） | 通过 | 通过 |
| 专用服务器启动到 `Done` | 未执行（见下） | 通过，模组与依赖共 22 项全部加载 |
| 词条标签 / 重铸资格（`couldReforge`） | 未执行 | 通过 |
| 获取时随机词条 + 配置开关 A/B | 未执行 | 通过（开：40 次判定 23 次获得词条；关：0 次） |
| 词条属性映射到 GunsmithLib | 未执行 | 通过（旧版默认映射逐条核对：`ranged_damage`→`gunsmithlib:bullet_damage`、`attack_speed`→`gunsmithlib:rpm`、`ranged_velocity`→`gunsmithlib:bullet_speed`，数值与运算方式不变；暴击率与击退保持原样） |

本次可配置映射在 1.20.1 Forge 与 1.21.1 NeoForge 均通过 `compileJava`；两边都未启动游戏检查新 TOML 映射的运行时行为。

1.20.1 分支未做运行时验证的原因：Confluence 1.20.1 强制依赖的 **PortLib** 没有发布到
任何公开 Maven、CurseForge、Modrinth 或 GitHub Release，无法在开发环境中组装出可启动的依赖集合。
该分支的编译是针对真实 Confluence 1.20.1 产物（含其内置的 Magic-Lib 与 TerraCurio）完成的。

## 已知限制

- **暴击率与击退需要额外配置目标属性**：Confluence 对应来源 ID 为
  `confluence_magic_lib:generic.critical_chance` 和 `minecraft:attack_knockback`。GunsmithLib 没有内置目标属性；
  可映射到其它模组注册的属性，但只有在枪械逻辑读取该目标属性时才会影响子弹效果。
- **关闭自动词条只影响“获取时”**：已经带有词条的枪械不会被清除；想换词条请用哥布林工匠重铸。
- **1.20.1 的重铸价格更高**：Confluence 1.20.1 的 `getReforgeCost` 尚未实现心情折扣
  （上游留有 `todo trade`），因此重铸费用约为 1.21.1 的三倍。这是上游行为，本模组不做修改。
- 词条写入使用 Confluence 自身的 `setAndUpdate`，因此稀有度与物品价值的变化与普通武器完全一致。

## 仓库结构

本仓库每个加载器一个分支，各自独立构建：

| 分支 | 工作树 | 加载器 | Minecraft | JDK |
|---|---|---|---|---|
| `forge-1.20.1`（主分支） | 仓库根目录 | Forge | 1.20.1 | 17 |
| `neoforge-1.21.1` | `neoforge-1.21.1/` | NeoForge | 1.21.1 | 21 |

可移植逻辑（枪械标签、词条获取拦截）在两个分支间逐字节相同，通过
`git cherry-pick -x` 传播；加载器相关的元数据、配置与属性事件适配层各自维护。
详见各分支的 `AGENTS.md`。
