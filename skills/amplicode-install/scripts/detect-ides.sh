#!/usr/bin/env bash
#
# Detects locally installed IntelliJ IDEA (Ultimate/Community) and GigaIDE installations.
# Prints a JSON array of candidates on stdout. Empty array if nothing found.
#
# No external deps beyond bash, find, grep, sed. We assemble JSON by hand to avoid
# requiring jq on the user's machine.

set -u

OS="$(uname -s)"

# ---------- helpers ----------

json_escape() {
  # Escape backslashes, quotes, and control chars for JSON string literal.
  local s="$1"
  s="${s//\\/\\\\}"
  s="${s//\"/\\\"}"
  s="${s//$'\n'/\\n}"
  s="${s//$'\r'/\\r}"
  s="${s//$'\t'/\\t}"
  printf '%s' "$s"
}

# Read a string field from product-info.json without requiring jq.
read_pi_field() {
  local file="$1" field="$2"
  grep -E "\"$field\"" "$file" 2>/dev/null \
    | head -n1 \
    | sed -E 's/.*"'"$field"'"[[:space:]]*:[[:space:]]*"([^"]*)".*/\1/'
}

# Given a product-info.json path, figure out the matching launcher binary for this OS.
# Sets globals: EXE_PATH, IDE_ROOT, APP_BUNDLE (macOS only; empty otherwise)
locate_launcher() {
  local pi="$1"
  EXE_PATH=""
  IDE_ROOT=""
  APP_BUNDLE=""

  local pi_dir
  pi_dir="$(dirname "$pi")"

  # macOS .app bundle: product-info.json lives in Contents/Resources, binary in ../MacOS/idea
  if [ -x "$pi_dir/../MacOS/idea" ]; then
    EXE_PATH="$(cd "$pi_dir/../MacOS" && pwd)/idea"
    IDE_ROOT="$(cd "$pi_dir/../.." && pwd)"
    APP_BUNDLE="$IDE_ROOT"
    return 0
  fi

  # Linux/Windows layout: product-info.json at the install root, launcher in bin/
  if [ -x "$pi_dir/bin/idea.sh" ]; then
    EXE_PATH="$pi_dir/bin/idea.sh"
    IDE_ROOT="$pi_dir"
    return 0
  fi
  if [ -x "$pi_dir/bin/idea" ]; then
    EXE_PATH="$pi_dir/bin/idea"
    IDE_ROOT="$pi_dir"
    return 0
  fi

  return 1
}

# Returns 0 if the candidate matches our target list (IDEA U/C or GigaIDE).
is_target() {
  local product_code="$1" name="$2"

  case "$product_code" in
    IU|IC) return 0 ;;
  esac

  # GigaIDE may use its own productCode; fall back to a name match.
  if printf '%s' "$name" | grep -qiE 'giga[[:space:]]*ide'; then
    return 0
  fi

  return 1
}

# Check whether Amplicode is already present in this IDE's plugins directory.
amplicode_installed_in() {
  local plugins_dir="$1"
  [ -d "$plugins_dir" ] || return 1
  # Plugin folder is typically "Amplicode" (sometimes versioned like "Amplicode-2025.x.x").
  local d
  while IFS= read -r -d '' d; do
    case "$(basename "$d")" in
      [Aa][Mm][Pp][Ll][Ii][Cc][Oo][Dd][Ee]*) return 0 ;;
    esac
  done < <(find "$plugins_dir" -maxdepth 1 -mindepth 1 -type d -print0 2>/dev/null)
  return 1
}

# Compute the runtime system directory.
# Vendor namespace is used in per-user IntelliJ Platform directories.
system_dir_for() {
  local data_dir_name="$1" vendor="${2:-JetBrains}"
  case "$OS" in
    Darwin) printf '%s' "$HOME/Library/Caches/$vendor/$data_dir_name" ;;
    Linux)  printf '%s' "$HOME/.cache/$vendor/$data_dir_name" ;;
    *)
      if [ -n "${LOCALAPPDATA:-}" ]; then
        printf '%s' "$LOCALAPPDATA/$vendor/$data_dir_name"
      else
        printf '%s' "$HOME/AppData/Local/$vendor/$data_dir_name"
      fi
      ;;
  esac
}

# Returns 0 when this script is running under the target IDE process.
is_descendant_of_pid() {
  local target_pid="$1"
  [ -n "$target_pid" ] || return 1
  local pid=$$
  local depth=0
  while [ -n "$pid" ] && [ "$pid" != "0" ] && [ "$pid" != "1" ]; do
    if [ "$pid" = "$target_pid" ]; then
      return 0
    fi
    pid="$(ps -o ppid= -p "$pid" 2>/dev/null | tr -d ' \t\n')"
    depth=$((depth + 1))
    [ "$depth" -gt 50 ] && return 1
  done
  return 1
}

# Returns the IDE PID from its .pid file when the process is still running.
ide_running_pid() {
  local data_dir_name="$1" vendor="${2:-JetBrains}"
  local system_dir
  system_dir="$(system_dir_for "$data_dir_name" "$vendor")"
  local pid_file="$system_dir/.pid"
  [ -f "$pid_file" ] || return 1
  local pid
  pid="$(head -n1 "$pid_file" 2>/dev/null | tr -dc '0-9')"
  [ -n "$pid" ] || return 1
  if kill -0 "$pid" 2>/dev/null; then
    printf '%s' "$pid"
    return 0
  fi
  return 1
}

