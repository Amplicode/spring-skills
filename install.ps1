$ErrorActionPreference = "Stop"

$RepoUrl = "https://github.com/Amplicode/spring-skills.git"
$BaseDir = "$HOME\.agents"
$RepoDir = "$BaseDir\.amplicode\spring-skills"
$AgentsSkillsDir = "$BaseDir\skills"
$QwenSkillsDir = "$HOME\.qwen\skills"
$KiloSkillsDir = "$HOME\.kilocode\skills"
$VeaiSkillsDir = "$HOME\.veai\skills"

Write-Host "== Amplicode Spring Skills Installer =="

New-Item -ItemType Directory -Force -Path "$BaseDir\.amplicode" | Out-Null
New-Item -ItemType Directory -Force -Path "$AgentsSkillsDir" | Out-Null

# --- Git sync ---
if (Test-Path "$RepoDir\.git") {
    Write-Host "✔ Repo exists, pulling latest..."
    git -C "$RepoDir" pull
} else {
    Write-Host "⬇ Cloning repository..."
    git clone $RepoUrl $RepoDir
}

# --- Symlinks to ~/.agents/skills/ (Codex, OpenCode, Gemini CLI) ---
Write-Host "🔗 Creating/updating symlinks in ~/.agents/skills/..."

Get-ChildItem "$RepoDir\skills" -Directory | ForEach-Object {
    $skillName = $_.Name
    $targetLink = Join-Path $AgentsSkillsDir $skillName

    if (Test-Path $targetLink) {
        Remove-Item $targetLink -Recurse -Force
    }

    cmd /c mklink /J "$targetLink" $_.FullName | Out-Null

    Write-Host "  ✔ $skillName"
}

Write-Host "✅ Skills ready (Codex, OpenCode, Gemini CLI)"

# --- KiloCode: symlinks to ~/.kilocode/skills/ ---
if (Test-Path "$HOME\.kilocode") {
    Write-Host "🔗 Creating/updating symlinks in ~/.kilocode/skills/..."
    New-Item -ItemType Directory -Force -Path "$KiloSkillsDir" | Out-Null

    Get-ChildItem "$RepoDir\skills" -Directory | ForEach-Object {
        $skillName = $_.Name
        $targetLink = Join-Path $KiloSkillsDir $skillName

        if (Test-Path $targetLink) {
            Remove-Item $targetLink -Recurse -Force
        }

        cmd /c mklink /J "$targetLink" $_.FullName | Out-Null

        Write-Host "  ✔ $skillName"
    }

    Write-Host "✅ KiloCode skills ready"
}

# --- Veai: symlinks to ~/.veai/skills/ ---
if (Test-Path "$HOME\.veai") {
    Write-Host "🔗 Creating/updating symlinks in ~/.veai/skills/..."
    New-Item -ItemType Directory -Force -Path "$VeaiSkillsDir" | Out-Null

    Get-ChildItem "$RepoDir\skills" -Directory | ForEach-Object {
        $skillName = $_.Name
        $targetLink = Join-Path $VeaiSkillsDir $skillName

        if (Test-Path $targetLink) {
            Remove-Item $targetLink -Recurse -Force
        }

        cmd /c mklink /J "$targetLink" $_.FullName | Out-Null

        Write-Host "  ✔ $skillName"
    }

    Write-Host "✅ Veai skills ready"
}

# --- Qwen Code: symlinks to ~/.qwen/skills/ ---
$qwenExists = (Test-Path "$HOME\.qwen") -or (Get-Command qwen -ErrorAction SilentlyContinue)

if ($qwenExists) {
    Write-Host "🔗 Creating/updating symlinks in ~/.qwen/skills/..."
    New-Item -ItemType Directory -Force -Path "$QwenSkillsDir" | Out-Null

    Get-ChildItem "$RepoDir\skills" -Directory | ForEach-Object {
        $skillName = $_.Name
        $targetLink = Join-Path $QwenSkillsDir $skillName

        if (Test-Path $targetLink) {
            Remove-Item $targetLink -Recurse -Force
        }

        cmd /c mklink /J "$targetLink" $_.FullName | Out-Null

        Write-Host "  ✔ $skillName"
    }

    Write-Host "✅ Qwen Code skills ready"
}

# --- Claude Code: marketplace plugin ---
$claudeExists = Get-Command claude -ErrorAction SilentlyContinue

if ($claudeExists) {
    Write-Host "🤖 Claude Code found, installing plugin..."

    try { claude plugin marketplace add $RepoUrl } catch {}
    try { claude plugin install spring-tools@spring-tools } catch {}
    try { claude plugin update spring-tools@spring-tools } catch {}

    Write-Host "✅ Claude Code plugin ready"
} else {
    Write-Host "⚠ Claude CLI not found, skipping Claude Code setup"
}

# --- GitHub Copilot CLI: marketplace plugin ---
$copilotExists = Get-Command copilot -ErrorAction SilentlyContinue

if ($copilotExists) {
    Write-Host "🤖 Copilot CLI found, installing plugin..."

    try { copilot plugin marketplace add $RepoUrl } catch {}
    try { copilot plugin install spring-tools@spring-tools } catch {}

    Write-Host "✅ Copilot plugin ready"
}

Write-Host "🎉 Done"
