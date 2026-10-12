# Literusie — Polska aplikacja edukacyjna dla dzieci na Android TV

Aplikacja edukacyjna na platformę **Android TV**, stworzona specjalnie dla 5-letnich dzieci, wspierająca naukę języka polskiego, czytania i pisania na fizycznej klawiaturze.

---

## 1. Wymagania systemowe i sprzętowe

- **Platforma**: Android TV / Google TV (minSdk 26 — Android 8.0 Oreo, targetSdk 34 — Android 14)
- **Ekran**: Zoptymalizowany dla 1920 × 1080 (Full HD) oraz 4K
- **Sterowanie**:
  - Fizyczna klawiatura USB lub Bluetooth (główny kanał wprowadzania liter)
  - Pilot telewizyjny D-Pad (strzałki, przycisk OK/Center, przycisk Back)
- **Dźwięk**: Systemowy Android TextToSpeech z zainstalowanym polskim głosem (np. Google TTS)
- **Pamięć i offline**: Aplikacja działa w 100% offline, bez konieczności połączenia z Internetem, bez konta i bez reklam

---

## 2. Najważniejsze funkcje i architektura

1. **Baza 5000 unikalnych, zweryfikowanych polskich słów**:
   - Podział na 23 kategorie edukacyjne (m.in. zwierzęta, dom, rodzina, owoce i warzywa, pojazdy, czynności itp.)
   - 4 stopnie trudności: od krótkich 3-literowych słówek po dłuższe pojęcia
   - Zawiera obowiązkowe słowa kontrolne: **ĆMA**, **ŻABA**, **ŁÓDŹ**, **GĘŚ**, **ŹREBAK**
   - 100% poprawności ortograficznej i zgodności ze słownikiem języka polskiego (SJP)
2. **Klawiatura fizyczna i tolerancja polskich znaków**:
   - Dziecko może wpisywać słowa bez polskich znaków diakrytycznych:
     - `Ą` → `A`, `Ć` → `C`, `Ę` → `E`, `Ł` → `L`, `Ń` → `N`, `Ó` → `O`, `Ś` → `S`, `Ź`/`Ż` → `Z`
   - Np. dla słowa `ŻABA` dziecko może wpisać `Z` `A` `B` `A` lub `Ż` `A` `B` `A`
   - Niewrażliwość na wielkość liter (Caps Lock / małe litery)
   - Pełna obsługa klawisza `Backspace`
3. **Mowa i dźwięk (TextToSpeech)**:
   - Wyraźne, spokojne tempo odczytu słów (0.85x)
   - Przycisk ponownego odsłuchania na ekranie
   - Płynne zarządzanie kolejką mowy (brak nakładania się dźwięków)
4. **Ilustracje edukacyjne**:
   - Komplet 5000 wektorowych ilustracji SVG osadzonych lokalnie w aplikacji
   - Ostre w każdej rozdzielczości telewizora (1080p, 4K)
5. **Lokalna baza postępów (Room Database)**:
   - Licznik ukończonych i opanowanych słówek
   - Bezpieczeństwo i prywatność: brak wysyłania jakichkolwiek danych na zewnątrz

---

## 3. Struktura projektu

- `app/src/main/java/pl/slowomistrz/app/`
  - `domain/normalizer/PolishCharNormalizer.kt`: Normalizator znaków diakrytycznych
  - `domain/engine/TypingEngine.kt`: Silnik sekwencyjnego wprowadzania liter
  - `domain/engine/LearningSessionManager.kt`: Zarządzanie sesją nauki
  - `domain/model/`: Modele `WordItem`, `WordProgress`, `WordInputState`
  - `data/local/`: Baza Room `SlowomistrzDatabase`, encja `WordProgressEntity`, DAO `WordProgressDao`
  - `data/repository/WordRepository.kt`: Repozytorium 5000 słów
  - `media/PolishTtsManager.kt`: Zarządca Android TextToSpeech z obsługą języka polskiego
  - `ui/theme/`: Kolory, typografia przystosowana do TV 1080p i motyw ciemny
  - `ui/components/`: Komponenty `WordIllustration`, `WordLetterSlotsRow`, `TvInteractiveButton`
  - `ui/screens/`: Ekrany `MainMenuScreen`, `LearningScreen`, `CategorySelectionScreen`, `ProgressScreen`
  - `MainActivity.kt`: Główna aktywność obsługująca Android TV i nawigację
- `app/src/main/assets/`
  - `words_database.json`: Baza 5000 słów w formacie JSON
  - `illustrations/`: 5000 wektorowych grafik SVG

---

## 4. Instrukcja instalacji na telewizorze Android TV

### Krok 1: Włączenie opcji programisty na Android TV
1. Na telewizorze przejdź do: **Ustawienia** → **System** → **Informacje**.
2. Kliknij 7 razy pozycję **Kompilacja systemu operacyjnego Android TV**, aż pojawi się komunikat: *„Jesteś teraz programistą!”*.
3. Wróć do menu **Opcje programisty** i włącz **Debugowanie USB** oraz **Debugowanie sieciowe**.

### Krok 2: Instalacja przez ADB (z komputera w tej samej sieci Wi-Fi)
1. Połącz się z adresem IP telewizora:
   ```bash
   adb connect <ADRES_IP_TELEWIZORA>:5555
   ```
2. Zainstaluj plik APK:
   ```bash
   adb install -r C:\Stosia-app\app\build\outputs\apk\debug\app-debug.apk
   ```

### Alternatywna metoda instalacji (Pendrive USB):
1. Skopiuj plik `app-debug.apk` na pendrive sformatowany w systemie FAT32 lub exFAT.
2. Podłącz pendrive do gniazda USB w telewizorze.
3. Za pomocą dowolnego menedżera plików na Android TV (np. *File Commander* lub *AnExplorer*) zainstaluj plik APK.

---

## 5. Uruchamianie testów i weryfikacji

Aby uruchomić automatyczne testy jednostkowe:
```bash
.\gradlew.bat testDebugUnitTest
```

Aby uruchomić walidację bazy 5000 słów:
```bash
python tools/dataset/validate_database.py
```
