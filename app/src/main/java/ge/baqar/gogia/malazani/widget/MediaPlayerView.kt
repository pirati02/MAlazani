package ge.baqar.gogia.malazani.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.databinding.ViewMediaPlayerContainerBinding
import ge.baqar.gogia.model.AutoPlayState

class MediaPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private lateinit var bottomNavigationView: BottomNavigationView

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

    private var animationDuration = 350L
    private var translate: Float = 0F
    var minimized = true

    private var binding: ViewMediaPlayerContainerBinding =
        ViewMediaPlayerContainerBinding.inflate(LayoutInflater.from(context), this, true)
    private var calculatedHeight = 0

    init {
        binding.mediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        binding.expandedMediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        translate = context.resources.getDimension(R.dimen.bottom_navigation_heght)
        initMinimizedMediaPlayerListeners()
        initMaximizedMediaPlayerListeners()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculatedHeight = h
    }

    private fun initMaximizedMediaPlayerListeners() {
        binding.expandedMediaPlayerView.playerViewCloseBtn.setOnClickListener {
            minimize()
        }

        binding.expandedMediaPlayerView.playerPlaylistButton.setOnClickListener {
            openPlayListListener?.invoke()
        }

        binding.expandedMediaPlayerView.playStopButton.setOnClickListener {
            onStop?.invoke()
            minimize()
            hide()
        }

        binding.expandedMediaPlayerView.playNextButton.setOnClickListener {
            onNext?.invoke()
        }

        binding.expandedMediaPlayerView.playPrevButton.setOnClickListener {
            onPrev?.invoke()
        }

        binding.expandedMediaPlayerView.playPauseButton.setOnClickListener {
            onPlayPause?.invoke()
        }

        binding.expandedMediaPlayerView.favBtn.setOnClickListener {
            setFabButtonClickListener?.invoke()
        }

        binding.expandedMediaPlayerView.playerProgressBar.setOnSeekBarChangeListener(object :
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

    private fun initMinimizedMediaPlayerListeners() {
        binding.mediaPlayerView.playerViewCloseBtn.setOnClickListener {
            setOnCloseListener?.invoke()
            if (minimized) {
                maximize()
            } else {
                minimize()
            }
        }

        binding.mediaPlayerView.favBtn.setOnClickListener {
            setFabButtonClickListener?.invoke()
        }

        binding.mediaPlayerView.playerAutoPlayButton.setOnClickListener {
            onAutoPlayChanged?.invoke()
        }

        binding.mediaPlayerView.playPauseButton.setOnClickListener {
            onPlayPause?.invoke()
        }
    }

    fun setTrackTitle(title: String) {
        binding.mediaPlayerView.playingTrackTitle.text = title
    }

    fun setIsFav(isFav: Boolean) {
        post {
            if (isFav) {
                binding.expandedMediaPlayerView.favBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
                binding.mediaPlayerView.favBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                binding.expandedMediaPlayerView.favBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                binding.mediaPlayerView.favBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }
    }

    fun setAutoPlayState(autoPlayState: Int) {
        post {
            when (autoPlayState) {
                AutoPlayState.OFF -> {
                    binding.mediaPlayerView.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24_off)
                }
                AutoPlayState.REPEAT_ALBUM -> {
                    binding.mediaPlayerView.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_24_on)
                }
                AutoPlayState.REPEAT_ONE -> {
                    binding.mediaPlayerView.playerAutoPlayButton.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                }
            }
        }
    }

    fun setDuration(durationString: String?, duration: Int) {
        post {
            binding.expandedMediaPlayerView.playingTrackDurationTime.text = durationString
            binding.expandedMediaPlayerView.playerProgressBar.max = duration
        }
    }

    fun setProgress(time: String?, progress: Int) {
        binding.expandedMediaPlayerView.playerProgressBar.post {
            binding.expandedMediaPlayerView.playingTrackTime.text = time
            binding.expandedMediaPlayerView.playerProgressBar.progress = progress
        }
    }

    fun setCurrentDuration(durationString: String?) {
        binding.expandedMediaPlayerView.playingTrackTime.text = durationString
    }

    fun isPlaying(it: Boolean) {
        post {
            if (it) {
                binding.expandedMediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                binding.mediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                binding.mediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                binding.expandedMediaPlayerView.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
    }

    private fun maximize() {
        minimized = false
        post {
            binding.expandedMediaPlayerViewContainer.animate()
                .setDuration(animationDuration)
                .alpha(1f)
                .start()
            binding.mediaPlayerViewContainer.animate()
                .setDuration(animationDuration)
                .alpha(0f)
                .start()
            bottomNavigationView.animate()
                .setDuration(animationDuration)
                .translationY(translate)
                .start()
        }
    }

    fun minimize() {
        minimized = true
        post {
            binding.expandedMediaPlayerViewContainer.animate()
                .setDuration(animationDuration)
                .alpha(0f)
                .start()

            binding.mediaPlayerViewContainer.animate()
                .setDuration(animationDuration)
                .alpha(1f)
                .start()

            bottomNavigationView.animate()
                .setDuration(animationDuration)
                .translationY(0F)
                .start()
        }
    }

    fun show() {
        binding.mediaPlayerViewContainer.animate()
            .setDuration(1)
            .alpha(1f)
            .start()
    }

    fun hide() {
        binding.mediaPlayerViewContainer.animate()
            .setDuration(animationDuration)
            .alpha(0f)
            .start()
    }

    fun setupWithBottomNavigation(navView: BottomNavigationView) {
        bottomNavigationView = navView
    }
}