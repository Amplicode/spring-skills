# Common DSL Fragments

These fragments are included in ALL authentication types.

## Generation Order (inside filterChain) — VARIANT-DEPENDENT

**HTTP Session (full DSL):**
1. Authorize requests (`_fragments/authorize-requests/{lang}.md`) -- **FIRST**
2. Headers (`_fragments/headers/{lang}.md`) -- if enabled (Customizer.withDefaults() by default)
3. Session Management (`_fragments/session-management/{lang}.md`) -- only if non-default session options are set (skip entirely when all defaults)
4. Exception Handling (`_fragments/exception-handling/{lang}.md`) -- if enabled
5. Remember Me (`_fragments/remember-me/{lang}.md`) -- if enabled (BEFORE formLogin!)
6. Form Login (`_fragments/form-login/{lang}.md`) -- variant-specific
7. Logout (`_fragments/logout/{lang}.md`) -- if non-default logout options
8. Anonymous (`_fragments/anonymous/{lang}.md`) -- if enabled
9. CSRF (`_fragments/csrf/{lang}.md`) -- if enabled
10. User Details Service -- if user storage is configured

**Custom / Authorization Server / LDAP (bare minimum):**
1. Authorize requests -- **FIRST**
2. Headers (`Customizer.withDefaults()`)
3. Anonymous (`Customizer.withDefaults()`)
4. CSRF (`Customizer.withDefaults()`)
5. **NO** sessionManagement, formLogin, or logout — even for LDAP despite its "Http Session" label!

**JWT (stateless):**
1. Authorize requests -- **FIRST**
2. Headers
3. oauth2ResourceServer (variant-specific)
4. Anonymous
5. CSRF (NOT auto-disabled! kept as Customizer.withDefaults())
6. No sessionManagement generated

**OAuth2/OIDC (stateful):**
1. oauth2Login (variant-specific) -- **FIRST** (with userInfoEndpoint mapper)
2. Logout with OIDC handler -- **SECOND**
3. Authorize requests -- **THIRD**
4. Headers
5. Anonymous
6. CSRF

## Headers Questions (batch, skip if "all defaults")

```
Security headers settings:
1. **Disable headers entirely?** [no]
2. **Frame Options?** [DENY] (Disabled / DENY / SAME_ORIGIN)
3. **Referrer Policy?** [NO_REFERRER] (NO_REFERRER / NO_REFERRER_WHEN_DOWNGRADE / SAME_ORIGIN / ORIGIN / STRICT_ORIGIN / ORIGIN_WHEN_CROSS_ORIGIN / STRICT_ORIGIN_WHEN_CROSS_ORIGIN / UNSAFE_URL)
4. **Permissions Policy?** [empty]
```

### Advanced Headers Questions (batch, skip if "all defaults")

```
Advanced headers settings:
1. **Disable defaults and select manually?** [no]
2. **Content Type Options?** [yes] (only if defaults are disabled)
3. **XSS Protection?** [yes] (only if defaults are disabled)
4. **Cache Control?** [yes] (only if defaults are disabled)
5. **HSTS?** [yes] (only if defaults are disabled)
6. **Header Writers?** [empty] (list of HeaderWriter bean references, comma-separated)
```

If all answers are defaults, skip the headers fragment entirely.
If disabled=true, use the `headers.disable()` variant.
If disableDefaults=true, use the `headers.defaultsDisabled()` variant and individually enable selected sub-options.

## CSRF Question

```
Disable CSRF? [no]
```

If yes: use `csrf.disable()` variant.
If user provides ignore patterns: use the `ignoringRequestMatchers` variant.

### Advanced CSRF Questions (skip if "all defaults")

```
Advanced CSRF settings:
1. **Ignore patterns (Ant matches)?** [empty] (comma-separated)
2. **CSRF Token Repository bean?** [empty]
3. **Require CSRF Protection Matcher bean?** [empty]
4. **Session Authentication Strategy bean?** [empty]
```

## Anonymous Question

```
Disable anonymous access? [no]
```

If yes: use `anonymous.disable()` variant.

### Advanced Anonymous Questions (skip if "all defaults")

```
Advanced anonymous access settings:
1. **Key?** [empty]
2. **Authorities?** [empty] (comma-separated)
3. **Authentication provider bean?** [empty]
4. **Authentication filter bean?** [empty]
```

## Authorize Requests Questions

```
Request authorization rules:
```

Ask user for URL patterns and their permissions. Default: `anyRequest().authenticated()`.

Use `list_spring_security_roles` to suggest available roles.

If user specifies a security matcher, add `http.securityMatcher(...)` before the authorize block.

### Matcher Options

For each rule, the matcher type can be:
- **All paths** (default) — uses `anyRequest()`
- **Request matcher** — uses `.requestMatchers("{pattern}")` with optional HTTP method
- **Dispatcher type** — uses `.dispatcherTypeMatchers(DispatcherType.{TYPE})` (FORWARD, INCLUDE, REQUEST, ASYNC, ERROR)

### Permission Options

- `permitAll()` — allow all
- `authenticated()` — require authentication
- `hasRole("{role}")` — single role
- `hasAnyRole("{role1}", "{role2}")` — multiple roles
- `hasAuthority("{authority}")` — single authority
- `hasAnyAuthority("{a1}", "{a2}")` — multiple authorities
- `denyAll()` — deny all
- `anonymous()` — anonymous only
- `rememberMe()` — remember-me only
- `fullyAuthenticated()` — fully authenticated only
