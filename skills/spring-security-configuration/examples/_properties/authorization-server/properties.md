# Authorization Server Properties

## application.properties

```properties
# Server settings:
spring.security.oauth2.authorizationserver.issuer={issuer}
spring.security.oauth2.authorizationserver.endpoint.authorization-uri={authorizationUri}
spring.security.oauth2.authorizationserver.endpoint.logout-uri={logoutUri}
spring.security.oauth2.authorizationserver.endpoint.client-registration-uri={clientRegistrationUri}
spring.security.oauth2.authorizationserver.endpoint.user-info-uri={userInfoUri}
spring.security.oauth2.authorizationserver.endpoint.device-authorization-uri={deviceAuthorizationUri}
spring.security.oauth2.authorizationserver.endpoint.device-verification-uri={deviceVerificationUri}
spring.security.oauth2.authorizationserver.endpoint.token-uri={tokenUri}
spring.security.oauth2.authorizationserver.endpoint.jwk-set-uri={jwkSetUri}
spring.security.oauth2.authorizationserver.endpoint.token-revocation-uri={tokenRevocationUri}
spring.security.oauth2.authorizationserver.endpoint.token-introspection-uri={tokenIntrospectionUri}

# Per client:
spring.security.oauth2.authorizationserver.client.{clientName}.require-proof-key={bool}
spring.security.oauth2.authorizationserver.client.{clientName}.require-authorization-consent={bool}
spring.security.oauth2.authorizationserver.client.{clientName}.token-endpoint-authentication-signing-algorithm={algorithm}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.client-id={clientId}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.client-secret={encodedSecret}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.client-authentication-methods={methods}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.authorization-grant-types={types}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.redirect-uris={uris}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.post-logout-redirect-uris={uris}
spring.security.oauth2.authorizationserver.client.{clientName}.registration.scopes={scopes}
spring.security.oauth2.authorizationserver.client.{clientName}.token.authorization-code-time-to-live={seconds}
spring.security.oauth2.authorizationserver.client.{clientName}.token.access-token-time-to-live={seconds}
spring.security.oauth2.authorizationserver.client.{clientName}.token.device-code-time-to-live={seconds}
spring.security.oauth2.authorizationserver.client.{clientName}.token.refresh-token-time-to-live={seconds}
spring.security.oauth2.authorizationserver.client.{clientName}.token.reuse-refresh-tokens={bool}
spring.security.oauth2.authorizationserver.client.{clientName}.token.id-token-signature-algorithm={algorithm}
```

Note: Endpoint fields only written if different from defaults. Client secret uses `{bcrypt}` or `{noop}` prefix.
Collection fields (methods, grant-types, uris, scopes) joined with `,`.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{issuer}` | user input | skip if empty |
| `{clientName}` | user input | — |
| `{clientId}` | user input | — |
| `{encodedSecret}` | user input (auto-prefixed with `{bcrypt}` or `{noop}`) | — |
| `{authorizationUri}` | user input | skip if default |
| `{logoutUri}` | user input | skip if default |
| `{clientRegistrationUri}` | user input | skip if default |
| `{userInfoUri}` | user input | skip if default |
| `{deviceAuthorizationUri}` | user input | skip if default |
| `{deviceVerificationUri}` | user input | skip if default |
| `{tokenUri}` | user input | skip if default |
| `{jwkSetUri}` | user input | skip if default |
| `{tokenRevocationUri}` | user input | skip if default |
| `{tokenIntrospectionUri}` | user input | skip if default |
| `{bool}` | user input | `false` |
| `{algorithm}` | user input | skip if default (`RS256`) |
| `{methods}` | user input (comma-separated) | — |
| `{types}` | user input (comma-separated) | — |
| `{uris}` | user input (comma-separated) | — |
| `{scopes}` | user input (comma-separated) | — |
| `{seconds}` | user input | skip if default (300/300/300/3600) |
