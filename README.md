# 🛡️ SecOps Console — v3.0

<div align="center">
  <img src="https://img.shields.io/badge/Version-3.0-brightgreen.svg?style=for-the-badge&logo=android&logoColor=white" alt="Version 3.0" />
  <img src="https://img.shields.io/badge/Created_By-H4cK3R-blueviolet.svg?style=for-the-badge&logo=cybersecurity&logoColor=white" alt="Created By H4cK3R" />
  <img src="https://img.shields.io/badge/Language-Kotlin-orange.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/AI_Copilot-Gemini_Powered-cyan.svg?style=for-the-badge&logo=google-gemini&logoColor=white" alt="Gemini Powered" />
</div>

---

**SecOps Console** არის მძლავრი, პროფესიონალური და მრავალფუნქციური მობილური კიბერუსაფრთხოების აპლიკაცია Android-ისთვის. იგი აერთიანებს რეალურ პენეტრაციული ტესტირების (Penetration Testing) ინსტრუმენტებს, ხელოვნური ინტელექტის (Gemini Copilot) მხარდაჭერასა და უახლეს, თანამედროვე Cyberpunk/Terminal UI დიზაინს.

**SecOps Console** is a powerful, professional, and fully-featured mobile cybersecurity and penetration testing suite for Android. It combines real security auditing tools with advanced AI integration (Gemini Copilot) and an immersive, premium Cyberpunk-themed user interface.

---

## 🌟 Capabilities & Features / შესაძლებლობები და ფუნქციები

### 1. 📶 WiFi Audit (Aircrack-ng)
*   **Real Auditing:** არ არის სიმულატორი! ინტეგრირებულია რეალური `aircrack-ng` ხელსაწყოების პაკეტი.
*   **Monitor Mode:** მხარს უჭერს მონიტორინგის რეჟიმის (Monitor Mode) ჩართვას თავსებად ჩიპსეტებზე.
*   **Handshake Capture:** WPA/WPA2 WPA Handshake ფაილების რეალური გზით ხელში ჩაგდება (Capture).
*   **Automated Interface Setup:** ავტომატური მონიტორინგის ინტერფეისის (`wlan0mon`) გამოვლენა და მართვა.

### 2. 📹 CCTV Cracker (RTSP & ONVIF Discovery)
*   **RTSP Discovery:** Nmap-ის გამოყენებით ქსელში აქტიური RTSP კამერების სკანირება (Port 554, 8554 და ა.შ.).
*   **ONVIF WS-Discovery:** UDP Multicast (`239.255.255.250:3702`) მოთხოვნის გაგზავნა ONVIF მოწყობილობების ავტომატური აღმოსაჩენად.
*   **Credentials Cracker:** 21 ყველაზე პოპულარული ქარხნული მომხმარებლის/პაროლის წყვილის (Default Credentials) სწრაფი შემოწმება RTSP Stream-ზე.

### 3. 💾 SQL Injection Testing (SqlMap Runner)
*   **SqlMap Integration:** `sqlmap` ბინარული ფაილის ავტომატური ინტეგრაცია Termux-იდან ან `/data/local/tmp`-იდან.
*   **Built-in SQLi Engine:** ბინარულის არარსებობის შემთხვევაში, აპლიკაციას აქვს საკუთარი HTTP SQL injection ტესტირების მექანიზმი (Timing-based SLEEP, Error-based).
*   **WAF Detection:** ქსელური ფაირვოლის (WAF - Cloudflare, Sucuri, ModSecurity და ა.შ.) ავტომატური აღმოჩენა და ანალიზი.

### 4. 🤖 AI Copilot (Gemini Assistant)
*   ინტეგრირებულია **Gemini 2.0 Flash** მოდელი.
*   კიბერუსაფრთხოების ვირტუალური ასისტენტი გეხმარებათ ლოგების ანალიზში, გირჩევთ საჭირო Nmap/Aircrack ბრძანებებს და გიჩვენებთ სკანირების შედეგების დეტალურ ახსნას.

