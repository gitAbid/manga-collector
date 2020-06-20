package core.mangacollector.service


import core.mangacollector.model.LatestMangaUpdate
import core.mangacollector.model.Manga
import core.mangacollector.repository.LatestUpdateRepository
import core.mangacollector.repository.MangaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service


@Service
class MangaServiceImpl(val mangaRepository: MangaRepository,
                       val latestUpdateRepository: LatestUpdateRepository) : MangaService {
    override fun getAllMangas(page: Int?, pageSize: Int?, sort: String?): Page<LatestMangaUpdate> {
        val pageNumber = page?.let { page } ?: 0
        val size = page?.let { pageSize } ?: 10
        val sortBy = sort?.let { sort } ?: "mangaName"
        return latestUpdateRepository.findAll(PageRequest.of(pageNumber, size, Sort.by(sortBy).ascending()))
    }

    override fun getMangaBySourceUrl(url: String): Manga? {
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

}