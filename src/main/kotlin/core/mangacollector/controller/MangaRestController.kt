package core.mangacollector.controller

import core.mangacollector.model.LatestMangaUpdate
import core.mangacollector.model.Manga
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
                     @RequestParam(defaultValue = "id") sortBy: String): Page<LatestMangaUpdate> {
        return mangaService.getAllMangas(pageNo, pageSize, sortBy);
    }

    @GetMapping(path = ["/name/{name}"])
    fun getMangasByName(@PathVariable name: String,
                        @RequestParam(defaultValue = "0") pageNo: Int,
                        @RequestParam(defaultValue = "10") pageSize: Int,
                        @RequestParam(defaultValue = "id") sortBy: String): Page<Manga> {
        return mangaService.getMangasByName(name, pageNo, pageSize, sortBy);
    }

    @GetMapping(path = ["/id/{id}"])
    fun getMangaById(@PathVariable id: String): Manga? {
        return mangaService.getMangaById(id);
    }
    @PostMapping(path = ["/srcUrl"])
    fun getMangaBySourceUrl(@RequestBody requestBySrcUrl: RequestBySrcUrl): Manga? {
        return mangaService.getMangaBySourceUrl(requestBySrcUrl.mangaUrl);
    }
}
