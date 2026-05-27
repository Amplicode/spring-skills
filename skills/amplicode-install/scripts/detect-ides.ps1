# Detects locally installed IntelliJ IDEA (Ultimate/Community) and GigaIDE installations on Windows.
# Prints a JSON array of candidates on stdout. Empty array if nothing found.
#
# Usage: pwsh detect-ides.ps1   (or  powershell -ExecutionPolicy Bypass -File detect-ides.ps1)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = 'Continue'

# ---------- search roots ----------
$searchRoots = @()
if ($env:LOCALAPPDATA) {
    $searchRoots += "$env:LOCALAPPDATA\Programs"
    $searchRoots += "$env:LOCALAPPDATA\JetBrains\Toolbox\apps"
}
$searchRoots += "C:\Program Files\JetBrains"
$searchRoots += "C:\Program Files (x86)\JetBrains"

# ---------- find product-info.json files ----------
$piPaths = New-Object System.Collections.Generic.HashSet[string]
foreach ($root in $searchRoots) {
    if (-not (Test-Path -LiteralPath $root)) { continue }
    $found = Get-ChildItem -Path $root -Filter 'product-info.json' -Recurse -Depth 6 -ErrorAction SilentlyContinue
    foreach ($f in $found) {
        $null = $piPaths.Add($f.FullName)
    }
}

# Fallback: scan all fixed drives at top level to catch custom install paths.
$skipTop = @('Windows', 'Windows.old', '$Recycle.Bin', 'System Volume Information',
             'PerfLogs', 'Recovery', 'Boot', 'EFI', 'MSOCache', 'OneDriveTemp')
