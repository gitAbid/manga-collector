package core.mangacollector.repository

import core.mangacollector.model.UpdateStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface UpdateStatusRepository : MongoRepository<UpdateStatus, String> {
}