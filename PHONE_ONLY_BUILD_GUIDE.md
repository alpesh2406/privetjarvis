# Private Jarvis — Build karo SIRF PHONE se (koi computer nahi chahiye)

Ye guide poori tarah phone se karne ke liye hai. Code GitHub par jaayega,
aur GitHub ka free cloud service (Actions) khud APK build karega — aapko
sirf download karke install karna hai.

## Step 1: GitHub account banao

1. Phone browser mein https://github.com kholo → Sign up (free).

## Step 2: Naya repository banao

1. GitHub app install karo (Play Store se) — ya browser se hi kaam chalega.
2. Website/app par "+" → "New repository" → naam do `PrivateJarvis` → Private
   rakho (kyunki isme API key jaayegi) → Create.

## Step 3: Termux install karo (sirf git ke liye, Android build tools nahi)

1. Termux **F-Droid se install karo** (Play Store wala version purana/broken
   hai) — https://f-droid.org/packages/com.termux/ — F-Droid app install karke
   usse Termux download karo.
2. Termux kholo, ye commands chalao (ek-ek line, Enter dabate jao):
   ```
   pkg update -y
   pkg install git -y
   ```

## Step 4: Project files Termux mein layo

Maine jo `PrivateJarvis.zip` diya tha, use apne phone ke **Downloads** folder
mein already hoga (jahan Claude app se download hua). Ab:

1. Termux mein storage access do:
   ```
   termux-setup-storage
   ```
   (ek permission popup aayega, Allow karo)
2. Zip ko unzip karo:
   ```
   cd ~
   unzip /sdcard/Download/PrivateJarvis.zip
   cd PrivateJarvis
   ```
   (agar `unzip` command na mile: `pkg install unzip -y` chala ke phir try karo)

## Step 5: API key daalo

1. Termux mein hi ek text editor use karo:
   ```
   pkg install nano -y
   nano app/src/main/java/com/ravan/jarvis/Config.kt
   ```
2. `PASTE_YOUR_ANTHROPIC_API_KEY_HERE` ko apni asli key se replace karo
   (arrow keys se move karo, type karo).
3. Save: `Ctrl+O` phir Enter, exit: `Ctrl+X`.

## Step 6: GitHub par push karo

Termux mein:
```
git config --global user.email "aapka-email@example.com"
git config --global user.name "Aapka Naam"
git init
git add .
git commit -m "Private Jarvis first version"
git branch -M main
git remote add origin https://github.com/AAPKA-USERNAME/PrivateJarvis.git
git push -u origin main
```
(`AAPKA-USERNAME` ki jagah apna GitHub username daalo)

Push karte waqt username/password maangega — password ki jagah ek
**Personal Access Token** chahiye hoga (GitHub ab plain password allow nahi
karta):
1. GitHub website → Settings → Developer settings → Personal access tokens →
   Generate new token (classic) → "repo" permission check karo → Generate.
2. Ye token copy karke, jab Termux password maange, wahi paste karo.

## Step 7: Build khud-ba-khud shuru ho jayega

1. Push hote hi GitHub Actions khud build shuru kar dega (maine workflow file
   already project mein daal di hai).
2. GitHub repo kholo → "Actions" tab → apna build chalte dikhega (2-4 min
   lagte hain).
3. Green tick aane ke baad, usi build run ke andar niche "Artifacts" section
   mein `PrivateJarvis-debug-apk` milega — download karo (ek zip milega jisme
   APK hai).

## Step 8: Phone pe install karo

1. Downloaded zip ko extract karo (koi bhi file manager app se, ya Termux mein
   `unzip`).
2. `.apk` file par tap karo → "Install" → agar "unknown source" warning aaye
   to Settings mein allow karo → Install.

Bas — Jarvis ab aapke phone mein hai, bina kisi computer ke bana hua.

## Agar kahin atko

Jis bhi step pe error aaye, uska exact message bata dena — main us hisaab se
next step batata hoon.
