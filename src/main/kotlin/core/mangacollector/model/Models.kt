package core.mangacollector.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*
import kotlin.collections.LinkedHashSet


@Document
data class LatestMangaUpdate(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var cover: String,
        var description: String,
        var mangaUrl: String,
        var latestChapter: String,
        var viewCount: Long,
        var lastUpdated: Date = Date(),
        var lastChapterUpdated: Date = Date(),
        @Field("isChapterUpdated")
        var chapterUpdated: Boolean = false)

@Document
data class Manga(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var cover: String,
        var description: String,
        var mangaUrl: String,
        var latestChapter: String,
        var viewCount: Long,
        var authors: LinkedHashSet<String>,
        var status: String,
        var genres: LinkedHashSet<String>,
        var rating: Double?,
        var trending: Boolean = false,
        var mostPopular: Boolean = false,
        var chapters: LinkedHashSet<Chapter>,
        var mangaLastUpdated: String,
        var lastUpdated: Date = Date(),
        var lastChapterUpdated: Date = Date())

@Document
data class Chapter(
        @Id
        var id: String = ObjectId().toHexString(),
        var chapterName: String,
        var chapterViewCount: Long = 0L,
        var chapterImages: LinkedHashSet<String> = LinkedHashSet(),
        var lastUpdated: Date = Date(),
        var chapterAdded: String = "",
        var chapterLink: String
)

@Document
data class UpdateStatus(var id: String = ObjectId().toHexString(),
                        var mangaUrl: String, var updatedOn: Date = Date(), var lastChapter: String)

data class Trending(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var mangaId: String
)

data class MostPopular(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var mangaId: String
)

data class Genres(@Id val genreName: String)

@Document(value = "manga")
data class MangaCompact(
        @Id
        var id: String = ObjectId().toHexString(),
        var mangaName: String,
        var cover: String,
        var description: String,
        var mangaUrl: String,
        var latestChapter: String,
        var viewCount: Long,
        var authors: LinkedHashSet<String>,
        var status: String,
        var genres: LinkedHashSet<String>,
        var rating: Double?,
        var trending: Boolean = false,
        var mostPopular: Boolean = false,
        var mangaLastUpdated: String,
        var lastUpdated: Date = Date(),
        var lastChapterUpdated: Date = Date())
