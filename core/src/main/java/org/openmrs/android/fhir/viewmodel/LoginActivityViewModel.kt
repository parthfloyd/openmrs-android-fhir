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
package org.openmrs.android.fhir.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import org.openmrs.android.fhir.LoginRepository
import timber.log.Timber

class LoginActivityViewModel @Inject constructor(private val applicationContext: Context) :
  ViewModel() {
  private val loginRepository by lazy { LoginRepository.getInstance(applicationContext) }

  suspend fun createIntent(): Intent? {
    loginRepository.updateAuthIfConfigurationChanged()
    loginRepository.initializeAppAuth()
    return loginRepository.getAuthIntent()
  }

  fun getLastConfigurationError(): AuthorizationException? {
    return loginRepository.getLastConfigurationError()
  }

  suspend fun isAuthAlreadyEstablished() = loginRepository.isAuthEstablished()

  suspend fun handleLoginResponse(response: AuthorizationResponse?, ex: AuthorizationException?) {
    if (response != null || ex != null) {
      loginRepository.updateAfterAuthorization(response, ex)
    }
    when {
      response?.authorizationCode != null -> {
        loginRepository.exchangeCodeForToken(response, ex)
      }
      ex != null -> {
        Timber.e("Authorization flow failed: " + ex.message)
      }
      else -> {
        Timber.e("No authorization state retained - reauthorization required")
      }
    }
  }
}
