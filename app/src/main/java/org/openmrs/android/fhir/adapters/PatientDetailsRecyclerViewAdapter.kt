/*
 * Copyright 2022-2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmrs.android.fhir

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import org.openmrs.android.fhir.databinding.PatientDetailsCardViewBinding
import org.openmrs.android.fhir.databinding.PatientDetailsHeaderBinding
import org.openmrs.android.fhir.databinding.PatientListItemViewBinding
import org.openmrs.android.fhir.databinding.VisitListItemBinding
import org.openmrs.android.fhir.PatientDetailsVisitItemViewHolder.*

class PatientDetailsRecyclerViewAdapter(
  private val onCreateEncountersClick: () -> Unit,
  private val onEditEncounterClick: (String, String, String) -> Unit,
  private val onEditVisitClick: (String) -> Unit,
) : ListAdapter<PatientDetailData, PatientDetailItemViewHolder>(PatientDetailDiffUtil()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientDetailItemViewHolder {
    return when (ViewTypes.from(viewType)) {
      ViewTypes.HEADER ->
        PatientDetailsHeaderItemViewHolder(
          PatientDetailsCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
      ViewTypes.PATIENT ->
        PatientOverviewItemViewHolder(
          PatientDetailsHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
          onCreateEncountersClick
        )
      ViewTypes.PATIENT_PROPERTY ->
        PatientPropertyItemViewHolder(
          PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
      ViewTypes.OBSERVATION ->
        PatientDetailsObservationItemViewHolder(
          PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
      ViewTypes.CONDITION ->
        PatientDetailsConditionItemViewHolder(
          PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
      ViewTypes.ENCOUNTER ->
        PatientDetailsEncounterItemViewHolder(
          PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
          onEditEncounterClick
        )
      ViewTypes.VISIT ->
        PatientDetailsVisitItemViewHolder(
          VisitListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
          onEditVisitClick
        )
    }
  }

  override fun onBindViewHolder(holder: PatientDetailItemViewHolder, position: Int) {
    val model = getItem(position)
    holder.bind(model)
    if (holder is PatientDetailsHeaderItemViewHolder) return

    holder.itemView.background =
      if (model.firstInGroup && model.lastInGroup) {
        allCornersRounded()
      } else if (model.firstInGroup) {
        topCornersRounded()
      } else if (model.lastInGroup) {
        bottomCornersRounded()
      } else {
        noCornersRounded()
      }
    if (holder is PatientDetailsEncounterItemViewHolder) {
      holder.bind(getItem(position) as PatientDetailEncounter)
    }
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is PatientDetailHeader -> ViewTypes.HEADER
      is PatientDetailOverview -> ViewTypes.PATIENT
      is PatientDetailProperty -> ViewTypes.PATIENT_PROPERTY
      is PatientDetailObservation -> ViewTypes.OBSERVATION
      is PatientDetailCondition -> ViewTypes.CONDITION
      is PatientDetailEncounter -> ViewTypes.ENCOUNTER
      is PatientDetailVisit -> ViewTypes.VISIT
      else -> {
        throw IllegalArgumentException("Undefined Item type")
      }
    }.ordinal
  }

  companion object {
    private const val STROKE_WIDTH = 2f
    private const val CORNER_RADIUS = 10f

    @ColorInt private const val FILL_COLOR = Color.TRANSPARENT

    @ColorInt private const val STROKE_COLOR = Color.GRAY

    fun allCornersRounded(): MaterialShapeDrawable {
      return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
          .setAllCornerSizes(CORNER_RADIUS)
          .setAllCorners(RoundedCornerTreatment())
          .build(),
      ).applyStrokeColor()
    }

    fun topCornersRounded(): MaterialShapeDrawable {
      return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
          .setTopLeftCornerSize(CORNER_RADIUS)
          .setTopRightCornerSize(CORNER_RADIUS)
          .setTopLeftCorner(RoundedCornerTreatment())
          .setTopRightCorner(RoundedCornerTreatment())
          .build(),
      ).applyStrokeColor()
    }

    fun bottomCornersRounded(): MaterialShapeDrawable {
      return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
          .setBottomLeftCornerSize(CORNER_RADIUS)
          .setBottomRightCornerSize(CORNER_RADIUS)
          .setBottomLeftCorner(RoundedCornerTreatment())
          .setBottomRightCorner(RoundedCornerTreatment())
          .build(),
      ).applyStrokeColor()
    }

    fun noCornersRounded(): MaterialShapeDrawable {
      return MaterialShapeDrawable(ShapeAppearanceModel.builder().build()).applyStrokeColor()
    }

    private fun MaterialShapeDrawable.applyStrokeColor(): MaterialShapeDrawable {
      strokeWidth = STROKE_WIDTH
      fillColor = ColorStateList.valueOf(FILL_COLOR)
      strokeColor = ColorStateList.valueOf(STROKE_COLOR)
      return this
    }
  }
}

abstract class PatientDetailItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
  abstract fun bind(data: PatientDetailData)
}

class PatientOverviewItemViewHolder(
  private val binding: PatientDetailsHeaderBinding,
  val onCreateEncountersClick: () -> Unit,
) : PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailOverview).let {
      binding.title.text = it.patient.name
      binding.patientIdValue.text = it.patient.resourceId
      binding.genderValue.text = it.patient.gender
      binding.patientPhoneValue.text = it.patient.phone
      binding.dateOfBirthValue.text = it.patient.dob?.toString() ?: ""
    }
  }
}

class PatientPropertyItemViewHolder(private val binding: PatientListItemViewBinding) :
  PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailProperty).let {
      binding.name.text = it.patientProperty.header
      binding.fieldName.text = it.patientProperty.value
    }
    binding.status.visibility = View.GONE
    binding.id.visibility = View.GONE
  }
}

class PatientDetailsHeaderItemViewHolder(private val binding: PatientDetailsCardViewBinding) :
  PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailHeader).let { binding.header.text = it.header }
  }
}

class PatientDetailsObservationItemViewHolder(private val binding: PatientListItemViewBinding) :
  PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailObservation).let {
      binding.name.text = it.observation.code
      binding.fieldName.text = it.observation.value
    }
    binding.status.visibility = View.GONE
    binding.id.visibility = View.GONE
  }
}

class PatientDetailsEncounterItemViewHolder(
  private val binding: PatientListItemViewBinding,
  private val onEditEncounterClick: (String, String, String) -> Unit
) : PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailEncounter).let {
      val encounter = it.encounter;
      binding.name.text = encounter.type
      binding.fieldName.text = encounter.dateTime
      binding.name.setOnClickListener {
        onEditEncounterClick(  encounter.encounterId ?: "", encounter.formDisplay ?: "", encounter.formResource ?: "")
      }
    }
    binding.status.visibility = View.GONE
    binding.id.visibility = View.GONE
  }
}

class PatientDetailsVisitItemViewHolder(
  private val binding: VisitListItemBinding, // Update to the correct binding class
  private val onEditVisitClick: (String) -> Unit
) : PatientDetailItemViewHolder(binding.root) {

  override fun bind(data: PatientDetailData) {
    (data as PatientDetailVisit).let {
      val visit = it.visit
      binding.encounterType.text = visit.code
      binding.encounterDate.text = visit.getPeriods()
      binding.encounterDate.setOnClickListener {
        onEditVisitClick( visit.id )
      }
    }
  }


class PatientDetailsConditionItemViewHolder(private val binding: PatientListItemViewBinding) :
  PatientDetailItemViewHolder(binding.root) {
  override fun bind(data: PatientDetailData) {
    (data as PatientDetailCondition).let {
      binding.name.text = it.condition.code
      binding.fieldName.text = it.condition.value
    }
    binding.status.visibility = View.GONE
    binding.id.visibility = View.GONE
  }
}

enum class ViewTypes {
  HEADER,
  PATIENT,
  PATIENT_PROPERTY,
  OBSERVATION,
  CONDITION,
  ENCOUNTER,
  VISIT;

  companion object {
    fun from(ordinal: Int): ViewTypes {
      return values()[ordinal]
    }
  }
}

class PatientDetailDiffUtil : DiffUtil.ItemCallback<PatientDetailData>() {
  override fun areItemsTheSame(o: PatientDetailData, n: PatientDetailData) = o == n

  override fun areContentsTheSame(o: PatientDetailData, n: PatientDetailData) = areItemsTheSame(o, n)
}
}
