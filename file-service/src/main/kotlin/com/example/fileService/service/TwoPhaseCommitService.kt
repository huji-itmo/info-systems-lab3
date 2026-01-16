package com.example.fileService.service

import com.example.fileService.model.SpaceMarineImportRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

data class TransactionContext(
    val transactionId: String = UUID.randomUUID().toString(),
    val preparedData: MutableList<PreparedSpaceMarineData> = mutableListOf(),
    var status: TransactionStatus = TransactionStatus.CREATED,
    var rollbackData: MutableList<RollbackData> = mutableListOf(),
)

data class PreparedSpaceMarineData(
    val request: SpaceMarineImportRequest,
    val chapterId: Long?,
    val coordinatesId: Long?,
    val isNewChapter: Boolean = false,
    val isNewCoordinates: Boolean = false,
)

data class RollbackData(
    val entityName: String,
    val entityId: Long?,
    val action: String,
)

enum class TransactionStatus {
    CREATED,
    PREPARING,
    PREPARED,
    COMMITTING,
    COMMITTED,
    ABORTING,
    ABORTED,
}

@Service
class TwoPhaseCommitService(
    private val chapterService: ChapterTransactionService,
    private val coordinatesService: CoordinatesTransactionService,
    private val spaceMarineService: SpaceMarineTransactionService,
) {
    @Transactional
    fun preparePhase(requests: List<SpaceMarineImportRequest>): TransactionContext {
        val context = TransactionContext()
        context.status = TransactionStatus.PREPARING

        try {
            // Phase 1: Prepare all entities
            for (request in requests) {
                val preparedData = prepareSpaceMarineData(request, context)
                context.preparedData.add(preparedData)
            }

            context.status = TransactionStatus.PREPARED
            return context
        } catch (e: Exception) {
            abortTransaction(context)
            throw IllegalStateException("Prepare phase failed: ${e.message}", e)
        }
    }

    @Transactional
    fun commitPhase(context: TransactionContext): TransactionContext {
        if (context.status != TransactionStatus.PREPARED) {
            throw IllegalStateException("Transaction must be in PREPARED state to commit")
        }

        context.status = TransactionStatus.COMMITTING

        try {
            // Phase 2: Commit all prepared entities
            for (preparedData in context.preparedData) {
                commitSpaceMarineData(preparedData, context)
            }

            context.status = TransactionStatus.COMMITTED
            return context
        } catch (e: Exception) {
            abortTransaction(context)
            throw IllegalStateException("Commit phase failed: ${e.message}", e)
        }
    }

    @Transactional
    fun abortTransaction(context: TransactionContext) {
        context.status = TransactionStatus.ABORTING

        try {
            // Rollback in reverse order
            context.rollbackData.reversed().forEach { rollbackData ->
                when (rollbackData.action) {
                    "CREATE_CHAPTER" ->
                        rollbackData.entityId?.let {
                            chapterService.deleteById(it)
                        }
                    "CREATE_COORDINATES" ->
                        rollbackData.entityId?.let {
                            coordinatesService.deleteById(it)
                        }
                    "CREATE_SPACEMARINE" ->
                        rollbackData.entityId?.let {
                            spaceMarineService.deleteById(it)
                        }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Rollback failed: ${e.message}", e)
        } finally {
            context.status = TransactionStatus.ABORTED
        }
    }

    private fun prepareSpaceMarineData(
        request: SpaceMarineImportRequest,
        context: TransactionContext,
    ): PreparedSpaceMarineData {
        // Prepare or validate chapter
        val chapterId =
            when {
                request.chapterId != null -> {
                    if (!chapterService.existsById(request.chapterId!!)) {
                        throw IllegalArgumentException("Chapter with ID ${request.chapterId} not found")
                    }
                    request.chapterId
                }
                request.chapter != null -> {
                    val newChapterId = chapterService.prepareCreate(request.chapter)
                    context.rollbackData.add(RollbackData("Chapter", newChapterId, "CREATE_CHAPTER"))
                    newChapterId
                }
                else -> throw IllegalArgumentException("Either chapterId or chapter must be provided")
            }

        // Prepare or validate coordinates
        val coordinatesId =
            when {
                request.coordinatesId != null -> {
                    if (!coordinatesService.existsById(request.coordinatesId!!)) {
                        throw IllegalArgumentException("Coordinates with ID ${request.coordinatesId} not found")
                    }
                    request.coordinatesId
                }
                request.coordinates != null -> {
                    val newCoordinatesId = coordinatesService.prepareCreate(request.coordinates)
                    context.rollbackData.add(RollbackData("Coordinates", newCoordinatesId, "CREATE_COORDINATES"))
                    newCoordinatesId
                }
                else -> throw IllegalArgumentException("Either coordinatesId or coordinates must be provided")
            }

        return PreparedSpaceMarineData(
            request = request,
            chapterId = chapterId,
            coordinatesId = coordinatesId,
            isNewChapter = request.chapter != null,
            isNewCoordinates = request.coordinates != null,
        )
    }

    private fun commitSpaceMarineData(
        preparedData: PreparedSpaceMarineData,
        context: TransactionContext,
    ) {
        // Commit chapter if it's new
        if (preparedData.isNewChapter && preparedData.request.chapter != null) {
            chapterService.commitCreate(preparedData.request.chapter!!)
        }

        // Commit coordinates if it's new
        if (preparedData.isNewCoordinates && preparedData.request.coordinates != null) {
            coordinatesService.commitCreate(preparedData.request.coordinates!!)
        }

        // Create and commit SpaceMarine
        val spaceMarineId =
            spaceMarineService.create(
                preparedData.request,
                preparedData.chapterId!!,
                preparedData.coordinatesId!!,
            )
        context.rollbackData.add(RollbackData("SpaceMarine", spaceMarineId, "CREATE_SPACEMARINE"))
    }
}
