import React, { useState } from 'react';
import {
  Download, ShieldCheck, Moon, Sun, Smartphone, Radio,
  FolderOpen, Sliders, Timer, Zap, CheckCircle2, ChevronRight,
  ExternalLink, Lock, AlertTriangle, Play, Sparkles, Heart,
  Volume2, Layers, Headphones, Music, Check
} from 'lucide-react';

export default function LandingPage() {
  const [isDark, setIsDark] = useState(true);
  const [activeStep, setActiveStep] = useState(1);
  const [downloadCount, setDownloadCount] = useState(1320);
  const [hasDownloaded, setHasDownloaded] = useState(false);

  const toggleTheme = () => setIsDark(!isDark);

  const handleDownload = () => {
    setDownloadCount(prev => prev + 1);
    setHasDownloaded(true);
    // Dispara download direto do APK
    const link = document.createElement('a');
    link.href = '/OlirumPlay.apk';
    link.download = 'OlirumPlay-v1.15.0.apk';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const genres = [
    { name: 'Música Católica', tag: 'Orações, Terço e Louvor', img: '/assets_app/genre_catolica.png' },
    { name: 'Música Clássica', tag: 'Bach, Mozart, Chopin', img: '/assets_app/genre_classica.png' },
    { name: 'Jazz & Blues', tag: 'Smooth Jazz & Instrumental', img: '/assets_app/genre_jazz_blues.png' },
    { name: 'Classic Rock', tag: 'Lendas dos Anos 70 e 80', img: '/assets_app/genre_classic_rock.png' },
    { name: 'Love Songs', tag: 'Baladas Românticas Globais', img: '/assets_app/genre_love_songs.png' },
    { name: 'Lounge & Chillout', tag: 'Easy Listening & Relax', img: '/assets_app/genre_lounge.png' },
    { name: 'Sertanejo Raiz & Top', tag: 'Modão e Sucessos', img: '/assets_app/genre_sertanejo.png' },
    { name: 'Pop & Top 40', tag: 'Grandes Hits Globais', img: '/assets_app/genre_pop_hits.png' },
  ];

  return (
    <div className={`min-h-screen transition-colors duration-300 font-sans ${
      isDark ? 'bg-[#0B0F19] text-slate-100' : 'bg-[#F1F5F9] text-slate-800'
    }`}>

      {/* ==================================================================== */}
      {/* 1. BARRA DE NAVEGAÇÃO SUPERIOR CLEAN                                 */}
      {/* ==================================================================== */}
      <header className={`sticky top-0 z-50 backdrop-blur-md border-b transition-colors ${
        isDark ? 'bg-[#0B0F19]/85 border-slate-800/80' : 'bg-[#F8FAFC]/85 border-slate-200/80'
      }`}>
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#00E5D4] via-[#008BFF] to-[#FF3DAD] flex items-center justify-center p-0.5 shadow-sm">
              <div className="w-full h-full bg-[#0B0F19] rounded-[10px] flex items-center justify-center">
                <Music className="w-5 h-5 text-[#00E5D4]" />
              </div>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-[#00E5D4] to-[#008BFF] bg-clip-text text-transparent">
                  OlirumPlay
                </span>
                <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  v1.15.0
                </span>
              </div>
              <p className="text-[11px] text-slate-500 hidden sm:block">Rádios ao Vivo & Pastas do Celular</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            {/* Botão Alternar Tema */}
            <button
              onClick={toggleTheme}
              aria-label="Alternar Tema"
              className={`p-2.5 rounded-xl border transition-all ${
                isDark 
                  ? 'bg-slate-800/60 border-slate-700 text-amber-400 hover:bg-slate-800' 
                  : 'bg-slate-100 border-slate-200 text-slate-600 hover:bg-slate-200/80'
              }`}
            >
              {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>

            {/* Botão Baixar APK Header */}
            <button
              onClick={handleDownload}
              className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-bold text-white bg-gradient-to-r from-[#008BFF] to-[#00E5D4] hover:opacity-95 shadow-md shadow-[#008BFF]/20 active:scale-95 transition-all"
            >
              <Download className="w-4 h-4" />
              <span>Baixar APK</span>
            </button>
          </div>
        </div>
      </header>

      {/* ==================================================================== */}
      {/* 2. HERO SECTION COM PROPOSTA DE VALOR & MOCKUP REAL                  */}
      {/* ==================================================================== */}
      <section className="relative pt-12 pb-16 overflow-hidden">
        {/* Glow de fundo sutil */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-gradient-to-tr from-[#00E5D4]/15 via-[#008BFF]/15 to-[#FF3DAD]/10 blur-3xl pointer-events-none rounded-full" />

        <div className="max-w-6xl mx-auto px-4 sm:px-6 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            
            {/* Coluna Texto e Conversão */}
            <div className="lg:col-span-7 space-y-6 text-center lg:text-left">
              <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full text-xs font-semibold bg-[#008BFF]/10 text-[#008BFF] border border-[#008BFF]/20">
                <Sparkles className="w-3.5 h-3.5 text-[#00E5D4]" />
                <span>O Player Completo que Respeita Você</span>
              </div>

              <h1 className="text-3xl sm:text-5xl font-black tracking-tight leading-tight">
                Suas músicas do celular e as melhores rádios. <br className="hidden sm:inline" />
                <span className="bg-gradient-to-r from-[#008BFF] via-[#00E5D4] to-[#FF3DAD] bg-clip-text text-transparent">
                  Sem mensalidade. Sem anúncios.
                </span>
              </h1>

              <p className={`text-base sm:text-lg leading-relaxed max-w-2xl mx-auto lg:mx-0 ${
                isDark ? 'text-slate-300' : 'text-slate-600'
              }`}>
                Chega de pagar R$ 34 por mês no Spotify ou ter suas músicas cortadas por propagandas chatas. 
                O <strong>OlirumPlay</strong> toca todas as pastas do seu aparelho e mais de 50 rádios do mundo todo com áudio contínuo em segundo plano.
              </p>

              {/* Botão de Download Principal em Destaque */}
              <div className="pt-2 flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4">
                <button
                  onClick={handleDownload}
                  className="w-full sm:w-auto px-8 py-4 rounded-2xl font-black text-base sm:text-lg text-white bg-gradient-to-r from-[#008BFF] via-[#00B4D8] to-[#00E5D4] hover:shadow-xl hover:shadow-[#008BFF]/25 active:scale-95 transition-all flex items-center justify-center space-x-3 cursor-pointer"
                >
                  <Download className="w-6 h-6 animate-bounce" />
                  <div className="text-left">
                    <div className="leading-tight">BAIXAR OLIRUMPLAY APK</div>
                    <div className="text-[11px] font-normal text-white/80">Versão 1.15.0 Oficial • 37.1 MB • 100% Grátis</div>
                  </div>
                </button>
              </div>

              {hasDownloaded && (
                <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs sm:text-sm font-medium flex items-center space-x-2">
                  <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
                  <span>Download iniciado! Siga as 3 etapas abaixo para instalar no seu Android sem medo.</span>
                </div>
              )}

              {/* Selos de Confiança Imediatos */}
              <div className="pt-3 grid grid-cols-2 sm:grid-cols-4 gap-3 text-left">
                <div className={`p-3 rounded-xl border ${isDark ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'}`}>
                  <ShieldCheck className="w-5 h-5 text-emerald-500 mb-1" />
                  <div className="text-xs font-bold">100% Seguro</div>
                  <div className="text-[10px] text-slate-500">Sem vírus ou malwares</div>
                </div>

                <div className={`p-3 rounded-xl border ${isDark ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'}`}>
                  <Zap className="w-5 h-5 text-[#00E5D4] mb-1" />
                  <div className="text-xs font-bold">Áudio Contínuo</div>
                  <div className="text-[10px] text-slate-500">Tela apagada & 2º plano</div>
                </div>

                <div className={`p-3 rounded-xl border ${isDark ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'}`}>
                  <Lock className="w-5 h-5 text-[#008BFF] mb-1" />
                  <div className="text-xs font-bold">Privacidade Total</div>
                  <div className="text-[10px] text-slate-500">Sem permissões abusivas</div>
                </div>

                <div className={`p-3 rounded-xl border ${isDark ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'}`}>
                  <Headphones className="w-5 h-5 text-[#FF3DAD] mb-1" />
                  <div className="text-xs font-bold">Equalizador</div>
                  <div className="text-[10px] text-slate-500">Mega Bass & Presets</div>
                </div>
              </div>
            </div>

            {/* Coluna Mockup Real do Celular */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="relative">
                {/* Borda do celular estilo iPhone/Galaxy */}
                <div className={`w-[290px] sm:w-[320px] rounded-[44px] p-3 shadow-2xl border transition-all ${
                  isDark ? 'bg-slate-900 border-slate-700 shadow-[#008BFF]/10' : 'bg-white border-slate-300 shadow-slate-300/60'
                }`}>
                  <div className="relative rounded-[36px] overflow-hidden bg-black aspect-[9/19.5] border border-slate-800/20">
                    <img 
                      src="/screens/screen_home.png" 
                      alt="Tela Principal do OlirumPlay" 
                      className="w-full h-full object-cover object-top"
                    />
                  </div>
                </div>

                {/* Badge Flutuante 1: Áudio Indestrutível */}
                <div className={`absolute -bottom-4 -left-6 sm:-left-8 p-3 rounded-2xl border shadow-xl flex items-center space-x-3 backdrop-blur-md ${
                  isDark ? 'bg-slate-900/90 border-slate-700' : 'bg-white/95 border-slate-200'
                }`}>
                  <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#00E5D4] to-[#008BFF] flex items-center justify-center text-white">
                    <Zap className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold">Áudio Indestrutível</div>
                    <div className="text-[11px] text-slate-500">Não para no WhatsApp ou Waze</div>
                  </div>
                </div>

                {/* Badge Flutuante 2: Sleep Timer */}
                <div className={`absolute -top-3 -right-4 sm:-right-6 p-3 rounded-2xl border shadow-xl flex items-center space-x-2.5 backdrop-blur-md ${
                  isDark ? 'bg-slate-900/90 border-slate-700' : 'bg-white/95 border-slate-200'
                }`}>
                  <Timer className="w-5 h-5 text-indigo-500" />
                  <div>
                    <div className="text-xs font-bold">Sleep Timer</div>
                    <div className="text-[10px] text-slate-500">Fade-out suave para dormir</div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>

      {/* ==================================================================== */}
      {/* 3. GUIA PASSO A PASSO ANTI-PÂNICO (COMO INSTALAR NO ANDROID)         */}
      {/* ==================================================================== */}
      <section className={`py-14 border-y transition-colors ${
        isDark ? 'bg-slate-900/40 border-slate-800/80' : 'bg-slate-50 border-slate-200/80'
      }`}>
        <div className="max-w-5xl mx-auto px-4 sm:px-6">
          <div className="text-center max-w-2xl mx-auto mb-10">
            <span className="text-xs font-extrabold tracking-widest text-[#008BFF] uppercase">Instalação Simples & Confiável</span>
            <h2 className="text-2xl sm:text-3xl font-extrabold mt-1">
              Como Instalar em Menos de 1 Minuto
            </h2>
            <p className={`text-sm mt-2 ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
              Como o OlirumPlay é baixado diretamente do nosso site (fora da Google Play Store), o Android exibe avisos padrão de proteção. É 100% seguro!
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            
            {/* Passo 1 */}
            <div className={`p-6 rounded-2xl border transition-all ${
              activeStep === 1 
                ? 'border-[#008BFF] ring-2 ring-[#008BFF]/20 shadow-md' 
                : isDark ? 'border-slate-800 bg-slate-900/60' : 'border-slate-200 bg-white'
            }`}
            onClick={() => setActiveStep(1)}
            >
              <div className="w-10 h-10 rounded-xl bg-[#008BFF]/10 text-[#008BFF] font-black flex items-center justify-center text-lg mb-4">
                1
              </div>
              <h3 className="font-bold text-base mb-2">Clique em Baixar APK</h3>
              <p className={`text-xs leading-relaxed mb-3 ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Se o Chrome exibir <em>"O arquivo pode ser nocivo"</em>, fique em paz! É a mensagem padrão do Android para qualquer arquivo baixado pela internet.
              </p>
              <div className="p-2.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                👉 Clique em <strong>"Fazer o download mesmo assim"</strong>
              </div>
            </div>

            {/* Passo 2 */}
            <div className={`p-6 rounded-2xl border transition-all ${
              activeStep === 2 
                ? 'border-[#00E5D4] ring-2 ring-[#00E5D4]/20 shadow-md' 
                : isDark ? 'border-slate-800 bg-slate-900/60' : 'border-slate-200 bg-white'
            }`}
            onClick={() => setActiveStep(2)}
            >
              <div className="w-10 h-10 rounded-xl bg-[#00E5D4]/10 text-[#00E5D4] font-black flex items-center justify-center text-lg mb-4">
                2
              </div>
              <h3 className="font-bold text-base mb-2">Abra e Permita a Fonte</h3>
              <p className={`text-xs leading-relaxed mb-3 ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Puxe a barra de notificações do celular e toque no <strong>OlirumPlay.apk</strong>. Se o Android pedir autorização:
              </p>
              <div className="p-2.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                👉 Toque em <strong>"Configurações"</strong> e ative <strong>"Permitir desta fonte"</strong>
              </div>
            </div>

            {/* Passo 3 */}
            <div className={`p-6 rounded-2xl border transition-all ${
              activeStep === 3 
                ? 'border-[#FF3DAD] ring-2 ring-[#FF3DAD]/20 shadow-md' 
                : isDark ? 'border-slate-800 bg-slate-900/60' : 'border-slate-200 bg-white'
            }`}
            onClick={() => setActiveStep(3)}
            >
              <div className="w-10 h-10 rounded-xl bg-[#FF3DAD]/10 text-[#FF3DAD] font-black flex items-center justify-center text-lg mb-4">
                3
              </div>
              <h3 className="font-bold text-base mb-2">Instalar e Aproveitar!</h3>
              <p className={`text-xs leading-relaxed mb-3 ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Toque no botão <strong>"Instalar"</strong>. Em 3 segundos o app estará pronto com todas as suas pastas e rádios ao vivo.
              </p>
              <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-[11px] font-semibold flex items-center space-x-1">
                <Check className="w-3.5 h-3.5" />
                <span>Pronto para tocar sem interrupções!</span>
              </div>
            </div>

          </div>

          {/* Banner de Tranquilidade & Segurança */}
          <div className={`mt-8 p-4 sm:p-5 rounded-2xl border flex flex-col sm:flex-row items-center justify-between gap-4 ${
            isDark ? 'bg-slate-900/80 border-slate-700/80' : 'bg-white border-slate-200'
          }`}>
            <div className="flex items-center space-x-3.5 text-center sm:text-left">
              <div className="w-12 h-12 rounded-xl bg-emerald-500/15 flex items-center justify-center flex-shrink-0 text-emerald-500">
                <ShieldCheck className="w-7 h-7" />
              </div>
              <div>
                <h4 className="text-sm font-bold">Por que o OlirumPlay é 100% Seguro?</h4>
                <p className="text-xs text-slate-500 max-w-xl">
                  O app não tem acesso aos seus dados pessoais, fotos, câmera nem agenda. Ele apenas lê os arquivos de música do seu celular para reproduzir com a melhor fidelidade sonora.
                </p>
              </div>
            </div>

            <button
              onClick={handleDownload}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-slate-900 dark:bg-slate-100 dark:text-slate-900 hover:opacity-90 active:scale-95 transition-all flex-shrink-0"
            >
              Iniciar Download Seguro
            </button>
          </div>
        </div>
      </section>

      {/* ==================================================================== */}
      {/* 4. OS GRANDES RECURSOS REAIS (COM PRINTS REAIS DO APP)               */}
      {/* ==================================================================== */}
      <section className="py-16">
        <div className="max-w-6xl mx-auto px-4 sm:px-6">
          <div className="text-center max-w-2xl mx-auto mb-14">
            <span className="text-xs font-extrabold tracking-widest text-[#00E5D4] uppercase">Tudo em Um Só Lugar</span>
            <h2 className="text-2xl sm:text-3xl font-extrabold mt-1">
              Recursos Reais que os Outros Players Cobram
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            
            {/* Card 1: Pastas Reais */}
            <div className={`rounded-3xl border overflow-hidden transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="h-56 bg-slate-100 dark:bg-slate-800/50 overflow-hidden flex items-center justify-center p-3">
                <img 
                  src="/screens/screen_folder.png" 
                  alt="Pastas e Músicas do Celular" 
                  className="h-full rounded-2xl shadow-md object-contain"
                />
              </div>
              <div className="p-6">
                <div className="w-9 h-9 rounded-xl bg-[#008BFF]/10 text-[#008BFF] flex items-center justify-center mb-3">
                  <FolderOpen className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base mb-1.5">Músicas & Pastas do Celular</h3>
                <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                  Navegue direto pelas pastas do seu cartão de memória ou armazenamento interno (`/Download`, áudios do WhatsApp, gravações). Botão de Tocar e Modo Aleatório na pasta com 1 toque.
                </p>
              </div>
            </div>

            {/* Card 2: 50+ Rádios Globais */}
            <div className={`rounded-3xl border overflow-hidden transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="h-56 bg-slate-100 dark:bg-slate-800/50 overflow-hidden flex items-center justify-center p-3">
                <img 
                  src="/screens/screen_explore.png" 
                  alt="Explorar Gêneros de Rádio" 
                  className="h-full rounded-2xl shadow-md object-contain"
                />
              </div>
              <div className="p-6">
                <div className="w-9 h-9 rounded-xl bg-[#00E5D4]/10 text-[#00E5D4] flex items-center justify-center mb-3">
                  <Radio className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base mb-1.5">Mais de 50 Rádios por Gênero</h3>
                <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                  Católica, Clássica, Jazz & Blues, Classic Rock, Love Songs, Sertanejo, Pop Top 40, Lounge e Eletrônica. Estações com transmissão ao vivo de alta fidelidade e zero custo.
                </p>
              </div>
            </div>

            {/* Card 3: Equalizador com Mega Bass & Presets */}
            <div className={`rounded-3xl border overflow-hidden transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="h-56 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-6 flex flex-col justify-center text-white">
                <div className="text-xs font-bold text-[#00E5D4] uppercase tracking-wider mb-2">Presets de 1 Toque</div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-xs bg-white/10 px-3 py-1.5 rounded-lg">
                    <span>🔥 Mega Bass (Graves Potentes)</span>
                    <span className="font-bold text-[#00E5D4]">100%</span>
                  </div>
                  <div className="flex items-center justify-between text-xs bg-white/10 px-3 py-1.5 rounded-lg">
                    <span>🎤 Voz Clara (Rádio & Pregações)</span>
                    <span className="font-bold text-[#008BFF]">95%</span>
                  </div>
                  <div className="flex items-center justify-between text-xs bg-white/10 px-3 py-1.5 rounded-lg">
                    <span>🚗 Modo Estrada (Anti-Ruído)</span>
                    <span className="font-bold text-[#FF3DAD]">Ativo</span>
                  </div>
                </div>
              </div>
              <div className="p-6">
                <div className="w-9 h-9 rounded-xl bg-[#FF3DAD]/10 text-[#FF3DAD] flex items-center justify-center mb-3">
                  <Sliders className="w-5 h-5" />
                </div>
                <h3 className="font-bold text-base mb-1.5">Equalizador & Mega Bass</h3>
                <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                  Dê aquele peso encorpado na caixa de som Bluetooth ou nos fones de ouvido. Presets inteligentes desenvolvidos para quem gosta de grave limpo e voz cristalina.
                </p>
              </div>
            </div>

            {/* Card 4: Sleep Timer */}
            <div className={`rounded-3xl border p-6 transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-500 flex items-center justify-center mb-4">
                <Timer className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base mb-2">Timer para Dormir com Fade-Out</h3>
              <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Deite para dormir ouvindo sua rádio favorita ou sons relaxantes. Defina 15, 30, 45 ou 60 minutos: o som não corta bruscamente, ele vai diminuindo aos poucos nos últimos 30 segundos até desligar.
              </p>
            </div>

            {/* Card 5: Áudio Indestrutível */}
            <div className={`rounded-3xl border p-6 transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="w-12 h-12 rounded-2xl bg-[#008BFF]/10 text-[#008BFF] flex items-center justify-center mb-4">
                <Zap className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base mb-2">Reprodução em 2º Plano Verdadeira</h3>
              <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Apague a tela, coloque o celular no bolso, responda mensagens no WhatsApp ou navegue no GPS. O OlirumPlay continua tocando perfeitamente sem engasgos.
              </p>
            </div>

            {/* Card 6: Widget de Tela Inicial */}
            <div className={`rounded-3xl border p-6 transition-all hover:shadow-lg ${
              isDark ? 'bg-slate-900/70 border-slate-800' : 'bg-white border-slate-200'
            }`}>
              <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center mb-4">
                <Smartphone className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base mb-2">Widget Nativo na Tela Inicial</h3>
              <p className={`text-xs leading-relaxed ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
                Controle o Play, Pause, faixa anterior, próxima e veja a capa do álbum diretamente na tela principal do seu Android sem nem precisar abrir o app.
              </p>
            </div>

          </div>
        </div>
      </section>

      {/* ==================================================================== */}
      {/* 5. VITRINE DE GÊNEROS DE RÁDIO COM CAPAS OFICIAIS                   */}
      {/* ==================================================================== */}
      <section className={`py-14 border-t transition-colors ${
        isDark ? 'bg-slate-900/30 border-slate-800/80' : 'bg-slate-100/60 border-slate-200/80'
      }`}>
        <div className="max-w-6xl mx-auto px-4 sm:px-6">
          <div className="flex flex-col sm:flex-row items-center justify-between mb-8 text-center sm:text-left gap-4">
            <div>
              <span className="text-xs font-extrabold tracking-widest text-[#008BFF] uppercase">Catálogo Aberto</span>
              <h2 className="text-2xl font-extrabold mt-1">Estilos de Rádio ao Vivo</h2>
            </div>
            <button
              onClick={handleDownload}
              className="text-xs font-bold text-[#008BFF] hover:underline flex items-center space-x-1"
            >
              <span>Ouvir todas no celular</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {genres.map((g, idx) => (
              <div
                key={idx}
                className={`group relative rounded-2xl overflow-hidden border p-3 flex flex-col justify-end aspect-[4/3] transition-all hover:scale-[1.02] cursor-pointer ${
                  isDark ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-200 shadow-sm'
                }`}
                onClick={handleDownload}
              >
                <img 
                  src={g.img} 
                  alt={g.name} 
                  className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent" />
                <div className="relative z-10 text-white">
                  <div className="text-xs sm:text-sm font-bold tracking-tight drop-shadow">{g.name}</div>
                  <div className="text-[10px] text-white/80 line-clamp-1">{g.tag}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ==================================================================== */}
      {/* 6. CALL TO ACTION FINAL LIMPO & DIRETO                              */}
      {/* ==================================================================== */}
      <section className="py-16 text-center">
        <div className="max-w-3xl mx-auto px-4 sm:px-6">
          <div className="w-16 h-16 mx-auto mb-6 rounded-2xl bg-gradient-to-tr from-[#00E5D4] via-[#008BFF] to-[#FF3DAD] flex items-center justify-center p-1 shadow-lg shadow-[#008BFF]/20">
            <div className="w-full h-full bg-[#070B14] rounded-xl flex items-center justify-center">
              <Music className="w-8 h-8 text-[#00E5D4]" />
            </div>
          </div>

          <h2 className="text-3xl sm:text-4xl font-black tracking-tight mb-4">
            Pronto para ter o seu som de volta?
          </h2>

          <p className={`text-base mb-8 max-w-lg mx-auto ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
            Baixe agora o OlirumPlay. Arquivo leve, instalação direta e suas músicas prontas para tocar.
          </p>

          <button
            onClick={handleDownload}
            className="w-full sm:w-auto px-10 py-4 rounded-2xl font-black text-lg text-white bg-gradient-to-r from-[#008BFF] via-[#00B4D8] to-[#00E5D4] hover:shadow-xl hover:shadow-[#008BFF]/30 active:scale-95 transition-all inline-flex items-center justify-center space-x-3 cursor-pointer"
          >
            <Download className="w-6 h-6" />
            <span>BAIXAR APK GRATUITO (v1.15.0)</span>
          </button>

          <div className="mt-4 text-xs text-slate-500">
            Compatível com Android 8.0, 9, 10, 11, 12, 13, 14 e 15 • 37.1 MB
          </div>
        </div>
      </section>

      {/* ==================================================================== */}
      {/* 7. FOOTER MINIMALISTA                                                */}
      {/* ==================================================================== */}
      <footer className={`py-8 border-t text-center text-xs transition-colors ${
        isDark ? 'bg-[#070B14] border-slate-800 text-slate-500' : 'bg-slate-50 border-slate-200 text-slate-500'
      }`}>
        <div className="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2">
            <span className="font-bold text-slate-700 dark:text-slate-300">OlirumPlay</span>
            <span>•</span>
            <span>© 2026 OlirumTec</span>
          </div>
          <div>
            Player de Áudio Local & Catálogo de Rádios Web Gratuitas
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-emerald-500 font-semibold flex items-center space-x-1">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>APK Verificado</span>
            </span>
          </div>
        </div>
      </footer>

    </div>
  );
}
