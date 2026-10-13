#!/usr/bin/env python3
"""Buduje pakiety słówek z piktogramów ARASAAC do wydania na GitHub Releases.

UWAGA: skrypt NIE był uruchomiony w środowisku, w którym powstał (brak dostępu do arasaac.org) –
zbudowano go wg dokumentacji API. Uruchom najpierw z --dry-run i obejrzyj raport.

Użycie:
  python3 tools/arasaac/build_packs.py --dry-run
  python3 tools/arasaac/build_packs.py --out dist/packs --tag packs-v1 --target 5000
Potem:
  gh release create packs-v1 dist/packs/* --title "Pakiety słówek" --notes "ARASAAC, CC BY-NC-SA 4.0" --latest

Licencja ARASAAC: CC BY-NC-SA 4.0 (Gobierno de Aragón, autor: Sergio Palao) – tylko użytek niekomercyjny, z uznaniem
autorstwa i na tej samej licencji. Pakiety muszą pozostać niekomercyjne.
"""
import argparse, collections, hashlib, io, json, re, sys, time, unicodedata, urllib.request, zipfile
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = "https://api.arasaac.org/v1/pictograms/all/pl"
IMG = "https://static.arasaac.org/pictograms/{id}/{id}_500.png"
REPO = "georek86/Literusie"
ALPHABET = set("aąbcćdeęfghijklłmnńoóprsśtuwyzźż")  # bez q, v, x – w polskich słowach dziecięcych praktycznie nie występują
SOURCE = "ARASAAC (arasaac.org), Gobierno de Aragón, autor: Sergio Palao"
PACK_SIZE = 500

# Kategorie ARASAAC -> nasze. Uzupełnij po obejrzeniu raportu --dry-run (sekcja „niezmapowane kategorie”).
CATEGORY_MAP = {
    "animal": "zwierzęta", "mammal": "zwierzęta", "bird": "zwierzęta", "insect": "zwierzęta", "fish": "zwierzęta",
    "food": "jedzenie i napoje", "drink": "jedzenie i napoje", "fruit": "owoce i warzywa", "vegetable": "owoce i warzywa",
    "family": "ludzie i rodzina", "people": "ludzie i rodzina", "body": "ciało człowieka",
    "furniture": "dom i jego wyposażenie", "house": "dom i jego wyposażenie", "kitchen": "dom i jego wyposażenie",
    "clothes": "ubrania", "clothing": "ubrania", "toy": "zabawki i zabawa", "game": "zabawki i zabawa",
    "transport": "pojazdy i transport", "vehicle": "pojazdy i transport", "weather": "przyroda i pogoda",
    "nature": "przyroda i pogoda", "plant": "rośliny", "colour": "kolory i kształty", "color": "kolory i kształty",
    "shape": "kolory i kształty", "number": "liczby i matematyka", "verb": "czynności", "action": "czynności",
    "building": "miejsca i budynki", "place": "miejsca i budynki", "occupation": "zawody", "profession": "zawody",
    "school": "szkoła i przedszkole", "sport": "sport i ruch", "emotion": "emocje", "feeling": "emocje",
    "time": "czas i pory roku", "season": "czas i pory roku", "safety": "bezpieczeństwo",
    "anatomy": "ciało człowieka", "physical features": "ciało człowieka", "gastronomy": "jedzenie i napoje",
    "cookery": "jedzenie i napoje", "baking": "jedzenie i napoje", "beverage": "jedzenie i napoje",
    "utensil": "dom i jego wyposażenie", "appliance": "dom i jego wyposażenie", "work tool": "dom i jego wyposażenie",
    "construction": "miejsca i budynki", "educational": "szkoła i przedszkole", "athlete": "sport i ruch",
    "accessories": "ubrania", "footwear": "ubrania", "seller": "zawody", "trade": "zawody", "agriculture": "zawody",
    "signaling": "bezpieczeństwo", "adjective": "przeciwieństwa", "basic concepts": "przeciwieństwa",
    "musical": "inne słowa", "instrument": "inne słowa",
}
# Słowa i kategorie nieodpowiednie dla małego dziecka.
BLOCK_CATEGORIES = {"christianity", "religion", "covid-19", "disease", "symptom", "disruptive behavior", "orofacial praxis",
                    "special education", "pronoun", "personal pronoun", "sex", "sexuality", "violence", "politics", "drug",
                    "alcohol", "tobacco", "weapon", "war", "death", "marital status"}
BLOCK_WORDS = {"seks", "alkohol", "papieros", "pistolet", "karabin", "wódka", "piwo", "wino", "narkotyk", "zabijać", "zabić", "śmierć"}


def level(word):
    n = len(word)
    return 1 if n <= 4 else 2 if n <= 6 else 3 if n <= 8 else 4


def get(url, tries=4):
    for i in range(tries):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "literusie-pack-builder"}), timeout=60) as r:
                return r.read()
        except Exception as e:  # noqa
            if i == tries - 1:
                raise
            time.sleep(2 ** i)


def map_category(cats):
    for c in cats:
        for key, ours in CATEGORY_MAP.items():
            if key in c.lower():
                return ours
    return None


