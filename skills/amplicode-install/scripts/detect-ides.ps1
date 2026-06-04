# Detects locally installed IntelliJ IDEA (Ultimate/Community) and GigaIDE installations on Windows.
# Prints a JSON array of candidates on stdout. Empty array if nothing found.
#
# Usage: pwsh detect-ides.ps1   (or  powershell -ExecutionPolicy Bypass -File detect-ides.ps1)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = 'Continue'

# ---------- bounded helpers ----------

# Runs a self-contained scriptblock with a wall-clock timeout.
# The scriptblock runs in a separate runspace and does not share caller variables.
function Invoke-WithTimeout {
    param(
        [Parameter(Mandatory)][scriptblock]$ScriptBlock,
        [int]$TimeoutMs = 5000
    )
    $ps = [System.Management.Automation.PowerShell]::Create()
    $null = $ps.AddScript($ScriptBlock.ToString())
    $async = $ps.BeginInvoke()
    if ($async.AsyncWaitHandle.WaitOne($TimeoutMs)) {
        try {
            return ,($ps.EndInvoke($async))
        } finally {
            $ps.Dispose()
        }
    }
    # Clean up in the background so detection can continue after timeout.
    [System.Threading.ThreadPool]::QueueUserWorkItem({
        param($p)
        try { $p.Stop() } catch {}
        try { $p.Dispose() } catch {}
    }, $ps) | Out-Null
    throw [System.TimeoutException]::new("Operation timed out after $TimeoutMs ms")
}

# Finds product-info.json files with bounded depth, time, and directory count.
# Reparse points are skipped.
function Find-ProductInfoFiles {
    param(
        [string]$Root,
        [int]$MaxDepth = 4,
        [int]$TimeoutMs = 8000,
        [int]$MaxDirs = 20000
    )
    $results = New-Object System.Collections.Generic.List[string]
    if ([string]::IsNullOrEmpty($Root) -or -not (Test-Path -LiteralPath $Root)) {
        return $results
    }

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $dirCount = 0
    $queue = New-Object System.Collections.Generic.Queue[object]
    $queue.Enqueue([pscustomobject]@{ Path = $Root; Depth = 0 })

    while ($queue.Count -gt 0) {
        if ($sw.ElapsedMilliseconds -gt $TimeoutMs) { break }
        if ($dirCount -ge $MaxDirs) { break }

        $node = $queue.Dequeue()
        $dirCount++

        $pi = Join-Path $node.Path 'product-info.json'
        if (Test-Path -LiteralPath $pi -PathType Leaf) {
            $results.Add($pi)
        }

        if ($node.Depth -ge $MaxDepth) { continue }

        $children = $null
        try {
            $children = Get-ChildItem -LiteralPath $node.Path -Directory -Force -ErrorAction SilentlyContinue
        } catch {
            $children = $null
        }
        if (-not $children) { continue }

        foreach ($child in $children) {
            if ($child.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { continue }
            $queue.Enqueue([pscustomobject]@{ Path = $child.FullName; Depth = $node.Depth + 1 })
        }
    }
    return $results
}

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
    foreach ($f in (Find-ProductInfoFiles -Root $root -MaxDepth 6 -TimeoutMs 10000)) {
        $null = $piPaths.Add($f)
    }
}

# Fallback: inspect top-level directories on fixed drives.
# Recursion is limited to IDE-looking directories.
$skipTop = @('Windows', 'Windows.old', '$Recycle.Bin', 'System Volume Information',
             'PerfLogs', 'Recovery', 'Boot', 'EFI', 'MSOCache', 'OneDriveTemp')
$ideNamePattern = '(?i)^(idea|intellij|giga|jetbrains|amplicode|toolbox)'

