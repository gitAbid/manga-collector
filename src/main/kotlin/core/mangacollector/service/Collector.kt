package core.mangacollector.service

import core.mangacollector.model.Chapter
import core.mangacollector.model.LatestMangaUpdate
import core.mangacollector.model.Manga
import core.mangacollector.repository.ChapterRepository
import core.mangacollector.repository.LatestUpdateRepository
import core.mangacollector.repository.MangaRepository
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

@Service
class Collector(var luRepo: LatestUpdateRepository,
                var mangaRepository: MangaRepository,
                var chapterRepository: ChapterRepository) {
    val logger = LoggerFactory.getLogger(Collector::class.java)

    @PostConstruct
    fun init() {
        logger.info("Collector initialized")
        timeElapsedToExecute {
            sourceFromManganelo()
            //chapterUrlFixer()
            //brokenImageUrl()
            latestUpdate()
            mangaDetailsCollector()
        }
    }

    private fun sourceFromManganelo() {
        val mangas = luRepo.findAll()
        val nameSet = HashSet<String>()
        mangas.forEach { manga ->
            if (manga.mangaUrl.startsWith("https://manganelo.com/")) {
                nameSet.add(manga.mangaName)
            }
        }
        logger.info("Source From Chapter URL $nameSet")
        logger.info("Source From Chapter Size ${nameSet.size}")

        nameSet.forEach {
            val latesManga = luRepo.findByMangaName(it)
            latesManga?.let {
                latesManga.isChapterUpdated = true
                luRepo.save(latesManga)
            }
        }
    }

    private fun chapterUrlFixer() {
        val mangas = mangaRepository.findAll()
        for (manga in mangas) {
            val updatedChapters = LinkedHashSet<Chapter>()
            val chapters = manga.chapters
            for (chapter in chapters) {
                val updatedImages = LinkedHashSet<String>()
                val images = chapter.chapterImages
                for (image in images) {
                    updatedImages.add(image.replace(Regex("(s\\d+\\.m)"), "s8.m").replace(Regex("((\\d+\\.com))"), "8.com"))
                }
                chapter.chapterImages = updatedImages
                updatedChapters.add(chapter)
            }
            manga.chapters = updatedChapters
            mangaRepository.save(manga)
        }
    }

    private fun brokenImageUrl() {
        val mangas = mangaRepository.findAll()
        val nameSet = HashSet<String>()
        mangas.forEach { manga ->
            manga.chapters.forEach { chapter ->
                chapter.chapterImages.forEach { image ->
                    if (image.split(".").size > 4) {
                        logger.info("ImageURL: $image")
                        logger.info("MangaNamr: ${manga.mangaName}")
                        nameSet.add(manga.mangaName)

                    }
                }
            }
        }
        logger.info("Broken Chapter URL $nameSet")
        nameSet.forEach { name ->
            val latestManga = luRepo.findByMangaName(name)
            latestManga?.let {
                logger.info("Collecting details for manga :${latestManga.mangaName}")
                collectMangaDetail(latestManga)
                logger.info("Collecting details finished for manga :${latestManga.mangaName}")
            }
        }
    }

    //@Scheduled(fixedRate = 900000)
    fun latestUpdate() {
        logger.info("Running latest manga updates collector")
        val initDoc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=1").get()
        val lastPageString = initDoc.select(".page_last").text()
                .toString()
                .replace(Regex("[A-z]"), "")
                .replace(Regex("\\("), "")
                .replace(Regex("\\)"), "")

        val firstPage = 1
        val lastPage = 5

        timeElapsedToExecute {
            for (page in firstPage..lastPage) {
                val doc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=$page").get()
                logger.info("$page")
                val elements = doc.select(".list-truyen-item-wrap")
                var name: String
                var mangaUrl: String
                var description: String
                var latestChapterUrl: String
                var coverImageUrl: String
                var viewCount: Long
                elements.forEach {
                    name = it.select("h3").text();
                    mangaUrl = it.select("a[href]").first().attr("abs:href")
                    description = it.select("p").text()
                    name = it.select("h3").text();
                    viewCount = it.select(".aye_icon").text().replace(Regex(","), "")?.toLong()
                    val link = it.select(".list-story-item-wrap-chapter").select("a[href]").first();
                    latestChapterUrl = link.attr("abs:href")
                    coverImageUrl = it.select("img").first().attr("abs:src")

                    luRepo.findByMangaName(name)?.let { mangaUpdate ->
                        mangaUpdate.mangaName = name
                        mangaUpdate.description = description
                        mangaUpdate.mangaUrl = mangaUrl
                        mangaUpdate.cover = coverImageUrl
                        if (mangaUpdate.latestChapter != latestChapterUrl) {
                            mangaUpdate.lastChapterUpdated = Date()
                            mangaUpdate.isChapterUpdated = true
                        }
                        mangaUpdate.viewCount = viewCount
                        mangaUpdate.latestChapter = latestChapterUrl
                        luRepo.save(mangaUpdate)

                    } ?: run {
                        luRepo.save(LatestMangaUpdate(
                                mangaName = name,
                                description = description,
                                mangaUrl = mangaUrl,
                                cover = coverImageUrl,
                                latestChapter = latestChapterUrl,
                                viewCount = viewCount,
                                isChapterUpdated = true
                        ))
                    }

                }
            }
        }
        logger.info("Finished running latest manga updates collector")
    }


    //@Scheduled(fixedRate = 300000)
    fun mangaDetailsCollector() {
        logger.info("Running manga details collector")
        val mangas = luRepo.findAll()
        mangas.filter { latestMangaUpdate -> latestMangaUpdate.isChapterUpdated }.forEach {
            try {
                collectMangaDetail(it);
                it.isChapterUpdated = false
                luRepo.save(it)
            } catch (e: Exception) {
                logger.error("Error occurred while getting manga details", e)
            }
        }
        logger.info("Finished running manga details collector")

    }

    private fun collectMangaDetail(latestManga: LatestMangaUpdate) {
        logger.info("Collecting from url: ${latestManga.mangaUrl}")
        val doc = Jsoup.connect(latestManga.mangaUrl).get()
        val cover: String
        val authors = LinkedHashSet<String>()
        val genres = LinkedHashSet<String>()
        var status = ""
        var mangaLastUpdated = ""
        var rating: Double? = 0.0
        val description: String
        val chapters = LinkedHashSet<Chapter>()

        if (latestManga.mangaUrl.startsWith("https://mangakakalot.com/")) {
            cover = doc.select(".manga-info-pic").select("img").first().attr("abs:src")
            val elements = doc.select(".manga-info-text").select("li")
            description = doc.select("#noidungm").text();
            elements?.forEach {
                if (it.text().contains("Author")) {
                    val authorLinks = it.select("a[href]")
                    authorLinks?.forEach { author ->
                        authors.add(author.text())
                    }
                } else if (it.text().contains("Status")) {
                    val statusArr = it.text().split(":")
                    status = statusArr[1]?.trim()
                } else if (it.text().contains("Last updated")) {
                    val updateArr = it.text().split(":")
                    mangaLastUpdated = updateArr[1]?.trim()
                } else if (it.text().contains("Genres")) {
                    val genresLink = it.select("a[href]")
                    genresLink?.forEach { author ->
                        genres.add(author.text())
                    }
                }
            }
            rating = try {
                doc.select("#rate_row_cmd")?.text()?.split("/")?.get(0)?.split(":")?.get(1)?.trim()?.toDouble()
            } catch (e: Exception) {
                logger.error("Error occurred while getting rating for ${latestManga.mangaName}")
                0.0
            }
            doc.select(".chapter-list").select("a[href]")
            val chapterLinks = doc.select(".chapter-list").select("a[href]");
            chapterLinks?.forEach {
                val chapterLink = it.attr("abs:href")
                val chapterName = it.text()
                val chapterImages = collectChapterImagesFromMangakakalotUrl(chapterLink)
                chapters.add(Chapter(
                        chapterName = chapterName,
                        chapterLink = chapterLink,
                        chapterImages = chapterImages
                ))
            }
            logger.info(latestManga.mangaName)
            logger.info(latestManga.mangaUrl)
            logger.info(cover)
            logger.info(authors.toString())
            logger.info(genres.toString())
            logger.info(status)
            logger.info(mangaLastUpdated)
            logger.info(rating.toString())

            mangaRepository.findByMangaName(latestManga.mangaName)?.let {
                it.mangaName = latestManga.mangaName
                it.mangaUrl = latestManga.mangaUrl
                it.viewCount = latestManga.viewCount
                it.latestChapter = latestManga.latestChapter
                it.description = description
                it.cover = cover
                it.authors = authors
                it.genres = genres
                it.status = status
                it.mangaLastUpdated = mangaLastUpdated
                it.rating = rating
                it.chapters = chapters
                it.lastUpdated = Date()
                mangaRepository.save(it)

            } ?: run {
                mangaRepository.save(Manga(
                        mangaName = latestManga.mangaName,
                        mangaUrl = latestManga.mangaUrl,
                        viewCount = latestManga.viewCount,
                        latestChapter = latestManga.latestChapter,
                        description = description,
                        cover = cover,
                        authors = authors,
                        genres = genres,
                        status = status,
                        mangaLastUpdated = mangaLastUpdated,
                        rating = rating,
                        chapters = chapters
                ))
            }


        } else if (latestManga.mangaUrl.startsWith("https://manganelo.com/")) {
            logger.error(latestManga.mangaUrl)
            cover = doc.select(".info-image").select("img").first().attr("abs:src")
            description = doc.select(".panel-story-info-description").text();
            val upperTable = doc.select(".variations-tableInfo").select(".table-value");
            upperTable?.let {
                val authorLinks = it[1].select("a[href]")
                authorLinks?.forEach { author ->
                    authors.add(author.text())
                }
                status = try {
                    it[2].text()
                } catch (e: Exception) {
                    "-"
                }
                try {
                    val genresLink = it[3].select("a[href]")
                    genresLink?.forEach { author ->
                        genres.add(author.text())
                    }
                } catch (e: Exception) {
                }
            }
            mangaLastUpdated = try {
                doc.select(".stre-value")[0].text()
            } catch (e: Exception) {
                ""
            }

            rating = try {
                doc.select("#rate_row_cmd")?.text()?.split("/")?.get(0)?.split(":")?.get(1)?.trim()?.toDouble()
            } catch (e: Exception) {
                logger.error("Error occurred while getting rating for ${latestManga.mangaName}")
                0.0
            }


            val chapterLinks = doc.select(".row-content-chapter").select("a[href]")
            chapterLinks?.forEach {
                val chapterLink = it.attr("abs:href")
                val chapterName = it.text()
                val chapterImages = collectChapterImagesManganeloFromUrl(chapterLink)
                chapters.add(Chapter(
                        chapterName = chapterName,
                        chapterLink = chapterLink,
                        chapterImages = chapterImages
                ))
            }
            logger.info(latestManga.mangaName)
            logger.info(latestManga.mangaUrl)
            logger.info(cover)
            logger.info(authors.toString())
            logger.info(genres.toString())
            logger.info(status)
            logger.info(mangaLastUpdated)
            logger.info(rating.toString())

            mangaRepository.findByMangaName(latestManga.mangaName)?.let {
                it.mangaName = latestManga.mangaName
                it.mangaUrl = latestManga.mangaUrl
                it.viewCount = latestManga.viewCount
                it.latestChapter = latestManga.latestChapter
                it.description = description
                it.cover = cover
                it.authors = authors
                it.genres = genres
                it.status = status
                it.mangaLastUpdated = mangaLastUpdated
                it.rating = rating
                it.chapters = chapters
                it.lastUpdated = Date()
                mangaRepository.save(it)

            } ?: run {
                mangaRepository.save(Manga(
                        mangaName = latestManga.mangaName,
                        mangaUrl = latestManga.mangaUrl,
                        viewCount = latestManga.viewCount,
                        latestChapter = latestManga.latestChapter,
                        description = description,
                        cover = cover,
                        authors = authors,
                        genres = genres,
                        status = status,
                        mangaLastUpdated = mangaLastUpdated,
                        rating = rating,
                        chapters = chapters
                ))
            }

        }

    }

    private fun collectChapterImagesManganeloFromUrl(chapterLink: String?): java.util.LinkedHashSet<String> {
        val chapterImages = LinkedHashSet<String>()
        val doc = Jsoup.connect(chapterLink).get()
        val images = doc.select(".container-chapter-reader").select("img")
        images?.forEach {
            val imageLink = it.attr("abs:src")
            chapterImages.add(imageLink)
        }
        return chapterImages
    }


    private fun collectChapterImagesFromMangakakalotUrl(chapterLink: String?): LinkedHashSet<String> {
        val chapterImages = LinkedHashSet<String>()
        val doc = Jsoup.connect(chapterLink).get()
        val images = doc.select(".vung-doc").select("img")
        images?.forEach {
            val imageLink = it.attr("abs:src")
            chapterImages.add(imageLink)
        }
        return chapterImages
    }

    fun timeElapsedToExecute(function: () -> Unit) {
        logger.info("Started ${Date()}")
        function.invoke()
        logger.info("Finished ${Date()}")
    }
}
