package core.mangacollector.repository

import core.mangacollector.model.Manga
import core.mangacollector.model.MangaCompact
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MangaCompactRepository : MongoRepository<MangaCompact, String> {
    fun findByMangaNameLike(name: String, pageable: Pageable): Page<MangaCompact>
    fun findByMangaUrl(url: String): MangaCompact?
    fun findByMangaName(name: String): MangaCompact?
    fun findByTrendingTrue(pageable: Pageable): Page<MangaCompact>
    fun findByMostPopularTrue(pageable: Pageable): Page<MangaCompact>
}