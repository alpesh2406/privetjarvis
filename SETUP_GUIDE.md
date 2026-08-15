# Private Jarvis — Setup Guide

Ye ek real Android app project hai. Voice se baat karo, Claude AI jawab de (bolke),
aur kuch basic phone actions (alarm set karna, WhatsApp message bhejna, call
karna, app kholna) khud kar sakta hai.

## Iska scope — clearly samjho

Ye JARVIS jaisa **voice assistant** hai, lekin movie wala full-control JARVIS
nahi (jo suit chalaye, sab kuch hack kare). Android security ki wajah se koi
bhi app deep system control nahi le sakta — ye sirf wahi actions kar sakta hai
jinke liye maine permission code likha hai (upar wale 4). Naya action chahiye
ho to bata dena, code add kar dunga.

## Step 1: Android Studio install karo

1. https://developer.android.com/studio se download karo (free, Windows/Mac/Linux sab pe).
2. Install karo, pehli baar khulne par "Standard" setup choose karo — khud SDK
   download kar lega (thoda time lagega, ~10-15 min).

## Step 2: API key daalo

1. https://console.anthropic.com par jaake ek API key banao (thoda credit load karna hoga).
2. Is project mein `app/src/main/java/com/ravan/jarvis/Config.kt` file kholo.
3. `PASTE_YOUR_ANTHROPIC_API_KEY_HERE` ki jagah apni asli key daal do.

## Step 3: Project kholo aur build karo

1. Android Studio kholo → "Open" → ye `PrivateJarvis` folder select karo.
2. Pehli baar khulte hi Gradle sync hoga (internet chahiye, thoda time lagega) —
   niche status bar mein progress dikhega, khatam hone do.
3. Upar toolbar mein ek green "Run" (▶) button hoga — apna phone USB se connect
   karo (Developer Options → USB Debugging on karna hoga phone mein), aur Run
   dabao. App seedha phone mein install ho jayegi.

**Ya, agar USB connect nahi karna:**
- Build menu → "Build Bundle(s) / APK(s)" → "Build APK(s)"
- Build khatam hone par "locate" link milega — wahi APK file hai
- Us APK ko phone mein transfer karo (WhatsApp/email/USB se) aur install karo
  (phone mein "unknown sources se install allow karo" ka prompt aayega, allow karna)

## Step 4: Phone permissions

Pehli baar app kholne par mic permission maangega — allow karna, warna sun nahi
payega. Call karne wala action use karoge to call permission bhi maangega.

## Kaise use karo

- App kholo, beech mein bada **mic button** hai, dabao aur bolo
- Jawab wapas aayega text mein aur bola bhi jayega (text-to-speech)
- Bol sakte ho: "Subah 7 baje alarm laga do", "Sanjana ko WhatsApp pe hi bol
  do 'aa rahi hu'" (number saath dena padega), "Ramesh ko call karo"

## Aage kya customize kar sakte ho

- `MainActivity.kt` mein `SYSTEM_PROMPT` — Jarvis ka personality/behaviour yahan hai
- Naye actions add karne ho (jaise reminders, calendar events) — batana, code
  add kar dunga
- Language: abhi Hindi/Hinglish ke liye set hai (`hi-IN`), English chahiye to
  `MainActivity.kt` mein `EXTRA_LANGUAGE` "en-IN" kar dena

## Cost

- Anthropic API: per-conversation bahut chhota cost (paise mein), aapke usage
  ke hisaab se console.anthropic.com par billing dikhegi
- App khud free hai, koi subscription nahi
