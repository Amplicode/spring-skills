---
name: spring-planning
description: Create structured implementation plan in docs/plans/
---

# Implementation Plan Creation

create an implementation plan in `docs/plans/yyyymmdd-<task-name>.md` with interactive context gathering.

## step 0: parse intent and gather context

before asking questions, understand what the user is working on:

1. **parse user's command arguments** to identify intent:
    - "add feature Z" / "implement W" → feature development
    - "fix bug" / "debug issue" → bug fix plan
    - "refactor X" / "improve Y" → refactoring plan
    - "migrate to Z" / "upgrade W" → migration plan
    - generic request → explore current work

2. **gather relevant context quickly** — use direct spring-explore skill, NOT an Explore agent. keep discovery under 30 seconds:

   **CRITICAL: do NOT launch an Explore agent. the goal is a quick scan, not exhaustive analysis. if more context is needed, ask the user in step 1.**

## step 1: present context and ask focused questions

show the discovered context, then ask questions **one at a time** using the AskUserQuestion tool:

"based on your request, i found: [context summary]"

**ask questions one at a time (do not overwhelm with multiple questions):**

1. **plan purpose**: use AskUserQuestion - "what is the main goal?"
    - provide multiple choice with suggested answer based on discovered intent
    - wait for response before next question

2. **scope**: use AskUserQuestion - "which components/files are involved?"
    - provide multiple choice with suggested discovered files/areas
    - wait for response before next question

3. **constraints**: use AskUserQuestion - "any specific requirements or limitations?"
    - can be open-ended if constraints vary widely
    - wait for response before next question

4. **testing approach**: use AskUserQuestion - "do you prefer TDD, regular approach or no tests?"
    - options: "TDD (tests first)" and "Regular (code first, then tests)" and "None (no tests)"
    - store preference for reference during implementation
    - wait for response before next question

5. **plan title**: use AskUserQuestion - "short descriptive title?"
    - provide suggested name based on intent

after all questions answered, synthesize responses into plan context.

## step 1.5: explore approaches

once the problem is understood, propose implementation approaches:

1. **propose 2-3 different approaches** with trade-offs for each
2. **lead with recommended option** and explain reasoning
3. **present conversationally** - not a formal document yet

example format:
```
i see three approaches:

**Option A: [name]** (recommended)
- how it works: ...
- pros: ...
- cons: ...

**Option B: [name]**
- how it works: ...
- pros: ...
- cons: ...

which direction appeals to you?
```

use AskUserQuestion tool to let user select preferred approach before creating the plan.

**skip this step** if:
- the implementation approach is obvious (single clear path)
- user explicitly specified how they want it done
- it's a bug fix with clear solution

## step 2: create plan file

check `docs/plans/` for existing files, then create `docs/plans/yyyymmdd-<task-name>.md` (use current date):

### plan structure

```markdown
# [Plan Title]

## Overview
- clear description of the feature/change being implemented
- problem it solves and key benefits
- how it integrates with existing system

## Context (from discovery)
- files/components involved: [list from step 0]
- related patterns found: [patterns discovered]
- dependencies identified: [dependencies]

## Development Approach
- **testing approach**: [TDD / Regular - from user preference in planning]
- complete each task fully before moving to the next
- make small, focused changes
- **CRITICAL: every task MUST include new/updated tests** for code changes in that task
    - tests are not optional - they are a required part of the checklist
    - write unit tests for new functions/methods
    - write unit tests for modified functions/methods
    - add new test cases for new code paths
    - update existing test cases if behavior changes
    - tests cover both success and error scenarios
- **CRITICAL: all tests must pass before starting next task** - no exceptions
- **CRITICAL: update this plan file when scope changes during implementation**
- run tests after each change
- maintain backward compatibility

## Testing Strategy
- **unit tests**: required for every task (see Development Approach above)
- **e2e tests**: if project has UI-based e2e tests (Playwright, Cypress, etc.):
    - UI changes → add/update e2e tests in same task as UI code
    - backend changes supporting UI → add/update e2e tests in same task
    - treat e2e tests with same rigor as unit tests (must pass before next task)
    - store e2e tests alongside unit tests (or in designated e2e directory)
    - example: if task implements new form field, add e2e test checking form submission

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with ➕ prefix
- document issues/blockers with ⚠️ prefix
- update plan if implementation deviates from original scope
- keep plan in sync with actual work done

## Solution Overview
- high-level approach and architecture chosen
- key design decisions and rationale
- how it fits into the existing system

## Technical Details
- data structures and changes
- parameters and formats
- processing flow

## What Goes Where
- **Implementation Steps** (`[ ]` checkboxes): tasks achievable within this codebase - code changes, tests, documentation updates
- **Post-Completion** (no checkboxes): items requiring external action - manual testing, changes in consuming projects, deployment configs, third-party verifications

## Implementation Steps

<!--
Task structure guidelines:
- Each task = ONE logical unit (one function, one endpoint, one component)
- Use specific descriptive names, not generic "[Core Logic]" or "[Implementation]"
- Each task MUST have a **Files:** block listing files to Create/Modify (before checkboxes)
- Aim for ~5 checkboxes per task (more is OK if logically atomic)
- **CRITICAL: Each task MUST end with writing/updating tests before moving to next**
  - tests are not optional - they are a required deliverable of every task
  - write tests for all NEW code added in this task
  - write tests for all MODIFIED code in this task
  - include both success and error scenarios in tests
  - list tests as SEPARATE checklist items, not bundled with implementation

Example (NOTICE: Files block + tests as separate checklist items):

### Task 1: Add password hashing utility

**Files:**
- Create: `src/auth/hash`
- Create: `src/auth/hash_test`

- [ ] create `src/auth/hash` with HashPassword and VerifyPassword functions
- [ ] implement bcrypt-based hashing with configurable cost
- [ ] write tests for HashPassword (success + error cases)
- [ ] write tests for VerifyPassword (success + error cases)
- [ ] run tests - must pass before task 2

### Task 2: Add user registration endpoint

**Files:**
- Create: `src/api/users`
- Modify: `src/api/router`
- Create: `src/api/users_test`

- [ ] create `POST /api/users` handler in `src/api/users`
- [ ] add input validation (email format, password strength)
- [ ] integrate with password hashing utility
- [ ] write tests for handler success case with table-driven cases
- [ ] write tests for handler error cases (invalid input, missing fields)
- [ ] run tests - must pass before task 3
-->

### Task 1: [specific name - what this task accomplishes]

**Files:**
- Create: `exact/path/to/new_file`
- Modify: `exact/path/to/existing`

- [ ] [specific action with file reference - code implementation]
- [ ] [specific action with file reference - code implementation]
- [ ] write tests for new/changed functionality (success cases)
- [ ] write tests for error/edge cases
- [ ] run tests - must pass before next task

### Task N-1: Verify acceptance criteria
- [ ] verify all requirements from Overview are implemented
- [ ] verify edge cases are handled
- [ ] run full test suite: `<project test command>`
- [ ] run e2e tests if project has them: `<project e2e test command>`
- [ ] verify test coverage meets project standard

### Task N: [Final] Update documentation
- [ ] update README.md if needed
- [ ] update CLAUDE.md if new patterns discovered
- [ ] move this plan to `docs/plans/completed/`

## Post-Completion
*Items requiring manual intervention or external systems - no checkboxes, informational only*

**Manual verification** (if applicable):
- manual UI/UX testing scenarios
- performance testing under load
- security review considerations

**External system updates** (if applicable):
- consuming projects that need updates after this library change
- configuration changes in deployment systems
- third-party service integrations to verify
```