# Inspect standard Program Files locations for IDEs installed under vendor folders.
# Other drives are handled by the top-level fallback below.
$programFilesRoots = @('C:\Program Files', 'C:\Program Files (x86)')
foreach ($pfRoot in $programFilesRoots) {
    if (-not (Test-Path -LiteralPath $pfRoot)) { continue }
    $children = $null
    try {
        $children = Get-ChildItem -LiteralPath $pfRoot -Directory -Force -ErrorAction SilentlyContinue
    } catch { $children = $null }
    if (-not $children) { continue }
    foreach ($td in $children) {
        if ($td.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { continue }

        $directPi = Join-Path $td.FullName 'product-info.json'
        if (Test-Path -LiteralPath $directPi -PathType Leaf) {
            $null = $piPaths.Add($directPi)
        }
        if ($td.Name -match $ideNamePattern) {
            foreach ($f in (Find-ProductInfoFiles -Root $td.FullName -MaxDepth 4 -TimeoutMs 8000)) {
                $null = $piPaths.Add($f)
            }
        }
    }
}

# Enumerate fixed drives with a bounded system query and a filesystem fallback.
try {
    $fixedDrives = Invoke-WithTimeout -TimeoutMs 5000 -ScriptBlock {
        Get-CimInstance -ClassName Win32_LogicalDisk -Filter 'DriveType=3' -OperationTimeoutSec 4 -ErrorAction Stop |
            ForEach-Object { "$($_.DeviceID)\" }
    }
} catch {
    $fixedDrives = $null
}
if (-not $fixedDrives) {
    $fixedDrives = Get-PSDrive -PSProvider FileSystem -ErrorAction SilentlyContinue |
        Where-Object { $_.Root -match '^[A-Z]:\\$' } |
        ForEach-Object { $_.Root }
}

# Global budget for the whole fallback scan.
$fallbackBudget = [System.Diagnostics.Stopwatch]::StartNew()
foreach ($drive in $fixedDrives) {
    if ($fallbackBudget.ElapsedMilliseconds -gt 30000) { break }
    if (-not (Test-Path -LiteralPath $drive)) { continue }

    $topDirs = $null
    try {
        $topDirs = Get-ChildItem -LiteralPath $drive -Directory -Force -ErrorAction SilentlyContinue
    } catch {
        $topDirs = $null
    }
    if (-not $topDirs) { continue }

    foreach ($td in $topDirs) {
        if ($skipTop -contains $td.Name) { continue }
        if ($td.Attributes -band [System.IO.FileAttributes]::ReparsePoint) { continue }

        # Fast path: product-info.json directly inside this top-level dir.
        $directPi = Join-Path $td.FullName 'product-info.json'
        if (Test-Path -LiteralPath $directPi -PathType Leaf) {
            $null = $piPaths.Add($directPi)
        }

        # Bounded recursion only for IDE-looking directories.
        if ($td.Name -match $ideNamePattern) {
            foreach ($f in (Find-ProductInfoFiles -Root $td.FullName -MaxDepth 4 -TimeoutMs 8000)) {
                $null = $piPaths.Add($f)
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

# Vendor namespace is used in per-user IntelliJ Platform directories.
# Older builds may omit it and use the JetBrains namespace.
function Get-PluginsDir {
    param([string]$dataDirName, [string]$vendor = 'JetBrains')
    if (-not $env:APPDATA) { return $null }
    return Join-Path $env:APPDATA "$vendor\$dataDirName\plugins"
}

function Get-SystemDir {
    param([string]$dataDirName, [string]$vendor = 'JetBrains')
    if (-not $env:LOCALAPPDATA) { return $null }
    return Join-Path $env:LOCALAPPDATA "$vendor\$dataDirName"
}

# Returns true when this script is running under the target IDE process.
# Uses one bounded process snapshot, then walks parent PIDs in memory.
function Test-DescendantOf {
    param([int]$targetPid)
    if ($targetPid -le 0) { return $false }

    $parentOf = @{}
    try {
        $all = Invoke-WithTimeout -TimeoutMs 5000 -ScriptBlock {
            Get-CimInstance -ClassName Win32_Process -OperationTimeoutSec 4 -ErrorAction Stop |
                Select-Object ProcessId, ParentProcessId
        }
    } catch {
        # If the process snapshot is unavailable, keep detection moving.
        return $false
    }
    if (-not $all) { return $false }
    foreach ($p in $all) {
        $parentOf[[int]$p.ProcessId] = [int]$p.ParentProcessId
    }

    $cur = $PID
    $depth = 0
    while ($cur -and $cur -ne 0 -and $depth -lt 50) {
        if ($cur -eq $targetPid) { return $true }
        if (-not $parentOf.ContainsKey($cur)) { return $false }
        $cur = $parentOf[$cur]
        $depth++
    }
    return $false
}

# Returns the IDE PID from its .pid file when the process is still running.
function Get-IdeRunningPid {
    param([string]$dataDirName, [string]$vendor = 'JetBrains')
    $systemDir = Get-SystemDir $dataDirName $vendor
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
    $productVendor = $info.productVendor
    if (-not $productVendor) { $productVendor = 'JetBrains' }

    if (-not $productCode -or -not $dataDirName) { continue }
    if (-not (Test-Target $productCode $productName)) { continue }

    $exePath = Find-Launcher $pi
    if (-not $exePath) { continue }

    $pluginsDir = Get-PluginsDir $dataDirName $productVendor
    $amplicodeInstalled = if ($pluginsDir) { Test-AmplicodeInstalled $pluginsDir } else { $false }
    $runningPid = Get-IdeRunningPid $dataDirName $productVendor
    $running = [bool]$runningPid
    # Check the process tree only when the IDE process is running.
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
