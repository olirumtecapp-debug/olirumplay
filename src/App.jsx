import React, { useState, useEffect, useRef } from 'react';
import {
  Play, Pause, SkipBack, SkipForward, Volume2, VolumeX,
  Repeat, Shuffle, Heart, Download, Trash2, Search,
  Music, WifiOff, PlusCircle, Compass, HardDrive,
  CheckCircle2, Loader2, Share2, Disc, Sparkles, Sliders,
  FolderOpen, FileAudio, Clock, ListMusic, ChevronDown,
  Copy, Check, Terminal, Layers, RefreshCw, AlertCircle,
  Smartphone, Archive, Upload, Radio, Flame, X, MoreVertical,
  Gauge, Moon, Plus, FolderPlus, ArrowUpDown, ExternalLink,
  FileText, Image as ImageIcon, CheckCircle, Zap
} from 'lucide-react';

// ============================================================================
// 1. CATÁLOGO INICIAL DE DEMONSTRAÇÃO (RESERVA CASO OFFLINE OU SEM CONEXÃO)
// ============================================================================
const DEMO_CATALOG = [
  {
    id: 'demo-1',
    title: 'Cyberpunk Neon Drive',
    artist: 'Olirum Synth Collective',
    album: 'Neon Horizons 2026',
    duration: 372,
    cover: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    genre: 'Synthwave',
    source: 'cloud',
    bitrate: '320 kbps',
    isFullSong: true
  },
  {
    id: 'demo-2',
    title: 'Midnight Rain Lo-Fi',
    artist: 'Cyber Chill BR',
    album: 'Madrugada Neon',
    duration: 425,
    cover: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    genre: 'Lo-Fi',
    source: 'cloud',
    bitrate: '320 kbps',
    isFullSong: true
  },
  {
    id: 'demo-3',
    title: 'Subgrave Cyber Baile',
    artist: 'DJ Neon & Trap MC',
    album: 'Fluxo Olirum',
    duration: 344,
    cover: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    genre: 'Trap / Funk',
    source: 'cloud',
    bitrate: '320 kbps',
    isFullSong: true
  },
  {
    id: 'demo-4',
    title: 'Electric Progressive Anthem',
    artist: 'Alok & Cyber Pulse',
    album: 'Festival Mainstage',
    duration: 302,
    cover: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    genre: 'Electronic',
    source: 'cloud',
    bitrate: '320 kbps',
    isFullSong: true
  },
  {
    id: 'demo-5',
    title: 'Acoustic Cyber Horizon',
    artist: 'Luna Waves',
    album: 'Unplugged Session',
    duration: 355,
    cover: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=1000&q=80',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    genre: 'Acoustic',
    source: 'cloud',
    bitrate: '320 kbps',
    isFullSong: true
  }
];

