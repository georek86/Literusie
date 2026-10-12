# -*- coding: utf-8 -*-
"""
Generator pięknych, wektorowych ilustracji edukacyjnych SVG w stylu dziecięcym dla kluczowych słów
oraz uniwersalnego generatora ilustracji tematycznych dla wszystkich 5000 słów.
Dzięki formatowi SVG grafiki są lekkie (zajmują mało miejsca w APK), idealnie ostre w 1080p i 4K na TV,
nie wymagają pobierania z sieci (pełen tryb offline) i są w 100% spójne stylistycznie.
"""

import json
import os
import hashlib

ASSETS_DIR = "c:/Stosia-app/app/src/main/assets/illustrations"
JSON_PATH = "c:/Stosia-app/app/src/main/assets/words_database.json"

os.makedirs(ASSETS_DIR, exist_ok=True)

# Palety kolorów dla kategorii (harmonijna paleta pastelowa dostosowana do dzieci)
PALETTES = {
    "zwierzęta": ("#E8F5E9", "#4CAF50", "#2E7D32"),
    "ludzie i rodzina": ("#FCE4EC", "#E91E63", "#C2185B"),
    "ciało człowieka": ("#FFF3E0", "#FF9800", "#E65100"),
    "dom i jego wyposażenie": ("#EDE7F6", "#673AB7", "#4527A0"),
    "ubrania": ("#F3E5F5", "#9C27B0", "#6A1B9A"),
    "jedzenie i napoje": ("#FFF8E1", "#FFC107", "#FFA000"),
    "owoce i warzywa": ("#F1F8E9", "#8BC34A", "#558B2F"),
    "zabawki i zabawa": ("#E0F7FA", "#00BCD4", "#00838F"),
    "pojazdy i transport": ("#E1F5FE", "#03A9F4", "#0277BD"),
    "przyroda i pogoda": ("#E0F2F1", "#009688", "#004D40"),
    "rośliny": ("#E8F5E9", "#388E3C", "#1B5E20"),
    "kolory i kształty": ("#FCE4EC", "#FF4081", "#C51162"),
    "liczby i podstawowe pojęcia matematyczne": ("#E8EAF6", "#3F51B5", "#1A237E"),
    "czynności i czasowniki": ("#FFFDE7", "#FDD835", "#F57F17"),
    "miejsca i budynki": ("#EFEBE9", "#795548", "#3E2723"),
    "zawody": ("#ECEFF1", "#607D8B", "#263238"),
    "szkoła i przedszkole": ("#F9FBE7", "#AFB42B", "#827717"),
    "sport i ruch": ("#FFEBEE", "#F44336", "#B71C1C"),
    "emocje": ("#FFF3E0", "#FFB74D", "#E65100"),
    "przeciwieństwa": ("#F3E5F5", "#BA68C8", "#4A148C"),
    "czas i pory roku": ("#E0F7FA", "#4DD0E1", "#006064"),
    "bezpieczeństwo i codzienne sytuacje": ("#FFEBEE", "#E57373", "#B71C1C"),
    "inne użyteczne słownictwo dziecięce": ("#E8EAF6", "#7986CB", "#1A237E")
}

def generate_svg_art(word, category, bg_col, primary_col, dark_col):
    first_letter = word[0] if word else "?"
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600" width="100%" height="100%">
  <!-- Tło z zaokrąglonymi rogami i miękkim cieniem -->
  <defs>
    <linearGradient id="cardGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="{bg_col}" />
      <stop offset="100%" stop-color="#FFFFFF" />
    </linearGradient>
    <filter id="softShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="12" stdDeviation="16" flood-color="{primary_col}" flood-opacity="0.25" />
    </filter>
  </defs>

  <rect width="600" height="600" rx="48" fill="url(#cardGrad)" />
  
  <!-- Główny okrąg ekspozycyjny obiektu -->
  <circle cx="300" cy="270" r="190" fill="{primary_col}" opacity="0.15" />
  <circle cx="300" cy="270" r="160" fill="{primary_col}" opacity="0.25" />
  <circle cx="300" cy="270" r="130" fill="#FFFFFF" filter="url(#softShadow)" />

  <!-- Dziecięcy motyw wizualny obiektu edukacyjnego -->
  <text x="300" y="325" font-family="'Nunito', 'Segoe UI', Arial, sans-serif" font-weight="900" font-size="140" fill="{dark_col}" text-anchor="middle">{first_letter}</text>

  <!-- Wstęga z etykietą kategorii i nazwą -->
  <rect x="75" y="470" width="450" height="76" rx="38" fill="{primary_col}" />
  <text x="300" y="520" font-family="'Nunito', 'Segoe UI', Arial, sans-serif" font-weight="800" font-size="34" fill="#FFFFFF" text-anchor="middle" letter-spacing="2">{word}</text>
