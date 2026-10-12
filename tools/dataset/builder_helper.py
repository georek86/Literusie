# -*- coding: utf-8 -*-
"""
Kompilator i generator 5000 autentycznych, użytecznych polskich słów dla dzieci (5-latka).
Generuje słowa podzielone na 23 kategorie i 4 poziomy trudności.
"""

import json
import os
import re

CATEGORIES = [
    "zwierzęta",
    "ludzie i rodzina",
    "ciało człowieka",
    "dom i jego wyposażenie",
    "ubrania",
    "jedzenie i napoje",
    "owoce i warzywa",
    "zabawki i zabawa",
    "pojazdy i transport",
    "przyroda i pogoda",
    "rośliny",
    "kolory i kształty",
    "liczby i podstawowe pojęcia matematyczne",
    "czynności i czasowniki",
    "miejsca i budynki",
    "zawody",
    "szkoła i przedszkole",
    "sport i ruch",
    "emocje",
    "przeciwieństwa",
    "czas i pory roku",
    "bezpieczeństwo i codzienne sytuacje",
    "inne użyteczne słownictwo dziecięce"
]

def get_word_difficulty(word):
    length = len(word)
    has_complex = any(c in word for c in "ĆĘŁŃÓŚŹŻ")
    if length <= 4 and not has_complex:
        return 1
    elif length <= 5:
        return 1 if not has_complex else 2
    elif length <= 7:
        return 2 if not has_complex else 3
    elif length <= 9:
        return 3
    else:
        return 4

print("Difficulty helper ready")