const GENRES = [
  { id: '', label: '🔥 Em Alta' },
  { id: 'Electronic', label: '⚡ Eletrônica' },
  { id: 'Chill', label: '☕ Lo-Fi & Chill' },
  { id: 'Rock', label: '🎸 Rock / Metal' },
  { id: 'Hip-Hop/Rap', label: '🎤 Hip-Hop & Trap' },
  { id: 'Pop', label: '✨ Pop & R&B' },
  { id: 'Acoustic', label: '🌿 Acústico' }
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
// 2. MOTOR INDEXED-DB PARA ARMAZENAMENTO OFFLINE E ARQUIVOS LOCAIS
// ============================================================================
const DB_NAME = 'OlirumPlayDB';
const DB_VERSION = 3;
const STORE_TRACKS = 'tracks';
const STORE_PLAYLISTS = 'playlists';
const STORE_LYRICS = 'lyrics_cache';

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
      if (!db.objectStoreNames.contains(STORE_LYRICS)) {
        db.createObjectStore(STORE_LYRICS, { keyPath: 'id' });
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
// 4. PARSER BINÁRIO NATIVO DE TAGS ID3v2 (LOCAL)
// ============================================================================
const generateGradientCover = (title = 'Olirum') => {
  const palettes = [
    ['#00f5d4', '#7040f0'],
    ['#7040f0', '#ff007f'],
    ['#00f5d4', '#050508'],
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
    <circle cx="300" cy="300" r="220" fill="url(#g)" opacity="0.35"/>
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
    artist: parts.length > 1 ? parts[0].trim() : 'Arquivo Local',
    album: 'Armazenamento Local',
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
        p++; // pula byte nulo do mime
        p++; // pula byte de picture type
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
    console.warn('Fallback ID3 parser:', err);
  }

  return fallbackMeta;
};

const getAudioFileDuration = (url) => {
  return new Promise((resolve) => {
    const tempAudio = new Audio();
    tempAudio.preload = 'metadata';
    const timer = setTimeout(() => resolve(210), 3500);
    tempAudio.onloadedmetadata = () => {
      clearTimeout(timer);
      resolve(tempAudio.duration && isFinite(tempAudio.duration) ? Math.round(tempAudio.duration) : 210);
    };
    tempAudio.onerror = () => {
      clearTimeout(timer);
      resolve(210);
    };
    tempAudio.src = url;
  });
};

// ============================================================================
// 5. COMPONENTE PRINCIPAL OLIRUMPLAY
// ============================================================================
export default function App() {
  // Navegação principal
  const [activeTab, setActiveTab] = useState('explore'); // explore | local | offline | lyrics | handoff
  const [localFilter, setLocalFilter] = useState('all'); // all | playlists | favorites

  // Catálogo Online & Filtros (Audius API)
  const [cloudTracks, setCloudTracks] = useState(DEMO_CATALOG);
  const [selectedGenre, setSelectedGenre] = useState('');
  const [isLoadingCatalog, setIsLoadingCatalog] = useState(false);

  // Listas Locais e Offline
  const [localTracks, setLocalTracks] = useState([]);
  const [offlineTracks, setOfflineTracks] = useState([]);
  const [favorites, setFavorites] = useState(() => {
    try { return JSON.parse(localStorage.getItem('olirum_favs') || '[]'); } catch { return []; }
  });
  const [playlists, setPlaylists] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('olirum_playlists') || JSON.stringify([
        { id: 'pl-neon', name: 'Cyber Neon Vibes ⚡', trackIds: ['demo-1', 'demo-3'] },
        { id: 'pl-chill', name: 'Madrugada Coding 💻', trackIds: ['demo-2', 'demo-5'] }
      ]));
    } catch { return []; }
  });

  // Estado do Player e Fila
  const [queue, setQueue] = useState(DEMO_CATALOG);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(0.9);
  const [isMuted, setIsMuted] = useState(false);
  const [isShuffle, setIsShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState('off'); // off | all | one
  const [playbackRate, setPlaybackRate] = useState(1.0);

  // Modais e Gavetas do Player
  const [showFullPlayer, setShowFullPlayer] = useState(false);
  const [playerSubView, setPlayerSubView] = useState('cover'); // cover | lyrics | eq | queue
  const [eqPreset, setEqPreset] = useState('flat');
  const [eqGains, setEqGains] = useState([0, 0, 0, 0, 0]);
  const [sleepTimerMinutes, setSleepTimerMinutes] = useState(0);
  const [sleepRemainingSec, setSleepRemainingSec] = useState(0);

  // Letras (Sincronizadas LRC e Estáticas)
  const [lyricsData, setLyricsData] = useState({ synced: [], plain: '', trackId: null, loading: false });
  const [manualLyricsQuery, setManualLyricsQuery] = useState('');

  // Busca Online
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearchingOnline, setIsSearchingOnline] = useState(false);

  // Capa HD Fetcher
  const [isFetchingCover, setIsFetchingCover] = useState(false);

  // Importação Local e Downloads
  const [downloadingIds, setDownloadingIds] = useState({});
  const [isScanningLocal, setIsScanningLocal] = useState(false);
  const [scanProgress, setScanProgress] = useState({ done: 0, total: 0 });
  const [persistLocalToIDB, setPersistLocalToIDB] = useState(true);
  const [localSearch, setLocalSearch] = useState('');
  const [isDraggingOver, setIsDraggingOver] = useState(false);

  // Nova Playlist Modal
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [playlistModalTrack, setPlaylistModalTrack] = useState(null);

  // Notificações e Ponto de Continuidade
  const [toast, setToast] = useState(null);
  const [copiedKey, setCopiedKey] = useState('');

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
  const lyricsContainerRef = useRef(null);
  const activeLyricRef = useRef(null);

  const currentTrack = queue[currentIndex] || DEMO_CATALOG[0];

  const notify = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  // ============================================================================
  // 6. INICIALIZAÇÃO: BANCO INDEXEDDB, SESSÃO PERSISTENTE E CATÁLOGO AUDIUS
  // ============================================================================
  useEffect(() => {
    loadSavedDatabase();
    restorePreviousSession();
    fetchAudiusTrending('');
    return () => {
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    };
  }, []);

  // Persistência contínua da sessão no localStorage (sobrevive a fechamentos acidentais)
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
        if (data.playbackRate) setPlaybackRate(data.playbackRate);
      }
    } catch (e) {
      console.warn('Erro ao restaurar sessão anterior:', e);
    }
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
    } catch (err) {
      console.error('Erro ao carregar IndexedDB:', err);
    }
  };

  useEffect(() => {
    localStorage.setItem('olirum_favs', JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    localStorage.setItem('olirum_playlists', JSON.stringify(playlists));
  }, [playlists]);

  // ============================================================================
  // 7. INTEGRAÇÃO AUDIUS API (CATÁLOGO GRATUITO DE MÚSICAS COMPLETAS)
  // ============================================================================
  const fetchAudiusTrending = async (genre = '') => {
    setIsLoadingCatalog(true);
    setSelectedGenre(genre);
    try {
      const genreParam = genre ? `&genre=${encodeURIComponent(genre)}` : '';
      const url = `https://discoveryprovider.audius.co/v1/tracks/trending?app_name=OlirumPlay${genreParam}&limit=35`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Falha Audius API');
      const data = await res.json();
      if (data.data && data.data.length > 0) {
        const mapped = data.data.map((track) => ({
          id: `audius-${track.id}`,
          title: track.title,
          artist: track.user?.name || 'Artista Independente',
          album: track.user?.handle ? `@${track.user.handle}` : 'Audius Cyber Stream',
          duration: track.duration || 180,
          cover:
            track.artwork?.['1000x1000'] ||
            track.artwork?.['480x480'] ||
            track.artwork?.['150x150'] ||
            generateGradientCover(track.title),
          audioUrl: `https://discoveryprovider.audius.co/v1/tracks/${track.id}/stream?app_name=OlirumPlay`,
          genre: track.genre || genre || 'Livre',
          source: 'audius',
          bitrate: '320 kbps (Stream Livre)',
          isFullSong: true
        }));
        setCloudTracks(mapped);
      }
    } catch (e) {
      console.warn('Falha ao consultar Audius, mantendo catálogo de segurança:', e);
      if (cloudTracks.length === 0) setCloudTracks(DEMO_CATALOG);
    } finally {
      setIsLoadingCatalog(false);
    }
  };

  const searchOnlineCatalog = async (e) => {
    if (e) e.preventDefault();
    if (!searchQuery.trim()) {
      fetchAudiusTrending(selectedGenre);
      return;
    }
    setIsSearchingOnline(true);
    try {
      // 1. Tenta buscar no Audius (músicas completas)
      const audiusUrl = `https://discoveryprovider.audius.co/v1/tracks/search?query=${encodeURIComponent(
        searchQuery
      )}&app_name=OlirumPlay&limit=25`;
      const res = await fetch(audiusUrl);
      const data = await res.json();

      let audiusResults = [];
      if (data.data && data.data.length > 0) {
        audiusResults = data.data.map((track) => ({
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
          genre: track.genre || 'Web',
          source: 'audius',
          bitrate: '320 kbps (Completa)',
          isFullSong: true
        }));
      }

      // 2. Se encontrar resultados no Audius, usa-os
      if (audiusResults.length > 0) {
        setCloudTracks(audiusResults);
        notify(`${audiusResults.length} músicas completas encontradas no Audius! ⚡`);
      } else {
        // Fallback iTunes
        const itunesRes = await fetch(
          `https://itunes.apple.com/search?term=${encodeURIComponent(searchQuery)}&entity=song&limit=15`
        );
        const itunesData = await itunesRes.json();
        if (itunesData.results && itunesData.results.length > 0) {
          const itunesResults = itunesData.results
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
              genre: item.primaryGenreName || 'Pop',
              source: 'itunes',
              bitrate: 'Preview 256 kbps',
              isFullSong: false
            }));
          setCloudTracks(itunesResults);
          notify(`${itunesResults.length} faixas encontradas!`);
        } else {
          notify('Nenhuma faixa encontrada para esta busca.', 'info');
        }
      }
    } catch (err) {
      console.error(err);
      notify('Erro de conexão ao buscar no catálogo online.', 'error');
    } finally {
      setIsSearchingOnline(false);
    }
  };

  // ============================================================================
  // 8. LETRAS SINCRONIZADAS EM TEMPO REAL (LRCLIB API)
  // ============================================================================
  const fetchLyricsForTrack = async (track, manualQuery = null) => {
    if (!track) return;
    setLyricsData({ synced: [], plain: '', trackId: track.id, loading: true });

    const cleanTitle = (manualQuery || track.title)
      .replace(/\s*[\(\[](feat|ft|remix|official|audio|video|prod|deluxe).*?[\)\]]/gi, '')
      .replace(/[^\w\s\u00C0-\u017F]/gi, ' ')
      .trim();
    const cleanArtist = (track.artist || '')
      .replace(/\s*[\(\[](feat|ft).*?[\)\]]/gi, '')
      .trim();

    try {
      // 1. Tenta correspondência exata
      let resp = await fetch(
        `https://lrclib.net/api/get?track_name=${encodeURIComponent(cleanTitle)}&artist_name=${encodeURIComponent(
          cleanArtist
        )}`
      );

      let data = resp.ok ? await resp.json() : null;

      // 2. Fallback por busca livre
      if (!data || (!data.syncedLyrics && !data.plainLyrics)) {
        const searchResp = await fetch(
          `https://lrclib.net/api/search?q=${encodeURIComponent(cleanTitle + ' ' + cleanArtist)}`
        );
        if (searchResp.ok) {
          const list = await searchResp.json();
          if (list && list.length > 0) {
            data = list[0];
          }
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
        if (parsedSynced.length > 0) {
          notify('Letra sincronizada em tempo real encontrada! 🎤⚡');
        } else {
          notify('Letra da música carregada com sucesso! 📄');
        }
      } else {
        setLyricsData({
          synced: [],
          plain: 'Letra não encontrada para esta faixa no momento.\nVocê pode pesquisar manualmente pelo nome da música.',
          trackId: track.id,
          loading: false
        });
      }
    } catch (e) {
      console.warn('Erro ao buscar letras:', e);
      setLyricsData({
        synced: [],
        plain: 'Sem conexão com o serviço de letras.',
        trackId: track.id,
        loading: false
      });
    }
  };

  // Carrega a letra quando a música ativa mudar ou quando o usuário abrir a aba de letras
  useEffect(() => {
    if (currentTrack && lyricsData.trackId !== currentTrack.id) {
      fetchLyricsForTrack(currentTrack);
    }
  }, [currentTrack?.id]);

  // Auto-scroll da linha ativa de letra sincronizada
  useEffect(() => {
    if (playerSubView === 'lyrics' && activeLyricRef.current) {
      activeLyricRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }, [currentTime, playerSubView]);

  // ============================================================================
  // 9. BUSCA E DOWNLOAD DE CAPA HD OFICIAL (1000x1000 PX)
  // ============================================================================
  const fetchHdCoverForTrack = async (track) => {
    if (!track) return;
    setIsFetchingCover(true);
    notify('Buscando capa oficial em Ultra HD (1000x1000)... 🎨', 'info');
    try {
      const q = `${track.artist} ${track.title}`;
      const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(q)}&entity=song&limit=5`);
      const data = await res.json();
      if (data.results && data.results.length > 0) {
        const best = data.results[0];
        const hdCover = best.artworkUrl100
          ? best.artworkUrl100.replace('100x100bb', '1000x1000bb.jpg')
          : track.cover;

        // Atualiza a faixa no estado
        const updatedTrack = { ...track, cover: hdCover };
        setQueue((prev) => prev.map((t) => (t.id === track.id ? updatedTrack : t)));
        setLocalTracks((prev) => prev.map((t) => (t.id === track.id ? updatedTrack : t)));
        setOfflineTracks((prev) => prev.map((t) => (t.id === track.id ? updatedTrack : t)));

        // Se estiver no IDB, atualiza
        try {
          await saveTrackToIDB(updatedTrack);
        } catch {}

        notify('Capa oficial em 1000x1000 HD aplicada com sucesso! ✨');
      } else {
        notify('Capa oficial não encontrada nos servidores globais.', 'info');
      }
    } catch (err) {
      notify('Não foi possível buscar a capa online.', 'error');
    } finally {
      setIsFetchingCover(false);
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
    notify('Iniciando download da Capa em Alta Resolução! 📥');
  };

  // ============================================================================
  // 10. SLEEP TIMER (DESLIGAMENTO AUTOMÁTICO)
  // ============================================================================
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
    if (mins > 0) notify(`Sleep Timer ativado para ${mins} minutos 🌙`);
    else notify('Sleep Timer desativado');
  };

  // ============================================================================
  // 11. WEB AUDIO API (EQUALIZADOR 5 BANDAS + ANALISADOR NEON)
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
      renderVisualizerLoop();
    } catch (e) {
      console.warn('Web Audio API fallback (CORS stream):', e);
    }
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

  const renderVisualizerLoop = () => {
    if (!canvasRef.current || !analyserRef.current) return;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    const analyser = analyserRef.current;
    const bufferLength = analyser.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);

    const draw = () => {
      animFrameRef.current = requestAnimationFrame(draw);
      analyser.getByteFrequencyData(dataArray);
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const barWidth = (canvas.width / bufferLength) * 1.6;
      let x = 0;

      for (let i = 0; i < bufferLength; i++) {
        const barHeight = (dataArray[i] / 255) * canvas.height;
        const grad = ctx.createLinearGradient(0, canvas.height, 0, 0);
        grad.addColorStop(0, '#7040f0');
        grad.addColorStop(0.5, '#00f5d4');
        grad.addColorStop(1, '#ffffff');
        ctx.fillStyle = grad;
        ctx.shadowColor = '#00f5d4';
        ctx.shadowBlur = 8;
        ctx.fillRect(x, canvas.height - barHeight, barWidth - 2, barHeight);
        x += barWidth;
      }
    };
    draw();
  };

  // ============================================================================
  // 12. MEDIA SESSION API & PERSISTÊNCIA NA TELA DE BLOQUEIO DO ANDROID
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
        album: currentTrack.album || 'OlirumPlay Cyber Player',
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
      navigator.mediaSession.setActionHandler('seekbackward', () => {
        if (audioRef.current) {
          audioRef.current.currentTime = Math.max(0, audioRef.current.currentTime - 10);
        }
      });
      navigator.mediaSession.setActionHandler('seekforward', () => {
        if (audioRef.current) {
          audioRef.current.currentTime = Math.min(duration, audioRef.current.currentTime + 10);
        }
      });

      if ('setPositionState' in navigator.mediaSession && duration > 0) {
        try {
          navigator.mediaSession.setPositionState({
            duration: Math.max(0, duration),
            playbackRate: playbackRate,
            position: Math.min(duration, Math.max(0, currentTime))
          });
        } catch {}
      }
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
      audioRef.current
        .play()
        .then(() => setIsPlaying(true))
        .catch((e) => {
          console.error(e);
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

  const changeSpeed = (rate) => {
    setPlaybackRate(rate);
    if (audioRef.current) audioRef.current.playbackRate = rate;
    notify(`Velocidade ajustada para ${rate}x ⚡`);
  };

  // ============================================================================
  // 13. SCANNER E IMPORTADOR DE MÚSICAS DO CELULAR (PASTAS / DOWNLOADS / MUSIC)
  // ============================================================================
  const processLocalAudioFiles = async (fileList) => {
    if (!fileList || fileList.length === 0) return;
    const validExtensions = /\.(mp3|m4a|wav|flac|ogg|aac|opus|webm)$/i;
    const audioFiles = Array.from(fileList).filter(
      (file) => file.type.startsWith('audio/') || validExtensions.test(file.name)
    );

    if (audioFiles.length === 0) {
      notify('Nenhum arquivo de áudio compatível (.mp3, .m4a, .wav, .flac) encontrado.', 'error');
      return;
    }

    setIsScanningLocal(true);
    setScanProgress({ done: 0, total: audioFiles.length });

    const importedBatch = [];

    for (let i = 0; i < audioFiles.length; i++) {
      const file = audioFiles[i];
      const objectUrl = URL.createObjectURL(file);
      const id3 = await parseID3v2Metadata(file);
      const dur = await getAudioFileDuration(objectUrl);

      const relativeFolder = file.webkitRelativePath
        ? file.webkitRelativePath.split('/')[0]
        : 'Memória Interna';

      const newLocalTrack = {
        id: `local-${file.name}-${file.size}-${file.lastModified}`,
        title: id3.title,
        artist: id3.artist,
        album: id3.album !== 'Armazenamento Local' ? id3.album : relativeFolder,
        duration: dur,
        cover: id3.cover,
        audioUrl: objectUrl,
        genre: file.name.split('.').pop().toUpperCase(),
        source: 'local-device',
        fileSize: file.size,
        fileName: file.name,
        folderName: relativeFolder,
        isSavedOffline: persistLocalToIDB,
        isFullSong: true
      };

      if (persistLocalToIDB) {
        try {
          await saveTrackToIDB(newLocalTrack, file);
        } catch (e) {
          console.warn('Espaço IndexedDB cheio para este arquivo:', e);
        }
      }

      importedBatch.push(newLocalTrack);
      setScanProgress({ done: i + 1, total: audioFiles.length });
    }

    setLocalTracks((prev) => {
      const existingIds = new Set(prev.map((t) => t.id));
      const uniqueNew = importedBatch.filter((t) => !existingIds.has(t.id));
      return [...uniqueNew, ...prev];
    });

    setIsScanningLocal(false);
    notify(`${importedBatch.length} música(s) importada(s) com leitor ID3 e capas! 🎵`);

    if (importedBatch.length > 0) {
      selectAndPlayTrack(importedBatch[0], [...importedBatch, ...localTracks]);
    }
  };

  const handleDropFiles = async (e) => {
    e.preventDefault();
    setIsDraggingOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      await processLocalAudioFiles(e.dataTransfer.files);
    }
  };

  const removeLocalTrack = async (id) => {
    await deleteTrackFromIDB(id);
    setLocalTracks((prev) => prev.filter((t) => t.id !== id));
    notify('Música removida da biblioteca local.', 'info');
  };

  // ============================================================================
  // 14. DOWNLOAD OFFLINE (STREAM -> INDEXEDDB + EXPORTAR MP3)
  // ============================================================================
  const handleDownloadOffline = async (track) => {
    if (offlineTracks.some((t) => t.id === track.id)) {
      notify('Esta faixa já está salva no seu Cofre Offline!', 'info');
      return;
    }

    setDownloadingIds((prev) => ({ ...prev, [track.id]: true }));
    try {
      const resp = await fetch(track.audioUrl);
      if (!resp.ok) throw new Error('Falha de rede');
      const blob = await resp.blob();
      await saveTrackToIDB({ ...track, source: 'cloud-offline' }, blob);
      await loadSavedDatabase();
      notify(`"${track.title}" salva no Cofre Offline para tocar sem internet! 📥`);
    } catch (err) {
      console.error(err);
      notify('Não foi possível salvar offline devido a bloqueio CORS da origem.', 'error');
    } finally {
      setDownloadingIds((prev) => ({ ...prev, [track.id]: false }));
    }
  };

  const exportMp3ToDevice = (track) => {
    const a = document.createElement('a');
    a.href = track.audioUrl;
    a.download = `${track.artist} - ${track.title}.mp3`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    notify('Transferindo arquivo .MP3 para seus Downloads!');
  };

  const removeOfflineTrack = async (id) => {
    await deleteTrackFromIDB(id);
    await loadSavedDatabase();
    notify('Faixa removida do armazenamento offline.', 'info');
  };

  // ============================================================================
  // 15. FAVORITOS, PLAYLISTS E PONTO DE CONTINUIDADE
  // ============================================================================
  const toggleFavorite = (id) => {
    setFavorites((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const createPlaylist = (e) => {
    e.preventDefault();
    if (!newPlaylistName.trim()) return;
    const pl = { id: `pl-${Date.now()}`, name: newPlaylistName.trim(), trackIds: [] };
    setPlaylists((prev) => [...prev, pl]);
    setNewPlaylistName('');
    notify(`Playlist "${pl.name}" criada com sucesso!`);
  };

  const addTrackToPlaylist = (playlistId, trackId) => {
    setPlaylists((prev) =>
      prev.map((pl) =>
        pl.id === playlistId && !pl.trackIds.includes(trackId)
          ? { ...pl, trackIds: [...pl.trackIds, trackId] }
          : pl
      )
    );
    setPlaylistModalTrack(null);
    notify('Faixa adicionada à playlist! ✨');
  };

  const exportContinuitySnapshot = () => {
    const snapshot = {
      project: 'OlirumPlay Cyber Neon Music Player',
      version: 'v1.0.0-cyber-pro',
      exportedAt: new Date().toISOString(),
      favorites,
      playlists,
      eqPreset,
      eqGains,
      localTracksMetadata: localTracks.map(({ audioUrl, ...rest }) => rest),
      offlineTracksMetadata: offlineTracks.map(({ audioUrl, audioBlob, ...rest }) => rest)
    };
    const blob = new Blob([JSON.stringify(snapshot, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'olirumplay-checkpoint-snapshot.json';
    a.click();
    notify('Snapshot de Continuidade OlirumPlay baixado! 🛡️');
  };

  const copyCodeBlock = (key, text) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    notify('Copiado para a área de transferência!');
    setTimeout(() => setCopiedKey(''), 2500);
  };

  // Helpers de formatação
  const formatSecs = (s) => {
    if (!s || isNaN(s)) return '0:00';
    const m = Math.floor(s / 60);
    const sec = Math.floor(s % 60);
    return `${m}:${sec < 10 ? '0' : ''}${sec}`;
  };

  const formatBytes = (bytes = 0) => {
    if (!bytes) return '4.2 MB';
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const filteredLocalTracks = localTracks.filter(
    (t) =>
      t.title.toLowerCase().includes(localSearch.toLowerCase()) ||
      t.artist.toLowerCase().includes(localSearch.toLowerCase()) ||
      (t.album && t.album.toLowerCase().includes(localSearch.toLowerCase()))
  );

  // Linha ativa na letra sincronizada
  const activeLyricIndex = lyricsData.synced.findIndex((item, idx) => {
    const next = lyricsData.synced[idx + 1];
    return currentTime >= item.time && (!next || currentTime < next.time);
  });

  // ============================================================================
  // 16. RENDERIZAÇÃO DA INTERFACE VISUAL (OLIRUMPLAY CYBER NEON)
  // ============================================================================
  return (
    <div
      className="min-h-screen bg-[#060B14] text-zinc-100 flex flex-col font-sans select-none overflow-x-hidden"
      onDragOver={(e) => { e.preventDefault(); setIsDraggingOver(true); }}
      onDragLeave={() => setIsDraggingOver(false)}
      onDrop={handleDropFiles}
    >
      {/* Elemento Nativo de Áudio HTML5 */}
      <audio
        ref={audioRef}
        crossOrigin="anonymous"
        onTimeUpdate={() => setCurrentTime(audioRef.current?.currentTime || 0)}
        onLoadedMetadata={() => setDuration(audioRef.current?.duration || currentTrack?.duration || 0)}
        onEnded={handleNext}
      />

      {/* Inputs Ocultos para Importação de Arquivos Locais e Pastas do Dispositivo */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.m4a,.wav,.flac,.ogg,.aac"
        className="hidden"
        onChange={(e) => processLocalAudioFiles(e.target.files)}
      />
      <input
        ref={folderInputRef}
        type="file"
        webkitdirectory=""
        directory=""
        multiple
        className="hidden"
        onChange={(e) => processLocalAudioFiles(e.target.files)}
      />

      {/* Overlay Drag & Drop Global */}
      {isDraggingOver && (
        <div className="fixed inset-0 z-50 bg-[#060B14]/95 backdrop-blur-xl border-4 border-dashed border-[#00F5D4] flex flex-col items-center justify-center p-6 text-center animate-fade-in">
          <FolderOpen className="w-20 h-20 text-[#00F5D4] animate-bounce mb-4" />
          <h2 className="text-2xl font-extrabold text-white">Solte suas músicas ou pastas aqui!</h2>
          <p className="text-zinc-300 mt-1">OlirumPlay lerá instantaneamente as tags ID3v2, capas e organizará na memória</p>
        </div>
      )}

      {/* Notificação Toast Flutuante Neon */}
      {toast && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2.5 px-4 py-3 rounded-2xl bg-[#0B132B]/95 border border-[#00F5D4]/50 shadow-2xl shadow-[#00F5D4]/20 backdrop-blur-md max-w-sm w-[92%] animate-fade-in">
          <Zap className="w-5 h-5 text-[#00F5D4] shrink-0" />
          <span className="text-xs sm:text-sm font-semibold text-white">{toast.msg}</span>
        </div>
      )}

      {/* =====================================================================
          HEADER PRINCIPAL (IDENTIDADE VISUAL OLIRUMPLAY)
      ===================================================================== */}
      <header className="sticky top-0 z-30 bg-[#060B14]/85 backdrop-blur-xl border-b border-[#00F5D4]/15 px-4 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl overflow-hidden p-0.5 bg-gradient-to-tr from-[#7040F0] via-[#00F5D4] to-[#00C2FF] flex items-center justify-center shadow-lg shadow-[#00F5D4]/25">
            <img src="/logo.png" alt="OlirumPlay" className="w-full h-full object-cover rounded-xl" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-extrabold text-base tracking-tight bg-gradient-to-r from-white via-[#00F5D4] to-[#7040F0] bg-clip-text text-transparent">
                OlirumPlay
              </h1>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#00F5D4]/15 text-[#00F5D4] border border-[#00F5D4]/30 shadow-[0_0_10px_rgba(0,245,212,0.2)]">
                Cyber Neon
              </span>
            </div>
            <p className="text-[11px] text-zinc-400 flex items-center gap-2">
              <span className="text-[#00F5D4] font-semibold">{localTracks.length} locais</span>
              <span>•</span>
              <span className="text-[#7040F0] font-semibold">{offlineTracks.length} offline</span>
              {sleepRemainingSec > 0 && (
                <>
                  <span>•</span>
                  <span className="text-amber-400 flex items-center gap-1 font-mono">
                    <Moon className="w-3 h-3" /> {formatSecs(sleepRemainingSec)}
                  </span>
                </>
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#00F5D4]/10 hover:bg-[#00F5D4]/20 border border-[#00F5D4]/30 text-[#00F5D4] text-xs font-bold transition shadow-[0_0_12px_rgba(0,245,212,0.15)]"
            title="Importar Músicas do Celular"
          >
            <FolderOpen className="w-4 h-4 text-[#00F5D4]" />
            <span className="hidden sm:inline">Abrir Músicas</span>
          </button>
          <button
            onClick={() => {
              setShowFullPlayer(true);
              setPlayerSubView('eq');
            }}
            className="p-2 rounded-xl bg-[#0B132B] hover:bg-[#111C3A] border border-white/10 text-[#00F5D4]"
            title="Equalizador 5 Bandas"
          >
            <Sliders className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* =====================================================================
          CONTEÚDO PRINCIPAL (ABAS DA APLICAÇÃO)
      ===================================================================== */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-4 pt-4 pb-44">
        {/* ===================================================================
            ABA 1: EXPLORAR & CATÁLOGO LIVRE (AUDIUS API + BUSCA ONLINE)
        =================================================================== */}
        {activeTab === 'explore' && (
          <div className="space-y-6">
            {/* Banner Oficial OlirumPlay */}
            <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/20 p-5 sm:p-6 shadow-xl shadow-[#00F5D4]/5">
              <div className="relative z-10 max-w-lg space-y-2">
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full bg-[#00F5D4]/15 text-[#00F5D4] border border-[#00F5D4]/30">
                  <Sparkles className="w-3.5 h-3.5 text-[#00F5D4]" /> Catálogo Livre + Músicas Locais + Letras Sincronizadas
                </span>
                <h2 className="text-xl sm:text-2xl font-extrabold text-white leading-tight">
                  Tudo o que você queria: streaming livre, capas em 1000x1000, letras LRC e persistência real.
                </h2>
                <p className="text-xs sm:text-sm text-zinc-300">
                  Ouça músicas completas, escaneie as pastas do seu smartphone e aproveite a reprodução ininterrupta em segundo plano.
                </p>
                <div className="pt-2 flex flex-wrap gap-2.5">
                  <button
                    onClick={() => setActiveTab('local')}
                    className="px-4 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs flex items-center gap-2 shadow-lg shadow-[#00F5D4]/30 hover:bg-[#40ffdf] transition"
                  >
                    <HardDrive className="w-4 h-4 text-zinc-950" />
                    Músicas Locais do Aparelho ({localTracks.length})
                  </button>
                  <button
                    onClick={() => {
                      if (currentTrack) {
                        setShowFullPlayer(true);
                        setPlayerSubView('lyrics');
                      }
                    }}
                    className="px-4 py-2.5 rounded-xl bg-[#7040F0]/30 border border-[#7040F0]/50 text-white font-semibold text-xs flex items-center gap-2 hover:bg-[#7040F0]/50 transition"
                  >
                    <FileText className="w-4 h-4 text-[#00F5D4]" />
                    Ver Letra Sincronizada 🎤
                  </button>
                </div>
              </div>
            </div>

            {/* Barra de Busca Integrada */}
            <form onSubmit={searchOnlineCatalog} className="relative flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-zinc-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Buscar artista, música livre ou álbum..."
                  className="w-full pl-10 pr-24 py-3.5 rounded-2xl bg-[#0B132B] border border-[#00F5D4]/20 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-[#00F5D4] focus:ring-1 focus:ring-[#00F5D4]"
                />
                {searchQuery && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchQuery('');
                      fetchAudiusTrending(selectedGenre);
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

            {/* Filtros de Gênero do Catálogo Livre */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
              {GENRES.map((g) => {
                const active = selectedGenre === g.id;
                return (
                  <button
                    key={g.id}
                    onClick={() => fetchAudiusTrending(g.id)}
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition border ${
                      active
                        ? 'bg-[#00F5D4] text-zinc-950 border-[#00F5D4] shadow-[0_0_12px_rgba(0,245,212,0.3)]'
                        : 'bg-[#0B132B] text-zinc-300 border-white/10 hover:border-[#00F5D4]/40'
                    }`}
                  >
                    {g.label}
                  </button>
                );
              })}
            </div>

            {/* Lista de Músicas do Catálogo */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Flame className="w-4 h-4 text-[#00F5D4]" />
                  <span>Catálogo de Streaming Gratuito & Completo</span>
                </h3>
                <span className="text-xs text-zinc-400">
                  {cloudTracks.length} faixas • 320 kbps
                </span>
              </div>

              {isLoadingCatalog ? (
                <div className="p-12 text-center space-y-3 bg-[#0B132B]/50 rounded-3xl border border-white/5">
                  <Loader2 className="w-8 h-8 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Carregando catálogo de streaming livre...</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
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
                            : 'bg-[#0B132B]/70 border-white/5 hover:border-[#00F5D4]/30 hover:bg-[#0B132B]'
                        }`}
                      >
                        <div
                          onClick={() => selectAndPlayTrack(track, cloudTracks)}
                          className="flex items-center gap-3 flex-1 min-w-0 cursor-pointer"
                        >
                          <div className="relative w-12 h-12 rounded-xl overflow-hidden shrink-0">
                            <img src={track.cover} alt={track.title} className="w-full h-full object-cover" />
                            {isCurrent && (
                              <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                                <Disc className="w-6 h-6 text-[#00F5D4] animate-spin" />
                              </div>
                            )}
                          </div>
                          <div className="min-w-0 flex-1">
                            <p className={`font-bold text-xs truncate ${isCurrent ? 'text-[#00F5D4]' : 'text-white'}`}>
                              {track.title}
                            </p>
                            <p className="text-[11px] text-zinc-400 truncate">{track.artist}</p>
                            <div className="flex items-center gap-1.5 mt-0.5">
                              <span className="text-[9px] px-1.5 py-0.2 rounded bg-[#00F5D4]/15 text-[#00F5D4] font-bold">
                                {track.genre || 'Música'}
                              </span>
                              <span className="text-[10px] text-zinc-500 font-mono">
                                {formatSecs(track.duration)}
                              </span>
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => toggleFavorite(track.id)}
                            className="p-2 text-zinc-400 hover:text-pink-500 transition"
                            title="Favoritar"
                          >
                            <Heart className={`w-4 h-4 ${isFav ? 'text-pink-500 fill-pink-500' : ''}`} />
                          </button>

                          <button
                            onClick={() => handleDownloadOffline(track)}
                            disabled={isDl || isOffline}
                            className={`p-2 transition ${
                              isOffline ? 'text-[#00F5D4]' : 'text-zinc-400 hover:text-[#00F5D4]'
                            }`}
                            title={isOffline ? 'Salvo no Cofre Offline' : 'Salvar Offline'}
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
                            onClick={() => setPlaylistModalTrack(track)}
                            className="p-2 text-zinc-400 hover:text-white"
                            title="Adicionar à Playlist"
                          >
                            <PlusCircle className="w-4 h-4" />
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
            ABA 2: MÚSICAS LOCAIS DO CELULAR & GERENCIADOR ID3
        =================================================================== */}
        {activeTab === 'local' && (
          <div className="space-y-5">
            {/* Cabeçalho Local */}
            <div className="p-5 rounded-3xl bg-gradient-to-r from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div className="space-y-1">
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase text-[#00F5D4]">
                  <HardDrive className="w-3.5 h-3.5" /> Armazenamento Local do Celular
                </span>
                <h2 className="text-lg sm:text-xl font-extrabold text-white">
                  Músicas do seu Dispositivo ({localTracks.length})
                </h2>
                <p className="text-xs text-zinc-300">
                  Lê tags ID3v2, capas embutidas e mantém os áudios guardados sem gastar seus dados de internet.
                </p>
              </div>

              <div className="flex flex-wrap gap-2 w-full sm:w-auto">
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="flex-1 sm:flex-initial px-3.5 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 text-xs font-bold flex items-center justify-center gap-2 shadow-lg shadow-[#00F5D4]/20 hover:bg-[#40ffdf] transition"
                >
                  <FileAudio className="w-4 h-4" /> Selecionar Arquivos
                </button>
                <button
                  onClick={() => folderInputRef.current?.click()}
                  className="flex-1 sm:flex-initial px-3.5 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 hover:border-[#00F5D4]/40 text-white text-xs font-bold flex items-center justify-center gap-2 transition"
                >
                  <FolderOpen className="w-4 h-4 text-[#00F5D4]" /> Pasta Inteira
                </button>
              </div>
            </div>

            {/* Progresso de Varredura */}
            {isScanningLocal && (
              <div className="p-4 rounded-2xl bg-[#0B132B] border border-[#00F5D4]/30 space-y-2">
                <div className="flex items-center justify-between text-xs text-zinc-300">
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin text-[#00F5D4]" />
                    Lendo tags ID3 e capas...
                  </span>
                  <span className="font-mono text-[#00F5D4] font-bold">
                    {scanProgress.done} / {scanProgress.total}
                  </span>
                </div>
                <div className="w-full h-2 bg-black/60 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-[#00F5D4] to-[#7040F0] transition-all"
                    style={{ width: `${(scanProgress.done / (scanProgress.total || 1)) * 100}%` }}
                  />
                </div>
              </div>
            )}

            {/* Barra de Filtro de Músicas Locais */}
            {localTracks.length > 0 && (
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-zinc-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={localSearch}
                    onChange={(e) => setLocalSearch(e.target.value)}
                    placeholder="Filtrar suas músicas locais por título ou artista..."
                    className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                  />
                </div>
              </div>
            )}

            {/* Lista de Músicas Locais */}
            {localTracks.length === 0 ? (
              <div className="p-10 rounded-3xl bg-[#0B132B]/40 border border-white/5 text-center space-y-3">
                <HardDrive className="w-12 h-12 text-zinc-600 mx-auto" />
                <h3 className="text-sm font-bold text-zinc-300">Nenhuma música local carregada ainda</h3>
                <p className="text-xs text-zinc-500 max-w-sm mx-auto">
                  Toque em <strong>Selecionar Arquivos</strong> ou <strong>Pasta Inteira</strong> para carregar os MP3s que você tem baixados no seu telefone.
                </p>
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="px-4 py-2 rounded-xl bg-[#00F5D4]/20 border border-[#00F5D4]/40 text-[#00F5D4] font-bold text-xs"
                >
                  Abrir Arquivos de Áudio
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
                        <img src={track.cover} alt={track.title} className="w-12 h-12 rounded-xl object-cover" />
                        <div className="min-w-0 flex-1">
                          <p className={`font-bold text-sm truncate ${isCurrent ? 'text-[#00F5D4]' : 'text-white'}`}>
                            {track.title}
                          </p>
                          <p className="text-xs text-zinc-400 truncate">{track.artist} • {track.album}</p>
                          <div className="flex items-center gap-2 mt-0.5">
                            <span className="text-[9px] px-1.5 py-0.2 rounded bg-white/5 text-zinc-400 font-mono">
                              {formatBytes(track.fileSize)}
                            </span>
                            <span className="text-[10px] text-zinc-500 font-mono">
                              {formatSecs(track.duration)}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => fetchHdCoverForTrack(track)}
                          className="p-2 text-zinc-400 hover:text-[#00F5D4]"
                          title="Buscar Capa Oficial HD (1000x1000)"
                        >
                          <ImageIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => removeLocalTrack(track.id)}
                          className="p-2 text-zinc-500 hover:text-rose-400"
                          title="Remover"
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
            ABA 3: COFRE OFFLINE (INDEXEDDB SEM INTERNET)
        =================================================================== */}
        {activeTab === 'offline' && (
          <div className="space-y-5">
            <div className="p-5 rounded-3xl bg-gradient-to-r from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/20 flex items-center justify-between">
              <div className="space-y-1">
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase text-[#00F5D4]">
                  <WifiOff className="w-3.5 h-3.5" /> Cofre Offline IndexedDB
                </span>
                <h2 className="text-lg font-extrabold text-white">
                  Músicas Salvas para Modo Avião ({offlineTracks.length})
                </h2>
                <p className="text-xs text-zinc-300">
                  Estes arquivos estão gravados na memória local protegida do player e nunca precisam de internet para tocar.
                </p>
              </div>
            </div>

            {offlineTracks.length === 0 ? (
              <div className="p-10 rounded-3xl bg-[#0B132B]/40 border border-white/5 text-center space-y-3">
                <WifiOff className="w-10 h-10 text-zinc-600 mx-auto" />
                <p className="text-sm font-bold text-zinc-300">Seu Cofre Offline está vazio</p>
                <p className="text-xs text-zinc-500 max-w-sm mx-auto">
                  Na aba <strong>Explorar</strong>, toque no ícone de download ao lado de qualquer música para guardá-la offline.
                </p>
              </div>
            ) : (
              <div className="space-y-2.5">
                {offlineTracks.map((track) => (
                  <div
                    key={track.id}
                    className="flex items-center justify-between p-3 rounded-2xl bg-[#0B132B]/70 border border-[#00F5D4]/20"
                  >
                    <div
                      onClick={() => selectAndPlayTrack(track, offlineTracks)}
                      className="flex items-center gap-3.5 flex-1 min-w-0 cursor-pointer"
                    >
                      <img src={track.cover} alt={track.title} className="w-12 h-12 rounded-xl object-cover" />
                      <div className="min-w-0 flex-1">
                        <p className="font-bold text-sm text-white truncate">{track.title}</p>
                        <p className="text-xs text-zinc-400 truncate">{track.artist}</p>
                        <span className="text-[10px] text-[#00F5D4] font-semibold">
                          Salvo no Cofre • {formatBytes(track.fileSize)}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() => exportMp3ToDevice(track)}
                        className="px-3 py-2 rounded-xl bg-[#00F5D4]/15 hover:bg-[#00F5D4]/25 text-[#00F5D4] text-xs font-bold flex items-center gap-1"
                        title="Salvar arquivo .MP3 no telefone"
                      >
                        <Download className="w-3.5 h-3.5" /> .MP3
                      </button>
                      <button
                        onClick={() => removeOfflineTrack(track.id)}
                        className="p-2 rounded-xl text-zinc-500 hover:text-rose-400"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ===================================================================
            ABA 4: LETRAS EM TEMPO REAL & KARAOKE (ABA DEDICADA)
        =================================================================== */}
        {activeTab === 'lyrics' && (
          <div className="space-y-5">
            <div className="p-5 rounded-3xl bg-gradient-to-r from-[#0B132B] via-[#111C3A] to-[#060B14] border border-[#00F5D4]/20 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div>
                <span className="inline-flex items-center gap-1.5 text-[11px] font-bold uppercase text-[#00F5D4]">
                  <FileText className="w-3.5 h-3.5" /> Letras Sincronizadas LRCLIB
                </span>
                <h2 className="text-lg font-extrabold text-white">
                  {currentTrack ? `${currentTrack.title} — ${currentTrack.artist}` : 'Nenhuma música tocando'}
                </h2>
                <p className="text-xs text-zinc-300">
                  Acompanhe a letra no estilo karaokê com rolagem automática e sincronia milimétrica.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => fetchLyricsForTrack(currentTrack)}
                  className="px-3 py-2 rounded-xl bg-[#0B132B] border border-white/10 hover:border-[#00F5D4]/40 text-xs text-[#00F5D4] font-bold flex items-center gap-1.5"
                >
                  <RefreshCw className="w-3.5 h-3.5" /> Atualizar Letra
                </button>
              </div>
            </div>

            {/* Painel de Busca Manual de Letra */}
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
                placeholder="Pesquisar manualmente (ex: Queen Bohemian Rhapsody)..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-[#0B132B] border border-white/10 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
              />
              <button
                type="submit"
                className="px-4 py-2.5 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs"
              >
                Buscar
              </button>
            </form>

            {/* Exibição da Letra */}
            <div className="p-6 rounded-3xl bg-[#0B132B]/80 border border-white/10 max-h-[500px] overflow-y-auto space-y-4 text-center">
              {lyricsData.loading ? (
                <div className="py-12 space-y-3">
                  <Loader2 className="w-8 h-8 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Buscando letra sincronizada nos servidores globais...</p>
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
                <div className="py-8 whitespace-pre-line text-sm text-zinc-300 font-sans leading-relaxed">
                  {lyricsData.plain || 'Nenhuma letra carregada para esta faixa.'}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ===================================================================
            ABA 5: PONTO DE CONTINUIDADE & WORKFLOW APK
        =================================================================== */}
        {activeTab === 'handoff' && (
          <div className="space-y-6">
            <div className="p-5 rounded-3xl bg-[#0B132B] border border-[#00F5D4]/20 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <span className="text-[11px] font-bold uppercase text-[#00F5D4] flex items-center gap-1.5">
                    <Smartphone className="w-3.5 h-3.5" /> OlirumPlay • Continuidade Antigravity
                  </span>
                  <h2 className="text-lg font-extrabold text-white">
                    Ponto de Continuidade & Snapshot do Projeto
                  </h2>
                </div>
                <button
                  onClick={exportContinuitySnapshot}
                  className="px-3.5 py-2 rounded-xl bg-[#00F5D4] text-zinc-950 font-bold text-xs flex items-center gap-1.5 shadow-lg shadow-[#00F5D4]/25"
                >
                  <Archive className="w-4 h-4" /> Baixar Backup JSON
                </button>
              </div>
              <p className="text-xs text-zinc-300">
                O OlirumPlay está totalmente estruturado no diretório <code>D:\PlayerMPro</code> com Capacitor Android configurado e permissões ativas de segundo plano.
              </p>
            </div>

            {/* Bloco 1: Permissões Android para Persistência e Memória */}
            <div className="p-5 rounded-3xl bg-[#0B132B]/80 border border-white/10 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-white flex items-center gap-2">
                    <Layers className="w-4 h-4 text-[#00F5D4]" />
                    1. Permissões de Reprodução Contínua (android/app/src/main/AndroidManifest.xml)
                  </h3>
                  <p className="text-xs text-zinc-400">
                    Garante que o app toque em segundo plano e com a tela apagada sem ser encerrado pelo Android:
                  </p>
                </div>
                <button
                  onClick={() =>
                    copyCodeBlock(
                      'manifest',
                      `<uses-permission android:name="android.permission.INTERNET" />\n<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />\n<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />\n<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />\n<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />\n<uses-permission android:name="android.permission.WAKE_LOCK" />`
                    )
                  }
                  className="px-3 py-2 rounded-xl bg-zinc-800 text-zinc-200 text-xs font-bold flex items-center gap-1.5"
                >
                  {copiedKey === 'manifest' ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                  Copiar XML
                </button>
              </div>
              <pre className="p-4 rounded-2xl bg-black/70 border border-white/5 text-[11px] text-[#00F5D4] overflow-x-auto font-mono">
{`<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />`}
              </pre>
            </div>

            {/* Bloco 2: Compilação de APK no GitHub Actions */}
            <div className="p-5 rounded-3xl bg-[#0B132B]/80 border border-white/10 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-white flex items-center gap-2">
                    <Smartphone className="w-4 h-4 text-[#7040F0]" />
                    2. Workflow de Compilação de APK (.github/workflows/build-apk.yml)
                  </h3>
                  <p className="text-xs text-zinc-400">
                    Compila automaticamente o APK na nuvem do GitHub pronto para instalar no seu aparelho:
                  </p>
                </div>
                <button
                  onClick={() =>
                    copyCodeBlock(
                      'workflow',
                      `name: Build OlirumPlay Android APK\non:\n  push:\n    branches: [ main, master ]\n  workflow_dispatch:\n\njobs:\n  build-android:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - uses: actions/setup-node@v4\n        with:\n          node-version: 20\n      - uses: actions/setup-java@v4\n        with:\n          distribution: 'temurin'\n          java-version: '17'\n      - name: Install & Build Web Assets\n        run: |\n          npm install\n          npm run build\n          npx cap sync android\n      - name: Assemble Debug APK\n        working-directory: ./android\n        run: |\n          chmod +x gradlew\n          ./gradlew assembleDebug\n      - name: Upload APK Artifact\n        uses: actions/upload-artifact@v4\n        with:\n          name: OlirumPlay-APK\n          path: android/app/build/outputs/apk/debug/app-debug.apk`
                    )
                  }
                  className="px-3 py-2 rounded-xl bg-zinc-800 text-zinc-200 text-xs font-bold flex items-center gap-1.5"
                >
                  {copiedKey === 'workflow' ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                  Copiar Workflow
                </button>
              </div>
              <pre className="p-4 rounded-2xl bg-black/70 border border-white/5 text-[11px] text-zinc-300 overflow-x-auto font-mono">
{`name: Build OlirumPlay Android APK
on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Install & Build Web Assets
        run: |
          npm install
          npm run build
          npx cap sync android
      - name: Assemble Debug APK
        working-directory: ./android
        run: |
          chmod +x gradlew
          ./gradlew assembleDebug
      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: OlirumPlay-APK
          path: android/app/build/outputs/apk/debug/app-debug.apk`}
              </pre>
            </div>
          </div>
        )}
      </main>

      {/* =====================================================================
          MODAL ADICIONAR À PLAYLIST
      ===================================================================== */}
      {playlistModalTrack && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#0B132B] border border-[#00F5D4]/30 rounded-3xl max-w-sm w-full p-5 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-white">Adicionar à Playlist</h3>
              <button onClick={() => setPlaylistModalTrack(null)} className="text-zinc-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>
            <p className="text-xs text-zinc-400 truncate">
              Faixa: <strong className="text-[#00F5D4]">{playlistModalTrack.title}</strong>
            </p>
            <div className="space-y-2 max-h-56 overflow-y-auto">
              {playlists.map((pl) => (
                <button
                  key={pl.id}
                  onClick={() => addTrackToPlaylist(pl.id, playlistModalTrack.id)}
                  className="w-full p-3 rounded-2xl bg-[#111C3A] hover:bg-[#00F5D4]/20 border border-white/5 text-left flex items-center justify-between transition"
                >
                  <span className="text-xs font-bold text-white">{pl.name}</span>
                  <span className="text-[10px] text-zinc-400">{pl.trackIds.length} faixas</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          PLAYER TELA CHEIA (CYBER NEON + LETRAS LRC + EQUALIZADOR 5 BANDAS)
      ===================================================================== */}
      {showFullPlayer && currentTrack && (
        <div className="fixed inset-0 z-50 bg-gradient-to-b from-[#0B132B] via-[#060B14] to-[#03060A] flex flex-col justify-between p-5 sm:p-8 overflow-y-auto animate-fade-in">
          {/* Topo do Player Expandido */}
          <div className="flex items-center justify-between max-w-lg w-full mx-auto">
            <button
              onClick={() => setShowFullPlayer(false)}
              className="p-2.5 rounded-2xl bg-white/5 hover:bg-white/10 text-zinc-200"
            >
              <ChevronDown className="w-6 h-6" />
            </button>

            <div className="flex items-center gap-1 bg-[#0B132B] p-1 rounded-2xl border border-white/10">
              {[
                { id: 'cover', label: 'Player' },
                { id: 'lyrics', label: 'Letras 🎤' },
                { id: 'eq', label: 'Equalizador' },
                { id: 'queue', label: `Fila (${queue.length})` }
              ].map((t) => (
                <button
                  key={t.id}
                  onClick={() => setPlayerSubView(t.id)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                    playerSubView === t.id
                      ? 'bg-[#00F5D4] text-zinc-950 shadow-[0_0_10px_rgba(0,245,212,0.4)]'
                      : 'text-zinc-400 hover:text-white'
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>

            <div className="flex items-center gap-1.5">
              <button
                onClick={() => fetchHdCoverForTrack(currentTrack)}
                className="p-2.5 rounded-2xl bg-white/5 hover:bg-white/10 text-[#00F5D4]"
                title="Buscar Capa Oficial HD (1000x1000)"
              >
                <ImageIcon className="w-5 h-5" />
              </button>
              <button
                onClick={() => exportMp3ToDevice(currentTrack)}
                className="p-2.5 rounded-2xl bg-white/5 hover:bg-white/10 text-zinc-200"
                title="Baixar arquivo MP3"
              >
                <Download className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Sub-View 1: Capa + Visualizador Neon */}
          {playerSubView === 'cover' && (
            <div className="my-auto max-w-md w-full mx-auto flex flex-col items-center text-center space-y-4 py-4">
              <div className="relative w-64 h-64 sm:w-72 sm:h-72 rounded-3xl overflow-hidden shadow-2xl shadow-[#00F5D4]/15 border-2 border-[#00F5D4]/40 group">
                <img src={currentTrack.cover} alt={currentTrack.title} className="w-full h-full object-cover" />
                <button
                  onClick={() => downloadCoverFile(currentTrack)}
                  className="absolute bottom-3 right-3 p-2 rounded-xl bg-black/75 backdrop-blur-md text-[#00F5D4] border border-[#00F5D4]/40 text-xs font-bold flex items-center gap-1 opacity-90 hover:opacity-100 transition shadow-lg"
                  title="Baixar Imagem da Capa em Alta Resolução (1000x1000)"
                >
                  <Download className="w-3.5 h-3.5" /> Capa HD
                </button>
                {currentTrack.source === 'local-device' && (
                  <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-black/70 backdrop-blur-md text-[10px] font-bold text-[#00F5D4] border border-[#00F5D4]/30">
                    MEMÓRIA LOCAL
                  </span>
                )}
              </div>

              {/* Canvas Visualizador Neon */}
              <canvas ref={canvasRef} width={280} height={42} className="w-64 h-10 opacity-90" />

              <div className="space-y-1 px-2">
                <h2 className="text-xl sm:text-2xl font-extrabold text-white line-clamp-1">{currentTrack.title}</h2>
                <p className="text-sm text-zinc-400">{currentTrack.artist} • {currentTrack.album}</p>
              </div>
            </div>
          )}

          {/* Sub-View 2: Letras Sincronizadas no Player Tela Cheia */}
          {playerSubView === 'lyrics' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/80 border border-white/10 rounded-3xl p-5 max-h-[380px] overflow-y-auto space-y-3 text-center">
              {lyricsData.loading ? (
                <div className="py-12 space-y-2">
                  <Loader2 className="w-7 h-7 animate-spin text-[#00F5D4] mx-auto" />
                  <p className="text-xs text-zinc-400">Carregando letra sincronizada...</p>
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
                  {lyricsData.plain || 'Nenhuma letra encontrada para esta faixa.'}
                </div>
              )}
            </div>
          )}

          {/* Sub-View 3: Equalizador 5 Bandas + Sleep Timer */}
          {playerSubView === 'eq' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/90 border border-[#00F5D4]/20 rounded-3xl p-5 space-y-5">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-sm text-white flex items-center gap-2">
                    <Sliders className="w-4 h-4 text-[#00F5D4]" /> Equalizador WebAudio 5 Bandas
                  </h3>
                  <p className="text-[11px] text-zinc-400">Processamento de frequência nativo em tempo real</p>
                </div>
                <button onClick={() => applyEqPreset('flat')} className="text-xs text-[#00F5D4] hover:underline">
                  Resetar
                </button>
              </div>

              {/* Presets Rápidos */}
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

              {/* Sliders das 5 Bandas */}
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

              {/* Velocidade de Reprodução & Sleep Timer */}
              <div className="grid grid-cols-2 gap-3 pt-2 border-t border-white/10">
                <div className="space-y-1.5">
                  <span className="text-[11px] font-bold text-zinc-300 flex items-center gap-1">
                    <Gauge className="w-3.5 h-3.5 text-[#00F5D4]" /> Velocidade
                  </span>
                  <div className="flex gap-1">
                    {[0.75, 1.0, 1.25, 1.5].map((r) => (
                      <button
                        key={r}
                        onClick={() => changeSpeed(r)}
                        className={`flex-1 py-1.5 rounded-lg text-[10px] font-bold ${
                          playbackRate === r ? 'bg-[#00F5D4] text-zinc-950 font-extrabold' : 'bg-zinc-800 text-zinc-300'
                        }`}
                      >
                        {r}x
                      </button>
                    ))}
                  </div>
                </div>

                <div className="space-y-1.5">
                  <span className="text-[11px] font-bold text-zinc-300 flex items-center gap-1">
                    <Moon className="w-3.5 h-3.5 text-amber-400" /> Sleep Timer
                  </span>
                  <div className="flex gap-1">
                    {[0, 15, 30, 60].map((m) => (
                      <button
                        key={m}
                        onClick={() => activateSleepTimer(m)}
                        className={`flex-1 py-1.5 rounded-lg text-[10px] font-bold ${
                          sleepTimerMinutes === m ? 'bg-amber-400 text-zinc-950 font-extrabold' : 'bg-zinc-800 text-zinc-300'
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

          {/* Sub-View 4: Fila de Reprodução */}
          {playerSubView === 'queue' && (
            <div className="my-auto max-w-md w-full mx-auto bg-[#0B132B]/90 border border-white/10 rounded-3xl p-4 max-h-80 overflow-y-auto space-y-2">
              <h3 className="text-xs font-bold uppercase tracking-wider text-zinc-400 px-1">
                Próximas na Fila de Reprodução ({queue.length})
              </h3>
              {queue.map((item, idx) => (
                <div
                  key={`${item.id}-${idx}`}
                  onClick={() => {
                    setCurrentIndex(idx);
                    setIsPlaying(true);
                  }}
                  className={`flex items-center justify-between p-2.5 rounded-xl cursor-pointer transition ${
                    idx === currentIndex
                      ? 'bg-[#00F5D4]/20 border border-[#00F5D4]/40 text-[#00F5D4]'
                      : 'hover:bg-white/5 text-white'
                  }`}
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <img src={item.cover} alt={item.title} className="w-9 h-9 rounded-lg object-cover" />
                    <div className="min-w-0">
                      <p className="text-xs font-bold truncate">{item.title}</p>
                      <p className="text-[10px] text-zinc-400 truncate">{item.artist}</p>
                    </div>
                  </div>
                  <span className="text-[10px] text-zinc-500 font-mono">{formatSecs(item.duration)}</span>
                </div>
              ))}
            </div>
          )}

          {/* Controles Inferiores do Player Expandido */}
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
              <button
                onClick={() => setIsShuffle(!isShuffle)}
                className={`p-2.5 rounded-xl transition ${isShuffle ? 'text-[#00F5D4] bg-[#00F5D4]/15' : 'text-zinc-400'}`}
              >
                <Shuffle className="w-5 h-5" />
              </button>

              <button onClick={handlePrev} className="p-3 rounded-2xl text-white hover:bg-white/10">
                <SkipBack className="w-7 h-7 fill-white" />
              </button>

              <button
                onClick={togglePlay}
                className="w-16 h-16 rounded-full bg-gradient-to-r from-[#00F5D4] to-[#7040F0] flex items-center justify-center text-zinc-950 shadow-xl shadow-[#00F5D4]/30 hover:scale-105 transition"
              >
                {isPlaying ? <Pause className="w-8 h-8 fill-zinc-950" /> : <Play className="w-8 h-8 fill-zinc-950 ml-1" />}
              </button>

              <button onClick={handleNext} className="p-3 rounded-2xl text-white hover:bg-white/10">
                <SkipForward className="w-7 h-7 fill-white" />
              </button>

              <button
                onClick={() =>
                  setRepeatMode((m) => (m === 'off' ? 'all' : m === 'all' ? 'one' : 'off'))
                }
                className={`p-2.5 rounded-xl relative transition ${
                  repeatMode !== 'off' ? 'text-[#00F5D4] bg-[#00F5D4]/15' : 'text-zinc-400'
                }`}
              >
                <Repeat className="w-5 h-5" />
                {repeatMode === 'one' && (
                  <span className="absolute top-1 right-1 text-[9px] font-bold text-[#00F5D4]">1</span>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* =====================================================================
          MINI-PLAYER FLUTUANTE + BARRA DE NAVEGAÇÃO INFERIOR
      ===================================================================== */}
      <div className="fixed bottom-0 inset-x-0 z-40 bg-gradient-to-t from-[#060B14] via-[#060B14]/95 to-transparent pt-2">
        <div className="max-w-4xl mx-auto px-3 pb-2 space-y-2">
          {/* Mini Player */}
          {currentTrack && (
            <div className="rounded-2xl bg-[#0B132B]/95 border border-[#00F5D4]/20 shadow-2xl backdrop-blur-xl overflow-hidden">
              {/* Barra de Progresso Fina no Topo do Mini Player */}
              <div className="w-full h-1 bg-zinc-800">
                <div
                  className="h-full bg-gradient-to-r from-[#00F5D4] to-[#7040F0] transition-all"
                  style={{ width: `${duration ? Math.min(100, (currentTime / duration) * 100) : 0}%` }}
                />
              </div>

              <div className="p-2.5 flex items-center justify-between gap-3">
                <div
                  onClick={() => setShowFullPlayer(true)}
                  className="flex items-center gap-3 flex-1 min-w-0 cursor-pointer"
                >
                  <img
                    src={currentTrack.cover}
                    alt={currentTrack.title}
                    className={`w-11 h-11 rounded-xl object-cover shrink-0 ${isPlaying ? 'ring-2 ring-[#00F5D4]' : ''}`}
                  />
                  <div className="min-w-0 flex-1">
                    <p className="font-bold text-xs sm:text-sm text-white truncate">{currentTrack.title}</p>
                    <p className="text-[11px] text-zinc-400 truncate flex items-center gap-1.5">
                      <span>{currentTrack.artist}</span>
                      {currentTrack.source === 'local-device' && (
                        <span className="text-[9px] px-1.5 py-0.2 rounded bg-[#00F5D4]/20 text-[#00F5D4] font-bold">
                          LOCAL
                        </span>
                      )}
                    </p>
                  </div>
                </div>

                {/* Controles Rápidos do Mini Player */}
                <div className="flex items-center gap-1 sm:gap-2">
                  <button
                    onClick={handlePrev}
                    className="p-2 text-zinc-300 hover:text-white hidden sm:inline-flex"
                  >
                    <SkipBack className="w-4 h-4 fill-current" />
                  </button>
                  <button
                    onClick={togglePlay}
                    className="w-10 h-10 rounded-xl bg-gradient-to-r from-[#00F5D4] to-[#7040F0] text-zinc-950 flex items-center justify-center shadow-lg shadow-[#00F5D4]/20 hover:opacity-90 transition"
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

          {/* Barra de Navegação Mobile-First */}
          <nav className="grid grid-cols-5 bg-[#060B14]/95 border border-[#00F5D4]/20 rounded-2xl p-1 backdrop-blur-xl">
            {[
              { id: 'explore', label: 'Explorar', icon: Compass },
              { id: 'local', label: 'Locais', icon: HardDrive, badge: localTracks.length },
              { id: 'offline', label: 'Offline', icon: WifiOff, badge: offlineTracks.length },
              { id: 'lyrics', label: 'Letras 🎤', icon: FileText },
              { id: 'handoff', label: 'APK & Setup', icon: Smartphone }
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
                    <span className="absolute top-1 right-3 px-1.5 py-0.2 rounded-full bg-[#7040F0] text-white text-[9px] font-extrabold shadow-[0_0_8px_#7040F0]">
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
