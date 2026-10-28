package com.olirum.player.data.remote

import com.google.gson.annotations.SerializedName
import com.olirum.player.model.RadioCategory
import com.olirum.player.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.ConcurrentHashMap

data class RadioBrowserStationDto(
    @SerializedName("stationuuid") val stationUuid: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("url_resolved") val urlResolved: String?,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("favicon") val favicon: String?,
    @SerializedName("tags") val tags: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("countrycode") val countryCode: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("votes") val votes: Int?,
    @SerializedName("codec") val codec: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("clickcount") val clickCount: Int?,
    @SerializedName("lastcheckok") val lastCheckOk: Int?
)

interface RadioBrowserApiService {
    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("tag") tag: String? = null,
        @Query("tagList") tagList: String? = null,
        @Query("countrycode") countryCode: String? = null,
        @Query("language") language: String? = null,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("limit") limit: Int = 40,
        @Query("offset") offset: Int = 0
    ): List<RadioBrowserStationDto>

    @GET("json/url/{stationuuid}")
    suspend fun registerClick(
        @Path("stationuuid") stationUuid: String
    ): Any
}

class RadioRepository {

    // Servidores oficiais espelhos da Radio Browser com fallback
    private val mirrorBaseUrls = listOf(
        "https://de1.api.radio-browser.info/",
        "https://at1.api.radio-browser.info/",
        "https://nl1.api.radio-browser.info/"
    )

    private var currentMirrorIndex = 0

    private fun getApi(): RadioBrowserApiService {
        val baseUrl = mirrorBaseUrls[currentMirrorIndex]
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(ApiClient.okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioBrowserApiService::class.java)
    }

    private fun rotateMirror() {
        currentMirrorIndex = (currentMirrorIndex + 1) % mirrorBaseUrls.size
    }

    // Cache em memória de estações por categoria e busca
    private val categoryCache = ConcurrentHashMap<String, List<RadioStation>>()
    private val searchCache = ConcurrentHashMap<String, List<RadioStation>>()

