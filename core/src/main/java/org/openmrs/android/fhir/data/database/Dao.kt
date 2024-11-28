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
package org.openmrs.android.fhir.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.openmrs.android.fhir.data.database.model.Identifier
import org.openmrs.android.fhir.data.database.model.IdentifierType

@Dao
interface Dao {

  @Query("SELECT * from identifier WHERE identifierTypeUUID=:identifierTypeUUID LIMIT 1")
  suspend fun getOneIdentifierByType(identifierTypeUUID: String): Identifier?

  @Query("SELECT * from IdentifierType WHERE uuid=:identifierTypeUUID LIMIT 1")
  suspend fun getIdentifierTypeById(identifierTypeUUID: String): IdentifierType?

  @Query("SELECT * FROM identifiertype") suspend fun getAllIdentifierTypes(): List<IdentifierType>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllIdentifierModel(identifiers: List<Identifier>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllIdentifierTypeModel(identifierTypes: List<IdentifierType>)

  @Query("SELECT COUNT(*) FROM identifier WHERE identifierTypeUUID=:identifierTypeId")
  suspend fun getIdentifierCountByTypeId(identifierTypeId: String): Int

  @Query("SELECT COUNT(*) FROM IdentifierType") suspend fun getIdentifierTypesCount(): Int

  @Delete suspend fun delete(identifier: Identifier)

  @Query("DELETE FROM identifier WHERE value = :identifierValue ")
  suspend fun deleteIdentifierByValue(identifierValue: String)
}
