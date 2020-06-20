package core.mangacollector.service

import core.mangacollector.model.*
import core.mangacollector.repository.*
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet


@Service
class Collector(val luRepo: LatestUpdateRepository,
                val mangaRepository: MangaRepository,
                val updateStatusRepository: UpdateStatusRepository ) {
    val logger = LoggerFactory.getLogger(Collector::class.java)

    @PostConstruct
    fun init() {
        logger.info("Collector initialized")
    }

    fun brokenUrlFixer() {
        val page = 1;
        val pageItem = 10;
        var updates = updateStatusRepository.findAll(PageRequest.of(page, pageItem));
        logger.info("Performing batch url fixes for ${updates.totalPages} pages for ${updates.totalElements} items")
        for (x in 1..updates.totalPages) {
            logger.info("Performing url fixes for batch : $x")
            updates = updateStatusRepository.findAll(PageRequest.of(x, pageItem));
            updates.content.forEach { update ->
                val manga = mangaRepository.findByMangaUrl(update.mangaUrl)
                manga?.let {
                    logger.debug("Fixing broken chapter for manga : ${manga.mangaName}")
                    val updatedChapters = LinkedHashSet<Chapter>()
                    val chapters = manga.chapters
                    for (chapter in chapters) {
                        val updatedImages = LinkedHashSet<String>()
                        val images = chapter.chapterImages
                        for (image in images) {
                            var updated = image.replace(Regex("(s\\d+\\.m)"), "s8.m").replace(Regex("(\\d+\\.com)"), "8.com")
                            updated = updated.replace("https://bu.mkklcdnbuv8.com", "https://bu.mkklcdnbuv1.com")
                            updatedImages.add(updated)
                        }
                        chapter.chapterImages = updatedImages
                        updatedChapters.add(chapter)
                    }
                    manga.chapters = updatedChapters
                    mangaRepository.save(manga)
                    logger.info("Removing updates from table : ${update}")
                    updateStatusRepository.delete(update)
                } ?: run {
                    logger.info("No manga found to fix url with name : ${update.mangaUrl}")
                }
            }
        }

    }

    fun brokenUrlFixerFull() {
        val page = 1;
        val pageItem = 10;
        var updates = mangaRepository.findAll(PageRequest.of(page, pageItem));
        logger.info("Performing batch url fixes for ${updates.totalPages} pages for ${updates.totalElements} items")
        for (x in 1..updates.totalPages) {
            logger.info("Performing url fixes for batch : $x")
            updates = mangaRepository.findAll(PageRequest.of(x, pageItem));
            updates.content.forEach { update ->
                val manga = mangaRepository.findByMangaUrl(update.mangaUrl)
                manga?.let {
                    logger.debug("Fixing broken chapter for manga : ${manga.mangaName}")
                    val updatedChapters = LinkedHashSet<Chapter>()
                    val chapters = manga.chapters
                    for (chapter in chapters) {
                        val updatedImages = LinkedHashSet<String>()
                        val images = chapter.chapterImages
                        for (image in images) {
                            var updated = image.replace(Regex("(s\\d+\\.m)"), "s8.m").replace(Regex("(\\d+\\.com)"), "8.com")
                            updated = updated.replace("https://bu.mkklcdnbuv8.com", "https://bu.mkklcdnbuv1.com")
                            updatedImages.add(updated)
                        }
                        chapter.chapterImages = updatedImages
                        updatedChapters.add(chapter)
                    }
                    manga.chapters = updatedChapters
                    mangaRepository.save(manga)
                } ?: run {
                    logger.info("No manga found to fix url with name : ${update.mangaUrl}")
                }
                return
            }
            return
        }

    }
    fun brokenChapterCollector() {
        val page = 1;
        val pageItem = 10;
        var mangas = mangaRepository.findAll(PageRequest.of(page, pageItem))
        val nameSet = HashSet<String>()
        logger.info("Performing batch chapter fixes for ${mangas.totalPages} pages for ${mangas.totalElements} items")
        for (x in 1..mangas.totalPages) {
            logger.info("Performing chapter fixes for batch : $x")
            mangas = mangaRepository.findAll(PageRequest.of(x, pageItem))
            mangas.content.forEach { manga ->
                logger.info("Searching broken chapter for manga : ${manga.mangaName}")
                manga.chapters.forEach { chapter ->
                    chapter.chapterImages.forEach { image ->
                        if (image.startsWith("https://bu.mkklcdnbuv8.com") || image.split(".").size > 4) {
                            nameSet.add(manga.mangaName)
                        }
                    }
                }
            }
        }
        logger.info("Broken Chapter URL $nameSet")
        nameSet.forEach { name ->
            val latestManga = luRepo.findByMangaName(name)
            latestManga?.let {
                logger.info("Collecting broken chapter details for  manga :${latestManga.mangaName}")
                collectMangaDetail(latestManga)
                logger.info("Collecting broken chapter details finished for manga :${latestManga.mangaName}")
            }

        }

    }

    fun latestMangaCollector(page: Int) {
        logger.info("Running latest manga updates collector for last page: $page")
        val firstPage = 1
        val lastPage = page
        timeElapsedToExecute {
            for (currentPage in firstPage..lastPage) {
                val doc = Jsoup.connect("https://mangakakalot.com/manga_list?type=latest&category=all&state=all&currentPage=$currentPage").get()
                logger.info("$currentPage")
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
                            mangaUpdate.chapterUpdated = true
                            updateStatusRepository.save(UpdateStatus(
                                    mangaUrl = mangaUrl,
                                    lastChapter = latestChapterUrl
                            ))
                        }
                        mangaUpdate.lastUpdated = Date()
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
                                chapterUpdated = true
                        ))
                    }

                }
            }
        }
        logger.info("Finished running latest manga updates collector")
    }

    fun mangaDetailsCollector() {
        logger.info("Running manga details collector")
        val page = 1;
        val pageItem = 20;
        var mangas = luRepo.findByChapterUpdatedTrue(PageRequest.of(page, pageItem))
        logger.info("Performing batch manga collecton for ${mangas.totalPages} pages for ${mangas.totalElements} items")
        for (x in 1..mangas.totalPages) {
            logger.info("Performing manga collecton for batch : $x")
            mangas = luRepo.findByChapterUpdatedTrue(PageRequest.of(x, pageItem))
            mangas.content.forEach {
                try {
                    collectMangaDetail(it);
                    it.chapterUpdated = false
                    luRepo.save(it)
                } catch (e: Exception) {
                    logger.error("Error occurred while getting manga details", e)
                }
            }
        }
        logger.info("Finished running manga details collector")

    }

    private fun collectMangaDetail(latestManga: LatestMangaUpdate) {
        logger.info("Collecting from url: ${latestManga.mangaUrl}")
        val doc = Jsoup.connect(latestManga.mangaUrl).timeout(300000).get()
        var cover = ""
        val authors = LinkedHashSet<String>()
        val genres = LinkedHashSet<String>()
        var status = ""
        var mangaLastUpdated = ""
        var rating: Double? = 0.0
        var description = ""
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
                    genresLink?.forEach { genre ->
                        genres.add(genre.text())
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
        logger.info("Name: ${latestManga.mangaName}")
        logger.info("MangaUrl: ${latestManga.mangaUrl}")
        logger.info("Cover: $cover")
        logger.info("Authors: $authors")
        logger.info("Genres: $genres")
        logger.info("Status: $status")
        logger.info("MangaLastUpdated: ${mangaLastUpdated}")
        logger.info("Rating: ${rating}")
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
        val start = Date();
        logger.info("Started $start")
        function.invoke()
        val end = Date();
        logger.info("Finished $end")
        val diffInMillisec: Long = end.getTime() - start.getTime()
        val diffInMin = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec)
        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec)
        logger.info("Time took to finish $diffInSec seconds or  $diffInMin minutes")
    }

    fun collectTrending() {
        logger.info("Start collecting trending manga")
        resetTrendingTag()
        val doc = Jsoup.connect("https://manganelo.com/").timeout(300000).get()
        val trendingDoc = doc.select(".owl-carousel").select(".slide-caption");
        trendingDoc?.forEach { trend ->
            val url = trend.select(".a-h").first().select("a").attr("href")
            mangaRepository.findByMangaUrl(url)?.let {
                it.trending = true
                mangaRepository.save(it)
            }
        }

        logger.info("Finished collecting trending manga")

    }

    fun collectMostPopular() {
        logger.info("Start collecting most popular manga")
        resetMostPopularTag();
        val doc = Jsoup.connect("https://mangakakalot.com/").timeout(300000).get()
        val mostPopularDoc = doc.select(".owl-carousel").select(".slide-caption")
        mostPopularDoc?.forEach { popular ->
            val url = popular.select("a[href]").first().select("a").attr("href")
            mangaRepository.findByMangaUrl(url)?.let {
                it.mostPopular = true
                mangaRepository.save(it)
            }
        }
        logger.info("Finished collecting most popular manga.")
    }


    fun resetTrendingTag() {
        val page = 1;
        val pageItem = 10;
        var resetContent = mangaRepository.findByTrendingTrue(PageRequest.of(page, pageItem));
        logger.info("Performing resetting popular status for ${resetContent.totalPages} pages for ${resetContent.totalElements} items")
        for (x in 1..resetContent.totalPages) {
            logger.info("Performing resetting popular status for batch : $x")
            resetContent = mangaRepository.findByTrendingTrue(PageRequest.of(page, pageItem));
            resetContent.content.map { mangaCompact -> mangaCompact.trending = false }
            mangaRepository.saveAll(resetContent)
        }
        logger.info("Finished resetting popular status for ${resetContent.totalPages} pages for ${resetContent.totalElements} items")

    }

    fun resetMostPopularTag() {
        val page = 1;
        val pageItem = 10;
        var resetContent = mangaRepository.findByMostPopularTrue(PageRequest.of(page, pageItem));
        logger.info("Performing resetting popular status for ${resetContent.totalPages} pages for ${resetContent.totalElements} items")
        for (x in 1..resetContent.totalPages) {
            logger.info("Performing resetting popular status for batch : $x")
            resetContent = mangaRepository.findByMostPopularTrue(PageRequest.of(x, pageItem));
            resetContent.content.map { mangaCompact -> mangaCompact.trending = false }
            mangaRepository.saveAll(resetContent)
        }
        logger.info("Finished resetting popular status for ${resetContent.totalPages} pages for ${resetContent.totalElements} items")

    }
}
