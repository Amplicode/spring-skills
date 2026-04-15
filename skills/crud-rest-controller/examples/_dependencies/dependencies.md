# Dependencies

| Artifact ID | Group ID | Scope | Condition |
|-------------|----------|-------|-----------|
| spring-boot-starter-web | org.springframework.boot | implementation | always |
| spring-boot-starter-data-jpa | org.springframework.boot | implementation | always |
| amplicode-ra-utils | io.amplicode | implementation | when ObjectPatcher patch strategy selected |
| spring-boot-starter-validation | org.springframework.boot | implementation | when entity has validation annotations |

## Gradle Kotlin DSL
```kotlin
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
// conditional:
implementation("io.amplicode:amplicode-ra-utils")
implementation("org.springframework.boot:spring-boot-starter-validation")
```

## Gradle Groovy
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
// conditional:
implementation 'io.amplicode:amplicode-ra-utils'
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## Maven
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- conditional: -->
<dependency>
    <groupId>io.amplicode</groupId>
    <artifactId>amplicode-ra-utils</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
