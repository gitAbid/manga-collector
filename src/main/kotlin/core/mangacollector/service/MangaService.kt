package core.mangacollector.service

import core.mangacollector.model.LatestMangaUpdate
import core.mangacollector.model.Manga
import org.springframework.data.domain.Page

interface MangaService {
    fun getAllMangas(page: Int?, pageSize: Int?, sort: String?): Page<LatestMangaUpdate>
    fun getMangasByName(name: String, page: Int?, pageSize: Int?, sort: String?): Page<Manga>
    fun getMangaById(id: String): Manga?
    fun getMangaBySourceUrl(url: String): Manga?
}