def candidates(core_texts):
    data = json.loads(get(API))
    picked, unmapped, rejected = {}, collections.Counter(), collections.Counter()
    blocked_by = collections.Counter()
    for p in data:
        cats = [str(c) for c in p.get("categories", [])]
        hit = [c.lower() for c in cats if c.lower() in BLOCK_CATEGORIES]   # dokładne dopasowanie nazwy kategorii
        if p.get("violence") or p.get("sex") or hit:
            rejected["kategoria/flaga nieodpowiednia"] += 1
            blocked_by.update(hit or ["(flaga violence/sex)"]); continue
        kws = [k.get("keyword", "").strip() for k in p.get("keywords", []) if k.get("keyword")]
        if not kws:
            rejected["brak słowa"] += 1; continue
        # pierwsze słowo kluczowe, które jest pojedynczym, małą literą pisanym polskim słowem (nie nazwą własną)
        w, why = None, "nie jest pojedynczym polskim słowem"
        for kw in kws:
            if not kw or kw[0].isupper():
                why = "nazwa własna/wielka litera"; continue
            c = unicodedata.normalize("NFC", kw.lower())
            if not (2 <= len(c) <= 14) or not set(c) <= ALPHABET:
                continue
            if c.upper() in core_texts or c in BLOCK_WORDS:
                why = "duplikat bazy podstawowej/zablokowane"; continue
            w = c; break
        if w is None:
            rejected[why] += 1; continue
        cat = map_category(cats)
        if cat is None:
            unmapped.update(cats or ["(brak kategorii)"]); cat = "inne słowa"
        prev = picked.get(w)
        if prev is None or len(p.get("keywords", [])) < prev["nk"]:   # wybierz najbardziej jednoznaczny piktogram
            picked[w] = {"pid": p["_id"], "cat": cat, "nk": len(p.get("keywords", []))}
    print("powody blokady kategorii:", blocked_by.most_common(15))
    return picked, unmapped, rejected


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="dist/packs"); ap.add_argument("--tag", default="packs-v1")
    ap.add_argument("--target", type=int, default=5000, help="docelowa liczba słów łącznie z bazą podstawową")
    ap.add_argument("--dry-run", action="store_true")
    a = ap.parse_args()

    core = json.loads((ROOT / "app/src/main/assets/words_database.json").read_text(encoding="utf-8"))
    core_texts = {w["word"] for w in core}
    picked, unmapped, rejected = candidates(core_texts)
    need = max(0, a.target - len(core))
    # kolejność deterministyczna: najpierw krótsze, potem alfabetycznie
    words = sorted(picked, key=lambda w: (level(w), w))[:need]
    print(f"baza podstawowa: {len(core)}; kandydatów z ARASAAC: {len(picked)}; wybrano: {len(words)}; cel: {a.target}")
    print("odrzucone:", dict(rejected))
    print("niezmapowane kategorie (uzupełnij CATEGORY_MAP):", unmapped.most_common(40))
    if len(core) + len(words) < a.target:
        print(f"UWAGA: osiągnięto tylko {len(core) + len(words)} z {a.target} – nie deklaruj 5000.")
    if a.dry_run:
        return
    out = Path(a.out); out.mkdir(parents=True, exist_ok=True)
    try:
        from PIL import Image
    except ImportError:
        Image = None
    packs = []
    for n, start in enumerate(range(0, len(words), PACK_SIZE), 1):
        chunk = words[start:start + PACK_SIZE]
        pid = f"p{n:02d}"
        def fetch(w):
            try:
                raw = get(IMG.format(id=picked[w]["pid"]))
                if Image:
                    im = Image.open(io.BytesIO(raw)).convert("RGBA"); im.thumbnail((384, 384))
                    b = io.BytesIO(); im.save(b, "PNG", optimize=True); raw = b.getvalue()
                return w, raw
            except Exception as e:  # noqa
                print("brak obrazka:", w, e); return w, None
        with ThreadPoolExecutor(6) as ex:
            results = [r for r in ex.map(fetch, chunk) if r[1]]
        recs, buf = [], io.BytesIO()
        with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as z:
            for i, (w, raw) in enumerate(results, 1):
                img = f"a_{picked[w]['pid']}"
                z.writestr(f"img/{img}.png", raw)
                recs.append({"id": f"{pid}_{i:04d}", "word": w.upper(), "displayWord": w.upper(), "category": picked[w]["cat"],
                             "difficulty": level(w), "imageId": img, "source": SOURCE,
                             "imageLicense": "CC BY-NC-SA 4.0", "imageAuthor": "Sergio Palao / ARASAAC"})
            z.writestr("words.json", json.dumps(recs, ensure_ascii=False, indent=1))
        data = buf.getvalue()
        (out / f"{pid}.zip").write_bytes(data)
        packs.append({"id": pid, "version": 1, "url": f"https://github.com/{REPO}/releases/download/{a.tag}/{pid}.zip",
                      "sha256": hashlib.sha256(data).hexdigest(), "size": len(data), "wordCount": len(recs)})
        print(pid, len(recs), "słów,", len(data) // 1024, "KB")
    (out / "manifest.json").write_text(json.dumps({"packs": packs}, indent=1), encoding="utf-8")
    print("gotowe:", sum(p["wordCount"] for p in packs), "nowych słów w", len(packs), "pakietach ->", out)


main()
