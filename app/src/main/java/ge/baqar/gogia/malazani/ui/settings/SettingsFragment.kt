package ge.baqar.gogia.malazani.ui.settings

import android.content.Context
import android.content.SharedPreferences
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

class SettingsFragment : Fragment() {
    private val preferences: SharedPreferences by lazy {
        context?.getSharedPreferences(context?.packageName, Context.MODE_PRIVATE)!!
    }
    private val storageOptionKey = "storageOption"
    private var binding: FragmentSettingsBinding? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val preferredStorageOption = preferences.getString(
            storageOptionKey,
            StorageOption.ApplicationCache.toString()
        )
        val storageOption = StorageOption.valueOf(
            preferredStorageOption ?: StorageOption.ApplianceStorage.toString()
        )

        if (storageOption == StorageOption.ApplicationCache) {
            binding?.applicationCacheBtn?.isChecked = true
        } else {
            binding?.applianceStorageBtn?.isChecked = true
        }

        binding?.applicationCacheBtn?.setOnClickListener {
            preferences.edit()
                ?.putString(storageOptionKey, StorageOption.ApplicationCache.toString())
                ?.apply()
        }
        binding?.applianceStorageBtn?.setOnClickListener {
            preferences.edit()
                ?.putString(storageOptionKey, StorageOption.ApplianceStorage.toString())
                ?.apply()
        }
        binding?.included?.tabBackImageView?.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding?.root!!
    }
}