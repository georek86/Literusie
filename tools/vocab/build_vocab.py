#!/usr/bin/env python3
"""Buduje bazę słów Literusie z ręcznie sprawdzonego pliku words.tsv.

Użycie: build_vocab.py <katalog z SVG Twemoji (pakiet npm @twemoji/svg)>
Wynik: app/src/main/assets/words_database.json + illustrations/*.svg
Każde słowo ma własną, unikalną ilustrację. Pozycje bez pliku ilustracji są odrzucane i raportowane.
"""
import json, re, shutil, sys, unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets"
CATS = {
    "Z": "zwierzęta", "L": "ludzie i rodzina", "C": "ciało człowieka", "D": "dom i jego wyposażenie",
    "U": "ubrania", "J": "jedzenie i napoje", "O": "owoce i warzywa", "T": "zabawki i zabawa",
    "P": "pojazdy i transport", "N": "przyroda i pogoda", "R": "rośliny", "K": "kolory i kształty",
    "M": "liczby i matematyka", "A": "czynności", "B": "miejsca i budynki", "W": "zawody",
    "S": "szkoła i przedszkole", "X": "sport i ruch", "E": "emocje", "V": "przeciwieństwa",
    "I": "czas i pory roku", "Y": "bezpieczeństwo", "Q": "inne słowa",
}
SRC = "Etykieta: Emojibase PL (MIT); ilustracja: Twemoji (CC-BY 4.0)"


def level(word: str) -> int:
    n = len(word)
    return 1 if n <= 4 else 2 if n <= 6 else 3 if n <= 8 else 4


def twemoji_file(src: Path, hexcode: str):
    parts = [p.lstrip("0") or "0" for p in hexcode.split("-") if p != "fe0f"]
    base = "-".join(parts)
    for cand in (base, hexcode, base + "-fe0f", base.replace("-200d-", "-200d-") + "-fe0f"):
        f = src / f"{cand}.svg"
        if f.exists():
            return f
    return None


def main():
    src = Path(sys.argv[1])
    words, missing, seen_w, seen_h = [], [], set(), set()
    for ln, line in enumerate((ROOT / "tools/vocab/words.tsv").read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip() or line.startswith("#"):
            continue
        f = line.split("|")
        w = unicodedata.normalize("NFC", f[0].strip().lower())
        hx = f[1].strip().lower()
        cats = f[2].strip().split("+")
        desc = f[3].strip() if len(f) > 3 else None
        if w in seen_w or hx in seen_h:
            sys.exit(f"duplikat w linii {ln}: {w}/{hx}")
        svg = twemoji_file(src, hx)
        if svg is None:
            missing.append(w)
            continue
        seen_w.add(w); seen_h.add(hx)
        words.append((w, svg, hx, cats, desc))
    words.sort(key=lambda t: (level(t[0]), t[0]))
    out_dir = ASSETS / "illustrations"
    shutil.rmtree(out_dir, ignore_errors=True)
    out_dir.mkdir(parents=True)
    db = []
    for i, (w, svg, hx, cats, desc) in enumerate(words, 1):
        img = "e_" + hx.replace("-", "_")
        shutil.copyfile(svg, out_dir / f"{img}.svg")
        rec = {"id": f"w_{i:04d}", "word": w.upper(), "displayWord": w.upper(), "category": CATS[cats[0]],
               "difficulty": level(w), "imageId": img, "source": SRC,
               "imageLicense": "CC-BY 4.0", "imageAuthor": "Twemoji (Twitter, Inc. i współtwórcy)"}
        if len(cats) > 1:
            rec["secondaryCategory"] = CATS[cats[1]]
        if desc:
            rec["description"] = desc
        db.append(rec)
    (ASSETS / "words_database.json").write_text(json.dumps(db, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"zapisano {len(db)} słów; odrzucono (brak ilustracji): {missing}")


main()
