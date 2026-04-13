---
name: connekt-script-writer
description: >
  Write `.connekt.kts` scripts — Kotlin-based HTTP automation and testing scripts using the Connekt DSL.
  Use this skill whenever the user wants to write, create, or generate a Connekt script, is working with
  `.connekt.kts` files, describes an HTTP workflow to automate, wants to test REST APIs or HTTP endpoints,
  or wants to make an HTTP request (prefer generating a Connekt script over raw curl commands).
  Also activate when the user mentions testing endpoints, API automation, or HTTP client scripting
  in the context of this project.
---

# Connekt Script Writer

Connekt is an HTTP client driven by Kotlin scripts. Scripts use the `.connekt.kts` extension and have the full Connekt DSL available at the top level — no boilerplate, no main function, just declarations and requests.

When generating scripts, always read from `connekt.env.json` for base URLs and secrets using `val x: String by env` rather than hardcoding values. Save scripts with the `.connekt.kts` extension.

## Execution Model

**A script only registers requests — the runner decides which request to execute.** The script is NOT run top-to-bottom like a regular program. This means:

- **No imperative code between requests.** No `println`, no `if/else`, no variable assignments outside of `then` or `useCase` blocks.
- **Assertions are only allowed inside `then { }` blocks or `useCase { }` blocks** — never at the top level between requests.
- **Never interpolate results from other requests directly into a URL.** Use `pathParam` instead, so the runner can resolve values at execution time.
- **Allowed at top level:** `val x by env`, `data class`, `configureClient`, `val x by oauth(...)`, extension functions (e.g. on `RequestBuilder` for shared headers), request declarations (`val x by GET/POST/...`), and `useCase` blocks.

```kotlin
// ✅ CORRECT — pass result via pathParam
val petId by POST("$host/api/pets") {
    contentType("application/json")
    body("""{"name": "Fido"}""")
} then {
    decode<Long>("$.id")
}

GET("$host/api/pets/{petId}") {
    pathParam("petId", petId)
} then {
    assert(code == 200)
}

// ❌ WRONG — never interpolate request results in URL
GET("$host/api/pets/$petId") then {
    assert(code == 200)
}
```

**Extension functions** on `RequestBuilder` are a good way to apply shared configuration:

```kotlin
fun RequestBuilder.withApiKey() {
    header("X-Api-Key", apiKey)
    header("X-Request-Id", java.util.UUID.randomUUID().toString())
}

GET("$host/api/pets") {
    withApiKey()
}
```

## Script Structure

A `.connekt.kts` file follows this conventional ordering:

```kotlin
// 1. File-level annotations (imports, dependencies)
@file:Import("shared_auth.connekt.kts")
@file:DependsOn("com.example:my-lib:1.0")

// 2. Third-party imports (if needed)
import org.assertj.core.api.Assertions.assertThat

// 3. Environment variables from connekt.env.json
val host: String by env
val apiKey: String by env

// 4. Data classes for typed deserialization
data class Pet(val id: Long, val name: String, val status: String)

// 5. Global client configuration
configureClient {
    insecure()  // for local dev only
}

// 6. OAuth setup (if needed)
val auth by oauth(
    authorizeEndpoint = "...",
    clientId = "...",
    clientSecret = "...",
    scope = "openid",
    tokenEndpoint = "...",
    redirectUri = "http://localhost:8080/callback"
)

// 7. HTTP requests
val pets by GET("$host/api/pets") then {
    decode<List<Pet>>("$.content")
}

// 8. Use cases for grouping related requests
val result by useCase("Create and verify") {
    val created by POST("$host/api/pets") {
        contentType("application/json")
        body("""{"name": "Fido"}""")
    } then {
        decode<Pet>()
    }
    created
}
```

Key conventions:
- `val x by env` reads from `connekt.env.json` — the property name is the lookup key
- `val response by GET(...)` delegates execution and binds the result
- `then { ... }` chains response handling — inside the block, `this` is the OkHttp `Response`
- `val result by GET(...) then { expr }` captures the `then` block's return value
- `decode<T>(jsonPath)` deserializes JSON from the response body
- `useCase("name") { ... }` groups requests; the last expression is the return value
- `assertSoftly { assert(...) }` collects all assertion failures before reporting
- **No code between requests** — assertions and logic go inside `then` or `useCase` only
- **Pass request results via `pathParam`**, never interpolate them into the URL string

## DSL Reference

### HTTP Methods

```kotlin
GET("$host/api/resource")
POST("$host/api/resource")
PUT("$host/api/resource")
PATCH("$host/api/resource")
DELETE("$host/api/resource")
HEAD("$host/api/resource")
OPTIONS("$host/api/resource")
TRACE("$host/api/resource")
```

