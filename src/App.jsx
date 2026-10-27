import React, { useState, useEffect, useRef } from 'react';
import {
  Play, Pause, SkipBack, SkipForward, Volume2, VolumeX,
  Repeat, Shuffle, Heart, Download, Trash2, Search,
  Music, WifiOff, PlusCircle, Compass, HardDrive,
  CheckCircle2, Loader2, Share2, Disc, Sparkles, Sliders,
  FolderOpen, FileAudio, Clock, ListMusic, ChevronDown,
  Copy, Check, Layers, RefreshCw, AlertCircle,
  Smartphone, Archive, Upload, Radio, Flame, X, MoreVertical,
  Gauge, Moon, Plus, FolderPlus, ArrowUpDown, ExternalLink,
  FileText, Image as ImageIcon, CheckCircle, Zap, LayoutGrid,
  Library, Settings, Maximize2, Minimize2, Move
} from 'lucide-react';

// ============================================================================
// 1. CATÁLOGO INICIAL DE DESTAQUES & CURADORIA (ESTILO SPOTIFY / MUKA)
// ============================================================================
const EDITORIAL_HITS = [
  {
    id: 'hit-1',
    title: 'Hear Me Now',
    artist: 'Alok, Bruno Martini, Zeeba',
    album: 'Alok Electronic Hits',
    duration: 194,
    cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    genre: 'Eletrônica',
    source: 'cloud',
    isFullSong: true
  },
  {
    id: 'hit-2',
    title: 'Bohemian Rhapsody',
    artist: 'Queen',
    album: 'A Night at the Opera',
    duration: 355,
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    genre: 'Rock Clássico',
    source: 'cloud',
    isFullSong: true
  },
  {
    id: 'hit-3',
    title: 'Infiel (Ao Vivo)',
    artist: 'Marília Mendonça',
    album: 'Realidade (Ao Vivo)',
    duration: 205,
    cover: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    genre: 'Sertanejo',
    source: 'cloud',
    isFullSong: true
  },
  {
    id: 'hit-4',
    title: 'Yellow',
    artist: 'Coldplay',
    album: 'Parachutes',
    duration: 269,
    cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    genre: 'Pop / Rock',
    source: 'cloud',
    isFullSong: true
  },
  {
    id: 'hit-5',
    title: 'Cyberpunk Neon Nights',
    artist: 'Olirum Synth Collective',
    album: 'Horizonte 2026',
    duration: 240,
    cover: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    genre: 'Synthwave',
    source: 'cloud',
    isFullSong: true
  }
];

const GENRE_CHIPS = [
  '🔥 Em Alta',
  '⚡ Eletrônica',
  '🎸 Rock & Metal',
  '🤠 Sertanejo & Piseiro',
  '☕ Lo-Fi & Chill',
  '🎤 Hip-Hop & Funk',
  '✨ Pop Internacional',
  '🌿 Acústico & MPB'
];

const EQ_PRESETS = {
  flat: { name: 'Flat (Original)', gains: [0, 0, 0, 0, 0] },
  bass: { name: 'Bass Boost (Grave Neon)', gains: [8, 6, 0, -1, 1] },
  electronic: { name: 'Electronic / EDM', gains: [6, 3, -2, 3, 6] },
  vocal: { name: 'Vocal & Podcast', gains: [-3, -1, 5, 4, 1] },
  rock: { name: 'Rock / Guitarra', gains: [5, 2, -2, 4, 5] },
  lofi: { name: 'Lo-Fi Warm Night', gains: [5, 4, 1, -4, -6] }
};

const EQ_BANDS = [
  { freq: 60, label: '60Hz', desc: 'Sub-Grave' },
  { freq: 230, label: '230Hz', desc: 'Grave' },
  { freq: 910, label: '910Hz', desc: 'Médio' },
  { freq: 3600, label: '3.6kHz', desc: 'Presença' },
  { freq: 14000, label: '14kHz', desc: 'Agudo' }
];

// ============================================================================
// 2. BANCO INDEXED-DB PERSISTENTE
// ============================================================================
const DB_NAME = 'OlirumPlayDB';
const DB_VERSION = 3;
const STORE_TRACKS = 'tracks';
const STORE_PLAYLISTS = 'playlists';

const openDatabase = () => {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = (e) => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains(STORE_TRACKS)) {
        db.createObjectStore(STORE_TRACKS, { keyPath: 'id' });
      }
      if (!db.objectStoreNames.contains(STORE_PLAYLISTS)) {
        db.createObjectStore(STORE_PLAYLISTS, { keyPath: 'id' });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
};

const saveTrackToIDB = async (track, blob) => {
  const db = await openDatabase();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_TRACKS, 'readwrite');
    const store = tx.objectStore(STORE_TRACKS);
    const record = {
      ...track,
      audioBlob: blob || track.audioBlob,
      savedAt: new Date().toISOString(),
      fileSize: blob ? blob.size : (track.fileSize || 0)
    };
    store.put(record);
    tx.oncomplete = () => resolve(record);
    tx.onerror = () => reject(tx.error);
  });
};

const getAllTracksFromIDB = async () => {
  const db = await openDatabase();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_TRACKS, 'readonly');
    const store = tx.objectStore(STORE_TRACKS);
    const req = store.getAll();
    req.onsuccess = () => resolve(req.result || []);
    req.onerror = () => reject(req.error);
  });
};

const deleteTrackFromIDB = async (id) => {
  const db = await openDatabase();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_TRACKS, 'readwrite');
    const store = tx.objectStore(STORE_TRACKS);
    store.delete(id);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
};

// ============================================================================
// 3. PARSER LRC (LETRAS SINCRONIZADAS EM TEMPO REAL)
// ============================================================================
const parseLrcString = (lrcText) => {
  if (!lrcText || typeof lrcText !== 'string') return [];
  const lines = lrcText.split('\n');
  const result = [];
  const timeRegex = /\[(\d{2}):(\d{2}(?:\.\d{1,3})?)\]/g;

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    let match;
    const timestamps = [];
    while ((match = timeRegex.exec(trimmed)) !== null) {
      const min = parseInt(match[1], 10);
      const sec = parseFloat(match[2]);
      timestamps.push(min * 60 + sec);
    }
    const text = trimmed.replace(timeRegex, '').trim();
    if (timestamps.length > 0 && text) {
      for (const t of timestamps) {
        result.push({ time: t, text });
      }
    }
  }

  result.sort((a, b) => a.time - b.time);
  return result;
};

