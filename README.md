# Literusie

Polska aplikacja edukacyjna dla dzieci (ok. 5 lat) na **Android TV**. Dziecko ogląda ilustrację, słyszy polskie
słowo i **wpisuje je literka po literce na fizycznej klawiaturze** podłączonej do telewizora.
Bez internetu, kont, reklam, zakupów i bez żadnych uprawnień.

## Stan projektu (uczciwie)

| Obszar | Stan |
|---|---|
| Baza słów | **604 słowa**, każde z własną ilustracją (nie 5000 – patrz niżej) |
| Logika nauki, normalizacja polskich znaków, zapis postępów | gotowe, **testy jednostkowe przechodzą** (JVM) |
| Interfejs (menu, kategorie, nauka, postępy, ustawienia) | gotowy; testy UI na JVM/Compose Desktop. **Łącznie 71 testów (jednostkowe + UI) przechodzi** (`cd tools/desktop-harness && gradle test`). Zrzuty: `docs/screens` |
| Budowa APK na Android TV | **nie zweryfikowana w tym środowisku** – brak dostępu do Android SDK / Google Maven. Wymaga uruchomienia u siebie (instrukcja niżej) |
| Test na prawdziwym telewizorze / klawiaturze / głosie TTS | **niewykonany** (brak sprzętu) |

### Dlaczego 604, a nie 5000 słów
Pierwotna baza „5000 słów” była nieużyteczna: pochodziła z listy częstości z napisów filmowych (imiona angielskie jak
OSCAR, JOHNSON, BISHOP, formy odmienione, spójniki „przy”, „jestem”), kategorie były przypisane przypadkowo, a „ilustracje”
były szablonem z jedną literą. Została **usunięta**. Nowa baza zawiera wyłącznie słowa, które dziecko zna i które da się
pokazać na obrazku. Rozszerzenie do kilku tysięcy wymaga nowych, spójnych ilustracji (patrz „Dalszy rozwój”).

## Struktura

```
app/                         moduł Android (Kotlin, Jetpack Compose, Room, Coil)
  src/main/java/pl/literusie/
    domain/                  czysta logika (bez Androida): text/ typing/ learning/ model/
    data/                    WordParser/WordRepository, room/ (postępy w SQLite)
    media/                   Speaker (TextToSpeech), efekty dźwiękowe, porty/atrapy
    ui/                      theme/, components/, screens/, app/LiterusieApp (nawigacja)
    ui/platform/WordImage    ładowanie SVG (Coil) – jedyny plik UI zależny od Androida
  src/main/assets/           words_database.json, illustrations/*.svg (604 pliki)
  src/test/                  testy jednostkowe (JVM)
tools/vocab/                 words.tsv (ręcznie dobrane słowa), build_vocab.py, validate_vocab.py
tools/desktop-harness/       testy UI i zrzuty ekranu na JVM (Compose Desktop) – bez Androida
docs/                        VALIDATION_REPORT.md, zrzuty ekranów, licencje
```

## Budowanie i instalacja na Android TV

Wymagania: JDK 17+, Android SDK 34 (Android Studio lub `sdkmanager "platforms;android-34" "build-tools;34.0.0"`),
dostęp do Google Maven.

```bash
echo "sdk.dir=/ścieżka/do/Android/sdk" > local.properties
gradle :app:testDebugUnitTest      # testy jednostkowe
gradle :app:assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
```

Instalacja na telewizorze (włącz „Opcje programisty” → „Debugowanie USB/sieciowe”):

```bash
adb connect <adres-IP-telewizora>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Aplikacja pojawi się w launcherze Android TV jako **Literusie**. Sterowanie: klawiatura USB/Bluetooth (litery,
Backspace, Esc = wyjście), pilot (strzałki, OK, Wstecz).

**Polski głos:** Ustawienia telewizora → Preferencje urządzenia → Zamiana tekstu na mowę → zainstaluj język polski
(np. „Usługi mowy Google”). Gdy głosu brakuje, aplikacja pokazuje dyskretną informację, a „Dla rodziców” ma przycisk skrótu.

## Testy

```bash
# Testy jednostkowe + testy UI (JVM, bez Androida) i zrzuty ekranów -> tools/desktop-harness/build/shots
cd tools/desktop-harness && gradle test

