package com.budilov.cognito.services.cognito

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.budilov.cognito.Properties
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.*
import java.net.URL
import java.security.interfaces.RSAKey

data class AuthServiceResult(val successful: Boolean = true,
                             val result: Any? = null,
                             val errorMessage: String? = null,
                             val errorType: String? = null)

/**
 * Created by Vladimir Budilov
 *
 * The API is documented here:
 * http://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_Operations.html
 *
 */
class CognitoService {


    private val logger = LoggerFactory.getLogger("CognitoService")
    private val cognitoUPClient: CognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
            .region(Region.of(Properties.regionName))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()

    @Throws(Exception::class)
    fun isTokenValid(token: String): Boolean {

        // Decode the key and set the kid
        val decodedJwtToken = JWT.decode(token)
        val kid = decodedJwtToken.keyId

        val http = UrlJwkProvider(URL(Properties.jwksUrl))
        // Let's cache the result from Cognito for the default of 10 hours
        val provider = GuavaCachedJwkProvider(http)
        val jwk = provider.get(kid)

        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAKey)
        val verifier = JWT.require(algorithm)
                .withIssuer(Properties.jwtTokenIssuer)
                .build() //Reusable verifier instance
        val jwt = try {
            verifier.verify(token)
        } catch (e: Exception) {
            false
        }

        return (jwt != null)
    }

    fun getUsername(idToken: String): String {
        return JWT.decode(idToken).getClaim("cognito:username").asString()
    }

    /**
     * Signs in a user without SRP with the provided [username] and [password]
     *
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html
     *
     * @return the auth payload or throws an Exception
     */
    fun signInNoSRP(username: String, password: String): AuthServiceResult {
        val authParametersMap = mutableMapOf("USERNAME" to username, "PASSWORD" to password)
        val authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParametersMap)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminInitiateAuth(authRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    /**
     *
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html
     */
    fun adminRefreshTokens(refreshToken: String): AuthServiceResult {
        val authParametersMap = mutableMapOf("REFRESH_TOKEN" to refreshToken)
        logger.info("map: ${authParametersMap}")
        val authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .authParameters(authParametersMap)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminInitiateAuth(authRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }


    fun signUp(username: String, password: String): AuthServiceResult {
        logger.debug("entering function")
        val attr = AttributeType.builder().name("email").value(username).build()

        val signUpRequest = SignUpRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .username(username)
                .password(password)
                .userAttributes(attr)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.signUp(signUpRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminConfirmSignUp(username: String): AuthServiceResult {
        val confirmSignupRequest = AdminConfirmSignUpRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminConfirmSignUp(confirmSignupRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }


    fun adminResetPassword(username: String): AuthServiceResult {
        val request = AdminResetUserPasswordRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminResetUserPassword(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminDeleteUser(username: String): AuthServiceResult {
        val request = AdminDeleteUserRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminDeleteUser(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }


}

fun main(args: Array<String>) {
    val username = "vladimirbudilov4442@budilov.com"
    val password = "SomethingInteresting23!"
    val service = CognitoService()

    println("signup body: " + service.signUp(username = username, password = password))
    println("confirmSignUp body: " + service.adminConfirmSignUp(username = username))
    println("signIn body: " + service.signInNoSRP(username = username, password = password))
    println("deleteUser body: " + service.adminDeleteUser(username = username))
}