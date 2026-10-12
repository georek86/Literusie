# -*- coding: utf-8 -*-
"""
Generator i weryfikator bazy 5000 polskich słów dla aplikacji Słowomistrz (Android TV).
Baza podzielona na 23 kategorie edukacyjne i 4 poziomy trudności dostosowane do rozwoju 5-latka.
"""

import json
import os
import re
import unicodedata

# 23 Kategorie edukacyjne
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

print("Kategorie zdefiniowane:", len(CATEGORIES))
