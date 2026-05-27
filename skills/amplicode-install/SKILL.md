---
name: amplicode-install
description: Installs the Amplicode IntelliJ plugin into IntelliJ IDEA (Ultimate/Community) and GigaIDE on the user's machine. Trigger explicitly when the user asks to install the Amplicode plugin into their IDE, in any language. Also trigger implicitly when another Spring Agent Toolkit skill (`spring-explore`, `spring-data-jpa`, `spring-data-jdbc`, `crud-rest-controller`, `dto-creator`, `mapper-creator`, `spring-security-configuration`, `spring-planning`, `java-debug`) detects that the Amplicode MCP server is not connected and delegates installation to this skill. After the IDE-plugin is installed, the user must open any project and click the "Настроить Spring Agent" button on the welcome screen — that is what triggers MCP auto-configuration and spring-skills install. This skill does NOT configure MCP itself. Skip GoLand, PyCharm, WebStorm, Rider, CLion, RubyMine, PhpStorm, DataGrip and other JetBrains products — only IDEA and GigaIDE are supported targets.
---

# amplicode-install

Installs the Amplicode IntelliJ plugin into all locally installed IntelliJ IDEA (Ultimate/Community) and GigaIDE installations the user picks.

## What this skill does

1. Runs a platform-specific detection script that finds installed IntelliJ-based IDEs on the machine.
2. Filters down to **IDEA Ultimate (`IU`)**, **IDEA Community (`IC`)**, and **GigaIDE**. Skips everything else.
3. Filters out IDEs where Amplicode is already installed.
4. Asks the user which IDE(s) to install into when more than one candidate remains.
5. Invokes the standard JetBrains CLI: `<ide-binary> installPlugins com.haulmont.amplicode https://amplicode.ru/marketplace`. The IDE itself picks the right version for its build, downloads the ZIP, and unpacks it into the right plugins directory.
6. Tells the user to launch the IDE, open any project, and **click the "Настроить Spring Agent" button** on the Amplicode welcome screen. That button (and only that button) triggers MCP auto-config + spring-skills install.

## What this skill does NOT do

- It does **not** write any MCP client config. The Amplicode plugin needs a running MCP server inside the IDE to know the port — only the plugin itself can fill in the configs of supported agents. That happens when the user clicks "Настроить Spring Agent" on the welcome screen (not automatically on IDE start).
- It does **not** install spring-skills or any agent-side skill packages. That is also done by the welcome-screen button.
- It does **not** download or extract the plugin ZIP by hand. JetBrains' `installPlugins` CLI handles fetching, version matching against the IDE build, and unpacking.

## Step-by-step

### 1. Run the detection script

The scripts live next to this `SKILL.md` under `scripts/`. Run the right one for the user's OS and parse its stdout as a JSON array; the script already filters down to compatible IDEs (IDEA Ultimate / Community / GigaIDE), so the agent only needs these fields per element: `amplicodeInstalled`, `name`, `running`, `pid`, `hostsCurrentProcess`, `dataDirectoryName`, `exePath`, `appBundle`.

- **macOS / Linux:** `bash scripts/detect-ides.sh`
- **Windows:** `pwsh scripts/detect-ides.ps1` (fallback `powershell` if `pwsh` is not available)

If the JSON array is empty — tell the user no compatible IDE was found and stop.

### 2. Filter

- Drop entries with `amplicodeInstalled: true` — never reinstall, never ask.
- If nothing left — tell the user Amplicode is already installed in every compatible IDE and stop.

### 3. Pick targets

- **Exactly one candidate left:** install into it without asking. Tell the user which IDE you are using.
- **Two or more:** ask the user a multi-select question (using whichever interactive-prompt tool the client provides). Each option is one IDE (label = `name`). Do not add "all of them" as a separate option — multi-select already covers it. Recommend selecting all, but let the user decide.

### 3a. Handle running IDEs (auto-restart flow)

For each picked IDE, check the `running` flag from the detection JSON. **Trust it — do not roll your own `pgrep` check; pgrep truncates long JVM command lines and gives false negatives.** If `running: true`, the JetBrains CLI will fail with "Only one instance of IDEA can be run at a time."

**Self-host check (do this first).** For each picked IDE with `running: true`, look at `hostsCurrentProcess`. If `true`, you are running inside that IDE's terminal — sending SIGTERM to the IDE will kill you before `installPlugins` ever runs, leaving the user with a closed IDE and no plugin. **Never offer auto-restart for such IDEs.** Skip the question below for that IDE and go straight to the manual instructions described after "If the user picks the second option" — substitute the IDE's own `exePath`. Tell the user up front (in their language) why: "I'm running inside this IDE — if I close it, I die with it and the install step never runs. So you'll need to do this part yourself."

When at least one picked IDE has `running: true` **and `hostsCurrentProcess: false`**, offer the user an auto-restart flow for those IDEs. **Phrase the user-facing question in the user's language** (mirror whatever language the user has been chatting in); the wording below is the English baseline:

> "<IDE name> is currently running. I can gracefully shut it down, install the plugin, and relaunch it. If the IDE has unsaved changes it will show a save dialog and refuse to exit — in that case I will stop and you handle it manually. OK?"

Ask a single-select question with two options:
- "Yes — close it, install, relaunch" (Recommended)
- "I will do it myself"

If the user picks the second option, **do not stop silently** and do not just ask them to "re-run this skill" — they may be running you from inside that very IDE, in which case re-running the skill after closing the IDE is impossible. Instead, give them the exact command and the post-install steps they need to perform themselves. Tell them (in their language):

> Close the IDE yourself, then run this command in a terminal:
>
> ```
> "<exePath>" installPlugins com.haulmont.amplicode https://amplicode.ru/marketplace
> ```
>
> Substitute `<exePath>` with the IDE launcher path from the detection JSON (quote it — there are usually spaces). Wait for it to finish; exit code 0 means the plugin is installed.
>
> Then open the IDE again. On any open project:
> 1. If the Amplicode welcome screen appears — click **Настроить Spring Agent**.
> 2. Otherwise open it manually via **Find Action** (`Cmd+Shift+A` on macOS, `Ctrl+Shift+A` on Windows/Linux) → type **Spring Agent Toolkit** → Enter, then click **Настроить Spring Agent**.
> 3. After that, restart the MCP client you are using (so it picks up the new MCP server config).

Then stop the skill.

If the user agrees to auto-restart, for each running picked IDE do **graceful quit → wait → install → relaunch**:

**Quit (SIGTERM, never SIGKILL):**
- macOS / Linux: `kill -TERM <pid>`
- Windows: `taskkill /PID <pid>` (no `/F`)

**Wait for shutdown** by polling: re-run the detect script and look for the same `dataDirectoryName`. The IDE is fully down when `running: false` (the `.pid` file is removed). Poll every **5 seconds** for up to 30 seconds — do NOT poll every second, that's just noise.

If still running after 30 seconds — stop. Tell the user (in their language): "The IDE didn't shut down within 30 seconds — there's probably an unsaved-changes dialog open. Finish saving/exiting it manually, then re-run this skill." Do not retry, do not force-kill.

**Install** as described in step 4.

**Relaunch:**
- macOS: `open -a "<appBundle>"` (this is what `appBundle` is for; falls back to `open -a "<exePath>"` if missing).
- Linux: `nohup "<exePath>" >/dev/null 2>&1 &` (detach from the agent process group).
- Windows: `Start-Process -FilePath "<exePath>"` from PowerShell, or `start "" "<exePath>"` from cmd.

### 4. Install

For each picked IDE, run:

```
"<exePath>" installPlugins com.haulmont.amplicode https://amplicode.ru/marketplace
```

Notes:
- Run it as a normal `Bash` call. Always quote `exePath` — it commonly contains spaces.
- Do NOT run this while that IDE is open by the user — JetBrains' CLI may behave unpredictably with a running instance. If you suspect the IDE is running, ask the user to close it first.
- If multiple targets — run them sequentially, not in parallel. Each invocation needs to write to that IDE's plugins directory.
- Treat a non-zero exit code as failure for that IDE. Show the user the stderr and continue with the remaining IDEs.

### 5. Wrap up

Tell the user the following (translate it into the user's language — mirror whatever language they have been chatting in):

> Amplicode plugin installed into: <list>.
>
> Next:
> 1. Open any project in the IDE — the Amplicode welcome screen should appear automatically.
>    If it does not, open it manually: **Find Action** (`Cmd+Shift+A` on macOS, `Ctrl+Shift+A` on Windows/Linux) → type **Spring Agent Toolkit** → Enter.
> 2. **Click the "Настроить Spring Agent" button on that screen.** The plugin will then write the Amplicode MCP server into the configs of the supported agents and install spring-skills. Without clicking this button, MCP is not configured. The welcome screen only shows up on an open project, not on the IDE's start window.
> 3. **Restart the MCP client you are currently using.** Most clients read MCP config only at startup, so the newly added `amplicode` server is not visible in the current session. After the restart, check the client's MCP server list.

Keep `Настроить Spring Agent` and `Spring Agent Toolkit` verbatim in the translated message — they are literal labels rendered in the IDE's UI, not strings to localize.

### 6. Detect failure of step 5 (optional, if the client exposes its MCP server list to you)

After the user restarts their MCP client and continues the conversation, if you can introspect the list of configured MCP servers and `amplicode` is **not** present, the user most likely skipped the welcome-screen button. Tell them (in their language):

> Looks like the `amplicode` MCP server isn't connected on your side. That means the welcome-screen button wasn't pressed yet. Open any project in the IDE, then run **Find Action** (`Cmd+Shift+A` / `Ctrl+Shift+A`) → **Spring Agent Toolkit** → click **Настроить Spring Agent** on the screen that opens. Then restart this client again.