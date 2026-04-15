---
name: spring-security-configuration
description: >
  Creates a Spring Security configuration class with authentication, authorization, and HTTP protection setup.
  Use this skill when a security configuration needs to be created, either standalone or as part of a larger task
  (e.g. adding authentication to a REST API, configuring OAuth2/OIDC login, setting up JWT resource server).
---

# Spring Security Configuration

Generates a `@Configuration @EnableWebSecurity` class with a `filterChain()` method, DSL chains for authentication and authorization, beans, dependencies, and properties in application.properties.

---

> **CRITICAL: Code ONLY from examples/ files. If no matching example -- STOP and ask user.**
> **CRITICAL: For questions with a fixed set of choices, prefer `AskUserQuestion` > its analogue > plain text list. Plain numbered text lists are the last resort when no interactive tool is available.**
> **CRITICAL: Read the conversation context BEFORE running Step 1.** Half the questions in Steps 2–3 may already be answered by the user's prompt and prior turns. Re-asking what was already said is the #1 reason this skill feels slow.

---

## Defaults

| Option | Default | Always ask? | Notes |
|--------|---------|-------------|-------|
| Authentication type | — | YES | main branching |
| Disable CSRF | no | NO | |
| Disable headers | no | NO | |
| Disable anonymous access | no | NO | |
| Authorization rules | anyRequest().authenticated() | NO | |
| language | from `get_project_summary` | NO | auto-detected |
| bootVersion | from `get_project_summary` | NO | auto-detected |
| className | SecurityConfiguration | NO | suggest, confirm only |
| packageName | same as existing @Configuration | NO | auto-detected |
| appPrefix | from existing `@Value` properties or `app` | NO | auto-detected from project |

**Smart defaults:** If user says "use defaults", "all defaults", "default settings",
or similar -- skip ALL questions where "Always ask?" = NO. Only ask mandatory questions.

**Smart answer recognition:** When user provides a value instead of choosing from a numbered
list, accept it directly. Examples:
- Question "Authentication type?" -> user answers "jwt" -> this IS the choice, don't show options
- Question "Client ID?" -> user answers "my-app" -> this IS the value, don't re-ask
- If user provides multiple answers in one message -> accept all, skip answered questions
- NEVER ask a question that the user already answered (even implicitly)

**Batch questions:** Group closely related questions into a single
`AskUserQuestion` call (up to 4 questions per call) when they:
- Belong to the same logical section (e.g. both are connection settings)
- Don't depend on each other's answers
- Have obvious defaults that the user can skip

Rules:
- Maximum **3-4 questions** per `AskUserQuestion` call
- Mark the recommended option with `(Recommended)` and place it first
- Never batch questions from DIFFERENT decision branches
- The primary branching question (authentication type) is always asked ALONE
- Prefer `AskUserQuestion` for choices; fall back to plain text lists only if the tool is unavailable

---

## Decision-making principle — context first, then ask

Before asking the user **any** question, attempt to derive the answer from
the context already gathered: project summary, module dependencies, existing
security configurations, prior turns of this conversation, and the user's
original prompt. Only ask when the context yields **no clear default** or
when the choice is genuinely user-specific (e.g. authentication type,
authorization rules).

Hierarchy of decisions:

1. **Context is unambiguous → decide silently, do NOT ask.**
   Examples: language and Boot version from `get_project_summary`; existing
   dependencies from `list_module_dependencies`; packageName from existing
   `@Configuration` classes; className = `SecurityConfiguration`; appPrefix
   from existing `@Value` annotations.

2. **Context gives a strong signal → state the decision + alternatives in one line, let the user override or stay silent.**
   Format:
   ```
   Will create SecurityConfiguration with JWT (spring-boot-starter-oauth2-resource-server found in dependencies).
   Alternatives: Form Login, OIDC, LDAP, Custom. OK?
   ```
   The user can answer "ok" / "yes" / silence → accept; or name an
   alternative → switch.

3. **Context yields no clear default → ask with `AskUserQuestion` (preferred), with the recommended option first.**
   Mark the recommended option with `(Recommended)` in its label and place
   it first. If no interactive choice tool is available, fall back to a
   plain text list.

4. **Context is fully empty for a critical input → ask plainly.**
   This applies to: authentication type (when no deps hint at it),
   authorization rules, variant-specific settings.

### How to ask — prefer `AskUserQuestion`

When a question must be asked, prefer the **`AskUserQuestion`** tool over
writing a numbered list in the response body. Fall back to plain text only
if no interactive choice tool is available.

Rules for `AskUserQuestion` calls in this skill:

