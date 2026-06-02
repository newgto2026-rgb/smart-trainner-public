package com.smarttrainner.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.smarttrainner.BuildConfig

internal sealed interface GoogleCredentialResult {
    data class Success(val idToken: String) : GoogleCredentialResult
    data object Cancelled : GoogleCredentialResult
    data object Failed : GoogleCredentialResult
}

@SuppressLint("CredentialManagerSignInWithGoogle")
internal suspend fun requestGoogleCredential(context: Context): GoogleCredentialResult {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val credential = credentialManager.getCredential(
            context = context,
            request = request
        ).credential
        when {
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL ->
                GoogleCredentialResult.Success(
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                )
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ->
                GoogleCredentialResult.Success(
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                )
            else -> GoogleCredentialResult.Failed
        }
    } catch (_: GetCredentialCancellationException) {
        GoogleCredentialResult.Cancelled
    } catch (_: NoCredentialException) {
        GoogleCredentialResult.Cancelled
    } catch (_: GoogleIdTokenParsingException) {
        GoogleCredentialResult.Failed
    } catch (_: GetCredentialException) {
        GoogleCredentialResult.Failed
    }
}
