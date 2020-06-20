package core.mangacollector.repository

import core.mangacollector.model.Manga
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MangaRepository : MongoRepository<Manga, String> {
    fun findByMangaNameLike(name: String, pageable: Pageable): Page<Manga>
    fun findByMangaUrl(url: String): Manga?
    fun findByMangaName(name: String): Manga?
    fun findByTrendingTrue(pageable: Pageable): Page<Manga>
    fun findByMostPopularTrue(pageable: Pageable): Page<Manga>
}