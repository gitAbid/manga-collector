package core.mangacollector.repository

import core.mangacollector.model.MostPopular
import org.springframework.data.mongodb.repository.MongoRepository

interface MostPopularRepository : MongoRepository<MostPopular, String> {
}