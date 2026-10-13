#!/usr/bin/env python3
"""Waliduje bazę słów i zapisuje raport docs/VALIDATION_REPORT.md. Kod wyjścia != 0, gdy są błędy."""
import collections, json, sys, unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets"
ALPHABET = set("AĄBCĆDEĘFGHIJKLŁMNŃOÓPQRSŚTUVWXYZŹŻ")
REQUIRED = ["id", "word", "displayWord", "category", "difficulty", "imageId", "source"]
BANNED = {"OSCAR", "JOHNSON", "BISHOP", "WASHINGTON", "JENNIFER", "PERCY", "MASON", "STANLEY", "TANNER", "DAVE", "LITTLE", "STAR", "EVA", "HAN", "BABY"}


def main():
    errors, warnings = [], []
    raw = (ASSETS / "words_database.json").read_bytes()
    try:
        data = json.loads(raw.decode("utf-8"))
    except Exception as e:  # noqa
        print("Plik nie jest poprawnym UTF-8/JSON:", e); sys.exit(1)

    seen_id, seen_word, seen_img = set(), set(), set()
    cats, levels, lengths = collections.Counter(), collections.Counter(), collections.defaultdict(list)
    ok = 0
    for i, r in enumerate(data):
        where = f"rekord {i} ({r.get('word', '?')})"
        miss = [k for k in REQUIRED if not str(r.get(k, "")).strip()]
        if miss:
            errors.append(f"{where}: brak pól {miss}"); continue
        w = r["word"]
        if unicodedata.normalize("NFC", w) != w:
            errors.append(f"{where}: tekst nie jest w formie NFC")
        if not set(w) <= ALPHABET:
            errors.append(f"{where}: znaki spoza alfabetu polskiego")
        if w in BANNED:
            errors.append(f"{where}: obce imię/słowo z listy zakazanych")
        for key, seen in (("id", seen_id), ("word", seen_word), ("imageId", seen_img)):
            if r[key] in seen:
                errors.append(f"{where}: duplikat {key}={r[key]}")
            seen.add(r[key])
        if r["difficulty"] not in (1, 2, 3, 4):
            errors.append(f"{where}: poziom {r['difficulty']}")
        svg = ASSETS / "illustrations" / f"{r['imageId']}.svg"
        if not svg.is_file() or "<svg" not in svg.read_text(encoding="utf-8", errors="replace"):
            errors.append(f"{where}: brak/uszkodzona ilustracja {svg.name}")
        cats[r["category"]] += 1
        levels[r["difficulty"]] += 1
        lengths[r["difficulty"]].append(len(w))
        ok += 1

    extra = ASSETS / "words_arasaac.json"
    if extra.is_file():
        for i, r in enumerate(json.loads(extra.read_text(encoding="utf-8"))):
            where = f"arasaac {i} ({r.get('word', '?')})"
            if not all(str(r.get(k, "")).strip() for k in REQUIRED):
                errors.append(f"{where}: brak pól"); continue
            if not set(r["word"]) <= ALPHABET: errors.append(f"{where}: znaki spoza alfabetu")
            if r["word"] in seen_word: errors.append(f"{where}: duplikat słowa"); continue
            seen_word.add(r["word"])
            if not (ASSETS / "illustrations_arasaac" / f"{r['imageId']}.webp").is_file():
                errors.append(f"{where}: brak obrazka")
            cats[r["category"]] += 1; levels[r["difficulty"]] += 1; lengths[r["difficulty"]].append(len(r["word"])); ok += 1
    files = {p.stem for p in (ASSETS / "illustrations").glob("*.svg")}
    orphans = sorted(files - seen_img)
    if orphans:
        warnings.append(f"{len(orphans)} plików SVG bez słowa: {orphans[:5]}…")
    for c, n in cats.items():
        if n < 3:
            warnings.append(f"kategoria „{c}” ma tylko {n} słów")

    lines = [
        "# Raport walidacji bazy słów", "",
        f"- Rekordów w pliku: **{len(data)}**",
        f"- Rekordów poprawnych: **{ok}**",
        f"- Unikalnych słów: **{len(seen_word)}**, unikalnych ilustracji: **{len(seen_img)}**",
        f"- Błędów: **{len(errors)}**, ostrzeżeń: **{len(warnings)}**", "",
        "## Poziomy trudności", "", "| Poziom | Słów | Średnia długość |", "|---|---|---|",
    ]
    for l in sorted(levels):
        lines.append(f"| {l} | {levels[l]} | {sum(lengths[l]) / len(lengths[l]):.1f} |")
    lines += ["", "## Kategorie (kategoria główna)", "", "| Kategoria | Słów |", "|---|---|"]
    lines += [f"| {c} | {n} |" for c, n in sorted(cats.items(), key=lambda t: -t[1])]
    lines += ["", "## Błędy", ""] + ([f"- {e}" for e in errors] or ["brak"])
    lines += ["", "## Ostrzeżenia", ""] + ([f"- {w}" for w in warnings] or ["brak"])
    lines += [
        "", "## Zakres weryfikacji", "",
        "Automatycznie sprawdzane: komplet pól, unikalność identyfikatorów/słów/ilustracji, alfabet polski, forma NFC,",
        "poziomy 1–4, istnienie i format plików SVG, brak ilustracji-sierot, lista zakazanych obcych imion.", "",
        "**Niesprawdzane automatycznie:** czy obrazek rzeczywiście pasuje do słowa. Dobór słowo → ilustracja jest ręczny",
        "(plik `tools/vocab/words.tsv`, jeden kod Twemoji na słowo) i wymaga przeglądu człowieka.",
    ]
    (ROOT / "docs").mkdir(exist_ok=True)
    (ROOT / "docs/VALIDATION_REPORT.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"rekordów: {len(data)}, poprawnych: {ok}, błędów: {len(errors)}, ostrzeżeń: {len(warnings)}")
    for e in errors[:20]:
        print(" -", e)
    sys.exit(1 if errors else 0)


main()