    // === DEFINIÇÃO DAS CATEGORIAS MUSICAIS OFICIAIS ===
    val categories: List<RadioCategory> = listOf(
        RadioCategory(
            id = "catolica",
            name = "Música Católica",
            subtitle = "Orações, Terço e Louvor",
            primaryTag = "catholic",
            tags = listOf("catholic", "catolica", "musica catolica", "sacra", "oracao", "terco", "evangelizar", "aparecida"),
            gradientColors = listOf(0xFF00E5D4, 0xFF008BFF),
            iconName = "Church",
            drawableResName = "genre_catolica",
            excludedKeywords = listOf("heavy metal", "death metal", "funk", "secular", "rock n roll", "hard rock")
        ),
        RadioCategory(
            id = "classica",
            name = "Música Clássica",
            subtitle = "Bach, Mozart, Chopin & Orquestras",
            primaryTag = "classical",
            tags = listOf("classical", "musica classica", "orchestral", "symphony", "baroque", "opera", "chopin", "bach", "mozart", "piano"),
            gradientColors = listOf(0xFF8B5CF6, 0xFF008BFF),
            iconName = "Piano",
            drawableResName = "genre_classica",
            excludedKeywords = listOf("rock", "metal", "heavy metal", "punk", "classic rock", "classic hits", "pop", "country", "dance", "electronic", "reggae", "hip hop", "rap", "r&b")
        ),
        RadioCategory(
            id = "jazz_blues",
            name = "Jazz & Blues",
            subtitle = "Smooth Jazz, Chicago Blues & Soul",
            primaryTag = "jazz",
            tags = listOf("jazz", "smooth jazz", "blues", "chicago blues", "bebop", "bossa jazz"),
            gradientColors = listOf(0xFFFF3DAD, 0xFF8B5CF6),
            iconName = "Saxophone",
            drawableResName = "genre_jazz_blues",
            excludedKeywords = listOf("heavy metal", "classical", "opera", "trance", "techno", "edm", "sertanejo")
        ),
        RadioCategory(
            id = "classic_rock",
            name = "Classic Rock",
            subtitle = "Lendas dos anos 60, 70 e 80",
            primaryTag = "classic rock",
            tags = listOf("classic rock", "rock classics", "hard rock", "rock 70s", "rock 80s", "rock legends"),
            gradientColors = listOf(0xFF008BFF, 0xFF00E5D4),
            iconName = "Guitar",
            drawableResName = "genre_classic_rock",
            excludedKeywords = listOf("classical", "orchestral", "opera", "chopin", "bach", "mozart", "electronic", "samba", "pagode")
        ),
        RadioCategory(
            id = "love_songs",
            name = "Love Songs",
            subtitle = "Baladas Românticas & Slow Hits",
            primaryTag = "love songs",
            tags = listOf("love songs", "romantic hits", "ballads", "baladas romanticas", "slow songs"),
            gradientColors = listOf(0xFFFF3DAD, 0xFF00E5D4),
            iconName = "Heart",
            drawableResName = "genre_love_songs",
            excludedKeywords = listOf("heavy metal", "death metal", "hard rock", "punk", "techno", "dance", "electronic")
        ),
        RadioCategory(
            id = "lounge",
            name = "Lounge & Chillout",
            subtitle = "Easy Listening, Smooth & Ambient",
            primaryTag = "lounge",
            tags = listOf("lounge", "chillout", "easy listening", "smooth lounge", "ambient", "downtempo"),
            gradientColors = listOf(0xFF00E5D4, 0xFF8B5CF6),
            iconName = "Spa",
            drawableResName = "genre_lounge",
            excludedKeywords = listOf("rock", "metal", "punk", "heavy metal", "hardcore")
        ),
        RadioCategory(
            id = "love_rock",
            name = "Love Rock",
            subtitle = "Soft Rock, Power Ballads & Baladas",
            primaryTag = "soft rock",
            tags = listOf("love rock", "soft rock", "romantic rock", "rock ballads", "power ballads"),
            gradientColors = listOf(0xFF8B5CF6, 0xFFFF3DAD),
            iconName = "MusicNote",
            drawableResName = "genre_love_rock",
            excludedKeywords = listOf("classical", "opera", "hip hop", "techno", "electronic", "samba")
        ),
        RadioCategory(
            id = "pop_hits",
            name = "Pop & Top 40",
            subtitle = "Sucessos Globais Contemporâneos",
            primaryTag = "pop",
            tags = listOf("pop", "pop hits", "pop music", "top 40", "contemporary hits"),
            gradientColors = listOf(0xFF008BFF, 0xFFFF3DAD),
            iconName = "Star",
            drawableResName = "genre_pop_hits",
            excludedKeywords = listOf("classical", "death metal", "heavy metal")
        ),
        RadioCategory(
            id = "greatest_hits",
            name = "Greatest Hits",
            subtitle = "Os Maiores Sucessos de Todos os Tempos",
            primaryTag = "greatest hits",
            tags = listOf("greatest hits", "classic hits", "all time hits", "oldies", "hits"),
            gradientColors = listOf(0xFF00E5D4, 0xFF008BFF),
            iconName = "Trophy",
            drawableResName = "genre_greatest_hits",
            excludedKeywords = listOf("death metal", "heavy metal")
        ),
        RadioCategory(
            id = "anos_70",
            name = "Anos 70",
            subtitle = "Disco, Classic Pop & Soul dos 70s",
            primaryTag = "70s",
            tags = listOf("70s", "70s music", "1970s", "seventies", "seventies hits"),
            gradientColors = listOf(0xFF8B5CF6, 0xFF00E5D4),
            iconName = "Album",
            drawableResName = "genre_anos_70",
            excludedKeywords = listOf("90s", "2000s", "2010s", "modern rock")
        ),
        RadioCategory(
            id = "anos_80",
            name = "Anos 80",
            subtitle = "Synthpop, New Wave & Hits dos 80s",
            primaryTag = "80s",
            tags = listOf("80s", "80s music", "1980s", "eighties", "eighties hits"),
            gradientColors = listOf(0xFFFF3DAD, 0xFF8B5CF6),
            iconName = "Album",
            drawableResName = "genre_anos_80",
            excludedKeywords = listOf("70s", "90s", "2000s", "2010s")
        ),
        RadioCategory(
            id = "anos_90",
            name = "Anos 90",
            subtitle = "Eurodance, Grunge & Pop dos 90s",
            primaryTag = "90s",
            tags = listOf("90s", "90s music", "1990s", "nineties", "nineties hits"),
            gradientColors = listOf(0xFF008BFF, 0xFF8B5CF6),
            iconName = "Album",
            drawableResName = "genre_anos_90",
            excludedKeywords = listOf("70s", "80s", "2000s", "2010s")
        ),
        RadioCategory(
            id = "anos_2000",
            name = "Anos 2000",
            subtitle = "Millennium Pop, R&B e Rock",
            primaryTag = "2000s",
            tags = listOf("2000s", "00s", "2000s music", "2000s hits", "millennium hits"),
            gradientColors = listOf(0xFF00E5D4, 0xFFFF3DAD),
            iconName = "Album",
            drawableResName = "genre_anos_2000",
            excludedKeywords = listOf("70s", "80s")
        ),
        RadioCategory(
            id = "sertanejo",
            name = "Sertanejo",
            subtitle = "Modão, Sertanejo Raiz e Universitário",
            primaryTag = "sertanejo",
            tags = listOf("sertanejo", "sertanejo raiz", "sertanejo universitario", "viola caipira", "modao"),
            gradientColors = listOf(0xFF008BFF, 0xFF00E5D4),
            iconName = "Radio",
            drawableResName = "genre_sertanejo",
            excludedKeywords = listOf("rock", "metal", "techno", "classical", "hip hop", "electronic")
        ),
        RadioCategory(
            id = "mpb_bossa",
            name = "MPB & Bossa Nova",
            subtitle = "Clássicos da Música Popular Brasileira",
            primaryTag = "mpb",
            tags = listOf("mpb", "bossa nova", "musica popular brasileira", "violao brasileiro"),
            gradientColors = listOf(0xFF8B5CF6, 0xFF008BFF),
            iconName = "MusicNote",
            drawableResName = "genre_mpb_bossa",
            excludedKeywords = listOf("heavy metal", "techno", "punk", "death metal")
        ),
        RadioCategory(
            id = "samba_pagode",
            name = "Samba & Pagode",
            subtitle = "Roda de Samba, Partido Alto e Pagode",
            primaryTag = "samba",
            tags = listOf("samba", "pagode", "partido alto", "roda de samba"),
            gradientColors = listOf(0xFFFF3DAD, 0xFF008BFF),
            iconName = "MusicNote",
            drawableResName = "genre_samba_pagode",
            excludedKeywords = listOf("heavy metal", "techno", "classical", "punk")
        ),
        RadioCategory(
            id = "gospel",
            name = "Gospel & Adoração",
            subtitle = "Louvor, Adoração e Música Cristã",
            primaryTag = "gospel",
            tags = listOf("gospel", "louvor", "adoracao", "christian", "worship"),
            gradientColors = listOf(0xFF00E5D4, 0xFF8B5CF6),
            iconName = "Church",
            drawableResName = "genre_gospel",
            excludedKeywords = listOf("catholic", "catolica", "heavy metal", "funk")
        ),
        RadioCategory(
            id = "eletronica",
            name = "Eletrônica & Dance",
            subtitle = "House, EDM, Techno & Trance",
            primaryTag = "electronic",
            tags = listOf("electronic", "dance", "house", "edm", "techno", "trance"),
            gradientColors = listOf(0xFF00E5D4, 0xFFFF3DAD),
            iconName = "Equalizer",
            drawableResName = "genre_eletronica",
            excludedKeywords = listOf("classical", "sertanejo", "country", "pagode", "samba", "rock")
        )
    )

