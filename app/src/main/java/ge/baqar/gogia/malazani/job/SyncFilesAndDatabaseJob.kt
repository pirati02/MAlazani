package ge.baqar.gogia.malazani.job

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ge.baqar.gogia.malazani.storage.db.FolkApiDao
import ge.baqar.gogia.storage.usecase.FileSaveController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class SyncFilesAndDatabaseJob(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams), KoinComponent, CoroutineScope {

    override val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val folkApiDao: FolkApiDao by inject()
    private val saveController: FileSaveController by inject()

    override fun doWork(): Result {
        launch {

            val ensembles = folkApiDao.ensembles()
            ensembles.forEach { ensemble ->
                val songs = folkApiDao.songsByEnsembleId(ensemble.referenceId)

                val removalSongs = songs.filter { song ->
                    val associatedFile = saveController.getFile(ensemble.nameEng, "${song.name}.mp3")
                    associatedFile == null
                }.map { it.id }
                folkApiDao.removeSongsByIds(removalSongs)
            }
        }


        val firstWorkRequest = OneTimeWorkRequestBuilder<SyncFilesAndDatabaseJob>()
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueue(firstWorkRequest)

        return Result.success()
    }
}