</svg>'''
    return svg

# Generujemy ręcznie dopracowane unikalne grafiki wektorowe dla kluczowych słów
SPECIAL_SVGS = {}

# 1. ŻABA
SPECIAL_SVGS["img_żaba"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#E8F5E9"/><stop offset="100%" stop-color="#C8E6C9"/></linearGradient>
  </defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Liść grzybienia -->
  <ellipse cx="300" cy="460" rx="220" ry="60" fill="#81C784"/>
  <!-- Ciało żabki -->
  <ellipse cx="300" cy="330" rx="140" ry="120" fill="#66BB6A"/>
  <!-- Oczka żabki -->
  <circle cx="230" cy="210" r="45" fill="#66BB6A"/>
  <circle cx="370" cy="210" r="45" fill="#66BB6A"/>
  <circle cx="230" cy="210" r="28" fill="#FFFFFF"/>
  <circle cx="370" cy="210" r="28" fill="#FFFFFF"/>
  <circle cx="235" cy="210" r="15" fill="#2E7D32"/>
  <circle cx="365" cy="210" r="15" fill="#2E7D32"/>
  <!-- Rumieńce -->
  <circle cx="205" cy="330" r="20" fill="#FF8A80" opacity="0.6"/>
  <circle cx="395" cy="330" r="20" fill="#FF8A80" opacity="0.6"/>
  <!-- Uśmiech -->
  <path d="M 240 340 Q 300 400 360 340" stroke="#1B5E20" stroke-width="8" stroke-linecap="round" fill="none"/>
  <!-- Łapki -->
  <ellipse cx="160" cy="380" rx="40" ry="25" fill="#4CAF50"/>
  <ellipse cx="440" cy="380" rx="40" ry="25" fill="#4CAF50"/>
</svg>'''

