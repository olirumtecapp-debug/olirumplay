# 🚀 PONTO DE CONTINUIDADE ANTIGRAVITY — OLIRUMPLAY

> **Projeto:** `OlirumPlay` (D:\PlayerMPro)  
> **Status:** Checkpoint #001 — Catálogo Streaming Completo (Audius), Letras Sincronizadas (LRCLIB), Leitor Local ID3, Capas HD 1000x1000 & Persistência Android  
> **Data:** 27 de Outubro de 2026  
> **Tecnologia:** React 18, Vite 5, Tailwind CSS, Lucide Icons, Capacitor 6, Web Audio API, IndexedDB, MediaSession API

---

## 🎧 O QUE É O OLIRUMPLAY?
O **OlirumPlay** é um player híbrido pessoal inspirado na experiência do Muka Player e Spotify, com design **Cyber Neon** (Preto Profundo `#060B14`, Ciano Elétrico `#00F5D4` e Roxo Cyber `#7040F0`), oferecendo:
1. **Catálogo de Músicas Gratuitas & Completas:** Integração nativa com a API do Audius (centenas de milhares de faixas em 320 kbps completas com filtros por gênero: Em Alta, Eletrônica, Lo-Fi & Chill, Rock, Hip-Hop/Trap, Pop, Acústico).
2. **Leitor de Músicas Locais do Celular:** Escaneamento direto de arquivos (`.mp3`, `.m4a`, `.wav`, `.flac`) com parser nativo de tags ID3v2 e extração de capas embutidas.
3. **Letras Sincronizadas em Tempo Real (LRC):** Conexão com LRCLIB para exibir letras estilo karaokê que rolam automaticamente e destacam em neon ciano o verso exato que está tocando.
4. **Buscador & Downloader de Capas em Alta Resolução (1000x1000 px):** Conexão com o banco de dados oficial em Ultra HD, permitindo baixar a arte original ou aplicar nas faixas locais.
5. **Persistência Indestrutível de Reprodução:**
   - Salva a posição do áudio, volume, fila e estado a cada progresso no `localStorage`.
   - Permissões nativas no `AndroidManifest.xml` (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `WAKE_LOCK`, `READ_MEDIA_AUDIO`).
   - Controles completos na tela de bloqueio e barra de notificações do Android via `navigator.mediaSession`.
6. **Equalizador WebAudio 5 Bandas & Sleep Timer:** Processamento de frequências sonoras nativo (Sub-Grave 60Hz a Agudos 14kHz), velocidade ajustável e timer de desligamento para dormir.

---

## 📁 ESTRUTURA DO PROJETO
```
D:\PlayerMPro
├── android/                 # Projeto nativo Android (Capacitor 6)
│   └── app/src/main/
│       └── AndroidManifest.xml # Permissões de segundo plano e mídia
├── public/
│   ├── logo.png             # Logo neon headphones OlirumPlay
│   ├── banner.png           # Banner oficial neon OlirumPlay
│   └── manifest.json        # PWA & Web App Manifest
├── src/
│   ├── App.jsx              # Aplicação principal completa (2200+ linhas)
│   ├── index.css            # Estilos cyber neon, scrollbars e glows
│   └── main.jsx             # Entrypoint React 18
├── .github/workflows/
│   └── build-apk.yml        # CI/CD no GitHub Actions para gerar o APK
├── capacitor.config.json    # Configurações do Capacitor (com.olirum.player)
└── package.json             # Dependências Vite, React e Capacitor
```

---

## 🛠️ COMO RODAR LOCALMENTE NO NAVEGADOR
```bash
cd D:\PlayerMPro
npm run dev
```
Acesse: `http://localhost:5173`

---

## 📱 COMO COMPILAR O APK PARA ANDROID
### Opção A: No seu computador via Android Studio
```bash
cd D:\PlayerMPro
npm run build
npx cap sync android
npx cap open android
```
*(No Android Studio, clique em Build > Build Bundle(s) / APK(s) > Build APK(s))*

### Opção B: Na nuvem via GitHub Actions
Suba o repositório para o GitHub; o workflow `.github/workflows/build-apk.yml` compilará o `OlirumPlay-APK` automaticamente na aba Actions.

---

## 🚨 FRASE DE EMERGÊNCIA
> *"Antigravity, continue o projeto OlirumPlay a partir do Checkpoint #001!"*