- Each call may contain up to **4 questions** that are independent of each
  other. Use this to batch related decisions in one round-trip.
- Each question has **2–4 options**. The tool auto-adds an "Other" choice
  for free-form input — never include it manually.
- Mark the recommended option by putting it **first** with `(Recommended)`
  appended to the label.
- `header` is a 12-char chip label (e.g. "Auth Type", "CSRF", "Headers").

When `AskUserQuestion` is **not** the right tool:
- Free-form input with no enumerable set of options (e.g. URL, client ID,
  secret, patterns) — ask in plain text.
- The "single confirmation line" from principle 2 — that is a plain
  yes/no, not an enumerated choice.

The question lists in Steps 2–3 and in reference files are a **fallback**
for case 4. They are NOT a script to execute top-to-bottom. If a question's
answer is already determined by principles 1–3, **skip the question**.

---

## Step 0 -- Conversation context first (REQUIRED, no tool calls)

Before any MCP call, before any question, **re-read the user's prompt and
the prior turns of this conversation** and extract whatever is already
stated. This step costs nothing and prevents the most common failure mode
of this skill — asking the user something they already said.

Build a mental checklist of inputs and tick off everything the user has
already provided, explicitly or implicitly:

| Input | Look for in the prompt / context |
|---|---|
| **authentication type** | "JWT", "form login", "OIDC", "Keycloak", "LDAP", "OAuth2", "authorization server" — any direct or implied mention |
| **provider** | "Keycloak", "Google", "GitHub", "Okta", "AWS Cognito" — implies OIDC or JWT variant |
| **authorization rules** | "only for ADMIN", "public API", "all endpoints secured" |
| **language** | Kotlin / Java — also implied by file extensions in discussion |
| **className** | "name it SecurityConfig", "class name XxxConfiguration" |
| **smart defaults** | "use defaults", "all defaults", "default settings", "minimal configuration" |
| **prior project facts** | language, Boot version, dependencies — already known if discussed earlier in this conversation; do not re-fetch |

For every input that is **explicitly or strongly implicitly answered**:
mark it as decided and skip the corresponding question in Steps 2–3. Do
NOT ask "Authentication type?" if the user wrote "configure JWT" — JWT is
the answer. Do NOT ask language if the user wrote "in Kotlin".

For every input that is **not** answered: defer to the Decision-making
principle above — try to derive it from project context first (Step 1),
and only then ask.

Step 0 is mental, not a tool call. Do not announce it to the user. Do not
write "Step 0 done". Just internalize what the user already said before
proceeding to Step 1.

---

## Step 1 -- Gather context (automatic, no questions)

Call **Amplicode MCP tools** (in parallel where possible).

| Tool | What to extract | Variable name |
|------|----------------|---------------|
| `get_project_summary` | language, springBootVersion, moduleName, buildFile, mainPackage | language, bootMajor, moduleName, buildFile, mainPackage |
| `list_module_dependencies(moduleName)` | artifact IDs | presentDeps |
| `list_application_properties_files(moduleName)` | path to properties file | propsFile |
| `list_security_configurations` | existing security configs | existingConfigs |
| `list_spring_security_roles` | existing roles for authorize rules | existingRoles |

If multi-module project (multiple modules in `get_project_summary`):
Ask which module to use. Then re-call module-specific MCP tools with that module.

If `existingConfigs` is not empty:
- Warn user: "Project already has security configuration(s): {list}. Create an additional one?"
- If user confirms — suggest a `{className}` that does not collide with existing names
- If user declines — STOP

Determine `appPrefix`: scan existing `@Value` annotations in the project for a common custom prefix
(e.g. `@Value("${myapp.something}")` → `appPrefix = myapp`). If none found, default to `app`.

---

## Step 2 -- Authentication type

Ask:
```
Authentication type?
1. Form Login (HTTP Session) -- form login with session
2. JWT (OAuth2 Resource Server) -- stateless JWT
3. OAuth2/OIDC Login -- login via external provider (Keycloak, Google, GitHub...)
4. Authorization Server -- own authorization server (Spring Boot >= 3.1)
5. LDAP -- LDAP authentication
6. Custom -- empty filterChain, only common DSL blocks
```

Map answer to reference file:
- 1 -> `references/http-session.md`
- 2 -> `references/jwt.md`
- 3 -> `references/oidc.md`
- 4 -> `references/authorization-server.md` **(requires bootMajor >= 3.1 — if lower, warn user and ask to choose another type)**
- 5 -> `references/ldap.md`
- 6 -> `references/custom.md`

---

## Step 3 -- Variant-specific questions (inline)

Read the mapped reference file and follow its questions flow.
Only asked if user did NOT say "all defaults".