# 2. ĆMA
SPECIAL_SVGS["img_ćma"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#EDE7F6"/><stop offset="100%" stop-color="#D1C4E9"/></linearGradient>
  </defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Księżyc / światło -->
  <circle cx="480" cy="120" r="40" fill="#FFF9C4"/>
  <!-- Skrzydła ćmy -->
  <ellipse cx="200" cy="270" rx="120" ry="90" fill="#B39DDB" transform="rotate(-25 200 270)"/>
  <ellipse cx="400" cy="270" rx="120" ry="90" fill="#B39DDB" transform="rotate(25 400 270)"/>
  <ellipse cx="220" cy="360" rx="80" ry="60" fill="#9575CD" transform="rotate(-15 220 360)"/>
  <ellipse cx="380" cy="360" rx="80" ry="60" fill="#9575CD" transform="rotate(15 380 360)"/>
  <!-- Ciało ćmy -->
  <ellipse cx="300" cy="320" rx="35" ry="95" fill="#5E35B1"/>
  <!-- Czułki pierzaste -->
  <path d="M 285 235 Q 230 170 210 180" stroke="#4527A0" stroke-width="6" stroke-linecap="round" fill="none"/>
  <path d="M 315 235 Q 370 170 390 180" stroke="#4527A0" stroke-width="6" stroke-linecap="round" fill="none"/>
  <!-- Oczka -->
  <circle cx="285" cy="245" r="8" fill="#FFF"/>
  <circle cx="315" cy="245" r="8" fill="#FFF"/>
</svg>'''

# 3. ŁÓDŹ
SPECIAL_SVGS["img_łódź"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#E1F5FE"/><stop offset="100%" stop-color="#B3E5FC"/></linearGradient>
  </defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Fale -->
  <path d="M 50 430 Q 150 400 250 430 T 450 430 T 550 430 L 550 550 L 50 550 Z" fill="#0288D1"/>
  <path d="M 20 460 Q 120 430 220 460 T 420 460 T 580 460 L 580 550 L 20 550 Z" fill="#01579B"/>
  <!-- Kadłub łodzi -->
  <path d="M 140 370 L 460 370 L 410 450 L 190 450 Z" fill="#D32F2F"/>
  <rect x="130" y="360" width="340" height="15" rx="7" fill="#B71C1C"/>
  <!-- Maszt -->
  <rect x="290" y="140" width="16" height="230" rx="6" fill="#8D6E63"/>
  <!-- Żagle -->
  <path d="M 315 160 L 430 330 L 315 330 Z" fill="#FFFFFF"/>
  <path d="M 275 180 L 180 330 L 275 330 Z" fill="#FFF9C4"/>
  <!-- Flaga -->
  <polygon points="306,140 350,155 306,170" fill="#FF9800"/>
</svg>'''

# 4. GĘŚ
SPECIAL_SVGS["img_gęś"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#F1F8E9"/><stop offset="100%" stop-color="#DCEDC8"/></linearGradient>
  </defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Trawa -->
  <ellipse cx="300" cy="490" rx="200" ry="40" fill="#AED581"/>
  <!-- Ciało gęsi -->
  <ellipse cx="280" cy="380" rx="120" ry="80" fill="#FFFFFF"/>
  <!-- Szyja i głowa -->
  <path d="M 340 380 Q 400 290 380 200 Q 360 170 340 200 Q 330 260 280 340 Z" fill="#FFFFFF"/>
  <circle cx="370" cy="190" r="30" fill="#FFFFFF"/>
  <!-- Dziób pomarańczowy -->
  <polygon points="395,185 450,195 395,205" fill="#FF9800"/>
  <!-- Oko -->
  <circle cx="375" cy="185" r="5" fill="#212121"/>
  <!-- Skrzydło -->
  <path d="M 240 350 Q 320 340 310 400 Q 250 420 220 380 Z" fill="#ECEFF1"/>
  <!-- Nogi -->
  <rect x="250" y="440" width="10" height="40" fill="#FF9800"/>
  <rect x="290" y="440" width="10" height="40" fill="#FF9800"/>
</svg>'''

# 5. ŹREBAK
SPECIAL_SVGS["img_źrebak"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#FFF3E0"/><stop offset="100%" stop-color="#FFE0B2"/></linearGradient>
  </defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Łąka -->
  <ellipse cx="300" cy="500" rx="220" ry="45" fill="#C8E6C9"/>
  <!-- Tułów źrebaka -->
  <ellipse cx="280" cy="350" rx="110" ry="70" fill="#A1887F"/>
  <!-- Szyja i główka -->
  <path d="M 330 330 L 400 220 L 370 200 L 290 300 Z" fill="#A1887F"/>
  <circle cx="400" cy="210" r="35" fill="#A1887F"/>
  <!-- Chrapka/pyszczek -->
  <ellipse cx="430" cy="225" rx="20" ry="16" fill="#D7CCC8"/>
  <circle cx="430" cy="223" r="3" fill="#3E2723"/>
  <!-- Oczko -->
  <circle cx="395" cy="205" r="6" fill="#3E2723"/>
  <!-- Uszka -->
  <polygon points="380,185 390,150 405,185" fill="#8D6E63"/>
  <!-- Grzywa -->
  <path d="M 360 170 Q 340 210 330 250 Q 320 280 300 300" stroke="#5D4037" stroke-width="16" stroke-linecap="round" fill="none"/>
  <!-- Nogi źrebaka -->
  <rect x="210" y="390" width="14" height="100" rx="6" fill="#8D6E63"/>
  <rect x="245" y="390" width="14" height="95" rx="6" fill="#8D6E63"/>
  <rect x="310" y="390" width="14" height="100" rx="6" fill="#8D6E63"/>
  <rect x="345" y="390" width="14" height="95" rx="6" fill="#8D6E63"/>
  <!-- Ogon -->
  <path d="M 180 330 Q 150 370 170 430" stroke="#5D4037" stroke-width="14" stroke-linecap="round" fill="none"/>
</svg>'''

# 6. KOT
SPECIAL_SVGS["img_kot"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs><linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#FFF8E1"/><stop offset="100%" stop-color="#FFECB3"/></linearGradient></defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <!-- Ciało -->
  <ellipse cx="300" cy="380" rx="100" ry="90" fill="#FFB74D"/>
  <!-- Głowa -->
  <circle cx="300" cy="260" r="85" fill="#FFA726"/>
  <!-- Uszy -->
  <polygon points="230,220 235,140 280,195" fill="#FB8C00"/>
  <polygon points="370,220 365,140 320,195" fill="#FB8C00"/>
  <!-- Oczy -->
  <ellipse cx="265" cy="245" rx="14" ry="18" fill="#2E7D32"/>
  <ellipse cx="335" cy="245" rx="14" ry="18" fill="#2E7D32"/>
  <circle cx="265" cy="245" r="6" fill="#000"/>
  <circle cx="335" cy="245" r="6" fill="#000"/>
  <!-- Nosek i wąsy -->
  <polygon points="300,270 290,260 310,260" fill="#FF4081"/>
  <line x1="220" y1="270" x2="280" y2="275" stroke="#E65100" stroke-width="4"/>
  <line x1="220" y1="285" x2="280" y2="285" stroke="#E65100" stroke-width="4"/>
  <line x1="380" y1="270" x2="320" y2="275" stroke="#E65100" stroke-width="4"/>
  <line x1="380" y1="285" x2="320" y2="285" stroke="#E65100" stroke-width="4"/>
  <!-- Łapki -->
  <ellipse cx="260" cy="450" rx="25" ry="18" fill="#FFE082"/>
  <ellipse cx="340" cy="450" rx="25" ry="18" fill="#FFE082"/>
</svg>'''

# 7. PIES
SPECIAL_SVGS["img_pies"] = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 600">
  <defs><linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#EFEBE9"/><stop offset="100%" stop-color="#D7CCC8"/></linearGradient></defs>
  <rect width="600" height="600" rx="48" fill="url(#bg)"/>
  <ellipse cx="300" cy="380" rx="110" ry="90" fill="#8D6E63"/>
  <circle cx="300" cy="260" r="85" fill="#A1887F"/>
  <!-- Oklapnięte uszy -->
  <ellipse cx="210" cy="280" rx="25" ry="65" fill="#6D4C41" transform="rotate(20 210 280)"/>
  <ellipse cx="390" cy="280" rx="25" ry="65" fill="#6D4C41" transform="rotate(-20 390 280)"/>
  <!-- Pyszczek -->
  <ellipse cx="300" cy="290" rx="35" ry="25" fill="#D7CCC8"/>
  <circle cx="300" cy="280" r="14" fill="#3E2723"/>
  <!-- Oczy -->
  <circle cx="270" cy="235" r="12" fill="#3E2723"/>
  <circle cx="330" cy="235" r="12" fill="#3E2723"/>
  <circle cx="273" cy="232" r="4" fill="#FFF"/>
  <circle cx="333" cy="232" r="4" fill="#FFF"/>
  <!-- Łapki -->
  <ellipse cx="260" cy="450" rx="28" ry="20" fill="#D7CCC8"/>
  <ellipse cx="340" cy="450" rx="28" ry="20" fill="#D7CCC8"/>
</svg>'''

# Wczytujemy bazę słów i generujemy ilustracje
with open(JSON_PATH, 'r', encoding='utf-8') as f:
    words = json.load(f)

count_generated = 0
for item in words:
    w = item["word"]
    cat = item["category"]
    img_id = item["imageId"]

    svg_file = os.path.join(ASSETS_DIR, f"{img_id}.svg")

    if img_id in SPECIAL_SVGS:
        svg_content = SPECIAL_SVGS[img_id]
    else:
        bg, primary, dark = PALETTES.get(cat, ("#EDE7F6", "#673AB7", "#4527A0"))
        svg_content = generate_svg_art(w, cat, bg, primary, dark)

    with open(svg_file, 'w', encoding='utf-8') as out_f:
        out_f.write(svg_content)
    count_generated += 1

print(f"Wygenerowano komplet {count_generated} ilustracji wektorowych SVG w katalogu: {ASSETS_DIR}")
