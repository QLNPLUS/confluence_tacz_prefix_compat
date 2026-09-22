# Branch Matrix

This repository delivers two independent Minecraft mod branches:

| Branch | Worktree | Loader | Minecraft | JDK | Gradle/plugin |
|---|---|---|---|---|---|
| `forge-1.20.1` (primary) | `D:\projects\confluence_tacz_prefix_compat` | Forge 47.4.20 | 1.20.1 | 17 | Gradle 8.12 wrapper / `net.neoforged.moddev.legacyforge` 2.0.141 |
| `neoforge-1.21.1` | `D:\projects\confluence_tacz_prefix_compat\neoforge-1.21.1` | NeoForge 21.1.219 | 1.21.1 | 21 | Gradle 8.12 wrapper / `net.neoforged.moddev` 2.0.107 |

The primary branch is `forge-1.20.1`. Develop and verify one branch before propagating loader-independent changes with `git cherry-pick -x`; never hand-copy cross-branch changes. Platform-specific loader metadata and API adapters remain branch-specific and must be reviewed separately.

Release tags use `v<mod-version>-<loader>-<minecraft-version>`, for example `v1.0.0-forge-1.20.1` and `v1.0.0-neoforge-1.21.1`.

Known platform gaps: Forge 1.20.1 uses PortLib-backed Confluence prefix components and Forge attribute events; NeoForge 1.21.1 uses vanilla data components and NeoForge attribute events. Confluence `setAndUpdate` and `initPrefix` have compatible descriptors, but attribute key types differ (`Attribute` versus `Holder<Attribute>`), so the mapping adapters are intentionally platform-specific.
