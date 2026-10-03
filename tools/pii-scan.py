#!/usr/bin/env python3
"""pootOS PII scan gate.

Scans every file reported by ``git ls-files`` for common personal-data
patterns -- e-mail, phone, IBAN, credit card (validated with the Luhn
checksum) and national-ID-like numbers -- and fails (exit 1) when a match is
not covered by the narrow allowlist (``tools/pii-allowlist.txt``).

The patterns are deliberately strict so that the repository's own
documentation, which discusses "PII" as a concept, is not flagged. Run it with
no arguments from the repository root::

    python3 tools/pii-scan.py
"""

from __future__ import annotations

import fnmatch
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ALLOWLIST = Path(__file__).resolve().parent / "pii-allowlist.txt"

# A strict e-mail pattern: a dotted domain that ends in a letter-only TLD.
EMAIL = re.compile(
    r"(?<![A-Za-z0-9._%+-])"
    r"[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}"
    r"(?![A-Za-z0-9.-])"
)
# International ("+") and parenthesised North-American formats only.
PHONE = re.compile(
    r"(?<![\dA-Za-z+])(?:"
    r"\+\d[\d\s()./-]{5,}\d"
    r"|\(\d{3}\)\s*\d{3}[-.\s]\d{4}"
    r")(?!\d)"
)
# Country code + check digits + BBAN, as in ISO 13616.
IBAN = re.compile(r"(?<![A-Za-z0-9])[A-Z]{2}\d{2}[A-Z0-9]{11,30}(?![A-Za-z0-9])")
# A 13-19 digit candidate; kept only when the Luhn checksum passes.
CARD = re.compile(r"(?<!\d)(?:\d[ -]?){12,18}\d(?!\d)")
# US-style national identifier; precise to avoid matching version strings.
NATIONAL_ID = re.compile(r"(?<!\d)\d{3}-\d{2}-\d{4}(?!\d)")


def luhn_ok(digits: str) -> bool:
    """Return True when ``digits`` passes the Luhn checksum."""
    total = 0
    parity = len(digits) % 2
    for index, char in enumerate(digits):
        value = int(char)
        if index % 2 == parity:
            value *= 2
            if value > 9:
                value -= 9
        total += value
    return total % 10 == 0


def matches_in(line: str) -> list[tuple[str, str]]:
    """Return ``(kind, text)`` PII matches found in one line."""
    found: list[tuple[str, str]] = []
    for match in EMAIL.finditer(line):
        found.append(("email", match.group(0)))
    for match in PHONE.finditer(line):
        found.append(("phone", match.group(0)))
    for match in IBAN.finditer(line):
        found.append(("iban", match.group(0)))
    for match in CARD.finditer(line):
        digits = re.sub(r"[ -]", "", match.group(0))
        if 13 <= len(digits) <= 19 and luhn_ok(digits):
            found.append(("card", match.group(0)))
    for match in NATIONAL_ID.finditer(line):
        found.append(("national-id", match.group(0)))
    return found


def load_allowlist(path: Path) -> tuple[list[str], list[re.Pattern], list[str]]:
    """Parse the allowlist into path globs, line patterns and errors."""
    paths: list[str] = []
    patterns: list[re.Pattern] = []
    errors: list[str] = []
    if not path.exists():
        return paths, patterns, errors
    previous_comment: str | None = None
    for number, raw in enumerate(
        path.read_text(encoding="utf-8").splitlines(), 1
    ):
        stripped = raw.strip()
        if not stripped:
            previous_comment = None
            continue
        if stripped.startswith("#"):
            previous_comment = stripped.lstrip("#").strip()
            continue
        entry, _, inline = stripped.partition("#")
        entry = entry.strip()
        comment = inline.strip() or previous_comment
        previous_comment = None
        if not comment:
            errors.append(
                "{}:{}: allowlist entry needs a comment".format(
                    path.name, number
                )
            )
        if entry.startswith("path:"):
            paths.append(entry[len("path:") :].strip())
        elif entry.startswith("pattern:"):
            patterns.append(re.compile(entry[len("pattern:") :].strip()))
        else:
            errors.append(
                "{}:{}: unknown allowlist entry '{}'".format(
                    path.name, number, entry
                )
            )
    return paths, patterns, errors


def tracked_files() -> list[str]:
    """Return the repository's tracked files."""
    output = subprocess.run(
        ["git", "-C", str(ROOT), "ls-files", "-z"],
        check=True,
        capture_output=True,
    ).stdout
    return [
        name.decode("utf-8", "surrogateescape")
        for name in output.split(b"\x00")
        if name
    ]


def is_allowed_path(path: str, globs: list[str]) -> bool:
    """Return True when the whole file is allowlisted by a path glob."""
    return any(fnmatch.fnmatch(path, glob) for glob in globs)


def main() -> int:
    """Run the scan and return a process exit code."""
    globs, patterns, errors = load_allowlist(ALLOWLIST)
    if errors:
        for error in errors:
            print(error)
        return 2
    findings: list[str] = []
    for relative in tracked_files():
        if is_allowed_path(relative, globs):
            continue
        data = (ROOT / relative).read_bytes()
        if b"\x00" in data:
            continue
        text = data.decode("utf-8", "replace")
        for number, line in enumerate(text.splitlines(), 1):
            if any(pattern.search(line) for pattern in patterns):
                continue
            for kind, match in matches_in(line):
                findings.append(
                    "{}:{}: [{}] {}".format(relative, number, kind, match)
                )
    if findings:
        print("PII scan FAILED: {} finding(s)".format(len(findings)))
        for finding in findings:
            print("  " + finding)
        return 1
    print("PII scan PASSED: no findings in tracked files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