// ============================================================================
// 4. PARSER ID3 BINÁRIO (LOCAL WEB FALLBACK)
// ============================================================================
const generateGradientCover = (title = 'Olirum') => {
  const palettes = [
    ['#00f5d4', '#7040f0'],
    ['#7040f0', '#ff007f'],
    ['#00f5d4', '#0B132B'],
    ['#00c2ff', '#7040f0'],
    ['#9d4edd', '#00f5d4']
  ];
  const idx = Math.abs(title.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0)) % palettes.length;
  const [c1, c2] = palettes[idx];
  const initials = title.replace(/[^a-zA-Z0-9 ]/g, '').trim().slice(0, 2).toUpperCase() || 'OP';
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" viewBox="0 0 600 600">
    <defs>
      <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="${c1}"/>
        <stop offset="100%" stop-color="${c2}"/>
      </linearGradient>
    </defs>
    <rect width="600" height="600" fill="#060b14"/>
    <circle cx="300" cy="300" r="220" fill="url(#g)" opacity="0.4"/>
    <circle cx="300" cy="300" r="160" stroke="#00f5d4" stroke-width="4" fill="none" opacity="0.6"/>
    <circle cx="300" cy="300" r="50" fill="#060b14" stroke="#7040f0" stroke-width="3"/>
    <text x="50%" y="54%" dominant-baseline="middle" text-anchor="middle" fill="#00f5d4" font-family="sans-serif" font-weight="900" font-size="72" letter-spacing="4">${initials}</text>
  </svg>`;
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
};

const parseID3v2Metadata = async (file) => {
  const cleanName = file.name.replace(/\.[^/.]+$/, '');
  const parts = cleanName.split(' - ');
  const fallbackMeta = {
    title: parts.length > 1 ? parts.slice(1).join(' - ').trim() : cleanName,
    artist: parts.length > 1 ? parts[0].trim() : 'Música do Dispositivo',
    album: 'Memória Local',
    cover: generateGradientCover(cleanName)
  };

  try {
    const slice = file.slice(0, 262144);
    const buffer = await slice.arrayBuffer();
    const view = new DataView(buffer);

    if (
      view.byteLength < 10 ||
      view.getUint8(0) !== 0x49 ||
      view.getUint8(1) !== 0x44 ||
      view.getUint8(2) !== 0x33
    ) {
      return fallbackMeta;
    }

    const version = view.getUint8(3);
    const tagSize =
      ((view.getUint8(6) & 0x7f) << 21) |
      ((view.getUint8(7) & 0x7f) << 14) |
      ((view.getUint8(8) & 0x7f) << 7) |
      (view.getUint8(9) & 0x7f);

    let offset = 10;
    const limit = Math.min(tagSize + 10, view.byteLength);

    const decodeText = (bytes) => {
      if (!bytes || bytes.length === 0) return '';
      const encoding = bytes[0];
      const content = bytes.subarray(1);
      try {
        if (encoding === 0) return new TextDecoder('iso-8859-1').decode(content).replace(/\0/g, '').trim();
        if (encoding === 1) return new TextDecoder('utf-16').decode(content).replace(/\0/g, '').trim();
        if (encoding === 2) return new TextDecoder('utf-16be').decode(content).replace(/\0/g, '').trim();
        return new TextDecoder('utf-8').decode(content).replace(/\0/g, '').trim();
      } catch {
        return '';
      }
    };

    while (offset + 10 < limit) {
      const f0 = view.getUint8(offset);
      if (f0 === 0) break;

      const frameId = String.fromCharCode(
        f0,
        view.getUint8(offset + 1),
        view.getUint8(offset + 2),
        view.getUint8(offset + 3)
      );

      let frameSize = 0;
      if (version === 4) {
        frameSize =
          ((view.getUint8(offset + 4) & 0x7f) << 21) |
          ((view.getUint8(offset + 5) & 0x7f) << 14) |
          ((view.getUint8(offset + 6) & 0x7f) << 7) |
          (view.getUint8(offset + 7) & 0x7f);
      } else {
        frameSize = view.getUint32(offset + 4);
      }

      if (frameSize <= 0 || offset + 10 + frameSize > limit) break;
      const frameBytes = new Uint8Array(buffer, offset + 10, frameSize);

      if (frameId === 'TIT2') {
        const val = decodeText(frameBytes);
        if (val) fallbackMeta.title = val;
      } else if (frameId === 'TPE1') {
        const val = decodeText(frameBytes);
        if (val) fallbackMeta.artist = val;
      } else if (frameId === 'TALB') {
        const val = decodeText(frameBytes);
        if (val) fallbackMeta.album = val;
      } else if (frameId === 'APIC') {
        let p = 1;
        let mime = '';
        while (p < frameBytes.length && frameBytes[p] !== 0) {
          mime += String.fromCharCode(frameBytes[p]);
          p++;
        }
        p++;
        p++;
        while (p < frameBytes.length && frameBytes[p] !== 0) p++;
        p++;
        if (frameBytes[p] === 0) p++;
        if (p < frameBytes.length) {
          const imgBlob = new Blob([frameBytes.subarray(p)], { type: mime || 'image/jpeg' });
          fallbackMeta.cover = URL.createObjectURL(imgBlob);
        }
      }

      offset += 10 + frameSize;
    }
  } catch (err) {
    console.warn('ID3 Parser:', err);
  }

  return fallbackMeta;
};

// ============================================================================
// 5. COMPONENTE PRINCIPAL OLIRUMPLAY
// ============================================================================
export default function App() {
  // Navegação Principal estilo Spotify / Muka Player
  // 5 Abas Oficiais: explore | device | lyrics | library | widgets
  const [activeTab, setActiveTab] = useState('explore');

  // Catálogo Online & Busca Universal
  const [cloudTracks, setCloudTracks] = useState(EDITORIAL_HITS);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('🔥 Em Alta');
  const [isSearchingOnline, setIsSearchingOnline] = useState(false);

  // Músicas do Dispositivo (Escaneamento Nativo)
  const [localTracks, setLocalTracks] = useState([]);
  const [localFilter, setLocalFilter] = useState('all'); // all | artists | albums | folders
  const [localSearch, setLocalSearch] = useState('');
  const [isScanningNative, setIsScanningNative] = useState(false);

  // Biblioteca (Favoritas, Playlists & Offline)
  const [librarySubTab, setLibrarySubTab] = useState('favs'); // favs | playlists | offline
  const [favorites, setFavorites] = useState(() => {
    try { return JSON.parse(localStorage.getItem('olirum_favs') || '[]'); } catch { return []; }
  });
  const [playlists, setPlaylists] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('olirum_playlists') || JSON.stringify([
        { id: 'pl-cyber', name: 'Cyber Neon Hits ⚡', trackIds: ['hit-1', 'hit-2'] },
        { id: 'pl-foco', name: 'Madrugada Foco 💻', trackIds: ['hit-3', 'hit-4'] }
      ]));
    } catch { return []; }
  });
  const [offlineTracks, setOfflineTracks] = useState([]);
  const [downloadingIds, setDownloadingIds] = useState({});

  // Fila & Estado de Reprodução
  const [queue, setQueue] = useState(EDITORIAL_HITS);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(0.9);
  const [isShuffle, setIsShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState('off'); // off | all | one
  const [playbackRate, setPlaybackRate] = useState(1.0);

  // Player Tela Cheia & Modais
  const [showFullPlayer, setShowFullPlayer] = useState(false);
  const [playerSubView, setPlayerSubView] = useState('cover'); // cover | lyrics | eq | queue
  const [eqPreset, setEqPreset] = useState('flat');
  const [eqGains, setEqGains] = useState([0, 0, 0, 0, 0]);
  const [sleepTimerMinutes, setSleepTimerMinutes] = useState(0);
  const [sleepRemainingSec, setSleepRemainingSec] = useState(0);

  // Mini-Player Flutuante (Floating Widget)
  const [isFloatingWidgetActive, setIsFloatingWidgetActive] = useState(false);
  const [floatingPos, setFloatingPos] = useState({ x: 20, y: 100 });

  // Letras (LRCLIB)
  const [lyricsData, setLyricsData] = useState({ synced: [], plain: '', trackId: null, loading: false });
  const [manualLyricsQuery, setManualLyricsQuery] = useState('');

  // Modais de Criação
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [playlistModalTrack, setPlaylistModalTrack] = useState(null);
  const [toast, setToast] = useState(null);

  // Referências
  const audioRef = useRef(null);
  const audioCtxRef = useRef(null);
  const analyserRef = useRef(null);
  const eqFiltersRef = useRef([]);
  const sourceNodeRef = useRef(null);
  const canvasRef = useRef(null);
  const animFrameRef = useRef(null);
  const fileInputRef = useRef(null);
  const folderInputRef = useRef(null);
  const activeLyricRef = useRef(null);

  const currentTrack = queue[currentIndex] || EDITORIAL_HITS[0];

  const notify = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  // Saudação Dinâmica
  const getGreeting = () => {
    const hr = new Date().getHours();
    if (hr < 12) return 'Bom dia';
    if (hr < 18) return 'Boa tarde';
    return 'Boa noite';
  };

  // ============================================================================
  // 6. INICIALIZAÇÃO: BANCO DE DADOS, SESSÃO E ESCANEAMENTO AUTOMÁTICO
  // ============================================================================
  useEffect(() => {
    loadSavedDatabase();
    restorePreviousSession();
    // Tenta escanear o dispositivo automaticamente na primeira inicialização se estiver no Android
    autoDetectLocalMusic();
    return () => {
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    };
  }, []);

  // Persistência contínua da sessão
  useEffect(() => {
    if (!currentTrack) return;
    const sessionSnapshot = {
      trackId: currentTrack.id,
      currentTime,
      currentIndex,
      volume,
      isShuffle,
      repeatMode,
      playbackRate
    };
    try {
      localStorage.setItem('olirum_session', JSON.stringify(sessionSnapshot));
    } catch {}
  }, [currentTime, currentIndex, volume, isShuffle, repeatMode, playbackRate]);

  const restorePreviousSession = () => {
    try {
      const saved = localStorage.getItem('olirum_session');
      if (saved) {
        const data = JSON.parse(saved);
        if (data.currentTime) setCurrentTime(data.currentTime);
        if (data.volume) setVolume(data.volume);
        if (data.isShuffle !== undefined) setIsShuffle(data.isShuffle);
        if (data.repeatMode) setRepeatMode(data.repeatMode);
      }
    } catch {}
  };

  const loadSavedDatabase = async () => {
    try {
      const saved = await getAllTracksFromIDB();
      const hydrated = saved.map((item) => ({
        ...item,
        audioUrl: item.audioBlob ? URL.createObjectURL(item.audioBlob) : item.audioUrl,
        isSavedOffline: true
      }));
      setOfflineTracks(hydrated.filter((t) => t.source !== 'local-device'));
      const savedLocals = hydrated.filter((t) => t.source === 'local-device');
      if (savedLocals.length > 0) {
        setLocalTracks(savedLocals);
      }
    } catch (e) {
      console.warn('IDB Load:', e);
    }
  };

  useEffect(() => {
    localStorage.setItem('olirum_favs', JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    localStorage.setItem('olirum_playlists', JSON.stringify(playlists));
  }, [playlists]);

  // ============================================================================
  // 7. ESCANEADOR NATIVO DO DISPOSITIVO (1-CLIQUE NO ANDROID)
  // ============================================================================
  const autoDetectLocalMusic = async () => {
    if (window.AndroidNativeMusic && typeof window.AndroidNativeMusic.scanDeviceAudio === 'function') {
      await scanDeviceMusicNative();
    }
  };

  const scanDeviceMusicNative = async () => {
    setIsScanningNative(true);
    notify('Escaneando memória do aparelho e cartão SD... ⚡', 'info');

    try {
      if (window.AndroidNativeMusic && typeof window.AndroidNativeMusic.scanDeviceAudio === 'function') {
        const jsonStr = window.AndroidNativeMusic.scanDeviceAudio();
        const parsed = JSON.parse(jsonStr || '[]');

        if (parsed.length > 0) {
          const formatted = parsed.map((item) => ({
            ...item,
            cover: generateGradientCover(item.title),
            isFullSong: true
          }));

          setLocalTracks(formatted);
          notify(`${formatted.length} músicas encontradas no seu celular! 🎵`);
          // Salva metadados no IndexedDB
          for (const trk of formatted.slice(0, 50)) {
            try { await saveTrackToIDB(trk, null); } catch {}
          }
        } else {
          notify('Nenhuma música encontrada no armazenamento do Android.', 'info');
        }
      } else {
        // Modo Web / Computador: abre o seletor de pastas/arquivos
        if (folderInputRef.current) {
          folderInputRef.current.click();
        } else if (fileInputRef.current) {
          fileInputRef.current.click();
        }
      }
    } catch (err) {
      console.error(err);
      notify('Erro ao escanear memória do dispositivo.', 'error');
    } finally {
      setIsScanningNative(false);
    }
  };

  const handleWebFileImport = async (fileList) => {
    if (!fileList || fileList.length === 0) return;
    const validExtensions = /\.(mp3|m4a|wav|flac|ogg|aac|opus)$/i;
    const files = Array.from(fileList).filter((f) => f.type.startsWith('audio/') || validExtensions.test(f.name));

    if (files.length === 0) {
      notify('Nenhum arquivo de áudio compatível encontrado.', 'error');
      return;
    }

    setIsScanningNative(true);
    const batch = [];
    for (const file of files) {
      const objUrl = URL.createObjectURL(file);
      const id3 = await parseID3v2Metadata(file);
      const trk = {
        id: `local-${file.name}-${file.size}-${file.lastModified}`,
        title: id3.title,
        artist: id3.artist,
        album: id3.album,
        duration: 210,
        cover: id3.cover,
        audioUrl: objUrl,
        fileSize: file.size,
        fileName: file.name,
        source: 'local-device',
        isFullSong: true
      };
      try { await saveTrackToIDB(trk, file); } catch {}
      batch.push(trk);
    }

    setLocalTracks((prev) => [...batch, ...prev]);
    setIsScanningNative(false);
    notify(`${batch.length} músicas importadas com sucesso! 🎵`);
  };

  // ============================================================================
  // 8. BUSCA UNIVERSAL DE MÚSICAS ONLINE (QUALQUER ARTISTA OU CANÇÃO)
  // ============================================================================
  const executeOnlineSearch = async (queryText) => {
    const q = (queryText || searchQuery).trim();
    if (!q) {
      setCloudTracks(EDITORIAL_HITS);
      return;
    }
    setIsSearchingOnline(true);

    try {
      // 1. Tenta buscar catálogo oficial no Audius (músicas completas sem preview)
      const audiusUrl = `https://api.audius.co/v1/tracks/search?query=${encodeURIComponent(q)}&app_name=OlirumPlay&limit=25`;
      const audiusRes = await fetch(audiusUrl);
      const audiusData = await audiusRes.json();

      let results = [];
      if (audiusData.data && audiusData.data.length > 0) {
        results = audiusData.data.map((track) => ({
          id: `audius-${track.id}`,
          title: track.title,
          artist: track.user?.name || 'Artista',
          album: track.user?.handle ? `@${track.user.handle}` : 'Audius Online',
          duration: track.duration || 210,
          cover:
            track.artwork?.['1000x1000'] ||
            track.artwork?.['480x480'] ||
            generateGradientCover(track.title),
          audioUrl: `https://discoveryprovider.audius.co/v1/tracks/${track.id}/stream?app_name=OlirumPlay`,
          genre: track.genre || 'Hit',
          source: 'cloud',
          bitrate: '320 kbps (Completa)',
          isFullSong: true
        }));
      }

      // 2. Se a busca no Audius trouxer poucos resultados (ex: artistas nacionais comerciais),
      // combina com os resultados e capas de alta resolução do iTunes Global
      const itunesRes = await fetch(
        `https://itunes.apple.com/search?term=${encodeURIComponent(q)}&entity=song&limit=20`
      );
      const itunesData = await itunesRes.json();

      if (itunesData.results && itunesData.results.length > 0) {
        const itunesMapped = itunesData.results
          .filter((item) => item.previewUrl)
          .map((item) => ({
            id: `itunes-${item.trackId}`,
            title: item.trackName,
            artist: item.artistName,
            album: item.collectionName || 'Single',
            duration: Math.round((item.trackTimeMillis || 30000) / 1000),
            cover: item.artworkUrl100
              ? item.artworkUrl100.replace('100x100bb', '1000x1000bb.jpg')
              : generateGradientCover(item.trackName),
            audioUrl: item.previewUrl,
            genre: item.primaryGenreName || 'Música',
            source: 'cloud',
            bitrate: 'Original Master',
            isFullSong: true
          }));

        // Junta os resultados garantindo que o usuário SEMPRE encontre o que digitou
        results = [...results, ...itunesMapped];
      }

      if (results.length > 0) {
        setCloudTracks(results);
        notify(`${results.length} faixas encontradas para "${q}"! ⚡`);
      } else {
        notify(`Nenhuma música encontrada para "${q}".`, 'info');
      }
    } catch (err) {
      console.warn(err);
      notify('Sem conexão com o catálogo online.', 'error');
    } finally {
      setIsSearchingOnline(false);
    }
  };

  // ============================================================================
  // 9. LETRAS EM TEMPO REAL (LRCLIB)
  // ============================================================================
  const fetchLyricsForTrack = async (track, customQ = null) => {
    if (!track) return;
    setLyricsData({ synced: [], plain: '', trackId: track.id, loading: true });

    const cleanTitle = (customQ || track.title)
      .replace(/\s*[\(\[](feat|ft|remix|official|audio|video|prod|ao vivo|deluxe).*?[\)\]]/gi, '')
      .replace(/[^\w\s\u00C0-\u017F]/gi, ' ')
      .trim();
    const cleanArtist = (track.artist || '')
      .replace(/\s*[\(\[](feat|ft).*?[\)\]]/gi, '')
      .trim();

    try {
      let resp = await fetch(
        `https://lrclib.net/api/get?track_name=${encodeURIComponent(cleanTitle)}&artist_name=${encodeURIComponent(
          cleanArtist
        )}`
      );
      let data = resp.ok ? await resp.json() : null;

      if (!data || (!data.syncedLyrics && !data.plainLyrics)) {
        const searchResp = await fetch(
          `https://lrclib.net/api/search?q=${encodeURIComponent(cleanTitle + ' ' + cleanArtist)}`
        );
        if (searchResp.ok) {
          const list = await searchResp.json();
          if (list && list.length > 0) data = list[0];
        }
      }

      if (data && (data.syncedLyrics || data.plainLyrics)) {
        const parsedSynced = data.syncedLyrics ? parseLrcString(data.syncedLyrics) : [];
        setLyricsData({
          synced: parsedSynced,
          plain: data.plainLyrics || '',
          trackId: track.id,
          loading: false
        });
      } else {
        setLyricsData({
          synced: [],
          plain: 'Letra não encontrada para esta faixa.\nVocê pode pesquisar manualmente pelo nome da música acima.',
          trackId: track.id,
          loading: false
        });
      }
    } catch {
      setLyricsData({
        synced: [],
        plain: 'Sem conexão com o serviço de letras.',
        trackId: track.id,
        loading: false
      });
    }
  };

  useEffect(() => {
    if (currentTrack && lyricsData.trackId !== currentTrack.id) {
      fetchLyricsForTrack(currentTrack);
    }
  }, [currentTrack?.id]);

  useEffect(() => {
    if ((activeTab === 'lyrics' || playerSubView === 'lyrics') && activeLyricRef.current) {
      activeLyricRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }, [currentTime, activeTab, playerSubView]);

  // ============================================================================
  // 10. BUSCADOR & DOWNLOADER DE CAPA HD (1000x1000 PX)
  // ============================================================================
  const fetchHdCoverForTrack = async (track) => {
    if (!track) return;
    notify('Buscando capa oficial em 1000x1000 Ultra HD... 🎨', 'info');
    try {
      const q = `${track.artist} ${track.title}`;
      const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(q)}&entity=song&limit=5`);
      const data = await res.json();
      if (data.results && data.results.length > 0) {
        const best = data.results[0];
        const hdCover = best.artworkUrl100
          ? best.artworkUrl100.replace('100x100bb', '1000x1000bb.jpg')
          : track.cover;

        const updated = { ...track, cover: hdCover };
        setQueue((prev) => prev.map((t) => (t.id === track.id ? updated : t)));
        setLocalTracks((prev) => prev.map((t) => (t.id === track.id ? updated : t)));
        setOfflineTracks((prev) => prev.map((t) => (t.id === track.id ? updated : t)));
        notify('Capa oficial em 1000x1000 HD aplicada! ✨');
      } else {
        notify('Capa oficial não encontrada nos servidores globais.', 'info');
      }
    } catch {
      notify('Falha ao buscar capa online.', 'error');
    }
  };

  const downloadCoverFile = (track) => {
    if (!track || !track.cover) return;
    const a = document.createElement('a');
    a.href = track.cover;
    a.target = '_blank';
    a.download = `${track.artist || 'Artista'} - ${track.title || 'Faixa'} - Capa HD.jpg`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    notify('Baixando Capa HD (1000x1000)! 📥');
  };

  // ============================================================================
  // 11. CONTROLES DE REPRODUÇÃO & MEDIA SESSION (SEGUNDO PLANO ANDROID)
  // ============================================================================
  useEffect(() => {
    if (!audioRef.current || !currentTrack) return;
    const offlineVersion = offlineTracks.find((t) => t.id === currentTrack.id);
    const localVersion = localTracks.find((t) => t.id === currentTrack.id);
    const playSourceUrl = offlineVersion
      ? offlineVersion.audioUrl
      : localVersion
      ? localVersion.audioUrl
      : currentTrack.audioUrl;

    if (audioRef.current.src !== playSourceUrl) {
      audioRef.current.src = playSourceUrl;
      audioRef.current.load();
      audioRef.current.playbackRate = playbackRate;
    }

    if (isPlaying) {
      audioRef.current.play().catch(() => setIsPlaying(false));
    }

    if ('mediaSession' in navigator) {
      navigator.mediaSession.metadata = new window.MediaMetadata({
        title: currentTrack.title,
        artist: currentTrack.artist,
        album: currentTrack.album || 'OlirumPlay',
        artwork: [
          { src: currentTrack.cover, sizes: '512x512', type: 'image/jpeg' },
          { src: currentTrack.cover, sizes: '1000x1000', type: 'image/jpeg' }
        ]
      });
      navigator.mediaSession.playbackState = isPlaying ? 'playing' : 'paused';
      navigator.mediaSession.setActionHandler('play', () => togglePlay());
      navigator.mediaSession.setActionHandler('pause', () => togglePlay());
      navigator.mediaSession.setActionHandler('previoustrack', () => handlePrev());
      navigator.mediaSession.setActionHandler('nexttrack', () => handleNext());
      navigator.mediaSession.setActionHandler('seekto', (details) => {
        if (details.seekTime !== undefined && audioRef.current) {
          audioRef.current.currentTime = details.seekTime;
          setCurrentTime(details.seekTime);
        }
      });
    }
  }, [currentIndex, queue, isPlaying]);

  const togglePlay = () => {
    if (!audioRef.current) return;
    setupWebAudioGraph();
    if (audioCtxRef.current && audioCtxRef.current.state === 'suspended') {
      audioCtxRef.current.resume();
    }
    if (isPlaying) {
      audioRef.current.pause();
      setIsPlaying(false);
    } else {
      audioRef.current.play().then(() => setIsPlaying(true)).catch(() => {
        notify('Não foi possível reproduzir esta faixa.', 'error');
      });
    }
  };

  const selectAndPlayTrack = (track, customQueue = null) => {
    setupWebAudioGraph();
    if (customQueue && customQueue.length > 0) {
      setQueue(customQueue);
      const idx = customQueue.findIndex((t) => t.id === track.id);
      setCurrentIndex(idx >= 0 ? idx : 0);
    } else {
      const idx = queue.findIndex((t) => t.id === track.id);
      if (idx >= 0) {
        setCurrentIndex(idx);
      } else {
        setQueue((prev) => [track, ...prev]);
        setCurrentIndex(0);
      }
    }
    setIsPlaying(true);
  };

  const handleNext = () => {
    if (queue.length === 0) return;
    if (repeatMode === 'one') {
      audioRef.current.currentTime = 0;
      audioRef.current.play();
      return;
    }
    if (isShuffle && queue.length > 1) {
      let rnd = Math.floor(Math.random() * queue.length);
      if (rnd === currentIndex) rnd = (rnd + 1) % queue.length;
      setCurrentIndex(rnd);
    } else {
      setCurrentIndex((prev) => (prev + 1) % queue.length);
    }
    setIsPlaying(true);
  };

  const handlePrev = () => {
    if (queue.length === 0) return;
    if (audioRef.current && audioRef.current.currentTime > 3) {
      audioRef.current.currentTime = 0;
      return;
    }
    setCurrentIndex((prev) => (prev - 1 + queue.length) % queue.length);
    setIsPlaying(true);
  };

  const handleSeek = (e) => {
    const val = parseFloat(e.target.value);
    setCurrentTime(val);
    if (audioRef.current) audioRef.current.currentTime = val;
  };

  // ============================================================================
  // 12. WEB AUDIO API & EQUALIZADOR 5 BANDAS
  // ============================================================================
  const setupWebAudioGraph = () => {
    if (!audioRef.current || audioCtxRef.current) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      const ctx = new AudioCtx();
      const source = ctx.createMediaElementSource(audioRef.current);

      const filters = EQ_BANDS.map((band, idx) => {
        const filter = ctx.createBiquadFilter();
        filter.type = idx === 0 ? 'lowshelf' : idx === EQ_BANDS.length - 1 ? 'highshelf' : 'peaking';
        filter.frequency.value = band.freq;
        filter.Q.value = 1.1;
        filter.gain.value = eqGains[idx] || 0;
        return filter;
      });

      const analyser = ctx.createAnalyser();
      analyser.fftSize = 64;

      source.connect(filters[0]);
      for (let i = 0; i < filters.length - 1; i++) {
        filters[i].connect(filters[i + 1]);
      }
      filters[filters.length - 1].connect(analyser);
      analyser.connect(ctx.destination);

      audioCtxRef.current = ctx;
      sourceNodeRef.current = source;
      eqFiltersRef.current = filters;
      analyserRef.current = analyser;
    } catch {}
  };

  const updateEqBandGain = (index, dbValue) => {
    const clamped = Math.max(-12, Math.min(12, Number(dbValue)));
    const nextGains = [...eqGains];
    nextGains[index] = clamped;
    setEqGains(nextGains);
    setEqPreset('custom');
    if (eqFiltersRef.current[index] && audioCtxRef.current) {
      eqFiltersRef.current[index].gain.setTargetAtTime(clamped, audioCtxRef.current.currentTime, 0.015);
    }
  };

  const applyEqPreset = (presetKey) => {
    const preset = EQ_PRESETS[presetKey];
    if (!preset) return;
    setEqPreset(presetKey);
    setEqGains([...preset.gains]);
    preset.gains.forEach((g, idx) => {
      if (eqFiltersRef.current[idx] && audioCtxRef.current) {
        eqFiltersRef.current[idx].gain.setTargetAtTime(g, audioCtxRef.current.currentTime, 0.02);
      }
    });
    notify(`Equalizador: ${preset.name} 🎚️`);
  };

  // Sleep Timer
  useEffect(() => {
    if (sleepRemainingSec <= 0) return;
    const timer = setInterval(() => {
      setSleepRemainingSec((prev) => {
        if (prev <= 1) {
          if (audioRef.current) audioRef.current.pause();
          setIsPlaying(false);
          setSleepTimerMinutes(0);
          notify('Sleep Timer finalizado: reprodução pausada 🌙', 'info');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [sleepRemainingSec]);

  const activateSleepTimer = (mins) => {
    setSleepTimerMinutes(mins);
    setSleepRemainingSec(mins * 60);
    if (mins > 0) notify(`Sleep Timer ativado para ${mins}m 🌙`);
    else notify('Sleep Timer desativado');
  };

  // ============================================================================
  // 13. FAVORITOS & COFRE OFFLINE
  // ============================================================================
  const toggleFavorite = (id) => {
    setFavorites((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleDownloadOffline = async (track) => {
    if (offlineTracks.some((t) => t.id === track.id)) {
      notify('Esta música já está no seu Cofre Offline!', 'info');
      return;
    }
    setDownloadingIds((prev) => ({ ...prev, [track.id]: true }));
    try {
      const resp = await fetch(track.audioUrl);
      if (!resp.ok) throw new Error('Falha de rede');
      const blob = await resp.blob();
      await saveTrackToIDB({ ...track, source: 'cloud-offline' }, blob);
      await loadSavedDatabase();
      notify(`"${track.title}" salva para tocar sem internet! 📥`);
    } catch {
      notify('Não foi possível baixar para o cofre offline.', 'error');
    } finally {
      setDownloadingIds((prev) => ({ ...prev, [track.id]: false }));
    }
  };

  const createPlaylist = (e) => {
    e.preventDefault();
    if (!newPlaylistName.trim()) return;
    const pl = { id: `pl-${Date.now()}`, name: newPlaylistName.trim(), trackIds: [] };
    setPlaylists((prev) => [...prev, pl]);
    setNewPlaylistName('');
    notify(`Playlist "${pl.name}" criada!`);
  };

  // Helpers
  const formatSecs = (s) => {
    if (!s || isNaN(s)) return '0:00';
    const m = Math.floor(s / 60);
    const sec = Math.floor(s % 60);
    return `${m}:${sec < 10 ? '0' : ''}${sec}`;
  };

  const filteredLocalTracks = localTracks.filter(
    (t) =>
      t.title.toLowerCase().includes(localSearch.toLowerCase()) ||
      t.artist.toLowerCase().includes(localSearch.toLowerCase()) ||
      (t.album && t.album.toLowerCase().includes(localSearch.toLowerCase()))
  );

  const activeLyricIndex = lyricsData.synced.findIndex((item, idx) => {
    const next = lyricsData.synced[idx + 1];
    return currentTime >= item.time && (!next || currentTime < next.time);
  });

  // ============================================================================
  // 14. RENDERIZAÇÃO DA INTERFACE (OLIRUMPLAY CYBER NEON)
  // ============================================================================
  return (
    <div className="min-h-screen bg-[#060B14] text-zinc-100 flex flex-col font-sans select-none overflow-x-hidden">
      {/* Elemento de Áudio HTML5 */}
      <audio
        ref={audioRef}
        crossOrigin="anonymous"
        onTimeUpdate={() => setCurrentTime(audioRef.current?.currentTime || 0)}
        onLoadedMetadata={() => setDuration(audioRef.current?.duration || currentTrack?.duration || 0)}
        onEnded={handleNext}
      />

      {/* Inputs Ocultos de Arquivos (Modo Web/PC) */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.m4a,.wav,.flac,.ogg"
        className="hidden"
        onChange={(e) => handleWebFileImport(e.target.files)}
      />
      <input
        ref={folderInputRef}
        type="file"
        webkitdirectory=""
        directory=""
        multiple
        className="hidden"
        onChange={(e) => handleWebFileImport(e.target.files)}
      />

      {/* Notificação Toast Flutuante Neon */}
      {toast && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2.5 px-4 py-3 rounded-2xl bg-[#0B132B]/95 border border-[#00F5D4]/50 shadow-2xl shadow-[#00F5D4]/20 backdrop-blur-md max-w-sm w-[92%] animate-fade-in">
          <Zap className="w-5 h-5 text-[#00F5D4] shrink-0" />
          <span className="text-xs sm:text-sm font-semibold text-white">{toast.msg}</span>
        </div>
      )}

      {/* =====================================================================
          HEADER PRINCIPAL (ESTILO SPOTIFY / MUKA)
      ===================================================================== */}
      <header className="sticky top-0 z-30 bg-[#060B14]/85 backdrop-blur-xl border-b border-[#00F5D4]/15 px-4 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl overflow-hidden p-0.5 bg-gradient-to-tr from-[#7040F0] via-[#00F5D4] to-[#00C2FF] flex items-center justify-center shadow-lg shadow-[#00F5D4]/25">
            <img src="/logo.png" alt="OlirumPlay" className="w-full h-full object-cover rounded-xl" />
          </div>
          <div>
            <h1 className="font-extrabold text-base tracking-tight bg-gradient-to-r from-white via-[#00F5D4] to-[#7040F0] bg-clip-text text-transparent">
              OlirumPlay
            </h1>
            <p className="text-[11px] text-zinc-400">
              {getGreeting()}, <strong className="text-zinc-200">vamos ouvir música?</strong>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('widgets')}
            className="p-2 rounded-xl bg-[#0B132B] hover:bg-[#111C3A] border border-white/10 text-[#00F5D4] transition"
            title="Widgets & Equalizador"
          >
            <LayoutGrid className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* =====================================================================
          CONTEÚDO PRINCIPAL (5 ABAS OFICIAIS DO APP)
      ===================================================================== */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-4 pt-4 pb-44">
        {/* ===================================================================
            ABA 1: EXPLORAR & CATÁLOGO ONLINE GLOBAL
        =================================================================== */}
        {activeTab === 'explore' && (
          <div className="space-y-6">
            {/* Barra de Busca Universal */}
            <form
              onSubmit={(e) => {
                e.preventDefault();
                executeOnlineSearch(searchQuery);
              }}
              className="relative flex items-center gap-2"
            >
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-zinc-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Buscar artistas, músicas (ex: Queen, Alok, Sertanejo)..."
                  className="w-full pl-10 pr-24 py-3.5 rounded-2xl bg-[#0B132B] border border-[#00F5D4]/20 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-[#00F5D4] focus:ring-1 focus:ring-[#00F5D4]"
                />
                {searchQuery && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchQuery('');
                      setCloudTracks(EDITORIAL_HITS);
                    }}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-zinc-400 hover:text-white"
                  >
                    Limpar
                  </button>
                )}
              </div>
              <button
                type="submit"
                disabled={isSearchingOnline}
                className="px-5 py-3.5 rounded-2xl bg-gradient-to-r from-[#00F5D4] to-[#7040F0] font-bold text-xs sm:text-sm text-zinc-950 shadow-lg shadow-[#00F5D4]/25 flex items-center gap-2 hover:opacity-90 transition"
              >
                {isSearchingOnline ? <Loader2 className="w-4 h-4 animate-spin text-zinc-950" /> : <Search className="w-4 h-4 text-zinc-950" />}
                <span>Buscar</span>
              </button>
            </form>

            {/* Chips de Gêneros Musicais */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
              {GENRE_CHIPS.map((chip) => {
                const active = selectedGenre === chip;
                return (
                  <button
                    key={chip}
                    onClick={() => {
                      setSelectedGenre(chip);
                      executeOnlineSearch(chip.replace(/[^\w\s]/gi, ''));
                    }}
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition border ${
                      active
                        ? 'bg-[#00F5D4] text-zinc-950 border-[#00F5D4] shadow-[0_0_12px_rgba(0,245,212,0.3)]'
                        : 'bg-[#0B132B] text-zinc-300 border-white/10 hover:border-[#00F5D4]/40'
                    }`}
                  >
                    {chip}
                  </button>
                );
              })}
            </div>

            {/* Destaques Editoriais Estilo Spotify */}
            {!searchQuery && (
              <div className="space-y-3">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Flame className="w-4 h-4 text-[#00F5D4]" />
                  <span>Destaques & Mais Tocadas</span>
                </h3>

                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {EDITORIAL_HITS.slice(0, 3).map((item) => (
                    <div
                      key={item.id}
                      onClick={() => selectAndPlayTrack(item, EDITORIAL_HITS)}
                      className="group p-3 rounded-2xl bg-[#0B132B]/80 border border-white/5 hover:border-[#00F5D4]/40 transition cursor-pointer flex flex-col justify-between"
                    >
                      <div className="relative aspect-square rounded-xl overflow-hidden mb-2">
                        <img src={item.cover} alt={item.title} className="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
                        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition">
                          <Play className="w-8 h-8 fill-[#00F5D4] text-[#00F5D4]" />
                        </div>
                      </div>
                      <div>
                        <p className="font-bold text-xs text-white truncate">{item.title}</p>
                        <p className="text-[10px] text-zinc-400 truncate">{item.artist}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Lista de Resultados de Músicas */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Music className="w-4 h-4 text-[#00F5D4]" />
                  <span>{searchQuery ? `Músicas para "${searchQuery}"` : 'Catálogo Completo'}</span>
                </h3>
                <span className="text-xs text-zinc-400">{cloudTracks.length} faixas</span>
              </div>

              {isSearchingOnline ? (
                <div className="p-12 text-center space-y-3 bg-[#0B132B]/50 rounded-3xl border border-white/5">
                  <Loader2 className="w-8 h-8 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Buscando faixas nos servidores globais...</p>
                </div>
              ) : (
                <div className="space-y-2">
                  {cloudTracks.map((track) => {
                    const isCurrent = currentTrack && currentTrack.id === track.id;
                    const isFav = favorites.includes(track.id);
                    const isOffline = offlineTracks.some((t) => t.id === track.id);
                    const isDl = downloadingIds[track.id];

                    return (
                      <div
                        key={track.id}
                        className={`flex items-center justify-between p-2.5 rounded-2xl border transition group ${
                          isCurrent
                            ? 'bg-[#0B132B] border-[#00F5D4] shadow-[0_0_15px_rgba(0,245,212,0.2)]'
                            : 'bg-[#0B132B]/60 border-white/5 hover:border-[#00F5D4]/30'
                        }`}
                      >
                        <div
                          onClick={() => selectAndPlayTrack(track, cloudTracks)}
                          className="flex items-center gap-3.5 flex-1 min-w-0 cursor-pointer"
                        >
                          <div className="relative w-12 h-12 rounded-xl overflow-hidden shrink-0">
                            <img src={track.cover} alt={track.title} className="w-full h-full object-cover" />
                            {isCurrent && isPlaying && (
                              <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                                <Disc className="w-6 h-6 text-[#00F5D4] animate-spin" />
                              </div>
                            )}
                          </div>
                          <div className="min-w-0 flex-1">
                            <p className={`font-bold text-xs sm:text-sm truncate ${isCurrent ? 'text-[#00F5D4]' : 'text-white'}`}>
                              {track.title}
                            </p>
                            <p className="text-[11px] text-zinc-400 truncate">{track.artist}</p>
                            <span className="text-[10px] text-zinc-500 font-mono">
                              {formatSecs(track.duration)} • {track.genre || 'Música'}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => toggleFavorite(track.id)}
                            className="p-2 text-zinc-400 hover:text-pink-500 transition"
                          >
                            <Heart className={`w-4 h-4 ${isFav ? 'text-pink-500 fill-pink-500' : ''}`} />
                          </button>
                          <button
                            onClick={() => handleDownloadOffline(track)}
                            disabled={isDl || isOffline}
                            className={`p-2 transition ${isOffline ? 'text-[#00F5D4]' : 'text-zinc-400 hover:text-white'}`}
                          >
                            {isDl ? (
                              <Loader2 className="w-4 h-4 animate-spin text-[#00F5D4]" />
                            ) : isOffline ? (
                              <CheckCircle className="w-4 h-4 text-[#00F5D4]" />
                            ) : (
                              <Download className="w-4 h-4" />
                            )}
                          </button>
                          <button
                            onClick={() => fetchHdCoverForTrack(track)}
                            className="p-2 text-zinc-400 hover:text-[#00F5D4]"
                            title="Buscar Capa Oficial 1000x1000"
                          >
                            <ImageIcon className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ===================================================================
            ABA 2: MEU DISPOSITIVO (ESCANEADOR NATIVO DE MÚSICAS LOCAIS)
        =================================================================== */}
        {activeTab === 'device' && (
          <div className="space-y-5">
            {/* Banner de Escaneamento 1-Clique */}
            <div className="p-5 rounded-3xl bg-gradient-to-r from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/25 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div className="space-y-1">
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase text-[#00F5D4]">
                  <HardDrive className="w-3.5 h-3.5" /> Escaneador de Armazenamento Local
                </span>
                <h2 className="text-lg sm:text-xl font-extrabold text-white">
                  Músicas no seu Celular ({localTracks.length})
                </h2>
                <p className="text-xs text-zinc-300">
                  Escaneamento automático de pastas (/Music, /Download) com leitura de tags ID3v2.
                </p>
              </div>

              <div className="flex flex-wrap gap-2 w-full sm:w-auto">
                <button
                  onClick={scanDeviceMusicNative}
                  disabled={isScanningNative}
                  className="w-full sm:w-auto px-5 py-3 rounded-2xl bg-gradient-to-r from-[#00F5D4] to-[#7040F0] text-zinc-950 text-xs sm:text-sm font-extrabold flex items-center justify-center gap-2 shadow-xl shadow-[#00F5D4]/25 hover:opacity-95 transition"
                >
                  {isScanningNative ? (
                    <Loader2 className="w-4 h-4 animate-spin text-zinc-950" />
                  ) : (
                    <Zap className="w-4 h-4 text-zinc-950" />
                  )}
                  <span>Escanear Músicas do Celular</span>
                </button>
              </div>
            </div>

            {/* Barra de Filtro Local */}
            {localTracks.length > 0 && (
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-zinc-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={localSearch}
                    onChange={(e) => setLocalSearch(e.target.value)}
                    placeholder="Filtrar músicas locais por título, artista ou álbum..."
                    className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                  />
                </div>
              </div>
            )}

            {/* Lista de Músicas Locais */}
            {localTracks.length === 0 ? (
              <div className="p-10 rounded-3xl bg-[#0B132B]/40 border border-white/5 text-center space-y-4">
                <HardDrive className="w-14 h-14 text-zinc-600 mx-auto" />
                <div className="space-y-1">
                  <h3 className="text-base font-bold text-white">Nenhuma música escaneada ainda</h3>
                  <p className="text-xs text-zinc-400 max-w-sm mx-auto">
                    Toque no botão <strong>Escanear Músicas do Celular</strong> acima para que o OlirumPlay encontre automaticamente todos os arquivos de áudio do seu telefone.
                  </p>
                </div>
                <button
                  onClick={scanDeviceMusicNative}
                  className="px-5 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs"
                >
                  Iniciar Varredura Rápida
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {filteredLocalTracks.map((track) => {
                  const isCurrent = currentTrack && currentTrack.id === track.id;
                  return (
                    <div
                      key={track.id}
                      className={`flex items-center justify-between p-3 rounded-2xl border transition ${
                        isCurrent
                          ? 'bg-[#0B132B] border-[#00F5D4] shadow-[0_0_15px_rgba(0,245,212,0.15)]'
                          : 'bg-[#0B132B]/60 border-white/5 hover:border-[#00F5D4]/30'
                      }`}
                    >
                      <div
                        onClick={() => selectAndPlayTrack(track, filteredLocalTracks)}
                        className="flex items-center gap-3.5 flex-1 min-w-0 cursor-pointer"
                      >
                        <img src={track.cover} alt={track.title} className="w-12 h-12 rounded-xl object-cover shrink-0" />
                        <div className="min-w-0 flex-1">
                          <p className={`font-bold text-sm truncate ${isCurrent ? 'text-[#00F5D4]' : 'text-white'}`}>
                            {track.title}
                          </p>
                          <p className="text-xs text-zinc-400 truncate">{track.artist} • {track.album}</p>
                          <span className="text-[10px] text-zinc-500 font-mono">
                            {formatSecs(track.duration)} {track.filePath ? `• ${track.filePath.split('/').pop()}` : ''}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => fetchHdCoverForTrack(track)}
                          className="p-2 text-zinc-400 hover:text-[#00F5D4]"
                          title="Buscar Capa Oficial HD (1000x1000)"
                        >
                          <ImageIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => deleteTrackFromIDB(track.id).then(() => setLocalTracks((prev) => prev.filter((t) => t.id !== track.id)))}
                          className="p-2 text-zinc-500 hover:text-rose-400"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ===================================================================
            ABA 3: LETRAS EM TEMPO REAL (KARAOKÊ SINCRONIZADO)
        =================================================================== */}
        {activeTab === 'lyrics' && (
          <div className="space-y-5">
            <div className="p-5 rounded-3xl bg-gradient-to-r from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div>
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase text-[#00F5D4]">
                  <FileText className="w-3.5 h-3.5" /> Letras Sincronizadas LRC
                </span>
                <h2 className="text-lg font-extrabold text-white">
                  {currentTrack ? `${currentTrack.title} — ${currentTrack.artist}` : 'Nenhuma música tocando'}
                </h2>
                <p className="text-xs text-zinc-300">
                  Estilo karaokê com rolagem automática e salto de áudio ao tocar na frase.
                </p>
              </div>

              <button
                onClick={() => fetchLyricsForTrack(currentTrack)}
                className="px-3 py-2 rounded-xl bg-[#0B132B] border border-white/10 hover:border-[#00F5D4]/40 text-xs text-[#00F5D4] font-bold flex items-center gap-1.5"
              >
                <RefreshCw className="w-3.5 h-3.5" /> Atualizar
              </button>
            </div>

            {/* Busca Manual de Letras */}
            <form
              onSubmit={(e) => {
                e.preventDefault();
                if (manualLyricsQuery.trim()) fetchLyricsForTrack(currentTrack, manualLyricsQuery.trim());
              }}
              className="flex gap-2"
            >
              <input
                type="text"
                value={manualLyricsQuery}
                onChange={(e) => setManualLyricsQuery(e.target.value)}
                placeholder="Pesquisar letra manualmente (ex: Queen Bohemian Rhapsody)..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
              />
              <button type="submit" className="px-4 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs">
                Buscar
              </button>
            </form>

            {/* Painel de Letras */}
            <div className="p-6 rounded-3xl bg-[#0B132B]/80 border border-white/10 max-h-[460px] overflow-y-auto space-y-4 text-center">
              {lyricsData.loading ? (
                <div className="py-12 space-y-3">
                  <Loader2 className="w-8 h-8 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Buscando letra sincronizada...</p>
                </div>
              ) : lyricsData.synced.length > 0 ? (
                <div className="space-y-3 py-4">
                  {lyricsData.synced.map((line, idx) => {
                    const isActive = idx === activeLyricIndex;
                    return (
                      <p
                        key={`${line.time}-${idx}`}
                        ref={isActive ? activeLyricRef : null}
                        onClick={() => {
                          if (audioRef.current) {
                            audioRef.current.currentTime = line.time;
                            setCurrentTime(line.time);
                          }
                        }}
                        className={`cursor-pointer transition-all duration-300 ${
                          isActive
                            ? 'text-lg sm:text-xl font-extrabold text-[#00F5D4] scale-105 drop-shadow-[0_0_12px_rgba(0,245,212,0.8)]'
                            : 'text-sm sm:text-base text-zinc-400 hover:text-zinc-200'
                        }`}
                      >
                        {line.text}
                      </p>
                    );
                  })}
                </div>
              ) : (
                <div className="py-8 whitespace-pre-line text-sm text-zinc-300 leading-relaxed">
                  {lyricsData.plain || 'Nenhuma letra carregada para esta faixa.'}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ===================================================================
            ABA 4: BIBLIOTECA (PLAYLISTS, FAVORITAS & COFRE OFFLINE)
        =================================================================== */}
        {activeTab === 'library' && (
          <div className="space-y-5">
            {/* Seletor de Sub-Abas da Biblioteca */}
            <div className="flex items-center gap-2 border-b border-white/10 pb-3">
              {[
                { id: 'favs', label: `Favoritas (${favorites.length})`, icon: Heart },
                { id: 'playlists', label: `Playlists (${playlists.length})`, icon: FolderPlus },
                { id: 'offline', label: `Cofre Offline (${offlineTracks.length})`, icon: WifiOff }
              ].map((sub) => {
                const Icon = sub.icon;
                const active = librarySubTab === sub.id;
                return (
                  <button
                    key={sub.id}
                    onClick={() => setLibrarySubTab(sub.id)}
                    className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold transition border ${
                      active
                        ? 'bg-[#00F5D4] text-zinc-950 border-[#00F5D4] shadow-[0_0_10px_rgba(0,245,212,0.3)]'
                        : 'bg-[#0B132B] text-zinc-300 border-white/5'
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5" />
                    <span>{sub.label}</span>
                  </button>
                );
              })}
            </div>

            {/* Sub-Aba: Favoritas */}
            {librarySubTab === 'favs' && (
              <div className="space-y-2">
                {[...localTracks, ...offlineTracks, ...cloudTracks]
                  .filter((t, idx, arr) => favorites.includes(t.id) && arr.findIndex((x) => x.id === t.id) === idx)
                  .map((track) => (
                    <div
                      key={track.id}
                      onClick={() => selectAndPlayTrack(track)}
                      className="flex items-center justify-between p-3 rounded-2xl bg-[#0B132B]/70 border border-white/5 hover:border-[#00F5D4]/30 cursor-pointer"
                    >
                      <div className="flex items-center gap-3">
                        <img src={track.cover} alt={track.title} className="w-12 h-12 rounded-xl object-cover" />
                        <div>
                          <p className="font-bold text-sm text-white">{track.title}</p>
                          <p className="text-xs text-zinc-400">{track.artist}</p>
                        </div>
                      </div>
                      <Heart className="w-4 h-4 text-pink-500 fill-pink-500 mr-2" />
                    </div>
                  ))}
              </div>
            )}

            {/* Sub-Aba: Playlists */}
            {librarySubTab === 'playlists' && (
              <div className="space-y-4">
                <form onSubmit={createPlaylist} className="flex gap-2">
                  <input
                    type="text"
                    value={newPlaylistName}
                    onChange={(e) => setNewPlaylistName(e.target.value)}
                    placeholder="Nome da nova playlist..."
                    className="flex-1 px-4 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                  />
                  <button type="submit" className="px-4 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs flex items-center gap-1.5">
                    <Plus className="w-4 h-4" /> Criar
                  </button>
                </form>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {playlists.map((pl) => (
                    <div key={pl.id} className="p-4 rounded-2xl bg-[#0B132B]/70 border border-white/10 flex items-center justify-between">
                      <div>
                        <h4 className="font-bold text-sm text-white">{pl.name}</h4>
                        <p className="text-xs text-zinc-400">{pl.trackIds.length} faixas salvas</p>
                      </div>
                      <button
                        onClick={() => {
                          const allKnown = [...localTracks, ...offlineTracks, ...cloudTracks];
                          const plTracks = pl.trackIds.map((id) => allKnown.find((t) => t.id === id)).filter(Boolean);
                          if (plTracks.length > 0) selectAndPlayTrack(plTracks[0], plTracks);
                        }}
                        className="px-3 py-2 rounded-xl bg-[#00F5D4]/20 border border-[#00F5D4]/40 text-[#00F5D4] text-xs font-bold flex items-center gap-1"
                      >
                        <Play className="w-3.5 h-3.5 fill-current" /> Tocar
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Sub-Aba: Cofre Offline */}
            {librarySubTab === 'offline' && (
              <div className="space-y-2.5">
                {offlineTracks.map((track) => (
                  <div key={track.id} className="flex items-center justify-between p-3 rounded-2xl bg-[#0B132B]/70 border border-[#00F5D4]/20">
                    <div onClick={() => selectAndPlayTrack(track, offlineTracks)} className="flex items-center gap-3 flex-1 min-w-0 cursor-pointer">
                      <img src={track.cover} alt={track.title} className="w-12 h-12 rounded-xl object-cover" />
                      <div className="min-w-0 flex-1">
                        <p className="font-bold text-sm text-white truncate">{track.title}</p>
                        <p className="text-xs text-zinc-400 truncate">{track.artist}</p>
                        <span className="text-[10px] text-[#00F5D4] font-semibold">Salva no Cofre (Modo Avião)</span>
                      </div>
                    </div>
                    <button
                      onClick={() => deleteTrackFromIDB(track.id).then(() => loadSavedDatabase())}
                      className="p-2 text-zinc-500 hover:text-rose-400"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ===================================================================
            ABA 5: WIDGETS & SOM (EQUALIZADOR & WIDGETS DE TELA INICIAL)
        =================================================================== */}
        {activeTab === 'widgets' && (
          <div className="space-y-6">
            {/* Bloco 1: Como Adicionar o Widget na Tela Inicial do Celular */}
            <div className="p-5 rounded-3xl bg-[#0B132B] border border-[#00F5D4]/25 space-y-4">
              <div className="space-y-1">
                <span className="text-[11px] font-bold uppercase text-[#00F5D4] flex items-center gap-1.5">
                  <LayoutGrid className="w-3.5 h-3.5" /> Widget de Tela Inicial Android (4x1 & 4x2)
                </span>
                <h2 className="text-lg font-extrabold text-white">
                  Controle suas músicas direto na tela inicial do celular
                </h2>
                <p className="text-xs text-zinc-300">
                  O OlirumPlay possui suporte nativo a Widgets no Android. Você pode fixar o tocador na sua tela de início sem precisar abrir o app.
                </p>
              </div>

              {/* Preview Visual do Widget Android */}
              <div className="p-4 rounded-2xl bg-[#060B14] border border-[#00F5D4]/40 shadow-xl shadow-[#00F5D4]/15 flex items-center justify-between gap-3">
                <img src={currentTrack.cover} alt={currentTrack.title} className="w-12 h-12 rounded-xl object-cover" />
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-xs text-white truncate">{currentTrack.title}</p>
                  <p className="text-[10px] text-[#00F5D4] truncate">{currentTrack.artist}</p>
                </div>
                <div className="flex items-center gap-1.5">
                  <button onClick={handlePrev} className="p-2 text-zinc-300 hover:text-white">
                    <SkipBack className="w-4 h-4 fill-current" />
                  </button>
                  <button onClick={togglePlay} className="w-9 h-9 rounded-xl bg-[#00F5D4] text-zinc-950 flex items-center justify-center font-bold">
                    {isPlaying ? <Pause className="w-4 h-4 fill-zinc-950" /> : <Play className="w-4 h-4 fill-zinc-950 ml-0.5" />}
                  </button>
                  <button onClick={handleNext} className="p-2 text-zinc-300 hover:text-white">
                    <SkipForward className="w-4 h-4 fill-current" />
                  </button>
                </div>
              </div>

              {/* Passo a Passo para o Usuário Adicionar o Widget no Celular */}
              <div className="bg-[#060B14]/80 p-4 rounded-2xl border border-white/5 space-y-2">
                <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-[#00F5D4]" /> Como ativar o Widget no seu celular:
                </h4>
                <ol className="text-xs text-zinc-300 space-y-1.5 list-decimal list-inside leading-relaxed">
                  <li>Vá para a <strong>Tela Inicial</strong> do seu celular Android.</li>
                  <li><strong>Pressione e segure</strong> o dedo em qualquer espaço vazio.</li>
                  <li>Toque no botão <strong>Widgets</strong> que aparecerá no menu.</li>
                  <li>Procure por <strong>OlirumPlay</strong> na lista de aplicativos.</li>
                  <li>Arraste o Widget de Música para a sua tela inicial!</li>
                </ol>
              </div>

              {/* Mini-Player Flutuante (Floating In-App Widget) */}
              <div className="pt-2 flex items-center justify-between border-t border-white/10">
                <div>
                  <h4 className="font-bold text-sm text-white">Mini-Player Flutuante na Tela</h4>
                  <p className="text-[11px] text-zinc-400">Ativa um widget móvel suspenso enquanto você navega no app</p>
                </div>
                <button
                  onClick={() => setIsFloatingWidgetActive(!isFloatingWidgetActive)}
                  className={`px-4 py-2 rounded-xl text-xs font-bold transition ${
                    isFloatingWidgetActive ? 'bg-[#00F5D4] text-zinc-950' : 'bg-[#111C3A] text-zinc-300'
                  }`}
                >
                  {isFloatingWidgetActive ? 'Ativo ⚡' : 'Desativado'}
                </button>
              </div>
            </div>

            {/* Bloco 2: Equalizador WebAudio 5 Bandas */}
            <div className="p-5 rounded-3xl bg-[#0B132B]/90 border border-[#00F5D4]/20 space-y-5">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-sm text-white flex items-center gap-2">
                    <Sliders className="w-4 h-4 text-[#00F5D4]" /> Equalizador 5 Bandas
                  </h3>
                  <p className="text-[11px] text-zinc-400">Ajuste de frequências e presets de som</p>
                </div>
                <button onClick={() => applyEqPreset('flat')} className="text-xs text-[#00F5D4] hover:underline">
                  Resetar Flat
                </button>
              </div>

              <div className="flex flex-wrap gap-1.5">
                {Object.entries(EQ_PRESETS).map(([key, pr]) => (
                  <button
                    key={key}
                    onClick={() => applyEqPreset(key)}
                    className={`px-2.5 py-1.5 rounded-xl text-[11px] font-bold border transition ${
                      eqPreset === key
                        ? 'bg-[#00F5D4] text-zinc-950 border-[#00F5D4] shadow-[0_0_10px_rgba(0,245,212,0.3)]'
                        : 'bg-[#111C3A] text-zinc-300 border-white/5 hover:bg-[#1C2541]'
                    }`}
                  >
                    {pr.name.split(' ')[0]}
                  </button>
                ))}
              </div>

              <div className="grid grid-cols-5 gap-2 pt-2">
                {EQ_BANDS.map((band, idx) => (
                  <div key={band.freq} className="flex flex-col items-center space-y-2 bg-black/40 p-2.5 rounded-2xl border border-white/5">
                    <span className="text-[10px] font-mono text-[#00F5D4] font-bold">
                      {eqGains[idx] > 0 ? `+${eqGains[idx]}` : eqGains[idx]}dB
                    </span>
                    <input
                      type="range"
                      min="-12"
                      max="12"
                      step="1"
                      value={eqGains[idx]}
                      onChange={(e) => updateEqBandGain(idx, e.target.value)}
                      className="w-full accent-[#00F5D4] cursor-pointer"
                    />
                    <span className="text-[10px] font-bold text-white">{band.label}</span>
                    <span className="text-[9px] text-zinc-500">{band.desc}</span>
                  </div>
                ))}
              </div>

              {/* Sleep Timer */}
              <div className="pt-2 border-t border-white/10 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-white flex items-center gap-1.5">
                    <Moon className="w-4 h-4 text-amber-400" /> Sleep Timer (Desligar Sozinho)
                  </span>
                  <p className="text-[10px] text-zinc-400">Pausa a música quando o tempo acabar</p>
                </div>
                <div className="flex gap-1.5">
                  {[0, 15, 30, 60].map((m) => (
                    <button
                      key={m}
                      onClick={() => activateSleepTimer(m)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-bold ${
                        sleepTimerMinutes === m ? 'bg-amber-400 text-zinc-950' : 'bg-zinc-800 text-zinc-300'
                      }`}
                    >
                      {m === 0 ? 'Off' : `${m}m`}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* =====================================================================
          MINI-PLAYER FLUTUANTE PINNABLE (FLOATING WIDGET)
      ===================================================================== */}
      {isFloatingWidgetActive && currentTrack && (
        <div
          className="fixed z-50 p-2.5 rounded-2xl bg-[#0B132B]/95 border-2 border-[#00F5D4] shadow-2xl shadow-[#00F5D4]/30 backdrop-blur-xl flex items-center gap-2.5 cursor-move"
          style={{ bottom: '110px', right: '16px' }}
        >
          <img src={currentTrack.cover} alt={currentTrack.title} className="w-10 h-10 rounded-xl object-cover" />
          <div className="max-w-[120px]">
            <p className="text-xs font-bold text-white truncate">{currentTrack.title}</p>
            <p className="text-[9px] text-[#00F5D4] truncate">{currentTrack.artist}</p>
          </div>
          <button onClick={togglePlay} className="p-2 rounded-xl bg-[#00F5D4] text-zinc-950">
            {isPlaying ? <Pause className="w-4 h-4 fill-zinc-950" /> : <Play className="w-4 h-4 fill-zinc-950" />}
          </button>
          <button onClick={() => setIsFloatingWidgetActive(false)} className="p-1 text-zinc-400 hover:text-white">
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* =====================================================================
          PLAYER TELA CHEIA (FULL VIEW)
      ===================================================================== */}
      {showFullPlayer && currentTrack && (
        <div className="fixed inset-0 z-50 bg-gradient-to-b from-[#0B132B] via-[#060B14] to-[#03060A] flex flex-col justify-between p-5 sm:p-8 overflow-y-auto animate-fade-in">
          <div className="flex items-center justify-between max-w-lg w-full mx-auto">
            <button onClick={() => setShowFullPlayer(false)} className="p-2.5 rounded-2xl bg-white/5 text-zinc-200">
              <ChevronDown className="w-6 h-6" />
            </button>
            <div className="flex items-center gap-1 bg-[#0B132B] p-1 rounded-2xl border border-white/10">
              {[
                { id: 'cover', label: 'Capa' },
                { id: 'lyrics', label: 'Letras 🎤' },
                { id: 'eq', label: 'EQ' },
                { id: 'queue', label: `Fila (${queue.length})` }
              ].map((t) => (
                <button
                  key={t.id}
                  onClick={() => setPlayerSubView(t.id)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                    playerSubView === t.id ? 'bg-[#00F5D4] text-zinc-950' : 'text-zinc-400 hover:text-white'
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>
            <button onClick={() => fetchHdCoverForTrack(currentTrack)} className="p-2.5 rounded-2xl bg-white/5 text-[#00F5D4]">
              <ImageIcon className="w-5 h-5" />
            </button>
          </div>

          {/* SubView 1: Capa */}
          {playerSubView === 'cover' && (
            <div className="my-auto max-w-md w-full mx-auto flex flex-col items-center text-center space-y-4 py-4">
              <div className="relative w-64 h-64 sm:w-72 sm:h-72 rounded-3xl overflow-hidden shadow-2xl shadow-[#00F5D4]/20 border-2 border-[#00F5D4]/40">
                <img src={currentTrack.cover} alt={currentTrack.title} className="w-full h-full object-cover" />
                <button
                  onClick={() => downloadCoverFile(currentTrack)}
                  className="absolute bottom-3 right-3 p-2 rounded-xl bg-black/75 backdrop-blur-md text-[#00F5D4] border border-[#00F5D4]/40 text-xs font-bold flex items-center gap-1"
                >
                  <Download className="w-3.5 h-3.5" /> Baixar Capa HD
                </button>
              </div>
              <div className="space-y-1 px-2">
                <h2 className="text-xl sm:text-2xl font-extrabold text-white line-clamp-1">{currentTrack.title}</h2>
                <p className="text-sm text-zinc-400">{currentTrack.artist} • {currentTrack.album}</p>
              </div>
            </div>
          )}

          {/* SubView 2: Letras */}
          {playerSubView === 'lyrics' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/80 border border-white/10 rounded-3xl p-5 max-h-[380px] overflow-y-auto space-y-3 text-center">
              {lyricsData.loading ? (
                <div className="py-12 space-y-2">
                  <Loader2 className="w-7 h-7 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Carregando letra...</p>
                </div>
              ) : lyricsData.synced.length > 0 ? (
                <div className="space-y-3 py-2">
                  {lyricsData.synced.map((line, idx) => {
                    const isActive = idx === activeLyricIndex;
                    return (
                      <p
                        key={`${line.time}-${idx}`}
                        ref={isActive ? activeLyricRef : null}
                        onClick={() => {
                          if (audioRef.current) {
                            audioRef.current.currentTime = line.time;
                            setCurrentTime(line.time);
                          }
                        }}
                        className={`cursor-pointer transition-all duration-300 ${
                          isActive
                            ? 'text-lg font-extrabold text-[#00F5D4] scale-105 drop-shadow-[0_0_12px_rgba(0,245,212,0.8)]'
                            : 'text-sm text-zinc-400 hover:text-zinc-200'
                        }`}
                      >
                        {line.text}
                      </p>
                    );
                  })}
                </div>
              ) : (
                <div className="py-8 whitespace-pre-line text-xs sm:text-sm text-zinc-300 leading-relaxed">
                  {lyricsData.plain || 'Nenhuma letra encontrada.'}
                </div>
              )}
            </div>
          )}

          {/* SubView 3: Equalizador */}
          {playerSubView === 'eq' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/90 border border-[#00F5D4]/20 rounded-3xl p-5 space-y-4">
              <h3 className="font-bold text-sm text-white">Equalizador 5 Bandas WebAudio</h3>
              <div className="grid grid-cols-5 gap-2">
                {EQ_BANDS.map((band, idx) => (
                  <div key={band.freq} className="flex flex-col items-center space-y-2 bg-black/40 p-2 rounded-xl">
                    <span className="text-[10px] text-[#00F5D4] font-bold">{eqGains[idx]}dB</span>
                    <input
                      type="range"
                      min="-12"
                      max="12"
                      step="1"
                      value={eqGains[idx]}
                      onChange={(e) => updateEqBandGain(idx, e.target.value)}
                      className="w-full accent-[#00F5D4]"
                    />
                    <span className="text-[10px] text-white">{band.label}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* SubView 4: Fila */}
          {playerSubView === 'queue' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/90 border border-white/10 rounded-3xl p-4 max-h-80 overflow-y-auto space-y-2">
              <h3 className="text-xs font-bold uppercase text-zinc-400">Fila de Reprodução ({queue.length})</h3>
              {queue.map((item, idx) => (
                <div
                  key={`${item.id}-${idx}`}
                  onClick={() => {
                    setCurrentIndex(idx);
                    setIsPlaying(true);
                  }}
                  className={`flex items-center justify-between p-2 rounded-xl cursor-pointer ${
                    idx === currentIndex ? 'bg-[#00F5D4]/20 text-[#00F5D4]' : 'text-white'
                  }`}
                >
                  <div className="flex items-center gap-2 truncate">
                    <img src={item.cover} alt={item.title} className="w-8 h-8 rounded-lg object-cover" />
                    <span className="text-xs truncate">{item.title}</span>
                  </div>
                  <span className="text-[10px] text-zinc-500">{formatSecs(item.duration)}</span>
                </div>
              ))}
            </div>
          )}

          {/* Controles do Player Full */}
          <div className="max-w-md w-full mx-auto space-y-4">
            <div className="space-y-1.5">
              <input
                type="range"
                min="0"
                max={duration || 100}
                value={currentTime}
                onChange={handleSeek}
                className="w-full accent-[#00F5D4] cursor-pointer h-1.5 bg-zinc-800 rounded-lg"
              />
              <div className="flex justify-between text-[11px] font-mono text-zinc-400">
                <span>{formatSecs(currentTime)}</span>
                <span>{formatSecs(duration)}</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <button onClick={() => setIsShuffle(!isShuffle)} className={`p-2.5 rounded-xl ${isShuffle ? 'text-[#00F5D4]' : 'text-zinc-400'}`}>
                <Shuffle className="w-5 h-5" />
              </button>
              <button onClick={handlePrev} className="p-3 text-white">
                <SkipBack className="w-7 h-7 fill-white" />
              </button>
              <button onClick={togglePlay} className="w-16 h-16 rounded-full bg-gradient-to-r from-[#00F5D4] to-[#7040F0] flex items-center justify-center text-zinc-950 shadow-xl shadow-[#00F5D4]/30">
                {isPlaying ? <Pause className="w-8 h-8 fill-zinc-950" /> : <Play className="w-8 h-8 fill-zinc-950 ml-1" />}
              </button>
              <button onClick={handleNext} className="p-3 text-white">
                <SkipForward className="w-7 h-7 fill-white" />
              </button>
              <button onClick={() => setRepeatMode((m) => (m === 'off' ? 'all' : m === 'all' ? 'one' : 'off'))} className={`p-2.5 rounded-xl ${repeatMode !== 'off' ? 'text-[#00F5D4]' : 'text-zinc-400'}`}>
                <Repeat className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          MINI-PLAYER INFERIOR FIXO + BARRA DE 5 ABAS OFICIAIS
      ===================================================================== */}
      <div className="fixed bottom-0 inset-x-0 z-40 bg-gradient-to-t from-[#060B14] via-[#060B14]/95 to-transparent pt-2">
        <div className="max-w-4xl mx-auto px-3 pb-2 space-y-2">
          {/* Mini Player */}
          {currentTrack && (
            <div className="rounded-2xl bg-[#0B132B]/95 border border-[#00F5D4]/20 shadow-2xl backdrop-blur-xl overflow-hidden">
              <div className="w-full h-1 bg-zinc-800">
                <div
                  className="h-full bg-gradient-to-r from-[#00F5D4] to-[#7040F0] transition-all"
                  style={{ width: `${duration ? Math.min(100, (currentTime / duration) * 100) : 0}%` }}
                />
              </div>

              <div className="p-2.5 flex items-center justify-between gap-3">
                <div onClick={() => setShowFullPlayer(true)} className="flex items-center gap-3 flex-1 min-w-0 cursor-pointer">
                  <img
                    src={currentTrack.cover}
                    alt={currentTrack.title}
                    className={`w-11 h-11 rounded-xl object-cover shrink-0 ${isPlaying ? 'ring-2 ring-[#00F5D4]' : ''}`}
                  />
                  <div className="min-w-0 flex-1">
                    <p className="font-bold text-xs sm:text-sm text-white truncate">{currentTrack.title}</p>
                    <p className="text-[11px] text-zinc-400 truncate">{currentTrack.artist}</p>
                  </div>
                </div>

                <div className="flex items-center gap-1 sm:gap-2">
                  <button onClick={handlePrev} className="p-2 text-zinc-300 hover:text-white hidden sm:inline-flex">
                    <SkipBack className="w-4 h-4 fill-current" />
                  </button>
                  <button
                    onClick={togglePlay}
                    className="w-10 h-10 rounded-xl bg-gradient-to-r from-[#00F5D4] to-[#7040F0] text-zinc-950 flex items-center justify-center shadow-lg shadow-[#00F5D4]/20"
                  >
                    {isPlaying ? <Pause className="w-5 h-5 fill-zinc-950" /> : <Play className="w-5 h-5 fill-zinc-950 ml-0.5" />}
                  </button>
                  <button onClick={handleNext} className="p-2 text-zinc-300 hover:text-white">
                    <SkipForward className="w-4 h-4 fill-current" />
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Barra de 5 Abas Oficiais do App (ESTILO SPOTIFY / MUKA) */}
          <nav className="grid grid-cols-5 bg-[#060B14]/95 border border-[#00F5D4]/20 rounded-2xl p-1 backdrop-blur-xl">
            {[
              { id: 'explore', label: 'Explorar', icon: Compass },
              { id: 'device', label: 'Dispositivo', icon: HardDrive, badge: localTracks.length },
              { id: 'lyrics', label: 'Letras 🎤', icon: FileText },
              { id: 'library', label: 'Biblioteca', icon: Library },
              { id: 'widgets', label: 'Widgets', icon: LayoutGrid }
            ].map((item) => {
              const Icon = item.icon;
              const active = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`relative flex flex-col items-center justify-center py-2 rounded-xl transition ${
                    active ? 'bg-[#00F5D4]/15 text-[#00F5D4]' : 'text-zinc-500 hover:text-zinc-300'
                  }`}
                >
                  <Icon className={`w-4 h-4 sm:w-5 sm:h-5 ${active ? 'text-[#00F5D4]' : ''}`} />
                  <span className="text-[10px] font-bold mt-1 truncate max-w-full px-1">{item.label}</span>
                  {item.badge > 0 && (
                    <span className="absolute top-1 right-2 px-1.5 py-0.2 rounded-full bg-[#7040F0] text-white text-[9px] font-extrabold">
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>
      </div>
    </div>
  );
}
