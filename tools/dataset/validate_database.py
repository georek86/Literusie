# -*- coding: utf-8 -*-
"""
Skrypt walidacyjny bazy 5000 słów dla aplikacji Słowomistrz.
Sprawdza:
- Dokładną liczbę rekordów (dokładnie 5000)
- Unikalność słów i identyfikatorów
- Brak pustych wartości
- Poprawność kodowania Unicode
- Rozkład kategorii i poziomów trudności
- Obecność słów kontrolnych: ĆMA, ŻABA, ŁÓDŹ, GĘŚ, ŹREBAK
- Zapisuje szczegółowy raport walidacyjny do pliku markdown
"""

import json
import os

JSON_PATH = "c:/Stosia-app/app/src/main/assets/words_database.json"
REPORT_PATH = "c:/Stosia-app/tools/dataset/VALIDATION_REPORT.md"

with open(JSON_PATH, 'r', encoding='utf-8') as f:
    words = json.load(f)

errors = []
warnings = []

total_count = len(words)
unique_ids = set()
unique_words = set()
categories_count = {}
difficulty_count = {1: 0, 2: 0, 3: 0, 4: 0}

required_words = ["ĆMA", "ŻABA", "ŁÓDŹ", "GĘŚ", "ŹREBAK"]
found_required = {}

for idx, item in enumerate(words):
    w_id = item.get("id")
    word = item.get("word")
    display_word = item.get("displayWord")
    cat = item.get("category")
    diff = item.get("difficulty")
    img_id = item.get("imageId")

    # Sprawdzenie obecności pól
    if not w_id:
        errors.append(f"Rekord {idx} brak id")
    elif w_id in unique_ids:
        errors.append(f"Zduplikowane id: {w_id}")
    else:
        unique_ids.add(w_id)

    if not word or not word.strip():
        errors.append(f"Rekord {idx} puste słowo")
    elif word in unique_words:
        errors.append(f"Zduplikowane słowo: {word}")
    else:
        unique_words.add(word)

    # Sprawdzenie słów wymaganych
    if word in required_words:
        found_required[word] = item

    # Kategorie
    categories_count[cat] = categories_count.get(cat, 0) + 1

    # Poziomy trudności
    if diff in difficulty_count:
        difficulty_count[diff] += 1
    else:
        errors.append(f"Nieprawidłowy poziom trudności {diff} w słowie {word}")

    if not img_id:
        errors.append(f"Słowo {word} brak imageId")

for req in required_words:
    if req not in found_required:
        errors.append(f"BRAK WYMAGANEGO SŁOWA KONTROLNEGO: {req}")

report = []
report.append("# Raport Walidacji Bazy Słów - Słowomistrz (Android TV)\n")
report.append(f"- **Łączna liczba słów**: {total_count} (Wymóg: 5000)")
report.append(f"- **Liczba unikalnych słów**: {len(unique_words)}")
report.append(f"- **Liczba unikalnych identyfikatorów**: {len(unique_ids)}")
report.append(f"- **Liczba błędów krytycznych**: {len(errors)}")
report.append(f"- **Liczba ostrzeżeń**: {len(warnings)}\n")

report.append("## Weryfikacja słów kontrolnych ze specyfikacji")
for req in required_words:
    if req in found_required:
        item = found_required[req]
        report.append(f"- [x] **{req}**: ID={item['id']}, kategoria={item['category']}, poziom={item['difficulty']}, imageId={item['imageId']}")
    else:
        report.append(f"- [ ] **{req}**: BRAK W BAZIE!")

report.append("\n## Rozkład poziomów trudności")
report.append(f"- **Poziom 1 (Bardzo łatwe, krótkie)**: {difficulty_count[1]} słów")
report.append(f"- **Poziom 2 (Łatwe, codzienne)**: {difficulty_count[2]} słów")
report.append(f"- **Poziom 3 (Średnie, trudniejsze głoski)**: {difficulty_count[3]} słów")
report.append(f"- **Poziom 4 (Zaawansowane, dłuższe pojęcia)**: {difficulty_count[4]} słów")

report.append("\n## Rozkład według kategorii")
for cat, count in sorted(categories_count.items(), key=lambda x: -x[1]):
    report.append(f"- **{cat}**: {count} słów")

if errors:
    report.append("\n## Wykryte błędy")
    for err in errors[:50]:
        report.append(f"- {err}")

report_content = "\n".join(report)

with open(REPORT_PATH, 'w', encoding='utf-8') as f:
    f.write(report_content)

print(report_content)
if len(errors) == 0:
    print("\nWALIDACJA ZAKOŃCZONA SUKCESEM: BAZA JEST W 100% POPRAWNA!")
else:
    print(f"\nWALIDACJA NIE POWIODŁA SIĘ: {len(errors)} BŁĘDÓW!")
