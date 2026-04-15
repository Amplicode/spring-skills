# HTTP Session (Form Login) Variant

Authentication type: `HTTP_SESSION_STATEFUL`

## Fragments Used

1. Common DSL (see `references/common-dsl.md`)
2. FormLogin (`_fragments/form-login/{lang}.md`)
3. Logout (`_fragments/logout/{lang}.md`) — only if non-default logout options
4. SessionManagement (`_fragments/session-management/{lang}.md`)
5. Exception Handling (`_fragments/exception-handling/{lang}.md`) — only if Access Denied Handling is enabled
6. Remember Me (`_fragments/remember-me/{lang}.md`) — only if enabled
7. User Storage bean (`_beans/user-storage/{lang}.md`) — always (default: In memory)

## Dependencies

- `_dependencies/base.md` (always)

## Questions

### FormLogin Questions (batch)

```
Form login settings:
1. **Login page URL?** [default Spring Security]
2. **Failure URL?** [default]
3. **Success URL?** [default]
```

If user provides no URLs, generate `http.formLogin(Customizer.withDefaults())`.

### FormLogin Advanced Questions (batch, skip if "all defaults")

```
Advanced form login settings:
1. **Username parameter?** [default]
2. **Password parameter?** [default]
3. **Login processing URL?** [default]
4. **Failure forward URL?** [default]
5. **Success forward URL?** [default]
6. **Authentication details source bean?** [empty]
```

### Handler Type Question (only if URLs are set)

```
Success/failure handler type?
1. HTTP status (default) -- inline lambda
2. Bean -- reference to existing bean
```

If STATUS: ask for HTTP status codes (e.g., OK, UNAUTHORIZED).
If BEAN: ask for bean references.

### Logout Questions (batch, skip if "all defaults")

```
Logout settings:
1. **Disable logout?** [no]
2. **Logout URL?** [/logout]
3. **Post-logout URL?** [/login?logout]
4. **Clear authentication?** [yes]
5. **Invalidate session?** [yes]
6. **Delete cookies?** [empty] (comma-separated)
```

### Logout Advanced Questions (batch, skip if "all defaults")

```
Advanced logout settings:
1. **Logout request matcher bean?** [empty]
2. **Logout handler bean(s)?** [empty]
3. **Logout success handler type?** [URL] (URL / Status / Bean)
4. **Logout success status code?** [empty] (only for Status type)
5. **Logout success handler bean?** [empty] (only for Bean type)
```

If all defaults — skip the logout fragment entirely (Spring Security enables default logout automatically).
If disabled=true — use `logout.disable()` variant.
If any non-default option — use the "with options" variant.

### Access Denied Handling Question

```
Configure Access Denied handling? [no]
```

If yes:
```
Access Denied settings:
1. **Disable Exception Handling?** [no]
2. **Access Denied Page URL?** [empty] (i.e. /errors/401)
3. **AccessDeniedHandler bean?** [empty]
4. **Handler type?** [Status] (Status / Bean) — Status creates HttpStatusAccessDeniedHandler, Bean — bean reference
5. **Authentication entry point?** [empty] (HTTP status code, creates HttpStatusEntryPoint)
```

### Remember Me Question

```
Enable Remember Me? [no]
```

If yes:
```
Remember Me settings:
1. **Key?** [empty]
2. **Token validity (seconds)?** [1209600 = 2 weeks]
3. **Use persistent tokens?** [no]
```

### Remember Me Advanced Questions (batch, skip if "all defaults")

```
Advanced Remember Me settings:
1. **Cookie domain?** [empty]
2. **Remember Me parameter?** [remember-me]
3. **Cookie name?** [remember-me]
4. **Secure cookie?** [default]
5. **Always remember?** [no]
6. **Authentication success handler bean?** [empty]
7. **Remember Me services bean?** [empty] (overrides default implementation)
```

### Session Management Questions (batch, skip if "all defaults")

```
Session settings:
1. **Disable Session Management?** [no]
2. **Session Fixation?** [CHANGE_SESSION_ID] (NONE / CHANGE_SESSION_ID / NEW_SESSION / MIGRATE_SESSION)
3. **Invalid session URL?** [empty]
4. **Session creation policy?** [IF_REQUIRED] (IF_REQUIRED / ALWAYS / NEVER / STATELESS)
5. **Authentication error URL?** [empty]
6. **URL rewriting?** [no]
7. **Maximum sessions?** [empty]
8. **Block login when max exceeded?** [no] (only if max is set)
9. **Expired session URL?** [empty] (only if max is set)
10. **Session registry bean?** [empty] (only if max is set)
```

If all defaults, skip session management fragment entirely.

### Session Management Advanced Questions (batch, skip if "all defaults")

```
Advanced session settings:
1. **Expired session strategy bean?** [empty]
2. **Session authentication strategy bean?** [empty]
3. **Invalid session strategy bean?** [empty]
4. **Session authentication failure handler bean?** [empty]
```

### User Storage Question

```
User storage type?
1. In memory (default) -- InMemoryUserDetailsManager
2. JDBC -- JdbcUserDetailsManager
3. JPA -- UserDetailsService bean reference
4. Custom -- UserDetailsService bean reference
```

If IN_MEMORY:
```
Users (can add multiple):
| Username | Password | Roles |
|----------|----------|-------|
| admin    | admin    | ADMIN |
| user     | user     | USER  |
```

If JDBC: add DataSource constructor parameter to the class.
If JPA/CUSTOM: ask for bean reference.

## Generation

**HTTP Session DSL order: authorizeHttpRequests → headers → sessionManagement → exceptionHandling → rememberMe → formLogin → logout → anonymous → csrf → userDetailsService**

1. Read skeleton from `_skeletons/{lang}.md`
2. Insert `http.authorizeHttpRequests(...)` fragment (FIRST in filterChain)
3. Insert `http.headers(...)` — Customizer.withDefaults() by default
4. Insert `http.sessionManagement(...)` fragment — only if non-default session options are set (skip entirely when all defaults)
5. Insert `http.exceptionHandling(...)` — only if Access Denied is enabled
6. Insert `http.rememberMe(...)` — only if enabled (BEFORE formLogin!)
7. Insert `http.formLogin(...)` fragment — Customizer.withDefaults() by default
8. Insert `http.logout(...)` fragment — only if non-default logout options
9. Insert `http.anonymous(...)` — Customizer.withDefaults() by default
10. Insert `http.csrf(...)` — Customizer.withDefaults() by default
11. Add User Storage method to class body from `_beans/user-storage/{lang}.md`
12. Add `http.userDetailsService(inMemoryUserDetailsService())` (or JDBC/JPA/Custom equivalent) to filterChain
13. Add dependencies from `_dependencies/base.md`

## Notes

- **Logout default behavior**: Spring Security enables logout at `/logout` by default. The `http.logout(...)` DSL is only needed when customizing logout behavior. Do NOT add `http.logout(Customizer.withDefaults())` — it's redundant.
- **User Storage is NOT a @Bean**: The `inMemoryUserDetailsService()` method is a plain method (not annotated with `@Bean`). It is called inline from the filterChain via `http.userDetailsService(inMemoryUserDetailsService())`.
- **No orphan methods**: Every generated method must be called from filterChain or from another bean.
