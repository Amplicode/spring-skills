#!/usr/bin/env python3
"""Summarise a PIT mutations.xml into the numbers and the actionable mutants.

Reading the XML by hand is slow and easy to get subtly wrong: PIT's own notion of
"killed" is the `detected` attribute, not a status name, so counting statuses
by hand quietly misclassifies TIMED_OUT and RUN_ERROR. This uses `detected` for
the score and reports statuses only as a breakdown.

Usage:
    summarize_mutations.py [path/to/mutations.xml]

Exit codes:
    0  report parsed
    2  report missing (the run produced none - do NOT fall back to an old number)
    3  report present but unparseable
"""

import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict

DEFAULT_PATH = "build/reports/pitest/mutations.xml"
ACTIONABLE = ("SURVIVED", "NO_COVERAGE")


def text(node, tag):
    return (node.findtext(tag) or "").strip()


def pct(part, whole):
    return f"{100.0 * part / whole:.1f}%" if whole else "n/a"


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_PATH

    try:
        root = ET.parse(path).getroot()
    except FileNotFoundError:
        print(f"NO REPORT: {path} does not exist - the run produced no result.")
        print("Find the cause in the build output (red test, PitHelpError, no mutations).")
        return 2
    except ET.ParseError as exc:
        print(f"UNPARSEABLE: {path}: {exc}")
        return 3

    mutations = list(root)
    if not mutations:
        print(f"NO MUTANTS: {path} is empty - PIT found nothing to mutate.")
        print("Either the code has no mutable logic yet, or targetClasses/excludedClasses")
        print("no longer match the codebase. Check which before reporting a score.")
        return 0

    statuses = Counter()
    per_class = defaultdict(Counter)
    actionable = defaultdict(list)
    killed = 0

    for m in mutations:
        status = m.get("status", "UNKNOWN")
        detected = m.get("detected", "false") == "true"
        cls = text(m, "mutatedClass")
        short = cls.rsplit(".", 1)[-1]

        statuses[status] += 1
        per_class[cls][status] += 1
        if detected:
            killed += 1
            per_class[cls]["_killed"] += 1

        if status in ACTIONABLE:
            actionable[status].append({
                "where": f"{short}.{text(m, 'mutatedMethod')}",
                "at": f"{text(m, 'sourceFile')}:{text(m, 'lineNumber')}",
                "mutator": text(m, "mutator").rsplit(".", 1)[-1],
                "what": text(m, "description"),
                "cls": cls,
            })

    total = len(mutations)
    covered = total - statuses.get("NO_COVERAGE", 0)

    print(f"Mutation score: {killed}/{total} ({pct(killed, total)})")
    print(f"Test strength:  {killed}/{covered} ({pct(killed, covered)})")
    print("Statuses:       " + ", ".join(f"{s} {n}" for s, n in statuses.most_common()))

    def unkilled(cls):
        counts = per_class[cls]
        return counts.get("SURVIVED", 0) + counts.get("NO_COVERAGE", 0)

    print("\nPer class (killed/total), worst first:")
    for cls in sorted(per_class, key=lambda c: (-unkilled(c), c)):
        counts = per_class[cls]
        cls_total = sum(n for s, n in counts.items() if s != "_killed")
        rest = ", ".join(
            f"{s} {n}" for s, n in sorted(counts.items())
            if s not in ("_killed", "KILLED")
        )
        print(f"  {counts['_killed']}/{cls_total}  {cls}" + (f"   [{rest}]" if rest else ""))

    for status, heading in (
        ("SURVIVED", "Survived (the line runs, nothing asserts on the result)"),
        ("NO_COVERAGE", "No coverage (not reached by the driving suite)"),
    ):
        items = actionable.get(status)
        if not items:
            continue
        print(f"\n{heading}, {len(items)}:")
        for it in sorted(items, key=lambda i: (i["cls"], i["at"])):
            print(f"  {it['where']}  {it['at']}  {it['mutator']}")
            if it["what"]:
                print(f"      {it['what']}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