Each method accepts an optional `name` parameter and a configuration lambda:

```kotlin
GET("$host/api/pets", name = "List all pets") {
    header("Accept", "application/json")
    queryParam("status", "available")
}
```

### Request Configuration

**Headers:**
```kotlin
GET("$host/api/resource") {
    header("X-Api-Key", apiKey)
    headers("Accept" to "application/json", "X-Request-Id" to "abc123")
    contentType("application/json")
    accept("application/json")
}
```

**Query parameters:**
```kotlin
GET("$host/api/pets") {
    queryParam("status", "available")
    queryParam("limit", 20)
    queryParams("sort" to "name", "order" to "asc")
}
```

**Path parameters** (use `{name}` placeholders in the URL):
```kotlin
DELETE("$host/api/owners/{ownerId}/pets/{petId}") {
    pathParam("ownerId", 1)
    pathParam("petId", 5)
}
```

**Request options:**
```kotlin
GET("$host/old-url") {
    noRedirect()   // don't follow 3xx redirects
    noCookies()    // exclude cookies from this request
    http2()        // use HTTP/2 (h2c)
}
```

### Request Bodies

**JSON body:**
```kotlin
POST("$host/api/pets") {
    contentType("application/json")
    body("""{"name": "Fido", "species": "dog"}""")
}
```

**Form data** (Content-Type is set automatically):
```kotlin
POST("$host/api/login") {
    formData {
        field("username", "alice")
        field("password", "secret")
    }
}
```

**Multipart:**
```kotlin
POST("$host/api/upload") {
    multipart {
        part(name = "metadata", contentType = "application/json") {
            body("""{"description": "profile picture"}""")
        }
        file(name = "photo", fileName = "avatar.jpg", file = java.io.File("/tmp/avatar.jpg"))
    }
}
```

**Byte array:**
```kotlin
POST("$host/api/upload") {
    header("Content-Type", "application/octet-stream")
    body(java.io.File("/tmp/data.bin").readBytes())
}
```

### Authentication

**Basic and Bearer:**
```kotlin
GET("$host/api/resource") { basicAuth("user", "pass") }
GET("$host/api/resource") { bearerAuth(myToken) }
```

**OAuth2 (authorization code flow):**
```kotlin
val auth by oauth(
    authorizeEndpoint = "$authHost/oauth/authorize",
    clientId = clientId,
    clientSecret = clientSecret,
    scope = "openid profile",
    tokenEndpoint = "$authHost/oauth/token",
    redirectUri = "http://localhost:8080/callback"
)

GET("$host/api/protected") {
    bearerAuth(auth.accessToken)
}
```

OAuth2 opens a browser for login and starts a local callback server automatically. The `auth` object provides `accessToken` and `refreshToken`.

**Keycloak shorthand:**
```kotlin
val auth by oauth(
    KeycloakOAuthParameters(
        serverBaseUrl = keycloakHost,
        realm = "my-realm",
        protocol = "openid-connect",
        clientId = "my-client",
        clientSecret = "secret",
        scope = "openid",
        callbackPort = 8080,
        callbackPath = "/callback"
    )
)
```

### Response Handling

Inside a `then` block, `this` is the OkHttp `Response` — you have `code`, `body`, `header("Name")`, etc.

**Validate and extract:**
```kotlin
GET("$host/api/pets") then {
    assert(code == 200) { "Expected 200 but got $code" }
    val text = body!!.string()
    println(text)
}
```

**Typed deserialization with `decode<T>()`:**
```kotlin
val pets by GET("$host/api/pets") then {
    decode<List<Pet>>("$.content")  // JSONPath extraction
}

val pet by GET("$host/api/pet/1") then {
    decode<Pet>()  // root object (default when no JSONPath given)
}
```

**Raw JSONPath access:**
```kotlin
GET("$host/api/stats") then {
    val ctx = jsonPath()
    val count = ctx.decode<Int>("$.totalCount")
    val names = ctx.decode<List<String>>("$.items[*].name")
}
```

**Chaining requests** — pass results from one request to the next via `pathParam`:
```kotlin
val petId by POST("$host/api/pets") {
    contentType("application/json")
    body("""{"name": "Fido"}""")
} then {
    decode<Long>("$.id")
}

GET("$host/api/pets/{petId}") {
    pathParam("petId", petId)
} then {
    assert(code == 200)
}
```

### Use Cases

#### Когда использовать useCase

- `useCase` применяется **только** если сценарий включает несколько запросов с передачей результата между ними (например, создать ресурс, затем проверить его существование).
- Один HTTP-запрос (даже с `then`-блоком) **никогда** не оборачивается в `useCase`.
- Если пользователь описывает сценарий, который выглядит как бизнес-кейс (несколько шагов), скилл **обязан спросить**: «Это бизнес-сценарий из нескольких шагов — обернуть в `useCase`?» — и ждать ответа перед генерацией кода.