## step 2.1: analyze plan for domain/JPA work

after writing the plan file, scan the plan for tasks that involve JPA entities. also check whether the project uses JPA.

**if the plan touches domain entities AND the project is a JPA project:**

1. activate the `spring-data-jpa` skill
2. follow the skill's environment setup steps
3. ask the user focused questions about the JPA-specific decisions the plan implies — one at a time using AskUserQuestion.
4. after all answers are collected, revise the plan:
    - add JPA-specific notes to relevant tasks (e.g. fetch type, cascade, index hints)
    - add a reminder in each entity task: "> **JPA skill required (spring-data-jpa).** Before writing any entity code: activate the skill and verify the implementation follows its rules. For any deviation — ask the developer before continuing."
    - add the corresponding DB migration task if not already present

**if the plan does NOT touch domain entities or the project is not a JPA project:** skip this step entirely.

## step 3: next steps

after creating the file, tell user: "created plan: `docs/plans/yyyymmdd-<task-name>.md`"

then use AskUserQuestion:

```json
{
  "questions": [{
    "question": "Plan created. What's next?",
    "header": "Next step",
    "options": [
      {"label": "Interactive review", "description": "Open plan in IDE for review and feedback"},
      {"label": "Implement", "description": "Commit plan and start implementing"},
      {"label": "Done", "description": "Commit plan, no further action"}
    ],
    "multiSelect": false
  }]
}
```

- **Interactive review**: open the plan file in the IDE by calling `mcp__ide__getDiagnostics` with the plan file's `uri` (absolute path prefixed with `file://`). This causes the IDE to navigate to the file. Then tell the user: "Plan is open in the IDE — review it and tell me what to change." Wait for feedback in the conversation, apply any requested changes, and ask again with the same options (minus "Interactive review").
- **Implement**: commit plan with message like "docs: add <topic> implementation plan", then begin implementing task 1 interactively in this session. Use TodoWrite tool to track progress and mark todos completed immediately (do not batch).
- **Done**: commit plan with message like "docs: add <topic> implementation plan", stop

## execution enforcement

**Rules during implementation:**

1. **complete each task fully before moving to the next**:
    - STOP before moving to next task
    - if testing approach is TDD or Regular: add/update tests for all new functionality and run them
    - if testing approach is None: verify manually via the running application
    - mark completed items with `[x]` in plan file

2. **if tests are used and fail**:
    - fix the failures before proceeding
    - do NOT move to next task with failing tests

3. **plan tracking during implementation**:
    - update checkboxes immediately when tasks complete
    - add ➕ prefix for newly discovered tasks
    - add ⚠️ prefix for blockers
    - modify plan if scope changes significantly

4. **on completion**:
    - move plan to `docs/plans/completed/`
    - create directory if needed: `mkdir -p docs/plans/completed`

this ensures each task is solid before building on top of it.

## key principles

- **one question at a time** - do not overwhelm user with multiple questions in a single message
- **multiple choice preferred** - easier to answer than open-ended when possible
- **DRY, YAGNI ruthlessly** - avoid unnecessary duplication and features, keep scope minimal (but prefer duplication over premature abstraction when it reduces coupling)
- **lead with recommendation** - have an opinion, explain why, but let user decide
- **explore alternatives** - always propose 2-3 approaches before settling (unless obvious)
- **duplication vs abstraction** - when code repeats, ask user: prefer duplication (simpler, no coupling) or abstraction (DRY but adds complexity)? explain trade-offs before deciding
