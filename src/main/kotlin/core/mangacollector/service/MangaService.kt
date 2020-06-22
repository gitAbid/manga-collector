package core.mangacollector.service

import core.mangacollector.model.Manga
import core.mangacollector.model.MangaCompact
import org.springframework.data.domain.Page

interface MangaService {
    fun getAllMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact>
    fun getMangasByName(name: String, page: Int?, pageSize: Int?, sort: String?): Page<Manga>
    fun getMangaById(id: String): Manga?
    fun getMangaBySourceUrl(url: String): List<Manga>?
    fun getTrendingMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact>
    fun getMostPopularMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact>
    fun getGenres(): List<String>
}