    // === ESTAÇÕES CURADAS E VERIFICADAS (GARANTIA DE ACESSO INSTANTÂNEO) ===
    val curatedStations: List<RadioStation> = listOf(
        // === MÚSICA CATÓLICA (Alta prioridade) ===
        RadioStation(
            id = "curated_evangelizar_pr",
            name = "Rádio Evangelizar (Pe. Reginaldo Manzotti)",
            genre = "Católica",
            streamUrl = "https://8239.brasilstream.com.br/stream?origem=radios.com.br",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio8239.png",
            location = "Curitiba - PR",
            description = "Mensagens de fé, oração e música católica 24 horas.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 450
        ),
        RadioStation(
            id = "curated_aparecida_fm",
            name = "Rádio Aparecida 104.3 FM",
            genre = "Católica",
            streamUrl = "https://aparecida.jmvstream.com/stream",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio1043.png",
            location = "Aparecida - SP",
            description = "A voz do Santuário Nacional de Nossa Senhora Aparecida.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 420
        ),
        RadioStation(
            id = "curated_cancao_nova",
            name = "Rádio Canção Nova FM",
            genre = "Católica",
            streamUrl = "https://cloud1.cdnseguro.com:20038/stream",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio_cancaonova.png",
            location = "Cachoeira Paulista - SP",
            description = "Evangelização, terço, missas e louvor católico.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 390
        ),
        RadioStation(
            id = "curated_evangelizar_familia",
            name = "Rádio Evangelizar Família",
            genre = "Católica",
            streamUrl = "https://8241.brasilstream.com.br/stream",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio8241.png",
            location = "Curitiba - PR",
            description = "Programação musical e espiritual voltada para a família.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 310
        ),
        RadioStation(
            id = "curated_catolica_brasil",
            name = "Rádio Católica Web Brasil",
            genre = "Católica",
            streamUrl = "https://stm5.painelcast.com:7574/stream",
            artworkUrl = null,
            location = "Brasil",
            description = "Música católica tradicional e contemporânea contínua.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 250
        ),

        // === MÚSICA CLÁSSICA (Alta prioridade) ===
        RadioStation(
            id = "curated_mec_fm_classica",
            name = "Rádio MEC FM 99.3 Clássica",
            genre = "Música Clássica",
            streamUrl = "https://radios.ebc.com.br/mecfm",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio416.png",
            location = "Rio de Janeiro - RJ",
            description = "Música clássica, ópera e orquestras de renome mundial.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 380
        ),
        RadioStation(
            id = "curated_swiss_classic",
            name = "Radio Swiss Classic",
            genre = "Música Clássica",
            streamUrl = "http://stream.srg-ssr.ch/m/rsc_de/mp3_128",
            artworkUrl = "https://www.radioswissclassic.ch/assets/images/rsc/rsc-logo.svg",
            location = "Suíça",
            description = "Música clássica de alta qualidade sem comerciais.",
            bitrate = "128 kbps",
            countryCode = "CH",
            language = "Alemão / Inglês",
            votes = 980
        ),
        RadioStation(
            id = "curated_venice_classic",
            name = "Venice Classic Radio Italia",
            genre = "Música Clássica",
            streamUrl = "http://174.36.206.197:8000/stream",
            artworkUrl = null,
            location = "Itália",
            description = "Obras-primas da Renascença, Barroco e Classicismo.",
            bitrate = "128 kbps",
            countryCode = "IT",
            language = "Italiano",
            votes = 640
        ),
        RadioStation(
            id = "curated_mozart_radio",
            name = "Calm Radio - Mozart",
            genre = "Música Clássica",
            streamUrl = "https://streams.calmradio.com:2128/stream",
            artworkUrl = null,
            location = "Internacional",
            description = "Dedicada integralmente às composições de Wolfgang Amadeus Mozart.",
            bitrate = "128 kbps",
            countryCode = "CA",
            language = "Inglês",
            votes = 420
        ),

        // === JAZZ & BLUES (Alta prioridade) ===
        RadioStation(
            id = "curated_swiss_jazz",
            name = "Radio Swiss Jazz",
            genre = "Jazz & Blues",
            streamUrl = "http://stream.srg-ssr.ch/m/rsj/mp3_128",
            artworkUrl = "https://www.radioswissjazz.ch/assets/images/rsj/rsj-logo.svg",
            location = "Suíça",
            description = "Jazz de todos os períodos, Swing, Bebop e Blues.",
            bitrate = "128 kbps",
            countryCode = "CH",
            language = "Inglês / Francês",
            votes = 1100
        ),
        RadioStation(
            id = "curated_1fm_blues",
            name = "1.FM Blues Radio",
            genre = "Jazz & Blues",
            streamUrl = "http://strm112.1.fm/blues_mobile_mp3",
            artworkUrl = null,
            location = "Internacional",
            description = "Delta Blues, Chicago Blues e clássicos de B.B. King e Muddy Waters.",
            bitrate = "128 kbps",
            countryCode = "US",
            language = "Inglês",
            votes = 780
        ),
        RadioStation(
            id = "curated_smooth_jazz_com",
            name = "SmoothJazz.com Global",
            genre = "Jazz & Blues",
            streamUrl = "http://sj128.hnux.com/",
            artworkUrl = null,
            location = "Estados Unidos",
            description = "A primeira estação de Smooth Jazz do mundo.",
            bitrate = "128 kbps",
            countryCode = "US",
            language = "Inglês",
            votes = 890
        ),

        // === CLASSIC ROCK ===
        RadioStation(
            id = "curated_89_rock",
            name = "89 FM A Rádio Rock",
            genre = "Classic Rock",
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_89FM_ADP.aac",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio89rock.png",
            location = "São Paulo - SP",
            description = "A maior rádio de Rock do Brasil, clássicos e novidades.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 850
        ),
        RadioStation(
            id = "curated_virgin_classic_rock",
            name = "Virgin Radio Classic Rock",
            genre = "Classic Rock",
            streamUrl = "http://icy.unitedradio.it/VirginRockClassics.mp3",
            artworkUrl = null,
            location = "Internacional",
            description = "Led Zeppelin, Pink Floyd, Queen, AC/DC, The Who e lendas do rock.",
            bitrate = "128 kbps",
            countryCode = "IT",
            language = "Italiano / Inglês",
            votes = 950
        ),

        // === LOVE SONGS & LOUNGE ===
        RadioStation(
            id = "curated_love_classics",
            name = "1.FM Love Classics",
            genre = "Love Songs",
            streamUrl = "http://strm112.1.fm/loveclassics_mobile_mp3",
            artworkUrl = null,
            location = "Internacional",
            description = "As canções mais românticas da história da música mundial.",
            bitrate = "128 kbps",
            countryCode = "US",
            language = "Inglês",
            votes = 620
        ),
        RadioStation(
            id = "curated_groove_salad",
            name = "SomaFM Groove Salad",
            genre = "Lounge & Chillout",
            streamUrl = "http://ice1.somafm.com/groovesalad-128-mp3",
            artworkUrl = "https://somafm.com/img3/groovesalad120.png",
            location = "San Francisco - US",
            description = "Ambiente suave, downtempo, chillout e batidas relaxantes.",
            bitrate = "128 kbps",
            countryCode = "US",
            language = "Inglês",
            votes = 1200
        ),

        // === SERTANEJO & MPB ===
        RadioStation(
            id = "curated_sertaneja_raiz",
            name = "Rádio Sertaneja Raiz",
            genre = "Sertanejo",
            streamUrl = "https://stm1.painelcast.com:6746/stream",
            artworkUrl = null,
            location = "Goiás - BR",
            description = "Tião Carreiro, Tonico & Tinoco, Milionário & José Rico.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 320
        ),
        RadioStation(
            id = "curated_novabrasil_mpb",
            name = "NovaBrasil FM MPB",
            genre = "MPB & Bossa Nova",
            streamUrl = "https://playerservices.streamtheworld.com/api/livestream-redirect/NOVABRASIL_SP_ADP.aac",
            artworkUrl = "https://s3.amazonaws.com/media.radios.com.br/share/banner/radio484.png",
            location = "São Paulo - SP",
            description = "O melhor da Música Popular Brasileira 24 horas por dia.",
            bitrate = "128 kbps",
            countryCode = "BR",
            language = "Português",
            votes = 560
        )
    )