Each reference file specifies:
- Which fragments to include
- Which beans to generate
- Which dependencies to add
- Which properties to write
- Variant-specific questions

---

## Step 4 -- Generate code

1. Read skeleton from `examples/_skeletons/{lang}.md` where `{lang}` = `java` or `kotlin` (from Step 1)

2. Create the configuration class file using the skeleton, substituting `{packageName}` and `{className}`

3. Read the reference file for the chosen variant. For each fragment listed:
   - Read the fragment file from `examples/_fragments/{feature}/{lang}.md`
   - Insert the appropriate code variant into the filterChain method body, before `return http.build();`
   - **Select the correct version variant** based on `bootMajor` from Step 1 (some fragments have Boot < 3.1 / Boot >= 3.1 sections — use the matching one)
   - Apply variable substitutions (ONLY variables declared in the Variables section)
   - Include ONLY the lines that match the user's chosen options (skip commented-out conditional lines)

4. **DSL ordering inside filterChain method** (IMPORTANT — order is VARIANT-DEPENDENT):

   See `references/common-dsl.md` → "Generation Order" for the exact ordering per variant.
   Always end with `return http.build();`

5. For each bean listed in the reference:
   - Read the bean file from `examples/_beans/{bean-type}/{lang}.md`
   - Add the bean method to the configuration class body
   - **CRITICAL**: Every bean method MUST be referenced from the filterChain DSL.
     Do NOT create methods that are unused.
   - If the bean requires autowired fields (e.g. `clientRegistrationRepository`), add them to the class

6. Variable substitution rules:
   - `{packageName}` -> from Step 1 context
   - `{className}` -> from user or default
   - Other variables -> from user answers or defaults
   - **NEVER substitute anything not listed in Variables**
   - **NEVER add imports, methods, or code not in the example**
   - **FQN only in imports, clean code in body.** Examples may contain fully qualified names
     inline (e.g. `new org.springframework.security.web.SecurityFilterChain(...)`).
     When generating code, extract FQNs into import statements and use short class names
     in the code body. The generated file must look like normal hand-written code.

---

## Step 5 -- Dependencies & properties (automatic)

1. Read `_dependencies/base.md` (always)
2. Read variant-specific dependency file (from the reference file)
3. **Select the correct version variant** based on `bootMajor` from Step 1:
   - Dependency files contain Boot 3.x / Boot 4.x sections — use the one matching `bootMajor`
   - Boot 4.x changed OAuth2 artifact names: `spring-boot-starter-oauth2-*` → `spring-boot-starter-security-oauth2-*`
   - Authorization Server on Boot 3.0: requires manual `spring-security-oauth2-authorization-server:1.1.0` (no starter)
4. For each artifact NOT in `presentDeps`:
   - Use `buildFile` from Step 1
   - Edit the build file to add the dependency
5. Call `refresh_build_system_model`
6. Read the variant-specific properties file from `_properties/{variant}/properties.md`
7. Write/append to the application properties file
8. Report: "Added dependencies: [list]. Wrote to application.properties: [keys]"

---

## Anti-hallucination checklist

Before writing ANY code, verify:
- [ ] The code comes from an examples/ file (cite which one)
- [ ] Only declared variables were substituted
- [ ] No framework API calls were added "from knowledge"
- [ ] Imports are derived from FQNs used in the example (no extra, no missing)
- [ ] Method signatures match the example exactly
- [ ] No comments or convenience methods were added
- [ ] FQNs from examples are extracted into imports; code body uses short class names only
- [ ] DSL ordering inside filterChain follows the VARIANT-SPECIFIC order defined in Step 4 (HTTP Session/JWT: authorizeHttpRequests first; OIDC: oauth2Login first, then logout, then authorizeHttpRequests)
- [ ] Helper methods (jwtAuthenticationConverter, oidcClientInitiatedLogoutSuccessHandler) are NOT @Bean — they are public methods called directly from filterChain. Note: userAuthoritiesMapper is @Bean for generic providers, but NOT @Bean for Keycloak (see role-mapper bean file)
- [ ] Every bean method generated is actually referenced from the filterChain DSL or from another bean — no orphan methods
- [ ] For HTTP Session: if User Storage is configured, `http.userDetailsService(...)` call is present in filterChain
- [ ] For HTTP Session: logout DSL is only generated when non-default logout options are set (Spring Security enables default logout automatically)
- [ ] Version-specific code/dependencies match `bootMajor`: Boot 4.x uses `spring-boot-starter-security-oauth2-*` (not `spring-boot-starter-oauth2-*`); session-management concurrent sessions API changed in Boot 3.1
