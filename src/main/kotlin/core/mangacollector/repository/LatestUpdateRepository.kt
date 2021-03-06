package core.mangacollector.repository

import core.mangacollector.model.LatestMangaUpdate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LatestUpdateRepository : MongoRepository<LatestMangaUpdate, String> {
    fun findByMangaName(name: String): LatestMangaUpdate?
    fun findByMangaUrl(name: String): List<LatestMangaUpdate>
    fun findByChapterUpdatedTrue(pageable: Pageable): Page<LatestMangaUpdate>
}