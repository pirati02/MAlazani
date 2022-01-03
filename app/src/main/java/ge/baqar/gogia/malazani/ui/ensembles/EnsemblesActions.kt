package ge.baqar.gogia.malazani.ui.ensembles

import ge.baqar.gogia.model.Ensemble


//Actions
open class EnsemblesAction
data class EnsemblesLoaded(val artists: MutableList<Ensemble>) : EnsemblesAction()
class EnsemblesRequested : EnsemblesAction()
class OldRecordingsRequested : EnsemblesAction()