package ge.baqar.gogia.model.events

import ge.baqar.gogia.model.DownloadableSong

data class SongsMarkedAsFavourite(val songs: MutableList<DownloadableSong>)

data class SongsUnmarkedAsFavourite(val songs: MutableList<DownloadableSong>)