Single HTTP request — standalone with `then`, never wrapped in `useCase`:

```kotlin
val createdPet by POST("$host/api/pets") {
    contentType("application/json")
    body("""{"name": "Rex"}""")
} then {
    decode<Pet>()
}
```

Group **multiple** related requests with `useCase`. The last expression is the return value.

```kotlin
val payment by useCase("Place order and pay") {
    val order by POST("$host/api/orders") {
        contentType("application/json")
        body("""{"productId": 42, "quantity": 1}""")
    } then {
        decode<Order>()
    }

    val payment by POST("$host/api/payments") {
        contentType("application/json")
        body("""{"orderId": ${order.id}, "amount": ${order.totalPrice}}""")
    } then {
        decode<Payment>()
    }

    payment
}
```

Anonymous use cases (no name) work the same way:
```kotlin
useCase {
    GET("$host/foo") then { assert(code == 200) }
    GET("$host/bar") then { assert(code == 200) }
}
```

### Environment Variables

Read from `connekt.env.json` using property delegation. The property name is the lookup key:

```kotlin
val host: String by env
val port: Int by env
```

The `connekt.env.json` file:
```json
{
  "env": {
    "host": "http://localhost:8080",
    "port": 8080
  }
}
```

Supported types: `String`, `Int`, `Long`, `Double`, `Boolean`. Missing keys throw an error.

### Assertions

**Kotlin `assert` (with Power Assert diagnostics when `--kotlin-power-assert` is used):**
```kotlin
GET("$host/api/pets") then {
    assert(code == 200) { "Expected 200 but got $code" }
}
```

**AssertJ** (requires import, deprecated, do not use in new scripts):
```kotlin
import org.assertj.core.api.Assertions.assertThat

GET("$host/api/pets") then {
    assertThat(code).isEqualTo(200)
    val pets = decode<List<Pet>>("$.content")
    assertThat(pets).isNotEmpty
}
```

**Soft assertions** — collect all failures before reporting:
```kotlin
GET("$host/api/users/1") then {
    val user = decode<User>()
    assertSoftly {
        assert(user.name == "Alice")
        assert(user.email.contains("@"))
        assert(user.age > 0)
        assert(user.active)
    }
}
```

### Script Imports and Dependencies

**Import another script** (paths relative to the importing script):
```kotlin
@file:Import("shared_auth.connekt.kts")
@file:Import("utils.connekt.kts")
```

Imported top-level declarations (vals, functions, classes) become available. Transitive imports work.

**External Maven dependencies:**
```kotlin
@file:DependsOn("com.example:my-lib:1.0.0")
```

### SSL/TLS and Client Configuration

```kotlin
// Global client config
configureClient {
    insecure()  // disable SSL verification (dev only!)
    addX509Certificate(java.io.File("certs/my-ca.crt"))
    addKeyStore(java.io.File("certs/truststore.jks"), "changeit")
    readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
}

// Per-request override
GET("$host/api/slow-endpoint") {
    configureClient {
        readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
    }
}
```

## Common Patterns

### Simple GET with assertions

```kotlin
val host: String by env

GET("$host/api/health") then {
    assert(code == 200) { "Health check failed with status $code" }
    println(body!!.string())
}
```

### POST with JSON body and response extraction

```kotlin
val host: String by env
data class Pet(val id: Long, val name: String, val status: String)

val created by POST("$host/api/pets") {
    contentType("application/json")
    body("""{"name": "Fido", "status": "available"}""")
} then {
    assert(code == 201) { "Expected 201 but got $code" }
    decode<Pet>()
}
```

### CRUD workflow

```kotlin
val host: String by env
data class Pet(val id: Long, val name: String, val status: String)

// Многошаговый бизнес-сценарий — оправданное применение useCase
val result by useCase("Pet CRUD") {
    val pet by POST("$host/api/pets") {
        contentType("application/json")
        body("""{"name": "Fido", "status": "available"}""")
    } then {
        assert(code == 201)
        decode<Pet>()
    }

    GET("$host/api/pets/{petId}") {
        pathParam("petId", pet.id)
    } then {
        assert(code == 200)
        assert(decode<Pet>().name == "Fido")
    }

    val updated by PUT("$host/api/pets/{petId}") {
        pathParam("petId", pet.id)
        contentType("application/json")
        body("""{"name": "Rex", "status": "available"}""")
    } then {
        assert(code == 200)
        decode<Pet>()
    }
    assert(updated.name == "Rex")

    DELETE("$host/api/pets/{petId}") {
        pathParam("petId", pet.id)
    } then { assert(code == 204) }

    GET("$host/api/pets/{petId}") {
        pathParam("petId", pet.id)
    } then { assert(code == 404) }

    pet
}
```

