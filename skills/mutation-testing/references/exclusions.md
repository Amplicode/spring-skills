# What to exclude from mutation testing

Exclude code with **no behaviour worth asserting on**; keep anything with a
branch, a computation or a decision, even when it is currently untested — a
missing test must stay visible in the report, a getter must not.

## Put the set to the user in one questionnaire

The exclusion set decides what the score means, so it is the user's call rather
than a default this skill imposes silently. Ask with a single `AskUserQuestion`
call, not one question per turn. Drop a question the request already answers.

Only the last column reaches the user — the `Becomes` column is for you. So every
description has to name the patterns it will write: approving "bean wiring" is not
approving four globs nobody was shown.

**Q1 · header `Noise`** · `multiSelect: true` — code that carries no behaviour to
assert on. Recommend all three: without them the first report is dominated by
mutants nobody would ever write a test for, and a report read once is a report
abandoned.

| Option | Becomes | Description to show the user |
|--------|---------|------------------------------|
| Bean wiring and bootstrap | `'com.example.MyApplication'`, `'*Configuration'`, `'*Config'`, `'*Properties'` | Wiring, not behaviour — nobody tests how a bean is assembled. Excludes the application class, `*Configuration`, `*Config`, `*Properties`. |
| Mappers and other generated code | `'*MapperImpl'` (MapStruct), `'*_'` (JPA metamodel) | Written by a code generator, so testing it tests the generator. Excludes `*MapperImpl` and `*_`. |
| Accessors and `Object` boilerplate | `excludedMethods = ['equals', 'hashCode', 'toString', 'get*', 'set*', 'is*']` | A getter just hands back a field. Excludes `equals`, `hashCode`, `toString`, `get*`, `set*`, `is*` — which also skips real logic named that way, like `getDiscountedPrice()`. |

**Q2 · header `Layers`** · `multiSelect: true` — which layers should be left out of
the measured set, because their tests live in a suite outside `testSourceSets`?
Selecting one narrows `targetClasses`: the score then describes the part of the
code the driving suite is actually responsible for, and the report names what was
left out. Left in, such a layer counts as untested however well tested it is.

| Option | What marks the layer | Description to show the user |
|--------|----------------------|------------------------------|
| Controllers/RestControllers | `@Controller`, `@RestController` | Covered by integration tests that are not driving this analysis, so they report as untested. Drops `@Controller`/`@RestController` classes from `targetClasses`. |
| Repositories | `@Repository`, or an interface extending `JpaRepository` / `CrudRepository` / `ListCrudRepository` | Only an interface, with no code of yours to change — usually a no-op. Drops `@Repository` and `*Repository` interfaces from `targetClasses`. |

**Q3 · header `Annotation`** — a marker annotation lets a single class or method be
skipped from the code itself, which is the only way to carve out something no name
pattern captures. Offer to create one.

| Option | Becomes | Description to show the user |
|--------|---------|------------------------------|
| Add `@DoNotMutate` (Recommended) | a `DoNotMutate` annotation in the project's root package; no PIT setting needed | Creates a `DoNotMutate` annotation in your root package; mark a class or a method with it. No build setting needed. |
| Add one under a name I will type | the same annotation under that name, plus a `features` entry registering it | Same, under a name you type here. Also adds a `features` entry to `pitest {}` registering that name. |
| Skip it | nothing | No annotation is created. Exclusions stay in the build file only, all visible in one place. |

```java
package com.example;

@Retention(RetentionPolicy.CLASS)   // RUNTIME works too; SOURCE is invisible to PIT
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DoNotMutate {
}
```

`DoNotMutate`, `Generated` and `CoverageIgnore` are recognised with no
configuration at all, which is why the first option needs none. A name of your own
has to be registered, and the parameter **replaces** those three rather than adding
to them, so always list all four — dropping them stops `@DoNotMutate` and
`@CoverageIgnore` from working, with no error, just a moved score:

```groovy
features = ['+fann(annotation[TypedName] annotation[DoNotMutate] annotation[Generated] annotation[CoverageIgnore])']
```

If that option comes back without a name, ask for it in one short follow-up. Never
invent one: an annotation the user did not name is a class they did not ask for.

## After the answers

Write the chosen globs into the `pitest {}` block, grouped by the question they
came from, so a later reader can tell which entries were a decision about noise
and which were a decision about what is being measured.
