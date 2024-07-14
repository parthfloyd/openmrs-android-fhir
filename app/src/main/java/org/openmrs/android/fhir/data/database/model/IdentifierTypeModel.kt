package org.openmrs.android.fhir.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IdentifierTypeModel(
    @PrimaryKey val uuid: String,
    val display: String?,
    val isUnique: String,
) {
}