# Compute the plugins directory from dataDirectoryName and vendor namespace.
plugins_dir_for() {
  local data_dir_name="$1" vendor="${2:-JetBrains}"
  case "$OS" in
    Darwin)
      printf '%s' "$HOME/Library/Application Support/$vendor/$data_dir_name/plugins"
      ;;
    Linux)
      printf '%s' "$HOME/.local/share/$vendor/$data_dir_name"
      ;;
    *)
      # MSYS/Cygwin fallback (Windows users should use the PowerShell script instead)
      if [ -n "${APPDATA:-}" ]; then
        printf '%s' "$APPDATA/$vendor/$data_dir_name/plugins"
      else
        printf '%s' "$HOME/AppData/Roaming/$vendor/$data_dir_name/plugins"
      fi
      ;;
  esac
}

# ---------- search ----------

# Storage for unique product-info.json paths.
pi_paths=()

add_pi() {
  local p="$1"
  [ -f "$p" ] || return 0
  local resolved
  if command -v realpath >/dev/null 2>&1; then
    resolved="$(realpath "$p" 2>/dev/null || printf '%s' "$p")"
  else
    resolved="$p"
  fi
  local existing
  for existing in "${pi_paths[@]:-}"; do
    [ "$existing" = "$resolved" ] && return 0
  done
  pi_paths+=("$resolved")
}

# Recursively find product-info.json files under a search root, limited depth.
# Search depth is limited per install location.
scan_root() {
  local root="$1" max_depth="$2"
  [ -d "$root" ] || return 0
  local pi
  while IFS= read -r -d '' pi; do
    add_pi "$pi"
  done < <(find "$root" -maxdepth "$max_depth" -name "product-info.json" -print0 2>/dev/null)
}

case "$OS" in
  Darwin)
    # Apps: /Applications/IntelliJ IDEA.app/Contents/Resources/product-info.json — depth 4
    scan_root "/Applications" 4
    scan_root "$HOME/Applications" 5
    # Toolbox v2 on macOS keeps apps under ~/Library/Application Support/JetBrains/Toolbox/apps
    scan_root "$HOME/Library/Application Support/JetBrains/Toolbox/apps" 8
    ;;
  Linux)
    # Installs: /opt/<ide>/product-info.json — depth 2
    scan_root "/opt" 3
    # Snap: /snap/<ide>/current/product-info.json — depth 3
    scan_root "/snap" 4
    # User-local installs and Toolbox
    scan_root "$HOME" 4
    scan_root "$HOME/.local/share/JetBrains/Toolbox/apps" 6
    ;;
  *)
    # MSYS/Cygwin/Git-Bash on Windows — limited support; recommend PowerShell script
    if [ -n "${LOCALAPPDATA:-}" ]; then
      scan_root "$LOCALAPPDATA/Programs" 3
      scan_root "$LOCALAPPDATA/JetBrains/Toolbox/apps" 6
    fi
    ;;
esac

# ---------- build JSON ----------

results=()

for pi in "${pi_paths[@]:-}"; do
  [ -f "$pi" ] || continue

  locate_launcher "$pi" || continue
  [ -n "$EXE_PATH" ] || continue

  product_code="$(read_pi_field "$pi" productCode)"
  product_name="$(read_pi_field "$pi" name)"
  version="$(read_pi_field "$pi" version)"
  data_dir_name="$(read_pi_field "$pi" dataDirectoryName)"
  product_vendor="$(read_pi_field "$pi" productVendor)"
  [ -n "$product_vendor" ] || product_vendor="JetBrains"

  [ -n "$product_code" ] || continue
  [ -n "$data_dir_name" ] || continue

  is_target "$product_code" "$product_name" || continue

  plugins_dir="$(plugins_dir_for "$data_dir_name" "$product_vendor")"

  if amplicode_installed_in "$plugins_dir"; then
    amplicode_installed=true
  else
    amplicode_installed=false
  fi

  running_pid="$(ide_running_pid "$data_dir_name" "$product_vendor" || true)"
  if [ -n "$running_pid" ]; then
    running=true
    pid_json="$running_pid"
    if is_descendant_of_pid "$running_pid"; then
      hosts_current_process=true
    else
      hosts_current_process=false
    fi
  else
    running=false
    pid_json="null"
    hosts_current_process=false
  fi

  if [ -n "$APP_BUNDLE" ]; then
    app_bundle_json='"'"$(json_escape "$APP_BUNDLE")"'"'
  else
    app_bundle_json="null"
  fi

  edition=""
  case "$product_code" in
    IU) edition="Ultimate" ;;
    IC) edition="Community" ;;
  esac

  display="$product_name"
  [ -n "$edition" ] && display="$product_name $edition"
  [ -n "$version" ] && display="$display $version"

  entry=""
  entry+='{'
  entry+='"name":"'"$(json_escape "$display")"'",'
  entry+='"dataDirectoryName":"'"$(json_escape "$data_dir_name")"'",'
  entry+='"exePath":"'"$(json_escape "$EXE_PATH")"'",'
  entry+='"amplicodeInstalled":'"$amplicode_installed"','
  entry+='"running":'"$running"','
  entry+='"pid":'"$pid_json"','
  entry+='"hostsCurrentProcess":'"$hosts_current_process"','
  entry+='"appBundle":'"$app_bundle_json"
  entry+='}'

  results+=("$entry")
done

# Emit JSON array
if [ "${#results[@]}" -eq 0 ]; then
  printf '[]\n'
else
  printf '['
  for i in "${!results[@]}"; do
    [ "$i" -gt 0 ] && printf ','
    printf '%s' "${results[$i]}"
  done
  printf ']\n'
fi
