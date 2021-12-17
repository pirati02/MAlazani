package ge.baqar.gogia.malazani.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ge.baqar.gogia.malazani.databinding.FragmentSettingsBinding
import ge.baqar.gogia.malazani.poko.StorageOption
import ge.baqar.gogia.malazani.storage.prefs.FolkAppPreferences
import org.koin.android.ext.android.inject

class SettingsFragment : Fragment() {

    private val folkAppPreferences: FolkAppPreferences by inject()
    private var binding: FragmentSettingsBinding? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val storageOption = folkAppPreferences.getStorageOption()
        if (storageOption == StorageOption.ApplicationCache) {
            binding?.applicationCacheBtn?.isChecked = true
        } else {
            binding?.applianceStorageBtn?.isChecked = true
        }

        binding?.applicationCacheBtn?.setOnClickListener {
            folkAppPreferences.updateStorageOption(StorageOption.ApplicationCache)
        }
        binding?.applianceStorageBtn?.setOnClickListener {
            folkAppPreferences.updateStorageOption(StorageOption.ApplianceStorage)
        }
        binding?.included?.tabBackImageView?.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding?.root!!
    }
}