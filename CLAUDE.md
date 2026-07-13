# CLAUDE.md

Guidance for working in this repository.

## Bumping the plugin version

The plugin version is duplicated across several manifests and **must stay in sync** —
Claude Code, Codex, and the GitHub marketplace each read their own copy, so a
version left behind means that distribution channel silently keeps serving the old
release.

When bumping the version, update **every** occurrence below (6 fields in 5 files):

| File | Field(s) |
|------|----------|
| `.claude-plugin/plugin.json` | `version` |
| `.codex-plugin/plugin.json` | `version` |
| `.github/plugin/plugin.json` | `version` |
| `.github/plugin/marketplace.json` | `metadata.version` **and** `plugins[].version` |

`.claude-plugin/marketplace.json` has no version field — nothing to change there.

After bumping, verify they all match and none were missed:

```bash
grep -rn '"version"' --include="*.json" . | grep -v node_modules
```

Every printed version must be identical. There should be no remaining occurrence
of the previous version:

```bash
grep -rn '<old-version>' --include="*.json" . | grep -v node_modules   # expect no output
```
