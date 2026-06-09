# LDAP application properties

## When to write
LDAP_STATEFUL authentication type. Properties are extracted via `@Value` field injection into the configuration class.

## Properties

| Key | Value | Condition |
|-----|-------|-----------|
| `{appPrefix}.ldap.url` | `ldap(s)://{host}:{port}` | always |
| `{appPrefix}.ldap.managerDn` | `{managerDn}` | if anonymousAccess=false |
| `{appPrefix}.ldap.managerPassword` | `${LDAP_MANAGER_PASSWORD}` | if anonymousAccess=false |
| `{appPrefix}.ldap.userDnPatterns` | `{userDnPatterns}` (comma-separated) | if userDnPatterns not empty |
| `{appPrefix}.ldap.groupSearchBase` | `{groupSearchBase}` | if authorities=GROUP or GROUP_WITH_HIERARCHY |

### Manager password handling (security)

**Never substitute a literal manager password into the file.** Always emit the value as `${LDAP_MANAGER_PASSWORD}`. After generation, report to the user the exact env var name that must be set before running the app (shell `export`, IDE run config, or deployment secret store). Do NOT ask the user for the password value — it must not enter the conversation.

Each property is injected as a `@Value`-annotated field. Replace `{appPrefix}` with the actual value from Step 1.

Example — if `appPrefix` = `spring-petclinic`:
```java
@Value("${spring-petclinic.ldap.url}") private String ldapUrl;
@Value("${spring-petclinic.ldap.managerDn}") private String ldapManagerDn;
```

Example — if `appPrefix` = `app` (default):
```java
@Value("${app.ldap.url}") private String ldapUrl;
@Value("${app.ldap.managerDn}") private String ldapManagerDn;
```

## Notes

- `{appPrefix}` is the application prefix (default: `app`), determined from existing `@Value` annotations or project conventions
- In the generated `@Value` annotation the property placeholder is `${<actual-prefix>.ldap.<key>}` — always substitute the real prefix, never leave `{appPrefix}` literally in the code
- URL is constructed from host, port, and SSL flag: `ldaps://{host}:{port}` (if SSL) or `ldap://{host}:{port}`