    // Consulta estações reais da Radio Browser por categoria com fallback inteligente
    suspend fun getStationsForCategory(category: RadioCategory, limit: Int = 40): List<RadioStation> = withContext(Dispatchers.IO) {
        // Verifica se já está em cache
        categoryCache[category.id]?.let { return@withContext it }

        val combinedList = mutableListOf<RadioStation>()
        val seenUuids = mutableSetOf<String>()

        // 1. Adiciona as estações curadas locais desta categoria (com filtro estrito anti-mistura)
        val curatedMatching = curatedStations.filter { st ->
            val text = "${st.name} ${st.genre} ${st.description}".lowercase()
            val isExcluded = category.excludedKeywords.any { excluded ->
                text.contains(excluded.lowercase())
            }
            if (isExcluded) return@filter false

            st.genre.equals(category.name, ignoreCase = true) ||
            category.tags.any { tag -> text.contains(tag.lowercase()) }
        }
        for (st in curatedMatching) {
            seenUuids.add(st.id)
            combinedList.add(st)
        }

        // 2. Consulta a API real da Radio Browser para a primaryTag e tags secundárias
        for (tag in category.tags.take(3)) {
            try {
                val api = getApi()
                val dtoList = api.searchStations(
                    tag = tag,
                    hideBroken = true,
                    order = "votes",
                    reverse = true,
                    limit = 35
                )

                for (dto in dtoList) {
                    val uuid = dto.stationUuid ?: continue
                    val streamUrl = dto.urlResolved ?: dto.url ?: continue
                    if (streamUrl.isBlank() || seenUuids.contains(uuid)) continue

                    // FILTRAGEM RIGOROSA DE GÊNERO:
                    val stName = dto.name ?: ""
                    val stTags = dto.tags ?: ""
                    val fullText = "$stName $stTags".lowercase()

                    // Descarta qualquer estação que contenha palavras excluídas desta categoria
                    val isExcluded = category.excludedKeywords.any { excluded ->
                        fullText.contains(excluded.lowercase())
                    }
                    if (isExcluded) continue

                    seenUuids.add(uuid)
                    combinedList.add(dto.toRadioStation(category.name))
                    if (combinedList.size >= limit) break
                }
            } catch (e: Exception) {
                rotateMirror()
            }
            if (combinedList.size >= limit) break
        }

        categoryCache[category.id] = combinedList
        combinedList
    }

