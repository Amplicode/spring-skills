# FormLogin DSL fragment (Java)

## Insert Point
Inside filterChain method body, before `return http.build();`

## Code

```java
// defaults: enabled, handlerType=STATUS, no URLs set
// Default output (no customization):
http.formLogin(org.springframework.security.config.Customizer.withDefaults());

// When disabled=true:
http.formLogin(formLogin -> formLogin.disable());

// With STATUS handlers:
http.formLogin(formLogin -> formLogin
    .loginPage("{loginUrl}")                                  // if loginUrl is not blank
    .usernameParameter("{usernameParam}")                      // if usernameParameter is not blank
    .passwordParameter("{passwordParam}")                      // if passwordParameter is not blank
    .failureUrl("{failureUrl}")                                // if failureUrl is not blank
    .defaultSuccessUrl("{defaultSuccessUrl}")                   // if defaultSuccessUrl is not blank
    .loginProcessingUrl("{loginProcessingUrl}")                 // if loginProcessingUrl is not blank
    .failureForwardUrl("{failureForwardUrl}")                   // if failureForwardUrl is not blank
    .successForwardUrl("{successForwardUrl}")                   // if successForwardUrl is not blank
    .authenticationDetailsSource({authDetailsSourceBean})        // if authenticationDetailsSource bean is set
    .successHandler((request, response, authentication) -> response.setStatus(org.springframework.http.HttpStatus.{successStatus}.value()))  // if successHandlerStatus is set
    .failureHandler((request, response, exception) -> response.setStatus(org.springframework.http.HttpStatus.{failureStatus}.value()))       // if failureHandlerStatus is set
    .permitAll()                                               // if loginUrl or loginProcessingUrl is not empty
);

// With BEAN handlers:
http.formLogin(formLogin -> formLogin
    .successHandler({successHandlerBean})                      // if successHandler bean is set
    .failureHandler({failureHandlerBean})                      // if failureHandler bean is set
    .permitAll()
);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{loginUrl}` | user input | skip if empty |
| `{usernameParam}` | user input | skip if empty |
| `{passwordParam}` | user input | skip if empty |
| `{failureUrl}` | user input | skip if empty |
| `{defaultSuccessUrl}` | user input | skip if empty |
| `{loginProcessingUrl}` | user input | skip if empty |
| `{failureForwardUrl}` | user input | skip if empty |
| `{successForwardUrl}` | user input | skip if empty |
| `{successStatus}` | user choice | skip if not set (e.g. `OK`) |
| `{failureStatus}` | user choice | skip if not set (e.g. `UNAUTHORIZED`) |
| `{authDetailsSourceBean}` | user input (bean ref) | skip if not set |
| `{successHandlerBean}` | user input (bean ref) | skip if not set |
| `{failureHandlerBean}` | user input (bean ref) | skip if not set |
