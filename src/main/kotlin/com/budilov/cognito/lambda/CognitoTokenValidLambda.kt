package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoTokenValidLambda : RequestHandler<ApiGatewayRequest.Input,
        CognitoTokenValidLambda.AuthResponse> {

    data class AuthResponse(val statusCode: Int,
                            val body: String)

    val cognito = CognitoService()

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): AuthResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 400
        val resultBody = if (idToken != null) {
            status = 200
            Gson().toJson(cognito.isTokenValid(idToken))
        } else {
            logger?.log("Username and password are required")
            "Username and password are required"
        }

        logger?.log("request payload: " + Gson().toJson(request))

        return AuthResponse(status, resultBody)
    }
}