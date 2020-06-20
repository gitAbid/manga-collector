package core.mangacollector.repository

import core.mangacollector.model.Trending
import org.springframework.data.mongodb.repository.MongoRepository

interface TrendingRepository : MongoRepository<Trending, String> {
}