$containerNames = @('Program Files', 'Program Files (x86)', 'Programs', 'Apps')
$ideNamePattern = '(?i)^(idea|intellij|giga|jetbrains|amplicode|toolbox)'
try {
    $fixedDrives = Get-CimInstance -ClassName Win32_LogicalDisk -Filter 'DriveType=3' -ErrorAction Stop |
        ForEach-Object { "$($_.DeviceID)\" }
} catch {
    $fixedDrives = Get-PSDrive -PSProvider FileSystem -ErrorAction SilentlyContinue |
        Where-Object { $_.Root -match '^[A-Z]:\\$' } |
        ForEach-Object { $_.Root }
}
foreach ($drive in $fixedDrives) {
    if (-not (Test-Path -LiteralPath $drive)) { continue }
    $topDirs = Get-ChildItem -LiteralPath $drive -Directory -Force -ErrorAction SilentlyContinue
    foreach ($td in $topDirs) {
        if ($skipTop -contains $td.Name) { continue }
        if ($td.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { continue }

        # Folders to inspect with the IDE-name filter: $td itself, plus its immediate
        # subdirectories if $td is a Program Files-style container.
        $candidates = New-Object System.Collections.Generic.List[object]
        $candidates.Add($td)
        if ($containerNames -contains $td.Name) {
            $sub = Get-ChildItem -LiteralPath $td.FullName -Directory -Force -ErrorAction SilentlyContinue
            foreach ($s in $sub) { $candidates.Add($s) }
        }

        foreach ($cf in $candidates) {
            if ($cf.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { continue }

            # Fast path: product-info.json directly inside this folder.
            $directPi = Join-Path $cf.FullName 'product-info.json'
            if (Test-Path -LiteralPath $directPi -PathType Leaf) {
                $null = $piPaths.Add($directPi)
            }

            # Deep path: only if the folder name suggests an IDE/JetBrains install.
            if ($cf.Name -match $ideNamePattern) {
                $found = Get-ChildItem -LiteralPath $cf.FullName -Filter 'product-info.json' -Recurse -Depth 4 -ErrorAction SilentlyContinue
                foreach ($f in $found) {
                    $null = $piPaths.Add($f.FullName)
                }
            }
        }
    }
}

# ---------- helpers ----------
function Find-Launcher {
    param([string]$piPath)

    $piDir = Split-Path -Parent $piPath
    # Windows IDE layout: product-info.json in install root, exe in bin\idea64.exe
    $candidates = @(
        (Join-Path $piDir 'bin\idea64.exe'),
        (Join-Path $piDir 'bin\idea.bat'),
        (Join-Path $piDir 'bin\idea.exe')
    )
    foreach ($c in $candidates) {
        if (Test-Path -LiteralPath $c) { return $c }
    }
    return $null
}

function Test-Target {
    param([string]$productCode, [string]$productName)
    if ($productCode -in @('IU', 'IC')) { return $true }
    if ($productName -match '(?i)giga\s*ide') { return $true }
    return $false
}

function Test-AmplicodeInstalled {
    param([string]$pluginsDir)
    if (-not (Test-Path -LiteralPath $pluginsDir)) { return $false }
    $hits = Get-ChildItem -LiteralPath $pluginsDir -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '(?i)^amplicode' }
    return [bool]$hits
}

function Get-PluginsDir {
    param([string]$dataDirName)
    if (-not $env:APPDATA) { return $null }
    return Join-Path $env:APPDATA "JetBrains\$dataDirName\plugins"
}

function Get-SystemDir {
    param([string]$dataDirName)
    if (-not $env:LOCALAPPDATA) { return $null }
    return Join-Path $env:LOCALAPPDATA "JetBrains\$dataDirName"
}

# Returns $true if the current PowerShell process is a descendant of $targetPid
# (walks ParentProcessId chain up from $PID). Used to detect the "self-host" case:
# the agent running in a JetBrains terminal inside the very IDE we are about to
# restart — killing it would kill the agent before installPlugins runs.
function Test-DescendantOf {
    param([int]$targetPid)
    if ($targetPid -le 0) { return $false }
    $cur = $PID
    $depth = 0
    while ($cur -and $cur -ne 0 -and $depth -lt 50) {
        if ($cur -eq $targetPid) { return $true }
        try {
            $proc = Get-CimInstance -ClassName Win32_Process -Filter "ProcessId=$cur" -ErrorAction Stop
            if (-not $proc) { return $false }
            $cur = [int]$proc.ParentProcessId
        } catch {
            return $false
        }
        $depth++
    }
    return $false
}

# If IDEA wrote a .pid file and the PID points at a live process, returns the PID.
# Otherwise returns $null. Mirrors the Unix detect-ides.sh implementation.
function Get-IdeRunningPid {
    param([string]$dataDirName)
    $systemDir = Get-SystemDir $dataDirName
    if (-not $systemDir) { return $null }
    $pidFile = Join-Path $systemDir '.pid'
    if (-not (Test-Path -LiteralPath $pidFile)) { return $null }
    $raw = (Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
    if (-not $raw) { return $null }
    $pidNum = 0
    if (-not [int]::TryParse(($raw -replace '\D', ''), [ref]$pidNum)) { return $null }
    if ($pidNum -le 0) { return $null }
    try {
        $null = Get-Process -Id $pidNum -ErrorAction Stop
        return $pidNum
    } catch {
        return $null
    }
}

# ---------- build results ----------
$results = @()
foreach ($pi in $piPaths) {
    try {
        $info = Get-Content -LiteralPath $pi -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        continue
    }

    $productCode = $info.productCode
    $productName = $info.name
    $version = $info.version
    $dataDirName = $info.dataDirectoryName

    if (-not $productCode -or -not $dataDirName) { continue }
    if (-not (Test-Target $productCode $productName)) { continue }

    $exePath = Find-Launcher $pi
    if (-not $exePath) { continue }

    $pluginsDir = Get-PluginsDir $dataDirName
    $amplicodeInstalled = if ($pluginsDir) { Test-AmplicodeInstalled $pluginsDir } else { $false }
    $runningPid = Get-IdeRunningPid $dataDirName
    $running = [bool]$runningPid
    $hostsCurrentProcess = if ($runningPid) { Test-DescendantOf $runningPid } else { $false }

    $edition = switch ($productCode) {
        'IU' { 'Ultimate' }
        'IC' { 'Community' }
        default { '' }
    }
    $display = if ($edition) { "$productName $edition $version" } else { "$productName $version" }

    $results += [pscustomobject]@{
        name              = $display.Trim()
        dataDirectoryName = $dataDirName
        exePath           = $exePath
        amplicodeInstalled = $amplicodeInstalled
        running           = $running
        pid               = $runningPid
        hostsCurrentProcess = $hostsCurrentProcess
        appBundle         = $null
    }
}

# Emit JSON array (always an array, even with a single element).
if ($results.Count -eq 0) {
    Write-Output '[]'
} else {
    ,$results | ConvertTo-Json -Depth 4 -Compress
}