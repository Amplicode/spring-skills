#!/usr/bin/env bash

set -e

PATH=$($SHELL -l -c 'echo $PATH' 2>/dev/null || echo "$PATH")
export PATH

REPO_URL="https://github.com/Amplicode/spring-skills.git"
BASE_DIR="$HOME/.agents"
REPO_DIR="$BASE_DIR/.amplicode/spring-skills"
AGENTS_SKILLS_DIR="$BASE_DIR/skills"
QWEN_SKILLS_DIR="$HOME/.qwen/skills"
KILO_SKILLS_DIR="$HOME/.kilocode/skills"
VEAI_SKILLS_DIR="$HOME/.veai/skills"

echo "== Amplicode Spring Skills Installer =="

mkdir -p "$BASE_DIR/.amplicode"
mkdir -p "$AGENTS_SKILLS_DIR"

# --- Git sync ---
if [ -d "$REPO_DIR/.git" ]; then
  echo "✔ Repo exists, pulling latest..."
  git -C "$REPO_DIR" pull
else
  echo "⬇ Cloning repository..."
  git clone "$REPO_URL" "$REPO_DIR"
fi

# --- Symlinks to ~/.agents/skills/ (Codex, OpenCode, Gemini CLI) ---
echo "🔗 Creating/updating symlinks in ~/.agents/skills/..."

for skill_path in "$REPO_DIR/skills/"*; do
  [ -d "$skill_path" ] || continue
  skill_name=$(basename "$skill_path")
  target_link="$AGENTS_SKILLS_DIR/$skill_name"

  if [ -L "$target_link" ] || [ -e "$target_link" ]; then
    rm -rf "$target_link"
  fi

  ln -s "$skill_path" "$target_link"
  echo "  ✔ $skill_name"
done

echo "✅ Skills ready (Codex, OpenCode, Gemini CLI)"

# --- KiloCode: symlinks to ~/.kilocode/skills/ ---
if [ -d "$HOME/.kilocode" ]; then
  echo "🔗 Creating/updating symlinks in ~/.kilocode/skills/..."
  mkdir -p "$KILO_SKILLS_DIR"

  for skill_path in "$REPO_DIR/skills/"*; do
    [ -d "$skill_path" ] || continue
    skill_name=$(basename "$skill_path")
    target_link="$KILO_SKILLS_DIR/$skill_name"

    if [ -L "$target_link" ] || [ -e "$target_link" ]; then
      rm -rf "$target_link"
    fi

    ln -s "$skill_path" "$target_link"
    echo "  ✔ $skill_name"
  done

  echo "✅ KiloCode skills ready"
fi

# --- Veai: symlinks to ~/.veai/skills/ ---
if [ -d "$HOME/.veai" ]; then
  echo "🔗 Creating/updating symlinks in ~/.veai/skills/..."
  mkdir -p "$VEAI_SKILLS_DIR"

  for skill_path in "$REPO_DIR/skills/"*; do
    [ -d "$skill_path" ] || continue
    skill_name=$(basename "$skill_path")
    target_link="$VEAI_SKILLS_DIR/$skill_name"

    if [ -L "$target_link" ] || [ -e "$target_link" ]; then
      rm -rf "$target_link"
    fi

    ln -s "$skill_path" "$target_link"
    echo "  ✔ $skill_name"
  done

  echo "✅ Veai skills ready"
fi

# --- Qwen Code: symlinks to ~/.qwen/skills/ ---
if [ -d "$HOME/.qwen" ] || command -v qwen >/dev/null 2>&1; then
  echo "🔗 Creating/updating symlinks in ~/.qwen/skills/..."
  mkdir -p "$QWEN_SKILLS_DIR"

  for skill_path in "$REPO_DIR/skills/"*; do
    [ -d "$skill_path" ] || continue
    skill_name=$(basename "$skill_path")
    target_link="$QWEN_SKILLS_DIR/$skill_name"

    if [ -L "$target_link" ] || [ -e "$target_link" ]; then
      rm -rf "$target_link"
    fi

    ln -s "$skill_path" "$target_link"
    echo "  ✔ $skill_name"
  done

  echo "✅ Qwen Code skills ready"
fi

# --- Claude Code: marketplace plugin ---
if command -v claude >/dev/null 2>&1; then
  echo "🤖 Claude Code found, installing plugin..."

  claude plugin marketplace add "$REPO_URL" || true
  claude plugin install spring-tools@spring-tools || true
  claude plugin update spring-tools@spring-tools || true

  echo "✅ Claude Code plugin ready"
else
  echo "⚠ Claude CLI not found, skipping Claude Code setup"
fi

# --- GitHub Copilot CLI: marketplace plugin ---
if command -v copilot >/dev/null 2>&1; then
  echo "🤖 Copilot CLI found, installing plugin..."

  copilot plugin marketplace add "$REPO_URL" || true
  copilot plugin install spring-tools@spring-tools || true

  echo "✅ Copilot plugin ready"
fi

echo "🎉 Done"