    // Busca geral na Radio Browser com filtros de gênero, país e idioma
    suspend fun searchStations(
        query: String = "",
        categoryName: String? = null,
        countryCode: String? = null,
        language: String? = null,
        limit: Int = 40
    ): List<RadioStation> = withContext(Dispatchers.IO) {
        val cacheKey = "${query.trim().lowercase()}_${categoryName ?: ""}_${countryCode ?: ""}_${language ?: ""}"
        searchCache[cacheKey]?.let { return@withContext it }

        val results = mutableListOf<RadioStation>()
        val seenUuids = mutableSetOf<String>()

        // Filtro prévio nas curadas se houver query
        if (query.isNotBlank()) {
            val localMatches = curatedStations.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.genre.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            for (st in localMatches) {
                seenUuids.add(st.id)
                results.add(st)
            }
        }

        try {
            val api = getApi()
            val tagParam = if (!categoryName.isNullOrBlank() && categoryName != "Todas") {
                val cat = categories.find { it.name.equals(categoryName, ignoreCase = true) || it.id == categoryName }
                cat?.primaryTag ?: categoryName.lowercase()
            } else null

            val dtoList = api.searchStations(
                name = query.takeIf { it.isNotBlank() },
                tag = tagParam,
                countryCode = countryCode?.takeIf { it.isNotBlank() },
                language = language?.takeIf { it.isNotBlank() },
                hideBroken = true,
                order = "votes",
                reverse = true,
                limit = limit
            )

            for (dto in dtoList) {
                val uuid = dto.stationUuid ?: continue
                val streamUrl = dto.urlResolved ?: dto.url ?: continue
                if (streamUrl.isBlank() || seenUuids.contains(uuid)) continue

                seenUuids.add(uuid)
                val defaultGenre = categoryName?.takeIf { it != "Todas" } ?: dto.tags?.split(",")?.firstOrNull()?.trim()?.replaceFirstChar { it.uppercase() } ?: "Rádio Online"
                results.add(dto.toRadioStation(defaultGenre))
            }
        } catch (e: Exception) {
            rotateMirror()
        }

        if (results.isNotEmpty()) {
            searchCache[cacheKey] = results
        }

        results
    }

