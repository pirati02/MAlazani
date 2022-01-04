package ge.baqar.gogia.malazani.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ViewMediaPlayerBinding
import ge.baqar.gogia.model.AutoPlayState

class MediaPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var seeking: Boolean = false
    var onAutoPlayChanged: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
    var onPrev: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onPlayPause: (() -> Unit)? = null
    var setFabButtonClickListener: (() -> Unit)? = null
    var openPlayListListener: (() -> Unit)? = null
    var setOnCloseListener: (() -> Unit)? = null
    var setSeekListener: ((Int) -> Unit)? = null
    private var binding: ViewMediaPlayerBinding

    init {
        inflate(context, R.layout.view_media_player, this)
        binding = ViewMediaPlayerBinding.bind(this)
        binding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)

        binding.playerViewCloseBtn.setOnClickListener {
            setOnCloseListener?.invoke()
        }
        binding.playerPlaylistButton.setOnClickListener {
            openPlayListListener?.invoke()
        }
        binding.favBtn.setOnClickListener {
            setFabButtonClickListener?.invoke()
        }
        binding.playStopButton.setOnClickListener {
            onStop?.invoke()
        }
        binding.playNextButton.setOnClickListener {
            onNext?.invoke()
        }
        binding.playPrevButton.setOnClickListener {
            onPrev?.invoke()
        }
        binding.playPauseButton.setOnClickListener {
            onPlayPause?.invoke()
        }
        binding.playerAutoPlayButton.setOnClickListener {
            onAutoPlayChanged?.invoke()
        }

        binding.playerProgressBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (seeking)
                    setSeekListener?.invoke(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                seeking = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                seeking = false
            }

        })
    }

    fun setTrackTitle(title: String) {
        binding.playingTrackTitle.text = title
    }

    fun setIsFav(isFav: Boolean) {
        post {
            if (isFav) {
                binding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                binding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }
    }

    fun setAutoPlayState(autoPlayState: Int) {
        post {
            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    binding.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24_off)
                }
                AutoPlayState.REPEAT_ONE -> {
                    binding.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    binding.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24_on)
                }
            }
        }
    }

    fun setDuration(durationString: String?, duration: Int) {
        post {
            binding.playingTrackDurationTime.text = durationString
            binding.playerProgressBar.max = duration
        }
    }

    fun setProgress(time: String?, progress: Int) {
        binding.playerProgressBar.post {
            binding.playingTrackTime.text = time
            binding.playerProgressBar.progress = progress
        }
    }

    fun setCurrentDuration(durationString: String?) {
        binding.playingTrackTime.text = durationString
    }

    fun isPlaying(it: Boolean) {
        post {
            if (it) {
                binding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                binding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
    }

    fun maximize() {
        binding.playerControlsView.visibility = View.VISIBLE
        binding.playerViewCloseBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
    }

    fun minimize() {
        binding.playerControlsView.visibility = View.GONE
        binding.playerViewCloseBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
    }
}