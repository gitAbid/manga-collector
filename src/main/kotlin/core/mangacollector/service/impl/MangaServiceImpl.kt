package core.mangacollector.service


import core.mangacollector.model.Manga
import core.mangacollector.model.MangaCompact
import core.mangacollector.repository.MangaCompactRepository
import core.mangacollector.repository.MangaRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service


@Service
class MangaServiceImpl(val mangaRepository: MangaRepository,
                       val mangaCompactRepository: MangaCompactRepository) : MangaService {

    @Value("#{'\${genres.list}'.split(',')}")
    var genresList: List<String> = listOf<String>()

    override fun getAllMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact> {
        val pageNumber = page?.let { page } ?: 0
        val size = page?.let { pageSize } ?: 10
        val sortBy = sort?.let { sort } ?: "viewCount"
        val orderBy = order?.let {
            if (it == "ASC") {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        } ?: Sort.Direction.DESC

        return mangaCompactRepository.findAll(PageRequest.of(pageNumber, size, Sort.by(orderBy, sortBy)))
    }

    override fun getMangaBySourceUrl(url: String): List<Manga>? {
        return mangaRepository.findByMangaUrl(url)
    }

    override fun getMangasByName(name: String, page: Int?, pageSize: Int?, sort: String?): Page<Manga> {
        val pageNumber = page?.let { page } ?: 0
        val sortBy = sort?.let { sort } ?: "mangaName"
        val size = page?.let { pageSize } ?: 10
        return mangaRepository.findByMangaNameLike(name, PageRequest.of(pageNumber, size, Sort.by(sortBy).ascending()))
    }

    override fun getMangaById(id: String): Manga? {
        return mangaRepository.findById(id).let {
            if (it.isPresent) {
                it.get()
            } else {
                null
            }
        }
    }

    override fun getTrendingMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact> {
        val pageNumber = page?.let { page } ?: 0
        val size = page?.let { pageSize } ?: 10
        val sortBy = sort?.let { sort } ?: "viewCount"
        val orderBy = order?.let {
            if (it == "ASC") {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        } ?: Sort.Direction.DESC
        return mangaCompactRepository.findByTrendingTrue(PageRequest.of(pageNumber, size, Sort.by(orderBy, sortBy)))
    }

    override fun getMostPopularMangas(page: Int?, pageSize: Int?, sort: String?, order: String?): Page<MangaCompact> {
        val pageNumber = page?.let { page } ?: 0
        val size = page?.let { pageSize } ?: 10
        val sortBy = sort?.let { sort } ?: "viewCount"
        val orderBy = order?.let {
            if (it == "ASC") {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        } ?: Sort.Direction.DESC

        return mangaCompactRepository.findByMostPopularTrue(PageRequest.of(pageNumber, size, Sort.by(orderBy, sortBy)))
    }

    override fun getGenres(): List<String> {
        return genresList;
    }

}