    // Registra clique/acesso da estação conforme exigido pela documentação oficial da Radio Browser
    suspend fun registerStationClick(stationUuid: String) = withContext(Dispatchers.IO) {
        if (stationUuid.startsWith("curated_")) return@withContext
        try {
            getApi().registerClick(stationUuid)
        } catch (_: Exception) {}
    }

    private fun RadioBrowserStationDto.toRadioStation(defaultGenre: String): RadioStation {
        val streamUrl = this.urlResolved ?: this.url ?: ""
        val tagsList = this.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        val genre = tagsList.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: defaultGenre

        val loc = when {
            !this.state.isNullOrBlank() && !this.country.isNullOrBlank() -> "${this.state} - ${this.country}"
            !this.country.isNullOrBlank() -> this.country
            else -> "Internacional"
        }

        val br = if ((this.bitrate ?: 0) > 0) "${this.bitrate} kbps" else "128 kbps"

        return RadioStation(
            id = this.stationUuid ?: "radio_${this.name.hashCode()}",
            name = this.name?.trim() ?: "Rádio ao Vivo",
            genre = genre,
            streamUrl = streamUrl,
            artworkUrl = this.favicon?.takeIf { it.isNotBlank() },
            location = loc,
            description = "Transmissão digital ao vivo via Radio Browser (${this.codec ?: "MP3"}, $br)",
            bitrate = br,
            countryCode = this.countryCode ?: "BR",
            language = this.language ?: "Português",
            votes = this.votes ?: 0,
            codec = this.codec ?: "MP3"
        )
    }
}
