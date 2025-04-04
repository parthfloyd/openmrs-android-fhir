/*
* BSD 3-Clause License
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice, this
*    list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
*
* 3. Neither the name of the copyright holder nor the names of its
*    contributors may be used to endorse or promote products derived from
*    this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.openmrs.android.fhir.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.extensions.logicalId
import com.google.android.fhir.search.search
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Questionnaire
import org.openmrs.android.fhir.FhirApplication
import org.openmrs.android.fhir.R
import org.openmrs.android.fhir.databinding.CreateEncounterFragmentBinding

class CreateEncountersFragment : Fragment(R.layout.create_encounter_fragment) {
  private var _binding: CreateEncounterFragmentBinding? = null
  private val binding
    get() = _binding!!

  private val args: CreateEncountersFragmentArgs by navArgs()

  @Inject lateinit var fhirEngine: FhirEngine

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (requireActivity().application as FhirApplication).appComponent.inject(this)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = CreateEncounterFragmentBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpActionBar()
    setHasOptionsMenu(true)
    updateArguments()
    onBackPressed()
    loadFormSchemas(view)
  }

  fun loadFormSchemas(view: View) {
    val buttonContainer = view.findViewById<LinearLayout>(R.id.button_container)

    lifecycleScope.launch(Dispatchers.IO) {
      val forms =
        fhirEngine
          .search<Questionnaire> {}
          .filter { questionnaire ->
            questionnaire.resource.logicalId != getString(R.string.registration_questionnaire_name)
          }
      withContext(Dispatchers.Main) {
        for (form in forms) {
          setupFormButton(buttonContainer, form.resource)
        }
      }
    }
  }

  private fun setupFormButton(parentView: ViewGroup, form: Questionnaire) {
    val encounterType =
      form.code.firstOrNull()?.code
        ?: throw IllegalArgumentException("No encounter type provided on questionnaire")
    val button =
      Button(context).apply {
        text = form.title
        setOnClickListener {
          findNavController()
            .navigate(
              CreateEncountersFragmentDirections
                .actionCreateEncounterFragmentToGenericFormEntryFragment(
                  form.title,
                  encounterType,
                  args.patientId,
                  form,
                ),
            )
        }
      }
    parentView.addView(button)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        NavHostFragment.findNavController(this@CreateEncountersFragment).navigateUp()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun setUpActionBar() {
    (requireActivity() as AppCompatActivity).supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
    }
  }

  private fun updateArguments() {
    requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "assessment.json")
  }

  private fun onBackPressed() {}

  companion object {
    const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
    const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
  }
}