# Walidacja bazy słów + raport docs/VALIDATION_REPORT.md
python3 tools/vocab/validate_vocab.py
```

Testy Androida (`app/src/test`) są te same pliki, które uruchamia harness. **Testów instrumentalnych (na urządzeniu)
nie ma** – obsługa klawiatury sprzętowej, TTS i pilota wymaga ręcznego sprawdzenia na telewizorze (lista kontrolna
w `docs/MANUAL_TEST_CHECKLIST.md`).

## Przebudowa bazy słów

Słowa i przypisane do nich ilustracje (kody Twemoji) są w `tools/vocab/words.tsv`.

```bash
npm pack @twemoji/svg@15.0.0 && mkdir tw && tar xzf twemoji-svg-*.tgz -C tw
python3 tools/vocab/build_vocab.py tw/package      # generuje words_database.json i illustrations/
python3 tools/vocab/validate_vocab.py
```

## Prywatność i licencje

Brak uprawnień (także INTERNET), brak analityki, `allowBackup=false`. Postępy są tylko w lokalnej bazie na urządzeniu.

- Ilustracje: **Twemoji** © Twitter, Inc. i współtwórcy, licencja **CC-BY 4.0** (wymaga uznania autorstwa – jest w aplikacji
  na ekranie głównym i w „Dla rodziców”). Etykiety słów pomocniczo z Emojibase (MIT).
- Czcionka: Nunito (SIL OFL 1.1) – `docs/Nunito-OFL.txt`.

## Dodatkowe słówka (pakiety z ARASAAC, GitHub Releases)

Baza podstawowa (604 słowa) jest w aplikacji i działa bez internetu. Dalsze słówka można pobrać **tylko na żądanie
dorosłego**: *Postępy → Dla rodziców → Pobierz słówka*. Jedyne uprawnienie aplikacji to INTERNET, używane wyłącznie do tego.
Pobieranie: tylko https, limity rozmiaru, weryfikacja SHA-256 przed rozpakowaniem, biała lista plików (ochrona przed
zip-slip), błąd sieci nie psuje nauki; po instalacji aplikacja przeładowuje bazę. Słowa duplikujące bazę podstawową
albo bez obrazka są pomijane.

Budowa pakietów (uruchamiasz u siebie – to środowisko nie miało dostępu do arasaac.org, więc **skrypt nie był jeszcze
uruchomiony**):

```bash
python3 tools/arasaac/build_packs.py --dry-run        # raport: ile słów, jakie kategorie niezmapowane
python3 tools/arasaac/build_packs.py --out dist/packs --tag packs-v1 --target 5000
gh release create packs-v1 dist/packs/* --title "Pakiety słówek" --latest
```

Aplikacja czyta `https://github.com/georek86/Literusie/releases/latest/download/manifest.json`, więc wydanie z pakietami
musi być oznaczone jako *latest*. **Licencja ARASAAC (CC BY-NC-SA 4.0): wyłącznie użytek niekomercyjny**, z podaniem
autorstwa (jest w „Dla rodziców”) i tą samą licencją. Liczba 5000 będzie prawdziwa dopiero, gdy raport skryptu pokaże
co najmniej tyle słów; filtr (pojedyncze polskie słowa, bez nazw własnych, bez treści dla dorosłych) i mapowanie
kategorii trzeba obejrzeć po pierwszym uruchomieniu.

**Wynik raportu `--dry-run` (autor repozytorium, własny komputer):** baza podstawowa 604 + 4272 słów z ARASAAC = **4876 słów**
(cel 5000 nie został osiągnięty – pozostałe piktogramy mają nazwy wielowyrazowe, nazwy własne lub są odfiltrowane jako
nieodpowiednie dla małego dziecka). Baza jest więc opisywana jako „ok. 4900 słów”, nie 5000.

## Decyzja: słówka w APK (bez pobierania)

Słówka i obrazki ARASAAC wchodzą **do APK**: `python tools/arasaac/build_packs.py --bundle` zapisuje
`app/src/main/assets/words_arasaac.json` i `assets/illustrations_arasaac/*.webp` (320 px). Aplikacja nie ma wtedy
uprawnienia INTERNET i działa w pełni offline. Kod pobierania pakietów (`data/packs`) pozostał, ale jest wyłączony.
Uwaga licencyjna: ARASAAC to CC BY-NC-SA 4.0, więc APK z tymi obrazkami wolno rozpowszechniać wyłącznie niekomercyjnie.
