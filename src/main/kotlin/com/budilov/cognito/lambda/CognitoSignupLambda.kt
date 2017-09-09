package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.PropertyFileConverter
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoSignupLambda : RequestHandler<ApiGatewayRequest.Input,
        CognitoSignupLambda.AuthResponse> {

    data class AuthResponse(val statusCode: Int,
                            val body: String)

    val cognito = CognitoService(PropertyFileConverter.readCredentials())

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): AuthResponse? {
        val logger = context?.logger

        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")

        logger?.log("${username} & ${password}")
        var status = 400
        val resultBody = if (username != null && password != null) {
            status = 200

            val signupResponse = cognito.signUp(username = username, password = password)

            cognito.adminConfirmSignUp(username = username)

            Gson().toJson(signupResponse)
        } else {
            logger?.log("Username and password are required")
            "Username and password are required"
        }

        logger?.log("request payload: " + Gson().toJson(request))

        return AuthResponse(status, resultBody)
    }
}