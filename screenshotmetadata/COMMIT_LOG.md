# Commit Log

## 2026-02-13
- chore(release): bump version to 1.2.0
  - Add ModMenu capture profiles (`Full`, `Lightweight`, `Privacy`) and remove keybind/tag screen flow
  - Add config schema versioning + migration for older config files
  - Add privacy redaction preview in ModMenu
  - Add game mode metadata capture
  - Fix config screen scrolling freeze risk in ModMenu
  - Update dev ModMenu runtime compatibility for Minecraft 1.21.11

## 2026-02-03
- feat: privacy mode, templates, tags, and ModMenu UX polish (v1.1.0)
  - Add privacy mode (coords obfuscation, server IP hide, seed hash)
  - Add screenshot filename templates with presets
  - Add tag input screen and export tags to JSON/XMP
  - Add ModMenu tooltips and collapsible sections
  - Fix mixin package helper class load crash

## 2026-02-03
- chore(release): bump version to 1.0.4.2
  - Add weather metadata toggle and output
  - Add JSON-only modpack context (resource packs, shaders, mod list)
  - Improve ModMenu section rendering and scrolling
  - Prefer iTXt PNG metadata with tEXt fallback
