package core.mangacollector.repository

import core.mangacollector.model.MostPopular
import core.mangacollector.model.Trending
import org.springframework.data.mongodb.repository.MongoRepository

interface MostPopularRepository : MongoRepository<MostPopular, String> {
}