### 5. 🔄 Auto-Update (ავტომატური განახლება)
*   **Zero-Manual Setup:** აღარ გჭირდებათ APK ფაილის ყოველ ჯერზე ხელით გადმოწერა და დაინსტალირება!
*   **GitHub Integration:** აპლიკაცია ავტომატურად უკავშირდება [secops Repository](https://github.com/G30RG3-GJ/secops) GitHub Releases-ს და ამოწმებს ვერსიებს.
*   **One-Click Install:** ახალი ვერსიის გამოსვლისას, ერთი კლიკით იწერს და აინსტალირებს განახლებას.

### 6. 🎨 Premium Parallax UI & UX
*   შექმნილია Jetpack Compose-ით, სრული Cyberpunk დიზაინით.
*   **Matrix Background Decor:** მატრიცული, მწვანე მანათობელი ეფექტები, მბრუნავი და მფეთქავი ანიმაციები About გვერდზე.
*   **CommonVisuals:** ერთგვაროვანი, დახვეწილი ტერმინალის სტილის ვიზუალები და ხმის ეფექტები.

---

## 🛠️ System Requirements / სისტემური მოთხოვნები

სრული ფუნქციონალისთვის აპლიკაციას ესაჭიროება:
1.  **Rooted Android Device** (საჭიროა `su` უფლებები Aircrack-ng, Nmap და WiFi მონიტორინგისთვის).
2.  **Kali NetHunter ან Termux**-ის გარემო, სადაც დაინსტალირებულია `aircrack-ng`, `nmap` და `sqlmap`.
3.  **თავსებადი WiFi ჩიპსეტი** (მონიტორინგის რეჟიმის მხარდაჭერით).

---

## 🚀 How To Run / როგორ გავუშვათ

### 1. Prerequisites (წინაპირობები):
*   [Android Studio](https://developer.android.com/studio) (Bumblebee ან უფრო ახალი).
*   თქვენი პერსონალური Gemini API Key.

### 2. Configuration (კონფიგურაცია):
1.  გახსენით პროექტი **Android Studio**-ში.
2.  შექმენით ფაილი `.env` პროექტის ძირეულ საქაღალდეში (Root Directory).
3.  `.env` ფაილში მიუთითეთ თქვენი Gemini API Key:
    ```env
    GEMINI_API_KEY=თქვენი_gemini_api_key_აქ
    APP_VERSION=3.0
    GITHUB_RELEASE_URL=https://api.github.com/repos/G30RG3-GJ/secops/releases/latest
    ```
4.  გაუშვით აპლიკაცია ემულატორზე ან რეალურ Android ტელეფონზე.

---

## 📂 Codebase Overview / კოდის სტრუქტურა

*   [`AircrackManager.kt`](file:///C:/Users/GEORGE/Desktop/android/secops/app/src/main/java/com/example/utils/AircrackManager.kt) — Aircrack-ng ბრძანებების მართვა და მონიტორინგის რეჟიმი.
*   [`CCTVCracker.kt`](file:///C:/Users/GEORGE/Desktop/android/secops/app/src/main/java/com/example/utils/CCTVCracker.kt) — RTSP და ONVIF სკანერი და Credential-ების გადამოწმება.
*   [`SqlMapRunner.kt`](file:///C:/Users/GEORGE/Desktop/android/secops/app/src/main/java/com/example/utils/SqlMapRunner.kt) — SqlMap-ის ინტეგრაცია და built-in SQLi ძრავი.
*   [`AutoUpdateManager.kt`](file:///C:/Users/GEORGE/Desktop/android/secops/app/src/main/java/com/example/utils/AutoUpdateManager.kt) — GitHub API-დან განახლებების ავტომატური შემოწმება და ინსტალაცია.
*   [`AboutPanel.kt`](file:///C:/Users/GEORGE/Desktop/android/secops/app/src/main/java/com/example/ui/components/AboutPanel.kt) — პრემიუმ About გვერდი, matrix rain დეკორაციითა და "Created By H4cK3R" წარწერით.

---

## ⚖️ Legal Disclaimer / იურიდიული პასუხისმგებლობა

> [!WARNING]
> ეს აპლიკაცია განკუთვნილია **მხოლოდ** ავტორიზებული უსაფრთხოების ტესტირებისა და საგანმანათლებლო მიზნებისთვის. კომპიუტერულ სისტემებზე უნებართვო წვდომა არის არალეგალური და ისჯება კანონით. დეველოპერი არ იღებს პასუხისმგებლობას აპლიკაციის არასწორად გამოყენებაზე.
>
> This application is intended **strictly** for authorized security testing and educational purposes. Unauthorized access to computer networks and systems is illegal. The author assumes no liability for any misuse or damage caused by this application.

---
<div align="center">
  <b>Created By H4cK3R © 2026</b>
</div>
