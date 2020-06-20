package core.mangacollector.controller

import core.mangacollector.model.Manga
import core.mangacollector.model.MangaCompact
import core.mangacollector.model.RequestBySrcUrl
import core.mangacollector.service.MangaService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/manga")
class MangaRestController(val mangaService: MangaService) {

    @GetMapping(path = ["/all"])
    fun getAllMangas(@RequestParam(defaultValue = "0") pageNo: Int,
                     @RequestParam(defaultValue = "10") pageSize: Int,
                     @RequestParam(defaultValue = "ASC") orderBy: String,
                     @RequestParam(defaultValue = "id") sortBy: String): Page<MangaCompact> {
        return mangaService.getAllMangas(pageNo, pageSize, sortBy, orderBy);
    }

    @GetMapping(path = ["/trending"])
    fun getTrendingMangas(@RequestParam(defaultValue = "0") pageNo: Int,
                          @RequestParam(defaultValue = "10") pageSize: Int,
                          @RequestParam(defaultValue = "DESC") orderBy: String,
                          @RequestParam(defaultValue = "viewCount") sortBy: String): Page<MangaCompact> {
        return mangaService.getTrendingMangas(pageNo, pageSize, sortBy, orderBy);
    }


    @GetMapping(path = ["/most-popular"])
    fun getMostPopularMangas(@RequestParam(defaultValue = "0") pageNo: Int,
                             @RequestParam(defaultValue = "10") pageSize: Int,
                             @RequestParam(defaultValue = "DESC") orderBy: String,
                             @RequestParam(defaultValue = "viewCount") sortBy: String): Page<MangaCompact> {
        return mangaService.getMostPopularMangas(pageNo, pageSize, sortBy, orderBy);
    }


    @GetMapping(path = ["/id/{id}"])
    fun getMangaById(@PathVariable id: String): Manga? {
        return mangaService.getMangaById(id);
    }

    @GetMapping(path = ["/genres"])
    fun getMangaById(): List<String> {
        return mangaService.getGenres();
    }

    @PostMapping(path = ["/srcUrl"])
    fun getMangaBySourceUrl(@RequestBody requestBySrcUrl: RequestBySrcUrl): Manga? {
        return mangaService.getMangaBySourceUrl(requestBySrcUrl.mangaUrl);
    }
}