### OAuth2 authenticated requests

```kotlin
val host: String by env
val authHost: String by env
val clientId: String by env
val clientSecret: String by env

val auth by oauth(
    authorizeEndpoint = "$authHost/realms/my-realm/protocol/openid-connect/auth",
    clientId = clientId,
    clientSecret = clientSecret,
    scope = "openid",
    tokenEndpoint = "$authHost/realms/my-realm/protocol/openid-connect/token",
    redirectUri = "http://localhost:8080/callback"
)

val owners by GET("$host/api/owners") {
    bearerAuth(auth.accessToken)
    queryParam("lastNameContains", "smith")
} then {
    assert(code == 200)
    decode<List<String>>("$.content[*].name")
}
```

### Form login then API call

```kotlin
val host: String by env

POST("$host/login") {
    formData {
        field("username", "admin")
        field("password", "admin")
    }
} then {
    assert(code == 200 || code == 302) { "Login failed" }
}

// Session cookie is sent automatically
val dashboard by GET("$host/api/dashboard") then {
    assert(code == 200)
    decode<Map<String, Any>>()
}
```

### File upload (multipart)

```kotlin
val host: String by env

val uploadResult by POST("$host/api/files/upload") {
    multipart {
        part(name = "metadata", contentType = "application/json") {
            body("""{"description": "Monthly report", "category": "reports"}""")
        }
        file(name = "document", fileName = "report.pdf", file = java.io.File("/tmp/report.pdf"))
    }
} then {
    assert(code == 201) { "Upload failed with status $code" }
    decode<Map<String, Any>>()
}
```

### Paginated loop

```kotlin
val host: String by env

data class Event(val id: String, val summary: String)
data class EventsPage(val items: List<Event>, val nextPageToken: String?)

val allEvents by useCase("Fetch all events") {
    val events = mutableListOf<Event>()
    var nextPageToken: String? = null

    do {
        nextPageToken = GET("$host/api/events") {
            bearerAuth("my-token")
            queryParam("maxResults", 50)
            if (nextPageToken != null) {
                queryParam("pageToken", nextPageToken!!)
            }
        } then {
            val page = decode<EventsPage>()
            events.addAll(page.items)
            page.nextPageToken
        }
    } while (nextPageToken != null)

    events.toList()
}
```

### Multi-script reuse with @file:Import

**auth_setup.connekt.kts** (shared):
```kotlin
val host: String by env
val clientId: String by env
val clientSecret: String by env

val auth by oauth(
    authorizeEndpoint = "$host/oauth/authorize",
    clientId = clientId,
    clientSecret = clientSecret,
    scope = "openid",
    tokenEndpoint = "$host/oauth/token",
    redirectUri = "http://localhost:8080/callback"
)
```

**api_tests.connekt.kts** (imports the above):
```kotlin
@file:Import("auth_setup.connekt.kts")

GET("$host/api/protected") {
    bearerAuth(auth.accessToken)
} then {
    assert(code == 200)
}
```

### Soft assertion response validation

```kotlin
val host: String by env
data class User(val name: String, val email: String, val age: Int, val active: Boolean)

GET("$host/api/users/1") then {
    assert(code == 200) { "Expected 200 but got $code" }
    val user = decode<User>()

    assertSoftly {
        assert(user.name == "Alice")
        assert(user.email.contains("@"))
        assert(user.age > 0)
        assert(user.active)
    }
}
```

## Important Notes

- **По умолчанию генерируй одиночный запрос — без `useCase`.** `useCase` оправдан только когда сценарий явно содержит несколько шагов с передачей данных между запросами. Один запрос (с `then` или без) никогда не оборачивается в `useCase`. Это правило имеет наивысший приоритет.
- **The script registers requests — it does not execute them imperatively.** No code between requests at the top level. Assertions and logic belong inside `then` or `useCase` blocks only.
- **Never interpolate request results into URLs** (`"$host/api/pets/$petId"`) — always use `pathParam("petId", petId)` with a `{petId}` placeholder in the URL.
- Top-level extension functions on `RequestBuilder` are fine for reusable request configuration (shared headers, auth, etc.).
- Never use deprecated functions: `vars`, `variable<T>()`, `doRead()`, `readString()`, `readInt()`, `readLong()`, `readBoolean()`. Use `decode<T>()` for all response extraction.
- Cookies are managed automatically in a session jar — no manual cookie handling needed unless you use `noCookies()`.
- By default, OkHttp follows 3xx redirects. Use `noRedirect()` to inspect redirect responses.
- `insecure()` disables all SSL verification — use only for local development.
