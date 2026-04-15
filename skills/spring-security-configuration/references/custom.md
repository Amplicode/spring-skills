# Custom Variant

Authentication type: `CUSTOM`

## Description

A minimal security configuration with only the common DSL blocks (headers, CSRF, anonymous, authorize requests).
No specific authentication mechanism is configured — the user is expected to add their own authentication logic manually.

## Fragments Used

1. Common DSL (see `references/common-dsl.md`) — all blocks

## Dependencies

- `_dependencies/base.md` (always)

## Properties

None.

## Questions

No variant-specific questions. Only common DSL questions apply (headers, CSRF, anonymous, authorize requests).

## Generation

**Custom DSL order: authorizeHttpRequests → headers → anonymous → csrf**

1. Read skeleton from `_skeletons/{lang}.md`
2. Insert `http.authorizeHttpRequests(...)` fragment (FIRST in filterChain)
3. Insert `http.headers(Customizer.withDefaults())`
4. Insert `http.anonymous(Customizer.withDefaults())`
5. Insert `http.csrf(Customizer.withDefaults())`
6. Add dependencies from `_dependencies/base.md`
7. No properties file to write

## Notes

- This is the simplest configuration — a bare `SecurityFilterChain` with only authorization rules and common protections.
- Useful when the user wants to set up authentication manually or integrate a custom authentication mechanism.
- The generated class will have `@Configuration @EnableWebSecurity` and a single `filterChain` method.
