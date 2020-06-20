package core.mangacollector.repository

import core.mangacollector.model.Genres
import org.springframework.data.mongodb.repository.MongoRepository

interface GenresRepository : MongoRepository<Genres, String> {
}